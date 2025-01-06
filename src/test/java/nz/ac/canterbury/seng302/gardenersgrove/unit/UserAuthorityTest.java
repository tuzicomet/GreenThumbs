package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Authority;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class UserAuthorityTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void createUser_GrantAuthority_UserHasAuthority() {
        // Create a new user
        User user = new User("Test", "User", "test@example.com", "password123", "2000-01-01", "hi");

        // Create a new authority
        Authority authority = new Authority("ADMIN");

        // Assign authority to the user
        user.grantAuthority(authority.getRole());

        // Save the user
        User savedUser = userRepository.save(user);

        // Retrieve the user from the database
        AbstractUser retrievedUser = userRepository.findByEmail("test@example.com");

        // Check that we have retrieved a user instead of null
        assertThat(retrievedUser).isNotNull();

        // Check if the user has the authority
        List<GrantedAuthority> authorities = retrievedUser.getAuthorities();
        // check that the user only has 1 authority
        assertEquals(1, authorities.size());
        // check that the authority at index 0 is "ADMIN"
        assertEquals("ADMIN", authorities.get(0).getAuthority());
    }
}