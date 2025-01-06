package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.ScheduledTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

@SpringBootTest
@Transactional
class WaitListModerationTest {

    @Autowired
    ScheduledTask scheduledTask;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    GardenRepository gardenRepository;

    @Autowired
    UserRepository userRepository;

    @MockBean
    ProfanityFilterService profanityFilterService;

    @MockBean
    UserService userService;

    @BeforeEach
    void setUp(){
        Tag verified = new Tag("tag1", true);
        Tag unverified = new Tag("tag2", false);

        User user1 = new User("John", "Doe", "john@example.com", "password", "1990-01-01", null);
        User user2 = new User("Jane", "Doe", "jane@example.com", "password", "1992-01-01", null);
        userRepository.saveAll(Arrays.asList(user1, user2));

        Garden garden1 = new Garden("Garden1", "100", user1, "Description1", false, null, null, false, null);
        Garden garden2 = new Garden("Garden2", "200", user2, "Description2", false, null, null, false, null);
        garden1.getTags().add(unverified);
        garden2.getTags().add(unverified);

        gardenRepository.saveAll(Arrays.asList(garden1, garden2));
        tagRepository.saveAll(Arrays.asList(verified, unverified));
    }

    @Test
    void moderateWaitListItem_notProfane_BecomesVerified(){
        Mockito.when((profanityFilterService.verifyTag(any()))).thenReturn(true);
        scheduledTask.moderateWaitlistItem();
        Assertions.assertEquals(true, tagRepository.findByContent("tag2").isPresent());
        Assertions.assertEquals(true, tagRepository.findByContent("tag2").get().isVerified());
    }

    @Test
    void moderateWaitListItem_profane_IsDeleted(){
        Mockito.when((profanityFilterService.verifyTag(any()))).thenReturn(false);
        scheduledTask.moderateWaitlistItem();
        Assertions.assertEquals(false, tagRepository.findByContent("tag2").isPresent());
    }

    @Test
    void moderateWaitListItem_profane_MultipleUsers_SingleStrikeEach() {
        Mockito.when((profanityFilterService.verifyTag(any()))).thenReturn(false);

        scheduledTask.moderateWaitlistItem();

        Mockito.verify(userService, times(2)).addStrike(any(User.class));
        Assertions.assertEquals(false, tagRepository.findByContent("tag2").isPresent());
    }

    @Test
    void moderateWaitListItem_failedModeration_StaysUnverified(){
        Mockito.when((profanityFilterService.verifyTag(any()))).thenThrow( new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        scheduledTask.moderateWaitlistItem();
        Assertions.assertEquals(true, tagRepository.findByContent("tag2").isPresent());
        Assertions.assertEquals(false, tagRepository.findByContent("tag2").get().isVerified());
    }

    @Test
    void moderateWaitListItem_waitListEmpty_NoModerationAttempt(){
        Mockito.when((profanityFilterService.verifyTag(any()))).thenReturn(true);
        scheduledTask.moderateWaitlistItem();
        Assertions.assertEquals(true, tagRepository.findByContent("tag2").isPresent());
        Assertions.assertEquals(true, tagRepository.findByContent("tag2").get().isVerified());

        scheduledTask.moderateWaitlistItem();
        Mockito.verify(profanityFilterService, times(1)).verifyTag(anyString());
    }

}
