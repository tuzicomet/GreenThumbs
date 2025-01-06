package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.FriendRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to provide support for managing friendships
 */
@Service
public class FriendshipService {
    private static final Logger LOG = LoggerFactory.getLogger(FriendshipService.class);

    private final FriendshipRepository friendshipRepository;

    private final FriendRequestRepository friendRequestRepository;

    @Autowired
    public FriendshipService(FriendRequestRepository friendRequestRepository, FriendshipRepository friendshipRepository) {
        this.friendRequestRepository = friendRequestRepository;
        this.friendshipRepository =friendshipRepository;
    }

    /**
     * Accepts friend request: deletes the request and makes the users friends
     * @param sender the user that sent the request
     * @param receiver the user that accepted the request
     * returns a boolean to indicate whether the process was accepted.
     */
    @Transactional
    public boolean acceptRequest(AbstractUser sender, AbstractUser receiver) {
        FriendRequest friendRequest = friendRequestRepository.findFriendRequestBySenderIdAndReceiverId(sender.getUserId(), receiver.getUserId());

        if (friendRequest != null && "PENDING".equals(friendRequest.getStatus())) {
            friendRequestRepository.delete(friendRequest);
            Friendship friendship = new Friendship(sender.getUserId(), receiver.getUserId());
            friendshipRepository.save(friendship);
            LOG.info("Saved Friendship between " + sender.getEmail() + " and " + receiver.getEmail());
            return false; // no error message
        } else {
            LOG.info("Friend request from " + sender.getEmail() + " to " + receiver.getEmail() + " is no longer pending.");
            return true;
        }
    }


    /**
     * Declines a friend request: updates the status of the request to declined
     * @param sender the user that sent the request
     * @param receiver the user that declined the request
     * returns a boolean to indicate whether the process was accepted.
     */
    @Transactional
    public boolean declineRequest(AbstractUser sender, AbstractUser receiver) {
        FriendRequest friendRequest = friendRequestRepository.findFriendRequestBySenderIdAndReceiverId(sender.getUserId(), receiver.getUserId());

        if (friendRequest != null && "PENDING".equals(friendRequest.getStatus())) {
            friendRequest.setStatus("DECLINED");
            friendRequestRepository.save(friendRequest);
            LOG.info("Declined friend request from " + sender.getEmail() + " to " + receiver.getEmail());
            return false; // no error message
        } else {
            LOG.info("Friend request from " + sender.getEmail() + " to " + receiver.getEmail() + " is no longer pending.");
            return true;
        }
    }


    /**
     * Method to delete any sent friend request from a sender to a given receiving user
     */
    @Transactional
    public void cancelRequest(AbstractUser sender, AbstractUser receiver) {
        friendRequestRepository.deleteAllBySenderIdAndReceiverId(sender.getUserId(), receiver.getUserId());
        LOG.info("Cancelled friend request between " + sender.getEmail() + " " + receiver.getEmail());
    }

    /**
     * Method to remove friendship between two given users
     */
    @Transactional
    public void removeFriendship(AbstractUser user1, AbstractUser user2) {
        // The friendship table uses two columns: User1_ID, and User2_ID.
        // A friendship between two users only requires one record with both of their ID's in
        // either of the two columns. As we do not know which user is in what column,
        // we can guarantee their friendship is removed by searching both ways.
        friendshipRepository.deleteAllByUser1IdAndUser2Id(user1.getUserId(), user2.getUserId());
        friendshipRepository.deleteAllByUser1IdAndUser2Id(user2.getUserId(), user1.getUserId());
        LOG.info("Friendship removed between " + user1.getEmail() + " " + user2.getEmail());
    }

    public boolean areFriends(AbstractUser user1, AbstractUser user2) {
        Friendship friendship1 = friendshipRepository.findByUser1IdAndUser2Id(user1.getUserId(), user2.getUserId());
        Friendship friendship2 = friendshipRepository.findByUser1IdAndUser2Id(user2.getUserId(), user1.getUserId());
        return friendship1 != null || friendship2 != null;
    }
}
