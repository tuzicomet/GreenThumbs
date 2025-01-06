package nz.ac.canterbury.seng302.gardenersgrove.validation;

import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class TagValidator {

    public static final int MAX_TAG_LENGTH = 25;
    public static final String TAG_REGEX = "^[\\p{L}\\p{N}\\p{M}  \\-_'\"]+$";
    public static ProfanityFilterService profanityFilterService = new ProfanityFilterService();

    /**
     * Checks if the provided tag is invalid according to these rules:
     * For a tag to be valid, it must:
     * - contain only alphanumeric characters, spaces, -, _, ', or ".
     *   (accentuated characters also allowed)
     * - be no longer than 25 characters.
     * @param tag the tag to validate
     * @param model the model used for passing the error to the front
     * @return true if there is an error false if not.
     */
    public static boolean isTagInvalid(String tag, Model model) {
        if (tag.length() > MAX_TAG_LENGTH) {
            model.addAttribute("tagError", "Tag must be 25 characters long or less.");
            return true;
        }

        if (!tag.matches(TAG_REGEX)) {
            model.addAttribute("tagError", "The tag name must only contain alphanumeric characters, spaces, -, _, ', or \"");
            return true;
        }

        return false;
    }
}
