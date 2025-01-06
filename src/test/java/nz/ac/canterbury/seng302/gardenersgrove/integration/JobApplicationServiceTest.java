package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.JobApplicationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LocationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ServiceRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.JobApplicationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing class to verify methods within the JobApplicationService class work as intended
 */
@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class JobApplicationServiceTest {
    @Autowired
    JobApplicationRepository jobApplicationRepository;
    @Autowired
    JobApplicationService jobApplicationService;
    @Autowired
    ServiceRequestRepository serviceRequestRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
    @Autowired
    LocationRepository locationRepository;

    private ServiceRequest serviceRequest1;
    private ServiceRequest serviceRequest2;
    private User user;
    private User userWithNoApplications;
    private long userId;
    private Contractor contractorUser;
    private Contractor contractorUserWithNoApplications;
    private Garden garden;
    private JobApplication pendingJobApplication1;
    private JobApplication pendingJobApplication2;
    private JobApplication declinedJobApplication1;
    private JobApplication declinedJobApplication2;

    @BeforeEach
    void setup() {

        // Initialize test data

        // Create a user (who we will give job applications to)
        user = new User(
                "User One",
                "test",
                "test1@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        userRepository.save(user);

        userWithNoApplications = new User(
                "User Two",
                "test",
                "test2@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        userRepository.save(userWithNoApplications);

        garden = new Garden(
                "Garden 1","1.0",user,"Valid",true,
                null,null,true,null
        );

        Location location1 = new Location(
                "Engineering Road, Riccarton, Christchurch 8041, New Zealand",
                "New Zealand","Christchurch City","Riccarton",
                "Engineering Road","8041"
        );

        location1.setLon(1.0);
        location1.setLat(2.0);
        locationRepository.save(location1);
        garden.setGardenId(1L);
        garden.setLocation(location1);

        // Create a second location for the second user (two users cant share the same location id)
        Location location2 = new Location(
                "Engineering Road, Riccarton, Christchurch 8041, New Zealand",
                "New Zealand","Christchurch City","Riccarton",
                "Engineering Road","8041"
        );

        location2.setLon(1.0);
        location2.setLat(2.0);
        locationRepository.save(location2);

        // Create the first test service request
        serviceRequest1 = new ServiceRequest(
                "Test service request 1", "Test description",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-12-31T23:59:59Z"),
                "/images/default.jpg",
                10.00,
                200.00,
                garden
        );
        serviceRequest1.setId(1L);
        serviceRequestRepository.save(serviceRequest1);

        // Create the second test service request
        serviceRequest2 = new ServiceRequest(
                "Test service request 1", "Test description",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-12-31T23:59:59Z"),
                "/images/default.jpg",
                10.00,
                200.00,
                garden
        );
        serviceRequest2.setId(2L);
        serviceRequestRepository.save(serviceRequest2);

        // Make the test users into contractors
        String aboutMe = "I am a skilled contractor.";
        List<String> workPictures = new ArrayList<>();
        workPictures.add("work1.jpg");
        workPictures.add("work2.jpg");
        userService.convertUserToContractor(user, aboutMe, workPictures, location1);
        contractorUser = (Contractor) userRepository.findByEmail(user.getEmail());
        userService.convertUserToContractor(userWithNoApplications, aboutMe, workPictures, location2);
        contractorUserWithNoApplications = (Contractor) userRepository.findByEmail(user.getEmail());

        // Create the job applications for user 1
        pendingJobApplication1 = new JobApplication(serviceRequest1, contractorUser, LocalDate.parse("01/02/2024", DateTimeFormatter.ofPattern("dd/MM/yyyy")), 11.0, "PENDING");
        pendingJobApplication2 = new JobApplication(serviceRequest1, contractorUser, LocalDate.parse("01/02/2024", DateTimeFormatter.ofPattern("dd/MM/yyyy")), 11.0, "PENDING");
        declinedJobApplication1 = new JobApplication(serviceRequest1, contractorUser, LocalDate.parse("01/02/2024", DateTimeFormatter.ofPattern("dd/MM/yyyy")), 11.0, "DECLINED");
        declinedJobApplication2 = new JobApplication(serviceRequest1, contractorUser, LocalDate.parse("01/02/2024", DateTimeFormatter.ofPattern("dd/MM/yyyy")), 11.0, "DECLINED");
    }

    @Test
    void saveJobApplication_JobApplicationIsValid_JobApplicationIsSaved() {
        JobApplication savedJobApplication = jobApplicationService.saveJobApplication(pendingJobApplication1);
        assertNotNull(savedJobApplication);
    }

    @Test
    void getLatestJobApplicationByContractorId_ContractorHasNoJobApplication_ReturnsNull() {
        assertNull(jobApplicationService.getLatestJobApplicationByContractorId(contractorUserWithNoApplications.getUserId()));
    }

    @Test
    void getLatestJobApplicationByContractorId_ContractorHasOneJobApplication_ReturnsCorrectApplication() {
        JobApplication savedJobApplication = jobApplicationService.saveJobApplication(pendingJobApplication1);
        JobApplication retrievedJobApplication = jobApplicationService.getLatestJobApplicationByContractorId(user.getUserId());
        assertEquals(savedJobApplication, retrievedJobApplication);
    }

    @Test
    void getLatestJobApplicationByContractorId_ContractorHasTwoJobApplications_ReturnsCorrectApplication() {
        // Save both job applications
        JobApplication savedJobApplication1 = jobApplicationService.saveJobApplication(pendingJobApplication1);
        JobApplication savedJobApplication2 = jobApplicationService.saveJobApplication(pendingJobApplication2);

        // retrieve the latest job application
        JobApplication retrievedJobApplication = jobApplicationService.getLatestJobApplicationByContractorId(user.getUserId());
        // The retrieved job application should be the last one added (savedJobApplication2)
        assertEquals(savedJobApplication2, retrievedJobApplication);
        // and check that it is NOT equal to the first one
        assertNotEquals(savedJobApplication1, retrievedJobApplication);
    }

    @Test
    void getActiveJobApplicationsByRequestId_RequestIdIsInvalid_ReturnsEmptyList() {
        // attempt to retrieve active job applications for a service request that does not exist
        List<JobApplication> retrievedJobApplication = jobApplicationService.getActiveJobApplicationsByRequestId(99999L);
        // no jobs should be returned (empty list)
        assertTrue(retrievedJobApplication.isEmpty());
    }

    @Test
    void getActiveJobApplicationsByRequestId_RequestHasOnePendingApplicationAndNoDeclined_ListReturnedWithApplication() {
        // save job applications applying to the service request with id 1
        JobApplication savedJobApplication1 = jobApplicationService.saveJobApplication(pendingJobApplication1);

        // attempt to retrieve active job applications for the service request with id 1
        List<JobApplication> retrievedJobApplications = jobApplicationService.getActiveJobApplicationsByRequestId(1L);

        // Check that the one job application is the only thing in the returned list
        assertEquals(1, retrievedJobApplications.size());
        assertEquals(savedJobApplication1.getId(), retrievedJobApplications.get(0).getId());
    }

    @Test
    void getActiveJobApplicationsByRequestId_RequestHasTwoPendingApplicationsAndNoDeclined_ListReturnedWithApplications() {
        // save job applications applying to the service request with id 1
        JobApplication savedJobApplication1 = jobApplicationService.saveJobApplication(pendingJobApplication1);
        JobApplication savedJobApplication2 = jobApplicationService.saveJobApplication(pendingJobApplication2);

        // attempt to retrieve active job applications for the service request with id 1
        List<JobApplication> retrievedJobApplications = jobApplicationService.getActiveJobApplicationsByRequestId(1L);

        // Check that the two job applications are the only things in the returned list
        assertEquals(2, retrievedJobApplications.size());
        assertEquals(savedJobApplication1.getId(), retrievedJobApplications.get(0).getId());
        assertEquals(savedJobApplication2.getId(), retrievedJobApplications.get(1).getId());
    }

    @Test
    void getActiveJobApplicationsByRequestId_RequestHasOnePendingAndOneDeclinedApplication_ListReturnedWithApplication() {
        // save job applications applying to the service request with id 1
        JobApplication savedJobApplication1 = jobApplicationService.saveJobApplication(pendingJobApplication1);
        // also save one of the declined job applications, which is also for the same service request
        jobApplicationService.saveJobApplication(declinedJobApplication1);

        // attempt to retrieve active job applications for the service request with id 1
        List<JobApplication> retrievedJobApplications = jobApplicationService.getActiveJobApplicationsByRequestId(1L);

        // Check that the one job application is the only thing in the returned list
        assertEquals(1, retrievedJobApplications.size());
        assertEquals(savedJobApplication1.getId(), retrievedJobApplications.get(0).getId());
    }

    @Test
    void getActiveJobApplicationsByRequestId_RequestHasTwoPendingAndOneDeclinedApplication_ListReturnedWithApplications() {
        // save job applications applying to the service request with id 1
        JobApplication savedJobApplication1 = jobApplicationService.saveJobApplication(pendingJobApplication1);
        JobApplication savedJobApplication2 = jobApplicationService.saveJobApplication(pendingJobApplication2);

        // also save both of the declined job applications, which is also for the same service request
        jobApplicationService.saveJobApplication(declinedJobApplication1);
        jobApplicationService.saveJobApplication(declinedJobApplication2);

        // attempt to retrieve active job applications for the service request with id 1
        List<JobApplication> retrievedJobApplications = jobApplicationService.getActiveJobApplicationsByRequestId(1L);

        // Check that the two job applications are the only things in the returned list
        assertEquals(2, retrievedJobApplications.size());
        assertEquals(savedJobApplication1.getId(), retrievedJobApplications.get(0).getId());
        assertEquals(savedJobApplication2.getId(), retrievedJobApplications.get(1).getId());
    }
}
