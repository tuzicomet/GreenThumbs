package nz.ac.canterbury.seng302.gardenersgrove.integration;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.component.CustomAuthenticationProvider;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.JobApplicationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LocationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ServiceRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Test class for testing functionality for Applying for jobs/service requests
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Transactional
class JobApplicationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    @Autowired
    private JobApplicationService jobApplicationService;
    @Autowired
    private ServiceRequestRepository serviceRequestRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private CustomAuthenticationProvider customAuthProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private ServiceRequest serviceRequest1;

    private ServiceRequest ownedServiceRequest;
    private Contractor contractorUser;
    private User user;
    private Garden garden;
    private Garden ownedGarden;

    @BeforeEach
    void setup() {

        // Initialize test data
        user = new User(
                "Mock",
                "User",
                "test@gmail.com",
                passwordEncoder.encode("Testp4$$"),
                "1990-01-01",
                null
        );
        user = userRepository.save(user);
        userService.enableUser(user.getUserId());

        // Initialize test data
        AbstractUser otherUser = new User(
                "Mock",
                "User",
                "test2@gmail.com",
                passwordEncoder.encode("Testp4$$"),
                "1990-01-01",
                null
        );
        userRepository.save(otherUser);

        garden = new Garden(
                "Garden 1",
                "1.0",
                otherUser,
                "Valid",
                true,
                null,
                null,
                true,
                null
        );
        ownedGarden = new Garden(
                "Garden 1",
                "1.0",
                user,
                "Valid",
                true,
                null,
                null,
                true,
                null
        );

        Location location = new Location(
                "Engineering Road, Riccarton, Christchurch 8041, New Zealand",
                "New Zealand",
                "Christchurch City",
                "Riccarton",
                "Engineering Road",
                "8041"
        );

        location.setLon(1.0);
        location.setLat(2.0);
        locationRepository.save(location);
        garden.setGardenId(1L);
        garden.setLocation(location);
        ownedGarden.setGardenId(1L);
        ownedGarden.setLocation(location);

        serviceRequest1 = new ServiceRequest(
                "Test service request", "Test description",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-12-31T23:59:59Z"),
                "/images/default.jpg",
                10.00,
                200.00,
                garden
                );
        serviceRequest1.setId(1L);
        serviceRequestRepository.save(serviceRequest1);

        ownedServiceRequest = new ServiceRequest(
                "Test service request", "Test description",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-12-31T23:59:59Z"),
                "/images/default.jpg",
                10.00,
                200.00,
                ownedGarden
        );
        ownedServiceRequest.setId(2L);
        serviceRequestRepository.save(ownedServiceRequest);

        String aboutMe = "I am a skilled contractor.";
        List<String> workPictures = new ArrayList<>();
        workPictures.add("work1.jpg");
        workPictures.add("work2.jpg");

        userService.convertUserToContractor(user, aboutMe, workPictures, location);
        contractorUser = (Contractor) userRepository.findByEmail(user.getEmail());

        Authentication authentication = customAuthProvider.authenticate(
                new UsernamePasswordAuthenticationToken("test@gmail.com", "Testp4$$")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void viewServiceRequestDetailsPage_ServiceRequestExists_StatusIsOk() throws Exception {
        mockMvc.perform(get("/serviceRequest/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void submitJobApplication_ServiceRequestIdIsInvalid_JobApplicationIsSavedWithCorrectDetails() throws Exception {
        // try posting to the service request with id 99999999, which shouldn't exist
        mockMvc.perform(post("/serviceRequest/99999999")
                        .with(csrf())
                        .param("date", "01/02/2024")
                        .param("price", "100.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/")); // should redirect to homepage

        // Try to find the saved job application in the database by getting the user's most recent application
        JobApplication savedJobApplication = jobApplicationService.getLatestJobApplicationByContractorId(user.getUserId());

        // Check that the saved job application does not exist
        assertNull(savedJobApplication);
    }

    @Test
    void submitJobApplication_UserIsNotAContractor_JobApplicationIsSavedWithCorrectDetails() throws Exception {
        User nonContractorUser = new User(
                "Mock",
                "User",
                "noncontractor@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        userRepository.save(nonContractorUser);
        // do not convert this user to a contractor

        // Override the authentication and securityContext mocks so that this user is used instead
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(nonContractorUser);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // perform an otherwise valid job application submission
        mockMvc.perform(post("/serviceRequest/1")
                        .with(csrf())
                        .param("date", "01/02/2024")
                        .param("price", "100.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/")); // should redirect to homepage

        // Try to find the saved job application in the database by getting the user's most recent application
        JobApplication savedJobApplication = jobApplicationService.getLatestJobApplicationByContractorId(user.getUserId());

        // Check that the saved job application does not exist
        assertNull(savedJobApplication);
    }

    @Test
    void submitJobApplication_ApplicationIsValid_JobApplicationIsSavedWithCorrectDetails() throws Exception {
        mockMvc.perform(post("/serviceRequest/1")
                        .with(csrf())
                        .param("date", "01/02/2024")
                        .param("price", "100.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/serviceRequest/1"));

        // Try to find the saved job application in the database by getting the user's most recent application
        JobApplication savedJobApplication = jobApplicationService.getLatestJobApplicationByContractorId(user.getUserId());

        // Check that the saved job application exists
        assertNotNull(savedJobApplication);

        // Check that the saved job application contains all the expected values
        assertEquals(serviceRequest1.getId(), savedJobApplication.getJob().getId());
        assertEquals(contractorUser, savedJobApplication.getContractor());
        assertEquals(LocalDate.parse("01/02/2024", DateTimeFormatter.ofPattern("dd/MM/yyyy")), savedJobApplication.getDate());
        assertEquals(100.00, savedJobApplication.getPrice());
    }

    @ParameterizedTest
    @CsvSource({
            "''",
            "' '",
            "not a number",
            "'a.46'",
            "'5.'",
            "'5,'",
            "'5.9898'",
            "'200.1'",
            "'12.10.10'",
            "'12.111'"
    })
    void postServiceRequestApplication_InvalidPrice_valuesPersistedNoApplicationMade(String invalidPrice) throws Exception {
        // try posting to the service with id invalid price
        MvcResult result = mockMvc.perform(post("/serviceRequest/1")
                        .with(csrf())
                        .param("date", "01/02/2024")
                        .param("price", invalidPrice))
                .andExpect(status().isOk())
                .andReturn();

        // Try to find the saved job application in the database by getting the user's most recent application
        JobApplication savedJobApplication = jobApplicationService.getLatestJobApplicationByContractorId(user.getUserId());
        ModelAndView mv = result.getModelAndView();

        // Check that the saved job application does not exist
        assertNull(savedJobApplication);
        assertNotNull(mv);
        assertNotNull(mv.getModel());


        assertEquals("01/02/2024", mv.getModel().get("date"));
        assertEquals(invalidPrice, mv.getModel().get("price"));
    }
    @Test
    void submitJobApplication_LoggedInContractorOwnsTheRequest_ApplicationIsNotSaved() throws Exception {
        mockMvc.perform(post("/serviceRequest/2")
                        .with(csrf())
                        .param("date", "01/02/2024")
                        .param("price", "100.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        JobApplication savedJobApplication = jobApplicationService.getLatestJobApplicationByContractorId(user.getUserId());

        assertNull(savedJobApplication);
    }

}