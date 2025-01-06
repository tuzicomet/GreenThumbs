package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.exceptions.FileSizeException;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.ImageTypeException;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.FileValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Controller for handling images being added or changed
 */
@RestController
public class ImageController {

    /**
     * Web URL of the placeholder error image.
     */
    public static final String PLACEHOLDER_IMAGE_NAME = "/images/error.png";

    /**
     * The location where user uploaded images should be requested from. (i.e. /USER_UPLOAD_MAPPING/image.jpg)
     */
    public static final String USER_UPLOAD_MAPPING = "user_uploads";
    private static final Logger LOG = LoggerFactory.getLogger(ImageController.class);
    private final FileService fileService;

    public ImageController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * GET mapping for getting a user-uploaded image.
     * @param imagePath name of the file in the format <code>{filename}.{extension}</code>
     * @return the requested file if exists and allowed, otherwise throw 404.
     */
    @GetMapping(
            value = {
                    "/" + USER_UPLOAD_MAPPING + "/{imagePath}",
                    "/" + USER_UPLOAD_MAPPING + "/{imagePath}/"
            },
            produces = {"image/jpeg", "image/png", "image/svg+xml"}
    )
    public ResponseEntity<byte[]> getImage(@PathVariable String imagePath) {
        LOG.info("GET /{}/{}", USER_UPLOAD_MAPPING, imagePath);
        String path = "images/" + imagePath;

        HttpHeaders header = new HttpHeaders();

        Resource file = fileService.getFile(path)
                .orElseGet(() -> fileService.getFile(PLACEHOLDER_IMAGE_NAME)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Error placeholder image could not be retrieved.")));

        String extension = imagePath.substring(imagePath.lastIndexOf(".") + 1);
        header.add("Content-Type", FileValidation.FILE_EXTENSION_TO_CONTENT_TYPE.get(extension));
        try {
            return ResponseEntity.ok()
                    .headers(header)
                    .body(file.getContentAsByteArray());
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .headers(header)
                    .body(new byte[0]);
        }
    }

    /**
     * POST mapping for uploading an image.
     * @param file Multipart file to upload. It should only be of the formats jpg/jpeg, png, or svg.
     * @return the path of the uploaded file, if errors throw 400.
     */
    @PostMapping(value = {USER_UPLOAD_MAPPING, USER_UPLOAD_MAPPING + "/"})
    @ResponseStatus(code = HttpStatus.CREATED)
    public String postImage(@RequestParam MultipartFile file) throws ResponseStatusException {
        LOG.info("POST /{}", USER_UPLOAD_MAPPING);
        try {
            FileValidation.validateImage(file);
            Path createdFile = fileService.addFile(file, "images").orElseThrow(() ->
                    new IOException("Upload failed."));
            return "[%s]".formatted(createdFile.getFileName().toString());
        } catch (ImageTypeException | FileSizeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
