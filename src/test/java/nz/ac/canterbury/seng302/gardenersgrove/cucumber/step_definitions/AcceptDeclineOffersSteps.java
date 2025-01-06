package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Contractor;
import nz.ac.canterbury.seng302.gardenersgrove.entity.JobApplication;
import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ServiceRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.JobApplicationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ServiceRequestService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class AcceptDeclineOffersSteps {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ServiceRequestRepository serviceRequestRepository;
    @Autowired
    ServiceRequestService serviceRequestService;
    @Autowired
    JobApplicationService jobApplicationService;
    @Autowired
    UserService userService;

    private MvcResult result;

    @When("I click the Accept button")
    public void iClickTheAcceptButton() throws Exception {
        JobApplication jobApplication = jobApplicationService.getJobApplicationsByRequestId(13L).getLast();
        result = mockMvc.perform(post("/serviceRequest/13/accept")
                        .with(csrf())
                        .param("applicationId", String.valueOf(jobApplication.getId()))
                )
                .andReturn();
    }

    @When("I click the Decline button")
    public void iClickTheDeclineButton() throws Exception {
        JobApplication jobApplication = jobApplicationService.getJobApplicationsByRequestId(14L).getLast();
        result = mockMvc.perform(post("/serviceRequest/14/decline")
                        .with(csrf())
                        .param("applicationId", String.valueOf(jobApplication.getId()))
                )
                .andReturn();
    }

    @And("there is a job application for service request {long}")
    public void thereIsAJobApplicationForServiceRequest(Long serviceRequestId) {
        ServiceRequest request = serviceRequestService.findById(serviceRequestId).orElseThrow();
        Contractor contractor = (Contractor) userService.getUserByEmail("contractor@gmail.com");
        JobApplication jobApplication = new JobApplication(
                request,
                contractor,
                LocalDate.ofInstant(request.getDateMin(), ZoneId.systemDefault()),
                request.getPriceMin(),
                "PENDING"
        );
        jobApplicationService.saveJobApplication(jobApplication);
    }

    @Then("The application is accepted")
    public void theApplicationIsAccepted() {
        JobApplication jobApplication = jobApplicationService.getJobApplicationsByRequestId(13L).getLast();
        Assertions.assertEquals("ACCEPTED", jobApplication.getStatus());
    }

    @Then("The application is declined")
    public void theApplicationIsDeclined() {
        JobApplication jobApplication = jobApplicationService.getJobApplicationsByRequestId(14L).getLast();
        Assertions.assertEquals("DECLINED", jobApplication.getStatus());
    }

    @And("I am taken back to the details page for service request {long}")
    public void iAmTakenBackToTheDetailsPageForTheServiceRequest(Long serviceRequestId) {
        String redirectedUrl = result.getResponse().getRedirectedUrl();
        Assertions.assertEquals("/serviceRequest/" + serviceRequestId, redirectedUrl);
    }
}
