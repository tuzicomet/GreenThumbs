package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
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

import java.util.Locale;

import static nz.ac.canterbury.seng302.gardenersgrove.validation.ServiceRequestValidation.validateApplicationPrice;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

class ApplicationForServiceValidationTest {
    private MessageSource messageSource;

    private Model model;

    private ServiceRequest serviceRequest;

    private final String attributeName = "errorPrice";

    private String errorMessage;

    @Captor
    ArgumentCaptor<String> errorIdCaptor;

    @BeforeEach
    void setup() {
        model = Mockito.mock(Model.class);

        messageSource = Mockito.mock(MessageSource.class);
        Mockito.when(messageSource.getMessage(anyString(), isNull(), eq(Locale.US))).thenReturn("Error Message Text");
        errorIdCaptor = ArgumentCaptor.forClass(String.class);

        serviceRequest = Mockito.mock(ServiceRequest.class);
        Mockito.when(serviceRequest.getPriceMin()).thenReturn(10.0);
        Mockito.when(serviceRequest.getPriceMax()).thenReturn(20.0);
        errorMessage  = "Error Message Text" + " $" + serviceRequest.getPriceMin() + " - $" + serviceRequest.getPriceMax();
    }

    @Test
    void validatePrice_priceFieldValid_returnsPriceDouble() {
        Double price = validateApplicationPrice("12", attributeName, model, messageSource, Locale
                .US, serviceRequest);
        Assertions.assertNotNull(price);
        Assertions.assertEquals(12, price);
    }

    @Test
    void validatePrice_priceFieldValidDecimal_returnsPriceDouble() {
        Double price = validateApplicationPrice("12.50", attributeName, model, messageSource, Locale
                .US, serviceRequest);
        Assertions.assertNotNull(price);
        Assertions.assertEquals(12.5, price);
    }


    @Test
    void validatePrice_priceFieldValidComma_returnsPriceDouble() {
        Double price = validateApplicationPrice("12,01", attributeName, model, messageSource, Locale
                .US, serviceRequest);
        Assertions.assertNotNull(price);
        Assertions.assertEquals(12.01, price);
    }

    @ParameterizedTest
    @CsvSource({
            "''",
            "' '",
            "not a number",
            "'a.46'",
            "'5.'",
            "'5,'",
            "'5.9898'",
            "'20.1'",
            "'12.10.10'",
            "'12.111'"
    })
    void validatePrice_priceInvalid_returnsNull(String invalidPrice) {
        Double price = validateApplicationPrice(invalidPrice, attributeName, model, messageSource, Locale
                .US, serviceRequest);
        Assertions.assertNull(price);
        verify(model).addAttribute(attributeName, errorMessage);
    }

    @Test
    void validatePrice_priceNegativeNumber_returnsNull() {
        Mockito.when(serviceRequest.getPriceMin()).thenReturn(-10.0);
        errorMessage  = "Error Message Text" + " $" + serviceRequest.getPriceMin() + " - $" + serviceRequest.getPriceMax();

        Double price = validateApplicationPrice("-5", attributeName, model, messageSource, Locale
                .US, serviceRequest);
        Assertions.assertNull(price);
        verify(model).addAttribute(attributeName, errorMessage);
    }


}
