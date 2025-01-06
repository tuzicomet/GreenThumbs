package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests to test functionality for logging in users with Spring Security
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserLoginTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private MockMvc mvc;

    /**
     * Setup to be performed before each test in this class
     */
    @BeforeEach
    void setup() {
        // initialize a user to test logging in as, and save them to the database
        User testUser = new User("Real", "User",
                "ExistingUser@gmail.com", passwordEncoder.encode("Testp4$$"),
                "2000-10-10", "default");
        userRepository.save(testUser);

        // Set up the MockMvc instance to perform HTTP requests and handle responses
        mvc = MockMvcBuilders
                // Initialize the web application context for testing
                .webAppContextSetup(webApplicationContext)
                // Apply Spring Security configuration
                .apply(springSecurity())
                // Add the Spring Security filter chain
                .addFilter(springSecurityFilterChain)
                // Build the MockMvc instance with the configured settings
                .build();
    }

    /**
     * Test to check that GET requests to the login endpoint returns a 200 OK status
     * @throws Exception
     */
    @Test
    @Transactional
    void getLoginEndpoint_returnsOkStatus() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk()); // check status is 200 OK
    }

    /**
     * Test case to verify that if valid credentials are submitted to the
     * login form, the user successfully gets logged in.
     *
     * @throws Exception
     */
    @Test
    @Transactional
    void userAttemptsLogin_ValidCredentials_UserIsLoggedIn() throws Exception {

        // Perform form login with valid user credentials, by using mvc
        // to simulate a POST request to the login page with provided params
        mvc.perform(formLogin("/login")
                    // (an existing user with their correct password)
                    .user("ExistingUser@gmail.com")
                    .password("Testp4$$")
                    // Specify "email" as the parameter name for the login username
                    // (as formLogin in securityConfig is set to .usernameParameter("email"))
                    .userParameter("email")
                )
                // check that user is authenticated after successful login
                .andExpect(authenticated())
                // and redirected to the homepage
                .andExpect(redirectedUrl("/homepage"));
    }

    /**
     * Test case to verify that if an existing email with invalid pasword are
     * submitted to the login form, the user does not get logged in.
     *
     * @throws Exception
     */
    @Test
    @Transactional
    void userAttemptsLogin_InvalidPassword_UserIsNotLoggedIn() throws Exception {
        // Perform form login with invalid user credentials
        mvc.perform(formLogin("/login")
                        // (an existing user, but an incorrect password)
                        .user("ExistingUser@gmail.com")
                        .password("wrongPassword")
                        // Specify "email" as the parameter name for the login username
                        .userParameter("email")
                )
                // check that user is not authenticated
                .andExpect(unauthenticated())
                // and they should be taken back to login, but with an error parameter
                .andExpect(redirectedUrl("./login?error"));
    }

    /**
     * Test case to verify that if a non-existent email (and password) is
     * submitted to the login form, the user does not get logged in.
     *
     * @throws Exception
     */
    @Test
    @Transactional
    void userAttemptsLogin_EmailDoesNotExist_UserIsNotLoggedIn() throws Exception {
        // Perform form login with invalid user credentials
        mvc.perform(formLogin("/login")
                        // (a non-existent user, with anything as password)
                        .user("NonExistentUser@gmail.com")
                        .password("wrongPassword")
                        // Specify "email" as the parameter name for the login username
                        .userParameter("email")
                )
                // check that user is not authenticated
                .andExpect(unauthenticated())
                // and they should be taken back to login, but with an error parameter
                .andExpect(redirectedUrl("./login?error"));
    }

}