package nz.ac.canterbury.seng302.gardenersgrove.integration;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.component.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.gardenersgrove.controller.HomepageController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.WidgetPreferences;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.RecentGardensService;
import nz.ac.canterbury.seng302.gardenersgrove.service.RecentPlantsService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.WidgetPreferencesService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.UserInformationValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Transactional
class HomepageControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @SpyBean
    private WidgetPreferencesService widgetPreferencesService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserInformationValidator userInformationValidator;
    @Mock
    private RecentGardensService recentGardensService;
    @Mock
    private RecentPlantsService recentPlantsService;
    @Autowired
    private CustomAuthenticationProvider customAuthProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    HomepageController controller;

    User user;
    Long userId;

    @BeforeEach
    void setup() {
        // Spring security linking mainly sourced from https://stackoverflow.com/questions/360520/unit-testing-with-spring-security

        user = new User(
                "Mock",
                "User",
                "test@gmail.com",
                passwordEncoder.encode("Testp4$$"),
                "1990-01-01",
                null
        );
        userRepository.save(user);

        user = (User) userService.getUserByEmail(user.getEmail());
        userId = user.getUserId();

        WidgetPreferences widgetPreferences = new WidgetPreferences(
                userId,
                true,
                true,
                true,
                true
        );

        userService.enableUser(userId);

        Authentication authentication = customAuthProvider.authenticate(
        new UsernamePasswordAuthenticationToken("test@gmail.com", "Testp4$$")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        this.controller = new HomepageController(userRepository, userService, userInformationValidator, widgetPreferencesService, recentGardensService, recentPlantsService);
    }

    // Tests for /homepage endpoint
    @Test
    void GetHomepage_StatusOk() throws Exception {
        mockMvc.perform(get("/homepage"))
                .andExpect(status().isOk());
    }

    // Tests for /homepage/1/edit endpoint - getting 'customise' form
    @Test
    void GetEditHomepageForm_StatusOk() throws Exception {
        mockMvc.perform(get("/homepage/edit")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void SubmitEditHomepageForm_AllWidgetsSelected_WidgetsSaved() throws Exception {
        mockMvc.perform(post("/homepage/edit")
                .with(csrf())
                .param("recentGardens", "true")
                .param("recentPlants", "true")
                .param("friends", "true"));
        WidgetPreferences userWidgetPreferences = widgetPreferencesService.findByUserId(userId);
        Assertions.assertEquals(true, userWidgetPreferences.getRecentGardens());
        Assertions.assertEquals(true, userWidgetPreferences.getRecentPlants());
        Assertions.assertEquals(true, userWidgetPreferences.getFriends());
    }

    @Test
    void SubmitEditHomepageForm_NoWidgetsSelected_WidgetsSaved() throws Exception {
        mockMvc.perform(post("/homepage/edit")
                .with(csrf())
                .param("recentGardens", "")
                .param("recentPlants", "")
                .param("friends", ""));

        WidgetPreferences userWidgetPreferences = widgetPreferencesService.findByUserId(userId);
        verify(widgetPreferencesService).addWidgetPreference(any(WidgetPreferences.class));
        Assertions.assertEquals(false, userWidgetPreferences.getRecentGardens());
        Assertions.assertEquals(false, userWidgetPreferences.getRecentPlants());
        Assertions.assertEquals(false, userWidgetPreferences.getFriends());
    }

    @Test
    void SubmitEditHomepageForm_AllWidgetsSelected_CorrectWidgetShownInMainPage() throws Exception {
        mockMvc.perform(post("/homepage/edit")
                .with(csrf())
                .param("recentGardens", "true")
                .param("recentPlants", "true")
                .param("friends", "true"));
        WidgetPreferences userWidgetPreferences = widgetPreferencesService.findByUserId(userId);
        verify(widgetPreferencesService).addWidgetPreference(any(WidgetPreferences.class));
        Assertions.assertEquals(true, userWidgetPreferences.getRecentGardens());
        Assertions.assertEquals(true, userWidgetPreferences.getRecentPlants());
        Assertions.assertEquals(true, userWidgetPreferences.getFriends());

        mockMvc.perform(get("/homepage"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("recentGardensWidgetEnabled", true))
                .andExpect(model().attribute("recentPlantsWidgetEnabled", true))
                .andExpect(model().attribute("friendWidgetEnabled", true));
    }

    @Test
    void SubmitEditHomepageForm_NoWidgetsSelected_CorrectWidgetShownInMainPage() throws Exception {
        mockMvc.perform(post("/homepage/edit")
                .with(csrf())
                .param("recentGardens", "")
                .param("recentPlants", "")
                .param("friends", ""));
        WidgetPreferences userWidgetPreferences = widgetPreferencesService.findByUserId(userId);
        verify(widgetPreferencesService).addWidgetPreference(any(WidgetPreferences.class));
        Assertions.assertEquals(false, userWidgetPreferences.getRecentGardens());
        Assertions.assertEquals(false, userWidgetPreferences.getRecentPlants());
        Assertions.assertEquals(false, userWidgetPreferences.getFriends());

        mockMvc.perform(get("/homepage"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("friendWidgetEnabled", false))
                .andExpect(model().attribute("recentGardensWidgetEnabled", false))
                .andExpect(model().attribute("recentPlantsWidgetEnabled", false));
    }

   @Test
   void SubmitEditHomepageForm_OnlyRecentGardensWidgetsSelected_CorrectWidgetShownInMainPage() throws Exception {
       mockMvc.perform(post("/homepage/edit")
               .with(csrf())
               .param("recentGardens", "true")
               .param("recentPlants", "")
               .param("friends", ""));
       WidgetPreferences userWidgetPreferences = widgetPreferencesService.findByUserId(userId);
       verify(widgetPreferencesService).addWidgetPreference(any(WidgetPreferences.class));
       Assertions.assertEquals(true, userWidgetPreferences.getRecentGardens());
       Assertions.assertEquals(false, userWidgetPreferences.getRecentPlants());
       Assertions.assertEquals(false, userWidgetPreferences.getFriends());

       mockMvc.perform(get("/homepage"))
               .andExpect(status().isOk())
               .andExpect(model().attribute("recentGardensWidgetEnabled", userWidgetPreferences.getRecentGardens()));
   }

   @Test
   void SubmitEditHomepageForm_OnlyRecentPlantsWidgetsSelected_CorrectWidgetShownInMainPage() throws Exception {
       mockMvc.perform(post("/homepage/edit")
               .with(csrf())
               .param("recentGardens", "")
               .param("recentPlants", "true")
               .param("friends", ""));
       WidgetPreferences userWidgetPreferences = widgetPreferencesService.findByUserId(userId);
       verify(widgetPreferencesService).addWidgetPreference(any(WidgetPreferences.class));
       Assertions.assertEquals(false, userWidgetPreferences.getRecentGardens());
       Assertions.assertEquals(true, userWidgetPreferences.getRecentPlants());
       Assertions.assertEquals(false, userWidgetPreferences.getFriends());

       mockMvc.perform(get("/homepage"))
               .andExpect(status().isOk())
               .andExpect(model().attribute("recentPlantsWidgetEnabled", userWidgetPreferences.getRecentPlants()));
   }

   @Test
   void SubmitEditHomepageForm_OnlyMyFriendsWidgetsSelected_CorrectWidgetShownInMainPage() throws Exception {
       mockMvc.perform(post("/homepage/edit")
               .with(csrf())
               .param("recentGardens", "")
               .param("recentPlants", "")
               .param("friends", "true"));
       
       WidgetPreferences userWidgetPreferences = widgetPreferencesService.findByUserId(userId);
       verify(widgetPreferencesService).addWidgetPreference(any(WidgetPreferences.class));
       Assertions.assertEquals(false, userWidgetPreferences.getRecentGardens());
       Assertions.assertEquals(false, userWidgetPreferences.getRecentPlants());
       Assertions.assertEquals(true, userWidgetPreferences.getFriends());
   
       mockMvc.perform(get("/homepage"))
               .andExpect(status().isOk())
               .andExpect(model().attribute("recentGardensWidgetEnabled", false))
               .andExpect(model().attribute("recentPlantsWidgetEnabled", false))
               .andExpect(model().attribute("friendWidgetEnabled", true));
   }

   @Test
   void SubmitEditHomepageForm_OnlyRecentGardensAndPlantsWidgetsSelected_CorrectWidgetShownInMainPage() throws Exception {
       mockMvc.perform(post("/homepage/edit")
               .with(csrf())
               .param("recentGardens", "true")
               .param("recentPlants", "true")
               .param("friends", ""));
       
       WidgetPreferences userWidgetPreferences = widgetPreferencesService.findByUserId(userId);
       verify(widgetPreferencesService).addWidgetPreference(any(WidgetPreferences.class));
       Assertions.assertEquals(true, userWidgetPreferences.getRecentGardens());
       Assertions.assertEquals(true, userWidgetPreferences.getRecentPlants());
       Assertions.assertEquals(false, userWidgetPreferences.getFriends());
   
       mockMvc.perform(get("/homepage"))
               .andExpect(status().isOk())
               .andExpect(model().attribute("recentGardensWidgetEnabled", userWidgetPreferences.getRecentGardens()))
               .andExpect(model().attribute("recentPlantsWidgetEnabled", userWidgetPreferences.getRecentPlants()))
               .andExpect(model().attribute("friendWidgetEnabled", false));
   }
}
