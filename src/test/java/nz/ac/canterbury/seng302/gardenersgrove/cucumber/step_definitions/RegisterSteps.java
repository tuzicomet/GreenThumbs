package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.MailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import java.time.ZonedDateTime;
import java.util.Objects;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class RegisterSteps {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MailService mailService;
    @Autowired
    UserService userService;

    String firstName;
    String lastName;
    String email;
    String password;
    String confirmPassword;
    String dateOfBirth;

    MvcResult result;

    private MvcResult postRegisterRequest() throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post("/register")
                .with(csrf())
                .param("firstName", firstName)
                .param("lastName", lastName)
                .param("email", email)
                .param("password", password)
                .param("confirmPassword", confirmPassword)
                .param("dateOfBirth", dateOfBirth)
                .param("submit", "Submit") // ??
        ).andReturn();
    }

    @Before
    public void setupArguments() {
        firstName = "Test";
        lastName = "User";
        email = "email@email.mail";
        password = "Password1@";
        confirmPassword = "Password1@";
        dateOfBirth = "09/05/2003";
    }

    @Given("I am on the register form")
    public void iAmOnTheRegisterForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("registerTemplate"))
                .andReturn();
    }

    @And("a user with the email {string} is registered")
    @Transactional
    public void setupUserWithEmail(String email) {
        User user = new User(
                "John",
                "Doe",
                email,
                "Testp4$$",
                "01/01/1990",
                User.DEFAULT_IMAGE_PATH
        );
        userService.addUser(user);
    }

    // Empty as it is done by default, but still needed to make the gherkin make sense.
    @When("I enter valid registration details")
    public void iEnterValidRegistrationDetails() {
    }

    @And("I click the Sign Up button")
    public void iClickTheSignUpButton() throws Exception {
        result = postRegisterRequest();
    }

    @Then("I am taken to the activate page")
    public void iAmTakenToTheActivatePage() {
        Assertions.assertEquals("/activate", result.getResponse().getRedirectedUrl());
    }

    @Then("I remain on the register page, and I see the first name error {string}")
    public void iRemainOnTheRegisterPageAndISeeTheFirstNameError(String message) {
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("registerTemplate", mv.getViewName());
        String error = (String) mv.getModel().get("firstNameError");
        Assertions.assertEquals(message, error);
    }

    @Then("I remain on the register page, and I see the last name error {string}")
    public void iRemainOnTheRegisterPageAndISeeTheLastNameError(String message) {
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("registerTemplate", mv.getViewName());
        String error = (String) mv.getModel().get("lastNameError");
        Assertions.assertEquals(message, error);
    }

    @Then("I remain on the register page, and I see the email error {string}")
    public void iRemainOnTheRegisterPageAndISeeTheEmailError(String message) {
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("registerTemplate", mv.getViewName());
        String error = (String) mv.getModel().get("emailError");
        Assertions.assertEquals(message, error);
    }

    @Then("I remain on the register page, and I see the date error {string}")
    public void iRemainOnTheRegisterPageAndISeeTheDateError(String message) {
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("registerTemplate", mv.getViewName());
        String error = (String) mv.getModel().get("dobError");
        Assertions.assertEquals(message, error);
    }

    @Then("I remain on the register page, and I see the password strength error {string}")
    public void iRemainOnTheRegisterPageAndISeeThePasswordStrengthError(String message) {
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("registerTemplate", mv.getViewName());
        String error = (String) mv.getModel().get("passwordStrengthError");
        Assertions.assertEquals(message, error);
    }

    @Then("I remain on the register page, and I see the password error {string}")
    public void iRemainOnTheRegisterPageAndISeeThePasswordError(String message) {
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("registerTemplate", mv.getViewName());
        String error = (String) mv.getModel().get("passwordError");
        Assertions.assertEquals(message, error);
    }

    @When("I enter the first name {string}")
    public void iEnterFirstName(String inputName) {
        firstName = inputName;
    }

    @When("I enter the last name {string}")
    public void iEnterLastName(String inputName) {
        lastName = inputName;
    }

    @When("I enter a first name over {int} characters")
    public void iEnterFirstNameWithNCharacters(int numCharacters) {
        firstName = "A".repeat(numCharacters + 1);
    }

    @When("I enter a last name over {int} characters")
    public void iEnterLastNameWithNCharacters(int numCharacters) {
        lastName = "A".repeat(numCharacters + 1);
    }

    @When("I enter the email {string}")
    public void iEnterEmail(String inputEmail) {
        email = inputEmail;
    }

    @When("I enter the date {string}")
    public void iEnterAMalformedDate(String inputDate) {
        dateOfBirth = inputDate;
    }

    @When("I enter a date that is {int} years ago")
    public void iEnterADateThatIsNYearsAgo(int numberOfYears) {
        ZonedDateTime nYearsAgo = ZonedDateTime.now().minusYears(numberOfYears);
        dateOfBirth = "01/01/" + nYearsAgo.getYear();
    }

    @When("I enter the password {string}")
    public void iEnterPassword(String inputPassword) {
        password = inputPassword;
    }

    @When("I enter the confirm password {string}")
    public void iEnterConfirmPassword(String inputPassword) {
        confirmPassword = inputPassword;
    }

    @And("I enter the same confirm password")
    public void iEnterTheSameConfirmPassword() {
        confirmPassword = password;
    }
}
