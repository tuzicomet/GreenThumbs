package nz.ac.canterbury.seng302.gardenersgrove.entity.historyOfWeatherEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherData implements Serializable {
    @JsonProperty("month")
    private int month;
    @JsonProperty("day")
    private int day;
    @JsonProperty("temp")
    private TempHistory temp;
    @JsonProperty("pressure")
    private WeatherStats pressure;
    @JsonProperty("humidity")
    private WeatherStats humidity;
    @JsonProperty("wind")
    private WeatherStats wind;
    @JsonProperty("precipitation")
    private WeatherStats precipitation;
    @JsonProperty("clouds")
    private WeatherStats counds;

    public WeatherStats getPrecipitation() {
        return precipitation;
    }
}
