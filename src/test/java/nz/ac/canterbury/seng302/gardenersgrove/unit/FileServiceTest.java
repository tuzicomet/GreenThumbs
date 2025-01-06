package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.exceptions.FileSizeException;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.ImageTypeException;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.nio.file.Files.readAllBytes;
import static org.mockito.Mockito.*;

class FileServiceTest {

    private FileService fileService;
    private FileService fileServiceSpy;
    private static MockedStatic<Files> fileSystem;
    final MultipartFile BINARY_FILE = new MockMultipartFile("file", "originalName.txt", "text/plain", new byte[0]);
    final String VALID_FILE = "valid_file.txt";
    final String MISSING_FILE = "non_existent_file.txt";
    final String UNREADABLE_FILE = "protected_file.txt";
    final String DIRECTORY_FILE = "/";
    final String ILLEGAL_FILE = "../../../../out/of/bounds";

    @BeforeEach
    void setUp() {
        fileService = new FileService();
        fileServiceSpy = Mockito.spy(FileService.class);
        fileSystem = Mockito.mockStatic(Files.class);
    }


    @AfterEach
    void close() {
        fileSystem.close();
    }

    @Test
    void getFileByPath_FileExistsAndReadable_ReturnsOptionalResource() {
        fileSystem.when(() -> readAllBytes(Mockito.any(Path.class))).thenReturn(new byte[0]);
        doReturn(true).when(fileServiceSpy).isFileExists(Mockito.anyString());
        Optional<Resource> image = fileServiceSpy.getFile(VALID_FILE);
        Assertions.assertInstanceOf(Optional.class, image);
        Assertions.assertTrue(image.isPresent());
        Assertions.assertInstanceOf(Resource.class, image.get());
    }

    @Test
    void getFileByPath_FileUnreadable_ReturnsEmptyOptional() {
        doReturn(true).when(fileServiceSpy).isFileExists(Mockito.anyString());
        fileSystem.when(() -> readAllBytes(Mockito.any(Path.class))).thenThrow(IOException.class);
        Optional<Resource> image = fileServiceSpy.getFile(UNREADABLE_FILE);
        Assertions.assertInstanceOf(Optional.class, image);
        Assertions.assertTrue(image.isEmpty());
    }

    @Test
    void getFileByPath_FileMissing_ReturnsEmptyOptional() {
        doReturn(false).when(fileServiceSpy).isFileExists(Mockito.anyString());
        Optional<Resource> image = fileServiceSpy.getFile(MISSING_FILE);
        Assertions.assertInstanceOf(Optional.class, image);
        Assertions.assertTrue(image.isEmpty());
    }

    @Test
    void isFileExists_FileExists_ReturnsTrue() {
        fileSystem.when(() -> Files.exists(Mockito.any(Path.class))).thenReturn(true);
        Assertions.assertTrue(fileService.isFileExists(VALID_FILE));
    }

    @Test
    void isFileExists_FileMissing_ReturnsFalse() {
        fileSystem.when(() -> Files.exists(Mockito.any(Path.class))).thenReturn(false);
        Assertions.assertFalse(fileService.isFileExists(MISSING_FILE));
    }

    @Test
    void isFileExists_FileIsDirectory_ReturnsFalse() {
        fileSystem.when(() -> Files.isDirectory(Mockito.any(Path.class))).thenReturn(true);
        Assertions.assertFalse(fileService.isFileExists(DIRECTORY_FILE));
    }

    @Test
    void isPathTraversal_AbsoluteIncludesTraversalOperators_ReturnsFalse() {
        try {
            File fileMock = Mockito.mock(File.class);
            Mockito.when(fileMock.getCanonicalFile()).thenReturn(new File("/test"));
            Mockito.when(fileMock.getAbsoluteFile()).thenReturn(new File("/../test"));
            Assertions.assertTrue(fileService.isPathTraversal("/%s".formatted(ILLEGAL_FILE)));
        } catch (IOException e) {
            Assertions.fail();
        }
    }

    @Test
    void isPathTraversal_AbsoluteHasNoTraversalOperators_ReturnsTrue() {
        try {
            File fileMock = Mockito.mock(File.class);
            Mockito.when(fileMock.getCanonicalFile()).thenReturn(new File("/test"));
            Mockito.when(fileMock.getAbsoluteFile()).thenReturn(new File("/test"));
            Assertions.assertFalse(fileService.isPathTraversal("/%s".formatted(VALID_FILE)));
        } catch (IOException e) {
            Assertions.fail();
        }
    }

