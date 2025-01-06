package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for the JobApplication entity
 */
@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    /**
     * Finds a job application with the given id, if it exists
     * @param id the id of the job application to find
     * @return the job application, optionally
     */
    Optional<JobApplication> findById(Long id);

    /**
     * Given a contractor user's id, returns a list containing all of their job applications,
     * ordered by id, ascending (oldest first, newest last)
     * @param contractorId the id of the contractor to retrieve job applications for
     * @return the contractor's list of job applications, in ascending order
     */
    List<JobApplication> findByContractorUserIdOrderByIdAsc(Long contractorId);

    /**
     * Given a contractor user's id, returns a list containing all of their job applications,
     * ordered by id, descending (newest first, oldest last)
     * @param contractorId the id of the contractor to retrieve job applications for
     * @return the contractor's list of job applications, in descending order
     */
    List<JobApplication> findByContractorUserIdOrderByIdDesc(Long contractorId);

    /**
     * Gets all job applications for a specific service request
     * @param serviceRequestId ID of the service request
     * @return List of job applications for the given service request
     */
    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.id = :serviceRequestId")
    List<JobApplication> findByRequestId(@Param("serviceRequestId") Long serviceRequestId);

    /**
     * JPA query which finds job requests for the given job id, where the status is not equal to the given string
     * @param jobId the id of the job to find requests for
     * @param status the status to exclude finding results with
     * @return a list of JobApplication objects which meet the criteria
     */
    List<JobApplication> findByJobIdAndStatusNot(Long jobId, String status);
}
