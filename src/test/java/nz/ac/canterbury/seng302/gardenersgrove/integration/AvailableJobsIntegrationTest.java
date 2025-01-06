package nz.ac.canterbury.seng302.gardenersgrove.integration;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.component.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.JobApplicationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ServiceRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ServiceRequestService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class to ensure the correct service requests are shown.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("emptydb")
class AvailableJobsIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ServiceRequestRepository serviceRequestRepository;
    @Autowired
    UserService userService;
    @Autowired
    LocationService locationService;
    @Autowired
    GardenRepository gardenRepository;
    @Autowired
    ServiceRequestService serviceRequestService;
    @Autowired
    JobApplicationRepository jobApplicationRepository;
    @Autowired
    private CustomAuthenticationProvider customAuthProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;
    SecurityContext securityContext;


    Location location;
    Location closestLocation;

    Location contractorLocation;

    Location furtherLocation;
    User user;
    Authority authority;

    Contractor contractor;
    Garden garden;
    Garden furthestGarden;
    Garden closestGarden;

    ServiceRequest assignedRequest;
    ServiceRequest latestRequest;
    ServiceRequest furthestRequest;
    ServiceRequest earliestRequest;
    ServiceRequest closestRequest;
    ServiceRequest lowestRequest;
    ServiceRequest highestRequest;



    @BeforeEach
    void setup(){
        location = new Location("string", "string", "string", "string", "string", "string");
        location.setLat(55.0);
        location.setLon(55.0);

        closestLocation = new Location("string", "string", "string", "string", "string", "string");
        closestLocation.setLat(45.0);
        closestLocation.setLon(45.0);

        furtherLocation = new Location("string", "string", "string", "string", "string", "string");
        furtherLocation.setLat(100.0);
        furtherLocation.setLon(100.0);

        contractorLocation = new Location("string", "string", "string", "string", "string", "string");
        contractorLocation.setLat(44.0);
        contractorLocation.setLon(44.0);

        location = locationService.saveLocation(location);
        furtherLocation = locationService.saveLocation(furtherLocation);
        closestLocation = locationService.saveLocation(closestLocation);
        contractorLocation = locationService.saveLocation(contractorLocation);

        user = new User("Real", "User",
                "test1@gmail.com", passwordEncoder.encode("Testp4$$"),
                "2000-10-10", null);
        userService.addUser(user);
        userService.enableUser(user.getUserId());

        AbstractUser temp = new User("Real", "User",
                "test@gmail.com", passwordEncoder.encode("Testp4$$"),
                "2000-10-10", null);
        contractor = new Contractor((User) temp, "about me", null, contractorLocation);
        contractor = (Contractor) userService.addUser(contractor);
        userService.enableUser(contractor.getUserId());

        garden = new Garden(
                "Garden 1",
                "1.0",
                user,
                "Valid",
                false,
                null,
                null,
                true,
                null
        );

        furthestGarden = new Garden(
                "Garden 1",
                "1.0",
                user,
                "Valid",
                false,
                null,
                null,
                true,
                null
        );

        closestGarden = new Garden(
                "Garden 1",
                "1.0",
                user,
                "Valid",
                false,
                null,
                null,
                true,
                null
        );

        furthestGarden.setLocation(furtherLocation);
        gardenRepository.save(furthestGarden);

        closestGarden.setLocation(closestLocation);
        gardenRepository.save(closestGarden);

        garden.setLocation(location);
        gardenRepository.save(garden);

        latestRequest = new ServiceRequest(
            "latestRequest",
            "description",
            Instant.now().plus(7, ChronoUnit.DAYS),
            Instant.now().plus(14, ChronoUnit.DAYS),
            "/images/PlantPlaceholder.jpg",
            50.0,
            80.0,
            garden
        );

        earliestRequest = new ServiceRequest(
            "earliestRequest",
            "description",
            Instant.now(),
            Instant.now().plus(7, ChronoUnit.DAYS),
            "/images/PlantPlaceholder.jpg",
            50.0,
            80.0,
            garden
        );

        closestRequest = new ServiceRequest(
            "closestRequest",
            "description",
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(7, ChronoUnit.DAYS),
            "/images/PlantPlaceholder.jpg",
            50.0,
            80.0,
            closestGarden
        );

        furthestRequest = new ServiceRequest(
            "furthestRequest",
            "description",
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(7, ChronoUnit.DAYS),
            "/images/PlantPlaceholder.jpg",
            50.0,
            80.0,
            furthestGarden
        );

        lowestRequest = new ServiceRequest(
            "lowestRequest",
            "description",
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(7, ChronoUnit.DAYS),
            "/images/PlantPlaceholder.jpg",
            1.0,
            2.0,
            garden
        );

        highestRequest = new ServiceRequest(
            "highestRequest",
            "description",
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(7, ChronoUnit.DAYS),
            "/images/PlantPlaceholder.jpg",
            100.0,
            200.0,
            garden
        );

        assignedRequest = new ServiceRequest(
            "assignedRequest",
            "description",
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(7, ChronoUnit.DAYS),
            "/images/PlantPlaceholder.jpg",
            50.0,
            80.0,
            garden
        );
        assignedRequest.setContractor(contractor);

        earliestRequest = serviceRequestRepository.save(earliestRequest);
        assignedRequest = serviceRequestRepository.save(assignedRequest);
        furthestRequest = serviceRequestRepository.save(furthestRequest);
        closestRequest = serviceRequestRepository.save(closestRequest);
        lowestRequest = serviceRequestRepository.save(lowestRequest);
        highestRequest =serviceRequestRepository.save(highestRequest);
        latestRequest = serviceRequestRepository.save(latestRequest);

        Authentication authentication = customAuthProvider.authenticate(
        new UsernamePasswordAuthenticationToken("test@gmail.com", "Testp4$$"));
        SecurityContextHolder.getContext().setAuthentication(authentication);

    }

    @Test
    void showsUnassignedJobs(){
        List<ServiceRequest> output = serviceRequestService.getAvailableJobs("releaseDateTime", null, null, null, null);

        Assertions.assertTrue(output.contains(latestRequest));
        Assertions.assertTrue(output.contains(earliestRequest));
        Assertions.assertFalse(output.contains(assignedRequest));
    }

    @Test
    void getAvailableJobsPage_NoPrompt_OrderByLatestRelease() throws Exception {
        MvcResult result = mockMvc.perform(get("/availableJobs")
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        Assertions.assertNotNull(mv.getModel().get("jobs"));
        List<ServiceRequest> jobs = (List<ServiceRequest>) mv.getModel().get("jobs");
        Assertions.assertEquals(latestRequest, jobs.getFirst());
    }

    @Test
    void getAvailableJobsPage_InvalidPrompt_OrderByLatestRelease() throws Exception {
        MvcResult result = mockMvc.perform(get("/availableJobs")
                        .param("orderPrompt", "Not a prompt")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        Assertions.assertNotNull(mv.getModel().get("jobs"));
        List<ServiceRequest> jobs = (List<ServiceRequest>) mv.getModel().get("jobs");
        Assertions.assertEquals(latestRequest, jobs.getFirst());
    }
    @Test
    void getAvailableJobsPage_LatestReleasePrompt_OrderByLatestRelease() throws Exception {
        MvcResult result = mockMvc.perform(get("/availableJobs")
                .param("orderPrompt", "latestRelease")
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        Assertions.assertNotNull(mv.getModel().get("jobs"));
        List<ServiceRequest> jobs = (List<ServiceRequest>) mv.getModel().get("jobs");
        Assertions.assertEquals(latestRequest, jobs.getFirst());
    }

    @Test
    void getAvailableJobsPage_EarliestReleasePrompt_OrderByEarliestRelease() throws Exception {
        MvcResult result = mockMvc.perform(get("/availableJobs")
                        .param("orderPrompt", "earliestRelease")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        Assertions.assertNotNull(mv.getModel().get("jobs"));
        List<ServiceRequest> jobs = (List<ServiceRequest>) mv.getModel().get("jobs");
        Assertions.assertEquals(earliestRequest, jobs.getFirst());
    }

    @Test
    void getAvailableJobsPage_EarliestAvailablePrompt_OrderByEarliestAvailable() throws Exception {
        MvcResult result = mockMvc.perform(get("/availableJobs")
                        .param("orderPrompt", "earliestAvailable")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        Assertions.assertNotNull(mv.getModel().get("jobs"));
        List<ServiceRequest> jobs = (List<ServiceRequest>) mv.getModel().get("jobs");
        Assertions.assertEquals(earliestRequest, jobs.getFirst());
    }

    @Test
    void getAvailableJobsPage_LatestAvailablePrompt_OrderByLatestAvailable() throws Exception {
        MvcResult result = mockMvc.perform(get("/availableJobs")
                        .param("orderPrompt", "latestAvailable")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        Assertions.assertNotNull(mv.getModel().get("jobs"));
        List<ServiceRequest> jobs = (List<ServiceRequest>) mv.getModel().get("jobs");
        Assertions.assertEquals(latestRequest, jobs.getFirst());
    }

    @Test
    void getAvailableJobsPage_LowestBudgetPrompt_OrderByLowestBudget() throws Exception {
        MvcResult result = mockMvc.perform(get("/availableJobs")
                        .param("orderPrompt", "lowestBudget")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        Assertions.assertNotNull(mv.getModel().get("jobs"));
        List<ServiceRequest> jobs = (List<ServiceRequest>) mv.getModel().get("jobs");
        Assertions.assertEquals(lowestRequest, jobs.getFirst());
    }

    @Test
    void getAvailableJobsPage_HighestBudgetPrompt_OrderByHighestBudget() throws Exception {
        MvcResult result = mockMvc.perform(get("/availableJobs")
                        .param("orderPrompt", "highestBudget")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        Assertions.assertNotNull(mv.getModel().get("jobs"));
        List<ServiceRequest> jobs = (List<ServiceRequest>) mv.getModel().get("jobs");
        Assertions.assertEquals(highestRequest, jobs.getFirst());
    }

    @Test
    void getAvailableJobsPage_ClosestDistancePrompt_OrderByClosestDistance() throws Exception {
        MvcResult result = mockMvc.perform(get("/availableJobs")
                        .param("orderPrompt", "closestDistance")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        Assertions.assertNotNull(mv.getModel().get("jobs"));
        List<ServiceRequest> jobs = (List<ServiceRequest>) mv.getModel().get("jobs");
        Assertions.assertEquals(closestRequest, jobs.getFirst());
    }

    @Test
    void getAvailableJobsPage_FurthestDistancePrompt_OrderByFurtherDistance() throws Exception {
        MvcResult result = mockMvc.perform(get("/availableJobs")
                        .param("orderPrompt", "furthestDistance")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        Assertions.assertNotNull(mv.getModel().get("jobs"));
        List<ServiceRequest> jobs = (List<ServiceRequest>) mv.getModel().get("jobs");
        Assertions.assertEquals(furthestRequest, jobs.getFirst());
    }

    @Test
    void getAvailableJobs_FilterByMinimumPrice_ReturnsCorrectResults(){
        List<ServiceRequest> output = serviceRequestService.getAvailableJobs("latestRelease", null, null, 81.0, null);

        Assertions.assertTrue(output.contains(highestRequest));
        Assertions.assertFalse(output.contains(latestRequest));
        Assertions.assertFalse(output.contains(earliestRequest));
        Assertions.assertFalse(output.contains(closestRequest));
        Assertions.assertFalse(output.contains(furthestRequest));
        Assertions.assertFalse(output.contains(lowestRequest));
    }
    @Test
    void getAvailableJobs_FilterByMaximumPrice_ReturnsCorrectResults(){
        List<ServiceRequest> output = serviceRequestService.getAvailableJobs("latestRelease", null, null, null, 55.0);

        Assertions.assertFalse(output.contains(highestRequest));
        Assertions.assertTrue(output.contains(latestRequest));
        Assertions.assertTrue(output.contains(earliestRequest));
        Assertions.assertTrue(output.contains(closestRequest));
        Assertions.assertTrue(output.contains(furthestRequest));
        Assertions.assertTrue(output.contains(lowestRequest));
    }
    @Test
    void getAvailableJobs_FilterByMinimumDate_ReturnsCorrectResults(){
        List<ServiceRequest> output = serviceRequestService.getAvailableJobs("latestRelease", LocalDate.now().plus(8, ChronoUnit.DAYS), null, null, null);

        Assertions.assertFalse(output.contains(highestRequest));
        Assertions.assertTrue(output.contains(latestRequest));
        Assertions.assertFalse(output.contains(earliestRequest));
        Assertions.assertFalse(output.contains(closestRequest));
        Assertions.assertFalse(output.contains(furthestRequest));
        Assertions.assertFalse(output.contains(lowestRequest));
    }
    @Test
    void getAvailableJobs_FilterByMaximumDate_ReturnsCorrectResults(){
        List<ServiceRequest> output = serviceRequestService.getAvailableJobs("latestRelease", null, LocalDate.now().plus(6, ChronoUnit.DAYS), null, null);

        Assertions.assertTrue(output.contains(highestRequest));
        Assertions.assertFalse(output.contains(latestRequest));
        Assertions.assertTrue(output.contains(earliestRequest));
        Assertions.assertTrue(output.contains(closestRequest));
        Assertions.assertTrue(output.contains(furthestRequest));
        Assertions.assertTrue(output.contains(lowestRequest));
    }

    @Test
    void getAvailableJobsPage_LastPage() throws Exception {
        int totalJobs = serviceRequestService.getAvailableJobs("latestRelease", null, null, null, null).size();
        int lastPage = (totalJobs / 10);

        MvcResult result = mockMvc.perform(get("/availableJobs")
                        .param("page", String.valueOf(lastPage))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        Assertions.assertNotNull(mv.getModel().get("jobs"));
        List<ServiceRequest> jobs = (List<ServiceRequest>) mv.getModel().get("jobs");
        Assertions.assertTrue(jobs.size() <= 10);
    }

    @Test
    void getAvailableJobsPage_PageLessThanZero_RedirectsToFirstPage() throws Exception {
        MvcResult result = mockMvc.perform(get("/availableJobs")
                        .param("page", "-1")
                        .param("size", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectedUrl = result.getResponse().getRedirectedUrl();
        Assertions.assertEquals("/availableJobs?page=0&size=10&orderPrompt=latestRelease", redirectedUrl);
    }

    @Test
    void getAvailableJobsPage_PageGreaterThanTotalPages_RedirectsToFirstPage() throws Exception {
        List<ServiceRequest> jobs = serviceRequestService.getAvailableJobs("latestRelease", null, null, null, null);
        int totalPages = (int) Math.ceil((double) jobs.size() / 10);

        MvcResult result = mockMvc.perform(get("/availableJobs")
                        .param("page", String.valueOf(totalPages + 1))
                        .param("size", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectedUrl = result.getResponse().getRedirectedUrl();
        Assertions.assertEquals("/availableJobs?page=0&size=10&orderPrompt=latestRelease", redirectedUrl);
    }


}
