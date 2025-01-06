package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.ActivationController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.MailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.VerificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivationController.class)
class ActivationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserService userService;

    @MockBean
    MailService mailService;

    @MockBean
    VerificationTokenService verificationTokenService;

    // GardenRepository is required by GlobalControllerAdvice
    @MockBean
    private GardenRepository gardenRepository;

    User user;

    private VerificationToken validToken;
    private VerificationToken secondToken;
    private VerificationToken expiredToken;

    @BeforeEach
    void setup() {
        // Spring security linking mainly sourced from https://stackoverflow.com/questions/360520/unit-testing-with-spring-security
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        user = new User(
                "Mock",
                "User",
                "test@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );

        user.setUserId(1L);

        validToken = new VerificationToken("validToken", user);
        validToken.setExpiryDate(new Timestamp(System.currentTimeMillis() + 100000));
        secondToken = new VerificationToken("secondToken", user);
        secondToken.setExpiryDate(new Timestamp(System.currentTimeMillis() + 100000));
        expiredToken = new VerificationToken("expiredToken", user);
        expiredToken.setExpiryDate(new Timestamp(System.currentTimeMillis() - 100000));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userService.getUserByEmail(Mockito.anyString())).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);

    }

    @Test
    void GetRequest_RealUser() throws Exception {
        mockMvc.perform(get("/activate")
                        .with(csrf())
                        .param("email", "test@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("userEmail", user.getEmail()));
    }

    @Test
    void PostActivate_InvalidToken_DirectActivate() throws Exception {
        when(verificationTokenService.findByUser(user)).thenReturn(expiredToken);
        mockMvc.perform(post("/activate")
                        .param("token", "expiredToken")
                        .param("userEmail", "test@gmail.com")
                        .with(csrf()))
                .andExpect(model().attribute("tokenError", true));
    }

    @Test
    void PostActivate_ValidToken_RedirectLogin() throws Exception {
        when(verificationTokenService.findByUser(user)).thenReturn(validToken);
        mockMvc.perform(post("/activate")
                        .param("token", "validToken")
                        .param("userEmail", user.getEmail())
                        .with(csrf()))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void PostActivate_ValidWrongToken_DirectActivate() throws Exception {
        when(verificationTokenService.findByUser(user)).thenReturn(secondToken);
        mockMvc.perform(post("/activate")
                        .param("token", "validToken")
                        .param("userEmail", "test@gmail.com")
                        .with(csrf()))
                .andExpect(model().attribute("tokenError", true));
    }


}
