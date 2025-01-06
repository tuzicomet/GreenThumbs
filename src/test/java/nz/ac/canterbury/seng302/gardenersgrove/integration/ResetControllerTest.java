package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.ResetController;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResetController.class)
class ResetControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserService userService;

    @MockBean
    MailService mailService;

    @MockBean
    VerificationTokenService verificationTokenService;

    @MockBean
    PasswordEncoder passwordEncoder;

    // GardenRepository is required by GlobalControllerAdvice
    @MockBean
    private GardenRepository gardenRepository;

    private VerificationToken validToken;
    private VerificationToken expiredToken;

    private User user;

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
        expiredToken = new VerificationToken("expiredToken", user);
        expiredToken.setExpiryDate(new Timestamp(System.currentTimeMillis() - 100000));


        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

    }

    @Test
    void GetRequest_ValidToken_AddTokenToModel() throws Exception {
        when(verificationTokenService.findByToken("validToken")).thenReturn(validToken);
        mockMvc.perform(get("/reset?token=validToken")
                        .with(csrf()))
                .andExpect(model().attribute("token", "validToken"));
    }

    @Test
    void GetRequest_InvalidToken_RedirectLogin() throws Exception {
        when(verificationTokenService.findByToken("expiredToken")).thenReturn(expiredToken);
        mockMvc.perform(get("/reset?token=expiredToken")
                        .with(csrf()))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void PostRequest_ValidToken_UpdatePassword() throws Exception {
        when(verificationTokenService.findByToken("validToken")).thenReturn(validToken);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        mockMvc.perform(post("/reset")
                        .param("token", "validToken")
                        .param("password", "Testp4$$")
                        .param("confirmPassword", "Testp4$$")
                        .with(csrf()))
                .andExpect(redirectedUrl("/login?resetSuccess"));
        verify(userService).updatePassword(eq(user.getUserId()), eq("encodedPassword"));
        verify(verificationTokenService).deleteVerificationToken(validToken);
    }

    @Test
    void PostRequest_ExpiredToken_InvalidRequest() throws Exception {
        when(verificationTokenService.findByToken("expiredToken")).thenReturn(expiredToken);
        mockMvc.perform(post("/reset")
                        .param("token", "expiredToken")
                        .param("password", "Testp4$$")
                        .param("confirmPassword", "Testp4$$")
                        .with(csrf()))
                .andExpect(model().attribute("message", "Invalid or expired token"));
    }

    @Test
    void PostRequest_InvalidPassword_InvalidRequest() throws Exception {
        mockMvc.perform(post("/reset")
                .param("token", "validToken")
                .param("password", "Testp")
                .param("confirmPassword", "Testp4$$")
                .with(csrf()))
                .andExpect(view().name("resetTemplate"));
    }




}
