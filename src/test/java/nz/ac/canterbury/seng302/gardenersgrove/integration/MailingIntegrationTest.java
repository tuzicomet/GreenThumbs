package nz.ac.canterbury.seng302.gardenersgrove.integration;

import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.MailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PdfService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.VerificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * Test class to ensure mailing methods are working as expected, and integrating correctly with the appropriate services
 */
@SpringBootTest
@Transactional
class MailingIntegrationTest {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    VerificationTokenService verificationTokenService;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    MessageSource messageSource;
    @Mock
    JavaMailSenderImpl javaMailSenderMock;
    @Mock
    PdfService pdfServiceMock;
    @PersistenceContext
    private EntityManager entityManager;

    @Mock
    MimeMessage mimeMessageMock;
    MailService mailService;

    @BeforeEach
    void setUp(){
        when(javaMailSenderMock.createMimeMessage()).thenReturn(mimeMessageMock);
        mailService = new MailService(verificationTokenService, templateEngine, javaMailSenderMock, messageSource, pdfServiceMock);
    }

    @Test
    @Transactional
    void sendRegistrationEmail_UserHasNoToken_DoesntSendEmail(){
        User user = new User("test", "user", "mailOne@mail.mail", "Password1!", null, null);
        userRepository.save(user);
        mailService.sendRegistrationEmail(user, Locale.US);
        verify(javaMailSenderMock, never()).send(any(MimeMessage.class));
    }

    @Test
    @Transactional
    void sendRegistrationEmail_UserDoesntExist_DoesntSendEmail(){
        User user = new User("test", "user", "mailOne@mail.mail", "Password1!", null, null);

        // Used chatGPT to figure this out
        // These lines prevent an issue with the user being an unsaved instance (trying to check for a user that doesn't exist)
        user = entityManager.merge(user);
        entityManager.detach(user);

        mailService.sendRegistrationEmail(user, Locale.US);

        verify(javaMailSenderMock, never()).send(any(MimeMessage.class));
    }
    @Test
    @Transactional
    void sendRegistrationEmail_UserHasToken_SendsEmail(){
        User user = new User("test", "user", "mailOne@mail.mail", "Password1!", null, null);
        userService.addUser(user);
        mailService.sendRegistrationEmail(user, Locale.US);
        verify(javaMailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendResetTokenEmail_InvalidEmail_DoesntThrowError(){
        assertDoesNotThrow(() -> mailService.sendResetTokenEmail("agfjshdg", "gskjhdagkjhfgsa", Locale.US));
    }
    @Test
    void sendConfirmationEmail_InvalidEmail_DoesntThrowError(){
        assertDoesNotThrow(() -> mailService.sendConfirmationEmail("agfjshdg", Locale.US));
    }
    @Test
    void sendRegistrationEmail_InvalidEmail_DoesntThrowError(){
        User user = new User("test", "user", "m", "Password1!", null, null);
        userService.addUser(user);
        mailService.sendRegistrationEmail(user, Locale.US);
        assertDoesNotThrow(() -> mailService.sendRegistrationEmail(user, Locale.US));
    }

    @Test
    @Transactional
    void sendPasswordChangeEmail_ValidUser_SendsEmail() {
        User user = new User("test", "user", "test@gmail.com", "Password1!", null, null);
        userRepository.save(user);
        mailService.sendPasswordChangeEmail(user, Locale.US);
        verify(javaMailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @Transactional
    void sendPasswordChangeEmail_InvalidEmail_DoesntThrowError(){
        User user = new User("test", "user", "invalid-email", "Password1!", null, null);
        userService.addUser(user);
        assertDoesNotThrow(() -> mailService.sendPasswordChangeEmail(user, Locale.US));
    }

    @Test
    @Transactional
    void sendFifthStrikesEmail_ValidUser_SendsEmail() {
        User user = new User("test", "user", "test@gmail.com", "Password1!", null, null);
        userRepository.save(user);
        mailService.sendFifthStrikesEmail(user, Locale.US);
        verify(javaMailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @Transactional
    void sendFifthStrikesEmail_InvalidEmail_DoesntThrowError(){
        User user = new User("test", "user", "invalid-email", "Password1!", null, null);
        userService.addUser(user);
        assertDoesNotThrow(() -> mailService.sendFifthStrikesEmail(user, Locale.US));
    }

    @Test
    @Transactional
    void sendAccountBlockedEmail_ValidUser_SendsEmail() {
        User user = new User("test", "user", "test@gmail.com", "Password1!", null, null);
        userRepository.save(user);
        mailService.sendAccountBlockedEmail(user, Locale.US);
        verify(javaMailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @Transactional
    void sendAccountBlockedEmail_InvalidEmail_DoesntThrowError(){
        User user = new User("test", "user", "invalid-email", "Password1!", null, null);
        userService.addUser(user);
        assertDoesNotThrow(() -> mailService.sendAccountBlockedEmail(user, Locale.US));
    }

    @Test
    @Transactional
    void sendInvoiceEmail_ValidUsers_SendsEmail() {
        User contractor = new User("contractor", "user", "contractor@test.com", "Testp4$$", null, null);
        User owner = new User("owner", "user", "owner@test.com", "Testp4$$", null, null);
        userRepository.save(contractor);
        userRepository.save(owner);
        byte[] pdfBytes = new byte[0];
        when(pdfServiceMock.getInvoice(any())).thenReturn(pdfBytes);
        mailService.sendInvoiceEmail(contractor, owner, Locale.US, null);
        verify(javaMailSenderMock, times(2)).send(any(MimeMessage.class));
    }

}
