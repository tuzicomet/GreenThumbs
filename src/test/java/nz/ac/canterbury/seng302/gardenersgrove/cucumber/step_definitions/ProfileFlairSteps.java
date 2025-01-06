package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Contractor;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ProfileFlairSteps {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FriendshipRepository friendshipRepository;

    private MvcResult result;
    private Contractor contractor;
    @And("contractor has completed {int} jobs")
    public void contractorHasCompletedJobs(int jobsCompleted) {
        contractor = (Contractor) userService.getUserByEmail("contractor@gmail.com");
        contractor.setNumRatings(jobsCompleted);
        userRepository.save(contractor);
    }

    @When("I am on the profile page")
    public void iAmOnTheProfilePage() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profileTemplate"))
                .andReturn();
    }

    @Then("profile image displays a {string} hat flair")
    public void profileImageDisplaysAHatFlair(String expectedFlair) {
        String actualFlair = (String) result.getModelAndView().getModel().get("contractorFlair");
        Assertions.assertEquals("/images/flair_" + expectedFlair + ".png", actualFlair);
    }

    @And("I am friends with user with email {string}")
    public void iAmFriendsWithUserWithEmail(String friendEmail) {
        Long currentUserId = userService.getUserByEmail("userwithgarden@gmail.com").getUserId();
        Long friendId = userService.getUserByEmail("contractor@gmail.com").getUserId();
        Friendship friendship = new Friendship(currentUserId, friendId);
        friendshipRepository.save(friendship);
    }

    @When("I am on their profile page")
    public void iAmOnTheirProfilePage() throws Exception {
        Long friendId = userService.getUserByEmail("contractor@gmail.com").getUserId();
        result = mockMvc.perform(MockMvcRequestBuilders.get("/profile/user/" + friendId))
                .andExpect(status().isOk())
                .andExpect(view().name("profileTemplate"))
                .andReturn();
    }
}
