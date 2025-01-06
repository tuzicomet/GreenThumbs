package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

@WebMvcTest(AbstractUser.class)
class AbstractUserTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserService userService;

    // GardenRepository is required by GlobalControllerAdvice
    @MockBean
    private GardenRepository gardenRepository;

    @MockBean
    private Authentication authentication;

    User user;

    User userNoLastName;

    @BeforeEach
    void setup() {

        authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);


        user = new User(
                "Mock",
                "User",
                "test@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );

        user.setUserId(1L);

        userNoLastName = new User(
                "Mock",
                null,
                "test@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );

        userNoLastName.setUserId(2L);

    }

    @Test
    void getFormattedName_UserWithLastName_returnsWholeName() {

        when(userService.getUserFromAuthentication(authentication)).thenReturn(user);

        String wholeName = user.getFormattedName();
        Assertions.assertNotNull(wholeName);
        Assertions.assertEquals(user.getFirstName() + " " + user.getLastName(), wholeName);
    }

    @Test
    void getFormattedName_UserWithOutLastName_returnsFirstName() {
        String wholeName = userNoLastName.getFormattedName();
        Assertions.assertNotNull(wholeName);
        Assertions.assertEquals(user.getFirstName(), wholeName);
    }
}
