package nz.ac.canterbury.seng302.gardenersgrove.entity.historyOfWeatherEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherHistory implements Serializable {

    @JsonProperty("cod")
    private int cod;
    @JsonProperty("city_id")
    private long city_id;
    @JsonProperty("calctime")
    private double calctime;
    @JsonProperty("result")
    private WeatherData result;

    public int getCod() {
        return cod;
    }

    public WeatherData getResult() {
        return result;
    }
    public long getCity_id() {
        return city_id;
    }

    public double getCalctime() {
        return calctime;
    }
}
