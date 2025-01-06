package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ServiceRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.ServiceRequestService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ServiceRequestSteps {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ServiceRequestRepository serviceRequestRepository;
    @Autowired
    ServiceRequestService serviceRequestService;
    private MvcResult result;
    private MvcResult resultEdit;
    final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private final long gardenId = 2L;
    private String title;
    private String description;
    private String dateMin;
    private String dateMax;
    private String priceMin;
    private String priceMax;

    private final long serviceRequestId = 1L;

    private MvcResult postCreateServiceRequest() throws Exception {
        return mockMvc.perform(post("/newServiceRequest")
                .param("title" ,title)
                .param("description", description)
                .param("dateMin", dateMin)
                .param("dateMax", dateMax)
                .param("priceMin", priceMin)
                .param("priceMax", priceMax)
                .param("garden", String.valueOf(gardenId))
                .with(csrf())
        ).andReturn();
    }

    private MvcResult postEditServiceRequest() throws Exception {
        return mockMvc.perform(post("/serviceRequest/" + serviceRequestId + "/edit")
                .param("title" ,title)
                .param("description", description)
                .param("dateMin", dateMin)
                .param("dateMax", dateMax)
                .param("priceMin", priceMin)
                .param("priceMax", priceMax)
                .param("garden", String.valueOf(gardenId))
                .with(csrf())
        ).andReturn();
    }

    private MvcResult getEditServiceRequest() throws Exception {
        return mockMvc.perform(get("/serviceRequest/" + serviceRequestId + "/edit")
                .param("title" ,title)
                .param("description", description)
                .param("dateMin", dateMin)
                .param("dateMax", dateMax)
                .param("priceMin", priceMin)
                .param("priceMax", priceMax)
                .param("garden", String.valueOf(gardenId))
                .with(csrf())
        ).andReturn();
    }

    @And("I am on the create new service request form")
    public void iAmOnTheCreateNewServiceRequestForm() throws Exception {
        mockMvc.perform(get("/newServiceRequest"))
                .andExpect(status().isOk());
        // Reset parameters to valid defaults
        title = "title";
        description = "description";
        dateMin = LocalDate.now().plusDays(1).format(DATE_FORMATTER);
        dateMax = LocalDate.now().plusDays(2).format(DATE_FORMATTER);
    }

    @When("I enter a earliest date with content {string}")
    public void iEnterAEarliestDateWithContent(String dateMinInput) {
        this.dateMin = dateMinInput;
    }

    @When("I enter a minimum price of {string}")
    public void iEnterAMinimumPriceOf(String minimumPrice) {
        this.priceMin = minimumPrice;
    }

    @When("I enter a maximum price of {string}")
    public void iEnterAMaximumPriceOf(String maximumPrice) {
        this.priceMax = maximumPrice;
    }

    @When("I enter a title of {string}")
    public void iEnterATitleOf(String title) { this.title = title; }

    @When("I enter a description of {string}")
    public void iEnterADescriptionOf(String description) {
        this.description = description;
    }

    @When("I enter a minimum price of {string}, and a maximum price of {string}")
    public void iEnterAMinimumPriceOf(String minimumPrice, String maximumPrice) {
        this.priceMin = minimumPrice;
        this.priceMax = maximumPrice;
    }

    @And("I enter valid values for the remaining fields")
    public void iEnterValidValuesForTheRemainingFields() {
        // If any of the required field variables have not been assigned a value (they're null)
        // then assign a valid value to them
        title = (title != null) ? title : "title";
        description = (description != null) ? description : "description";
        dateMin = (dateMin != null) ? dateMin : LocalDate.now().plusDays(1).format(DATE_FORMATTER);
        dateMax = (dateMax != null) ? dateMax : LocalDate.now().plusDays(100).format(DATE_FORMATTER);
        priceMin = (priceMin != null) ? priceMin : "1";
        priceMax = (priceMax != null) ? priceMax : "99999";
    }

    @And("I click the service request form's submit button")
    public void iClickTheServiceRequestFormSSubmitButton() throws Exception {
        result = postCreateServiceRequest();
    }

    @And("I click the edit service request form's submit button")
    public void iClickTheEditServiceRequestFormSSubmitButton() throws Exception {
        resultEdit = postEditServiceRequest();
    }

    @Then("I remain on the service request form page, and I see the title error {string}")
    public void iRemainOnTheServiceRequestFormPageAndISeeTheTitleError(String errorTitle) {
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("newServiceRequestTemplate", mv.getViewName());
        Assertions.assertEquals(errorTitle, mv.getModel().get("errorTitle"));
    }

    @Then("I remain on the service request form page, and I see the description error {string}")
    public void iRemainOnTheServiceRequestFormPageAndISeeTheDescriptionError(String errorDescription) {
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("newServiceRequestTemplate", mv.getViewName());
        Assertions.assertEquals(errorDescription, mv.getModel().get("errorDescription"));
    }

    @Then("I remain on the service request form page, and I see the earliest date error {string}")
    public void iRemainOnTheServiceRequestFormPageAndISeeTheEarliestDateError(String errorDateMin) {
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("newServiceRequestTemplate", mv.getViewName());
        Assertions.assertEquals(errorDateMin, mv.getModel().get("errorDateMin"));
    }

    @Then("I remain on the service request form page, and I see the latest date error {string}")
    public void iRemainOnTheServiceRequestFormPageAndISeeTheLatestDateError(String errorDateMax) {
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("newServiceRequestTemplate", mv.getViewName());
        Assertions.assertEquals(errorDateMax, mv.getModel().get("errorDateMax"));
    }

    @Then("I remain on the service request form page, and I see the minimum price error {string}")
    public void iRemainOnTheServiceRequestFormPageAndISeeTheMinimumPriceError(String errorPriceMin) {
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("newServiceRequestTemplate", mv.getViewName());
        Assertions.assertEquals(errorPriceMin, mv.getModel().get("errorPriceMin"));
    }

    @Then("I remain on the service request form page, and I see the maximum price error {string}")
    public void iRemainOnTheServiceRequestFormPageAndISeeTheMaximumPriceError(String errorPriceMax) {
        ModelAndView mv = Objects.requireNonNull(result.getModelAndView());
        Assertions.assertEquals("newServiceRequestTemplate", mv.getViewName());
        Assertions.assertEquals(errorPriceMax, mv.getModel().get("errorPriceMax"));
    }

    @When("I enter a earliest date that is {int} days away")
    public void iEnterAEarliestDateThatIsDaysAway(int daysAway) {
        dateMin = LocalDate.now().plusDays(daysAway).format(DATE_FORMATTER);
    }

    @And("I enter a latest date that is {int} days away")
    public void iEnterALatestDateThatIsDaysAway(int daysAway) {
        dateMax = LocalDate.now().plusDays(daysAway).format(DATE_FORMATTER);
    }

    @Then("I am taken to my service requests and the service request is updated")
    public void iAmTakenToMyServiceRequestsAndTheServiceRequestIsUpdated() {
        Assertions.assertEquals(
                302,
                resultEdit.getResponse().getStatus()
        );
    }

    @And("I am on the edit service request form")
    public void iAmOnTheEditServiceRequestForm() throws Exception {
        mockMvc.perform(get("/serviceRequest/" + serviceRequestId + "/edit"))
                .andExpect(status().isOk());
        // Reset parameters to valid defaults
        title = "title";
        description = "description";
        dateMin = LocalDate.now().plusDays(1).format(DATE_FORMATTER);
        dateMax = LocalDate.now().plusDays(2).format(DATE_FORMATTER);
    }

    @When("I enter valid values for the service request")
    public void     iEnterValidValuesForTheServiceRequest() {
        title = (title != null) ? title : "title";
        description = (description != null) ? description : "description";
        dateMin = (dateMin != null) ? dateMin : LocalDate.now().plusDays(1).format(DATE_FORMATTER);
        dateMax = (dateMax != null) ? dateMax : LocalDate.now().plusDays(100).format(DATE_FORMATTER);
        priceMin = (priceMin != null) ? priceMin : "1";
        priceMax = (priceMax != null) ? priceMax : "99999";
    }

    @And("I am on the service request details page")
    public void iAmOnTheServiceRequestDetailsPage() throws Exception {
        mockMvc.perform(get("/serviceRequest/" + serviceRequestId))
                .andExpect(status().isOk())
                .andExpect(view().name("serviceRequestDetailsTemplate"));
    }

    @When("I click the edit button")
    public void iClickTheEditButton() throws Exception {
        result = getEditServiceRequest();
    }

    @Then("I am redirected to the edit service request form")
    public void iAmRedirectedToTheEditServiceRequestForm() {
        Assertions.assertEquals(
                200,
                result.getResponse().getStatus()
        );
    }

    @Then("The edit form is prefilled with the current information")
    public void theEditFormIsPrefilledWithTheCurrentInformation() {
        Assertions.assertEquals(200, result.getResponse().getStatus());

        List<String> attributes = List.of("title", "description", "dateMin", "dateMax", "priceMin", "priceMax", "gardenId");

        Assertions.assertTrue(
                attributes.stream()
                        .allMatch(attr -> Objects.requireNonNull(result.getModelAndView()).getModel().get(attr) != null),
                "One or more attributes are null in the model."
        );
    }
}
