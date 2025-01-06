package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class GardenTest {
    static Tag tag;
    static Garden garden;
    @BeforeAll
    static void createDefaultTag() {
        tag = new Tag("Sample tag", true);
    }

    @BeforeEach
    void createDefaultGarden() {
        User user = Mockito.mock(User.class);
        garden = new Garden(
                "Sample Name",
                "15",
                user,
                "Sample Description",
                false,
                null,
                null,
                false,
                null
        );
    }

    @Test
    void garden_addNewTag_AddsTag() {
        garden.addTag(tag);

        Assertions.assertEquals(1, garden.getTags().size());
        Assertions.assertEquals(List.of(tag), garden.getTags());
    }

    @Test
    void garden_addExistingTag_DoesntAddTagAgain() {
        garden.addTag(tag);
        // Add the same tag object again
        garden.addTag(tag);

        Assertions.assertEquals(1, garden.getTags().size());
        Assertions.assertEquals(List.of(tag), garden.getTags());
    }

    @Test
    void garden_addTagWithSameContent_DoesntAddTagAgain() {
        // Add the same tag
        garden.addTag(tag);
        // Add tag again
        Tag identicalTag = new Tag(tag.getContent(), true);
        garden.addTag(identicalTag);

        Assertions.assertEquals(1, garden.getTags().size());
        Assertions.assertEquals(List.of(tag), garden.getTags());
    }
}
