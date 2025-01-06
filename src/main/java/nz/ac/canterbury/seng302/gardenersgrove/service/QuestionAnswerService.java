package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Contractor;
import nz.ac.canterbury.seng302.gardenersgrove.entity.QuestionAnswer;
import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import nz.ac.canterbury.seng302.gardenersgrove.repository.QuestionAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for the QuestionAnswer entity
 */
@Service
public class QuestionAnswerService {
    private final QuestionAnswerRepository questionAnswerRepository;

    @Autowired
    public QuestionAnswerService(QuestionAnswerRepository questionAnswerRepository) {
        this.questionAnswerRepository = questionAnswerRepository;
    }

    public QuestionAnswer saveQuestionAnswer(QuestionAnswer questionAnswer) {
        return questionAnswerRepository.save(questionAnswer);
    }

    /**
     * Finds the QuestionAnswers by service request id
     * @param serviceRequestId
     * @return questionAnswerLookup
     */
    public List<QuestionAnswer> findQuestionAnswersByServiceRequest(Long serviceRequestId) {
        Optional<List<QuestionAnswer>> questionAnswerLookup = questionAnswerRepository.findQuestionAnswersByServiceRequest(serviceRequestId);
        return questionAnswerLookup.orElseGet(ArrayList::new);
    }
    /**
     * Gets the number of unanswered questions by a given contractor on a given service request
     * @param serviceRequest
     * @param contractor
     * @return The count
     */
    public int getNumberOfUnansweredQuestions(ServiceRequest serviceRequest, Contractor contractor){
        return questionAnswerRepository.countQuestionAnswerByServiceRequestAndContractorAndAnswerIsNull(serviceRequest, contractor);
    }

    /**
     * Gets the total number of unanswered questions from anyone on a given service request
     * @param serviceRequest the service request to retrieve the number of unanswered questions for
     * @return the number of unanswered questions, as an int
     */
    public int getTotalNumberOfUnansweredQuestions(ServiceRequest serviceRequest){
        return questionAnswerRepository.countQuestionAnswerByServiceRequestAndAnswerIsNull(serviceRequest);
    }

    public Optional<QuestionAnswer> getQuestionAnswerById(Long questionId) {
        return questionAnswerRepository.findById(questionId);
    }



}
