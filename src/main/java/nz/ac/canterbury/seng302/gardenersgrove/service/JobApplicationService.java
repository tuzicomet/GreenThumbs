package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.JobApplication;
import nz.ac.canterbury.seng302.gardenersgrove.repository.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for the JobApplication entity
 */
@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;

    @Autowired
    public JobApplicationService(JobApplicationRepository jobApplicationRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
    };

    /**
     * Saves a given job application to the database
     * @param jobApplication the job application to be saved
     * @return the saved job application
     */
    public JobApplication saveJobApplication(JobApplication jobApplication) {
        return jobApplicationRepository.save(jobApplication);
    }

    /**
     * Gets the latest job application submitted by the contractor with the given contractor id
     * @param contractorId the id of the contractor to retrieve the most recent job application for
     * @return the most recent job application submitted by the contractor
     */
    public JobApplication getLatestJobApplicationByContractorId(Long contractorId) {
        // Get the list of all job applications by the contractor with the given contractor id, in descending order
        List<JobApplication> jobApplications = jobApplicationRepository.findByContractorUserIdOrderByIdDesc(contractorId);
        // If the contractor does not have any job applications
        if (jobApplications.isEmpty()) {
            return null;
        }
        // Otherwise, return the latest job application, which should be the first one in the list
        return jobApplications.get(0);
    }

    /**
     * Gets all job applications submitted for service requests with the given service request id
     * @param serviceRequestId the id of the service request to retrieve the all job applications for service requests for
     * @return the job applications submitted to service request
     */
    public List<JobApplication> getJobApplicationsByRequestId(Long serviceRequestId) {
        return jobApplicationRepository.findByRequestId(serviceRequestId);
    }

    /**
     * Gets all job applications submitted for service requests with the given service request id,
     * which are active (i.e. not declined)
     * @param serviceRequestId the service request to find active job applications for
     * @return the active (non-declined) job applications submitted to service request
     */
    public List<JobApplication> getActiveJobApplicationsByRequestId(Long serviceRequestId) {
        return jobApplicationRepository.findByJobIdAndStatusNot(serviceRequestId, "DECLINED");
    }

    public Optional<JobApplication> findById(Long jobApplicationId) {
        return jobApplicationRepository.findById(jobApplicationId);
    }
}
