package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.TagValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

class TagValidatorTest {

    @Mock
    private Model model;

    @Mock
    private ProfanityFilterService profanityFilterService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TagValidator.profanityFilterService = profanityFilterService;
    }


    @Test
    void checkIsTagInvalid_TagIsValidAlphasOnly_FalseReturned() {
        assertFalse(TagValidator.isTagInvalid("ValidTag", model));
    }

    @Test
    void checkIsTagInvalid_TagIsValidWithAccents_FalseReturned() {
        assertFalse(TagValidator.isTagInvalid("Māori", model));
    }

    @Test
    void checkIsTagInvalid_TagIsValidWithManyAccents_FalseReturned() {
        assertFalse(TagValidator.isTagInvalid("Guðmundsdóttir", model));
    }

    @Test
    void checkIsTagInvalid_TagIsValidWithMaxLength_FalseReturned() {
        assertFalse(TagValidator.isTagInvalid("a".repeat(25), model));
    }

    @Test
    void checkIsTagInvalid_TagIsValidWithOneCharacter_FalseReturned() {
        assertFalse(TagValidator.isTagInvalid("a", model));
    }

    @Test
    void checkIsTagInvalid_TagIsValidAlphasWithSpaces_FalseReturned() {
        assertFalse(TagValidator.isTagInvalid("valid tag with spaces", model));
    }

    @Test
    void checkIsTagInvalid_TagIsValidWithAccentsAndSpaces_FalseReturned() {
        assertFalse(TagValidator.isTagInvalid("crème brûlée", model));
    }

    @Test
    void checkIsTagInvalid_TagIsValidAlphasWithHyphens_FalseReturned() {
        assertFalse(TagValidator.isTagInvalid("valid-tag-with-hyphens", model));
    }

    @Test
    void checkIsTagInvalid_TagIsValidWithAccentsAndHyphens_FalseReturned() {
        assertFalse(TagValidator.isTagInvalid("Māori-Place", model));
    }

    @Test
    void checkIsTagInvalid_TagIsValidAlphasWithUnderscores_FalseReturned() {
        assertFalse(TagValidator.isTagInvalid("valid_tag_underscores", model));
    }

    @Test
    void checkIsTagInvalid_TagIsValidAlphasWithApostrophes_FalseReturned() {
        assertFalse(TagValidator.isTagInvalid("valid'tag'apostrophes", model));
    }

    @Test
    void checkIsTagInvalid_TagIsValidWithAccentsAndApostrophes_FalseReturned() {
        assertFalse(TagValidator.isTagInvalid("crème'brûlée'", model));
    }

    @Test
    void checkIsTagInvalid_TagIsValidAlphasWithHyphensAndUnderscores_FalseReturned() {
        assertFalse(TagValidator.isTagInvalid("valid-tag_with-characters", model));
    }

    @Test
    void checkIsTagInvalid_TagIsEmpty_TrueReturnedWithError() {
        assertTrue(TagValidator.isTagInvalid("", model));
        verify(model).addAttribute("tagError", "The tag name must only contain alphanumeric characters, spaces, -, _, ', or \"");
    }

    @Test
    void checkIsTagInvalid_TagIsOneAboveMaxLength_TrueReturnedWithError() {
        assertTrue(TagValidator.isTagInvalid("this_tag_is_way_too_long_for_the_validator", model));
        verify(model).addAttribute("tagError", "Tag must be 25 characters long or less.");
    }

    @Test
    void checkIsTagInvalid_TagContainsInvalidCharacters_TrueReturnedWithError() {
        assertTrue(TagValidator.isTagInvalid("invalid@tag!", model));
        verify(model).addAttribute("tagError", "The tag name must only contain alphanumeric characters, spaces, -, _, ', or \"");
    }

    @Test
    void checkIsTagInvalid_TagContainsInvalidCharactersAndIsTooLong_TrueReturnedWithError() {
        assertTrue(TagValidator.isTagInvalid("this_tag_is_way_too_long_for_the_validator_with@invalid!", model));
        verify(model).addAttribute("tagError", "Tag must be 25 characters long or less.");
    }
}
