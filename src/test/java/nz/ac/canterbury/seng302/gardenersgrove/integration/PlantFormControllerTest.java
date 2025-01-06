package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.PlantFormController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.ImageTypeException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.validation.FileValidation;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static nz.ac.canterbury.seng302.gardenersgrove.validation.FileValidation.validateImage;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlantFormController.class)
class PlantFormControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    private PlantService plantService;
    @MockBean
    private GardenService gardenService;
    @MockBean
    private FileService fileService;
    @MockBean
    private WeatherService weatherService;
    @Autowired
    private MessageSource messageSource;
    @MockBean
    private RecentPlantsService recentPlantsService;
    // GardenRepository is required by GlobalControllerAdvice
    @MockBean
    private GardenRepository gardenRepository;
    private static MockedStatic<FileValidation> fileValidation;

    ArgumentCaptor<Plant> plantArgumentCaptor = ArgumentCaptor.forClass(Plant.class);

    private final String VALID_NAME = "Carrot";
    private final String VALID_COUNT = "1";
    private final String VALID_DESCRIPTION = "Orange";
    private final String VALID_DATE_STRING = "01/01/1990";
    private final LocalDate VALID_DATE = LocalDate.parse("01/01/1990", DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    private final byte[] VALID_IMAGE = {'I', 'M', 'A', 'G', 'E'};
    private final String VALID_IMAGE_PATH = "/images/default.jpg";
    private final String INVALID_NAME = "C@rrot";
    private final String INVALID_COUNT = "-5";
    private final String INVALID_DESCRIPTION = "A".repeat(513);
    private final String INVALID_DATE = "2005-40-40";

    @BeforeAll
    static void setup() {
        fileValidation = Mockito.mockStatic(FileValidation.class);
    }

    @AfterAll
    static void cleanup() {
        fileValidation.close();
    }

    @BeforeEach
    void setUpEach() {
        // Spring security linking mainly sourced from https://stackoverflow.com/questions/360520/unit-testing-with-spring-security
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        User user = new User(
                "Mock",
                "User",
                "test@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );

        user.setUserId(1L);

        Garden garden = new Garden(
                "Garden",
                "10",
                user,
                "Description",
                false,
                null,
                null,
                true,
                null
        );

        Plant plant = new Plant(
                garden,
                "Carrot",
                "1",
                "Certainly orange",
                LocalDate.parse("1990-01-01"),
                null
        );

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(gardenService.getGarden(1L)).thenReturn(Optional.of(garden));
        Mockito.when(plantService.getPlantsInGarden(1L)).thenReturn(List.of(plant));
        Mockito.when(plantService.getPlant(1L)).thenReturn(Optional.of(plant));
    }

    @Test
    void GetPlantForm_GardenDoesntExist_StatusNotFound() throws Exception {
        mockMvc.perform(get("/garden/99")).andExpect(status().isNotFound());
    }

    @Test
    void GetPlantForm_StatusOk() throws Exception {
        mockMvc.perform(get("/garden/1/plant")
        ).andExpect(status().isOk());
    }

    @Test
    void PostPlant_ValidPlant_RedirectsCorrectly() throws Exception {
        mockMvc.perform(multipart("/garden/1/plant")
                        .file("image", new byte[0])
                        .with(csrf())
                        .param("name", VALID_NAME)
                        .param("count", VALID_COUNT)
                        .param("description", VALID_DESCRIPTION)
                        .param("date", VALID_DATE_STRING)
                        .param("imagePath", VALID_IMAGE_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/garden/1"));
    }

    @Test
    void PostPlant_InvalidPlantNoImage_NoRedirectCorrectErrorCodes() throws Exception {
        String expectedMessage = "Plant name cannot be empty and must only include letters, numbers, spaces, dots, hyphens, or apostrophes";
        mockMvc.perform(multipart("/garden/1/plant")
                        .file("image", new byte[0])
                        .with(csrf())
                        .param("name", INVALID_NAME)
                        .param("count", VALID_COUNT)
                        .param("description", VALID_DESCRIPTION)
                        .param("date", VALID_DATE_STRING)
                        .param("imagePath", VALID_IMAGE_PATH))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errorName", expectedMessage))
                .andExpect(model().attribute("errorCount", ""))
                .andExpect(model().attribute("errorDescription", ""))
                .andExpect(model().attribute("errorDate", ""));
    }

    // Tests for /garden/{id}/plant endpoint - get add plant form and add plant functions
    @Test
    void PostGarden_ValidGardenNoImage_GardenSavedWithCorrectDetails() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant")
                        .file("image", new byte[0])
                        .with(csrf())
                        .param("name", VALID_NAME)
                        .param("count", VALID_COUNT)
                        .param("description", VALID_DESCRIPTION)
                        .param("date", VALID_DATE_STRING)
                        .param("imagePath", VALID_IMAGE_PATH)
                );

        verify(plantService, times(1)).addPlant(plantArgumentCaptor.capture());
        Assertions.assertEquals(VALID_NAME, plantArgumentCaptor.getValue().getName());
        Assertions.assertEquals(VALID_COUNT, plantArgumentCaptor.getValue().getCount());
        Assertions.assertEquals(VALID_DESCRIPTION, plantArgumentCaptor.getValue().getDescription());
        Assertions.assertEquals(VALID_DATE, plantArgumentCaptor.getValue().getDate());
    }

    @Test
    void PostGarden_ValidGardenNoImage_IsRedirectedToGardenDetails() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant")
                .file("image", new byte[0])
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
                .param("imagePath", VALID_IMAGE_PATH)
        ).andExpect(redirectedUrl("/garden/1"));
    }

    @Test
    void PostGarden_ValidGardenHasImage_IsRedirectedToGardenDetails() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant")
                .file("image", VALID_IMAGE)
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
                .param("imagePath", VALID_IMAGE_PATH)
        ).andExpect(redirectedUrl("/garden/1"));
    }

    @Test
    void PostGarden_ValidGardenHasImage_GardenSavedWithCorrectDetails() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant")
                .file("image", VALID_IMAGE)
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
                .param("imagePath", VALID_IMAGE_PATH)
        );
        verify(plantService, times(1)).addPlant(plantArgumentCaptor.capture());
        Assertions.assertEquals(VALID_NAME, plantArgumentCaptor.getValue().getName());
        Assertions.assertEquals(VALID_COUNT, plantArgumentCaptor.getValue().getCount());
        Assertions.assertEquals(VALID_DESCRIPTION, plantArgumentCaptor.getValue().getDescription());
        Assertions.assertEquals(VALID_DATE, plantArgumentCaptor.getValue().getDate());
        Assertions.assertEquals(Plant.DEFAULT_IMAGE_PATH, plantArgumentCaptor.getValue().getImagePath());
    }

    @Test
    void PostPlant_InValidPlantImage_PlantNotSaved() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenThrow(new ImageTypeException("Invalid image type"));
        mockMvc.perform(multipart("/garden/1/plant")
                .file("image", VALID_IMAGE)
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
                .param("imagePath", VALID_IMAGE_PATH)
        );
        verify(plantService, never()).addPlant(Mockito.any(Plant.class));
    }

    @Test
    void PostGarden_InvalidGardenName_GardenNotSaved() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant")
                .file("image", new byte[0])
                .with(csrf())
                .param("name", INVALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
        );

        verify(plantService, never()).addPlant(Mockito.any(Plant.class));
    }

    @Test
    void PostGarden_InvalidGardenCount_GardenNotSaved() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant")
                .file("image", new byte[0])
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", INVALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
        );

        verify(plantService, never()).addPlant(Mockito.any(Plant.class));
    }

    @Test
    void PostGarden_InvalidGardenDescription_GardenNotSaved() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant")
                .file("image", new byte[0])
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", INVALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
        );

        verify(plantService, never()).addPlant(Mockito.any(Plant.class));
    }

    @Test
    void PostGarden_InvalidGardenDate_GardenNotSaved() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant")
                .file("image", new byte[0])
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", INVALID_DATE)
        );

        verify(plantService, never()).addPlant(Mockito.any(Plant.class));
    }

    @Test
    void PostGarden_InvalidGardenName_ModelHasError() throws Exception {
        String expectedMessage = "Plant name cannot be empty and must only include letters, numbers, spaces, dots, hyphens, or apostrophes";
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant")
                .file("image", new byte[0])
                .with(csrf())
                .param("name", INVALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
                .param("imagePath", VALID_IMAGE_PATH)
        ).andExpect(model().attribute("errorName", expectedMessage));
    }

    // Tests for /garden/{gardenId}/plant/{plantId}/edit endpoint - editing individual plant
    @Test
    void GetEditForm_StatusOk() throws Exception {
        mockMvc.perform(get("/garden/1/plant/1/edit")
        ).andExpect(status().isOk());
    }

    // Tests for /garden/{id}/plant/{id}/edit endpoint - get add plant form and add plant functions
    @Test
    void EditPlant_ValidPlantNoImage_PlantSavedWithCorrectDetails() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant/1/edit")
                .file("image", new byte[0])
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
                .param("imagePath", VALID_IMAGE_PATH)
        );

        verify(plantService, times(1)).addPlant(plantArgumentCaptor.capture());
        Assertions.assertEquals(VALID_NAME, plantArgumentCaptor.getValue().getName());
        Assertions.assertEquals(VALID_COUNT, plantArgumentCaptor.getValue().getCount());
        Assertions.assertEquals(VALID_DESCRIPTION, plantArgumentCaptor.getValue().getDescription());
        Assertions.assertEquals(VALID_DATE, plantArgumentCaptor.getValue().getDate());
    }

    @Test
    void PostEditPlant_ValidPlantImage_IsRedirectedToGardenDetails() throws Exception {
        mockMvc.perform(multipart("/garden/1/plant/1/edit")
                .file("image", new byte[0])
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
                .param("imagePath", VALID_IMAGE_PATH)
        ).andExpect(redirectedUrl("/garden/1"));
    }

    @Test
    void PostEditPlant_ValidPlantHasImage_IsRedirectedToGardenDetails() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant/1/edit")
                .file("image", VALID_IMAGE)
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
                .param("imagePath", VALID_IMAGE_PATH)
        ).andExpect(redirectedUrl("/garden/1"));
    }

    @Test
    void PostEditPlant_ValidPlantHasImage_PlantSavedWithCorrectDetails() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant/1/edit")
                .file("image", VALID_IMAGE)
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
                .param("imagePath", VALID_IMAGE_PATH)
        );
        verify(plantService, times(1)).addPlant(plantArgumentCaptor.capture());
        Assertions.assertEquals(VALID_NAME, plantArgumentCaptor.getValue().getName());
        Assertions.assertEquals(VALID_COUNT, plantArgumentCaptor.getValue().getCount());
        Assertions.assertEquals(VALID_DESCRIPTION, plantArgumentCaptor.getValue().getDescription());
        Assertions.assertEquals(VALID_DATE, plantArgumentCaptor.getValue().getDate());
        Assertions.assertEquals(Plant.DEFAULT_IMAGE_PATH, plantArgumentCaptor.getValue().getImagePath());
    }

    @Test
    void PostEditPlant_InvalidPlantName_PlantNotSaved() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant/1/edit")
                .file("image", new byte[0])
                .with(csrf())
                .param("name", INVALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
                .param("imagePath", VALID_IMAGE_PATH)
        );

        verify(plantService, never()).addPlant(Mockito.any(Plant.class));
    }

    @Test
    void PostEditPlant_InvalidPlantCount_PlantNotSaved() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant/1/edit")
                .file("image", new byte[0])
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", INVALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
                .param("imagePath", VALID_IMAGE_PATH)
        );

        verify(plantService, never()).addPlant(Mockito.any(Plant.class));
    }

    @Test
    void PostEditPlant_InvalidPlantDescription_PlantNotSaved() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant/1/edit")
                .file("image", new byte[0])
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", INVALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
                .param("imagePath", VALID_IMAGE_PATH)
        );

        verify(plantService, never()).addPlant(Mockito.any(Plant.class));
    }

    @Test
    void PostEditPlant_InvalidPlantDate_PlantNotSaved() throws Exception {
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant/1/edit")
                .file("image", new byte[0])
                .with(csrf())
                .param("name", VALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", INVALID_DATE)
                .param("imagePath", VALID_IMAGE_PATH)
        );

        verify(plantService, never()).addPlant(Mockito.any(Plant.class));
    }

    @Test
    void PostEditPlant_InvalidPlantName_ModelHasError() throws Exception {
        String expectedMessage = "Plant name cannot be empty and must only include letters, numbers, spaces, dots, hyphens, or apostrophes";
        fileValidation.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        mockMvc.perform(multipart("/garden/1/plant/1/edit")
                .file("image", new byte[0])
                .with(csrf())
                .param("name", INVALID_NAME)
                .param("count", VALID_COUNT)
                .param("description", VALID_DESCRIPTION)
                .param("date", VALID_DATE_STRING)
                .param("imagePath", VALID_IMAGE_PATH)
        ).andExpect(model().attribute("errorName", expectedMessage));
    }
}
