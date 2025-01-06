package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class CreatePlantSteps {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    GardenService gardenService;

    private MvcResult result;
    private Garden garden;

    // Plant default parameters
    private String name = "Valid Name";
    private String count = "1";
    private String description = "Pretty cool plant";
    private String date = "01/01/1990";
    private String imagePath = Plant.DEFAULT_IMAGE_PATH;
    private byte[] image = new byte[0];


    private MvcResult postCreatePlantRequest() throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.multipart("/garden/%s/plant".formatted(garden.getId()))
                .file("image", image)
                .param("name", name)
                .param("count", count)
                .param("description", description)
                .param("date", date)
                .param("imagePath", imagePath)
                .with(csrf())
        ).andReturn();
    }

    @And("I have a garden")
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

    @Given("I am on the create plant form")
    public void iAmOnTheCreateGardenForm() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get("/garden/" + garden.getId() + "/plant").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("plantFormTemplate"))
                .andReturn();
    }

    @When("I enter the plant name {string}")
    public void iEnterAnEmptyGardenName(String inputName) {
        name = inputName;
    }

    @When("I click the Submit Plant button")
    public void iClickTheSubmitButton() throws Exception {
        result = postCreatePlantRequest();
    }

    @Then("I am taken to the Garden Details page")
    public void iAmTakenToTheMyGardensPage(){
        Assertions.assertEquals(
                "/garden/" + garden.getId(),
                result.getResponse().getRedirectedUrl()
        );
    }

    @Then("I remain on the plant form, and I see the name error message {string}")
    public void iRemainOnThePlantFormAndISeeTheNameErrorMessage(String expectedError){
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("plantFormTemplate",
                mv.getViewName());
        String actualError = (String) mv.getModel().get("errorName");
        Assertions.assertEquals(expectedError, actualError);
    }

    @Then("I remain on the plant form, and I see the description error message {string}")
    public void iRemainOnThePlantFormAndISeeTheDescriptionErrorMessage(String expectedError){
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("plantFormTemplate",
                mv.getViewName());
        String actualError = (String) mv.getModel().get("errorDescription");
        Assertions.assertEquals(expectedError, actualError);
    }

    @Then("I remain on the plant form, and I see the count error message {string}")
    public void iRemainOnThePlantFormAndISeeTheCountErrorMessage(String expectedError){
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("plantFormTemplate",
                mv.getViewName());
        String actualError = (String) mv.getModel().get("errorCount");
        Assertions.assertEquals(expectedError, actualError);
    }

    @Then("I remain on the plant form, and I see the date error message {string}")
    public void iRemainOnThePlantFormAndISeeTheDateErrorMessage(String expectedError){
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("plantFormTemplate",
                mv.getViewName());
        String actualError = (String) mv.getModel().get("errorDate");
        Assertions.assertEquals(expectedError, actualError);
    }

    // Empty as valid by default, kept to make sure the Gherkin makes sense
    @When("I enter valid plant details")
    public void iEnterValidGardenDetails() {}

    @When("I enter an invalid plant name")
    public void iEnterAnInvalidGardenName() {
        name = "sdfsdf/fds/??";
    }
    @When("I enter a description that is too long")
    public void iEnterADescriptionThatIsTooLong() {
        description = "A".repeat(513);
    }
    @When("I enter an invalid plant count")
    public void iEnterAnInvalidPlantCount() {
        count = "10ABC";
    }

    @When("I enter a malformed plant date")
    public void iEnterAMalformedPlantDate() {
        date = "2003-123-09";
    }
}