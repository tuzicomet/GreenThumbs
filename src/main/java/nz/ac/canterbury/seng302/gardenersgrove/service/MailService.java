package nz.ac.canterbury.seng302.gardenersgrove.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.core.io.ByteArrayResource;
import java.util.Locale;

/**
 * Class to provide methods that send emails to users with spring boot starter mail
 */
@Service
public class MailService {
    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);
    private final JavaMailSenderImpl mailSender;
    private final VerificationTokenService tokenService;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;
    private final PdfService pdfService;


    /**
     * Uses constructor injection to autowire the required beans
     * @param tokenService service used to access verification tokens
     * @param templateEngine thymeleaf, to format html files
     * @param javaMailSender from spring-boot-starter-mail, used to send emails. The properties for SBSM have been configured in application.properties
     * @param messageSource bean for resolving messages for different locales, from the message property files.
     * @param pdfService used to generate PDFs
     */
    @Autowired
    public MailService(VerificationTokenService tokenService, TemplateEngine templateEngine, JavaMailSenderImpl javaMailSender, MessageSource messageSource, PdfService pdfService) {
        this.tokenService = tokenService;
        this.templateEngine = templateEngine;
        this.mailSender = javaMailSender;
        this.messageSource = messageSource;
        this.pdfService = pdfService;
    }

    /**
     * Helper method to send an email using the provided template and context.
     *
     * @param toEmail   the recipient's email address.
     * @param subject   the subject of the email.
     * @param template  the Thymeleaf template to be used.
     * @param context   the context containing variables for the template.
     * @param attachment the attachment to be included in the email.
     * @param attachmentName the name of the attachment.
     */
    private void sendEmail(String toEmail, String subject, String template, Context context, ByteArrayResource attachment, String attachmentName) {
        String body = templateEngine.process(template, context);
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            if (attachment != null && attachmentName != null) {
                helper.addAttachment(attachmentName, attachment);
            }
            mailSender.send(message);
            LOG.info("Sent an email");
        } catch (MessagingException e) {
            LOG.error("Failed to send email");
        }
    }

    /**
     * Given a user, this function sends an email to the user containing a registration token, which has been stored in the database.
     * @param user the user to whom the email should be sent.
     */
    @Async
    public void sendRegistrationEmail(AbstractUser user, Locale locale) {
        VerificationToken tokenObject = tokenService.findByUser(user);
        if (tokenObject != null) {
            Context context = new Context();
            context.setLocale(locale);
            context.setVariable("name", user.getFirstName() + ",");
            context.setVariable("token", tokenObject.getToken());
            sendEmail(user.getEmail(), messageSource.getMessage("registrationEmail.subject", null, locale), "registrationEmailTemplate", context, null, null);
        }
    }

    /**
     * Sends an email to the provided address including a token in the header
     *
     * @param email    email of the user
     * @param token    a randomly generated token that is used for expiration
     */
    @Async
    public void sendResetTokenEmail(String email, String token, Locale locale) {
        String resetUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/reset")
                .queryParam("token", token)
                .toUriString();

        Context context = new Context();
        context.setLocale(locale);
        context.setVariable("resetUrl", resetUrl);
        sendEmail(email, messageSource.getMessage("resetPasswordEmail.subject", null, locale), "resetPasswordEmailTemplate", context, null, null);
    }

    /**
     * Sends an email to the provided address confirming that they have updated
     * their password.
     *
     * @param email    email of the user
     */
    @Async
    public void sendConfirmationEmail(String email, Locale locale) {
        Context context = new Context();
        context.setLocale(locale);
        sendEmail(email, messageSource.getMessage("resetPasswordSuccessEmail.subject", null, locale), "confirmationEmailTemplate", context, null, null);
    }

    /**
     * Given a user, this function will send an email informing the user of a password change
     * @param user the user to whom the email should be sent.
     */
    @Async
    public void sendPasswordChangeEmail(AbstractUser user, Locale locale) {
        Context context = new Context();
        context.setLocale(locale);
        context.setVariable("name", user.getFirstName() + ",");
        sendEmail(user.getEmail(), messageSource.getMessage("passwordChangedSuccessEmail.subject", null, locale), "passwordChangeEmailTemplate", context, null, null);
    }

    /**
     * Given a user, this function sends an email warning the user after their fifth inappropriate tag.
     * @param user the user to whom the email should be sent.
     */
    @Async
    public void sendFifthStrikesEmail(AbstractUser user, Locale locale) {
        Context context = new Context();
        context.setLocale(locale);
        context.setVariable("name", user.getFirstName() + ",");
        sendEmail(user.getEmail(), messageSource.getMessage("fifthStrikeEmail.subject", null, locale), "fifthStrikeEmailTemplate", context, null, null);
    }

    /**
     * Given a user, this function will send an email informing the user of the fact
     * that their account has been blocked after a sixth inappropriate tag was added.
     * @param currentUser the user the email should be sent to.
     */
    @Async
    public void sendAccountBlockedEmail(AbstractUser currentUser, Locale locale) {
        Context context = new Context();
        context.setLocale(locale);
        context.setVariable("name", currentUser.getFirstName() + ",");
        sendEmail(currentUser.getEmail(), messageSource.getMessage("sixthStrikeEmail.subject", null, locale), "sixthStrikeEmailTemplate", context, null, null);
    }

    /**
     * Sends an email to the contractor and owner of a service request with the invoice attached
     * @param contractor the contractor to send the email to
     * @param owner the owner to send the email to
     * @param locale the locale to use for the email
     * @param serviceRequest the service request to generate the invoice for
     */
    @Async
    public void sendInvoiceEmail(AbstractUser contractor, AbstractUser owner, Locale locale, ServiceRequest serviceRequest) {
        Context context = new Context();
        context.setLocale(locale);
        context.setVariable("name", contractor.getFirstName() + ",");
        byte[] pdfBytes = pdfService.getInvoice(serviceRequest);
        ByteArrayResource pdfAttachment = new ByteArrayResource(pdfBytes);
        sendEmail(contractor.getEmail(), messageSource.getMessage("invoiceEmail.subject", null, locale), "invoiceEmailTemplate", context, pdfAttachment, "invoice.pdf");
        context.setVariable("name", owner.getFirstName() + ",");
        sendEmail(owner.getEmail(), messageSource.getMessage("invoiceEmail.subject", null, locale), "invoiceEmailTemplate", context, pdfAttachment, "invoice.pdf");
    }
}