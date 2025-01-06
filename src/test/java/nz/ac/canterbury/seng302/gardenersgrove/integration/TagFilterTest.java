package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.component.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Testing class to verify Garden creation and interactions with the GARDEN table.
 */
@Transactional
@SpringBootTest
class TagFilterTest {

    @Autowired
    private GardenRepository gardenRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private GardenService gardenService;
    @Autowired
    private UserService userService;
    @Autowired
    AlertService alertService;
    @Autowired
    PlantRepository plantRepository;
    @Autowired
    ProfanityFilterService profanityFilterService;
    @Autowired
    CustomAuthenticationProvider customAuthenticationProvider;
    @Autowired
    PasswordEncoder passwordEncoder;

    private AbstractUser user;
    private Garden defaultGarden1;
    private Garden defaultGarden2;
    private Garden defaultGarden3;
    private Garden defaultGarden4;

    private Plant plant1;
    private Plant plant2;


    @BeforeEach
    void setup() {
        // Create a blank user and activate them
        user = new User("Real", "User",
                "blankuser@gmail.com", passwordEncoder.encode("Testp4$$"),
                "2000-10-10", null);
        user = userService.addUser(user);
        userService.enableUser(user.getUserId());

        Authentication authentication = customAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), "Testp4$$")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        defaultGarden1 = new Garden(
                "search",
                "1.0",
                user,
                "Valid",
                true,
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
                true,
                null,
                null,
                true,
                null
        );
        defaultGarden3 = new Garden(
                "search",
                "1.0",
                user,
                "Valid",
                true,
                null,
                null,
                true,
                null
        );

        defaultGarden4 = new Garden(
                "search",
                "1.0",
                user,
                "Valid",
                true,
                null,
                null,
                true,
                null
        );

        ProfanityFilterService profanityFilterServiceMock = Mockito.mock();
        when(profanityFilterServiceMock.containsProfanity(Mockito.anyString())).thenReturn(false);
        when(profanityFilterServiceMock.verifyTag(Mockito.anyString())).thenReturn(true);

        gardenService = new GardenService(gardenRepository, tagRepository, alertService, profanityFilterServiceMock, userService);
        defaultGarden1 = gardenRepository.save(defaultGarden1);
        defaultGarden2 = gardenRepository.save(defaultGarden2);
        defaultGarden3 = gardenRepository.save(defaultGarden3);
        defaultGarden4 = gardenRepository.save(defaultGarden4);

        plant1 = new Plant(defaultGarden1, "plant", "1", "cool", null, null);
        plant2 = new Plant(defaultGarden2, "plant", "1", "cool", null, null);

        plant1 = plantRepository.save(plant1);
        plant2 = plantRepository.save(plant2);

        gardenService.addTagToGarden(defaultGarden1, "tag 1");
        gardenService.addTagToGarden(defaultGarden1, "tag 2");
        gardenService.addTagToGarden(defaultGarden2, "tag 1");
        gardenService.addTagToGarden(defaultGarden3 , "tag 3");
    }


    @Test
    void getRecentPublicGardens_NoTags_ReturnsAll(){
        List<String> tags = new ArrayList<>();
        Page<Garden> result = gardenService.getRecentPublicGardens(PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id")), tags);
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden1.getId())));
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden2.getId())));
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden3.getId())));
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden4.getId())));

    }
    @Test
    void getRecentPublicGardens_OneTag_ReturnsCorrectGardens(){
        List<String> tags = new ArrayList<>();
        tags.add("tag 1");

        Page<Garden> result = gardenService.getRecentPublicGardens(PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id")), tags);
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden1.getId())));
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden2.getId())));
        Assertions.assertFalse(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden3.getId())));
        Assertions.assertFalse(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden4.getId())));

    }
    @Test
    void getRecentPublicGardens_MultipleTags_ReturnsCorrectGardens(){
        List<String> tags = new ArrayList<>();
        tags.add("tag 2");
        tags.add("tag 3");
        Page<Garden> result = gardenService.getRecentPublicGardens(PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id")), tags);
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden1.getId())));
        Assertions.assertFalse(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden2.getId())));
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden3.getId())));
        Assertions.assertFalse(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden4.getId())));

    }
    @Test
    void getSearchGardens_NoTags_ReturnsAllMatchingSearch(){
        String searchTerm = "search";
        List<String> tags = new ArrayList<>();
        Page<Garden> result = gardenService.getAllPublicGardensByName(searchTerm, PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id")), tags);
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden1.getId())));
        Assertions.assertFalse(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden2.getId())));
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden3.getId())));
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden4.getId())));

    }
    @Test
    void getSearchGardens_OneTag_ReturnsCorrectGardens(){
        String searchTerm = "search";
        List<String> tags = new ArrayList<>();
        tags.add("tag 1");
        Page<Garden> result = gardenService.getAllPublicGardensByName(searchTerm, PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id")), tags);
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden1.getId())));
        Assertions.assertFalse(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden2.getId())));
        Assertions.assertFalse(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden3.getId())));
        Assertions.assertFalse(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden4.getId())));

    }
    @Test
    void getSearchGardensByPlant_MultipleTags_ReturnsCorrectGardens(){
        String searchTerm = "plant";
        List<String> tags = new ArrayList<>();
        tags.add("tag 2");
        tags.add("tag 3");
        Page<Garden> result = gardenService.getAllPublicGardensByPlantName(searchTerm, PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id")), tags);
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden1.getId())));
        Assertions.assertFalse(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden2.getId())));
        Assertions.assertFalse(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden3.getId())));
        Assertions.assertFalse(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden4.getId())));

    }
    @Test
    void getSearchGardensByPlant_NoTags_ReturnsCorrectGardens(){
        String searchTerm = "plant";
        List<String> tags = new ArrayList<>();
        Page<Garden> result = gardenService.getAllPublicGardensByPlantName(searchTerm, PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id")), tags);
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden1.getId())));
        Assertions.assertTrue(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden2.getId())));
        Assertions.assertFalse(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden3.getId())));
        Assertions.assertFalse(result.stream().anyMatch(garden -> garden.getId().equals(defaultGarden4.getId())));

    }

}
