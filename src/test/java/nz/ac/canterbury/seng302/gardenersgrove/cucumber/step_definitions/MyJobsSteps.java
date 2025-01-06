package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Contractor;
import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ServiceRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class MyJobsSteps {
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

    String requestUrl;

    Contractor contractor;

    @When("I open the My Jobs page")
    public void iOpenTheMyJobsPage() { requestUrl = "/myJobs";
    }
    @Then("I am shown the My Jobs page")
    public void iAmShownTheMyJobsPage() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get(requestUrl))
                .andExpect(status().isOk())
                .andExpect(view().name("assignedJobsTemplate"))
                .andReturn().getResponse();
    }

    @Then("I am redirected to the Homepage")
    public void iAmRedirectedToTheHomepage() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get(requestUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/homepage"))
                .andReturn().getResponse();
    }
    @When("I am on the {string} tab")
    public void iAmOnTheTab(String tabName) throws Exception{
        contractor = (Contractor) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        result = mockMvc.perform(MockMvcRequestBuilders.get(requestUrl).param("tab", tabName))
                .andExpect(status().isOk())
                .andExpect(view().name("assignedJobsTemplate"))
                .andReturn();
    }

    @Then("I see all of the incomplete jobs I am assigned to")
    public void iSeeAllOfTheIncompleteJobsIAmAssignedTo() {
        List<ServiceRequest> jobs = (List<ServiceRequest>) result.getModelAndView().getModel().get("jobs");
        List<ServiceRequest> allJobs = serviceRequestRepository.findAll();
        assertTrue(jobs.stream().allMatch(job -> job.getContractor().getUserId().equals(contractor.getUserId())));
        assertFalse(jobs.size() != 10 && allJobs.stream().anyMatch(
                job -> job.getContractor() != null
                        && job.getContractor().getUserId().equals(contractor.getUserId())
                        && jobs.stream().noneMatch(job2 -> job2.getId().equals(job.getId()))
                        && !job.isCompleted()
                ));
        // checking that there are no jobs assigned to this contractor that didn't show up
    }
    @Then("I see all of the completed jobs I am assigned to")
    public void iSeeAllOfTheCompletedJobsIAmAssignedTo() {
        List<ServiceRequest> jobs = (List<ServiceRequest>) result.getModelAndView().getModel().get("jobs");
        List<ServiceRequest> allJobs = serviceRequestRepository.findAll();
        assertTrue(jobs.stream().allMatch(job -> job.getContractor().getUserId().equals(contractor.getUserId())));
        assertFalse(jobs.size() != 10 && allJobs.stream().anyMatch(
                job -> job.getContractor() != null
                        && job.getContractor().getUserId().equals(contractor.getUserId())
                        && jobs.stream().noneMatch(job2 -> job2.getId().equals(job.getId()))
                        && job.isCompleted()
        ));
        // checking that there are no jobs assigned to this contractor that didn't show up
    }

    @And("The jobs are sorted by most to least recent")
    public void theJobsAreSortedByMostToLeastRecent() {
        List<ServiceRequest> jobs = (List<ServiceRequest>) result.getModelAndView().getModel().get("jobs");
        Instant prevDate = Instant.now().plus(2000, ChronoUnit.DAYS);
        for(ServiceRequest job : jobs.reversed()){
            assertTrue(job.getAgreedDate().isBefore(prevDate));
        }
    }


}
