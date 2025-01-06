package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.FriendRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing class to verify methods within the FriendshipService class work as intended
 */
@SpringBootTest
@Transactional // roll back changes made to database after each test
@AutoConfigureTestDatabase // configures a database for the tests, see https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/test/autoconfigure/jdbc/AutoConfigureTestDatabase.html
class FriendshipServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private FriendshipService friendshipService;

    /**
     * Test to ensure that acceptRequest works when the request is valid,
     * and makes the users friends, while also deleting the friend request
     */
    @Test
    void acceptRequest_RequestIsValid_UsersAreNowFriendsAndRequestIsDeleted() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        userRepository.save(userA);
        userRepository.save(userB);

        // create a pending friend request from user A to user B
        FriendRequest request = new FriendRequest(userA.getUserId(), userB.getUserId(), "PENDING");
        friendRequestRepository.save(request);

        // accept the friend request
        friendshipService.acceptRequest(userA, userB);

        // try and find the friend request
        FriendRequest foundRequest = friendRequestRepository.findFriendRequestBySenderIdAndReceiverId(
                userA.getUserId(), userB.getUserId());
        // Check that the friend request does not exist
        assertNull(foundRequest);

        // Check if userA is friends with userB
        assertTrue(userService.areUsersFriends(userA, userB));
    }

    /**
     * Test to ensure that declineRequest works when the request is valid,
     * and it updates the status of the request to "DECLINED"
     */
    @Test
    void declineRequest_RequestIsValid_RequestIsDeclined() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        userRepository.save(userA);
        userRepository.save(userB);

        // create a pending friend request from user A to user B
        FriendRequest request = new FriendRequest(userA.getUserId(), userB.getUserId(), "PENDING");
        friendRequestRepository.save(request);

        // decline the friend request
        friendshipService.declineRequest(userA, userB);

        // get the status of the request between userA and userB
        String retrievedStatus = userService.getStatus(userA, userB);
        // Check that the status is DECLINED
        assertThat(retrievedStatus).isEqualTo("DECLINED");
    }

    /**
     * Test to ensure that cancelRequest works when the request is valid,
     * and deletes the friend request
     */
    @Test
    void cancelRequest_RequestIsValid_RequestIsDeleted() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        userRepository.save(userA);
        userRepository.save(userB);

        // create a pending friend request from user A to user B
        FriendRequest request = new FriendRequest(userA.getUserId(), userB.getUserId(), "PENDING");
        friendRequestRepository.save(request);

        // cancel the friend request
        friendshipService.cancelRequest(userA, userB);

        // try and find the friend request
        FriendRequest foundRequest = friendRequestRepository.findFriendRequestBySenderIdAndReceiverId(
                userA.getUserId(), userB.getUserId());
        // Check that the friend request does not exist
        assertNull(foundRequest);

    }

    /**
     * Test to ensure that removeFriendship
     */
    @Test
    void removeFriendship_FriendshipExists_FriendshipIsRemoved() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        userRepository.save(userA);
        userRepository.save(userB);

        // create friendship for user A, with user B
        Friendship friendship = new Friendship(userA.getUserId(), userB.getUserId());
        // save the friendships to the database
        friendshipRepository.save(friendship);

        // use removeFriendship to remove the friendship between userA and userB
        friendshipService.removeFriendship(userA, userB);

        // Check if userA is friends with userB
        assertFalse(userService.areUsersFriends(userA, userB));
    }
}
