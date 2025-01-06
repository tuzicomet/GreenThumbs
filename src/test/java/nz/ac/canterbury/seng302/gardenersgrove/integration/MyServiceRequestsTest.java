package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.MyServiceRequestsController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.JobApplicationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ServiceRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.*;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MyServiceRequestsController.class)
class MyServiceRequestsTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private ServiceRequestService serviceRequestService;
    private ServiceRequest serviceRequest;
    @MockBean
    private User user;
    @MockBean
    private GardenService gardenService;
    @MockBean
    private ServiceRequestRepository serviceRequestRepository;
    @MockBean
    private JobApplicationRepository jobApplicationRepository;
    @MockBean
    private JobApplicationService jobApplicationService;
    // GardenRepository is required by GlobalControllerAdvice
    @MockBean
    private GardenRepository gardenRepository;
    @MockBean
    FileService fileService;
    @MockBean
    QuestionAnswerService questionAnswerService;
    @MockBean
    private MailService mailService;
    private Garden garden;

    private Garden garden2;
    private Location location;
    private User user2;
    private Authentication authentication;

    final String VALID_TITLE = "title";
    final String VALID_DESCRIPTION = "description";
    final String VALID_DATE = LocalDate.now().plusDays(2).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
    final String VALID_PRICE = "10";

    final String VALID_PATH = "/user_uploads/path.jpg";


    final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    @BeforeEach
    void setup() {
        authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        // user 1 owns garden 1
        User user = new User(
                "Mock",
                "User",
                "test@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        user.setUserId(1L);

        // garden 1
        garden = new Garden(
                "Garden 1",
                "1.0",
                user,
                "Valid",
                true,
                null,
                null,
                true,
                null
        );

        // user 2 owns garden 2
        user2 = new User(
                "Mock",
                "User",
                "test@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        user2.setUserId(2L);

        // garden 2
        garden2 = new Garden(
                "Garden 1",
                "1.0",
                user2,
                "Valid",
                true,
                null,
                null,
                true,
                null
        );

        // Location shared between gardens
        location = new Location(
                "Engineering Road, Riccarton, Christchurch 8041, New Zealand",
                "New Zealand",
                "Christchurch City",
                "Riccarton",
                "Engineering Road",
                "8041"
        );
        location.setLon(1.0);
        location.setLat(2.0);
        garden.setGardenId(1L);
        garden.setLocation(location);
        garden2.setGardenId(2L);
        garden2.setLocation(location);

        // The service request for editing
        ServiceRequest serviceRequest1 = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                garden
        );
        serviceRequest1.setId(1L);

        Mockito.when(gardenService.getGarden(1L)).thenReturn(Optional.of(garden));
        Mockito.when(gardenService.getGarden(2L)).thenReturn(Optional.of(garden2));
        Mockito.when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest1));
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(user);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        Mockito.when(fileService.addFile(any(), any())).thenReturn(Optional.of(Path.of(VALID_PATH)));
        SecurityContextHolder.setContext(securityContext);

        serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(1L).get()
        );
        serviceRequest.setId(1L);

        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));
    }

    

    // Test the myServiceRequests page is accessible
    @Test
    void GetMyServiceRequests_StatusOK() throws Exception {
        Page<ServiceRequest> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        Mockito.when(serviceRequestService.getCurrentServiceRequests(any(AbstractUser.class), any(Pageable.class)))
               .thenReturn(emptyPage);
        Mockito.when(serviceRequestService.getPastServiceRequests(any(AbstractUser.class), any(Pageable.class)))
               .thenReturn(emptyPage);
        mockMvc.perform(get("/myServiceRequests"))
                .andExpect(status().isOk());
    }

    // Test the newServiceRequest page is accessible
    @Test
    void GetNewServiceRequests_StatusOK() throws Exception {
        mockMvc.perform(get("/newServiceRequest"))
                .andExpect(status().isOk());
    }

    // Test the form is submitted and the user is redirected
    @Test
    void SubmitRequestForm_Saved() throws Exception {
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "21/08/2025")
                        .param("dateMax", "21/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"));
        verify(serviceRequestService).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_ValidImage_Saved() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", new byte[5]);

        mockMvc.perform(multipart("/newServiceRequest")
                        .file(image)
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "21/08/2025")
                        .param("dateMax", "21/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"));
        verify(serviceRequestService).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_InvalidImageSize_ShowsErrorAndNotSaved() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "file1.jpg", "image/jpeg", new byte[10000001]);
        mockMvc.perform(multipart("/newServiceRequest")
                        .file(image)
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "21/08/2025")
                        .param("dateMax", "21/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorImage", "Files must be no greater than 10MB in size"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_InvalidImageFormat_ShowsErrorAndNotSaved() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "file5.pdf", "application/pdf", new byte[5]);

        mockMvc.perform(multipart("/newServiceRequest")
                        .file(image)
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "21/08/2025")
                        .param("dateMax", "21/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorImage", "Invalid file type"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_TitleNotGiven_ShowsErrorAndNotSaved() throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", "")
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorTitle", "Title cannot be empty"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_TitleTooLong_ShowsErrorAndNotSaved() throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", "a".repeat(33))
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorTitle", "Title must be 32 characters long or less"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_DescriptionNotGiven_ShowsErrorAndNotSaved() throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", "")
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorDescription", "Description cannot be empty and must only include letters, spaces, hyphens or apostrophes, and cannot consist solely of non-alphabetical characters"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_DescriptionTooLong_ShowsErrorAndNotSaved() throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", "a".repeat(513))
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorDescription", "Description cannot be empty and must only include letters, spaces, hyphens or apostrophes, and cannot consist solely of non-alphabetical characters"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_DateInvalidFormat_ShowsErrorAndNotSaved() throws Exception {
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "08/21/2025")
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorDateMin", "Date is not in valid format, (DD/MM/YYYY)"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_DateInThePast_ShowsErrorAndNotSaved() throws Exception {
        String twoDaysAgo = LocalDate.now().minusDays(2).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", twoDaysAgo)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                )
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorDateMin", "Earliest date cannot be in the past"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_InputNotGiven_ShowsErrorAndNotSaved() throws Exception {
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", "")
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                )
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorDateMin", "Earliest date must not be empty"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_TooFarInFuture_ShowsErrorAndNotSaved() throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                )
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorDateMin", "Earliest date must be less than a year away"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_LatestDateIsBeforeEarliest_ShowsErrorAndNotSaved() throws Exception {
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", LocalDate.now().plusDays(5).format(DATE_FORMATTER))
                        .param("dateMax", LocalDate.now().plusDays(4).format(DATE_FORMATTER))
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                )
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorDateMax", "Latest date must not be before the earliest date"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    // Had an issue where the 'latest date before earliest' check couldn't be calculated if the earliest had an error.
    // Fixed now, but this makes sure that doesn't regress. See Optional logic/explanation in MyServiceRequestsController
    @Test
    void SubmitRequestForm_LatestDateIsBeforeEarliestAndEarliestInvalid_ShowsBothErrorsAndNotSaved() throws Exception {
        LocalDate yearAway = LocalDate.now().plusYears(1).plusDays(5);
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", yearAway.plusDays(1).format(DATE_FORMATTER))
                        .param("dateMax", yearAway.minusDays(1).format(DATE_FORMATTER))
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                )
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorDateMin", "Earliest date must be less than a year away"))
                .andExpect(model().attribute("errorDateMax", "Latest date must not be before the earliest date"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @ParameterizedTest
    @CsvSource({
            "''",
            "' '",
            "not a number",
            "'a.46'",
            "'5.'",
            "'5,'",
            "'5.9898'",
    })
    void SubmitRequestForm_InvalidMinimumPriceGiven_ShowsErrorAndNotSaved(String priceMin) throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", priceMin)
                        .param("priceMax", VALID_PRICE)
                )
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorPriceMin", "Minimum price must be valid and between 0 and 100000 (inclusive)."));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @ParameterizedTest
    @CsvSource({
            "''",
            "' '",
            "not a number",
            "'a.46'",
            "'5.'",
            "'5,'",
            "'5.9898'",
    })
    void SubmitRequestForm_InvalidMaximumPriceGiven_ShowsErrorAndNotSaved(String priceMax) throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", priceMax)
                )
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorPriceMax", "Maximum price must be valid and between 0 and 100000 (inclusive)."));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @ParameterizedTest
    @CsvSource({
            "'', ''",
            "' ', ' '",
            "'not a number', 'also not a number'",
            "'E.14', 'I.59'",
            "'2.', 6.'",
            "'5,', '3,'",
            "'5.', '8,979'",
            "'3,238', '4.626'",
    })
    void SubmitRequestForm_InvalidMinimumAndMaximumPriceGiven_ShowsErrorsAndNotSaved(
            String priceMin, String priceMax) throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", priceMin)
                        .param("priceMax", priceMax)
                )
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorPriceMax", "Maximum price must be valid and between 0 and 100000 (inclusive)."))
                .andExpect(model().attribute("errorPriceMax", "Maximum price must be valid and between 0 and 100000 (inclusive)."));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_GardenNotPublic_ErrorOnModel() throws Exception {
        Garden privateGarden = new Garden(
                "Private garden",
                "1.0",
                user,
                "Valid",
                false,
                null,
                null,
                true,
                null
        );

        privateGarden.setGardenId(2L);

        Mockito.when(gardenService.getGarden(any())).thenReturn(Optional.of(privateGarden));

        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "20/08/2025")
                        .param("dateMax", "20/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorGarden", "This is not one of your public gardens."));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_UserIsNotGardenOwner_ErrorOnModel() throws Exception {
        User otherUser = new User(
                "Other",
                "User",
                "test@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        otherUser.setUserId(2L);

        Garden someoneElsesGarden = new Garden(
                "Private garden",
                "1.0",
                otherUser,
                "Valid",
                true,
                null,
                null,
                true,
                null
        );

        someoneElsesGarden.setGardenId(3L);

        Mockito.when(gardenService.getGarden(any())).thenReturn(Optional.of(someoneElsesGarden));

        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "20/08/2025")
                        .param("dateMax", "20/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorGarden", "This is not one of your public gardens."));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }


    @Test
    void SubmitRequestForm_GardenDoesNotExist_ErrorOnModel() throws Exception {

        Mockito.when(gardenService.getGarden(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "20/08/2025")
                        .param("dateMax", "20/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "99999"))
                .andExpect(status().isOk())
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorGarden", "This is not one of your public gardens."));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitRequestForm_InvalidLocation_ErrorOnModel() throws Exception {
        Location location = new Location(
                "Engineering Road, Riccarton, Christchurch 8041, New Zealand",
                "Not real location",
                "Not real",
                "",
                " ",
                "");
        garden.setLocation(location);
        Mockito.when(gardenService.getGarden(any())).thenReturn(Optional.of(garden));

        mockMvc.perform(post("/newServiceRequest")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "21/08/2025")
                        .param("dateMax", "21/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("newServiceRequestTemplate"))
                .andExpect(model().attribute("errorGarden", "Your garden must have a valid location"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void GetServiceRequestDetails_ValidRequestIamAmTheOwner_ShowsDetails() throws Exception {
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(1L).get()
        );
        serviceRequest.setId(1L);

        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));

        mockMvc.perform(get("/serviceRequest/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("serviceRequestDetailsTemplate"))
                .andExpect(model().attributeExists("serviceRequest"))
                .andExpect(model().attribute("serviceRequest", serviceRequest));
    }

    @Test
    void GetServiceRequestDetails_RequestHasQuestionsButAllAnswered_noUnansweredQuestionsIsTrue() throws Exception {
        // Make a service request owned by user 1
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(1L).get()
        );
        serviceRequest.setId(1L);

        // Turn user 2 into a contractor and make them ask a question
        Contractor contractor1 = new Contractor(user2, "Hi", new ArrayList<>(), location);
        QuestionAnswer question = new QuestionAnswer(serviceRequest, contractor1, "This is a valid question");
        question.setId(1L);
        question.setAnswer("This is a valid answer");

        Mockito.when(serviceRequestService.findById(1L))
                .thenReturn(Optional.of(serviceRequest));
        Mockito.when(questionAnswerService.findQuestionAnswersByServiceRequest(1L))
                .thenReturn(Collections.singletonList(question));
        Mockito.when(questionAnswerService.getTotalNumberOfUnansweredQuestions(serviceRequest))
                .thenReturn(0);

        // Go to the service request's details page and check that the noUnansweredQuestions model attribute is true
        mockMvc.perform(get("/serviceRequest/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("noUnansweredQuestions", true));
    }

    @Test
    void GetServiceRequestDetails_RequestHasQuestionsButNotAnswered_noUnansweredQuestionsIsNotPresent() throws Exception {
        // Make a service request owned by user 1
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(1L).get()
        );
        serviceRequest.setId(1L);

        // Turn user 2 into a contractor and make them ask a question
        Contractor contractor1 = new Contractor(user2, "Hi", new ArrayList<>(), location);
        QuestionAnswer question = new QuestionAnswer(serviceRequest, contractor1, "This is a valid question");
        // Do not answer the question

        Mockito.when(serviceRequestService.findById(1L))
                .thenReturn(Optional.of(serviceRequest));
        Mockito.when(questionAnswerService.findQuestionAnswersByServiceRequest(1L))
                .thenReturn(Collections.singletonList(question));
        Mockito.when(questionAnswerService.getTotalNumberOfUnansweredQuestions(serviceRequest))
                .thenReturn(1);

        // Go to the service request's details page and check that the noUnansweredQuestions model attribute is not present
        mockMvc.perform(get("/serviceRequest/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("noUnansweredQuestions"));
    }

    @Test
    void GetAssignedServiceRequestDetails_AsOwner_ShowsDetailsAndIsAssignedPresent() throws Exception {
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(1L).get()
        );
        serviceRequest.setId(1L);

        Contractor contractor1 = new Contractor(user2, "Hi", new ArrayList<>(),location);

        serviceRequest.setContractor(contractor1);

        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));

        mockMvc.perform(get("/serviceRequest/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("serviceRequestDetailsTemplate"))
                .andExpect(model().attributeExists("serviceRequest"))
                .andExpect(model().attribute("serviceRequest", serviceRequest))
                .andExpect(model().attribute("isAssigned", true))
                .andExpect(model().attribute("assignedContractor", contractor1));
    }

    @Test
    void GetServiceRequestDetails_InvalidRequest_RedirectsToMyServiceRequests() throws Exception {
        when(serviceRequestService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/serviceRequest/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"));
    }

    @Test
    void GetEditServiceRequest_ValidRequest_ShowsEditForm() throws Exception {
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(1L).get()
        );
        serviceRequest.setId(1L);

        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));

        mockMvc.perform(get("/serviceRequest/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("editServiceRequestTemplate"));
    }

    @Test
    void GetEditServiceRequest_AssignedRequest_RedirectAndShowsErrorMessage() throws Exception {
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(1L).get()
        );
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(),location);

        serviceRequest.setContractor(contractor);
        serviceRequest.setId(1L);

        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));

        mockMvc.perform(get("/serviceRequest/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/myServiceRequests"))
                .andExpect(flash().attribute("accessError", "You cannot edit this service request."));
    }

    @Test
    void GetEditServiceRequest_ExpiredRequest_RedirectAndShowsErrorMessage() throws Exception {
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDateTime.of(2001, 9, 11, 0, 0).toInstant(ZoneOffset.UTC),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(1L).get()
        );

        serviceRequest.setId(1L);

        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));

        mockMvc.perform(get("/serviceRequest/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/myServiceRequests"))
                .andExpect(flash().attribute("accessError", "This service request is expired"));
    }

    //Test case for image persist on invalid submission
    @Test
    void GetEditServiceRequest_WithImagePathParam_ShowsEditFormWithImagePath() throws Exception {
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(1L).get()
        );
        serviceRequest.setId(1L);

        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));

        mockMvc.perform(get("/serviceRequest/1/edit")
                .param("imagePath", "anImagePath"))
                .andExpect(status().isOk())
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("imagePath", "anImagePath"));
    }

    @Test
    void GetEditServiceRequest_InvalidRequest_RedirectsToMyServiceRequests() throws Exception {
        when(serviceRequestService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/serviceRequest/1/edit"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void PostEditServiceRequest_InvalidTitle_ServiceRequestNotSaved() throws Exception {
        mockMvc.perform(post("/serviceRequest/1/edit")
                .with(csrf())
                .param("title", "98359854")
                .param("description", VALID_DESCRIPTION)
                .param("dateMin", "21/08/2025")
                .param("dateMax", "21/08/2025")
                .param("priceMin", VALID_PRICE)
                .param("priceMax", VALID_PRICE)
                .param("garden", "1"));
        verify(serviceRequestService, Mockito.never()).saveServiceRequest(Mockito.any());
    }

    @Test
    void PostEditServiceRequest_AssignedRequest_ServiceRequestNotSaved() throws Exception {
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(1L).get()
        );
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(),location);

        serviceRequest.setContractor(contractor);
        serviceRequest.setId(1L);

        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));
        mockMvc.perform(post("/serviceRequest/1/edit")
                .with(csrf())
                .param("title", "Title")
                .param("description", VALID_DESCRIPTION)
                .param("dateMin", "21/08/2025")
                .param("dateMax", "21/08/2025")
                .param("priceMin", VALID_PRICE)
                .param("priceMax", VALID_PRICE)
                .param("garden", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/myServiceRequests"))
                .andExpect(flash().attribute("accessError", "You cannot edit this service request."));
        verify(serviceRequestService, Mockito.never()).saveServiceRequest(Mockito.any());
    }

    @Test
    void PostEditServiceRequest_ExpiredRequest_ServiceRequestNotSaved() throws Exception {
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDateTime.of(1939, 9, 1, 0, 0).toInstant(ZoneOffset.UTC),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(1L).get()
        );

        serviceRequest.setId(1L);

        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", "Title")
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "21/08/2025")
                        .param("dateMax", "21/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/myServiceRequests"))
                .andExpect(flash().attribute("accessError", "This service request is expired"));
        verify(serviceRequestService, Mockito.never()).saveServiceRequest(Mockito.any());
    }

    @Test
    void PostEditServiceRequest_TitleNotGiven_ShowsErrorAndNotSaved() throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", "")
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorTitle", "Title cannot be empty"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_TitleTooLong_ShowsErrorAndNotSaved() throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", "a".repeat(33))
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorTitle", "Title must be 32 characters long or less"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_DescriptionNotGiven_ShowsErrorAndNotSaved() throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", "")
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorDescription", "Description cannot be empty and must only include letters, spaces, hyphens or apostrophes, and cannot consist solely of non-alphabetical characters"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_DescriptionTooLong_ShowsErrorAndNotSaved() throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", "a".repeat(513))
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorDescription", "Description cannot be empty and must only include letters, spaces, hyphens or apostrophes, and cannot consist solely of non-alphabetical characters"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_DateInvalidFormat_ShowsErrorAndNotSaved() throws Exception {
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "08/21/2025")
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorDateMin", "Date is not in valid format, (DD/MM/YYYY)"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_DateInThePast_ShowsErrorAndNotSaved() throws Exception {
        String twoDaysAgo = LocalDate.now().minusDays(2).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", twoDaysAgo)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                )
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorDateMin", "Earliest date cannot be in the past"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_InputNotGiven_ShowsErrorAndNotSaved() throws Exception {
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", "")
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                )
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorDateMin", "Earliest date must not be empty"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_TooFarInFuture_ShowsErrorAndNotSaved() throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                )
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorDateMin", "Earliest date must be less than a year away"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_LatestDateIsBeforeEarliest_ShowsErrorAndNotSaved() throws Exception {
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", LocalDate.now().plusDays(5).format(DATE_FORMATTER))
                        .param("dateMax", LocalDate.now().plusDays(4).format(DATE_FORMATTER))
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                )
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorDateMax", "Latest date must not be before the earliest date"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    // Had an issue where the 'latest date before earliest' check couldn't be calculated if the earliest had an error.
    // Fixed now, but this makes sure that doesn't regress. See Optional logic/explanation in MyServiceRequestsController
    @Test
    void PostEditServiceRequest_LatestDateIsBeforeEarliestAndEarliestInvalid_ShowsBothErrorsAndNotSaved() throws Exception {
        LocalDate yearAway = LocalDate.now().plusYears(1).plusDays(5);
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", yearAway.plusDays(1).format(DATE_FORMATTER))
                        .param("dateMax", yearAway.minusDays(1).format(DATE_FORMATTER))
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                )
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorDateMin", "Earliest date must be less than a year away"))
                .andExpect(model().attribute("errorDateMax", "Latest date must not be before the earliest date"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @ParameterizedTest
    @CsvSource({
            "''",
            "' '",
            "not a number",
            "'a.46'",
            "'5.'",
            "'5,'",
            "'5.9898'",
    })
    void PostEditServiceRequest_InvalidMinimumPriceGiven_ShowsErrorAndNotSaved(String priceMin) throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", priceMin)
                        .param("priceMax", VALID_PRICE)
                )
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorPriceMin", "Minimum price must be valid and between 0 and 100000 (inclusive)."));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @ParameterizedTest
    @CsvSource({
            "''",
            "' '",
            "not a number",
            "'a.46'",
            "'5.'",
            "'5,'",
            "'5.9898'",
    })
    void PostEditServiceRequest_InvalidMaximumPriceGiven_ShowsErrorAndNotSaved(String priceMax) throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", priceMax)
                )
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorPriceMax", "Maximum price must be valid and between 0 and 100000 (inclusive)."));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @ParameterizedTest
    @CsvSource({
            "'', ''",
            "' ', ' '",
            "'not a number', 'also not a number'",
            "'E.14', 'I.59'",
            "'2.', 6.'",
            "'5,', '3,'",
            "'5.', '8,979'",
            "'3,238', '4.626'",
    })
    void PostEditServiceRequest_InvalidMinimumAndMaximumPriceGiven_ShowsErrorsAndNotSaved(
            String priceMin, String priceMax) throws Exception {
        String oneYearAway = LocalDate.now().plusYears(1).format(DATE_FORMATTER);
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("garden", "1")
                        .param("dateMin", oneYearAway)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", priceMin)
                        .param("priceMax", priceMax)
                )
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorPriceMax", "Maximum price must be valid and between 0 and 100000 (inclusive)."))
                .andExpect(model().attribute("errorPriceMax", "Maximum price must be valid and between 0 and 100000 (inclusive)."));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_GardenNotPublic_ErrorOnModel() throws Exception {
        Garden privateGarden = new Garden(
                "Private garden",
                "1.0",
                user,
                "Valid",
                false,
                null,
                null,
                true,
                null
        );

        privateGarden.setGardenId(2L);

        Mockito.when(gardenService.getGarden(any())).thenReturn(Optional.of(privateGarden));

        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "20/08/2025")
                        .param("dateMax", "20/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorGarden", "This is not one of your public gardens."));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_UserIsNotGardenOwner_ErrorOnModel() throws Exception {
        User otherUser = new User(
                "Other",
                "User",
                "test@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        otherUser.setUserId(2L);

        Garden someoneElsesGarden = new Garden(
                "Private garden",
                "1.0",
                otherUser,
                "Valid",
                true,
                null,
                null,
                true,
                null
        );

        someoneElsesGarden.setGardenId(3L);

        Mockito.when(gardenService.getGarden(any())).thenReturn(Optional.of(someoneElsesGarden));

        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "20/08/2025")
                        .param("dateMax", "20/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorGarden", "This is not one of your public gardens."));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }


    @Test
    void PostEditServiceRequest_GardenDoesNotExist_ErrorOnModel() throws Exception {

        Mockito.when(gardenService.getGarden(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "20/08/2025")
                        .param("dateMax", "20/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "99999"))
                .andExpect(status().isOk())
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorGarden", "This is not one of your public gardens."));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_InvalidLocation_ErrorOnModel() throws Exception {
        Location location = new Location(
                "Engineering Road, Riccarton, Christchurch 8041, New Zealand",
                "Not real location",
                "Not real",
                "",
                " ",
                "");
        garden.setLocation(location);
        Mockito.when(gardenService.getGarden(any())).thenReturn(Optional.of(garden));

        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "21/08/2025")
                        .param("dateMax", "21/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorGarden", "Your garden must have a valid location"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }
    @Test
    void PostEditServiceRequest_ValidImage_Saved() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", new byte[5]);

        mockMvc.perform(multipart("/serviceRequest/1/edit")
                        .file(image)
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "21/08/2025")
                        .param("dateMax", "21/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/myServiceRequests"));
        verify(serviceRequestService).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_InvalidImageFormat_ShowsErrorAndNotSaved() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "file5.pdf", "application/pdf", new byte[5]);

        mockMvc.perform(multipart("/serviceRequest/1/edit")
                        .file(image)
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "21/08/2025")
                        .param("dateMax", "21/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorImage", "Invalid file type"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }


    @Test
    void PostEditServiceRequest_InvalidImageSize_ShowsErrorAndNotSaved() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "file1.jpg", "image/jpeg", new byte[10000001]);

        mockMvc.perform(multipart("/serviceRequest/1/edit")
                        .file(image)
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", "21/08/2025")
                        .param("dateMax", "21/08/2025")
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorImage", "Files must be no greater than 10MB in size"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void getCurrentServiceRequest_NoServiceRequests_ShowsEmptyList() throws Exception {
        when(serviceRequestService.getCurrentServiceRequests(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/myServiceRequests?tab=current"))
                .andExpect(status().isOk())
                .andExpect(view().name("myServiceRequestsTemplate"))
                .andExpect(model().attribute("listIsEmpty", true))
                .andExpect(model().attribute("requests", Collections.emptyList()));
    }
    
    @Test
    void getPastServiceRequest_NoServiceRequests_ShowsEmptyList() throws Exception {
        when(serviceRequestService.getPastServiceRequests(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/myServiceRequests?tab=past"))
                .andExpect(status().isOk())
                .andExpect(view().name("myServiceRequestsTemplate"))
                .andExpect(model().attribute("listIsEmpty", true))
                .andExpect(model().attribute("requests", Collections.emptyList()));
    }

    @Test
    void getCurrentServiceRequest_CurrentServiceRequests_ShowsList() throws Exception {
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(1L).get()
        );
        serviceRequest.setId(1L);
        when(serviceRequestService.getCurrentServiceRequests(any(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(serviceRequest)));
        mockMvc.perform(get("/myServiceRequests?tab=current"))
                .andExpect(status().isOk())
                .andExpect(view().name("myServiceRequestsTemplate"))
                .andExpect(model().attribute("listIsEmpty", false))
                .andExpect(model().attribute("requests", Collections.singletonList(serviceRequest)));
    }

    @Test
    void getPastServiceRequest_PastServiceRequests_ShowsList() throws Exception {
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().minusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().minusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(1L).get()
        );
        serviceRequest.setId(1L);
        when(serviceRequestService.getPastServiceRequests(any(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(serviceRequest)));
        mockMvc.perform(get("/myServiceRequests?tab=past"))
                .andExpect(status().isOk())
                .andExpect(view().name("myServiceRequestsTemplate"))
                .andExpect(model().attribute("listIsEmpty", false))
                .andExpect(model().attribute("requests", Collections.singletonList(serviceRequest)));
    }

    @Test
    void GetServiceRequestDetails_IAmNotTheOwnerAndRegularUser_RedirectsToMyRequests() throws Exception {
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(2L).get()
                );
        serviceRequest.setId(2L);

        when(serviceRequestService.findById(2L)).thenReturn(Optional.of(serviceRequest));

        mockMvc.perform(get("/serviceRequest/2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"))
                .andExpect(flash().attributeExists("accessError"));
    }
    @Test
    void GetServiceRequestDetails_IAmNotTheOwnerButIAmAContractor_RedirectsToMyRequests() throws Exception {
        User contractor = new User(
                "Mock",
                "User",
                "test@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        contractor.setUserId(2L);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(contractor);
        Mockito.when(authentication.getPrincipal()).thenReturn(contractor);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        ServiceRequest serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                gardenService.getGarden(2L).get()
                );
        serviceRequest.setId(2L);

        when(serviceRequestService.findById(2L)).thenReturn(Optional.of(serviceRequest));

        mockMvc.perform(get("/serviceRequest/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("serviceRequestDetailsTemplate"))
                .andExpect(model().attributeExists("serviceRequest"))
                .andExpect(model().attribute("serviceRequest", serviceRequest));
    }

    @Test
    void GetEditServiceRequest_UserIsNotOwner_RedirectsToMyRequests() throws Exception {
        Mockito.when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));
        User otherUser = new User("Other", "User", "otheruser@gmail.com", "Password123", "1990-01-01", null);
        otherUser.setUserId(2L);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(otherUser);
        mockMvc.perform(get("/serviceRequest/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"))
                .andExpect(flash().attributeExists("accessError"));
    }

    @Test
    void PostEditServiceRequest_UserIsNotOwner_RedirectsToMyRequests() throws Exception {
        Mockito.when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));
        User otherUser = new User("Other", "User", "otheruser@gmail.com", "Password123", "1990-01-01", null);
        otherUser.setUserId(2L);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(otherUser);
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", "Updated Title")
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", VALID_DATE)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"))
                .andExpect(flash().attributeExists("accessError"));

        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_OwnerEdits_SuccessfullySaved() throws Exception {
        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", "Updated Title")
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", VALID_DATE)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"));

        verify(serviceRequestService).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void PostEditServiceRequest_InvalidGarden_RendersErrorOnModel() throws Exception {
        Garden invalidGarden = new Garden("Invalid Garden", "1.0", user, "Valid", false, null, null, true, null);
        invalidGarden.setGardenId(3L);
        Mockito.when(gardenService.getGarden(3L)).thenReturn(Optional.of(invalidGarden));

        mockMvc.perform(post("/serviceRequest/1/edit")
                        .with(csrf())
                        .param("title", VALID_TITLE)
                        .param("description", VALID_DESCRIPTION)
                        .param("dateMin", VALID_DATE)
                        .param("dateMax", VALID_DATE)
                        .param("priceMin", VALID_PRICE)
                        .param("priceMax", VALID_PRICE)
                        .param("garden", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("editServiceRequestTemplate"))
                .andExpect(model().attribute("errorGarden", "This is not one of your public gardens."));

        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void myServiceRequests_PageLessThanZero_RedirectsToFirstPage() throws Exception {
        mockMvc.perform(get("/myServiceRequests")
                        .param("tab", "current")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests?tab=current&page=0&size=10"));
    }

    @Test
    void myServiceRequests_PageGreaterThanTotalPages_RedirectsToFirstPage() throws Exception {
        List<ServiceRequest> serviceRequests = new ArrayList<>();
        Page<ServiceRequest> serviceRequestPage = new PageImpl<>(serviceRequests);

        when(serviceRequestService.getCurrentServiceRequests(any(), any(Pageable.class))).thenReturn(serviceRequestPage);

        mockMvc.perform(get("/myServiceRequests")
                        .param("tab", "current")
                        .param("page", "10")
                        .param("size", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests?tab=current&page=0&size=10"));
    }

    @Test
    void PostAcceptJobApplication_ValidDetails_UpdatesContractor() throws Exception {
        User user3 = new User(
                "Hello",
                "User",
                "lasdkf@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        User user4 = new User(
                "Bad",
                "User",
                "lkjadsfj@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(),location);
        Contractor contractor1 = new Contractor(user3, "Hi", new ArrayList<>(),location);
        Contractor contractor2 = new Contractor(user4, "Hi", new ArrayList<>(),location);

        JobApplication jobApplication1 = new JobApplication(serviceRequest, contractor,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication1.setId(1L);
        JobApplication jobApplication2 = new JobApplication(serviceRequest, contractor1,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication2.setId(2L);
        JobApplication jobApplication3 = new JobApplication(serviceRequest, contractor2,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication3.setId(3L);
        Mockito.when(jobApplicationService.findById(1L)).thenReturn(Optional.of(jobApplication1));
        Mockito.when(jobApplicationService.getJobApplicationsByRequestId(1L)).thenReturn(List.of(jobApplication1, jobApplication2, jobApplication3));



        mockMvc.perform(post("/serviceRequest/1/accept")
                        .with(csrf())
                        .param("applicationId", jobApplication1.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/serviceRequest/1"));

        Assertions.assertEquals("ACCEPTED", jobApplication1.getStatus());
        Assertions.assertEquals("DECLINED", jobApplication2.getStatus());
        Assertions.assertEquals("DECLINED", jobApplication3.getStatus());
    }

    @Test
    void PostAcceptJobApplication_InvalidServiceRequest_Redirects() throws Exception {
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(),location);
        JobApplication jobApplication1 = new JobApplication(serviceRequest, contractor,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication1.setId(1L);

        mockMvc.perform(post("/serviceRequest/2/accept")
                        .with(csrf())
                        .param("applicationId", jobApplication1.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"));
    }

    @Test
    void PostAcceptJobApplication_InvalidJobApplication_Redirects() throws Exception {
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(),location);
        JobApplication jobApplication1 = new JobApplication(serviceRequest, contractor,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication1.setId(1L);

        mockMvc.perform(post("/serviceRequest/1/accept")
                        .with(csrf())
                        .param("applicationId", jobApplication1.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"));
    }

    @Test
    void PostAcceptJobApplication_ServiceRequestAlreadyAssigned_Redirects() throws Exception {
        // Create users and make them contractors - user2 is created already in setup()
        User user3 = new User(
                "Hello",
                "User",
                "lasdkf@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        Contractor contractor1 = new Contractor(user2, "Hi", new ArrayList<>(),location);
        Contractor contractor2 = new Contractor(user3, "Hi", new ArrayList<>(),location);

        // Create a job application from each of the contractors - set IDs as DB mocked.
        JobApplication jobApplication1 = new JobApplication(serviceRequest, contractor1,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication1.setId(1L);
        JobApplication jobApplication2 = new JobApplication(serviceRequest, contractor2,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication2.setId(2L);

        Mockito.when(jobApplicationService.findById(1L)).thenReturn(Optional.of(jobApplication1));
        Mockito.when(jobApplicationService.getJobApplicationsByRequestId(1L)).thenReturn(List.of(jobApplication1, jobApplication2));

        // First, accepts job application #1, should be successful
        mockMvc.perform(
                post("/serviceRequest/1/accept")
                        .with(csrf())
                        .param("applicationId", jobApplication1.getId().toString())
        ).andExpect(redirectedUrl("/serviceRequest/1"));

        // Second, tries to accept job application #2, should be redirected to myServiceRequests - a fail.
        mockMvc.perform(
                post("/serviceRequest/1/accept")
                        .with(csrf())
                        .param("applicationId", jobApplication2.getId().toString())
        ).andExpect(redirectedUrl("/myServiceRequests"));
    }

    @Test
    void PostDeclineJobApplication_RequestHasMultipleApplications_SpecificApplicationDenied() throws Exception {
        // Create test users and convert them into contractors
        User user3 = new User(
                "Hello",
                "User",
                "lasdkf@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        User user4 = new User(
                "Bad",
                "User",
                "lkjadsfj@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(),location);
        Contractor contractor1 = new Contractor(user3, "Hi", new ArrayList<>(),location);
        Contractor contractor2 = new Contractor(user4, "Hi", new ArrayList<>(),location);

        // Create test job applications
        JobApplication jobApplication1 = new JobApplication(serviceRequest, contractor,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication1.setId(1L);
        JobApplication jobApplication2 = new JobApplication(serviceRequest, contractor1,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication2.setId(2L);
        JobApplication jobApplication3 = new JobApplication(serviceRequest, contractor2,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication3.setId(3L);
        Mockito.when(jobApplicationService.findById(1L)).thenReturn(Optional.of(jobApplication1));
        Mockito.when(jobApplicationService.getJobApplicationsByRequestId(1L)).thenReturn(List.of(jobApplication1, jobApplication2, jobApplication3));

        mockMvc.perform(post("/serviceRequest/1/decline")
                        .with(csrf())
                        .param("applicationId", jobApplication1.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/serviceRequest/1"));

        Assertions.assertEquals("DECLINED", jobApplication1.getStatus());
        Assertions.assertEquals("PENDING", jobApplication2.getStatus());
        Assertions.assertEquals("PENDING", jobApplication3.getStatus());
    }

    @Test
    void PostDeclineJobApplication_InvalidServiceRequest_Redirects() throws Exception {
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(),location);
        JobApplication jobApplication1 = new JobApplication(serviceRequest, contractor,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication1.setId(1L);

        mockMvc.perform(post("/serviceRequest/2/decline")
                        .with(csrf())
                        .param("applicationId", jobApplication1.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"));
    }

    @Test
    void PostDeclineJobApplication_InvalidJobApplication_Redirects() throws Exception {
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(),location);
        JobApplication jobApplication1 = new JobApplication(serviceRequest, contractor,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication1.setId(1L);

        mockMvc.perform(post("/serviceRequest/1/decline")
                        .with(csrf())
                        .param("applicationId", jobApplication1.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"));
    }

    @Test
    void PostDeclineJobApplication_ServiceRequestAlreadyAssigned_Redirects() throws Exception {
        // Create users and make them contractors - user2 is created already in setup()
        User user3 = new User(
                "Hello",
                "User",
                "lasdkf@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        Contractor contractor1 = new Contractor(user2, "Hi", new ArrayList<>(),location);
        Contractor contractor2 = new Contractor(user3, "Hi", new ArrayList<>(),location);

        // Create a job application from each of the contractors - set IDs as DB mocked.
        JobApplication jobApplication1 = new JobApplication(serviceRequest, contractor1,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication1.setId(1L);
        JobApplication jobApplication2 = new JobApplication(serviceRequest, contractor2,
                LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 70, "PENDING");
        jobApplication2.setId(2L);

        Mockito.when(jobApplicationService.findById(1L)).thenReturn(Optional.of(jobApplication1));
        Mockito.when(jobApplicationService.getJobApplicationsByRequestId(1L)).thenReturn(List.of(jobApplication1, jobApplication2));

        // First, accepts job application #1, should be successful
        mockMvc.perform(
                post("/serviceRequest/1/accept")
                        .with(csrf())
                        .param("applicationId", jobApplication1.getId().toString())
        ).andExpect(redirectedUrl("/serviceRequest/1"));

        // Second, tries to decline job application #2, should be redirected to myServiceRequests - a fail.
        mockMvc.perform(
                post("/serviceRequest/1/decline")
                        .with(csrf())
                        .param("applicationId", jobApplication2.getId().toString())
        ).andExpect(redirectedUrl("/myServiceRequests"));
    }

    @Test
    void SubmitQuestion_AsContractor_QuestionPosted() throws Exception {
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(),location);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(contractor);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(contractor);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);

        mockMvc.perform(post("/serviceRequest/1/question")
                .with(csrf())
                .param("question", "This is a valid question"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/serviceRequest/1"));

        verify(questionAnswerService).saveQuestionAnswer(any(QuestionAnswer.class));
    }

    @Test
    void SubmitQuestion_AsOwner_QuestionNotSaved() throws Exception {
        mockMvc.perform(post("/serviceRequest/1/question")
                        .with(csrf())
                        .param("question", "This is a valid question"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"));

        verify(questionAnswerService, never()).saveQuestionAnswer(any(QuestionAnswer.class));
    }

    @Test
    void SubmitQuestion_AsUser_QuestionNotSaved() throws Exception {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(user2);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(user2);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);

        mockMvc.perform(post("/serviceRequest/1/question")
                        .with(csrf())
                        .param("question", "This is a valid question"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"));

        verify(questionAnswerService, never()).saveQuestionAnswer(any(QuestionAnswer.class));
    }

    @Test
    void SubmitQuestion_AsContractorAndServiceRequestNotExist_QuestionNotSaved() throws Exception {
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(),location);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(contractor);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(contractor);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        Mockito.when(serviceRequestService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/serviceRequest/1/question")
                        .with(csrf())
                        .param("question", "This is a valid question"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myServiceRequests"));

        verify(questionAnswerService, never()).saveQuestionAnswer(any(QuestionAnswer.class));
    }

    @Test
    void SubmitInvalidCharQuestion_AsContractor_QuestionNotSaved() throws Exception {
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(),location);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(contractor);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(contractor);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);

        mockMvc.perform(post("/serviceRequest/1/question")
                        .with(csrf())
                        .param("question", "^&$^$%^&^&"))
                .andExpect(view().name("redirect:/serviceRequest/1"))
                .andExpect(flash().attribute("errorQuestion", "Question must be 512 characters or less and contain some text"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }

    @Test
    void SubmitQuestionTooLong_AsContractor_QuestionNotSaved() throws Exception {
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(),location);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(contractor);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(contractor);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);

        mockMvc.perform(post("/serviceRequest/1/question")
                        .with(csrf())
                        .param("question", "a".repeat(513)))
                .andExpect(view().name("redirect:/serviceRequest/1"))
                .andExpect(flash().attribute("errorQuestion", "Question must be 512 characters or less and contain some text"));
        verify(serviceRequestService, never()).saveServiceRequest(any(ServiceRequest.class));
    }
    @Test
    void SubmitAnswer_AsOwner_AnswerSaved() throws Exception {
        Contractor contractor1 = new Contractor(user2, "Hi", new ArrayList<>(),location);
        QuestionAnswer question = new QuestionAnswer(serviceRequest, contractor1, "This is a valid question");
        question.setId(1L);
        Mockito.when(questionAnswerService.getQuestionAnswerById(1L)).thenReturn(Optional.of(question));
        mockMvc.perform(post("/serviceRequest/1/answer")
                .with(csrf())
                .param("answer", "This is a valid answer")
                .param("questionId", "1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/serviceRequest/1"));

        verify(questionAnswerService).saveQuestionAnswer(any(QuestionAnswer.class));
    }

    @Test
    void SubmitAnswer_AsOwnerQuestionDoesntExist_Redirects() throws Exception {
        mockMvc.perform(post("/serviceRequest/1/answer")
                .with(csrf())
                .param("answer", "This is a valid answer")
                .param("questionId", "1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/myServiceRequests"));
    }

    @Test
    void SubmitAnswer_AsOwnerAndServiceRequestDoesntExist_Redirects() throws Exception {
        Mockito.when(serviceRequestService.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(post("/serviceRequest/1/answer")
                .with(csrf())
                .param("answer", "This is a valid answer")
                .param("questionId", "1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/myServiceRequests"));

        verify(questionAnswerService, never()).saveQuestionAnswer(any(QuestionAnswer.class));
    }

    @Test
    void SubmitAnswer_NotAsOwner_Redirects() throws Exception {
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(user2);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(user2);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        mockMvc.perform(post("/serviceRequest/1/answer")
                .with(csrf())
                .param("answer", "This is a valid answer")
                .param("questionId", "1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/myServiceRequests"));

        verify(questionAnswerService, never()).saveQuestionAnswer(any(QuestionAnswer.class));
    }

    @Test
    void CompleteAServiceRequest_AsAOwner_StatusOKAndCompleted() throws Exception{

        Contractor contractor1 = new Contractor(user2, "Hi", new ArrayList<>(),location);

        serviceRequest.setContractor(contractor1);

        mockMvc.perform(post("/serviceRequest/1/complete")
                .with(csrf()))
                .andExpect(redirectedUrl("/serviceRequest/1"));
        Assertions.assertTrue(serviceRequest.isCompleted());
    }

    @Test
    void CompleteAServiceRequest_AsAOwnerAndRequestIsNotAssigned_Redirects() throws Exception{
        mockMvc.perform(post("/serviceRequest/1/complete")
                        .with(csrf()))
                .andExpect(redirectedUrl("/myServiceRequests"));
        Assertions.assertFalse(serviceRequest.isCompleted());
    }

    @Test
    void CompleteAServiceRequest_NotOwner_Redirects() throws Exception{
        User user3 = new User(
                "Hello",
                "User",
                "lasdkf@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        user.setUserId(4L);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(user3);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(user3);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);

        Contractor contractor1 = new Contractor(user2, "Hi", new ArrayList<>(),location);

        serviceRequest.setContractor(contractor1);


        mockMvc.perform(post("/serviceRequest/1/complete")
                        .with(csrf()))
                .andExpect(redirectedUrl("/myServiceRequests"));
        Assertions.assertFalse(serviceRequest.isCompleted());
    }

    @Test
    void CompleteAServiceRequest_ServiceRequestNotExist_Redirects() throws Exception{
        when(serviceRequestService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/serviceRequest/1/complete")
                        .with(csrf()))
                .andExpect(redirectedUrl("/myServiceRequests"));
        Assertions.assertFalse(serviceRequest.isCompleted());
    }

    @Test
    void CompleteAServiceRequest_ServiceRequestIsCompleted_Redirects() throws Exception{
        serviceRequest.setCompleted(true);

        mockMvc.perform(post("/serviceRequest/1/complete")
                        .with(csrf()))
                .andExpect(redirectedUrl("/myServiceRequests"));
    }

    @Test
    void SubmitFourthQuestion_AsContractor_QuestionNotSaved() throws Exception {
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(),location);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(contractor);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(contractor);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        Mockito.when(questionAnswerService.getNumberOfUnansweredQuestions(any(), any())).thenReturn(3);
        mockMvc.perform(post("/serviceRequest/1/question")
                        .with(csrf())
                        .param("question", "This is a valid question"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/myServiceRequests"));

        verify(questionAnswerService, never()).saveQuestionAnswer(any(QuestionAnswer.class));
    }
    @Test
    void GetServiceRequestDetails_AsContractorWithTooManyQuestions_ModelIsCorrect() throws Exception {
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(), location);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(contractor);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(contractor);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        Mockito.when(questionAnswerService.getNumberOfUnansweredQuestions(any(), any())).thenReturn(3);
        mockMvc.perform(get("/serviceRequest/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasMaxUnansweredQuestions", true));
    }
    @Test
    void GetServiceRequestDetails_AsContractorNotWithTooManyQuestions_ModelIsCorrect() throws Exception {
        Contractor contractor = new Contractor(user2, "Hi", new ArrayList<>(), location);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(contractor);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(contractor);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        Mockito.when(questionAnswerService.getNumberOfUnansweredQuestions(any(), any())).thenReturn(2);
        mockMvc.perform(get("/serviceRequest/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasMaxUnansweredQuestions", false));
    }
    @Test
    void SubmitInvalidCharAnswer_AsOwner_AnswerNotSaved() throws Exception {
        Contractor contractor1 = new Contractor(user2, "Hi", new ArrayList<>(),location);
        QuestionAnswer question = new QuestionAnswer(serviceRequest, contractor1, "This is a valid question");
        question.setId(1L);
        Mockito.when(questionAnswerService.getQuestionAnswerById(1L)).thenReturn(Optional.of(question));
        mockMvc.perform(post("/serviceRequest/1/answer")
                        .with(csrf())
                        .param("answer", "???/&$^$%^&^&//.")
                        .param("questionId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/serviceRequest/1"));

        verify(questionAnswerService, never()).saveQuestionAnswer(any(QuestionAnswer.class));
    }

    @Test
    void SubmitAnswerTooLong_AsOwner_AnswerNotSaved() throws Exception {
        Contractor contractor1 = new Contractor(user2, "Hi", new ArrayList<>(),location);
        QuestionAnswer question = new QuestionAnswer(serviceRequest, contractor1, "This is a valid question");
        question.setId(1L);
        Mockito.when(questionAnswerService.getQuestionAnswerById(1L)).thenReturn(Optional.of(question));
        mockMvc.perform(post("/serviceRequest/1/answer")
                        .with(csrf())
                        .param("answer", "Q".repeat(513))
                        .param("questionId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/serviceRequest/1"));

        verify(questionAnswerService, never()).saveQuestionAnswer(any(QuestionAnswer.class));
    }
    @Test
    void SubmitEmptyAnswer_AsOwner_AnswerNotSaved() throws Exception {
        Contractor contractor1 = new Contractor(user2, "Hi", new ArrayList<>(),location);
        QuestionAnswer question = new QuestionAnswer(serviceRequest, contractor1, "This is a valid question");
        question.setId(1L);
        Mockito.when(questionAnswerService.getQuestionAnswerById(1L)).thenReturn(Optional.of(question));
        mockMvc.perform(post("/serviceRequest/1/answer")
                        .with(csrf())
                        .param("answer", "")
                        .param("questionId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/serviceRequest/1"));

        verify(questionAnswerService, never()).saveQuestionAnswer(any(QuestionAnswer.class));
    }

}


