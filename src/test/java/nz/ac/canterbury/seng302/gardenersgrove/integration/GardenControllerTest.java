package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenFormController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Location;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GardenFormController.class)
class GardenControllerTest {
    @Autowired
    private MockMvc mockMvc;
    // GardenRepository is required by GlobalControllerAdvice
    @MockBean
    private GardenRepository gardenRepository;
    @MockBean
    GardenService gardenService;
    @MockBean
    UserService userService;
    @MockBean
    PlantService plantService;
    @MockBean
    ProfanityFilterService profanityFilterService;
    @MockBean
    LocationService locationService;
    @MockBean
    WeatherService weatherService;
    @MockBean
    AlertService alertService;
    @MockBean
    MailService mailService;
    @Autowired
    MessageSource messageSource;
    @MockBean
    RecentGardensService recentGardensService;
    GardenFormController controller;

    ArgumentCaptor<Garden> gardenArgumentCaptor = ArgumentCaptor.forClass(Garden.class);

    @BeforeEach
    void setup() {
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
                1L,
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

        Location location = new Location("Country", "City", "Suburb", "StreetNumber", "Street", "Postcode");
        garden.setLocation(location);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        Mockito.when(gardenService.getGarden(1L)).thenReturn(Optional.of(garden));
        Mockito.when(gardenService.getGarden(Mockito.anyLong())).thenReturn(Optional.of(garden));
        Mockito.when(gardenService.addGarden(any(Garden.class))).thenReturn(garden);
        Mockito.when(locationService.saveLocation(Mockito.any(Location.class))).thenReturn(location);
        Mockito.when(alertService.getAllActiveAlertsFromGarden(Mockito.anyLong())).thenReturn(new ArrayList<>());
        Mockito.when(userService.getUserFromAuthentication(any(Authentication.class))).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);

