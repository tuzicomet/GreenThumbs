package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Entity class to represent a contractor's application for a specific Job/Service Request
 */
@Entity
public class JobApplication {

    /**
     * Unique ID number of the specific job application
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The job/service request which this application is for
     */
    @ManyToOne
    @JoinColumn(name = "job_id")
    private ServiceRequest job;

    /**
     * The contractor who this application belongs to
     */
    @ManyToOne
    @JoinColumn(name = "contractor_id")
    private Contractor contractor;

    /**
     * The date the applicant proposes to perform the job.
     */
    @Column
    private LocalDate date;

    /**
     * The price the applicant proposes to charge for completing the job.
     */
    @Column
    private double price;

    /**
     * The status of the application.
     */
    @Column(name = "STATUS")
    private String status;

    /**
     * Default, no-args constructor, required for JPA.
     */
    public JobApplication() {}

    /**
     * Parameterized constructor to create a JobApplication instance
     * @param job the job which this application is applying for
     * @param contractor the contractor who this application was submitted by
     * @param date the proposed date of completion
     * @param price the proposed price charge for completion
     * @param status the proposed start of application
     */
    public JobApplication(ServiceRequest job, Contractor contractor,
                          LocalDate date, double price, String status) {
        this.job = job;
        this.contractor = contractor;
        this.date = date;
        this.price = price;
        this.status = status;
    }

    // Getter and Setter methods

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ServiceRequest getJob() {
        return job;
    }

    public void setJob(ServiceRequest job) {
        this.job = job;
    }

    public Contractor getContractor() {
        return contractor;
    }

    public void setContractor(Contractor contractor) {
        this.contractor = contractor;
    }

    public LocalDate getDate() {
        return date;
    }

    public Date getDateAsDate() {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
