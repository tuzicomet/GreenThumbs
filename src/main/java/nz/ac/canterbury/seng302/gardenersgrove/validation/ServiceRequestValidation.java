package nz.ac.canterbury.seng302.gardenersgrove.validation;

import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;

import static nz.ac.canterbury.seng302.gardenersgrove.validation.FormValidation.parseDate;

/**
 * Class for validating service request fields.
 */
public class ServiceRequestValidation {
    // Allows alphanumerics, spaces, dots, commas, hyphens, and apostrophes. Must have at least one letter.
    public static final String BASIC_FORMAT_REGEX = "(?=.*\\p{L}.*)[\\p{L}\\p{M} \\-']+";
    static final String PRICE_REGEX = "^[\\d]+([.,][\\d]{1,2})?$";

    private ServiceRequestValidation() {
        throw new IllegalStateException("Utility classes should not be initialized");
    }

    /**
     * Add translated error to model, attribute name needs to be the i18n group name, and errorId needs to be the i18n ID.
     */
    private static void addErrorToModel(String errorId, String attributeName, Model model, Locale locale, MessageSource messageSource) {
        String message = messageSource.getMessage("%s.%s".formatted(attributeName, errorId), null, locale);
        model.addAttribute(attributeName, message);
    }

    /**
     * Validates the dates given and adds the errors to the given model.
     * Returning a LocalDate does not mean there aren't errors
     * @param date Date to validate
     * @param attributeName Name of the model attribute the errors will be added to. Should be the same as the i18n group
     * @param model Model to add errors to
     * @param locale Locale to translate error messages to
     * @param messageSource Source of i18n messages
     * @return Returns a LocalDate if it was able to parse, otherwise Optional.empty().
     */
    public static Optional<LocalDate> validateDate(String date, String attributeName, Model model, Locale locale, MessageSource messageSource) {
        try {
            LocalDate dateParsed = parseDate(date);
            if (dateParsed == null) {
                addErrorToModel("empty", attributeName, model, locale, messageSource);
                return Optional.empty();
            }
            if (dateParsed.isBefore(LocalDate.now(ZoneId.systemDefault()))) {
                addErrorToModel("pastDate", attributeName, model, locale, messageSource);
            }
            // Date must be less than a year away
            if (dateParsed.isAfter(LocalDate.now(ZoneId.systemDefault()).plusYears(1).minusDays(1))) {
                addErrorToModel("tooFarInFuture", attributeName, model, locale, messageSource);
            }
            return Optional.of(dateParsed);
        } catch (DateTimeParseException e) {
            String message = messageSource.getMessage("error.dateFormat", null, locale);
            model.addAttribute(attributeName, message);
            return Optional.empty();
        }
    }

    /**
     * Validates the title given and adds the errors to the given model.
     * Returning a String does not mean there aren't errors
     * @param title Description to validate
     * @param attributeName Name of the model attribute the errors will be added to. Should be the same as the i18n group
     * @param model Model to add errors to
     * @param locale Locale to translate error messages to
     * @param messageSource Source of i18n messages
     * @return Returns a LocalDate if it was able to parse, otherwise Optional.empty().
     */
    public static String validateTitle(String title, String attributeName, Model model, Locale locale, MessageSource messageSource) {
        if (title.trim().isEmpty()) {
            String message = messageSource.getMessage("errorTitle.titleEmpty", null, locale);
            model.addAttribute(attributeName, message);
            return null;
        }

        if (title.length() > 32) {
            String message = messageSource.getMessage("errorTitle.titleTooLong", null, locale);
            model.addAttribute(attributeName, message);
            return null;
        }

        if (!title.matches(BASIC_FORMAT_REGEX)) {
            String message = messageSource.getMessage("errorTitle.title", null, locale);
            model.addAttribute(attributeName, message);
            return null;
        }

        return title;
    }

    /**
     * Validates the description given and adds the errors to the given model.
     * Returning a String does not mean there aren't errors
     * @param description Description to validate
     * @param attributeName Name of the model attribute the errors will be added to. Should be the same as the i18n group
     * @param model Model to add errors to
     * @param locale Locale to translate error messages to
     * @param messageSource Source of i18n messages
     * @return Returns a LocalDate if it was able to parse, otherwise Optional.empty().
     */
    public static String validateDescription(String description, String attributeName, Model model, Locale locale, MessageSource messageSource) {
        if (description.trim().isEmpty() || description.length() > 512 || !description.matches(BASIC_FORMAT_REGEX)) {
            String message = messageSource.getMessage("errorDescription.description", null, locale);
            model.addAttribute(attributeName, message);
            return null;
        }

        return description;
    }

