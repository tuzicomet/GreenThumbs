package nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherEntry implements Serializable{
    /**
     * Weather information for this date
     */
    @JsonProperty("dt")
    private long dt;
    /**
     * Time the sun rises this day
     */
    @JsonProperty("sunrise")
    private long sunrise;
    /**
     * Time the sun sets this day
     */
    @JsonProperty("sunset")
    private long sunset;
    /**
     * Temperature data
     */
    @JsonProperty("temp")
    private Temp temp;
    /**
     * Feels like this, temperature data
     */
    @JsonProperty("feels_like")
    private FeelsLike feels_like;
    /**
     * Atmospheric pressure on the sea level, hPa
     */
    @JsonProperty("pressure")
    private int pressure;
    /**
     * Humidity, %
     */
    @JsonProperty("humidity")
    private int humidity;

    /**
     * Main weather data, i.e sunny, icon for the weather
     */
    @JsonProperty("weather")
    private List<Weather> weather;
    /**
     * Maximum wind speed for the day. meters/sec
     */
    @JsonProperty("speed")
    private int speed;
    /**
     * Wind degree (direction), relevent to the maximum wind speed. degrees meteorological
     */
    @JsonProperty("deg")
    private int deg;
    /**
     *Wind gust meter/sec
     */
    @JsonProperty("gust")
    private float gust;
    /**
     * Cloudiness, %
     */
    @JsonProperty("clouds")
    private int clouds;
    /**
     * Precipitation volume, mm
     */
    @JsonProperty("rain")
    private float rain;
    /**
     * Snow volume, mm
     */
    @JsonProperty("snow")
    private float snow;
    /**
     * Probability of precipitation. The values of the parameter vary between 0 and 1, where 0 is equal to 0%, 1 is equal to 100%
     */
    @JsonProperty("pop")
    private float pop;


    public long getDt() {
        return dt;
    }


    public List<Weather> getWeather() {
        return weather;
    }

    public int getClouds() {
        return clouds;
    }

    public float getPop() {
        return pop;
    }

    public float getRain() {
        return rain;
    }

    public long getSunrise() {
        return sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public Temp getTemp() {
        return temp;
    }

    public FeelsLike getFeels_like() {
        return feels_like;
    }

    public int getPressure() {
        return pressure;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getSpeed() {
        return speed;
    }

    public int getDeg() {
        return deg;
    }

    public float getGust() {
        return gust;
    }

    public float getSnow() {
        return snow;
    }
}
