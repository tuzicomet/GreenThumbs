package nz.ac.canterbury.seng302.gardenersgrove.validation;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.*;

/**
 * class for form validation support functions
 */
public class FormValidation {
    public static final String DATE_FORMAT = "dd/MM/yyyy";

    // Allows alphanumerics, spaces, dots, commas, hyphens, and apostrophes. Must have at least one letter.
    static final String BASIC_FORMAT_REGEX = "(?!\\s+$)[0-9\\p{L}\\p{M} .,\\-']+";

    /**
     * Overwrites the default constructor, avoids an illegal state where FormValidation is initialized.
     */
    private FormValidation() {
        throw new IllegalStateException("FormValidation is a static class. It should never be initialized.");
    }

    /**
     * Performs a validity check on all parameters of a garden post
     *
     * @param name        name to validity check
     * @param country     country to validity check
     * @param city        city to validity check
     * @param size        size to validity check
     * @param description description to validity check
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return a list of error messages for the given fields (or "" if no error), in the order they are entered as parameters.
     */
    public static List<String> validateGardenPost(String name, String country, String city, String suburb,
                                                  String street, String postcode, String size, String description,
                                                  MessageSource messageSource, Locale locale, ProfanityFilterService profanityFilterService) {
        return Arrays.asList(
            validateGardenName(name, messageSource, locale),
            validateGardenCountry(country, messageSource, locale),
            validateGardenCity(city, messageSource, locale),
            validateGardenLocationBasic(suburb, messageSource, locale),
            validateGardenStreet(street, messageSource, locale),
            validateGardenPostcode(postcode, messageSource, locale),
            validateGardenSize(size, messageSource, locale),
            validateGardenDescription(description, messageSource, locale, profanityFilterService)
        );
    }

    /**
     * Validates a garden name is valid, returning an error message if not.
     * @param gardenName Garden name to validate
     * @return Error message string if name is invalid, otherwise an empty string.
     */
    static String validateGardenName(String gardenName, MessageSource messageSource, Locale locale) {
        if (gardenName.trim().isEmpty()) {
            return messageSource.getMessage("error.gardenNameEmpty", null, locale);
        }
        if (gardenName.length() > 100) {
            return messageSource.getMessage("error.gardenNameTooLong", null, locale);
        }
        if (!gardenName.matches(BASIC_FORMAT_REGEX)) {
            return messageSource.getMessage("error.gardenName", null, locale);
        }
        return "";
    }

    /**
     * Validates a garden country is valid, returning an error message if not.
     * @param country Garden name to validate
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return Error message string if country is invalid, otherwise an empty string.
     */
    static String validateGardenCountry(String country, MessageSource messageSource, Locale locale) {
        if (country.trim().isEmpty()) {
            return messageSource.getMessage("error.countryEmpty", null, locale);
        }
        if (country.length() > 100) {
            return messageSource.getMessage("error.countryTooLong", null, locale);
        }
        if (!country.matches(BASIC_FORMAT_REGEX)) {
            return messageSource.getMessage("error.locationFormat", null, locale);
        }
        return "";
    }

    /**
     * Validates a garden city is valid, returning an error message if not.
     * @param city Garden city to validate
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return Error message string if city is invalid, otherwise an empty string.
     */
    static String validateGardenCity(String city, MessageSource messageSource, Locale locale) {
        if (city.trim().isEmpty()) {
            return messageSource.getMessage("error.cityEmpty", null, locale);
        }
        if (city.length() > 100) {
            return messageSource.getMessage("error.cityTooLong", null, locale);
        }
        if (!city.matches(BASIC_FORMAT_REGEX)) {
            return messageSource.getMessage("error.locationFormat", null, locale);
        }
        return "";
    }

