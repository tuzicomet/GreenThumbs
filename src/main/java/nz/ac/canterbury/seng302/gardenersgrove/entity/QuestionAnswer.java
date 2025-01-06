package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity class to represent a question and answer
 * Takes the service request, owner, contractor, question, answer, question date, and answer date.
 */
@Entity
@Table(name = "QUESTIONANSWER")
public class QuestionAnswer {

    /**
     * ID of the question and answer pair
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QUESTION_ID")
    private Long questionId;

    /**
     * The ID of the service request that the Q&A is associated with
     * Many-to-one relationship, one service request can have many Q&A
     */
    @ManyToOne
    @JoinColumn(name = "REQUEST_ID", nullable = false)
    private ServiceRequest serviceRequest;

    /**
     * ID of the owner of the service request
     * Many-to-one relationship, one owner can have many service requests
     */
    @ManyToOne
    @JoinColumn(name="OWNER_ID", nullable = false)
    private AbstractUser owner;

    /**
     * ID of the contractor who asked the question
     * Many-to-one relationship, one contractor can ask many questions
     */
    @ManyToOne
    @JoinColumn(name = "CONTRACTOR_ID", nullable = false)
    private Contractor contractor;


    /**
     * String of the question
     */
    @Column(nullable = false, length=512)
    private String question;

    /**
     * String of the answer
     */
    @Column(length=512)
    private String answer;

    /**
     * Date the question was asked
     */
    @Column
    private Instant questionDate;

    /**
     * Date the answer was given
     */
    @Column
    private Instant answerDate;


    /**
     * JPA Required constructor
     */
    public QuestionAnswer() {}

    /**
     * Question and answer constructor
     */
    public QuestionAnswer(ServiceRequest serviceRequest, Contractor contractor, String question) {
        this.serviceRequest = serviceRequest;
        this.owner = serviceRequest.getOwner();
        this.contractor = contractor;
        this.question = question;
        this.questionDate = Instant.now();
    }

    public Long getQuestionId() {
        return questionId;
    }

    public ServiceRequest getServiceRequest() {
        return serviceRequest;
    }

    public AbstractUser getOwner() {
        return owner;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public Instant getQuestionDate() {
        return questionDate;
    }

    public Instant getAnswerDate() {
        return answerDate;
    }

    public void setAnswer(String answer){
        this.answer = answer;
        this.setAnswerDate(Instant.now());
    }
    public void setAnswerDate(Instant answerDate){
        this.answerDate = answerDate;
    }

    /**
     *
     * @return path to contractor's profile picture
     */
    public String getContractorImagePath() {
        return this.contractor.getProfilePicture();
    }

    /**
     * Finds if the QuestionAnswer has an answer attached to it
     * @return True if the question has an answer.
     */
    public Boolean hasAnswer() {
        return this.getAnswer() != null;
    }

    public void setId(long id) {
        this.questionId = id;
    }
}


