package nz.ac.canterbury.seng302.gardenersgrove.integration;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.component.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ServiceRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MyServiceRequestIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private ServiceRequestService serviceRequestService;
    @Autowired
    private CustomAuthenticationProvider customAuthProvider;
    @Autowired
    private GardenRepository gardenRepository;
    @Autowired
    private LocationService locationService;
    @Autowired
    private ServiceRequestRepository serviceRequestRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JobApplicationService jobApplicationService;
    @MockBean
    private MailService mailService;

    SecurityContext securityContext;
    Location contractorLocation;

    Contractor contractor;

    ServiceRequest serviceRequest;

    User owner;



    @BeforeEach
    void setup() {
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

        serviceRequest = new ServiceRequest(
                "assignedRequest",
                "description",
                Instant.now().plus(1, ChronoUnit.DAYS),
                Instant.now().plus(7, ChronoUnit.DAYS),
                "/images/PlantPlaceholder.jpg",
                50.0,
                80.0,
                garden
        );

        serviceRequest = serviceRequestRepository.save(serviceRequest);

        Authentication authentication = customAuthProvider.authenticate(
                new UsernamePasswordAuthenticationToken("test@gmail.com", "Testp4$$"));
        SecurityContextHolder.getContext().setAuthentication(authentication);


    }

    @Test
    void getServiceRequestDetails_AsContractorNotYetApplied_CanApply() throws Exception {

        mockMvc.perform(get("/serviceRequest/"+serviceRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("serviceRequestDetailsTemplate"))
                .andExpect(model().attributeExists("serviceRequest"))
                .andExpect(model().attribute("serviceRequest", serviceRequest))
                .andExpect(model().attribute("isAssigned", false))
                .andExpect(model().attribute("appliedContractorsIds", new ArrayList<>()));
    }

    @Test
    void getServiceRequestDetails_AsContractorApplied_CannotApply() throws Exception {

        JobApplication jobApplication = new JobApplication(serviceRequest, contractor, LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 55.0, "PENDING");
        jobApplicationService.saveJobApplication(jobApplication);
        serviceRequestService.saveServiceRequest(serviceRequest);

        List<Long> expectedIds = new ArrayList<>();
        expectedIds.add(contractor.getUserId());

        mockMvc.perform(get("/serviceRequest/"+serviceRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("serviceRequestDetailsTemplate"))
                .andExpect(model().attributeExists("serviceRequest"))
                .andExpect(model().attribute("serviceRequest", serviceRequest))
                .andExpect(model().attribute("isAssigned", false))
                .andExpect(model().attribute("appliedContractorsIds", expectedIds));
    }

    @Test
    void applyForJob_AlreadyApplied_Redirects() throws Exception {
        JobApplication jobApplication = new JobApplication(serviceRequest, contractor, LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault()), 55.0, "PENDING");
        jobApplicationService.saveJobApplication(jobApplication);
        serviceRequestService.saveServiceRequest(serviceRequest);

        mockMvc.perform(post("/serviceRequest/"+serviceRequest.getId())
                .with(csrf())
                .param("date", String.valueOf(LocalDate.ofInstant(serviceRequest.getDateMax(), ZoneId.systemDefault())))
                .param("price", String.valueOf(55.0))
                .param("status", "PENDING"))
            .andExpect(status().is3xxRedirection());

    }


}
