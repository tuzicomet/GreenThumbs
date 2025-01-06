package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.component.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.AlertService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import static org.mockito.ArgumentMatchers.any;

/**
 * Testing class to verify Garden creation and interactions with the GARDEN table.
 */
@Transactional
@SpringBootTest
class GardenServiceTest {
    
    @Autowired
    private GardenRepository gardenRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private GardenService gardenService;
    @Autowired
    private UserService userService;
    @Autowired
    private AlertService alertService;
    @MockBean
    private ProfanityFilterService profanityFilterService;
    @Autowired
    private CustomAuthenticationProvider customAuthProvider;

    private User user;
    private Garden defaultGarden1;
    private Garden defaultGarden2;

    @BeforeEach
    void setup() {

        user = new User("Real", "User",
                "majewoh954@dxice.com", "Testp4$$",
                "2000-10-10", null);
        userService.addUser(user);

        defaultGarden1 = new Garden(
                "Garden 1",
                "1.0",
                user,
                "Valid",
                false,
                null,
                null,
                true,
                null
        );

        defaultGarden2 = new Garden(
                "Garden 2",
                "1.0",
                user,
                "Valid",
                false,
                null,
                null,
                true,
                null
        );
        Authentication authentication = customAuthProvider.authenticate(
                new UsernamePasswordAuthenticationToken("verifieduser@gmail.com", "Testp4$$")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    /**
     * Simple add and retrieve from database with all parameters provided.
     * Asserts the name is correct and the size was converted to a Float.
     */
    @Test
    void addGardenAllFields() {
        Garden result = gardenService.addGarden(defaultGarden1);
        Assertions.assertEquals(defaultGarden1.getName(), result.getName());
        Assertions.assertInstanceOf(String.class, result.getSize());
    }

    /**
     * Add and retrieve again, but missing the size parameter.
     * Asserts the name is correct and the size is null.
     */
    @Test
    void addGardenNoSize() {
        GardenService gardenService = new GardenService(gardenRepository, tagRepository, alertService, profanityFilterService, userService);
        Garden result = gardenService.addGarden(new Garden("Botanic Gardens", null, user, "Has cool plants", true, null, null, true, null));
        Assertions.assertEquals("Botanic Gardens", result.getName());
        Assertions.assertNull(result.getSize());
    }

    /**
     * Attempt to add a garden with all null parameters, and verify the database rejects the commit.
     */
    @Test
    void addGardenAllNullFails() {
        Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> gardenService.addGarden(
                        new Garden(
                                null,
                                null,
                                user,
                                null,
                                false,
                                null,
                                null,
                                true,
                                null
                        )
                )
        );
    }

    /**
     * Attempt to add a garden, then edit that same garden.
     */
    @Test
    void editGarden_allValid_successfullyUpdated() {
        Garden garden = new Garden("Valid", "1.0", user, "Valid", false, null, null, true, null);
        gardenService.addGarden(garden);
        garden.setName("Updated");
        gardenService.addGarden(garden);

        Assertions.assertEquals("Updated",
                                gardenService.getGarden(garden.getId()).get().getName());
    }

    @Test
    void addTagToGarden_tagDoesntExist_tagIsAddedToGarden() {
        Mockito.when((profanityFilterService.verifyTag(any()))).thenReturn(true);
        final String content = "VALID";
        gardenService.addTagToGarden(defaultGarden1, content);
        Assertions.assertEquals(1, defaultGarden1.getTags().size());
        Assertions.assertEquals(content, defaultGarden1.getTags().getFirst().getContent());
    }

    @Test
    void addTagToGarden_tagContentAlreadyExists_tagIsAddedToGarden() {
        Mockito.when((profanityFilterService.verifyTag(any()))).thenReturn(true);
        final String content = "VALID";
        // Add tag to another garden initially
        gardenService.addTagToGarden(defaultGarden1, content);
        // Try to add another tag with the same content
        gardenService.addTagToGarden(defaultGarden2, content);

        // Verify the different gardens' tags are the same object
        Assertions.assertEquals(1, defaultGarden1.getTags().size());
        Assertions.assertEquals(content, defaultGarden1.getTags().getFirst().getContent());
    }

    @Test
    void addTagToGarden_tagContentAlreadyExists_tagIsNotDuplicated() {
        Mockito.when((profanityFilterService.verifyTag(any()))).thenReturn(true);
        final String content = "VALID";
        // Add tag to another garden initially
        gardenService.addTagToGarden(defaultGarden1, content);
        // Try to add another tag with the same content
        gardenService.addTagToGarden(defaultGarden2, content);

        // Verify the different gardens' tags are the same object
        Assertions.assertEquals(
                defaultGarden1.getTags().getFirst(),
                defaultGarden2.getTags().getFirst()
        );
    }

    @Test
    void addTagToGarden_addTagsToDifferentGardens_tagsAreDifferent() {
        Mockito.when((profanityFilterService.verifyTag(any()))).thenReturn(true);
        final String content1 = "FIRST TAG";
        final String content2 = "SECOND TAG";
        gardenService.addTagToGarden(defaultGarden1, content1);
        gardenService.addTagToGarden(defaultGarden2, content2);

        // Verify the different gardens have the correct tags
        Assertions.assertEquals(1, defaultGarden1.getTags().size());
        Assertions.assertEquals(content1, defaultGarden1.getTags().getFirst().getContent());
        Assertions.assertEquals(content2, defaultGarden2.getTags().getFirst().getContent());
    }
    @Test
    void addTagToGarden_failedModeration_addedButNotVerified() {
        Mockito.when((profanityFilterService.verifyTag(any()))).thenThrow( new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        final String content = "TAG";
        gardenService.addTagToGarden(defaultGarden1, content);
        Assertions.assertEquals(1, defaultGarden1.getTags().size());
        Assertions.assertEquals(content, defaultGarden1.getTags().getFirst().getContent());
        Assertions.assertEquals(false, defaultGarden1.getTags().getFirst().isVerified());
    }
    @Test
    void addTagToGarden_notProfaneAndModerated_addedAndVerified() {
        Mockito.when((profanityFilterService.verifyTag(any()))).thenReturn(true);
        final String content = "TAG";
        gardenService.addTagToGarden(defaultGarden1, content);
        Assertions.assertEquals(1, defaultGarden1.getTags().size());
        Assertions.assertEquals(content, defaultGarden1.getTags().getFirst().getContent());
        Assertions.assertEquals(true, defaultGarden1.getTags().getFirst().isVerified());
    }

    @Test
    void addTagToGarden_profaneAndModerated_deletedAndAddedStrike() {
        UserService mockUserService = Mockito.mock(UserService.class);

        Mockito.when(profanityFilterService.verifyTag(any())).thenReturn(false);

        GardenService gardenServiceWithMock = new GardenService(gardenRepository, tagRepository, alertService, profanityFilterService, mockUserService);

        final String content = "bad word";

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        gardenServiceWithMock.addTagToGarden(defaultGarden1, content);
        Assertions.assertEquals(0, defaultGarden1.getTags().size());

        Mockito.verify(mockUserService, Mockito.times(1)).addStrike(any());
    }


}
