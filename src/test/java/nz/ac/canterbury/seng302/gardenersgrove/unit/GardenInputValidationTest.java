package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

import static nz.ac.canterbury.seng302.gardenersgrove.validation.FormValidation.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class GardenInputValidationTest {

    @Mock
    MessageSource messageSource;
    @MockBean
    ProfanityFilterService profanityFilterService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // If trying to get any message from messageSource, always return some example text
        // (Different integration test classes are used in order to test actual message text)
        when(messageSource.getMessage(any(), isNull(), eq(Locale.US)))
                .thenReturn("Error Message Text");
        profanityFilterService = Mockito.mock(ProfanityFilterService.class);
        when(profanityFilterService.containsProfanity(anyString()))
                .thenReturn(false);
    }

    @Test
    void validateInputs_AllFieldsValid_ReturnsAllValid() {
        List<String> result = validateGardenPost("name", "country", "city","suburb", "street", "4130", "5.3", "jsdhkgfjhgsdjf", messageSource, Locale.US, profanityFilterService);
        Assertions.assertEquals("", result.get(0));
        Assertions.assertEquals("", result.get(1));
        Assertions.assertEquals("", result.get(2));
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertEquals("", result.get(6));
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateInputs_MacronAndDiaeresis_ReturnsValid() {
        List<String> result = validateGardenPost("valid māori name", "Müller lodge","city","suburb", "street", "4130", "5.3", "", messageSource, Locale.US, profanityFilterService);
        Assertions.assertEquals("", result.get(0));
        Assertions.assertEquals("", result.get(1));
        Assertions.assertEquals("", result.get(2));
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertEquals("", result.get(6));
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateInputs_SizeNull_ReturnsAllValid() {
        List<String> result = validateGardenPost("name", "country", "city","suburb", "street", "4130","", "", messageSource, Locale.US, profanityFilterService);
        Assertions.assertEquals("", result.get(0));
        Assertions.assertEquals("", result.get(1));
        Assertions.assertEquals("", result.get(2));
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertEquals("", result.get(6));
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateInputs_CountryNull_ReturnsLocationError() {
        List<String> result = validateGardenPost("name", "", "city","suburb", "street", "4130","5.3", "", messageSource, Locale.US, profanityFilterService);
        Assertions.assertEquals("", result.get(0));
        Assertions.assertTrue(result.get(1) != null && !result.get(1).isEmpty());
        Assertions.assertEquals("", result.get(2));
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertEquals("", result.get(6));
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateInputs_CityNull_ReturnsLocationError() {
        List<String> result = validateGardenPost("name", "country", "","suburb", "street", "4130", "5.3", "", messageSource, Locale.US, profanityFilterService);
        Assertions.assertEquals("", result.get(0));
        Assertions.assertEquals("", result.get(1));
        Assertions.assertTrue(result.get(2) != null && !result.get(2).isEmpty());
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertEquals("", result.get(6));
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateInputs_NameNullWithWhitespace_ReturnsNameError() {
        List<String> result = validateGardenPost("  ", "country", "city","suburb", "street", "4130", "5.3", "", messageSource, Locale.US, profanityFilterService);
        Assertions.assertTrue(result.get(0) != null && !result.get(0).isEmpty());
        Assertions.assertEquals("", result.get(1));
        Assertions.assertEquals("", result.get(2));
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertEquals("", result.get(6));
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateInputs_NameInvalid_ReturnsNameError() {
        List<String> result = validateGardenPost("name????", "country", "city","suburb", "street", "4130", "5.3", "", messageSource, Locale.US, profanityFilterService);
        Assertions.assertTrue(result.get(0) != null && !result.get(0).isEmpty());
        Assertions.assertEquals("", result.get(1));
        Assertions.assertEquals("", result.get(2));
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertEquals("", result.get(6));
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateInputs_AllValidWithAllowedSpecialCharacters_ReturnsAllValid() {
        List<String> result = validateGardenPost("name-", "country.", "city,","suburb", "street", "4130", "5.3", "", messageSource, Locale.US, profanityFilterService);
        Assertions.assertEquals("", result.get(0));
        Assertions.assertEquals("", result.get(1));
        Assertions.assertEquals("", result.get(2));
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertEquals("", result.get(6));
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateInputs_AllFieldsValidEuropeanFormat_ReturnsAllValid() {
        List<String> result = validateGardenPost("name", "country", "city","suburb", "street", "4130", "5,3", "", messageSource, Locale.US, profanityFilterService);
        Assertions.assertEquals("", result.get(0));
        Assertions.assertEquals("", result.get(1));
        Assertions.assertEquals("", result.get(2));
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertEquals("", result.get(6));
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateInputs_SizeOnlyDecimalPoint_ReturnsSizeError() {
        List<String> result = validateGardenPost("name", "country", "city","suburb", "street", "4130", ".", "", messageSource, Locale.US, profanityFilterService);
        Assertions.assertEquals("", result.get(0));
        Assertions.assertEquals("", result.get(1));
        Assertions.assertEquals("", result.get(2));
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertTrue(result.get(6) != null && !result.get(6).isEmpty());
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateInputs_SizeTooSmall_ReturnsSizeError() {
        List<String> result = validateGardenPost("valid name", "country", "city", "suburb", "street", "4130", "0.09999999", "valid description", messageSource, Locale.US, profanityFilterService);
        Assertions.assertEquals("", result.get(0));
        Assertions.assertEquals("", result.get(1));
        Assertions.assertEquals("", result.get(2));
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertTrue(result.get(6) != null && !result.get(6).isEmpty());
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateInputs_SizeTooLong_ReturnsSizeError() {
        List<String> result = validateGardenPost("valid name", "country", "city", "suburb", "street", "4130", "12345678900", "valid description", messageSource, Locale.US, profanityFilterService);
        Assertions.assertEquals("", result.get(0));
        Assertions.assertEquals("", result.get(1));
        Assertions.assertEquals("", result.get(2));
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertTrue(result.get(6) != null && !result.get(6).isEmpty());
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateInputs_SizeInvalidFormat_ReturnsSizeError() {
        List<String> result = validateGardenPost("name", "country", "city","suburb", "street", "4130", "abcd123", "sdffsdf2//??sdfsd", messageSource, Locale.US, profanityFilterService);
        Assertions.assertEquals("", result.get(0));
        Assertions.assertEquals("", result.get(1));
        Assertions.assertEquals("", result.get(2));
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertTrue(result.get(6) != null && !result.get(6).isEmpty());
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateInputs_PostcodeComplicated_ReturnsValid() {
        List<String> result = validateGardenPost("name", "england", "piccadilly","piccadilly", "street", "W1J 7NT - 149", "100", "sdffsdf2//??sdfsd", messageSource, Locale.US, profanityFilterService);
        Assertions.assertEquals("", result.get(0));
        Assertions.assertEquals("", result.get(1));
        Assertions.assertEquals("", result.get(2));
        Assertions.assertEquals("", result.get(3));
        Assertions.assertEquals("", result.get(4));
        Assertions.assertEquals("", result.get(5));
        Assertions.assertEquals("", result.get(6));
        Assertions.assertEquals("", result.get(7));
    }

    @Test
    void validateGardenPost_AllFieldsOnLengthBoundary_ReturnsValid() {
        List<String> result = validateGardenPost(
                "N".repeat(100),
                "C".repeat(100),
                "C".repeat(100),
                "S".repeat(100),
                "S".repeat(100),
                "P".repeat(20),
                "1".repeat(10),
                "D".repeat(512),
                messageSource, Locale.US,
                profanityFilterService
        );
        result.forEach(error -> Assertions.assertEquals("", error));
    }

    @Test
    void validateGardenPost_AllFieldsPastLengthBoundary_ReturnsErrors() {
        List<String> result = validateGardenPost(
                "N".repeat(101),
                "C".repeat(101),
                "C".repeat(101),
                "S".repeat(101),
                "S".repeat(101),
                "P".repeat(21),
                "1".repeat(11),
                "D".repeat(513),
                messageSource, Locale.US,
                profanityFilterService
        );
        Assertions.assertTrue(result.get(0) != null && !result.get(0).isEmpty());
        Assertions.assertTrue(result.get(1) != null && !result.get(1).isEmpty());
        Assertions.assertTrue(result.get(2) != null && !result.get(2).isEmpty());
        Assertions.assertTrue(result.get(3) != null && !result.get(3).isEmpty());
        Assertions.assertTrue(result.get(4) != null && !result.get(4).isEmpty());
        Assertions.assertTrue(result.get(5) != null && !result.get(5).isEmpty());
        Assertions.assertTrue(result.get(6) != null && !result.get(6).isEmpty());
        Assertions.assertTrue(result.get(7) != null && !result.get(7).isEmpty());
    }

    @Test
    void getSize_RegularFormat_ReturnsFloat() {
        float result = parseFloat("2.7");
        Assertions.assertEquals((float) 2.7, result);
    }

    @Test
    void getSize_EuropeanFormat_ReturnsFloat() {
        float result = parseFloat("2,7");
        Assertions.assertEquals((float) 2.7, result);
    }

    @Test
    void getSize_sizeEmpty_ReturnsZero() {
        float result = parseFloat("");
        Assertions.assertEquals((float) 0, result);
    }

    @Test
    void processRefererWithId_HasReferer_ReturnDefault() {
        String processedReferer = processRefererWithId("referer", 1L);
        Assertions.assertEquals("referer", processedReferer);
    }

    @Test
    void processRefererWithId_NoReferer_ReturnDefault() {
        String processedReferer = processRefererWithId(null, 1L);
        Assertions.assertEquals("/garden/1", processedReferer);
    }
}
