package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ServiceRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class RateContractorSteps {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    MessageSource messageSource;
    @Autowired
    ServiceRequestRepository serviceRequestRepository;
    @Autowired
    LocationService locationService;
    @Autowired
    GardenRepository gardenRepository;

    MvcResult result;


    String rating;
    private final long serviceRequestId = 17L; // This one is completed

    private final long incompleteServiceRequestId = 15L; // This one is not

    private Long id;



    @Then("I am redirected to the \"My Service Requests\" page")
    public void iAmRedirectedToTheMyServiceRequestsPage(){
        assertEquals("/myServiceRequests", result.getResponse().getRedirectedUrl());
    }
    @Then("I am redirected to the \"Service Request Details\" page")
    public void iAmRedirectedToTheServiceRequestDetails(){
        assertEquals("/serviceRequest/" + serviceRequestId, result.getResponse().getRedirectedUrl());
    }

    @When("I click the submit button")
    public void iClickTheSubmitButton() throws Exception {
        result = mockMvc.perform(post("/serviceRequest/"+ id +"/rating").param("rating", rating).with(csrf())).andReturn();
    }

    @And("I have selected a {string}-star rating")
    public void iHaveSelectedAStarRating(String rating) {
        this.rating = rating;
    }

    @Given("I am on the service request details page for a complete service request")
    public void iAmOnTheServiceRequestDetailsPageComplete() throws Exception {
        mockMvc.perform(get("/serviceRequest/" + serviceRequestId))
                .andExpect(status().isOk())
                .andExpect(view().name("serviceRequestDetailsTemplate"));
        id = serviceRequestId;
    }
    @Given("I am on the service request details page for an incomplete service request")
    public void iAmOnTheServiceRequestDetailsPageIncomplete() throws Exception {
        mockMvc.perform(get("/serviceRequest/" + incompleteServiceRequestId))
                .andExpect(status().isOk())
                .andExpect(view().name("serviceRequestDetailsTemplate"));
        id = incompleteServiceRequestId;
    }
}