    @Test
    void removeFile_FileExistsAndWritable_ReturnsTrue() {
        doReturn(true).when(fileServiceSpy).isFileExists(Mockito.anyString());
        fileSystem.when(() -> Files.delete(Mockito.any(Path.class))).thenAnswer(invocation -> null);

        Assertions.assertTrue(fileServiceSpy.removeFile(VALID_FILE));
    }

    @Test
    void removeFile_FileExistsAndWritable_DeleteIsCalled() {
        doReturn(true).when(fileServiceSpy).isFileExists(Mockito.any(String.class));
        fileSystem.when(() -> Files.delete(Mockito.any(Path.class))).thenAnswer(invocation -> null);
        fileServiceSpy.removeFile(DIRECTORY_FILE);
        fileSystem.verify(() -> Files.delete(Mockito.any(Path.class)), times(1));
    }

    @Test
    void removeFile_FileDoesntExist_ReturnsFalse() {
        doReturn(false).when(fileServiceSpy).isFileExists(Mockito.anyString());
        fileSystem.when(() -> Files.delete(Mockito.any(Path.class))).thenAnswer(invocation -> null);
        Assertions.assertFalse(fileServiceSpy.removeFile(MISSING_FILE));
    }

    @Test
    void removeFile_FileDoesntExist_DoesntDelete() {
        doReturn(false).when(fileServiceSpy).isFileExists(Mockito.anyString());
        fileSystem.when(() -> Files.delete(Mockito.any(Path.class))).thenAnswer(invocation -> null); // do nothing
        fileServiceSpy.removeFile(MISSING_FILE);
        fileSystem.verify(() -> Files.delete(Mockito.any(Path.class)), never());
    }

    @Test
    void removeFile_FileExistsUnableToRemove_ReturnsFalse() {
        doReturn(true).when(fileServiceSpy).isFileExists(Mockito.anyString());
        fileSystem.when(() -> Files.delete(Mockito.any(Path.class))).thenThrow(IOException.class);
        Assertions.assertFalse(fileServiceSpy.removeFile(UNREADABLE_FILE));
    }

    @Test
    void addFile_SuccessfulAdd_ReturnsPresentOptional() {
        fileSystem.when(() -> Files.write(Mockito.any(Path.class), Mockito.any(byte[].class))).thenReturn(Path.of(VALID_FILE));
        Optional<Path> path = fileService.addFile(BINARY_FILE, "/test_directory/");
        Assertions.assertTrue(path.isPresent());
    }

    @Test
    void addFile_SuccessfulAdd_ReturnedPathIsCorrectFormat() {
        fileSystem.when(() -> Files.write(Mockito.any(Path.class), Mockito.any(byte[].class))).thenAnswer(invocation -> null);
        Optional<Path> path = fileService.addFile(BINARY_FILE, "/test_directory/");
        Assertions.assertTrue(path.isPresent());
        Assertions.assertTrue(path.get().getFileName().toString().matches("[a-fA-F0-9]{16}\\.[a-zA-Z0-9]+"));
    }

    @Test
    void addFile_FailedAdd_ReturnsEmptyOptional() {
        fileSystem.when(() -> Files.write(Mockito.any(Path.class), Mockito.any(byte[].class))).thenThrow(IOException.class);
        Optional<Path> path = fileService.addFile(BINARY_FILE, "test");
        Assertions.assertTrue(path.isEmpty());
    }

    @Test
    void addFile_FileHasNoOriginalFilename_ReturnsEmptyOptional() {
        Optional<Path> path = fileService.addFile(new MockMultipartFile(
                "file",
                null,
                "text/plain",
                new byte[0]
        ), "test");
        Assertions.assertTrue(path.isEmpty());
    }

    @Test
    void addFiles_ValidFiles_ReturnPaths() throws ImageTypeException, FileSizeException, IllegalArgumentException {
        List<MultipartFile> files = List.of(
                new MockMultipartFile("file1", "image1.png", "image/png", new byte[0]),
                new MockMultipartFile("file2", "image2.jpg", "image/jpeg", new byte[0]),
                new MockMultipartFile("file3", "image3.svg", "image/svg+xml", new byte[0])
        );
        List<String> paths = fileService.uploadFiles(files);
        Assertions.assertEquals(3, paths.size());
    }

    @Test
    void addFiles_EmptyFiles_ReturnEmptyList() throws ImageTypeException, FileSizeException, IllegalArgumentException {
        List<String> paths = fileService.uploadFiles(new ArrayList<>());
        Assertions.assertTrue(paths.isEmpty());
    }
}
