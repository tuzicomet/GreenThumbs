package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class EditGardenSteps {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    GardenRepository gardenRepository;
    @Autowired
    GardenService gardenService;

    private String name = "DEFAULT NAME";
    private String location = "formatted";
    private String country = "New Zealand";
    private String city = "Christchurch";
    private String size = "100";
    private String description = "description";
    private long id = 0L;

    private MvcResult result;
    private MvcResult postEditGardenRequest() throws Exception {
        return mockMvc.perform(post("/garden/" + id + "/edit")
                        .with(csrf())
                        .param("name", name)
                        .param("location", location)
                        .param("country", country)
                        .param("city", city)
                        .param("size", size)
                        .param("description", description)
                )
                .andReturn();
    }
    @Given("I have a garden to edit")
    public void iHaveAGardenWithId() throws Exception{
        AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        id = gardenService.addGarden(
                new Garden(
                        "DEFAULT NAME",
                        "100",
                        currentUser,
                        "description",
                        true,
                        null,
                        null,
                        true,
                        null
                )
        ).getId();
    }
    @Given("I am on the edit garden form")
    public void iAmOnTheEdtGardenForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/garden/" + id +"/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("editGardenFormTemplate"))
                .andReturn().getResponse();
    }

    @When("I enter valid garden details to edit")
    public void iEnterValidGardenDetails() {}
    @When("I click the Submit button to edit")
    public void iClickTheSubmitButton() throws Exception {
        result = postEditGardenRequest();
    }

    @Then("The garden details are updated")
    public void theGardenDetailsAreUpdated() {
        Garden garden = gardenRepository.findById(id).get();
        Assertions.assertEquals(name, garden.getName());
        Assertions.assertEquals(size, garden.getSize());
        Assertions.assertEquals(description, garden.getDescription());
    }

    @Then("I am taken to the Garden Details page after edit")
    public void iAmTakenToTheGardenDetailsPage(){
        Garden garden = gardenRepository.findById(id).get();
        Assertions.assertEquals(
                "/garden/" + garden.getId(),
                result.getResponse().getRedirectedUrl()
        );
    }

    @When("I enter the name {string} to edit")
    public void iEnterTheNameToEdit(String newName) {
        name = newName;
    }
    @When("I enter the size {string} to edit")
    public void iEnterTheSizeToEdit(String newSize) {
        size = newSize;
    }
    @Then("The garden details are not updated")
    public void theGardenDetailsAreNotUpdated() {
        Garden garden = gardenRepository.findById(id).get();
        Assertions.assertEquals("DEFAULT NAME", garden.getName());
        Assertions.assertEquals("100", garden.getSize());
        Assertions.assertEquals("description", garden.getDescription());

    }
    @Then("I am not redirected")
    public void iAmTakenToTheEditGardenPage(){
        Garden garden = gardenRepository.findById(id).get();
        Assertions.assertEquals(
                200,
                result.getResponse().getStatus()
        );
    }
}