    /**
     * Validates the format of an optional garden location element is valid,
     * returning an error message if not. Checks
     * @param element Garden element to validate
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return Error message string if element is invalid, otherwise an empty string.
     */
    static String validateGardenLocationBasic(String element, MessageSource messageSource, Locale locale) {
        if (element.trim().isEmpty()) {
            return "";
        }
        if (element.length() > 100) {
            return messageSource.getMessage("error.locationTooLong", null, locale);
        }
        if (!element.matches(BASIC_FORMAT_REGEX)) {
            return messageSource.getMessage("error.locationFormat", null, locale);
        }
        return "";
    }

    /**
     * Validates a garden street is valid, return an error message if not.
     * @param street Garden street to validate
     * @return Error message if street is invalid, otherwise an empty string
     */
    static String validateGardenStreet(String street, MessageSource messageSource, Locale locale) {
        final String STREET_REGEX = "(?!\\s+$)[0-9\\p{L}\\p{M} .,\\-'/]+";
        if (street.trim().isEmpty()) {
            return "";
        }
        if (street.length() > 100) {
            return messageSource.getMessage("error.locationTooLong", null, locale);
        }
        if (!street.matches(STREET_REGEX)) {
            return messageSource.getMessage("error.locationFormat", null, locale);
        }
        return "";
    }

    /**
     * Validates a garden postcode is valid, returning an error message if not.
     * @param postcode Garden postcode to validate
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return Error message string if postcode is invalid, otherwise an empty string.
     */
    static String validateGardenPostcode(String postcode, MessageSource messageSource, Locale locale) {
        final String POSTCODE_REGEX = "[A-Za-z0-9\\s-]{3,20}";
        if (postcode.trim().isEmpty()) {
            return "";
        }
        if (!postcode.matches(POSTCODE_REGEX)) {
            return messageSource.getMessage("error.postcodeFormat", null, locale);
        }
        return "";
    }

    /**
     * Validates a garden size is valid, returning an error message if not.
     * @param size Garden size to validate
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return Error message string if size is invalid, otherwise an empty string.
     */
    static String validateGardenSize(String size, MessageSource messageSource, Locale locale){
        if (size.isEmpty()) {
            return "";
        }
        // check the input is a positive integer or decimal
        if(!size.matches("\\d+[.,]?\\d+") && !size.matches("\\d")){
            return messageSource.getMessage("error.gardenSizeMustBePositive", null, locale);
        }
        // check the input is not too long
        if(size.length() > 10){
            return messageSource.getMessage("error.gardenSizeTooLong", null, locale);
        }

        // replace is required because we allow commas as decimal points, and parseFloat will not parse this format correctly
        String inputAsDecimal = size.replace(",", ".");
        if(parseFloat(inputAsDecimal) < 0.1){
            return messageSource.getMessage("error.gardenSizeTooSmall", null, locale);
        }
        return "";
    }

    /**
     * Validates a garden description is valid, returning an error message if not.
     * @param description Garden description to validate
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return Error message string if description is invalid, otherwise an empty string.
     */
    static String validateGardenDescription(String description, MessageSource messageSource, Locale locale, ProfanityFilterService profanityFilterService) {
        final String CONTAINS_LETTER_REGEX = "^[^\\p{L}]*\\p{L}.*$";
        // Short circuits eval, empty description is valid
        if (description.trim().isEmpty()) {
            return "";
        }
        if (description.length() > 512 || !description.matches(CONTAINS_LETTER_REGEX)) {
            return messageSource.getMessage("error.gardenDescription", null, locale);
        }
        if(profanityFilterService.containsProfanity(description)){
            return messageSource.getMessage("error.gardenDescriptionProfanity", null, locale);
        }
        return "";
    }

