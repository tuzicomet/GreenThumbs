package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class BrowseSteps {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    MessageSource messageSource;
    @Autowired
    private GardenService gardenService;
    @Autowired
    private UserService userService;

    private MvcResult result;

    private Garden createdGarden;

    @Transactional
    @Given("another user owns {int} gardens marked public")
    public void aUserOwnsAGarden(int numGardens) {

        AbstractUser user = userService.addUser(
                new User(
                        "John",
                        "Doe",
                        "john.doe@test.com",
                        "Testp4$$",
                        "01/01/1990",
                        User.DEFAULT_IMAGE_PATH
                )
        );

        for (int i = 0; i < numGardens; i++) {
            createdGarden = gardenService.addGarden(
                    new Garden(
                            "Garden" + i,
                            "Size" + i,
                            user,
                            "Description" + i,
                            true,
                            null,
                            null,
                            true,
                            null
                    )
            );
        }
    }

    @When("I click on a link to the garden")
    public void iClickOnALinkToTheGarden() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get("/garden/" + createdGarden.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("gardenDetailsTemplate"))
                .andReturn();
    }

    @Then("I can view the garden name, size, and plants")
    public void iCanViewTheGardenNameSizeAndPlants() {
        assertTrue(result.getModelAndView().getModel().containsKey("garden"));
    }

    @Given("I am on the browse gardens page")
    public void iAmOnTheBrowseGardensPage() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get("/browseGardens"))
                .andExpect(status().isOk())
                .andExpect(view().name("browseGardensTemplate"))
                .andReturn();

    }

    @When("I enter a search string and click the search button")
    public void iEnterASearchStringAndClickTheSearchButton() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get("/browseGardens")
                        .param("search", "Garden")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("browseGardensTemplate"))
                .andReturn();
    }

    @Then("I am shown gardens whose names or plants include my search value")
    public void iAmShownGardensWhoseNamesOrPlantsIncludeMySearchValue() {
        assertTrue(Objects.requireNonNull(result.getModelAndView()).getModel().containsKey("browseGardens"));
        List<Garden> gardens = (List<Garden>) result.getModelAndView().getModel().get("browseGardens");
        gardens.forEach(garden ->
                assertTrue(garden.getName().contains("Garden") || garden.getDescription().contains("Garden"))
        );
    }

    @When("I enter the search string {string}")
    public void iEnterASearchStringThatHasNoMatches(String searchString) throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get("/browseGardens")
                        .param("search", searchString)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("browseGardensTemplate"))
                .andReturn();
    }

    @Then("a message tells me “No gardens match your search”")
    public void aMessageTellsMeNoGardensMatchYourSearch() {
        assertTrue(result.getModelAndView().getModel().containsKey("message"));
        assertEquals("No gardens match your search", result.getModelAndView().getModel().get("message"));
    }

    @Given("there are more than {int} public gardens")
    public void thereAreMoreThanNPublicGardens(int numGardens) throws Exception {
        for (int i = 0; i < numGardens + 1; i++) {
            gardenService.addGarden(
                    new Garden(
                            "Garden" + i,
                            "Size" + i,
                            createdGarden.getOwner(),
                            "Description" + i,
                            true,
                            null,
                            null,
                            true,
                            null
                    )
            );
        }

        result = mockMvc.perform(MockMvcRequestBuilders.get("/browseGardens")
                        .param("search", "Garden")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("browseGardensTemplate"))
                .andReturn();
    }

    @Then("the results are paginated with 10 per page")
    public void theResultsArePaginatedWith10PerPage() {
        assertTrue(Objects.requireNonNull(result.getModelAndView()).getModel().containsKey("browseGardens"));
        List<Garden> gardens = (List<Garden>) result.getModelAndView().getModel().get("browseGardens");
        assertEquals(10, gardens.size());

        assertTrue(result.getModelAndView().getModel().containsKey("currentPage"));
        assertTrue(result.getModelAndView().getModel().containsKey("totalItems"));
        assertTrue(result.getModelAndView().getModel().containsKey("totalPages"));
        assertTrue(result.getModelAndView().getModel().containsKey("size"));

        long totalItems = (long) result.getModelAndView().getModel().get("totalItems");
        int totalPages = (int) result.getModelAndView().getModel().get("totalPages");
        int size = (int) result.getModelAndView().getModel().get("size");

        assertEquals((totalItems + size - 1) / size, totalPages);
    }


    @Given("I am on any page")
    public void iAmOnAnyPage() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get("/browseGardens")
                        .param("search", "Garden")
                        .param("page", "1")
                        .param("size", "10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("browseGardensTemplate"))
                .andReturn();
    }
}
