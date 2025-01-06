package nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherForecast implements Serializable {
    /**
     * Response code from the API, i.e 200, 400, etc
     */
    @JsonProperty("cod")
    private String cod;
    /**
     * "Internal parameter" idk what this does
     */
    @JsonProperty("message")
    private int message;
    /**
     * number of days in forecast
     */
    @JsonProperty("cnt")
    private int cnt;
    /**
     * Weather data, one per day in forecast
     */
    @JsonProperty("list")
    private List<WeatherEntry> list;
    /**
     * City that the weather is for
     */
    @JsonProperty("city")
    private City city;

    public String getCod() {
        return cod;
    }

    public int getMessage() {
        return message;
    }

    public int getCnt() {
        return cnt;
    }

    public List<WeatherEntry> getList() {
        return list;
    }

    public City getCity() {
        return city;
    }
}

