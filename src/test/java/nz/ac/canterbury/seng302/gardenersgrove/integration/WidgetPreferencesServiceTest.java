package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.WidgetPreferences;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.WidgetPreferencesRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.WidgetPreferencesService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testing class to verify methods within the WidgetPreferencesService class work as intended
 */
@SpringBootTest
@Transactional // roll back changes made to database after each test
@AutoConfigureTestDatabase // configures a database for the tests
class WidgetPreferencesServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WidgetPreferencesService widgetPreferencesService;

    @Autowired
    private WidgetPreferencesRepository widgetPreferencesRepository;

    User user1;
    User user2;
    WidgetPreferences user1Preferences;

    // Add a default user to test with
    @BeforeEach
    void addDefaultUsers() {
        // Set up the test users and save them to the database
        user1 = new User(
                "John",
                "Doe",
                "john.doe@test.com",
                "password",
                "2001-01-01",
                User.DEFAULT_IMAGE_PATH
        );
        user2 = new User(
                "Jane",
                "Foe",
                "jane.foe@test.com",
                "Testp4$$",
                "2001-09-11",
                User.DEFAULT_IMAGE_PATH
        );
        userRepository.save(user1);
        userRepository.save(user2);

        // Set up the widget preferences for user1 only, and save it to the database
        user1Preferences = new WidgetPreferences(
                user1.getUserId(), true, false, true, false
        );
        widgetPreferencesRepository.save(user1Preferences);
        // Do not initialise widget preferences for user2
    }

    @Test
    void findPreferencesByUserId_UserDoesNotExist_NullReturned() {
        Assertions.assertEquals(null, widgetPreferencesService.findByUserId(99999999L));
    }

    @Test
    void findPreferencesByUserId_UserDoesNotHavePreferences_NullReturned() {
        Assertions.assertEquals(null, widgetPreferencesService.findByUserId(user2.getUserId()));
    }

    @Test
    void findPreferencesByUserId_UserHasPreferences_CorrectPreferencesReturned() {
        // Retrieve user 2's widget preferences again
        WidgetPreferences userWidgetPreferences = widgetPreferencesService.findByUserId(user1.getUserId());

        // Check that all options have been initialized correctly (should all be set to true)
        Assertions.assertTrue(userWidgetPreferences.getWelcome());
        Assertions.assertFalse(userWidgetPreferences.getRecentGardens());
        Assertions.assertTrue(userWidgetPreferences.getRecentPlants());
        Assertions.assertFalse(userWidgetPreferences.getFriends());
    }

    @Test
    void saveUserPreferences_PreferencesAreValid_PreferencesArePersisted() {
        WidgetPreferences userWidgetPreferences = widgetPreferencesService.findByUserId(user1.getUserId());

        userWidgetPreferences.setRecentGardens(true);
        userWidgetPreferences.setRecentPlants(false);
        userWidgetPreferences.setFriends(true);

        WidgetPreferences widgetPreferences = widgetPreferencesService.addWidgetPreference(userWidgetPreferences);
        Assertions.assertEquals(true, widgetPreferences.getRecentGardens());
        Assertions.assertEquals(false, widgetPreferences.getRecentPlants());
        Assertions.assertEquals(true, widgetPreferences.getFriends());

    }

    @Test
    void initialisePreferencesForUserWithId_UserHasNoPreferences_PreferencesAreInitialisedCorrectly() {
        // Retrieve user 2's widget preferences (they should not have any yet)
        // and check that nothing is returned
        Assertions.assertNull(widgetPreferencesService.findByUserId(user2.getUserId()));

        // Use the initialisePreferences method to initialise their preferences
        widgetPreferencesService.initialisePreferences(user2.getUserId());

        // Retrieve user 2's widget preferences again
        WidgetPreferences userWidgetPreferences = widgetPreferencesService.findByUserId(user2.getUserId());

        // Check that the user's widget preferences were created
        Assertions.assertNotNull(userWidgetPreferences);

        // Check that all options have been initialized correctly (should all be set to true)
        Assertions.assertTrue(userWidgetPreferences.getWelcome());
        Assertions.assertTrue(userWidgetPreferences.getRecentGardens());
        Assertions.assertTrue(userWidgetPreferences.getRecentPlants());
        Assertions.assertTrue(userWidgetPreferences.getFriends());
    }

    @Test
    void initialisePreferencesForUserWithId_UserAlreadyHasPreferences_PreferencesAreNotUpdated() {
        // Retrieve user 1's widget preferences
        WidgetPreferences originalWidgetPreferences = widgetPreferencesService.findByUserId(user1.getUserId());

        // Use the initialisePreferences method to initialise their preferences
        widgetPreferencesService.initialisePreferences(user1.getUserId());

        // Retrieve user 1's widget preferences again
        WidgetPreferences widgetPreferences = widgetPreferencesService.findByUserId(user1.getUserId());

        // Check that everything is the same
        Assertions.assertEquals(originalWidgetPreferences, widgetPreferences);
    }
}