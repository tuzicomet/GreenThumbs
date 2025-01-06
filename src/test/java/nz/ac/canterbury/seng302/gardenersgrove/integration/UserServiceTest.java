package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LocationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Testing class to verify methods within the UserService class work as intended
 */
@SpringBootTest
@Transactional // roll back changes made to database after each test
@AutoConfigureTestDatabase // configures a database for the tests, see https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/test/autoconfigure/jdbc/AutoConfigureTestDatabase.html
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;
    @Autowired
    private LocationRepository locationRepository;

    User defaultUser;

    @BeforeEach
    void addDefaultUser() {
        defaultUser = new User(
                "John",
                "Doe",
                "john.doe@test.com",
                "password",
                "2001-01-01",
                User.DEFAULT_IMAGE_PATH
        );
        userRepository.save(defaultUser);

    }

    /**
     * Test to ensure that the getFriendsOfUser method works if the user has no friends
     */
    @Test
    void getUsersFriends_UserHasNoFriends_EmptyListReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        userRepository.save(userA);

        // Get user A's friends
        List<AbstractUser> friendsList = userService.getFriendsOfUser(userA);
        // Check that the friends list is empty
        assertThat(friendsList).isEmpty();
    }

    /**
     * Test to ensure that the getFriendsOfUser method works if the user has only one friend
     */
    @Test
    void getUsersFriends_UserHasOneFriend_FriendReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        userRepository.save(userA);
        userRepository.save(userB);

        // create a friendship between userA and userB
        Friendship friendship = new Friendship(userA.getUserId(), userB.getUserId());
        // save the friendship to the database
        friendshipRepository.save(friendship);

        // Get user A's friends
        List<AbstractUser> friendsList = userService.getFriendsOfUser(userA);
        // Check that user B is the only friend of user A
        assertThat(friendsList).containsExactly(userB);
    }

    /**
     * Test to ensure that the getFriendsOfUser method works when user has multiple friends
     */
    @Test
    void getUsersFriends_UserHasMultipleFriends_AllFriendsReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        User userC = new User("userC", "Chicken", "userC@test.com", "dog", "2006-04-12", "building.jpg");
        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);

        // create friendships for user A, with user B and user C
        Friendship friendship1 = new Friendship(userA.getUserId(), userB.getUserId());
        Friendship friendship2 = new Friendship(userA.getUserId(), userC.getUserId());
        // save the friendships to the database
        friendshipRepository.save(friendship1);
        friendshipRepository.save(friendship2);

        // Get user A's friends
        List<AbstractUser> friendsList = userService.getFriendsOfUser(userA);
        // Check that user B and user C are the only friends of user A
        assertThat(friendsList).containsExactlyInAnyOrder(userB, userC);
    }

    /**
     * Test to check that even if user alternates between the user1_id and user2_id columns
     * in the database, the getFriendsOfUser method still returns exactly the user's friends
     */
    @Test
    void getUsersFriends_UserAlternatesColumnsInFriendshipTable_AllFriendsReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        User userC = new User("userC", "Chicken", "userC@test.com", "dog", "2006-04-12", "building.jpg");
        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);

        // create friendship for user A, with user B
        Friendship friendship1 = new Friendship(userA.getUserId(), userB.getUserId());
        // create friendship for user A, with user C,
        // but put user A as the second parameter (user2_id column)
        Friendship friendship2 = new Friendship(userC.getUserId(), userA.getUserId());
        // save the friendships to the database
        friendshipRepository.save(friendship1);
        friendshipRepository.save(friendship2);

        // Get user A's friends
        List<AbstractUser> friendsList = userService.getFriendsOfUser(userA);
        // Check that user B and user C are the only friends of user A
        assertThat(friendsList).containsExactlyInAnyOrder(userB, userC);
    }

    /**
     * Test to check that the areUsersFriends method in UserService returns true if the
     * two given users are friends
     */
    @Test
    void areUsersFriends_UsersAreFriends_TrueReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        userRepository.save(userA);
        userRepository.save(userB);

        // create friendship for user A, with user B
        Friendship friendship = new Friendship(userA.getUserId(), userB.getUserId());
        // save the friendships to the database
        friendshipRepository.save(friendship);

        // Check that userA is friends with userB
        assertTrue(userService.areUsersFriends(userA, userB));
    }

    /**
     * Test to check that the areUsersFriends method in UserService returns false if the
     * two given users are not friends
     */
    @Test
    void areUsersFriends_UsersAreNotFriends_FalseReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        userRepository.save(userA);
        userRepository.save(userB);

        // Do not add a friendship between userA and userB

        // Check that userA is not friends with userB
        assertFalse(userService.areUsersFriends(userA, userB));
    }

    /**
     * Test to ensure that the getUsersWithPendingRequestsToUser method
     * returns an empty list if the user has no requests
     */
    @Test
    void getFriendRequestsToUser_UserHasNoRequests_EmptyListReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        userRepository.save(userA);

        // Get user A's pending requests
        List<AbstractUser> pendingRequestList = userService.getUsersWithPendingRequestsToUser(userA);
        // Check that both user B and user C are returned
        assertThat(pendingRequestList).isEmpty();
    }

    /**
     * Test to ensure that the getUsersWithPendingRequestsToUser method
     * works if the user has only pending friend requests
     */
    @Test
    void getFriendRequestsToUser_UserHasOnlyPendingRequests_PendingRequestsReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        User userC = new User("userC", "Chicken", "userC@test.com", "dog", "2006-04-12", "building.jpg");
        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);

        // create a pending friend request from user B to user A
        FriendRequest request1 = new FriendRequest(userB.getUserId(), userA.getUserId(), "PENDING");
        // create a declined friend request from user C to user A
        FriendRequest request2 = new FriendRequest(userC.getUserId(), userA.getUserId(), "PENDING");
        // save the friend requests to the database
        friendRequestRepository.save(request1);
        friendRequestRepository.save(request2);

        // Get user A's pending requests
        List<AbstractUser> pendingRequestList = userService.getUsersWithPendingRequestsToUser(userA);
        // Check that both user B and user C are returned
        assertThat(pendingRequestList).containsExactly(userB, userC);
    }

    /**
     * Test to ensure that the getUsersWithPendingRequestsToUser method
     * only returns pending requests, if user has both pending and declined requests
     */
    @Test
    void getFriendRequestsToUser_UserHasPendingAndDeclinedRequests_PendingRequestsReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        User userC = new User("userC", "Chicken", "userC@test.com", "dog", "2006-04-12", "building.jpg");
        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);

        // create a pending friend request from user B to user A
        FriendRequest pendingRequest = new FriendRequest(userB.getUserId(), userA.getUserId(), "PENDING");
        // create a declined friend request from user C to user A
        FriendRequest declinedRequest = new FriendRequest(userC.getUserId(), userA.getUserId(), "DECLINED");
        // save the friend requests to the database
        friendRequestRepository.save(pendingRequest);
        friendRequestRepository.save(declinedRequest);

        // Get user A's pending requests
        List<AbstractUser> pendingRequestList = userService.getUsersWithPendingRequestsToUser(userA);
        // Check that user B (the pending requester) is the only request there
        assertThat(pendingRequestList).containsExactly(userB);
    }

    /**
     * Test to ensure that the getUsersWithSentRequestsFromUser method
     * returns an empty list if the user has not sent any requests
     */
    @Test
    void getSentRequestsUsers_UserHasNotSentRequests_EmptyListReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        userRepository.save(userA);

        // Get user A's pending requests
        List<AbstractUser> sentRequestList = userService.getUsersWithSentRequestsFromUser(userA);
        // Check that an empty list is returned
        assertThat(sentRequestList).isEmpty();
    }

    /**
     * Test to ensure that the getUsersWithSentRequestsFromUser method
     * works if the user has sent requests all with the same status
     */
    @Test
    void getSentRequestsUsers_UserHasSentRequestsWithSameStatus_AllRequestsReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        User userC = new User("userC", "Chicken", "userC@test.com", "dog", "2006-04-12", "building.jpg");
        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);

        // create a pending friend request from user A to user B
        FriendRequest pendingRequest = new FriendRequest(userA.getUserId(), userB.getUserId(), "PENDING");
        // create a declined friend request from user A to user C
        FriendRequest pendingRequest2 = new FriendRequest(userA.getUserId(), userC.getUserId(), "DECLINED");
        // save the friend requests to the database
        friendRequestRepository.save(pendingRequest);
        friendRequestRepository.save(pendingRequest2);

        // Get user A's pending requests
        List<AbstractUser> sentRequestList = userService.getUsersWithSentRequestsFromUser(userA);
        // Check that both user B and user C are returned
        assertThat(sentRequestList).containsExactly(userB, userC);
    }

    /**
     * Test to ensure that the getUsersWithSentRequestsFromUser method
     * returns all users which a given user has sent requests to, regardless of status
     */
    @Test
    void getSentRequestsUsers_UserHasSentRequestsWithVaryingStatus_AllRequestsReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        User userC = new User("userC", "Chicken", "userC@test.com", "dog", "2006-04-12", "building.jpg");
        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);

        // create a pending friend request from user A to user B
        FriendRequest pendingRequest = new FriendRequest(userA.getUserId(), userB.getUserId(), "PENDING");
        // create a declined friend request from user A to user C
        FriendRequest declinedRequest = new FriendRequest(userA.getUserId(), userC.getUserId(), "DECLINED");
        // save the friend requests to the database
        friendRequestRepository.save(pendingRequest);
        friendRequestRepository.save(declinedRequest);

        // Get user A's pending requests
        List<AbstractUser> sentRequestList = userService.getUsersWithSentRequestsFromUser(userA);
        // Check that both user B and user C are returned
        assertThat(sentRequestList).containsExactly(userB, userC);
    }

    /**
     * Test to ensure that if the UserService's sendFriendRequest method works if two valid users are passed in
     */
    @Test
    void sendFriendRequest_RequestIsValid_FriendRequestCreated() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        userRepository.save(userA);
        userRepository.save(userB);

        // send a friend request from userA to userB
        userService.sendFriendRequest(userA, userB);

        // try and find the friend request
        FriendRequest foundRequest = friendRequestRepository.findFriendRequestBySenderIdAndReceiverId(
                userA.getUserId(), userB.getUserId());

        // assert that the request is not null
        // and that the sender and requester ids are correct
        assertNotNull(foundRequest);
        assertThat(foundRequest.getSenderId()).isEqualTo(userA.getUserId());
        assertThat(foundRequest.getReceiverId()).isEqualTo(userB.getUserId());
    }

    /**
     * Test to ensure that the UserService getStatus method returns the correct status
     * of a friend request between two users, when the request is pending
     */
    @Test
    void getRequestStatus_RequestHasPendingStatus_PendingReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        userRepository.save(userA);
        userRepository.save(userB);

        // create a pending friend request from user A to user B
        FriendRequest pendingRequest = new FriendRequest(userA.getUserId(), userB.getUserId(), "PENDING");
        // save the friend requests to the database
        friendRequestRepository.save(pendingRequest);

        // get the status of the request between userA and userB
        String retrievedStatus = userService.getStatus(userA, userB);
        // Check that the status is PENDING
        assertThat(retrievedStatus).isEqualTo("PENDING");
    }

    /**
     * Test to ensure that the UserService getStatus method returns the correct status
     * of a friend request between two users, when the request is declined
     */
    @Test
    void getRequestStatus_RequestHasDeclinedStatus_DeclinedReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("userB", "Bob", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        userRepository.save(userA);
        userRepository.save(userB);

        // create a pending friend request from user A to user B
        FriendRequest declinedRequest = new FriendRequest(userA.getUserId(), userB.getUserId(), "DECLINED");
        // save the friend requests to the database
        friendRequestRepository.save(declinedRequest);

        // get the status of the request between userA and userB
        String retrievedStatus = userService.getStatus(userA, userB);
        // Check that the retrieved status is DECLINED
        assertThat(retrievedStatus).isEqualTo("DECLINED");
    }

    /**
     * Test to check that if no users with matching names exist, the getUsersByName method
     * returns an empty list
     */
    @Test
    void getUsersByName_NoMatchesExist_EmptyListReturned() {
        // use getUsersByName to find users with the firstname Test and last name User
        // (no users with this name exists)
        List<AbstractUser> matchingUsers = userService.getUsersByName("Test User");
        // check that no results were returned (empty list)
        assertThat(matchingUsers).isEmpty();
    }

    /**
     * Test to check that if the getUsersByName method is called to find users with only a
     * first name (last name is empty string), it returns the correct list of users
     */
    @Test
    void getUsersByFirstNameOnly_MatchesExist_CorrectListReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("TestUser", "", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        User userC = new User("TestUser", "", "userC@test.com", "dog", "2006-04-12", "building.jpg");
        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);

        // use getUsersByName to find users with the firstname TestUser and no last name
        List<AbstractUser> matchingUsers = userService.getUsersByName("TestUser");
        // check the result is as expected
        assertThat(matchingUsers).contains(userB, userC);
    }

    /**
     * Test to check that if the getUsersByName method is called to find users with
     * first name and last name, it returns the correct list of users
     */
    @Test
    void getUsersByFullName_MatchesExist_CorrectListReturned() {
        // Create test users and save them to the database
        User userA = new User("userA", "Adams", "userA@test.com", "password", "2001-01-01", "image.jpg");
        User userB = new User("Test", "User", "userB@example.com", "snake", "1999-07-07", "different_image.jpg");
        User userC = new User("Test", "User", "userC@test.com", "dog", "2006-04-12", "building.jpg");
        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);

        // use getUsersByName to find users with the firstname Test and last name User
        List<AbstractUser> matchingUsers = userService.getUsersByName("Test User");
        // check the result is as expected
        assertThat(matchingUsers).contains(userB, userC);
    }

    @Test
    void addStrike_AddStrike_StrikeIsIncreasedByOne() {
        int beforeNStrikes = defaultUser.getNumberOfStrikes();
        userService.addStrike(defaultUser);
        Assertions.assertEquals(beforeNStrikes + 1, defaultUser.getNumberOfStrikes());
    }

    @Test
    void banUserForDays_UserGetsBanned_AmountIsRoundedToNearestDay() {
        userService.banUserForDays(defaultUser, 2);
        Instant bannedUntil = defaultUser.getAccountDisabledUntil();
        Assertions.assertEquals(
                bannedUntil.truncatedTo(ChronoUnit.DAYS),
                bannedUntil
        );
    }

    @Test
    void banUserForDays_UserGetsBanned_BannedUntilIsSetCorrectly() {
        userService.banUserForDays(defaultUser, 2);
        Instant bannedUntil = defaultUser.getAccountDisabledUntil();
        Assertions.assertTrue(
                bannedUntil.isBefore(Instant.now().plus(3, ChronoUnit.DAYS))
        );
    }


    @Test
    void convertUserToContractor_ConvertUser_ConvertedToContractor() {
        String aboutMe = "I am a skilled contractor.";
        List<String> workPictures = new ArrayList<>();
        workPictures.add("work1.jpg");
        workPictures.add("work2.jpg");

        Location location = new Location("formatted", "country", "city", "suburb", "street", "postcode");
        locationRepository.save(location);

        userService.convertUserToContractor(defaultUser, aboutMe, workPictures, location);
        Contractor converted = (Contractor) userRepository.findByEmail(defaultUser.getEmail());

        assertInstanceOf(Contractor.class, converted);
        assertEquals("CONTRACTOR", String.valueOf(converted.getUserType()));
        assertEquals(aboutMe, converted.getAboutMe());
        assertEquals(workPictures, converted.getWorkPictures());
        assertEquals(location, converted.getLocation());
    }

    @Test
    void getContractorFlair_userNotContractor_getsNull() {
        String flairPath = userService.getContractorFlair(1L, Locale.ENGLISH).get(0);
        String flairNum = userService.getContractorFlair(1L, Locale.ENGLISH).get(1);
        Assertions.assertNull(flairPath);
        Assertions.assertNull(flairNum);
    }

    @Test
    void getContractorFlair_userDoesntExist_getsNull() {
        String flairPath = userService.getContractorFlair(999999L, Locale.ENGLISH).get(0);
        String flairNum = userService.getContractorFlair(999999L, Locale.ENGLISH).get(1);
        Assertions.assertNull(flairPath);
        Assertions.assertNull(flairNum);
    }




    @ParameterizedTest
    @CsvSource({
            "-1, /images/flair_leaf.png",
            "0, /images/flair_leaf.png",
            "1, /images/flair_straw.png",
            "5, /images/flair_straw.png",
            "9, /images/flair_straw.png",
            "10, /images/flair_cowboy.png",
            "32, /images/flair_cowboy.png",
            "49, /images/flair_cowboy.png",
            "50, /images/flair_archer.png",
            "79, /images/flair_archer.png",
            "99, /images/flair_archer.png",
            "100, /images/flair_crown.png",
            "333, /images/flair_crown.png",
            "499, /images/flair_crown.png",
            "500, /images/flair_flower_crown.png",
            "999, /images/flair_flower_crown.png",
            "1000000, /images/flair_flower_crown.png",

    })
    void getContractorFlair_hasCertainNumRatings_getsCorrectFlairPath(int numRatings, String expectedFlairPath) {
        String aboutMe = "I am a skilled contractor.";
        List<String> workPictures = new ArrayList<>();
        workPictures.add("work1.jpg");
        workPictures.add("work2.jpg");

        Location location = new Location("formatted", "country", "city", "suburb", "street", "postcode");
        locationRepository.save(location);

        userService.convertUserToContractor(defaultUser, aboutMe, workPictures, location);
        Contractor contractorUser = (Contractor) userRepository.findByEmail(defaultUser.getEmail());

        contractorUser.setNumRatings(numRatings);
        userRepository.save(contractorUser);


        String flairPath = userService.getContractorFlair(contractorUser.getUserId(), Locale.ENGLISH).get(0);
        Assertions.assertEquals(expectedFlairPath, flairPath);
    }
}