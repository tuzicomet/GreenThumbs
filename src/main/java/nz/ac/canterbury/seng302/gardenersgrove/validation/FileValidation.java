package nz.ac.canterbury.seng302.gardenersgrove.validation;

import nz.ac.canterbury.seng302.gardenersgrove.exceptions.FileSizeException;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.ImageTypeException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Map;

/**
 * Class for file validation support functions
 */
public class FileValidation {
    /**
     * Valid MultipartFile image MIME content types
     */
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/svg+xml"};

    public static final Map<String, String> FILE_EXTENSION_TO_CONTENT_TYPE = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "svg", "image/svg+xml"
    );

    /**
     * Maximum allowed size of an image in bytes.
     */
    public static final int MAX_IMAGE_SIZE_IN_BYTES = 10000000;

    /**
     * Validate the image is of the correct type and size
     * @param image image to validate
     * @throws IllegalArgumentException if type or size invalid
     */
    public static void validateImage(MultipartFile image) throws ImageTypeException, FileSizeException {
        if (!Arrays.asList(ALLOWED_IMAGE_TYPES).contains(image.getContentType())) {
            throw new ImageTypeException("Image must be of type png, jpg or svg");
        }
        if (image.getSize() > MAX_IMAGE_SIZE_IN_BYTES) {
            throw new FileSizeException("Image must be less than 10MB");
        }
    }
}