        this.controller = new GardenFormController(gardenService, userService, plantService, weatherService, locationService, profanityFilterService, alertService, mailService, messageSource, recentGardensService);
    }

    // Tests for /garden endpoint - getting 'create' form and creating gardens
    @Test
    void GetGardenForm_StatusOk() throws Exception {
        mockMvc.perform(get("/garden"))
                .andExpect(status().isOk());
    }

    @Test
    void SubmitGarden_ValidGarden_RedirectsCorrectly() throws Exception {
        MvcResult result = mockMvc.perform(post("/garden")
                        .with(csrf())
                        .param("name", "valid")
                        .param("location", "formatted")
                        .param("country", "Country")
                        .param("city", "City")
                        .param("size", "100")
                        .param("description", "Description"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String redirectedUrl = Objects.requireNonNull(result.getResponse().getRedirectedUrl());
        Assertions.assertTrue(redirectedUrl.matches("/garden/[0-9]+"));
    }

    @Test
    void SubmitGarden_InvalidGarden_NoRedirect() throws Exception {
        mockMvc.perform(post("/garden")
                        .with(csrf())
                        .param("name", "invalid/")
                        .param("location", "formatted")
                        .param("country", "Country")
                        .param("city", "City")
                        .param("size", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void SubmitGarden_ValidGarden_GardenSaved() throws Exception {
        mockMvc.perform(post("/garden")
                .with(csrf())
                .param("name", "name")
                .param("country", "Country")
                .param("location", "formatted")
                .param("city", "City")
                .param("description", "Description")
                .param("size", "9.5"));
        verify(gardenService).addGarden(gardenArgumentCaptor.capture());
    }

    @Test
    void SubmitGarden_InvalidCount_GardenNotSaved() throws Exception {
        mockMvc.perform(post("/garden")
                .with(csrf())
                .param("name", "name")
                .param("location", "formatted")
                .param("country", "Country")
                .param("city", "City")
                .param("size", "bad"));
        verify(gardenService, Mockito.never()).addGarden(Mockito.any());
    }

    @Test
    void SubmitGarden_InvalidName_GardenNotSaved() throws Exception {
        mockMvc.perform(post("/garden")
                .with(csrf())
                .param("name", "//")
                .param("country", "Country")
                .param("city", "City")
                .param("size", "9.5"));
        verify(gardenService, Mockito.never()).addGarden(Mockito.any());
    }

    // Tests for /garden/responses endpoint - getting list of gardens
    @Test
    void GetGardens_RequestOwnPage_StatusOk() throws Exception {
        mockMvc.perform(get("/garden/responses")).andExpect(status().isOk());
    }

    // Tests /garden/{id}/edit endpoint - editing individual gardens
    @Test
    void EditGarden_GetEditForm_StatusOk() throws Exception {
        mockMvc.perform(get("/garden/1/edit")).andExpect(status().isOk());
    }

    @Test
    void EditGarden_ValidGarden_GardenSaved() throws Exception {
        mockMvc.perform(post("/garden/1/edit")
                .with(csrf())
                .param("name", "name")
                .param("location", "formatted")
                .param("country", "Country")
                .param("city", "City")
                .param("size", "9.5")
                .param("description", "Description"));
        verify(gardenService).addGarden(gardenArgumentCaptor.capture());
    }

    @Test
    void EditGarden_InvalidCount_GardenNotSaved() throws Exception {
        mockMvc.perform(post("/garden/1/edit")
                .with(csrf())
                .param("name", "name")
                .param("country", "Country")
                .param("city", "City")
                .param("size", "bad"));
        verify(gardenService, Mockito.never()).addGarden(Mockito.any());
    }

    @Test
    void EditGarden_InvalidName_GardenNotSaved() throws Exception {
        mockMvc.perform(post("/garden/1/edit")
                .with(csrf())
                .param("name", "//")
                .param("country", "Country")
                .param("city", "City")
                .param("size", "9.5"));
        verify(gardenService, Mockito.never()).addGarden(Mockito.any());
    }

    @Test
    void GetPlantForm_GardenExists_StatusOk() throws Exception {
        mockMvc.perform(get("/garden/1")).andExpect(status().isOk());
    }

    @Test
    void addTag_SubmitForm_RedirectedBackToGardenDetails() throws Exception {
        final String content = "VALID TAG";
        mockMvc.perform(post("/garden/1/tag")
                        .with(csrf())
                        .param("tag", content)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/garden/1")
                );
    }
    @Test
    void addTag_ValidString_TagIsSaved() throws Exception {
        final String content = "VALID TAG";
        mockMvc.perform(post("/garden/1/tag")
                .with(csrf())
                .param("tag", content)
        );
        verify(gardenService, times(1))
                .addTagToGarden(Mockito.any(Garden.class), Mockito.anyString());
    }

    @Test
    void addTag_ValidStringWithWhiteSpace_TagIsStripped() throws Exception {
        ArgumentCaptor<String> tagCaptor = ArgumentCaptor.forClass(String.class);
        final String content = "       WHITESPACE TAG        ";
        mockMvc.perform(post("/garden/1/tag")
                .with(csrf())
                .param("tag", content)
        );
        verify(gardenService, times(1))
                .addTagToGarden(Mockito.any(Garden.class), tagCaptor.capture());
        Assertions.assertEquals(content.strip(), tagCaptor.getValue());
    }

    @Test
    void addTag_EmptyString_TagIsNotSaved() throws Exception {
        mockMvc.perform(post("/garden/1/tag")
                .with(csrf())
                .param("tag", "")
        );
        verify(gardenService, never())
                .addTagToGarden(Mockito.any(Garden.class), Mockito.anyString());
    }

    @Test
    void addTag_ValidTag_UserIsOwner_TagIsSaved() throws Exception {
        String validTag = "ValidTag";
        when(gardenService.addTagToGarden(any(Garden.class), eq(validTag))).thenReturn(true);

        mockMvc.perform(post("/garden/1/tag")
                        .with(csrf())
                        .param("tag", validTag)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/garden/1"));

        verify(gardenService, times(1)).addTagToGarden(any(Garden.class), eq(validTag));
    }

    @Test
    void addTag_InvalidTag_TagIsNotSaved_UserReceivesStrike() throws Exception {
        String invalidTag = "InappropriateTag";

        doAnswer(invocation -> {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            userService.addStrike(currentUser);
            return false;
        }).when(gardenService).addTagToGarden(any(Garden.class), eq(invalidTag));

        mockMvc.perform(post("/garden/1/tag")
                        .with(csrf())
                        .param("tag", invalidTag)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/garden/1"))
                .andExpect(flash().attributeExists("tagError"));

        verify(gardenService, times(1)).addTagToGarden(any(Garden.class), eq(invalidTag));
        verify(userService, times(1)).addStrike(any(User.class));
    }

    @Test
    void addTag_UserHasFiveStrikes_TagNotSavedAndFifthStrikeMessageShown() throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user.setNumberOfStrikes(5);
        String invalidTag = "InappropriateTag";
        doAnswer(invocation -> {
            userService.addStrike(user);
            return false;
        }).when(gardenService).addTagToGarden(any(Garden.class), eq(invalidTag));

        mockMvc.perform(post("/garden/1/tag")
                        .with(csrf())
                        .param("tag", invalidTag)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/garden/1"))
                .andExpect(flash().attributeExists("tagError"))
                .andExpect(flash().attributeExists("fifthStrike"));

        verify(userService, times(1)).addStrike(any(User.class));
        verify(mailService, times(1)).sendFifthStrikesEmail(user, Locale.ENGLISH);
    }

    @Test
    void addTag_UserHasSixStrikes_UserIsBannedAndRedirectedToLogin() throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user.setNumberOfStrikes(6);
        String invalidTag = "InappropriateTag";

        doAnswer(invocation -> {
            userService.addStrike(user);
            return false;
        }).when(gardenService).addTagToGarden(any(Garden.class), eq(invalidTag));

        mockMvc.perform(post("/garden/1/tag")
                        .with(csrf())
                        .param("tag", invalidTag)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("blockedMessage"));

        verify(userService, times(1)).banUserForDays(user, 7);
        verify(mailService, times(1)).sendAccountBlockedEmail(user, Locale.ENGLISH);
    }

}
