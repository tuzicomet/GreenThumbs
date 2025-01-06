package nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Temp implements Serializable {

    /**
     * Temperature at 12:00 local time, Celsius
     */
    @JsonProperty("day")
    private float day;
    /**
     * Min daily temperature, Celsius
     */
    @JsonProperty("min")
    private float min;
    /**
     * Max daily temperature, Celsius
     */
    @JsonProperty("max")
    private float max;
    /**
     * Temperature at 00:00 local time, Celsius
     */
    @JsonProperty("night")
    private float night;
    /**
     * Temperature at 18:00 local time, Celsius
     */
    @JsonProperty("eve")
    private float eve;
    /**
     * Temperature at 6:00 local time, Celsius
     */
    @JsonProperty("morn")
    private float morn;

    public float getDay() {
        return day;
    }

    public float getNight() {
        return night;
    }

    public float getEve() {
        return eve;
    }

    public float getMorn() {
        return morn;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }
}
