package nz.ac.canterbury.seng302.gardenersgrove.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static nz.ac.canterbury.seng302.gardenersgrove.validation.FormValidation.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlantInputValidationTest {

    @Mock
    MessageSource messageSource;

    // Test error message
    private final String ErrorMessage = "Error Message Text";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // If trying to get any message from messageSource, always return
        // the ErrorMessage string (as actual messages require messageSource)
        when(messageSource.getMessage(any(), isNull(), eq(Locale.US)))
                .thenReturn(ErrorMessage);
    }

    /**
     * Tests the plant post input validation method with blue sky input (all fields are valid)
     * expected to return empty string for all booleans, indicating no errors
     */
    @Test
    void validateInputs_AllFieldsValid_ReturnsNoErrors() {
        String[] result = validatePlantPost("valid name", "7", "valid description/?", "09/05/2003", messageSource, Locale.US);
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }

    /**
     * Tests the plant post input validation method with accentuated letters in the name and description
     * expected to return empty string for all booleans, indicating no errors
     */
    @Test
    void validateInputs_MacronAndDiaeresis_ReturnsNoErrors() {
        String[] result = validatePlantPost("valid māori name", "2", "description with ü", "09/05/2003", messageSource, Locale.US);
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }

    /**
     * Tests the plant post the input validation method with empty count
     * expected to return empty string for all booleans, indicating no errors
     */
    @Test
    void validateInputs_CountIsNull_ReturnsNoErrors() {
        String[] result = validatePlantPost("valid name", "", "description", "09/05/2003", messageSource, Locale.US);
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }

    /**
     * Tests the plant post the input validation method with empty description
     * expected to return empty string for all booleans, indicating no errors
     */
    @Test
    void validateInputs_DescriptionIsNull_ReturnsNoErrors() {
        String[] result = validatePlantPost("valid name", "7", "", "09/05/2003", messageSource, Locale.US);
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }

    /**
     * Tests the plant post input validation method with empty name
     * The name field (result[0]) should have the error.plantNameEmpty error message
     */
    @Test
    void validateInputs_NameIsNull_ReturnsNameError() {
        String[] result = validatePlantPost("", "7", "description", "09/05/2003", messageSource, Locale.US);
        // check that the correct error message key was called for
        verify(messageSource).getMessage(eq("error.plantNameEmpty"), isNull(), eq(Locale.US));
        // check that only the correct field has the error message
        Assertions.assertEquals(ErrorMessage, result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }

    /**
     * Tests the plant post input validation method with name containing only whitespace
     * The name field (result[0]) should have the error.plantNameEmpty error message
     */
    @Test
    void validateInputs_NameNullWithWhitespace_ReturnsNameError() {
        String[] result = validatePlantPost("  ", "7", "description", "09/05/2003", messageSource, Locale.US);
        // check that the correct error message key was called for
        verify(messageSource).getMessage(eq("error.plantNameEmpty"), isNull(), eq(Locale.US));
        // check that only the correct field has the error message
        Assertions.assertEquals(ErrorMessage, result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }

    /**
     * Tests the plant post input validation method with a count value which exceeds the maximum limit
     * The count field (result[1]) should have the error.plantCountTooBig error message
     */
    @Test
    void validateInputs_CountIsTooLarge_ReturnsCountError() {
        String[] result = validatePlantPost("Valid Name", "99999999999999999999999999", "description", "09/05/2003", messageSource, Locale.US);
        // check that the correct error message key was called for
        verify(messageSource).getMessage(eq("error.plantCountTooBig"), isNull(), eq(Locale.US));
        // check that only the correct field has the error message
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals(ErrorMessage, result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }

    /**
     * Tests the plant post input validation method with count = 0
     * The count field (result[1]) should have the error.plantCount error message
     */
    @Test
    void validateInputs_CountIsZero_ReturnsCountError() {
        String[] result = validatePlantPost("Valid Name", "0", "description", "09/05/2003", messageSource, Locale.US);
        // check that the correct error message key was called for
        verify(messageSource).getMessage(eq("error.plantCount"), isNull(), eq(Locale.US));
        // check that only the correct field has the error message
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals(ErrorMessage, result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }

    /**
     * Tests the plant post input validation method with name an invalid count value
     * The count field (result[1]) should have the error.plantCount error message
     */
    @Test
    void validateInputs_CountInvalid_ReturnsCountError() {
        String[] result = validatePlantPost("Valid Name", "2a", "description", "09/05/2003", messageSource, Locale.US);
        // check that the correct error message key was called for
        verify(messageSource).getMessage(eq("error.plantCount"), isNull(), eq(Locale.US));
        // check that only the correct field has the error message
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals(ErrorMessage, result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }

    /**
     * Tests the plant post input validation method with an invalid name
     * The name field (result[0]) should have the error.plantNameFormat error message
     */
    @Test
    void validateInputs_NameInvalid_ReturnsNameFormatError() {
        String[] result = validatePlantPost("invalid name :)", "7", "description??@#$%$", "09/05/2003", messageSource, Locale.US);
        // check that the correct error message key was called for
        verify(messageSource).getMessage(eq("error.plantNameFormat"), isNull(), eq(Locale.US));
        // check that only the correct field has the error message
        Assertions.assertEquals(ErrorMessage, result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }
    
    /**
     * Tests the plant post input validation method with valid inputs including commas, dots, hyphens and apostrophes.
     * expected to return empty string for all booleans, indicating no error
     */
    @Test
    void validateInputs_NameValidWithAllowedSpecialCharacters_ReturnsNoErrors() {
        String[] result = validatePlantPost("valid name-.,'", "7", "hfkjashfjk", "09/05/2003", messageSource, Locale.US);
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }
    
    /**
     * Tests the plant post input validation method with all fields valid. count in European format.
     * expected to return empty string for all booleans, indicating no errors
     */
    @Test
    void validateInputs_AllFieldsValidEuropeanFormat_ReturnsNoErrors() {
        String[] result = validatePlantPost("valid name", "53", "description", "09/05/2003", messageSource, Locale.US);
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }
    
    /**
     * Tests the plant post input validation method with "," as count
     * The count field (result[1]) should have the error.plantCount error message
     */
    @Test
    void validateInputs_DecimalPointOnly_ReturnsCountError() {
        String[] result = validatePlantPost("valid name", ",", "description", "09/05/2003", messageSource, Locale.US);
        // check that the correct error message key was called for
        verify(messageSource).getMessage(eq("error.plantCount"), isNull(), eq(Locale.US));
        // check that only the correct field has the error message
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals(ErrorMessage, result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }
    
    /**
     * Tests the plant post input validation method with date empty
     * expected to return empty string for all booleans, indicating no errors
     */
    @Test
    void validateInputs_DateEmpty_ReturnsNoErrors() {
        String[] result = validatePlantPost("valid name", "53", "description", "", messageSource, Locale.US);
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }
    
    /**
     * Tests the plant post input validation method with date invalid format
     * The date field (result[3]) should have the error.dateFormat error message
     */
    @Test
    void validateInputs_DateInvalidFormat_ReturnsDateFormatError() {
        String[] result = validatePlantPost("valid name", "53", "description", "09-05-2003", messageSource, Locale.US);
        // check that the correct error message key was called for
        verify(messageSource).getMessage(eq("error.dateFormat"), isNull(), eq(Locale.US));
        // check that only the correct field has the error message
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals(ErrorMessage, result[3]);
    }
    
    /**
     * Tests the plant post input validation method with date illegal in valid format
     * The date field (result[3]) should have the error.dateDoesNotExist error message
     */
    @Test
    void validateInputs_ValidFormatIllegalDate_ReturnsDateExistenceError() {
        String[] result = validatePlantPost("valid name", "53", "description", "30/02/2024", messageSource, Locale.US);
        // check that the correct error message key was called for
        verify(messageSource).getMessage(eq("error.dateDoesNotExist"), isNull(), eq(Locale.US));
        // check that only the correct field has the error message
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals(ErrorMessage, result[3]);
    }

    /**
     * Tests the plant post input validation method with a date from the future
     * The date field (result[3]) should have the error.dateInFuture error message
     */
    @Test
    void validateInputs_DateInTheFuture_ReturnsDateInFutureError() {
        String[] result = validatePlantPost("valid name", "53", "description", "01/01/9999", messageSource, Locale.US);
        // check that the correct error message key was called for
        verify(messageSource).getMessage(eq("error.dateInFuture"), isNull(), eq(Locale.US));
        // check that only the correct field has the error messageAssertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals(ErrorMessage, result[3]);
    }

    /**
     * Tests the plant post input validation method with a date which is too old
     * The date field (result[3]) should have the error.dateTooLongAgo error message
     */
    @Test
    void validateInputs_DateTooLongAgo_ReturnsDateTooLongAgoError() {
        String[] result = validatePlantPost("valid name", "53", "description", "01/01/1750", messageSource, Locale.US);
        // check that the correct error message key was called for
        verify(messageSource).getMessage(eq("error.dateTooLongAgo"), isNull(), eq(Locale.US));
        // check that only the correct field has the error message
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals(ErrorMessage, result[3]);
    }

    /**
     * Tests the plant post input validation method with a description which is at the length limit
     */
    @Test
    void validateInputs_NameIsNullAndDescriptionIsAtLimit_ReturnsNoErrors() {
        String[] result = validatePlantPost("Name", "7", "a".repeat(512), "09/05/2003", messageSource, Locale.US);
        // check that no fields have error messages
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }

    /**
     * Tests the plant post input validation method with a description which is one character over the length limit
     */
    @Test
    void validateInputs_NameIsNullAndDescriptionIsOneOverLimit_ReturnsDescriptionError() {
        String[] result = validatePlantPost("Name", "7", "a".repeat(513), "09/05/2003", messageSource, Locale.US);
        // check that the correct error message key was called for
        verify(messageSource).getMessage(eq("error.plantDescription"), isNull(), eq(Locale.US));
        // check that only the correct fields have the error message
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals(ErrorMessage, result[2]);
        Assertions.assertEquals("", result[3]);
    }

    /**
     * Tests the plant post input validation method with empty name and invalid count
     */
    @Test
    void validateInputs_NameIsNullAndCountIsInvalid_ReturnsNameEmptyAndPlantCountErrors() {
        String[] result = validatePlantPost("", "#", "description", "09/05/2003", messageSource, Locale.US);
        // check that the correct error message keys were called for
        verify(messageSource).getMessage(eq("error.plantNameEmpty"), isNull(), eq(Locale.US));
        verify(messageSource).getMessage(eq("error.plantCount"), isNull(), eq(Locale.US));
        // check that only the correct fields have the error message
        Assertions.assertEquals(ErrorMessage, result[0]);
        Assertions.assertEquals(ErrorMessage, result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals("", result[3]);
    }

    /**
     * Tests the plant post input validation method with empty name and a description which is too long
     */
    @Test
    void validateInputs_NameIsNullAndDescriptionIsTooLong_ReturnsNameEmptyAndPlantCountErrors() {
        String[] result = validatePlantPost("", "7", "a".repeat(513), "09/05/2003", messageSource, Locale.US);
        // check that the correct error message keys were called for
        verify(messageSource).getMessage(eq("error.plantNameEmpty"), isNull(), eq(Locale.US));
        verify(messageSource).getMessage(eq("error.plantDescription"), isNull(), eq(Locale.US));
        // check that only the correct fields have the error message
        Assertions.assertEquals(ErrorMessage, result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals(ErrorMessage, result[2]);
        Assertions.assertEquals("", result[3]);
    }

    /**
     * Tests the plant post input validation method with empty name and a date which does not exist
     */
    @Test
    void validateInputs_NameIsNullAndDateIsIllegal_ReturnsNameEmptyAndDateExistenceErrors() {
        String[] result = validatePlantPost("", "3", "description", "30/02/2003", messageSource, Locale.US);
        // check that the correct error message keys were called for
        verify(messageSource).getMessage(eq("error.plantNameEmpty"), isNull(), eq(Locale.US));
        verify(messageSource).getMessage(eq("error.dateDoesNotExist"), isNull(), eq(Locale.US));
        // check that only the correct fields have the error message
        Assertions.assertEquals(ErrorMessage, result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
        Assertions.assertEquals(ErrorMessage, result[3]);
    }

    /**
     * Tests the plant post input validation method with all fields being invalid
     */
    @Test
    void validateInputs_AllFieldsInvalid_AllFieldsReturnErrors() {
        String[] result = validatePlantPost("", "#", "a".repeat(513), "30/02/2003", messageSource, Locale.US);
        // check that all fields have an error message
        Assertions.assertEquals(ErrorMessage, result[0]);
        Assertions.assertEquals(ErrorMessage, result[1]);
        Assertions.assertEquals(ErrorMessage, result[2]);
        Assertions.assertEquals(ErrorMessage, result[3]);
    }

    /**
     * test date formatters with valid input
     */
    @Test
    void formatDate_ValidInput_ReturnsCorrectStrings() {
        LocalDate date = LocalDate.parse("09/05/2003", DateTimeFormatter.ofPattern(DATE_FORMAT));
        Assertions.assertEquals("09/05/2003", formatDateLoadField(date));
        Assertions.assertEquals("09-05-2003", formatDateDisplay(date));
    }


    // NOTE: the following tests work by repeating the same test but with different sets of parameters
    @ParameterizedTest
    @CsvSource({
            "'', description that is less than 512 characters",
            "'', This is a description that is exactly 512 characters long...........................................This is a description that is exactly 512 characters long...........................................This is a description that is exactly 512 characters long...........................................This is a description that is exactly 512 characters long...........................................This is a description that is exactly 512 characters long.......................................................",
            "Error Message Text, This is a description that is exactly 527 characters long...........................................This is a description that is exactly 527 characters long...........................................This is a description that is exactly 527 characters long...........................................This is a description that is exactly 512 characters long...........................................This is a description that is exactly 512 characters long.......................................................plus a bit more"
    })
    void validateInputs_ChangingLengthDescription_ReturnsDescriptionErrorIfOver512Characters(String expectedOutput, String description) {
        String[] result = validatePlantPost("valid name", "5", description, "20-02-2024", messageSource, Locale.US);
        // check that the description field has the correct expected output
        Assertions.assertEquals("", result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals(expectedOutput, result[2]);
    }
    
    @ParameterizedTest
    @CsvSource({
            "'', name that is less than 100 characters",
            "'', This name is exactly 100 characters long....................This name is exactly 100 characters long",
            "Error Message Text, This name is exactly 101 characters long..........|..........This name is exactly 101 characters long"})
    void validateInputs_ChangingLengthName_ReturnsNameErrorIfOver100Characters(String expectedOutput, String name) {
        String[] result = validatePlantPost(name, "5", "valid description" , "20-02-2024", messageSource, Locale.US);
        // check that the name field has the correct expected output
        Assertions.assertEquals(expectedOutput, result[0]);
        Assertions.assertEquals("", result[1]);
        Assertions.assertEquals("", result[2]);
    }
}
