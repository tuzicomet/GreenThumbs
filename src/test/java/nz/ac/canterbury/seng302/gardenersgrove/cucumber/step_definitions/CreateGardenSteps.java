package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Objects;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class CreateGardenSteps {
    @Autowired
    MockMvc mockMvc;

    private String name = "DEFAULT NAME";
    private String location = "formatted";
    private String country = "New Zealand";
    private String city = "Christchurch";
    private String size = "100";
    private String description = "description";

    private MvcResult result;

    private MvcResult postCreateGardenRequest() throws Exception {
        return mockMvc.perform(post("/garden")
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

    @Given("I am on the create garden form")
    public void iAmOnTheCreateGardenForm() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/garden"))
                .andExpect(status().isOk())
                .andExpect(view().name("gardenFormTemplate"))
                .andReturn().getResponse();
    }


    @When("I enter an empty garden name")
    public void iEnterAnEmptyGardenName() {
        name = "";
    }

    @When("I enter the garden name {string}")
    public void iEnterTheGardenName(String inputName) {
        name = inputName;
    }

    @When("I click the Submit button")
    public void iClickTheSubmitButton() throws Exception {
        result = postCreateGardenRequest();
    }

    @Then("I am taken to the garden details page")
    public void iAmTakenToTheGardenDetailsPage(){
        String redirectedUrl = Objects.requireNonNull(result.getResponse().getRedirectedUrl());
        Assertions.assertTrue(redirectedUrl.matches("/garden/[0-9]+"));
    }

    @Then("I remain on the garden form and I am shown the message {string}")
    public void iRemainOnTheGardenFormAndIAmShownTheMessage(String errorMessage){
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("gardenFormTemplate",
                mv.getViewName());
        List<String> errors = (List<String>) mv.getModel().get("errors");
        Assertions.assertTrue(errors.contains(errorMessage));
    }

    @When("I enter valid garden details")
    public void iEnterValidGardenDetails() {}

    @When("I enter the garden size {string}")
    public void iEnterAnInvalidSize(String inputSize) {
        size = inputSize;
    }
}