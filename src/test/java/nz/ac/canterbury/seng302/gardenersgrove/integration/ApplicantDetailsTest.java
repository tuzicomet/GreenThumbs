package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.MyServiceRequestsController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(MyServiceRequestsController.class)
class ApplicantDetailsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ServiceRequestService serviceRequestService;

    @MockBean
    private JobApplicationService jobApplicationService;

    @MockBean
    private Authentication authentication;

    @MockBean
    private GardenRepository gardenRepository;

    @MockBean
    private GardenService gardenService;

    @MockBean
    FileService fileService;

    @MockBean
    QuestionAnswerService questionAnswerService;

    @MockBean
    private MailService mailService;

    @BeforeEach
    void setup() {
        authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    void getApplicantDetails_Success() throws Exception {
        AbstractUser owner = new AbstractUser();
        owner.setUserId(1L);

        Contractor contractor = new Contractor();
        contractor.setUserId(2L);
        contractor.setFirstName("John");
        contractor.setProfilePicture("/images/john.jpg");
        contractor.setAboutMe("Experienced contractor");
        contractor.setNumRatings(10);
        contractor.setRatingTotal(45);

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setOwner(owner);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setContractor(contractor);

        when(userService.getUserFromAuthentication(authentication)).thenReturn(owner);
        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));
        when(userService.getContractorByUserId(2L)).thenReturn(Optional.of(contractor));
        when(jobApplicationService.getJobApplicationsByRequestId(1L)).thenReturn(List.of(jobApplication));
        when(userService.getContractorFlair(any(), any())).thenReturn(List.of("1", "1"));

        mockMvc.perform(get("/serviceRequest/1/applicant/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.profilePicture").value("/images/john.jpg"))
                .andExpect(jsonPath("$.aboutMe").value("Experienced contractor"))
                .andExpect(jsonPath("$.numRatings").value("Jobs Completed: 10"))
                .andExpect(jsonPath("$.avgRating").value(4.5));
    }

    @Test
    void getApplicantDetails_ServiceRequestDoesNotExist() throws Exception {
        when(serviceRequestService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/serviceRequest/1/applicant/2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getApplicantDetails_UserNotOwner() throws Exception {
        AbstractUser owner = new AbstractUser();
        owner.setUserId(1L);

        AbstractUser currentUser = new AbstractUser();
        currentUser.setUserId(3L);

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setOwner(owner);

        when(userService.getUserFromAuthentication(authentication)).thenReturn(currentUser);
        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));
        mockMvc.perform(get("/serviceRequest/1/applicant/2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getApplicantDetails_ContractorDoesNotExist() throws Exception {
        AbstractUser owner = new AbstractUser();
        owner.setUserId(1L);

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setOwner(owner);

        when(userService.getUserFromAuthentication(authentication)).thenReturn(owner);
        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));
        when(userService.getContractorByUserId(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/serviceRequest/1/applicant/2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getApplicantDetails_ContractorNotApplying() throws Exception {
        AbstractUser owner = new AbstractUser();
        owner.setUserId(1L);

        Contractor contractor = new Contractor();
        contractor.setUserId(2L);

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setOwner(owner);

        when(userService.getUserFromAuthentication(authentication)).thenReturn(owner);
        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));
        when(userService.getContractorByUserId(2L)).thenReturn(Optional.of(contractor));
        when(jobApplicationService.getJobApplicationsByRequestId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/serviceRequest/1/applicant/2"))
                .andExpect(status().isBadRequest());
    }
}
