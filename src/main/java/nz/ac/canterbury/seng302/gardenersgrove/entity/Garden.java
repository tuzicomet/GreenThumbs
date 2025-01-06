package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities.WeatherForecast;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity class to represent a garden.
 * Takes its name, location, and size (in m^2).
 */
@Entity
@Table(name = "GARDEN")
public class Garden {
    /**
     * The CrudRepository automatically generates an id as a primary key on upload.
     * Each Garden has a completely unique id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the Garden, required field
     */
    @Column(nullable = false)
    private String name;

    /**
     * Size of the Garden in m^2
     */
    @Column
    private String size;

    /**
     * Weather forecast
     */
    @Column(name = "weather_forecast")
    @Lob
    private WeatherForecast weatherForecast;

    /**
     * When the weather forecast was last updated in the db
     */
    @Column(name = "updated_weather")
    private LocalDateTime updatedWeather;

    /**
     * Weather History previous day
     */
    @Column(name = "has_rained")
    private boolean hasRained;

    /**
     * When the weather history was last updated in the db
     */
    @Column(name = "updated_has_rained")
    private LocalDateTime updatedHasRained;

    /**
     * Description of the Garden
     */
    @Column(length=512)
    private String description;

    /**
     * Publicised Garden default false
     */
    @Column
    private boolean publicised;

    /**
     * Set of owners of the garden.
     * ManyToMany + CascadeType.ALL means the Garden.owners and User.gardens sets are always in sync
     */
    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private AbstractUser owner;

    @OneToMany(mappedBy = "garden")
    private Set<Plant> plants;

    /**
     * Location of the Garden, required field
     */
    @OneToOne(cascade = CascadeType.PERSIST)
    private Location location;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "attached_tag",
            joinColumns = @JoinColumn(name = "id"),
            inverseJoinColumns = @JoinColumn(name = "tagId")
    )
    private List<Tag> tags = new ArrayList<>();

    /**
     * JPA (Java Persistance API) required no-args constructor, needed for DB to work
     */
    public Garden() {}

    /**
     * Represents a Garden using its name, location, and size
     * @param name the name of the Garden
     * @param size the size of the Garden in m^2.
     */
    public Garden(String name, String size, AbstractUser owner, String description, Boolean publicised, WeatherForecast weatherForecast, LocalDateTime updatedWeather, boolean hasRained, LocalDateTime updatedHasRained) {
        this.name = name;
        this.size = size;
        this.owner = owner;
        this.plants = new HashSet<>();
        this.description = description;
        this.publicised = publicised;
        this.weatherForecast = weatherForecast;
        this.updatedWeather = updatedWeather;
        this.hasRained = hasRained;
        this.updatedHasRained = updatedHasRained;
    }

    /**
     * Same as above, but has the ID field. Because it is set by the repository, this should only be used for mocking.
     */
    public Garden(Long id, String name, String size, AbstractUser owner, String description, Boolean publicised, WeatherForecast weatherForecast, LocalDateTime updatedWeather, boolean hasRained, LocalDateTime updatedHasRained) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.owner = owner;
        this.plants = new HashSet<>();
        this.description = description;
        this.publicised = publicised;
        this.weatherForecast = weatherForecast;
        this.updatedWeather = updatedWeather;
        this.hasRained = hasRained;
        this.updatedHasRained = updatedHasRained;
    }

    /**
     * Get generated primary key ID of the Garden object from the DB
     * @return unique ID of the Garden
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the garden Id
     */
    public void setGardenId(Long gardenId) {
        this.id = gardenId;
    }

    /**
     * @return the name of the Garden
     */
    public String getName() {
        return name;
    }

    /**
     * @return the location of the Garden
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return size of the Garden in m^2
     */
    public String getSize() {
        return size;
    }

    public List<Tag> getTags() { return tags; }

    /**
     * returns a list of all verified tags, for use on public pages
     * @return
     */

    public List<Tag> getVerifiedTags() { return tags.stream().filter(tag -> tag.isVerified()).toList(); }

    /**
     * Sets the name of the garden
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the location of the garden
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Sets the size of the garden (in m^2)
     */
    public void setSize(String size) {
        this.size = size;
    }

    public WeatherForecast getForecast() {
        return this.weatherForecast;
    }

    public LocalDateTime getUpdatedTime() {
        return this.updatedWeather;
    }

    public void setWeatherForecast(WeatherForecast weatherForecast) {
        this.weatherForecast = weatherForecast;
    }

    public void setUpdatedWeather(LocalDateTime updatedWeather) {
        this.updatedWeather = updatedWeather;
    }

    public LocalDateTime getUpdatedHasRained() {
        return this.updatedHasRained;
    }

    public void setHasRained(Boolean hasRained) {
        this.hasRained = hasRained;
    }
    public boolean getHasRained() {
       return this.hasRained;
    }

    public void setUpdatedHasRained(LocalDateTime updatedHasRained) {
        this.updatedHasRained = updatedHasRained;
    }

    public AbstractUser getOwner() {
        return owner;
    }

    public void setOwner(AbstractUser user) {
        owner = user;
    }

    public Set<Plant> getPlants() {
        return plants;
    }

    public boolean addPlant(Plant plant) {
        return plants.add(plant);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublicised() {
        return publicised;
    }

    public void setPublicised(boolean publicised) {
        this.publicised = publicised;
    }

    /**
     * Add tag to the garden's list of tags. If the tag is already on the garden, it won't be added.
     * @param newTag Tag object to add to the garden
     * @return true if the tag was added, false if it was already on the garden
     */
    public boolean addTag(Tag newTag) {
        // If the tag is already on the garden, don't add it
        if (tags.stream().map(Tag::getContent).anyMatch(s -> s.equals(newTag.getContent()))) {
            return false;
        }
        return tags.add(newTag);
    }

    /**
     * Remove tag from the garden's list of tags. If it isn't in the list, return False.
     * @param tag Tag object to remove from the garden
     * @return true if the tag was removed, false if the tag isn't in the list
     */
    public boolean removeTag(Tag tag) {
        return tags.remove(tag);
    }

    /**
     * Generate a string representation of the Garden object
     */
    @Override
    public String toString() {
        return "Garden{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", size=" + size +
                ", description=" + description +
                '}';
    }
}
