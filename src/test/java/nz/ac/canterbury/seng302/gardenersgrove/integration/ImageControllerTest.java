package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.ImageController;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.FileSizeException;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.ImageTypeException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.FileValidation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Optional;

import static nz.ac.canterbury.seng302.gardenersgrove.validation.FileValidation.validateImage;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageControllerTest.class)
class ImageControllerTest {
    @Autowired
    private MockMvc mockMvc;
    // GardenRepository is required by GlobalControllerAdvice
    @MockBean
    private GardenRepository gardenRepository;
    private FileService fileService;
    private static MockedStatic<FileValidation> validationMock;

    private static final Path VALID_PATH = Path.of("/valid/path.jpg");
    private static final Resource VALID_FILE = new ByteArrayResource(new byte[0]);
    private static final Resource PLACEHOLDER_FILE = new ByteArrayResource(new byte[1]);

    private final MockMultipartFile BINARY_FILE = new MockMultipartFile(
            "file",
            "originalName.txt",
            "text/plain",
            new byte[0]
    );

    @BeforeAll
    static void setupAllTests() {
        validationMock = Mockito.mockStatic(FileValidation.class);
    }

    @AfterAll
    static void cleanUp() {
        validationMock.close();
    }

    @BeforeEach
    void setup() {
        fileService = Mockito.mock(FileService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ImageController(fileService)).build();
    }

    @Test
    void getImage_FileExists_ReturnsValidResource() throws Exception {
        Mockito.when(fileService.getFile(Mockito.anyString())).thenReturn(Optional.of(VALID_FILE));
        String existentRequestPath = "/%s/exists.jpg".formatted(ImageController.USER_UPLOAD_MAPPING);
        this.mockMvc.perform(get(existentRequestPath))
                .andExpect(status().isOk())
                .andExpect(content().bytes(VALID_FILE.getContentAsByteArray()));
    }

    @Test
    void getImage_FileDoesntExist_ReturnsPlaceholderImage() throws Exception {
        Mockito.when(fileService.getFile("/images/nonexistent.jpg")).thenReturn(Optional.empty());
        Mockito.when(fileService.getFile(ImageController.PLACEHOLDER_IMAGE_NAME)).thenReturn(Optional.of(PLACEHOLDER_FILE));
        String nonexistentRequestPath = "/%s/nonexistent.jpg".formatted(ImageController.USER_UPLOAD_MAPPING);
        this.mockMvc.perform(get(nonexistentRequestPath))
                .andExpect(status().isOk())
                .andExpect(content().bytes(PLACEHOLDER_FILE.getContentAsByteArray()));
    }

    @Test
    void getImage_PlaceholderImageNotFound_Throws404() throws Exception {
        Mockito.when(fileService.getFile(Mockito.anyString())).thenReturn(Optional.empty());
        this.mockMvc.perform(get("/images/nonexistent.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postImage_FileIsValid_ReturnsFilenameInJSONArray() throws Exception {
        Mockito.when(fileService.addFile(Mockito.any(MultipartFile.class), Mockito.anyString()))
                .thenReturn(Optional.of(VALID_PATH));
        validationMock.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        String postRequestPath = "/%s".formatted(ImageController.USER_UPLOAD_MAPPING);
        this.mockMvc.perform(multipart(postRequestPath).file(BINARY_FILE))
                .andExpect(status().isCreated())
                .andExpect(content().string("[" + VALID_PATH.getFileName().toString() + "]"));
    }

    @Test
    void postImage_FileIsBadType_Throws400() throws Exception {
        validationMock.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenThrow(ImageTypeException.class);
        String postRequestPath = "/%s".formatted(ImageController.USER_UPLOAD_MAPPING);
        this.mockMvc.perform(multipart(postRequestPath).file(BINARY_FILE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postImage_FileTooLarge_Throws400() throws Exception {
        validationMock.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenThrow(FileSizeException.class);
        String postRequestPath = "/%s".formatted(ImageController.USER_UPLOAD_MAPPING);
        this.mockMvc.perform(multipart(postRequestPath).file(BINARY_FILE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postImage_CannotAddImage_Throws500() throws Exception {
        validationMock.when(() -> validateImage(Mockito.any(MultipartFile.class))).thenAnswer(invocation -> null);
        Mockito.when(fileService.addFile(Mockito.any(MultipartFile.class), Mockito.anyString()))
                .thenReturn(Optional.empty());

        String postRequestPath = "/%s".formatted(ImageController.USER_UPLOAD_MAPPING);
        this.mockMvc.perform(multipart(postRequestPath).file(BINARY_FILE))
                .andExpect(status().isInternalServerError());
    }

}
