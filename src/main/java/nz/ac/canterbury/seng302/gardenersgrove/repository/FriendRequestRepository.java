package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for performing CRUD operations on FriendRequest entities.
 */
@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    /**
     * Retrieves a list of friend request records where the value in the receiver id
     * column matches the given user id
     */
    List<FriendRequest> findByReceiverId(Long userId);

    /**
     * Retrieves a list of friend request records where the value in the sender id
     * column matches the given user id
     */
    List<FriendRequest> findBySenderId(Long userId);

    /**
     * Retrieves a list of friend request records where the values in the receiver id
     * and status columns match the given values, respectively
     */
    List<FriendRequest> findByReceiverIdAndStatus(Long userId, String status);

    /**
     * Deletes requests between a certain sender and receiver
     */
    void deleteAllBySenderIdAndReceiverId(Long senderId, Long receiverId);
    /**
     * Finds a Requests between a certain sender and receiver
     */
    FriendRequest findFriendRequestBySenderIdAndReceiverId(Long senderId, Long receiverId);

}