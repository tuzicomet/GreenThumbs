package nz.ac.canterbury.seng302.gardenersgrove.entity.historyOfWeatherEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TempHistory implements Serializable {

    @JsonProperty("record_min")
    private float record_min;
    @JsonProperty("record_max")
    private float record_max;
    @JsonProperty("average_min")
    private float average_min;
    @JsonProperty("average_max")
    private float average_max;
    @JsonProperty("median")
    private float median;
    @JsonProperty("mean")
    private float mean;
    @JsonProperty("p25")
    private float p25;
    @JsonProperty("p75")
    private float p75;
    @JsonProperty("st_dev")
    private float st_dev;
    @JsonProperty("num")
    private float num;
}
