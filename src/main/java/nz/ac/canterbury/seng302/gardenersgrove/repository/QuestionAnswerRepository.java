package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Contractor;
import nz.ac.canterbury.seng302.gardenersgrove.entity.QuestionAnswer;
import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for the QuestionAnswer Entity
 */
@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {


    @Query("SELECT q from QuestionAnswer q WHERE q.serviceRequest.id = :serviceRequestId")
    Optional<List<QuestionAnswer>> findQuestionAnswersByServiceRequest(@Param("serviceRequestId") Long serviceRequestId);

    /**
     * Gets the number of unanswered questions by a given contractor on a given service request
     * @param serviceRequest
     * @param contractor
     * @return The count
     */
    int countQuestionAnswerByServiceRequestAndContractorAndAnswerIsNull(ServiceRequest serviceRequest, Contractor contractor);

    /**
     * Gets the total number of unanswered questions on a given service request
     * @param serviceRequest the service request to retrieve the number of unanswered questions for
     * @return the number of unanswered questions, as an int
     */
    int countQuestionAnswerByServiceRequestAndAnswerIsNull(ServiceRequest serviceRequest);
}
