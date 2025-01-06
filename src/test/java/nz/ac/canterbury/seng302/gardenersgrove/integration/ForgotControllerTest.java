package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.ForgotController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
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

import java.util.Locale;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForgotController.class)
class ForgotControllerTest {

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

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userService.getUserByEmail(Mockito.anyString())).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);

    }

    @Test
    void GetRequest_DirectForgot() throws Exception {
        mockMvc.perform(get("/forgot")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void PostForgotForm_SuccesfulMessage() throws Exception {
        mockMvc.perform(post("/forgot")
                .with(csrf())
                .param("email", "test@gmail.com"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(model().attribute("confirmation", "An email was sent to the address if it was recognised"));
    }

    @Test
    void CreatePasswordToken_SuccesfulCreate() throws Exception {
        mockMvc.perform(post("/forgot")
                .with(csrf())
                .param("email", "test@gmail.com"));
        verify(mailService).sendResetTokenEmail(Mockito.any(), Mockito.any(), Mockito.any(Locale.class));
    }

    @Test
    void SubmitEmail_InvalidEmail_RedirectForgot() throws Exception {
        mockMvc.perform(post("/forgot")
                .with(csrf())
                .param("email", "InvalidEmail"))
                .andExpect(view().name("forgotTemplate"));
    }

}
