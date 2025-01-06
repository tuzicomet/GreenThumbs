package nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherCurrent implements Serializable {
    @JsonProperty("weather")
    private List<Weather> weather;
    @JsonProperty("main")
    private Main main;

    public Weather getWeather() {
        return weather.getFirst();
    }
    public Main getMain() {
        return main;
    }
    public boolean isRaining(){
        return weather.getFirst().getMain().equals("Rain") || weather.getFirst().getMain().equals("Thunderstorm") || weather.getFirst().getMain().equals("Drizzle");
    }
}
