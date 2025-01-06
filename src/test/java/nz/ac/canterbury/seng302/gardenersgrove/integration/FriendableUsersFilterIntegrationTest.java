package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.FriendRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.utility.FriendableUsersFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FriendableUsersFilterIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private FriendableUsersFilter filter;
    private User currentUser;
    private User friendableUser;
    private User friendedUser;
    private User requestedUser;
    @BeforeEach
    void setUp(){
        //sets up users to test different cases, and saves them to the repositories
        currentUser = new User("current", "user", "current@gmail.com", "Password1@", null, null);
        friendableUser = new User("valid", "user", "valid@gmail.com", "Password1@", null, null);
        friendedUser = new User("friended", "user", "friended@gmail.com", "Password1@", null, null);
        requestedUser = new User("requested", "user", "requested@gmail.com", "Password1@", null, null);
        userRepository.save(currentUser);
        userRepository.save(friendableUser);
        userRepository.save(friendedUser);
        userRepository.save(requestedUser);

        FriendRequest req = new FriendRequest(currentUser.getUserId(), requestedUser.getUserId(), "PENDING");
        friendRequestRepository.save(req);
        Friendship friendship = new Friendship(currentUser.getUserId(), friendedUser.getUserId());
        friendshipRepository.save(friendship);

    }
    @Test
    void FilterUsers_UserIsCurrentUser_UserRemoved(){
        List<AbstractUser> users = new ArrayList<>();
        users.add(friendableUser);
        users.add(currentUser);
        List<AbstractUser> results = filter.getFriendAbleUsers(users, currentUser);

        assertTrue(results.contains(friendableUser));
        assertFalse(results.contains(currentUser));
        assertEquals(results.size(), 1);
    }
    @Test
    void FilterUsers_UserAlreadyFriend_UserRemoved(){
        List<AbstractUser> users = new ArrayList<>();
        users.add(friendableUser);
        users.add(friendedUser);
        List<AbstractUser> results = filter.getFriendAbleUsers(users, currentUser);

        assertTrue(results.contains(friendableUser));
        assertFalse(results.contains(friendedUser));
        assertEquals(results.size(), 1);
    }
    @Test
    void FilterUsers_RequestAlreadySentToUser_UserRemoved(){
        List<AbstractUser> users = new ArrayList<>();
        users.add(friendableUser);
        users.add(requestedUser);
        List<AbstractUser> results = filter.getFriendAbleUsers(users, currentUser);

        assertTrue(results.contains(friendableUser));
        assertFalse(results.contains(requestedUser));
        assertEquals(results.size(), 1);
    }
    @Test
    void FilterUsers_AllInvalidCases_ALLRemoved(){
        List<AbstractUser> users = new ArrayList<>();
        users.add(requestedUser);
        users.add(friendedUser);
        users.add(currentUser);
        List<AbstractUser> results = filter.getFriendAbleUsers(users, currentUser);

        assertFalse(results.contains(requestedUser));
        assertFalse(results.contains(friendedUser));
        assertFalse(results.contains(currentUser));
        assertEquals(results.size(), 0);
    }
}
