package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

import java.time.*;
import java.time.format.DateTimeFormatter;

import java.util.Locale;

import org.springframework.data.domain.Range;

import java.util.Objects;

/**
 * Entity class to represent a Service Request
 *
 */
@Entity
@Table(name = "SERVICE_REQUEST")
public class ServiceRequest {

    /**
     * ID of the service request
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String releaseDateTime;


    /**
     * Title of the service request, required field
     */
    @Column(nullable = false)
    private String title;

    /**
     * Description of the service request, required field
     */
    @Column(length=512, nullable = false)
    private String description;

    @Column
    private Instant dateMin;

    @Column
    private Instant dateMax;

    @Column
    private String imagePath;

    @Column
    private double priceMin;

    @Column
    private double priceMax;

    @Column
    private boolean completed;

    @Column
    private Double agreedPrice;

    @Column
    private Instant agreedDate;

    @Column
    private Integer rating;


    /**
     * Set of owners of the service request.
     * ManyToMany + CascadeType. ALL means the ServiceRequest.owners and User.serviceRequest sets are always in sync
     */
    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private AbstractUser owner;

    @ManyToOne
    @JoinColumn(name = "garden_id")
    private Garden garden;

    /**
     * The contractor assigned to the job
     */
    @ManyToOne
    @JoinColumn(name = "contractor_id")
    private Contractor contractor;

    /**
     * Default constructor for ServiceRequest
     */
    public ServiceRequest() {}

    public ServiceRequest(String title, String description, Instant dateMin, Instant dateMax, String imagePath, double priceMin, double priceMax, Garden garden) {
        this.title = title;
        this.description = description;
        this.dateMin = dateMin;
        this.dateMax = dateMax;
        this.imagePath = imagePath;
        this.priceMin = priceMin;
        this.priceMax = priceMax;
        this.garden = garden;
        this.owner = garden.getOwner();
        this.completed = false;
        this.agreedPrice = null;
        this.agreedDate = null;
        this.rating = null;
    }


    /**
     * @return the title of the Service Request
     */
    public String getTitle() { return title; }

    /**
     * @return the description of the Service Request
     */
    public String getDescription() { return description; }

    /**
     * @return the dateMin of the Service Request
     */
    public Instant getDateMin() { return dateMin; }

    /**
     * @return the dateMax of the Service Request
     */
    public Instant getDateMax() { return dateMax; }

    /**
     * @return the priceMin of the Service Request
     */
    public double getPriceMin() { return priceMin; }

    /**
     * @return the priceMax of the Service Request
     */
    public double getPriceMax() { return priceMax; }

    /**
     * Returns the range of times the user requested.
     * @return a Range of Instants generated from the min and max the user gave.
     */
    public Range<Instant> getDateRange() {
        return Range.closed(dateMin, dateMax);
    }

    /**
     * Returns the range of prices accepted for the service.
     * @return a Range of Doubles generated from the min and max prices the user gave.
     */
    public Range<Double> getPriceRange() { return Range.closed(priceMin, priceMax); }


    public Long getId() {
        return id;
    }

    public Contractor getContractor() {
        return contractor;
    }

    public void setContractor(Contractor contractor) {
        this.contractor = contractor;
    }

    public Range<Double> getCostRange() {
        return Range.closed(priceMin, priceMax);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGarden(Garden garden) {
        this.garden = garden;
    }
    public Location getLocation(){
        return garden.getLocation();
    }

    public AbstractUser getOwner() { return this.owner; }


    /**
     * Get a string representing the date range, or just the single date if the start and end dates are the same.
     * @return The formatted string
     */
    public String getDateRangeString(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());
        if(formatter.format(dateMax).equals(formatter.format(dateMin))){
            return formatter.format(dateMin);
        }
        return formatter.format(dateMin) + "  -  " + formatter.format(dateMax);
    }

    /**
     * Get a string representing the price range, or just the single price if the start and end prices are the same.
     * @return The formatted string
     */
    public String getPriceRangeString(){
        if(Objects.equals(priceMax, priceMin)){
            return "$" + priceMin;
        }
        return "$" + priceMin + " - $" + priceMax;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setPriceMin(Double priceMin) {
        this.priceMin = priceMin;
    }

    public void setPriceMax(Double priceMax) {
        this.priceMax = priceMax;
    }

    public void setDateMin(String dateMin) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
        LocalDate localDate = LocalDate.parse(dateMin, dateFormatter);
        this.dateMin = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public void setDateMax(String dateMax) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
        LocalDate localDate = LocalDate.parse(dateMax, dateFormatter);
        this.dateMax = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public Garden getGarden() {
        return garden;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setId(long l) {
        this.id = l;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Double getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(Double agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

    public Instant getAgreedDate() {
        return agreedDate;
    }

    public void setAgreedDate(Instant agreedDate) {
        this.agreedDate = agreedDate;
    }
    public String getAgreedDateString(){
        if(this.agreedDate == null){
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());
        return formatter.format(agreedDate);
    }

    public boolean isRated(){
        return rating != null;
    }

    public Integer getRating() {
        return this.rating;
    }
    public void setRating(Integer rating){
        this.rating = rating;
    }

    public void setOwner(AbstractUser owner) {
        this.owner = owner;
    }
}
