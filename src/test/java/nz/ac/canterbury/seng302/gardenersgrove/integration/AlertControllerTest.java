package nz.ac.canterbury.seng302.gardenersgrove.integration;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.component.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Alert;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.AlertRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.AlertService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlertControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    AlertService alertService;
    @Autowired
    AlertRepository alertRepository;
    @Autowired
    UserService userService;
    @Autowired
    GardenService gardenService;
    @Autowired
    GardenRepository gardenRepository;
    @Autowired
    private CustomAuthenticationProvider customAuthProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;
    Garden garden;
    Garden gardenUnowned;
    Alert alert1;
    Alert alert2;
    Alert alertUnowned1;
    Alert alertUnowned2;

    SecurityContext securityContext;


    @BeforeEach
    void setup(){

        AbstractUser user = new User("Real", "User",
                "test@gmail.com", passwordEncoder.encode("Testp4$$"),
                "2000-10-10", null);
        user = userService.addUser(user);
        userService.enableUser(user.getUserId());

        AbstractUser userNotMe = new User("Real", "User",
                "test1@gmail.com", passwordEncoder.encode("Testp4$$"),
                "2000-10-10", null);
        userNotMe = userService.addUser(userNotMe);
        userService.enableUser(userNotMe.getUserId());


        garden = new Garden(
                "Garden 1",
                "1.0",
                user,
                "Valid",
                false,
                null,
                null,
                true,
                null
        );
        garden = gardenService.addGarden(garden);

        List<Alert> alerts = alertService.getAllActiveAlertsFromGarden(garden.getId());
        assertEquals(2, alerts.size());
        alert1 = alerts.get(0);
        alert2 = alerts.get(1);

        gardenUnowned = new Garden(
                "Garden 1",
                "1.0",
                userNotMe,
                "Valid",
                false,
                null,
                null,
                true,
                null
        );
        gardenUnowned = gardenService.addGarden(gardenUnowned);

        List<Alert> alertsUnowned = alertService.getAllActiveAlertsFromGarden(gardenUnowned.getId());
        assertEquals(2, alertsUnowned.size());
        alertUnowned1 = alertsUnowned.get(0);
        alertUnowned2 = alertsUnowned.get(1);

        Authentication authentication = customAuthProvider.authenticate(
        new UsernamePasswordAuthenticationToken("test@gmail.com", "Testp4$$"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    @Test
    void DismissAlert_ValidRequest_AlertIsNotInActiveAlerts() throws Exception{
        mockMvc.perform(post("/closeAlert")
                        .param("alertId", alert1.getId().toString())
                        .param("gardenId", garden.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        List<Alert> activeAlertsAfterPost = alertService.getAllActiveAlertsFromGarden(garden.getId());
        assertEquals(1, activeAlertsAfterPost.size());
        assertFalse(activeAlertsAfterPost.contains(alert1));
        assertTrue(activeAlertsAfterPost.contains(alert2));
    }
    @Test
    void DismissAlert_IAmNotTheOwner_NothingHasChangedForAnyAlert() throws Exception{
        mockMvc.perform(post("/closeAlert")
                        .param("alertId", alertUnowned1.getId().toString())
                        .param("gardenId", gardenUnowned.getId().toString())
                        .with(csrf()))
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertAlertsAreUnchanged();
    }

    @Test
    void DismissAlert_GardenAndAlertDoNotMatch_NothingHasChangedForAnyAlert() throws Exception{
        mockMvc.perform(post("/closeAlert")
                        .param("alertId", alertUnowned1.getId().toString())
                        .param("gardenId", garden.getId().toString())
                        .with(csrf()))
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertAlertsAreUnchanged();
    }

    @Test
    void DismissAlert_GardenDoesNotExist_NothingHasChangedForAnyAlert() throws Exception{
        mockMvc.perform(post("/closeAlert")
                        .param("alertId", alert1.getId().toString())
                        .param("gardenId", "200")
                        .with(csrf()))
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertAlertsAreUnchanged();
    }
    @Test
    void DismissAlert_AlertDoesNotExist_NothingHasChangedForAnyAlert() throws Exception{
        mockMvc.perform(post("/closeAlert")
                        .param("alertId", "200")
                        .param("gardenId", garden.getId().toString())
                        .with(csrf()))
                .andExpect(status().is4xxClientError())
                .andReturn();
        assertAlertsAreUnchanged();
    }

    private void assertAlertsAreUnchanged(){
        List<Alert> activeAlertsAfterPost = alertService.getAllActiveAlertsFromGarden(garden.getId());
        assertEquals(2, activeAlertsAfterPost.size());
        assertTrue(activeAlertsAfterPost.contains(alert1));
        assertTrue(activeAlertsAfterPost.contains(alert2));

        List<Alert> activeAlertsAfterPost2 = alertService.getAllActiveAlertsFromGarden(gardenUnowned.getId());
        assertEquals(2, activeAlertsAfterPost2.size());
        assertTrue(activeAlertsAfterPost2.contains(alertUnowned1));
        assertTrue(activeAlertsAfterPost2.contains(alertUnowned2));
    }
}

