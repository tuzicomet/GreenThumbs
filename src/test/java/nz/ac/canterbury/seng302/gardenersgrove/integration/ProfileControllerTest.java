package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Contractor;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Location;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.FileSizeException;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.ImageTypeException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.FileValidation;
import nz.ac.canterbury.seng302.gardenersgrove.validation.UserInformationValidator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private FileService fileService;
    @MockBean
    private UserInformationValidator userInformationValidator;
    @MockBean 
    private FriendshipService friendshipService;
    @MockBean
    LocationService locationService;

    @MockBean
    private UserRepository userRepository;
    // GardenRepository is required by GlobalControllerAdvice
    @MockBean
    private GardenRepository gardenRepository;
    @Autowired
    private MessageSource messageSource;
    private static MockedStatic<FileValidation> fileValidation;

    private SecurityContext securityContext;




    ProfileController controller;
    ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

    private final List<String> VALID_IMAGE_PATHS = Arrays.asList(
            "/user_uploads/path.jpg"
    );
    final String VALID_DESCRIPTION = "Test";



    @BeforeAll
     static void staticSetup() {
        fileValidation = Mockito.mockStatic(FileValidation.class);

    }

    @AfterAll
    static void cleanUp() {
        fileValidation.close();
    }
    @BeforeEach
    void setup() throws ImageTypeException, FileSizeException {
        Authentication authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);

        User user = new User(
                "Mock",
                "User",
                "test@gmail.com",
                "+Password123",
                "2000-1-1",
                null
        );
        user.setUserId(1L);

        User updatedUser = new User(
                "new",
                "user",
                "new@gmai.com",
                "+Password123",
                "2000-01-02",
                null
        );

        Path path = Path.of("/valid/path.jpg");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userService.getUserFromAuthentication(Mockito.any(Authentication.class))).thenReturn(user);
        when(userService.getUserByUserId(Mockito.any())).thenReturn(Optional.of(user));
        when(userService.updateUserDetails(Mockito.any())).thenReturn(updatedUser);
        // Mock fileService behavior
        when(fileService.addFile(Mockito.any(MultipartFile.class), Mockito.anyString())).thenReturn(Optional.of(path));
        when(fileService.uploadFiles(anyList())).thenReturn(List.of("/user_uploads/path.jpg"));

        // Mock userService behavior
        doNothing().when(userService).updateUserImage(Mockito.anyLong(), Mockito.anyString());


        SecurityContextHolder.setContext(securityContext);


        this.userInformationValidator = new UserInformationValidator(userService);
        this.controller = new ProfileController(userRepository, userService, userInformationValidator, fileService, friendshipService, messageSource, locationService);
    }

    @Test
    void getContractorRegistrationPage_StatusOK() throws Exception {
        mockMvc.perform(get("/profile/contractor"))
                .andExpect(status().isOk())
                .andExpect(view().name("contractorRegisterTemplate"));
    }

    @Test
    void contractorRegistration_ValidLocation_RedirectToProfile() throws Exception {
        Map<String, Object> mockCoordinates = Map.of("lon", 171.7246, "lat", -43.6341);
        when(locationService.fetchCoordinate(any(Location.class))).thenReturn(mockCoordinates);

        mockMvc.perform(multipart("/profile/contractor")
                        .file("validUpload", new byte[0])
                        .with(csrf())
                        .param("description", VALID_DESCRIPTION)
                        .param("location", "Methven 7730, New Zealand")
                        .param("country", "New Zealand")
                        .param("city", "Ashburton District")
                        .param("suburb", "")
                        .param("street", "")
                        .param("postcode", "7730"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
        verify(locationService).saveLocation(locationCaptor.capture());
        Location capturedLocation = locationCaptor.getValue();

        assertEquals("Methven 7730, New Zealand", capturedLocation.getFormatted());
        assertEquals("New Zealand", capturedLocation.getCountry());
        assertEquals("Ashburton District", capturedLocation.getCity());
        assertEquals("", capturedLocation.getSuburb());
        assertEquals("", capturedLocation.getStreet());
        assertEquals("7730", capturedLocation.getPostcode());
        assertEquals(mockCoordinates.get("lon"), capturedLocation.getLon());
        assertEquals(mockCoordinates.get("lat"), capturedLocation.getLat());
    }


    @Test
    void contractorRegistration_InvalidLocation_StayOnFormWithError() throws Exception {
        when(locationService.fetchCoordinate(any(Location.class))).thenReturn(null);

        mockMvc.perform(multipart("/profile/contractor")
                        .file("validUpload", new byte[0])
                        .with(csrf())
                        .param("description", VALID_DESCRIPTION)
                        .param("location", "Invalid Location")
                        .param("country", "Invalid Country")
                        .param("city", "Invalid City")
                        .param("suburb", "")
                        .param("street", "")
                        .param("postcode", ""))
                .andExpect(view().name("contractorRegisterTemplate"))
                .andExpect(model().attributeExists("locationError"));
        // If location has an error, we should stay on the contractorRegisterTemplate, but with the location error

        // check that location is not saved, and user is not converted to a contractor
        verify(locationService, never()).saveLocation(any(Location.class));
        verify(userService, never()).convertUserToContractor(any(User.class), anyString(), anyList(), any(Location.class));
    }


    @Test
    void GetProfile_StatusOK() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/activate"));
    }

    @Test
    void GetProfile_UnauthenticatedUser_StatusForbidden() throws Exception {
        //override authentication behaviour for this test case
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        User user = new User(
                "Mock",
                "User",
                "test@gmail.com",
                "+Password123",
                "2000-1-1",
                null
        );



        Mockito.when(
                        securityContext.getAuthentication())
                .thenReturn(authentication);
        Mockito.when(
                        authentication.getPrincipal())
                .thenReturn(user);
        Mockito.when(
                        authentication.isAuthenticated())
                .thenReturn(false);
        Mockito.when(
                    userService.getUserByUserId(Mockito.any()))
                .thenReturn(Optional.of(user));
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void CancelEdit_EditModeDisabled() throws Exception {
        mockMvc.perform(post("/profile")
            .with(csrf())
            .param("edit", "true")
            .param("cancel", "cancel"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void SaveChanges_ValidUser_UserUpdated() throws Exception {
        mockMvc.perform(post("/profile")
                .with(csrf())
                .param("edit", "true")
                .param("submit", "Submit")
                .param("firstName", "new")
                .param("lastName", "user")
                .param("email", "new@gmai.com")
                .param("dateOfBirth", "02/01/2000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(userService).updateUserDetails(userArgumentCaptor.capture());
    }

    @Test
    void SaveChanges_ValidUserNoSurname_UserUpdated() throws Exception {
        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("edit", "true")
                        .param("submit", "Submit")
                        .param("firstName", "new")
                        .param("noSurname", "true")
                        .param("email", "new@gmai.com")
                        .param("dateOfBirth", "02/01/2000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(userService).updateUserDetails(userArgumentCaptor.capture());
    }

    @Test
    void SaveChanges_ValidUserEmptyDoB_UserUpdated() throws Exception {
        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("edit", "true")
                        .param("submit", "Submit")
                        .param("firstName", "new")
                        .param("lastName", "user")
                        .param("email", "new@gmai.com")
                .param("dateOfBirth", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(userService).updateUserDetails(userArgumentCaptor.capture());
    }

    @Test
    void SaveChanges_InvalidName_UserNotUpdated() throws Exception {
        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("edit", "true")
                        .param("submit", "Submit")
                        .param("firstName", "123")
                        .param("lastName", "user")
                        .param("email", "new@gmai.com")
                        .param("dateOfBirth", "02/01/2000"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("edit", true));

        verify(userService, Mockito.never()).updateUserDetails(Mockito.any());
    }

    @Test
    void SaveChanges_InvalidEmail_UserNotUpdated() throws Exception {
        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("edit", "true")
                        .param("submit", "Submit")
                        .param("firstName", "new")
                        .param("lastName", "user")
                        .param("email", "notAnEmail")
                        .param("dateOfBirth", "02/01/2000"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("edit", true));

        verify(userService, Mockito.never()).updateUserDetails(Mockito.any());
    }

    @Test
    void SaveChanges_InvalidDoB_UserNotUpdated() throws Exception {
        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("edit", "true")
                        .param("submit", "Submit")
                        .param("firstName", "new")
                        .param("lastName", "user")
                        .param("email", "new@gmai.com")
                        .param("dateOfBirth", "NotADate"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("edit", true));

        verify(userService, Mockito.never()).updateUserDetails(Mockito.any());
    }

    @Test
    void saveProfilePic_ValidPicture_PictureSaved() throws Exception {
        fileValidation.when(() -> FileValidation.validateImage(Mockito.any(MultipartFile.class)))
                .thenAnswer(invocation -> null);

        MockMultipartFile mockImg = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/png",
                new byte[0]
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/profile")
                        .file(mockImg)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(userService).updateUserImage(Mockito.anyLong(), Mockito.anyString());
        verify(fileService).addFile(Mockito.any(MultipartFile.class), Mockito.anyString());
    }

    @Test
    void saveProfilePic_InvalidImageType_PictureSaved() throws Exception {
        MockMultipartFile mockImg = new MockMultipartFile(
                "file",
                "test.pdf",
                "image/png",
                new byte[0]
        );

        fileValidation.when(() -> FileValidation.validateImage(Mockito.any(MultipartFile.class)))
                        .thenThrow(new ImageTypeException());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/profile")
                .file(mockImg)
                .with(csrf()))
                .andExpect(MockMvcResultMatchers.flash().attribute("error", "Image must be of type png, jpg, or svg"))
                .andExpect(redirectedUrl("/profile"));

        verify(userService, Mockito.never()).updateUserImage(Mockito.anyLong(), Mockito.anyString());
        verify(fileService, Mockito.never()).addFile(Mockito.any(MultipartFile.class), Mockito.anyString());
    }


    @Test
    void saveProfilePic_InvalidImageSize_PictureSaved() throws Exception {
        MockMultipartFile mockImg = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                new byte[999]
        );

        fileValidation.when(() -> FileValidation.validateImage(Mockito.any(MultipartFile.class)))
                .thenThrow(new FileSizeException());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/profile")
                        .file(mockImg)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.flash().attribute("error", "Image must be less than 10MB"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(userService, Mockito.never()).updateUserImage(Mockito.anyLong(), Mockito.anyString());
        verify(fileService, Mockito.never()).addFile(Mockito.any(MultipartFile.class), Mockito.anyString());
    }


    @Test
    void saveProfilePic_NoImage_PictureSaved() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/profile")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.flash().attribute("error", "Error with upload"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(userService, Mockito.never()).updateUserImage(Mockito.anyLong(), Mockito.anyString());
        verify(fileService, Mockito.never()).addFile(Mockito.any(MultipartFile.class), Mockito.anyString());
    }

    @Test
    void getContractorRegistrationForm_AsUser_ExpectStatusOK() throws Exception {
        mockMvc.perform(get("/profile/contractor")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void getContractorRegistrationForm_AsContractor_ExpectRedirection() throws Exception {
        Contractor contractor = new Contractor();
        when(userService.getUserFromAuthentication(Mockito.any(Authentication.class))).thenReturn(contractor);

        mockMvc.perform(get("/profile/contractor")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void registerContractor_ValidRegistration_ContractorRegistered() throws Exception {
        Contractor contractor = new Contractor();
        Optional<Contractor> optionalContractor = Optional.of(contractor);
        when(userService.getContractorByUserId(any())).thenReturn(optionalContractor);

        Map<String, Object> mockCoordinates = Map.of("lon", 171.7246, "lat", -43.6341);
        when(locationService.fetchCoordinate(any(Location.class))).thenReturn(mockCoordinates);


        mockMvc.perform(multipart("/profile/contractor")
                        .file("validUpload", new byte[0])
                        .with(csrf())
                        .param("description", VALID_DESCRIPTION)
                        .param("location", "Methven 7730, New Zealand")
                        .param("country", "New Zealand")
                        .param("city", "Ashburton District")
                        .param("suburb", "")
                        .param("street", "")
                        .param("postcode", "7730")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(userService).convertUserToContractor(any(), any(), any(), any());
        verify(securityContext).setAuthentication(any());
    }

    @Test
    void registerContractor_AsContractor_Redirected() throws Exception {
        Contractor contractor = new Contractor();
        when(userService.getUserFromAuthentication(Mockito.any(Authentication.class))).thenReturn(contractor);

        mockMvc.perform(multipart("/profile/contractor")
                        .file("validUpload", new byte[0])
                        .with(csrf())
                        .param("description", VALID_DESCRIPTION)
                        .param("location", "Methven 7730, New Zealand")
                        .param("country", "New Zealand")
                        .param("city", "Ashburton District")
                        .param("suburb", "")
                        .param("street", "")
                        .param("postcode", "7730")
                )
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void registerContractor_NoDescription_ModelHasEmptyError() throws Exception {
        mockMvc.perform(multipart("/profile/contractor")
                        .file("validUpload", new byte[0])
                        .with(csrf())
                        .param("description", "")
                        .param("location", "Methven 7730, New Zealand")
                        .param("country", "New Zealand")
                        .param("city", "Ashburton District")
                        .param("suburb", "")
                        .param("street", "")
                        .param("postcode", "7730")
                )
                .andExpect(view().name("contractorRegisterTemplate"))
                .andExpect(model().attribute("errorDescription", "You must provide a description"));
    }

    @Test
    void registerContractor_DescriptionTooLong_ModelHasTooLongError() throws Exception {
        mockMvc.perform(multipart("/profile/contractor")
                        .file("validUpload", new byte[0])
                        .with(csrf())
                        .param("description", "A".repeat(1025))
                        .param("location", "Methven 7730, New Zealand")
                        .param("country", "New Zealand")
                        .param("city", "Ashburton District")
                        .param("suburb", "")
                        .param("street", "")
                        .param("postcode", "7730")
                )
                .andExpect(view().name("contractorRegisterTemplate"))
                .andExpect(model().attribute("errorDescription", "Description must be 1024 characters or fewer"));
    }

    @Test
    void registerContractor_DescriptionOnLengthBoundary_InputIsAccepted() throws Exception {
        Map<String, Object> mockCoordinates = Map.of("lon", 171.7246, "lat", -43.6341);
        when(locationService.fetchCoordinate(any(Location.class))).thenReturn(mockCoordinates);

        mockMvc.perform(multipart("/profile/contractor")
                        .file("validUpload", new byte[0])
                        .with(csrf())
                        .param("description", "A".repeat(1024))
                        .param("location", "Methven 7730, New Zealand")
                        .param("country", "New Zealand")
                        .param("city", "Ashburton District")
                        .param("suburb", "")
                        .param("street", "")
                        .param("postcode", "7730")
                )
                .andExpect(view().name("redirect:/profile"));
    }

    @Test
    void contractorRegistration_DescriptionAndLocationInvalid_BothErrorsShown() throws Exception {
        when(locationService.fetchCoordinate(any(Location.class))).thenReturn(null);

        mockMvc.perform(multipart("/profile/contractor")
                        .file("validUpload", new byte[0])
                        .with(csrf())
                        .param("description", "")
                        .param("location", "Invalid Location")
                        .param("country", "Invalid Country")
                        .param("city", "Invalid City")
                        .param("suburb", "")
                        .param("street", "")
                        .param("postcode", ""))
                .andExpect(view().name("contractorRegisterTemplate"))
                // check both errors are there
                .andExpect(model().attribute("errorDescription", "You must provide a description"))
                .andExpect(model().attributeExists("locationError"));

        // Also check that location is not saved, and user is not converted to a contractor
        verify(locationService, never()).saveLocation(any(Location.class));
        verify(userService, never()).convertUserToContractor(any(User.class), anyString(), anyList(), any(Location.class));
    }

    @Test
    void contractorRegistration_MoreThanFiveImages_ErrorPresent() throws Exception{
        MockMultipartFile file1 = new MockMultipartFile("validUpload", "file1.jpg", "image/jpeg", new byte[5]);
        MockMultipartFile file2 = new MockMultipartFile("validUpload", "file2.jpg", "image/jpeg", new byte[5]);
        MockMultipartFile file3 = new MockMultipartFile("validUpload", "file3.jpg", "image/jpeg", new byte[5]);
        MockMultipartFile file4 = new MockMultipartFile("validUpload", "file4.jpg", "image/jpeg", new byte[5]);
        MockMultipartFile file5 = new MockMultipartFile("validUpload", "file5.jpg", "image/jpeg", new byte[5]);
        MockMultipartFile file6 = new MockMultipartFile("validUpload", "file5.jpg", "image/jpeg", new byte[5]);

        mockMvc.perform(multipart("/profile/contractor")
                        .file(file1)
                        .file(file2)
                        .file(file3)
                        .file(file4)
                        .file(file5)
                        .file(file6)
                        .with(csrf())
                        .param("description", VALID_DESCRIPTION)
                        .param("location", "Methven 7730, New Zealand")
                        .param("country", "New Zealand")
                        .param("city", "Ashburton District")
                        .param("suburb", "")
                        .param("street", "")
                        .param("postcode", "7730")
                )
                .andExpect(view().name("contractorRegisterTemplate"))
                .andExpect(model().attribute("errorImagesMoreThanFive", "You can not submit more than 5 images"));
    }

    @Test
    void contractorRegistration_InvalidImagesFormat_ErrorPresent() throws Exception{
        MockMultipartFile file = new MockMultipartFile("validUpload", "file5.pdf", "application/pdf", new byte[5]);
        when(fileService.uploadFiles(anyList())).thenThrow(new ImageTypeException());


        mockMvc.perform(multipart("/profile/contractor")
                        .file(file)
                        .with(csrf())
                        .param("description", VALID_DESCRIPTION)
                        .param("location", "Methven 7730, New Zealand")
                        .param("country", "New Zealand")
                        .param("city", "Ashburton District")
                        .param("suburb", "")
                        .param("street", "")
                        .param("postcode", "7730")
                )
                .andExpect(view().name("contractorRegisterTemplate"))
                .andExpect(model().attribute("errorImageFormat", "Invalid file type"));
        verify(userService, never()).convertUserToContractor(any(User.class), anyString(), anyList(), any(Location.class));

    }

    @Test
    void contractorRegistration_InvalidImagesSize_ErrorPresent() throws Exception{
        when(fileService.uploadFiles(anyList())).thenThrow(new FileSizeException());

        MockMultipartFile file = new MockMultipartFile("validUpload", "file1.jpg", "image/jpeg", new byte[5]);

        mockMvc.perform(multipart("/profile/contractor")
                        .file(file)
                        .with(csrf())
                        .param("description", VALID_DESCRIPTION)
                        .param("location", "Methven 7730, New Zealand")
                        .param("country", "New Zealand")
                        .param("city", "Ashburton District")
                        .param("suburb", "")
                        .param("street", "")
                        .param("postcode", "7730")
                )
                .andExpect(view().name("contractorRegisterTemplate"))
                .andExpect(model().attribute("errorImageSize", "Files must be no greater than 10MB in size"));
        verify(userService, never()).convertUserToContractor(any(User.class), anyString(), anyList(), any(Location.class));
    }


    @Test
    void contractorRegistration_ValidImageAndInvalidDescription_ValidImagePathPresent() throws Exception{

        MockMultipartFile file = new MockMultipartFile("validUpload", "file1.jpg", "image/jpeg", new byte[5]);

        mockMvc.perform(multipart("/profile/contractor")
                        .file(file)
                        .with(csrf())
                        .param("description", "")
                        .param("location", "Methven 7730, New Zealand")
                        .param("country", "New Zealand")
                        .param("city", "Ashburton District")
                        .param("suburb", "")
                        .param("street", "")
                        .param("postcode", "7730")
                )
                .andExpect(view().name("contractorRegisterTemplate"))
                .andExpect(model().attribute("imagesPaths", VALID_IMAGE_PATHS));
        verify(userService, never()).convertUserToContractor(any(User.class), anyString(), anyList(), any(Location.class));
    }
}