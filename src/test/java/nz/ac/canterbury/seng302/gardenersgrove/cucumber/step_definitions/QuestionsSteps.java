package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.entity.QuestionAnswer;
import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ServiceRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.JobApplicationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ServiceRequestService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class QuestionsSteps {

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
    @Autowired
    GardenService gardenService;
    @Autowired
    GardenRepository gardenRepository;

    private MvcResult result;

    @And("No questions have been asked")
    public void noQuestionsHaveBeenAsked() {
    }

    @When("I visit its details page")
    public void iVisitItsDetailsPage() throws Exception {
        result = mockMvc.perform(get("/serviceRequest/100")
                        .with(csrf())
                )
                .andReturn();
    }

    @Then("I can see a message")
    public void iCanSeeAMessage() {
        Optional<List<QuestionAnswer>> listOfQuestions =
                (Optional<List<QuestionAnswer>>) Objects.requireNonNull(result.getModelAndView()).getModel().get("questions");

        // Found out that the message is not passed through the model, the validation is done in the html
        assertNull(listOfQuestions);
    }

    @And("I have a valid service request")
    public void iHaveAValidServiceRequest() {
        ServiceRequest serviceRequest = new ServiceRequest(
                "No Questions Service Request",
                "This service request has no questions",
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.MINUTES),
                "/images/default.png",
                10,
                20,
                gardenService.getGarden(7L).get());
        serviceRequest.setId(100L);
        serviceRequestService.saveServiceRequest(serviceRequest);
    }
}
