package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import java.util.HashSet;

@Entity // Indicates that this class is a JPA entity
@DiscriminatorValue("USER")
public class User extends AbstractUser{
    public User() {
        // Default constructor, only to be accessed by JPA
    }

    public User(String firstName, String lastName, String email, String password, String dateOfBirth, String profilePicture) {
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setEmail(email);
        this.setPassword(password);
        this.setDateOfBirth(dateOfBirth);
        this.setProfilePicture(profilePicture != null ? profilePicture : DEFAULT_IMAGE_PATH);
        this.gardens = new HashSet<>();
    }
}