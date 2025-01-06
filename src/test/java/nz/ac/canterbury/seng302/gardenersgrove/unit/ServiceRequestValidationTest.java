package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ServiceRequestValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ServiceRequestValidationTest {
    private MessageSource messageSource;
    private Model model;
    private ServiceRequest serviceRequest;

    @Captor
    ArgumentCaptor<String> errorIdCaptor;
    @BeforeEach
    void setup() {
        model = Mockito.mock(Model.class);
        messageSource = Mockito.mock(MessageSource.class);
        serviceRequest = Mockito.mock(ServiceRequest.class);
        when(messageSource.getMessage(anyString(), isNull(), eq(Locale.US))).thenReturn("");
        errorIdCaptor = ArgumentCaptor.forClass(String.class);

        LocalDate dateMin = LocalDate.of(2023, 1, 1);
        LocalDate dateMax = LocalDate.of(2023, 12, 31);
        ZonedDateTime dateMinZoned = dateMin.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime dateMaxZoned = dateMax.atStartOfDay(ZoneId.systemDefault());

        when(serviceRequest.getDateMin()).thenReturn(dateMinZoned.toInstant());
        when(serviceRequest.getDateMax()).thenReturn(dateMaxZoned.toInstant());
    }

    @Test
    void validateApplicationDate_validDateWithinRange_NoErrorAddedToModel() {
        ServiceRequestValidation.validateApplicationDate(
                "15/07/2023",
                "applicationDate",
                model,
                messageSource,
                Locale.US,
                serviceRequest
        );

        verify(model, never()).addAttribute(anyString(), anyString());
    }

    @ParameterizedTest
    @CsvSource({
            "''",
            "01/01/2022",
            "01/01/2024",
            "99/99/9999"
    })
    void validateApplicationDate_invalidDate_AddsErrorToModel(String inputDate) {
        ServiceRequestValidation.validateApplicationDate(
                inputDate,
                "applicationDate",
                model,
                messageSource,
                Locale.US,
                serviceRequest
        );

        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("applicationDate.notInRequestedRange", errorIdCaptor.getValue());
    }

    @Test
    void validateApplicationDate_dateBeforeMin_AddsErrorToModel() {
        ServiceRequestValidation.validateApplicationDate(
                "31/12/2022",
                "applicationDate",
                model,
                messageSource,
                Locale.US,
                serviceRequest
        );

        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("applicationDate.notInRequestedRange", errorIdCaptor.getValue());
    }

    @Test
    void validateApplicationDate_dateAfterMax_AddsErrorToModel() {
        ServiceRequestValidation.validateApplicationDate(
                "01/01/2024",
                "applicationDate",
                model,
                messageSource,
                Locale.US,
                serviceRequest
        );

        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("applicationDate.notInRequestedRange", errorIdCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "15/07/2023",
            "01/01/2023",
            "31/12/2023"
    })
    void validateApplicationDate_validDateWithinRange_ReturnsValidDate(String inputDate) {
        Optional<LocalDate> returnedDate = ServiceRequestValidation.validateApplicationDate(
                inputDate,
                "applicationDate",
                model,
                messageSource,
                Locale.US,
                serviceRequest
        );

        assertTrue(returnedDate.isPresent());
        verify(model, never()).addAttribute(anyString(), anyString());
    }

    @Test
    void validateApplicationDate_invalidDateFormat_AddsErrorToModel() {
        ServiceRequestValidation.validateApplicationDate(
                "99/99/9999",
                "applicationDate",
                model,
                messageSource,
                Locale.US,
                serviceRequest
        );

        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("applicationDate.notInRequestedRange", errorIdCaptor.getValue());
    }

    @Test
    void validateTitle_fullyValid_NoErrorAddedToModel() {
        ServiceRequestValidation.validateTitle(
                "title",
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        verify(model, never()).addAttribute(anyString(), anyString());
    }

    @ParameterizedTest
    @CsvSource({
            "''", // Empty string
            "ffghj^#&#&",
            "&*&",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "31/12/99999"
    })
    void validateTitle_invalid_ReturnsEmptyOptional(String title) {
        String returnedTitle = ServiceRequestValidation.validateTitle(
                title,
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        Assertions.assertNull(returnedTitle);
    }

    @ParameterizedTest
    @CsvSource({
            "ffghj^#&#&",
            "&*&",
            "31/12/99999"
    })
    void validateTitle_invalid_AddsErrorToModel(String inputTitle) {
        ServiceRequestValidation.validateTitle(
                inputTitle,
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("errorTitle.title", errorIdCaptor.getValue());
    }

    @Test
    void validateTitle_emptyString_AddsErrorToModel() {
        ServiceRequestValidation.validateTitle(
                "",
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("errorTitle.titleEmpty", errorIdCaptor.getValue());
    }

    @Test
    void validateTitle_stringTooLong_AddsErrorToModel() {
        ServiceRequestValidation.validateTitle(
                "a".repeat(65),
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("errorTitle.titleTooLong", errorIdCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "Mowing",
            "Cucumber Planting",
            "Weeding"
    })
    void validateTitle_validFormat_ReturnsString(String inputTitle) {
        String returnedTitle = ServiceRequestValidation.validateTitle(
                inputTitle,
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        assertTrue(!returnedTitle.isEmpty());
    }


    @Test
    void validateDescription_fullyValid_NoErrorAddedToModel() {
        ServiceRequestValidation.validateDescription(
                "description",
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        verify(model, never()).addAttribute(anyString(), anyString());
    }

    @ParameterizedTest
    @CsvSource({
            "''", // Empty string
            "2323423",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    })
    void validateDescription_invalid_ReturnsEmptyOptional(String inputDescription) {
        String returnedDescription = ServiceRequestValidation.validateDescription(
                inputDescription,
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        Assertions.assertNull(returnedDescription);
    }

    @Test
    void validateDescription_emptyString_AddsErrorToModel() {
        ServiceRequestValidation.validateDescription(
                "",
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("errorDescription.description", errorIdCaptor.getValue());
    }

    @Test
    void validateDescription_stringTooLong_AddsErrorToModel() {
        ServiceRequestValidation.validateDescription(
                "a".repeat(513),
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("errorDescription.description", errorIdCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "description",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    })
    void validateDescription_validFormat_ReturnsString(String inputDescription) {
        String returnedDescription = ServiceRequestValidation.validateDescription(
                inputDescription,
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        assertTrue(!returnedDescription.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "31/12/99999",
            "^&*(7673",
            "34432"
    })
    void validateDescription_invalid_AddsErrorToModel(String inputDescription) {
        ServiceRequestValidation.validateDescription(
                inputDescription,
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("errorDescription.description", errorIdCaptor.getValue());
    }

    @Test
    void validateDate_fullyValid_NoErrorAddedToModel() {
        ServiceRequestValidation.validateDate(
                LocalDate.now().plusDays(10).format(DateTimeFormatter.ofPattern("dd/MM/uuuu")),
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        verify(model, never()).addAttribute(anyString(), anyString());
    }

    @ParameterizedTest
    @CsvSource({
            "''", // Empty string
            "DD/MM/YYYY",
            "99/10/1890",
            "01/30/1990",
            "31/12/99999"
    })
    void validateDate_invalid_ReturnsEmptyOptional(String inputDate) {
        Optional<LocalDate> returnedDate = ServiceRequestValidation.validateDate(
                inputDate,
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        assertTrue(returnedDate.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "DD/MM/YYYY",
            "99/10/1890",
            "01/30/1990",
            "31/02/2024",
            "29/02/2023",
            "31/12/99999",
            "01/01/0000"
    })
    void validateDate_invalidFormat_AddsErrorToModel(String inputDate) {
        ServiceRequestValidation.validateDate(
                inputDate,
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("error.dateFormat", errorIdCaptor.getValue());
    }

    @Test
    void validateDate_emptyString_AddsErrorToModel() {
        ServiceRequestValidation.validateDate(
                "",
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("sampleAttribute.empty", errorIdCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "31/01/1990",
            "29/02/2000",
            "31/12/9999"
    })
    void validateDate_validFormat_ReturnsDateObject(String inputDate) {
        Optional<LocalDate> returnedDate = ServiceRequestValidation.validateDate(
                inputDate,
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        assertTrue(returnedDate.isPresent());
    }

    @Test
    void validateDate_dayInThePast_AddsErrorToModel() {
        ServiceRequestValidation.validateDate(
                LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/uuuu")),
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("sampleAttribute.pastDate", errorIdCaptor.getValue());
    }

    @Test
    void validateDate_dayMoreThanAYearInFuture_AddsErrorToModel() {
        ServiceRequestValidation.validateDate(
                LocalDate.now().plusYears(1).format(DateTimeFormatter.ofPattern("dd/MM/uuuu")),
                "sampleAttribute",
                model,
                Locale.US,
                messageSource
        );
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("sampleAttribute.tooFarInFuture", errorIdCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0.0",
            "10, 10.0",
            "63452, 63452.0",
            "100000, 100000.0"
    })
    void validatePriceMin_priceMinIsValidInteger_CorrectPriceDoubleReturned(String priceMin, Double expectedReturnedPrice) {
        Double returnedPriceMin = ServiceRequestValidation.validatePriceMin(
                priceMin,
                "sampleAttribute",
                model,
                messageSource,
                Locale.US
        );
        assertEquals(returnedPriceMin, expectedReturnedPrice);
    }

    @ParameterizedTest
    @CsvSource({
            "0.1, 0.1",
            "'23,6', 23.6",
            "99999.9, 99999.9"
    })
    void validatePriceMin_priceMinIsValidWithOneDecimalPlace_CorrectPriceDoubleReturned(String priceMin, Double expectedReturnedPrice) {
        Double returnedPriceMin = ServiceRequestValidation.validatePriceMin(
                priceMin,
                "sampleAttribute",
                model,
                messageSource,
                Locale.US
        );
        assertEquals(returnedPriceMin, expectedReturnedPrice);
    }

    @ParameterizedTest
    @CsvSource({
            "0.01, 0.01",
            "23.65, 23.65",
            "99999.99, 99999.99"
    })
    void validatePriceMin_priceMinIsValidWithTwoDecimalPlaces_CorrectPriceDoubleReturned(String priceMin, Double expectedReturnedPrice) {
        Double returnedPriceMin = ServiceRequestValidation.validatePriceMin(
                priceMin,
                "sampleAttribute",
                model,
                messageSource,
                Locale.US
        );
        assertEquals(returnedPriceMin, expectedReturnedPrice);
    }

    @ParameterizedTest
    @CsvSource({
            "'0,01', 0.01",
            "'23,65', 23.65",
            "'99999,99', 99999.99"
    })
    void validatePriceMin_priceMinIsValidWithCommaAndTwoDecimalPlaces_CorrectPriceDoubleReturned(String priceMin, Double expectedReturnedPrice) {
        Double returnedPriceMin = ServiceRequestValidation.validatePriceMin(
                priceMin,
                "sampleAttribute",
                model,
                messageSource,
                Locale.US
        );
        assertEquals(returnedPriceMin, expectedReturnedPrice);
    }

    @ParameterizedTest
    @CsvSource({
            "''",
            "' '",
            "a",
            "True",
            "this is not a valid price"
    })
    void validatePriceMin_priceMinIsNotANumber_returnsNullAndAddsCorrectError(
            String priceMin) {
        Double returnedPriceMin = ServiceRequestValidation.validatePriceMin(
                priceMin,
                "sampleAttribute",
                model,
                messageSource,
                Locale.US
        );
        Assertions.assertNull(returnedPriceMin);
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("error.minPriceInvalid", errorIdCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "100001",
            "999999999999999999",
            "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"
    })
    void validatePriceMin_priceMinIsOverLimit_returnsNullAndAddsCorrectError(
            String priceMin) {
        Double returnedPriceMin = ServiceRequestValidation.validatePriceMin(
                priceMin,
                "sampleAttribute",
                model,
                messageSource,
                Locale.US
        );
        Assertions.assertNull(returnedPriceMin);
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("error.minPriceInvalid", errorIdCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, 0.0",
            "5, 10, 10.0",
            "89, 100, 100.0",
            "99999, 100000, 100000.0"
    })
    void validatePriceMax_maxPriceIsValidIntAndGreaterThanMinPriceInt_CorrectPriceDoubleReturned(
            String priceMin, String priceMax, Double expectedReturnedPriceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        assertEquals(returnedPriceMax, expectedReturnedPriceMax);
    }

    @ParameterizedTest
    @CsvSource({
            "0.01, 0.01, 0.01",
            "23.64, 23.65, 23.65",
            "0.00, 99999.99, 99999.99"
    })
    void validatePriceMax_maxPriceIsValidWithTwoDecimalsAndGreaterThanMinPriceWithTwoDecimals_CorrectPriceDoubleReturned(
            String priceMin, String priceMax, Double expectedReturnedPriceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        assertEquals(returnedPriceMax, expectedReturnedPriceMax);
    }

    @ParameterizedTest
    @CsvSource({
            "'0,00', '0,01', 0.01",
            "'10,72', '23,65', 23.65",
            "'999,00', '99999,99', 99999.99"
    })
    void validatePriceMax_maxPriceIsValidWithCommaAndTwoDecimalsAndGreaterThanMinPriceWithCommaAndTwoDecimals_CorrectPriceDoubleReturned(
            String priceMin, String priceMax, Double expectedReturnedPriceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        assertEquals(returnedPriceMax, expectedReturnedPriceMax);
    }

    @ParameterizedTest
    @CsvSource({
            "'0,00', '0.01', 0.01",
            "'10,72', '23.65', 23.65",
            "'999,00', '99999.99', 99999.99"
    })
    void validatePriceMax_maxPriceIsValidWithTwoDecimalsAndGreaterThanMinPriceWithCommaAndTwoDecimals_CorrectPriceDoubleReturned(
            String priceMin, String priceMax, Double expectedReturnedPriceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        assertEquals(returnedPriceMax, expectedReturnedPriceMax);
    }

    @ParameterizedTest
    @CsvSource({
            "'0.00', '0,01', 0.01",
            "'10.72', '23,65', 23.65",
            "'999.00', '99999,99', 99999.99"
    })
    void validatePriceMax_maxPriceIsValidWithCommaAndTwoDecimalsAndGreaterThanMinPriceWithTwoDecimals_CorrectPriceDoubleReturned(
            String priceMin, String priceMax, Double expectedReturnedPriceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        assertEquals(returnedPriceMax, expectedReturnedPriceMax);
    }

    @ParameterizedTest
    @CsvSource({
            "'0.00', '1', 1.0",
            "'10.72', '23', 23.0",
            "'999.00', '99999', 99999.0"
    })
    void validatePriceMax_maxPriceIsValidIntAndGreaterThanMinPriceWithTwoDecimals_CorrectPriceDoubleReturned(
            String priceMin, String priceMax, Double expectedReturnedPriceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        assertEquals(returnedPriceMax, expectedReturnedPriceMax);
    }

    @ParameterizedTest
    @CsvSource({
            "'0,00', '1', 1.0",
            "'10,72', '23', 23.0",
            "'999,00', '99999', 99999.0"
    })
    void validatePriceMax_maxPriceIsValidIntAndGreaterThanMinPriceWithCommaAndTwoDecimals_CorrectPriceDoubleReturned(
            String priceMin, String priceMax, Double expectedReturnedPriceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        assertEquals(returnedPriceMax, expectedReturnedPriceMax);
    }

    @ParameterizedTest
    @CsvSource({
            "'0', '0.01', 0.01",
            "'10', '23.65', 23.65",
            "'999', '99999.99', 99999.99"
    })
    void validatePriceMax_maxPriceIsValidWithTwoDecimalsAndGreaterThanMinPriceInt_CorrectPriceDoubleReturned(
            String priceMin, String priceMax, Double expectedReturnedPriceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        assertEquals(returnedPriceMax, expectedReturnedPriceMax);
    }

    @ParameterizedTest
    @CsvSource({
            "'0', '0,01', 0.01",
            "'10', '23,65', 23.65",
            "'999', '99999,99', 99999.99"
    })
    void validatePriceMax_maxPriceIsValidWithCommaAndTwoDecimalsAndGreaterThanMinPriceInt_CorrectPriceDoubleReturned(
            String priceMin, String priceMax, Double expectedReturnedPriceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        assertEquals(returnedPriceMax, expectedReturnedPriceMax);
    }

    @ParameterizedTest
    @CsvSource({
            "'0', '0', 0",
            "'7', '7,0', 7.0",
            "'7', '7.00', 7.00",
            "'10.00', '10.00', 10.00",
            "'999,99', '999,99', 999.99",
            "'1000.9', '1000,9', 1000.9",
    })
    void validatePriceMax_maxPriceIsValidAndEqualToMinPrice_CorrectPriceDoubleReturned(
            String priceMin, String priceMax, Double expectedReturnedPriceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        assertEquals(returnedPriceMax, expectedReturnedPriceMax);
    }

    @ParameterizedTest
    @CsvSource({
            "1, ''",
            "1, ' '",
            "1, a",
            "1, True",
            "1, this is not a valid price"
    })
    void validatePriceMax_priceMaxIsNotANumber_returnsNullAndAddsCorrectError(
            String priceMin, String priceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        Assertions.assertNull(returnedPriceMax);
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("error.maxPriceInvalid", errorIdCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "1, '1.'",
            "1, '1,'",
            "1, 23.",
            "1, '76,'"
    })
    void validatePriceMax_priceMaxIsInvalidEndingWithCommaOrDecimalPoint_returnsNullAndAddsCorrectError(
            String priceMin, String priceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        Assertions.assertNull(returnedPriceMax);
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("error.maxPriceInvalid", errorIdCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "1, '1.567'",
            "1, '1,657'",
            "1, 23.2686",
            "1, '76,368938'"
    })
    void validatePriceMax_priceMaxIsInvalidHasMoreThan2DecimalPlaces_returnsNullAndAddsCorrectError(
            String priceMin, String priceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        Assertions.assertNull(returnedPriceMax);
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("error.maxPriceInvalid", errorIdCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "1, 100001",
            "1, 999999999999999999",
            "1, 9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"
    })
    void validatePriceMax_priceMaxIsOverLimit_returnsNullAndAddsCorrectError(
            String priceMin, String priceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        Assertions.assertNull(returnedPriceMax);
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("error.maxPriceInvalid", errorIdCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "10, 1",
            "10.01, 10.00",
            "1000, 0.00",
            "49.01, 49",
            "'10,99', '10,98'",
            "'99,99', 99.98",
            "99.99, '99,98'",
            "500, '499,99'",
            "'500,01', 500"
    })
    void validatePriceMax_priceMaxIsLessThanPriceMin_returnsNullAndAddsCorrectError(
            String priceMin, String priceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        Assertions.assertNull(returnedPriceMax);
        verify(messageSource).getMessage(errorIdCaptor.capture(), isNull(), eq(Locale.US));
        verify(model).addAttribute(anyString(), anyString());
        assertEquals("error.maxPriceLessThanMin", errorIdCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "'', 10",
            "' ', '10,00'",
            "not a number, 10.00",
            "'5.', 10.00",
            "'5,', 10.00",
            "'5.9898', 10.00",
    })
    void validatePriceMax_priceMinIsInvalid_returnsNullWithNoErrorAdded(
            String priceMin, String priceMax) {
        Double returnedPriceMax = ServiceRequestValidation.validatePriceMax(priceMin, priceMax,
                "sampleAttribute", model, messageSource, Locale.US
        );
        Assertions.assertNull(returnedPriceMax);
        verify(messageSource, never()).getMessage(anyString(), any(), any(Locale.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

}
