package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.controller.ImageController;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.FileSizeException;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.ImageTypeException;
import nz.ac.canterbury.seng302.gardenersgrove.utility.Crypto;
import nz.ac.canterbury.seng302.gardenersgrove.validation.FileValidation;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Optional;

@Service
public class FileService {

    /**
     * The root path of all user uploaded content. Any requests to save/read above this directory should be denied.
     */
    public static final String UPLOAD_ROOT_PATH = "user_uploads";
    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    /**
     * Get the current Path of the upload root, relative to the current working directory.
     * @return Path of the upload root.
     */
    private Path getAbsoluteRootPath() {
        return Path.of("").resolve(UPLOAD_ROOT_PATH);
    }

    /**
     * Get the Path of the file relative to the current working directory.
     * @param fileName relative path to the file from the upload root. Should be in the form {subdirectory}/{filename}.{extension}
     * @return the file Path relative to the current working directory.
     */
    private Path getAbsoluteFilePath(String fileName) {
        return getAbsoluteRootPath().resolve(fileName);
    }

    /**
     * Checks if a filepath include path traversal operators (/../ and /./) by comparing the outputs of
     * getCanonicalFile() and getAbsoluteFile(). getCanonicalFile removes the operators, while getAbsoluteFile() doesn't
     * so if they differ then the path includes the operators.
     * @param fileName Filename to check. Should be in the format <br><code>{subdirectory}/{filename}.{extension}</code>
     * @return true if path includes /../ or /./, otherwise false
     */
    public boolean isPathTraversal(String fileName) {
        try {
            Path absoluteRootPath = getAbsoluteRootPath().toFile().getCanonicalFile().toPath();
            File file = absoluteRootPath.resolve(fileName).toFile();
            // Check if path includes any path traversal characters (.. or .), if so deny the request as malicious.
            if (!file.getCanonicalFile().equals(file.getAbsoluteFile())) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * Check if a file exists and valid
     * @param fileName Filename to check. Should be in the format <br><code>{subdirectory}/{filename}.{extension}</code>
     * @return false if the file is nonexistent, includes path traversal operators, or errors; true otherwise
     */
    public boolean isFileExists(String fileName) {
        Path file = getAbsoluteFilePath(fileName);
        return Files.exists(file) && !Files.isDirectory(file) && !isPathTraversal(fileName);
    }

    /**
     * Get file at specified path.
     * @param fileName Filename to get. Should be in the format <br><code>{subdirectory}/{filename}.{extension}</code>
     * @return Optional Resource representing the file. If the get was unsuccessful, will be Optional.empty().
     */
    public Optional<Resource> getFile(String fileName) {
        if (!isFileExists(fileName)) {
            return Optional.empty();
        }
        try {
            Path filePath = getAbsoluteFilePath(fileName);
            return Optional.of(new ByteArrayResource(Files.readAllBytes(filePath)));
        } catch (IOException e) {
            LOG.error("Failed to load image from user directory.");
        }
        return Optional.empty();
    }

    /**
     * Remove a file at the specified path
     * @param fileName filename to delete. should be in the format <br><code>{subdirectory}/{filename}.{extension}</code>
     * @return boolean flag representing whether the removal was successful.
     */
    public boolean removeFile(String fileName) {
        if (!isFileExists(fileName)) {
            return false;
        }
        Path file = getAbsoluteFilePath(fileName);
        try {
            Files.delete(file);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Adds a file to the filesystem with an anonymized filename under the specified directory.
     * Creates parent directories if they don't already exist
     * @param file data to put in the file
     * @param directory subdirectory to create the file in. No slashes needed, unless nesting.
     * @return Optional Path to the created file.
     */
    public Optional<Path> addFile(MultipartFile file, String directory) {
        try (InputStream data = file.getInputStream()) {
            String newFileName = generateUniqueNameWithSameExtension(file, directory);
            Path newFilePath = Path.of(getAbsoluteFilePath(newFileName).toString());
            Files.createDirectories(newFilePath.getParent()); // if not created, create parent directories
            Files.write(newFilePath, data.readAllBytes());
            return Optional.of(newFilePath);
        } catch (IOException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Generates a unique 16-character hex path with the same extension as the provided file.
     * @param userUploadedFile file to take the extension from
     * @param directory directory within the UPLOAD_ROOT_PATH the file will be stored at
     * @return Currently unused Path with the same extension as the original file.
     */
    String generateUniqueNameWithSameExtension(MultipartFile userUploadedFile, String directory) throws IllegalArgumentException {
        SecureRandom sr = new SecureRandom();
        Path fileName;
        do {
            byte[] values = new byte[8];
            sr.nextBytes(values);
            fileName = Path.of(directory)
                    .resolve("%s.%s".formatted(
                        Crypto.bytesToHex(values),
                        getExtensionFromMultipartFile(userUploadedFile)
                    ));
        } while (isFileExists(fileName.toString()));
        return fileName.toString();
    }

    /**
     * Extracts the extension from the original file name and returns it.
     * @param file File to get the extension of
     * @return the file extension, defined as the characters after the final period.
     */
    String getExtensionFromMultipartFile(MultipartFile file) throws IllegalArgumentException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Original filename of MultipartFile not supplied, cannot find extension.");
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Validates and uploads a list of image files.
     * Each file is validated for type and size before being uploaded. The method
     * generates and returns a list of file paths for successfully uploaded files.
     *
     * @param files a list of {@link MultipartFile} objects to be uploaded. Each file
     *              must be an image and within the allowed size limits.
     * @return a list of {@link String} file paths where the uploaded images are stored.
     * @throws IllegalArgumentException if the input files list is null or contains invalid files.
     * @throws ImageTypeException if any of the files are not of the allowed image types (e.g., png, jpg, svg).
     * @throws FileSizeException if any of the files exceed the maximum allowed size.
     */
    public List<String> uploadFiles(List<MultipartFile> files) throws IllegalArgumentException, ImageTypeException, FileSizeException {
        List<String> filePaths = new ArrayList<>();
        for (MultipartFile file : files) {
            FileValidation.validateImage(file);
            Path fileName = addFile(file, "images").orElseThrow();
            filePaths.add("/%s/%s".formatted(ImageController.USER_UPLOAD_MAPPING, fileName.getFileName().toString()));
        }
        return filePaths;
    }

}