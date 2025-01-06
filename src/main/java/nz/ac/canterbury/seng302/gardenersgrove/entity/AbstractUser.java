package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AbstractUser is the base class representing a user in the application.
 * This class is mapped to a single table in the database using the SINGLE_TABLE inheritance strategy.
 * It contains common properties and methods for all types of users.
 * When query user and contractor can be treated as two separate tables
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "APP_USERS")  // Specifies the name of the table
public class AbstractUser{

    private static final Logger LOG = LoggerFactory.getLogger(User.class);

    public static final String DEFAULT_IMAGE_PATH = "/images/default.jpg";

    @Column(name = "user_type", insertable = false, updatable = false)
    private String userType;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId; // Primary key

    @Column(name = "first_name", nullable = false)
    private String firstName; // First name of the user

    @Column(name = "last_name")
    private String lastName; // Last name of the user

    @Column(name = "email", nullable = false)

    private String email; // Email of the user

    @Column(name = "password", nullable = false)
    private String password; // Password of the user

    @Column(name = "date_of_birth")
    private String dateOfBirth; // Date of birth of the user (as a string for simplicity)

    @Column(name = "profile_picture")
    private String profilePicture; // URL of the user's profile picture

    @Column()
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Authority> userRoles = new ArrayList<>(); // Initialize it here

    @Column(name = "number_of_strikes")
    @ColumnDefault("0")
    private int numberOfStrikes = 0;

    @OneToMany(mappedBy = "user")
    private List<RecentGardens> gardenVisits;

    /**
     * Timestamp of the point the user is banned until. If null, means the user hasn't been banned before.
     */
    @Column
    private Instant accountDisabledUntil;

    /**
     * Set of owned gardens.
     * ManyToMany + CascadeType. ALL means the Garden. Owners and User. Gardens sets are always in sync
     */
    @OneToMany(mappedBy = "owner",fetch = FetchType.EAGER)
    Set<Garden> gardens;


    public void grantAuthority(String authority) {
        if (userRoles == null) {
            userRoles = new ArrayList<>();
        }
        userRoles.add(new Authority(authority));
    }

    public void removeAuthority(String authority) {
        if (userRoles != null) {
            userRoles.removeIf(a -> a.getRole().equals(authority));
        }
    }

    public boolean isEnabled() {
        if (userRoles != null) {
            return userRoles.stream()
                    .anyMatch(authority -> "ROLE_USER_VERIFIED".equals(authority.getRole()));
        }
        return false;
    }

    public List<GrantedAuthority> getAuthorities(){
        List<GrantedAuthority> authorities = new ArrayList<>();
        this.userRoles.forEach(authority -> authorities.add(new SimpleGrantedAuthority(authority.getRole())));
        return authorities;
    }

    // Getters and setters for all fields
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFormattedName() {
        return (this.lastName == null ? this.firstName : this.firstName + " " + this.lastName);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Set<Garden> getOwnedGardens() { return gardens; }

    public Set<Garden> getOwnedPublicGardens() {
        return gardens.stream().filter(Garden::isPublicised).collect(Collectors.toSet());
    }

    public int getNumberOfStrikes() {
        return numberOfStrikes;
    }

    public void setNumberOfStrikes(int strikes) {this.numberOfStrikes = strikes;}

    public Instant getAccountDisabledUntil() {
        if (accountDisabledUntil == null) {
            return Instant.now().minus(5, ChronoUnit.MINUTES);
        }
        return accountDisabledUntil;
    }

    public void setAccountDisabledUntil(Instant accountDisabledUntil) {
        this.accountDisabledUntil = accountDisabledUntil;
    }

    /**
     * Returns whether the account is currently disabled
     * @return true if the account is currently disabled/banned, false otherwise
     */
    public boolean isDisabled() {
        if (accountDisabledUntil == null) {
            return false;
        }
        return accountDisabledUntil.isAfter(Instant.now());
    }

    public String getUserType() {
        return userType;
    }

    /**
     * Increments the user's strike count.
     */
    public void incrementStrikes() {
        numberOfStrikes++;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                '}';
    }
}
