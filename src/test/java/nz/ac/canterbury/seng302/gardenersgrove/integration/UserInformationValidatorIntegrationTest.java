package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.UserInformationValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test is supposed to be an integration tests checking the interaction
 * between already existing emails and the validator. Using Transactional tag
 * ensures none of the additions to the database are persistent.
 **/
@SpringBootTest
@Transactional
class UserInformationValidatorIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserInformationValidator validator;

    @MockBean
    private MessageSource messageSource;

    @Test
    void validateEmailExists_ShouldIdentifyExistingEmail() {
        User user = new User();
        user.setEmail("existing@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("&&lkjsdfaTT");
        userService.addUser(user);

        Model model = new ExtendedModelMap();
        boolean result = validator.validateEmailExists("existing@example.com", model, null, messageSource, Locale.US);

        assertThat(result).isTrue();
        assertThat(model.containsAttribute("emailError")).isTrue();
    }

    @Test
    void validateEmailExists_ShouldPassForNewEmail() {
        Model model = new ExtendedModelMap();
        boolean result = validator.validateEmailExists("new@example.com", model, null, messageSource, Locale.US);

        assertThat(result).isFalse();
        assertThat(model.containsAttribute("emailError")).isFalse();
    }
}

