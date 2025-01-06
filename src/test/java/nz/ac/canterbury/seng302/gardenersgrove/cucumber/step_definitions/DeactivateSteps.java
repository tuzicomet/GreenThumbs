package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.MailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class DeactivateSteps {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    public MailService mailService;
    @Autowired
    public GardenService gardenService;
    @Autowired
    public UserService userService;

    public Garden garden;
    private ResultActions tagResult;
    private ResultActions loginResult;

    private ResultActions postTagAddRequest(String content) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.multipart("/garden/%s/tag".formatted(garden.getId()))
                .param("tag", content)
                .with(csrf())
        );
    }

    private ResultActions postLoginRequest(String email, String password) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .param("email", email)
                .param("password", password)
                .with(csrf())
        );
    }

    @And("I have added {int} inappropriate tags")
    public void iHaveAddedInappropriateTags(int numTags) throws Exception {
        for (int i=0; i < numTags; i++) {
            postTagAddRequest("fuck");
        }
    }

    @And("I have a valid garden")
    public void setupGarden() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        garden = gardenService.addGarden(
                new Garden(
                        "Garden",
                        "Size",
                        currentUser,
                        "Description",
                        true,
                        null,
                        null,
                        true,
                        null
                )
        );
    }

    @When("I add a tag with content {string}")
    public void iAddATagWithContent(String content) throws Exception {
        tagResult = postTagAddRequest(content);
    }

    @Then("a message gives me a warning saying my account will be blocked")
    public void aMessageGivesMeAWarningSayingMyAccountWillBeBlocked() throws Exception {
        tagResult.andExpect(MockMvcResultMatchers.flash().attributeExists("fifthStrike"));
    }

    @And("I receive a fifth strike email")
    public void iReceiveAFifthStrikeEmail() {
        Mockito.verify(mailService, Mockito.times(1))
                .sendFifthStrikesEmail(any(User.class), any(Locale.class));
    }

    @And("I receive an account blocked email")
    public void iReceiveAnAccountBlockedEmail() {
        Mockito.verify(mailService, Mockito.times(1))
                .sendAccountBlockedEmail(any(User.class), any(Locale.class));
    }

    @Then("I am logged out")
    public void iAmLoggedOut() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/garden")).andExpect(
                MockMvcResultMatchers.status().is3xxRedirection()
        );
    }

    @Given("the user with email {string} is banned for {int} days")
    public void theUserWithEmailIsBannedForDays(String email, int numDaysBanned) {
        AbstractUser user = userService.getUserByEmail(email);
        userService.banUserForDays(user, numDaysBanned);
    }

    @When("I enter the email {string} and the password {string}")
    public void iEnterTheEmailAndThePassword(String email, String password) throws Exception {
        loginResult = postLoginRequest(email, password);
    }

    @Then("I see a message saying my account is blocked for {int} days")
    public void iSeeAMessageSayingYourAccountIsBlockedForDays(int days) {
        HttpSession session = Objects.requireNonNull(loginResult.andReturn().getRequest().getSession());
        Assertions.assertEquals("./login?error", loginResult.andReturn().getResponse().getRedirectedUrl());
        Assertions.assertEquals(
                Integer.toString(days),
                session.getAttribute("accountDisabledDate")
        );
    }

    @Given("the user with email {string} was banned for {int} days, {int} days ago")
    public void theUserWithEmailWasBannedDaysAgo(String email, int daysBanned, int daysAgo) {
        User user = (User) userService.getUserByEmail(email);
        user.setAccountDisabledUntil(Instant.now().minus(daysAgo - daysBanned, ChronoUnit.DAYS));
        userService.updateUserDetails(user);
    }

    @Then("I am logged in")
    public void iAmLoggedIn() {
        Assertions.assertEquals("/homepage", loginResult.andReturn().getResponse().getRedirectedUrl());
    }

    @And("I see a message saying I have been banned for one week")
    public void iSeeAMessageSayingIHaveBeenBannedForOneWeek() {
    }
}
