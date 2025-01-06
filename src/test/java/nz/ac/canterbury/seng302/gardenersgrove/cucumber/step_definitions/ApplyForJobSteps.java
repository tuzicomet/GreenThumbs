package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ApplyForJobSteps {
    @Autowired
    MockMvc mockMvc;
    private MvcResult result;
    final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private String date;
    private String price;
    private int serviceRequestId;

    private MvcResult postJobApplication() throws Exception {
        return mockMvc.perform(post("/serviceRequest/" + serviceRequestId)
                        .with(csrf())
                        .param("date", date)
                        .param("price", price)
        ).andReturn();
    }

    @And("I am on the job application modal for service request {int}")
    public void iAmOnTheJobApplicationModalForAServiceRequest(int serviceRequestId) throws Exception {
        // id of the test service request
        // this current service request has a price range of 10.0 to 20.0
        // and a date range of 11/08/2024 - 24/09/2025
        this.serviceRequestId = serviceRequestId;
        mockMvc.perform(get("/serviceRequest/" + serviceRequestId));
    }

    @When("I enter valid values for the date and quote price")
    public void iEnterValidValuesForTheDateAndQuotePrice() {
        date = "07/12/2024";
        price = "15";
    }

    @When("I enter a valid date and invalid quote price {string}")
    public void iEnterAValidDateAndInvalidQuotePrice(String inputPrice) {
        date = "07/12/2024";
        price = inputPrice;
    }

    @And("I click the Submit button to submit my job application")
    public void iClickTheSubmitButtonToSubmitMyJobApplication() throws Exception {
        result = postJobApplication();
    }

    @Then("I am taken back to the service request details page")
    public void iAmTakenBackToTheServiceRequestDetailsPage() {
        String redirectedUrl = Objects.requireNonNull(result.getResponse().getRedirectedUrl());
        Assertions.assertTrue(redirectedUrl.matches("/serviceRequest/" + serviceRequestId));
    }

    @Then("I am on the same page and data is persisted and error message shows")
    public void iAmOnTheSamePageAndDataIsPersistedAndErrorMessageShows() throws Exception {
        MvcResult result = postJobApplication();
        // Try to find the saved job application in the database by getting the user's most recent application
        ModelAndView mv = result.getModelAndView();

        // Check that the saved job application does not exist
        assertNotNull(mv);
        assertNotNull(mv.getModel());

        assertNotNull(mv.getViewName());

        assertEquals("serviceRequestDetailsTemplate", mv.getViewName());


        assertEquals(date, mv.getModel().get("date"));
        assertEquals(price, mv.getModel().get("price"));
        assertNotNull(mv.getModel().get("errorPrice"));
    }

}
