package nz.ac.canterbury.seng302.gardenersgrove.entity.historyOfWeatherEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherStats implements Serializable {

    /**
     * Minimum absolute precipitation volume based on all historical measurements for this day, mm
     */
    @JsonProperty("min")
    private float min;

    /**
     * Maximum absolute precipitation volume based on all historical measurements for this day, mm
     */
    @JsonProperty("max")
    private float max;

    /**
     * The median value of the precipitation volume, mm
     */
    @JsonProperty("median")
    private float median;

    /**
     * Average of all precipitation volume measurements for this day, mm
     */
    @JsonProperty("mean")
    private float mean;

    /**
     * The first quartile value of the precipitation volume, mm
     */
    @JsonProperty("p25")
    private float p25;

    /**
     * The third quartile value of the precipitation volume, mm
     */
    @JsonProperty("p75")
    private float p75;

    /**
     * The standard deviation of the precipitation volume, mm
     */
    @JsonProperty("st_dev")
    private float st_dev;

    /**
     * Number of measurements
     */
    @JsonProperty("num")
    private float num;

    public float getMean() {
        return mean;
    }
    public float getNum(){return num;}


}