    /**
     * Validates the minimum price given and adds errors to the given model if required.
     * @param priceMin the minimum price given
     * @param attributeName Name of the model attribute the errors will be added to.
     * @param model Model to add errors to
     * @param messageSource Source of i18n messages
     * @param locale Locale to translate error messages to
     */
    public static Double validatePriceMin(
            String priceMin, String attributeName,
            Model model, MessageSource messageSource, Locale locale
    ) {
        // replace any use of , in the price with .
        String formattedPriceMin = priceMin.replace(",", ".").trim();

        // Price must either be an integer, or a number followed by a . or , then two decimal places
        final String PRICE_REGEX = "^\\d+([.,]\\d{1,2})?$";

        // If the input does not meet the price regex, or is empty
        if (!formattedPriceMin.matches(PRICE_REGEX) || formattedPriceMin.trim().isEmpty()) {
            String message = messageSource.getMessage("error.minPriceInvalid", null, locale);
            model.addAttribute(attributeName, message);
            return null;
        }

        // If it meets regex, it should be safe to convert into a double
        double minValue = Double.parseDouble(formattedPriceMin);
        // make sure the value is not less than 0, but not over 100,000
        if (minValue < 0 || minValue > 100000) {
            String message = messageSource.getMessage("error.minPriceInvalid", null, locale);
            model.addAttribute(attributeName, message);
            return null;
        }

        // if everything is successful, return the formatted minValue double
        return minValue;
    }

    /**
     * Validates the maximum price given and adds errors to the given model if required.
     * @param priceMin the minimum price given
     * @param priceMax the maximum price given
     * @param attributeName Name of the model attribute the errors will be added to.
     * @param model Model to add errors to
     * @param messageSource Source of i18n messages
     * @param locale Locale to translate error messages to
     */
    public static Double validatePriceMax(
            String priceMin, String priceMax, String attributeName,
            Model model, MessageSource messageSource, Locale locale
    ) {
        String formattedPriceMin = priceMin.replace(",", ".").trim();
        String formattedPriceMax = priceMax.replace(",", ".").trim();

        // Price must either be an integer, or a number followed by a . or , then two decimal places
        final String PRICE_REGEX = "^\\d+([.,]\\d{1,2})?$";

        // First check that the priceMax string meets regex and is not empty
        if (!formattedPriceMax.matches(PRICE_REGEX) || formattedPriceMax.trim().isEmpty()) {
            String message = messageSource.getMessage("error.maxPriceInvalid", null, locale);
            model.addAttribute(attributeName, message);
            return null;
        }

        // If it meets regex, it should be safe to convert into a double
        double maxValue = Double.parseDouble(formattedPriceMax);
        // ensure that maxValue is not less than 0, but not over 100,000
        if (maxValue < 0|| maxValue > 100000) {
            String message = messageSource.getMessage("error.maxPriceInvalid", null, locale);
            model.addAttribute(attributeName, message);
            return null;
        }

        // check if a minimum price is present & valid. If not, then we can just return null without giving an error
        if (formattedPriceMin.isEmpty() || !formattedPriceMin.matches(PRICE_REGEX)) {
            return null;
        }
        double minValue = Double.parseDouble(formattedPriceMin);

        // Check that the maximum value is greater or equal to the minimum value
        if (minValue > maxValue) {
            String message = messageSource.getMessage("error.maxPriceLessThanMin", null, locale);
            model.addAttribute(attributeName, message);
            return null;
        }

        // if everything is successful, we can return maxValue
        return maxValue;
    }

