package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

/**
 * Entity class to represent widget preferences belonging to a user,
 * represents rows from the widget_preferences table
 */
@Entity
@Table(name="WIDGET_PREFERENCES")
public class WidgetPreferences {

    /**
     * Primary key, id of the widget preferences row
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * ID of the user who the widget preferences belong to
     */
    @JoinColumn(name = "user_id")
    private Long userId;

    /**
     * Boolean for if the welcome widget should be on
     */
    @Column(nullable = false)
    private Boolean welcome = true; // Default value is true

    /**
     * Boolean for if the recent gardens widget should be on
     */
    @Column(name = "recent_gardens", nullable = false)
    private Boolean recentGardens = true; // Default value is true

    /**
     * Boolean for if the recent plants widget should be on
     */
    @Column(name = "recent_plants", nullable = false)
    private Boolean recentPlants = true; // Default value is true

    /**
     * Boolean for if the friends widget should be on
     */
    @Column(nullable = false)
    private Boolean friends = true; // Default value is true

    /**
     * Default constructor, required by JPA
     */
    protected WidgetPreferences() {}

    /**
     * Creates a new WidgetPreferences object
     */
    public WidgetPreferences(Long userId, Boolean welcome, Boolean recentGardens, Boolean recentPlants, Boolean friends) {
        this.userId = userId;
        this.welcome = welcome;
        this.recentGardens = recentGardens;
        this.recentPlants = recentPlants;
        this.friends = friends;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Boolean getWelcome() {
        return welcome;
    }

    public Boolean getRecentGardens() {
        return recentGardens;
    }

    public Boolean getRecentPlants() {
        return recentPlants;
    }

    public Boolean getFriends() {
        return friends;
    }

    public void setWelcome(Boolean welcome) {
        this.welcome = welcome;
    }

    public void setRecentGardens(Boolean recentGardens) {
        this.recentGardens = recentGardens;
    }

    public void setRecentPlants(Boolean recentPlants) {
        this.recentPlants = recentPlants;
    }

    public void setFriends(Boolean friends) {
        this.friends = friends;
    }
}
