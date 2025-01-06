package nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Weather implements Serializable {
    /**
     * The weather condition id
     */
    @JsonProperty("id")
    private int id;
    /**
     * The main weather condition, i.e, Rain, snow , clouds
     */
    @JsonProperty("main")
    private String main;
    /**
     * Slightly more descriptive main weather, i.e light weather
     */
    @JsonProperty("description")
    private String description;
    /**
     * Key for the weather image
     */
    @JsonProperty("icon")
    private String icon;

    public int getId() {
        return id;
    }

    public String getMain() {
        return main;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
}