    /**
     * Validates the max distance given and adds errors to the given model if required.
     * @param maxDistance the minimum price given
     * @param attributeName Name of the model attribute the errors will be added to.
     * @param model Model to add errors to
     * @param messageSource Source of i18n messages
     * @param locale Locale to translate error messages to
     */
    public static Double validateMaxDistance(
            String maxDistance, String attributeName,
            Model model, MessageSource messageSource, Locale locale
    ) {
        // replace any use of , in the distance with .
        String formattedMaxDistance = maxDistance.replace(",", ".").trim();


        // If the input does not meet the price regex, or is empty
        if (!formattedMaxDistance.matches(PRICE_REGEX) || formattedMaxDistance.trim().isEmpty()) {
            String message = messageSource.getMessage("error.maxDistanceInvalid", null, locale);
            model.addAttribute(attributeName, message);
            return null;
        }

        // If it meets regex, it should be safe to convert into a double
        double distance = Double.parseDouble(formattedMaxDistance);
        // make sure the value is not less than 0, but not over 100,000
        if (distance < 0 || distance > 100000) {
            String message = messageSource.getMessage("error.maxDistanceInvalid", null, locale);
            model.addAttribute(attributeName, message);
            return null;
        }

        // if everything is successful, return the formatted minValue double
        return distance;
    }

    /**
     * Validates that a requested ServiceRequest exists (requested via a URL PathVariable)
     * If database doesn't return a value, the ServiceRequest doesn't exist so should show an error.
     * @param serviceRequestResult Optional<ServiceRequest> object resulting from a database lookup
     * @return ServiceRequest if it is present in serviceRequestResult
     * @throws ResponseStatusException if ServiceRequest doesn't exist in the database, user is requesting invalid page.
     */
    public static ServiceRequest validateServiceRequestExists(Optional<ServiceRequest> serviceRequestResult) throws ResponseStatusException {
        if (serviceRequestResult.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service Request not found.");
        }
        return serviceRequestResult.get();
    }


    // Application for a service Validation

    /**
     * Validates the minimum price given and adds errors to the given model if required.
     * @param price the minimum price given
     * @param attributeName Name of the model attribute the errors will be added to.
     * @param model Model to add errors to
     * @param messageSource Source of i18n messages
     * @param locale Locale to translate error messages to
     */
    public static Double validateApplicationPrice(
            String price, String attributeName,
            Model model, MessageSource messageSource, Locale locale, ServiceRequest serviceRequest
    ) {
        // replace any use of , in the price with .
        String formattedPrice = price.replace(",", ".").trim();

        // Price must either be an integer, or a number followed by a . or , then two decimal places
        final String PRICE_REGEX = "^[0-9]+([.,][0-9]{1,2})?$";

        // If the input does not meet the price regex, or is empty
        if (!formattedPrice.matches(PRICE_REGEX) || formattedPrice.trim().isEmpty()) {
            String message = messageSource.getMessage("error.priceInvalid", null, locale) + " $" + serviceRequest.getPriceMin() + " - $" + serviceRequest.getPriceMax();
            model.addAttribute(attributeName, message);
            return null;
        }

        // If it meets regex, it should be safe to convert into a double
        double priceValue = Double.parseDouble(formattedPrice);
        // make sure the value is not less than 0, but not over 100,000
        if (priceValue < serviceRequest.getPriceMin() || priceValue > serviceRequest.getPriceMax()) {
            String message = messageSource.getMessage("error.priceInvalid", null, locale) + " $" + serviceRequest.getPriceMin() + " - $" + serviceRequest.getPriceMax();
            model.addAttribute(attributeName, message);
            return null;
        }

        // if everything is successful, return the formatted minValue double
        return priceValue;
    }

    /**
     * Validates the date field
     * @param date the date to validate
     * @param attributeName Name of the model attribute the errors will be added to.
     * @param model Model to add errors to
     * @param messageSource Source of i18n messages
     * @param locale Locale to translate error messages to
     */
    public static Optional<LocalDate> validateApplicationDate(
            String date, String attributeName,
            Model model, MessageSource messageSource, Locale locale, ServiceRequest serviceRequest) {
        try {
            LocalDate dateParsed = parseDate(date);
            if (dateParsed == null) {
                addErrorToModel("notInRequestedRange", attributeName, model, locale, messageSource);
                return Optional.empty();
            }
            LocalDate dateMin = serviceRequest.getDateMin().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate dateMax = serviceRequest.getDateMax().atZone(ZoneId.systemDefault()).toLocalDate();
            if (dateParsed.isBefore(dateMin)) {
                addErrorToModel("notInRequestedRange", attributeName, model, locale, messageSource);
            }
            if (dateParsed.isAfter(dateMax)) {
                addErrorToModel("notInRequestedRange", attributeName, model, locale, messageSource);
            }
            return Optional.of(dateParsed);
        } catch (DateTimeParseException e) {
            addErrorToModel("notInRequestedRange", attributeName, model, locale, messageSource);
            return Optional.empty();
        }
    }
}