    /**
     * Performs a validity check on all parameters of a plant post
     * @param plantName Name to validity check
     * @param plantCount Count to validity check
     * @param description description to validity check
     * @param date date to validity check
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return an array of error messages for the given fields (or "" if no error), in the order they are entered as parameters.
     */
    public static String[] validatePlantPost(String plantName, String plantCount,
                                             String description, String date,
                                             MessageSource messageSource, Locale locale) {
        return new String[] {
                validatePlantName(plantName, messageSource, locale),
                validatePlantCount(plantCount, messageSource, locale),
                validatePlantDescription(description, messageSource, locale),
                validatePlantDate(date, messageSource, locale)
        };
    }

    /**
     * Validates a plant name is valid, returning an error message if not.
     * @param plantName Plant name to validate
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return Error message string if name is invalid, otherwise an empty string.
     */
    static String validatePlantName(String plantName, MessageSource messageSource, Locale locale) {
        if (plantName.trim().isEmpty()) {
            return messageSource.getMessage("error.plantNameEmpty", null, locale);
        }
        if (plantName.length() > 100) {
            return messageSource.getMessage("error.plantNameTooLong", null, locale);
        }
        if (!plantName.matches(BASIC_FORMAT_REGEX)) {
            return messageSource.getMessage("error.plantNameFormat", null, locale);
        }
        return "";
    }

    /**
     * Validates a plant count is valid, returning an error message if not.
     * @param plantCount Plant count to validate
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return Error message string if count is invalid, otherwise an empty string.
     */
    static String validatePlantCount(String plantCount, MessageSource messageSource, Locale locale) {
        final String PLANT_COUNT_REGEX = "\\d*";
        final int PLANT_MAX_DIGITS = 9; // Allow up to 999,999,999
        // Short circuits eval, empty count is valid
        if (plantCount.trim().isEmpty()) {
            return "";
        }
        if (!plantCount.matches(PLANT_COUNT_REGEX)) {
            return messageSource.getMessage("error.plantCount", null, locale);
        }
        if (plantCount.length() > PLANT_MAX_DIGITS) {
            return messageSource.getMessage("error.plantCountTooBig", null, locale);
        }
        int count = Integer.parseInt(plantCount);
        if (count == 0) {
            return messageSource.getMessage("error.plantCount", null, locale);
        }
        return "";
    }

    /**
     * Validates a plant description is valid, returning an error message if not.
     * @param plantDescription Plant description to validate
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return Error message string if description is invalid, otherwise an empty string.
     */
    static String validatePlantDescription(String plantDescription, MessageSource messageSource, Locale locale) {
        final String CONTAINS_LETTER_REGEX = "^[^\\p{L}]*\\p{L}.*$";
        // Short circuits eval, empty count is valid
        if (plantDescription.trim().isEmpty()) {
            return "";
        }
        if (plantDescription.length() > 512 || !plantDescription.matches(CONTAINS_LETTER_REGEX)) {
            return messageSource.getMessage("error.plantDescription", null, locale);
        }
        return "";
    }

    /**
     * Validates a plant's planted date is valid, returning an error message if not.
     * @param plantedDateString Planted date to validate
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return Error message string if date is invalid, otherwise an empty string.
     */
    static String validatePlantDate(String plantedDateString, MessageSource messageSource, Locale locale) {
        // Short circuits eval, empty count is valid
        if (plantedDateString.isEmpty()) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        SimpleDateFormat invalidDateFormatter = new SimpleDateFormat(DATE_FORMAT);
        LocalDate plantedDate;
        try {
            plantedDate = LocalDate.parse(plantedDateString, formatter);
            // Makes sure a day like 31/9/23 doesn't roll over to 1/10/23, instead throws an error.
            invalidDateFormatter.setLenient(false);
            invalidDateFormatter.parse(plantedDateString);
        } catch (DateTimeParseException e) { // Catches completely invalid dates
            return messageSource.getMessage("error.dateFormat", null, locale);
        } catch (ParseException e) { // Catches roll-over dates, i.e. 31/9/23
            return messageSource.getMessage("error.dateDoesNotExist", null, locale);
        }
        if (plantedDate.isAfter(LocalDate.now())) {
            return messageSource.getMessage("error.dateInFuture", null, locale);
        }
        if (plantedDate.isBefore(LocalDate.now().minusYears(120))) {
            return messageSource.getMessage("error.dateTooLongAgo", null, locale);
        }
        return "";
    }

