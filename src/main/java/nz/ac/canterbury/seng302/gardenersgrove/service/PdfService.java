package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class to assist with the templating and creation of PDF files.
 */
@Service
public class PdfService {
    TemplateEngine templateEngine;
    private static final Logger LOG = LoggerFactory.getLogger(PdfService.class);

    /**
     * Initialize the PDF Service class
     * @param templateEngine By passing this in instead of creating a new one, means all our setup in MvcConfig is applied
     */
    @Autowired
    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Generates a PDF by filling the given template with the given attributes and saving it with a given name.
     * @param attributeMap Map of key, value pairs to pass to the thymeleaf renderer. Used in the same way as a Spring Model
     * @param templateName Name of the template to make a PDF from
     */
    public byte[] generatePdf(Map<String, Object> attributeMap, String templateName) {
        String htmlString = parseThymeleafTemplate(attributeMap, templateName);
        try {
            return generatePdfFromHtml(htmlString);
        } catch (IOException e) {
            LOG.error("Failed to generate PDF of template {}", templateName, e);
            return null;
        }
    }

    /**
     * Given a set of html, actually generates and saves the PDF.
     * @param html Rendered HTML string, can include inline CSS for styling.
     * @throws IOException If the file could not be created under the given name
     */
    private byte[] generatePdfFromHtml(String html) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Fetches the given template, fills it with the attributes specified in contentMap, and returns the result as HTML.
     * Locale is used so i18n strings will render correctly.
     * @param attributeMap Map of key, value pairs to pass to the thymeleaf renderer. Used in the same way as a Spring Model
     * @param templateName Name of the template to parse to
     * @return HTML string with attributes and locale applied.
     */
    private String parseThymeleafTemplate(Map<String, Object> attributeMap, String templateName) {
        Context context = new Context(LocaleContextHolder.getLocale());
        for (var entry : attributeMap.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }
        return templateEngine.process(templateName, context);
    }

    /**
     * gets an invoice with the specific details from a service request
     * @param serviceRequest the given completed service request
     * @return the invoice as a byte array
     */
    public byte[] getInvoice(ServiceRequest serviceRequest){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).format(formatter);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("request", serviceRequest);
        attributes.put("date", formattedDate);
        return generatePdf(attributes, "invoice");
    }
}