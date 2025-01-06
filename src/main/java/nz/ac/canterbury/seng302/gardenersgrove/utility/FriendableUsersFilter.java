package nz.ac.canterbury.seng302.gardenersgrove.utility;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.FriendRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to filter search results to only include users to whom the active user can send a friend request.
 */
@Service
public class FriendableUsersFilter {
    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    @Autowired
    public FriendableUsersFilter(FriendshipRepository friendshipRepository, FriendRequestRepository friendRequestRepository){
        this.friendshipRepository = friendshipRepository;
        this.friendRequestRepository = friendRequestRepository;
    }

    /**
     * Filters a list of users, returning the subset of the given user list this can be sent  friend request by the active user
     * @param users A list of users to filter
     * @param currentUser The active user
     * @return A filtered list of users, containing only those of the input that can be sent a friend request by the active user.
     */
    public List<AbstractUser> getFriendAbleUsers(List<AbstractUser> users, AbstractUser currentUser){
        List<AbstractUser> results = new ArrayList<>();
        for(AbstractUser user: users){
            if(userIsFriendable(user, currentUser)){
                results.add(user);}
        }
        return results;
    }

    /**
     * Checks if a given user can be sent a friend request by the active user
     * @param user The user to determine friendability for
     * @param currentUser The active user
     * @return A boolean: true if the user is can be sent a friend request by the active user, false otherwise
     */
    public boolean userIsFriendable(AbstractUser user, AbstractUser currentUser){
        if(usersAreFriends(user, currentUser)){return false;}
        if(requestAlreadyExists(user, currentUser)){return false;}
        return !userIsCurrentUser(user, currentUser);
    }

    /**
     * Checks if two user are already friends
     * @param userOne The first user
     * @param userTwo The second user
     * @return A boolean: true if the two users are friends, false otherwise
     */
    public boolean usersAreFriends(AbstractUser userOne, AbstractUser userTwo){
        // get two lists of friends, to get all friendships including userOne
        List<Friendship> friendsOne = friendshipRepository.findByUser1Id(userOne.getUserId());
        List<Friendship> friendsTwo = friendshipRepository.findByUser2Id(userOne.getUserId());

        // check each of the friendships to see if the other use is userTwo
        boolean firstCheck = friendsOne.stream()
                .anyMatch(friend -> friend.getUser2Id().equals(userTwo.getUserId()));
        boolean secondCheck = friendsTwo.stream()
                .anyMatch(friend -> friend.getUser1Id().equals(userTwo.getUserId()));
        return firstCheck || secondCheck;
    }

    /**
     * Checks if the current user has already sent a request to the given user
     * The status of the request is irrelevant, as users cannot send another request after being declined
     * @param user The user to whom the active user would like to send a request
     * @param currentUser The active user
     * @return A boolean: true if the active user has already sent the given user a friend request, false otherwise
     */
    public boolean requestAlreadyExists(AbstractUser user, AbstractUser currentUser){
        FriendRequest req = friendRequestRepository.findFriendRequestBySenderIdAndReceiverId(currentUser.getUserId(), user.getUserId());
        return req != null;
    }

    /**
     * Checks if the given user is the active user
     * @param user The given user
     * @param currentUser The active user
     * @return A boolean: true if the active user is the given user, false otherwise;
     */
    public boolean userIsCurrentUser(AbstractUser user, AbstractUser currentUser){
        return user.getUserId().equals(currentUser.getUserId());
    }
}

