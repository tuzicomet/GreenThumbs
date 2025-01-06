package nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class City implements Serializable {
    /**
     * City Id within openWeatherApi (depreciated)
     */
    @JsonProperty("id")
    private int id;
    /**
     * City name within openWeatherApi (depreciated)
     */
    @JsonProperty("name")
    private String name;
    /**
     * City co-ordinates
     */
    @JsonProperty("coord")
    private Coord coord;
    /**
     * Country name within openWeatherApi (depreciated)
     */
    @JsonProperty("country")
    private String country;
    /**
     * "internal parameter"
     */
    @JsonProperty("population")
    private int population;
    /**
     *  Shift in seconds from UTC
     */
    @JsonProperty("timezone")
    private int timezone;



    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coord getCoord() {
        return coord;
    }

    public String getCountry() {
        return country;
    }

    public int getPopulation() {
        return population;
    }

    public int getTimezone() {
        return timezone;
    }
}


