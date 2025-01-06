package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.TagController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
class TagAutocompleteIntegrationTest {
    @Autowired
    TagRepository tagRepository;
    @Autowired
    GardenRepository gardenRepository;
    @Autowired
    GardenService gardenService;
    @Autowired
    TagController tagController;

    @BeforeEach
    void setUp() {
        Tag tag1 = new Tag("abc", true);
        Tag tag2 = new Tag("abcd", true);
        Tag tag3 = new Tag("abcde", true);
        Tag tag4 = new Tag("abcdef", true);
        Tag tag5 = new Tag("abcdefg", true);
        Tag tag6 = new Tag("abcdefgh", true);
        Tag tag7 = new Tag("xyzz", true);
        Tag tag8 = new Tag("xyzwxz", true);
        Tag tag9 = new Tag("xyzwxzz", true);
        Tag tag10 = new Tag("xmiddlex", true);
        tagRepository.save(tag1);
        tagRepository.save(tag2);
        tagRepository.save(tag3);
        tagRepository.save(tag4);
        tagRepository.save(tag5);
        tagRepository.save(tag6);
        tagRepository.save(tag7);
        tagRepository.save(tag8);
        tagRepository.save(tag9);
        tagRepository.save(tag10);
    }

    @Test
    void getAutocompleteTags_NoMatch_ReturnsEmptyList() {
        String input = "nomatch";
        List<String> results = tagController.getAutocompleteSuggestions(input);
        Assertions.assertEquals(0, results.size());
    }

    @Test
    void getAutocompleteTags_OverFiveMatches_ReturnsShortestMatches() {
        String input = "abc";
        List<String> results = tagController.getAutocompleteSuggestions(input);
        Assertions.assertEquals(5, results.size());
        Assertions.assertEquals("abc", results.get(0));
        Assertions.assertEquals("abcd", results.get(1));
        Assertions.assertEquals("abcde", results.get(2));
        Assertions.assertEquals("abcdef", results.get(3));
        Assertions.assertEquals("abcdefg", results.get(4));
        Assertions.assertFalse(results.contains("abcdefgh"));
    }

    @Test
    void getAutocompleteTags_UnderFiveMatches_ReturnsAllMatches() {
        String input = "xyz";
        List<String> results = tagController.getAutocompleteSuggestions(input);
        Assertions.assertEquals(3, results.size());
        Assertions.assertEquals("xyzz", results.get(0));
        Assertions.assertEquals("xyzwxz", results.get(1));
        Assertions.assertEquals("xyzwxzz", results.get(2));
    }

    @Test
    void getAutocompleteTags_MatchInTheMiddleOfTag_DoesNotMatch() {
        String input = "middle";
        List<String> results = tagController.getAutocompleteSuggestions(input);
        Assertions.assertEquals(0, results.size());
        Assertions.assertFalse(results.contains("xmiddlex"));
    }
}
