package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ServiceRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class BrowseAvailableJobsSteps {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    MessageSource messageSource;
    @Autowired
    private UserService userService;
    @Autowired
    ServiceRequestRepository serviceRequestRepository;
    @Autowired
    LocationService locationService;
    @Autowired
    GardenRepository gardenRepository;

    private String requestUrl;

    Location location;

    Location location2;

    AbstractUser user;
    AbstractUser temp;

    Contractor contractor;
    Garden garden;
    ServiceRequest serviceRequest1;
    ServiceRequest serviceRequest2;
    ServiceRequest serviceRequest3;

    List<ServiceRequest> availableJobs;

    String minBudget = null;
    String maxBudget = null;
    String dateMin = null;
    String dateMax = null;
    String maxDistance = null;

    MvcResult mvcResult;

    @Then("I am redirected to the homepage")
    public void iAmRedirectedToTheHomepage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(requestUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/homepage"))
                .andReturn().getResponse();
    }

    @When("I attempt to open the Available Jobs page")
    public void iAttemptToOpenThePage() {
        requestUrl = "/availableJobs";
    }

    @Then("I am shown the Available Jobs page")
    public void iAmShownThePage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(requestUrl))
                .andExpect(status().isOk())
                .andExpect(view().name("availableJobsTemplate"))
                .andReturn().getResponse();
    }

    @Then("I should see {int} jobs listed on the page")
    public void iShouldSeeJobsListedOnThePage(int expectedJobCount) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(requestUrl))
                .andExpect(status().isOk())
                .andExpect(view().name("availableJobsTemplate"))
                .andReturn();

        List<ServiceRequest> jobs = (List<ServiceRequest>) result.getModelAndView().getModel().get("jobs");
        assertEquals(expectedJobCount, jobs.size());
    }

    @And("pagination should be available")
    public void paginationShouldBeAvailable() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(requestUrl))
                .andExpect(status().isOk())
                .andReturn();

        int totalPages = (int) result.getModelAndView().getModel().get("totalPages");
        assertTrue(totalPages > 1, "Pagination should have more than one page if there are more than 10 jobs.");
    }
    @When("I have navigated to the Available Jobs page")
    public void iHaveNavigatedToTheAvailableJobsPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/availableJobs"))
                .andExpect(status().isOk())
                .andExpect(view().name("availableJobsTemplate"))
                .andReturn().getResponse();
    }

    @And("I have entered {string} for the minimum budget")
    public void iHaveEnteredAValidValueForTheMinimumBudget(String minBudgetInput) {
        minBudget = minBudgetInput;
    }

    @When("I apply the filters")
    public void iApplyTheFilters() throws Exception{
         mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/availableJobs")
                        .param("priceMin", minBudget)
                        .param("priceMax", maxBudget)
                        .param("dateMin", dateMin)
                        .param("dateMax", dateMax)
                        .param("maxDistance", maxDistance))
                .andExpect(status().isOk())
                .andExpect(view().name("availableJobsTemplate"))
                .andReturn();
    }

    @Then("I am only shown jobs that have a maximum budget that is greater than {string}")
    public void iAmOnlyShownJobsThatHaveAMaximumBudgetThatIsGreaterThanTheMinimumBudgetIFilteredBy(String minBudgetInput) {
        List<ServiceRequest> jobs = (List<ServiceRequest>) mvcResult.getModelAndView().getModel().get("jobs");
        assertTrue(jobs.stream().allMatch(job -> job.getPriceMax() >= Double.parseDouble(minBudgetInput)));
    }

    @And("I have entered {string} for the maximum budget")
    public void iHaveEnteredForTheMaximumBudget(String arg0) {
        maxBudget = arg0;
    }

    @Then("I am only shown jobs that have a minimum budget that is less than {string}")
    public void iAmOnlyShownJobsThatHaveAMinimumBudgetThatIsLessThan(String arg0) {
        List<ServiceRequest> jobs = (List<ServiceRequest>) mvcResult.getModelAndView().getModel().get("jobs");
        assertTrue(jobs.stream().allMatch(job -> job.getPriceMin() <= Double.parseDouble(arg0)));
    }

    @And("I have entered {string} for the minimum date")
    public void iHaveEnteredForTheMinimumDate(String arg0) {
        dateMin = arg0;
    }

    @Then("I am only shown jobs that have a latest date that is after {string}")
    public void iAmOnlyShownJobsThatHaveALatestDateThatIsAfter(String arg0) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime dateTime = LocalDate.parse(arg0, formatter).atStartOfDay();
        Instant instant = dateTime.toInstant(ZonedDateTime.now(ZoneId.systemDefault()).getOffset());
        List<ServiceRequest> jobs = (List<ServiceRequest>) mvcResult.getModelAndView().getModel().get("jobs");
        assertTrue(jobs.stream().allMatch(job -> job.getDateMax().isAfter(instant)));
    }

    @And("I have entered {string} for the maximum date")
    public void iHaveEnteredForTheMaximumDate(String arg0) {
        dateMax = arg0;
    }

    @Then("I am only shown jobs that have a earliest date that is before {string}")
    public void iAmOnlyShownJobsThatHaveAEarliestDateThatIsBefore(String arg0) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime dateTime = LocalDate.parse(arg0, formatter).atStartOfDay();
        Instant instant = dateTime.toInstant(ZonedDateTime.now(ZoneId.systemDefault()).getOffset());
        List<ServiceRequest> jobs = (List<ServiceRequest>) mvcResult.getModelAndView().getModel().get("jobs");
        assertTrue(jobs.stream().allMatch(job -> job.getDateMin().isBefore(instant)));
    }

    @And("I have entered {string} for the maximum distance")
    public void iHaveEnteredForTheMaximumDistance(String arg0) {
        maxDistance = arg0;
    }

    @Then("I am only shown jobs that have a distance that is less than {string}")
    public void iAmOnlyShownJobsThatHaveADistanceThatIsLessThan(String arg0) {
        List<ServiceRequest> jobs = (List<ServiceRequest>) mvcResult.getModelAndView().getModel().get("jobs");
        HashMap<Long, Double> jobDistance = (HashMap<Long, Double> ) mvcResult.getModelAndView().getModel().get("jobDistances");
        assertTrue(jobs.stream().allMatch(job -> jobDistance.get(job.getId()) < Double.parseDouble(arg0)));
    }

    @Then("I am shown all jobs")
    public void iAmShownAllJobs() throws Exception {
        int totalJobs = ((List<ServiceRequest>) mockMvc.perform(MockMvcRequestBuilders.get("/availableJobs"))
                .andExpect(status().isOk())
                .andExpect(view().name("availableJobsTemplate"))
                .andReturn().getModelAndView().getModel().get("jobs")).size();
        assertEquals(totalJobs, ((List<ServiceRequest>) mvcResult.getModelAndView().getModel().get("jobs")).size());
    }

    @And("I see at least one error message")
    public void iSeeAtLeastOneErrorMessage() {
        assertTrue(mvcResult.getModelAndView().getModel().keySet().stream().anyMatch(key -> key.contains("error")));
    }
    @And("I see no error message")
    public void iSeeNoErrorMessage() {
        assertFalse(mvcResult.getModelAndView().getModel().keySet().stream().anyMatch(key -> key.contains("error")));
    }
}
