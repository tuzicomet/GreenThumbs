package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Entity class to represent a Plant.
 * Takes its name, count, description, planting date, and image path.
 */
@Entity
@Table(name = "PLANT")
public class Plant {

    /**
     * Path of the default plant image.
     */
    public final static String DEFAULT_IMAGE_PATH = "/images/PlantPlaceholder.jpg";

    /**
     * The CrudRepository automatically generates an id as a primary key on upload.
     * Each Garden has a completely unique id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gardenId")
    private Garden garden;

    /**
     * Name of the Plant, required field
     */
    @Column(nullable = false)
    private String name;

    /**
     * Count of the given plant in this garden
     */
    @Column
    private String count;

    /**
     * Description of the plant
     */
    @Column(length=512)
    private String description;

    /**
     * Date that the plant was planted
     */
    @Column
    private LocalDate date;

    /**
     * Path of the image relative to the user_uploads directory, required field but default value on init.
     * Pass null for default image.
     */
    @Column
    private String imagePath;



    /**
     * JPA (Java Persistance API) required no-args constructor, needed for DB to work
     */
    protected Plant() {}

    /**
     * Represents a Plant using its name, location, and size
     * @param garden parent Garden object
     * @param name name of the Plant
     * @param count total count of this plant for the given garden
     * @param description description of the plant
     * @param date date the plant was planted
     * @param imagePath path of the uploaded plant image
     */
    public Plant(Garden garden, String name, String count, String description, LocalDate date, String imagePath) {
        this.garden = garden;
        this.name = name;
        this.count = count;
        this.description = description;
        this.date = date;
        this.imagePath = imagePath != null ? imagePath : DEFAULT_IMAGE_PATH;
    }

    /**
     * Get generated primary key ID of the Plant object from the DB
     * @return unique ID of the Plant
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the Garden object this plant belongs to
     */
    public Garden getGarden() {
        return garden;
    }

    /**
     * @return the name of the Plant
     */
    public String getName() {
        return name;
    }

    /**
     * @return the total count of the Plant in the given garden
     */
    public String getCount() {
        return count;
    }

    /**
     * @return description of the Plant
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return date the Plant was planted
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @return path of the image relative to the user_uploads directory
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * @return date the Plant was planted in little endian string form
     */
    public String getDisplayDate() {
        if(this.date == null){
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return date.format(formatter);
    }
    /**
     * Sets the name of the plant
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Sets the date the plant was planted
     */
    public void setDate(LocalDate date){this.date = date;}

    /**
     * Sets the count of the plant
     */
    public void setCount(String count) {
        this.count = count;
    }

    /**
     * Sets the description of the plant
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param imagePath the path of the image to set. format /images/{filename}.{extension}
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath != null ? imagePath : DEFAULT_IMAGE_PATH;
    }

    /**
     * Generate a string representation of the Plant object
     */
    @Override
    public String toString() {
        return "Plant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", count='" + count + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                '}';
    }
}

