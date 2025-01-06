package nz.ac.canterbury.seng302.gardenersgrove.integration;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.component.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AssignedJobsControllerTest {
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
    private CustomAuthenticationProvider customAuthProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;
    SecurityContext securityContext;
    Location contractorLocation;

    Contractor contractor;

    ServiceRequest assignedRequest;

    ServiceRequest assignedRequestPast;
    ServiceRequest notAssignedRequest;


    AbstractUser owner;

    @BeforeEach
    void setup(){

        // set up contractor
        contractorLocation = new Location("string", "string", "string", "string", "string", "string");
        contractorLocation.setLat(44.0);
        contractorLocation.setLon(44.0);
        contractorLocation = locationService.saveLocation(contractorLocation);
        AbstractUser temp = new User("Real", "User",
                "test@gmail.com", passwordEncoder.encode("Testp4$$"),
                "2000-10-10", null);
        contractor = new Contractor((User) temp, "about me", null, contractorLocation);
        contractor = (Contractor) userService.addUser(contractor);
        userService.enableUser(contractor.getUserId());

        // set up owner user
        owner = new User("Owner", "User",
                "test1@gmail.com", passwordEncoder.encode("Testp4$$"),
                "2000-10-10", null);
        owner = (User) userService.addUser(owner);

        // set up service requests
        Garden garden = new Garden(
                "Garden 1",
                "1.0",
                owner,
                "Valid",
                false,
                null,
                null,
                true,
                null
        );
        garden.setLocation(contractorLocation);
        garden = gardenRepository.save(garden);

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
        assignedRequest.setContractor((Contractor) contractor);
        assignedRequest = serviceRequestRepository.save(assignedRequest);
        assignedRequestPast = new ServiceRequest(
                "assignedRequest",
                "description",
                Instant.now().minus(7, ChronoUnit.DAYS),
                Instant.now().minus(5, ChronoUnit.DAYS),
                "/images/PlantPlaceholder.jpg",
                50.0,
                80.0,
                garden
        );
        assignedRequestPast.setContractor((Contractor) contractor);
        assignedRequestPast.setCompleted(true);
        assignedRequestPast = serviceRequestRepository.save(assignedRequestPast);

        notAssignedRequest = new ServiceRequest(
                "assignedRequest",
                "description",
                Instant.now().plus(1, ChronoUnit.DAYS),
                Instant.now().plus(7, ChronoUnit.DAYS),
                "/images/PlantPlaceholder.jpg",
                50.0,
                80.0,
                garden
        );
        notAssignedRequest = serviceRequestRepository.save(notAssignedRequest);

        Authentication authentication = customAuthProvider.authenticate(
        new UsernamePasswordAuthenticationToken("test@gmail.com", "Testp4$$"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getCurrentRequests_ValidRequest_ReturnsCorrectRequests() throws Exception{
        MvcResult result = mockMvc.perform(get("/myJobs")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        Assertions.assertNotNull(mv.getModel().get("jobs"));
        List<ServiceRequest> jobs = (List<ServiceRequest>) mv.getModel().get("jobs");
        Assertions.assertTrue(jobs.contains(assignedRequest));
        Assertions.assertFalse(jobs.contains(assignedRequestPast));
        Assertions.assertFalse(jobs.contains(notAssignedRequest));
    }
    @Test
    void getPastRequests_ValidRequest_ReturnsCorrectRequests() throws Exception{
        MvcResult result = mockMvc.perform(get("/myJobs")
                        .with(csrf()).param("tab", "history"))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        Assertions.assertNotNull(mv.getModel().get("jobs"));
        List<ServiceRequest> jobs = (List<ServiceRequest>) mv.getModel().get("jobs");
        Assertions.assertFalse(jobs.contains(assignedRequest));
        Assertions.assertTrue(jobs.contains(assignedRequestPast));
        Assertions.assertFalse(jobs.contains(notAssignedRequest));
    }
    @Test
    void getPastRequests_NotContractor_RedirectsToHomepage() throws Exception{
        Authentication authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(owner);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/myJobs")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();
    }
    @Test
    void getPastRequests_NotAuthenticated_RedirectsToLogin() throws Exception{
        securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/myJobs")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login")) // Cheers ChatGPT
                .andReturn();
    }
}
