package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "FRIENDSHIPS")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FRIENDSHIP_ID")
    private Long friendshipId;

    @Column(name = "USER1_ID")
    private Long user1Id;

    @Column(name = "USER2_ID")
    private Long user2Id;

    /**
     * Default constructor, for use by JPA
     */
    public Friendship() {}

    /**
     * Constructor for creating a Friendship entity
     */
    public Friendship(Long user1Id, Long user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }

    // Getters and setters for each column

    public Long getFriendshipId() {
        return friendshipId;
    }

    public void setFriendshipId(Long friendshipId) {
        this.friendshipId = friendshipId;
    }

    public Long getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(Long user1Id) {
        this.user1Id = user1Id;
    }

    public Long getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(Long user2Id) {
        this.user2Id = user2Id;
    }
}