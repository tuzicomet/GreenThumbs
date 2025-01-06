package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

/**
 * Entity class to represent a verification token
 * takes a token, user, and expiry timestamp
 */
@Entity // Indicates that this class is a JPA entity
@Table(name = "VERIFICATION_TOKEN") // Specifies the table name in the database
public class VerificationToken {
    /**
     * The CrudRepository automatically generates an id as a primary key on upload.
     * Each Garden has a completely unique id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique activation token, required field
     */
    @Column
    private String token;

    /**
     * Timestamp for token expiry, required field
     */
    @Column(name = "expiry_date")
    private Timestamp expiryDate;

    /**
     * User which can be activated by this token, required field
     */
    @OneToOne( cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn( name = "user_id", referencedColumnName = "user_id")
    private AbstractUser user;

    /**
     * JPA (Java Persistance API) required no-args constructor, needed for DB to work
     */
    public VerificationToken(){}

    /**
     * Creates and stores a VerificationToken, which is attached to a user.
     * Tokens are deleted when used, otherwise they expire after 10 minutes.
     * @param token the unique token
     * @param user the user which can be activated by the token
     */
    public VerificationToken(String token, AbstractUser user){
        this.token = token;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Timestamp getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Timestamp expiryDate) {
        this.expiryDate = expiryDate;
    }

    public AbstractUser getUser() {
        return user;
    }

    public void setUser(AbstractUser user) {
        this.user = user;
    }
}
