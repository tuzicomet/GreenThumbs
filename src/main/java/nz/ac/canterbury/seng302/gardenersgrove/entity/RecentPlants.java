package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "RECENT_PLANTS")
public class RecentPlants {
    
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
    @JoinColumn(name = "plantId")
    private Plant plant;

    /**
     * Date that the garden was visited
     */
    @Column(name = "visit_date")
    private LocalDateTime visitDate;

    /**
     * JPA (Java Persistance API) required no-args constructor, needed for DB to work
     */
    public RecentPlants() {}

    /**
     * Represents a Plant using its name, location, and size
     * @param user the user viewing the gardens
     * @param plant the plant the user has been viewing
     * @param visitDate the date when the user viewed the garden
     * 
     */
    public RecentPlants(AbstractUser user, Plant plant, LocalDateTime visitDate) {
        this.user = user;
        this.plant = plant;
        this.visitDate = visitDate;
    }

    /**
     * Get garden ID
     * @return unique ID of the plant
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id of the plant
     * 
     * @param id the unique ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user viewing the plants
     * 
     * @return the user viewing the plants
     */
    public AbstractUser getUser() {
        return user;
    }

    /**
     * Sets the user viewing the plants
     * 
     * @param user the user to set
     */
    public void setUser(AbstractUser user) {
        this.user = user;
    }

    /**
     * gets the plant object
     * 
     * @return the plant object
     */
    public Plant getPlant() {
        return plant;
    }

    /**
     * Sets the plant object
     * 
     * @param plant the plant object to set
     */
    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    /**
     * Gets the date the plant was visited
     * 
     * @return date the plant was visited
     */
    public LocalDateTime getVisitDate() {
        return visitDate;
    }

    /**
     * Sets the date the garden was visited
     */
    public void setVisitDate(LocalDateTime visitDate){
        this.visitDate = visitDate;
    }
}
