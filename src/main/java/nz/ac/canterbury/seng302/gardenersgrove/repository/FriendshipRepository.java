package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for performing CRUD operations on Friendship entities.
 */
@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * Retrieves a list of friendship records where the value in the USER1_ID
     * column matches the given user1Id
     */
    List<Friendship> findByUser1Id(Long user1Id);

    /**
     * Retrieves a list of friendship records where the value in the USER2_ID
     * column matches the given value
     */
    List<Friendship> findByUser2Id(Long user2Id);

    /**
     * Finds and deletes any Friendship record which match the given user1Id and user2Id
     */
    void deleteAllByUser1IdAndUser2Id(Long user1Id, Long user2Id);


    Friendship findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

}