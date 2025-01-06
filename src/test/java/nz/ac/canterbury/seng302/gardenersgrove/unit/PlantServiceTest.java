package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;


/**
 * Testing class to verify Plant creation and interactions with the PLANT repository.
 */
@Transactional
@SpringBootTest
class PlantServiceTest {
    @Autowired
    private PlantRepository plantRepository;
    @Autowired
    private GardenService gardenService;
    @Autowired
    private PlantService plantService;
    @Autowired
    private UserService userService;
    private Garden garden;

    @BeforeEach
    void setup() {
        User user = new User("Real", "User",
                "ExistingUser@gmail.com", "Testp4$$",
                "2000-10-10", null);
        userService.addUser(user);
        garden = gardenService.addGarden(new Garden("Garden", "1.0", user, "Has cool plants", true, null, null, true, null));
    }

    /**
     * Simple add and retrieve from database with all parameters provided.
     * Asserts the name is correct and the count was converted to an Integer.
     */
    @Test
    void addPlantAllFields() {
        Plant result = plantService.addPlant(
                new Plant(
                        garden,
                        "Rose",
                        "5.0",
                        "red flower with thorns on the stalk",
                        LocalDate.parse("2003-05-09", ISO_LOCAL_DATE),
                        null
                )
        );
        Assertions.assertEquals(result.getName(), "Rose");
        Assertions.assertInstanceOf(String.class, result.getCount());
    }

    /**
     * Add and retrieve again, but missing the description (optional) parameter.
     * Asserts the name is correct and the description is null.
     */
    @Test
    void addPlantNoCount() {
        Plant result = plantService.addPlant(
                new Plant(
                        garden,
                        "Rose",
                        null,
                        "red flower with thorns on the stalk",
                        LocalDate.parse("2003-05-09", ISO_LOCAL_DATE),
                        null));
        Assertions.assertEquals(result.getName(), "Rose");
        Assertions.assertNull(result.getCount());
    }

    /**
     * Attempt to add a plant with all null parameters, and verify the database rejects the commit.
     */
    @Test
    void addPlantAllNullFails() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> plantService.addPlant(
                new Plant(null, null, null, null, null, null)));
    }

    /**
     * Attempts to parse an invalid date when adding a new plant, and verify the date is not parsed.
     */
    @Test
    void addPlantInvalidDate() {
        Assertions.assertThrows(DateTimeParseException.class, () -> plantService.addPlant(
                new Plant(garden,
                        "Rose",
                        "5.0",
                        "red flower with thorns on the stalk",
                        LocalDate.parse("2003-02-30", ISO_LOCAL_DATE),
                        null)));
    }
}

