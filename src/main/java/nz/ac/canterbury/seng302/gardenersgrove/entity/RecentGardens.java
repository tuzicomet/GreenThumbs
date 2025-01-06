package nz.ac.canterbury.seng302.gardenersgrove.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class to represent a Recent Garden.
 * 
 */
@Entity
@Table(name = "RECENT_GARDENS")
public class RecentGardens {

    /**
     * The CrudRepository automatically generates an id as a primary key on upload.
     * Each Garden has a completely unique id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AbstractUser user;

    @ManyToOne
    @JoinColumn(name = "gardenId")
    private Garden garden;

    /**
     * Date that the garden was visited
     */
    @Column(name = "visit_date")
    private LocalDateTime visitDate;

    /**
     * JPA (Java Persistance API) required no-args constructor, needed for DB to work
     */
    public RecentGardens() {}

    /**
     * Represents a Plant using its name, location, and size
     * @param user the user viewing the gardens
     * @param garden the gardens the user has been viewing
     * @param visitDate the date when the user viewed the garden
     * 
     */
    public RecentGardens(AbstractUser user, Garden garden, LocalDateTime visitDate) {
        this.user = user;
        this.garden = garden;
        this.visitDate = visitDate;
    }

    /**
     * Get garden ID
     * @return unique ID of the garden
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the garden ID
     * 
     * @param id the unique ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user viewing the gardens
     * 
     * @return the user viewing the gardens
     */
    public AbstractUser getUser() {
        return user;
    }

    /**
     * Sets the user viewing the gardens
     * 
     * @param user the user to set
     */
    public void setUser(AbstractUser user) {
        this.user = user;
    }

    /**
     * @return the Garden object
     */
    public Garden getGarden() {
        return garden;
    }

    /**
     * Sets the garden
     * 
     * @param garden the garden object
     */
    public void setGarden(Garden garden) {
        this.garden = garden;
    }

    /**
     * @return date the garden was visited
     */
    public LocalDateTime getVisitDate() {
        return visitDate;
    }

    /**
     * Sets the date the garden was visited
     * @param visitDate the date the garden was visited
     */
    public void setVisitDate(LocalDateTime visitDate){
        this.visitDate = visitDate;
    }
}