    /**
     * Parses a valid string as a float, accepting European or regular format
     * @param num string to parse
     * @return parsed float
     */
    public static float parseFloat(String num){
        if (num.isEmpty()) {
            return 0;
        }
        String commaRemoved = num.replace(",", ".");
        return Float.parseFloat(commaRemoved);
    }

    /**
     * Parses a valid string as a double, accepting European or regular format
     * @param num string to parse
     * @return parsed double
     */
    public static double parseDouble(String num){
        if (num.isEmpty()) {
            return 0;
        }
        String commaRemoved = num.replace(",", ".");
        return Double.parseDouble(commaRemoved);
    }

    /**
     * parses a string as a LocalDate
     * @param date string to parse
     * @return parsed date
     */
    public static LocalDate parseDate(String date) {
        if (date.isEmpty()) {
            return null;
        } else {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern(DATE_FORMAT)
                    .parseDefaulting(ChronoField.ERA, 1) // Default to AD
                    .toFormatter().withResolverStyle(ResolverStyle.STRICT);
            return LocalDate.parse(date, formatter);
        }
    }

    /**
     * Validates that a requested Garden exists (requested via a URL PathVariable)
     * If database doesn't return a value, the Garden doesn't exist so should show an error.
     * @param gardenResult Optional<Garden> object resulting from a database lookup
     * @return Garden if it is present in gardenResult
     * @throws ResponseStatusException if Garden doesn't exist in the database, user is requesting invalid page.
     */
    public static Garden validateGardenExists(Optional<Garden> gardenResult) throws ResponseStatusException {
        if (gardenResult.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Garden not found.");
        }
        return gardenResult.get();
    }

    /**
     * Validates that a requested plant exists (requested via a URL PathVariable)
     * If database doesn't return a value, the plant doesn't exist so should show an error.
     * @param plantResult Optional<Plant> object resulting from a database lookup
     * @return Plant if it is present in plantResult
     * @throws ResponseStatusException if Plant doesn't exist in the database, user is requesting invalid page.
     */
    public static Plant validatePlantExists(Optional<Plant> plantResult) throws ResponseStatusException {
        if (plantResult.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found.");
        }
        return plantResult.get();
    }

    /**
     * Formats a date object into our desired display format
     * @param date LocalDate to format
     * @return formatted date string
     */
    public static String formatDateDisplay(LocalDate date){
        if(date!=null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-uuuu");
            return date.format(formatter);
        }
        return null;
    }

    /**
     * Formats a date object into the format required to load into the 'date' field in the plant form
     * @param date LocalDate to format
     * @return formatted date string
     */
    public static String formatDateLoadField(LocalDate date){
        if(date!=null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            return date.format(formatter);
        }
        return null;
    }

    /**
     * Processes the referer to return a default string of '/garden/responses' if empty
     * @param referer url of previous page
     * @return formatted referer string
     */
    public static String processRefererWithoutId(String referer){
        return Objects.requireNonNullElse(referer, "/garden/responses");
    }

    /**
     * Processes the referer to return a default string of '/profile' if empty
     * @param referer url of previous page
     * @return formatted referer string
     */
    public static String processRefererWithoutIdContractor(String referer){
        return Objects.requireNonNullElse(referer, "/profile");
    }

    /**
     * Processes the referer to return a default string of '/garden/{id}' if empty
     * @param referer url of previous page
     * @param gardenId id of garden
     * @return formatted referer string
     */
    public static String processRefererWithId(String referer, Long gardenId){
        return Objects.requireNonNullElseGet(referer, () -> "/garden/%d".formatted(gardenId));
    }
}
