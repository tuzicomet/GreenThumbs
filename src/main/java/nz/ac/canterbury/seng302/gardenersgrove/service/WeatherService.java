package nz.ac.canterbury.seng302.gardenersgrove.service;


import nz.ac.canterbury.seng302.gardenersgrove.entity.Alert;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.historyOfWeatherEntities.WeatherHistory;
import nz.ac.canterbury.seng302.gardenersgrove.entity.historyOfWeatherEntities.WeatherStats;
import nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities.WeatherCurrent;
import nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities.WeatherForecast;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.utility.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

/**
 * WeatherService class to handle the fetching of weather data from OpenWeatherMap.
 * Followed <a href="https://rapidapi.com/guides/make-api-call-java">this tutorial</a>
 */
@Service
public class WeatherService {
    private static final String LON_STRING = "&lon=";
    private static final String WEATHER_KEY = System.getenv("WEATHER_KEY");
    private static final Logger LOG = LoggerFactory.getLogger(WeatherService.class);

    private final GardenRepository gardenRepository;
    private final GardenService gardenService;
    private final AlertService alertService;

    @Autowired
    public WeatherService (GardenService gardenService, GardenRepository gardenRepository, AlertService alertService) {
        this.gardenRepository = gardenRepository;
        this.gardenService = gardenService;
        this.alertService = alertService;
    }

    /**
     * Takes a gardens location and returns the relevant weatherForecast.
     * If the data is recently added to the repository it will access it there.
     * else it will call the API
     * @param lat latitude of the garden
     * @param lng longitude of the garden
     * @param id if of the garden
     * @return null if lat or lng is null, or if the garden doesn't exist, otherwise the weatherForecast object
     */
    public WeatherForecast getWeather(Double lat, Double lng, long id, Locale locale) {
        // check for nulls so garden page doesn't error
        if (lat == null || lng == null) {
            LOG.error("Latitude or longitude is null for garden id: {}", id);
            return null;
        }
        Optional<Garden> gardenOptional = gardenService.getGarden(id);
        if (gardenOptional.isEmpty()) {
            return null;
        }
        Garden garden = gardenOptional.get();
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime updatedTime = garden.getUpdatedTime();
        //If the data in the repository does not exist or is old call the API
        if (updatedTime == null || localDateTime.minusHours(1).isAfter(updatedTime)) {
            return callForecastApi(lat, lng, garden, locale);
        } else {
            return gardenRepository.findWeatherById(id);
        }
    }

    /**
    * Retrieves the current weather for latitude and longitude.
    *
    * @param lat The latitude of the location.
    * @param lng The longitude of the location.
    * @return A {@link WeatherCurrent} object containing the current weather information, or {@code null} if an error occurs.
    */
    public WeatherCurrent getCurrentWeather(Double lat, Double lng, Locale locale) {
        // Create the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(
                        URI.create(
                                "https://api.openweathermap.org/data/2.5/weather?lang=%s&units=metric&lat=%s&lon=%s&appid=%s"
                                        .formatted(
                                                locale.getLanguage(),
                                                lat,
                                                lng,
                                                WEATHER_KEY
                                        )
                        )
                )
                .build();

        try (HttpClient currentClient = HttpClient.newHttpClient()){ //Send the HTTP request and get the response
            HttpResponse<String> response = currentClient.send(request, HttpResponse.BodyHandlers.ofString());
            // Convert the JSON response to a WeatherForecast object
            return Converter.convertWeatherJsonToObject(response.body(), WeatherCurrent.class);
        } catch (IOException | InterruptedException e) {
            LOG.error("Failed to call current weather API.");
            if(e.getClass() == InterruptedException.class){
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

    /**
     * Version of getCurrentWeather(Double lat, Double lng, Locale locale) that keeps the Locale as default.
     * @param lat latitude to get the weather of
     * @param lng longitude to get the weather of
     * @return Current weather at the supplied coordinates
     */
    public WeatherCurrent getCurrentWeather(Double lat, Double lng) {
        return getCurrentWeather(lat, lng, Locale.getDefault());
    }

    /**
     * Takes a garden's location and makes a call to OpenWeatherAPI, saves the response in the garden repository.
     * @param lat latitude of the garden
     * @param lng longitude of the garden
     * @param garden garden object
     * @param locale Locale to return the weather results as
     * @return Weather forecast for the garden
     */
    public WeatherForecast callForecastApi(Double lat, Double lng, Garden garden, Locale locale) {
        // Create the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(
                        URI.create(
                                "https://api.openweathermap.org/data/2.5/forecast/daily?lang=%s&lat=%s&lon=%s&cnt=5&units=metric&appid=%s"
                                        .formatted(
                                                locale.getLanguage(),
                                                lat,
                                                lng,
                                                WEATHER_KEY
                                        )
                        )
                )
                .build();

        HttpResponse<String> response;
        try (HttpClient forecastClient = HttpClient.newHttpClient()){ //Send the HTTP request and get the response
            response = forecastClient.send(request, HttpResponse.BodyHandlers.ofString());
            // Convert the JSON response to a WeatherForecast object
            WeatherForecast weatherForecast = Converter.convertWeatherJsonToObject(response.body(), WeatherForecast.class);
            garden.setWeatherForecast(weatherForecast);
            garden.setUpdatedWeather(LocalDateTime.now());
            gardenRepository.save(garden);
            return weatherForecast;
        } catch (IOException | InterruptedException e) {
            LOG.error("Failed to call forecast API.");
            if(e.getClass() == InterruptedException.class){
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

/**
 * Checks whether it has been raining in the gardens location
 * Checks over the current day and previous day (2 days total)
 *
 * @param lat The latitude of the garden
 * @param lng The longitude of the garden
 * @param id The garden ID
 * @return true if it has rained in the garden's location, false if not
 */
    public boolean getHasRained(Double lat, Double lng, long id) {
        Garden garden;
        if (gardenService.getGarden(id).isPresent()) {
            garden = gardenService.getGarden(id).orElse(null);
            if(garden == null){return false;}

            LocalDateTime localDateTime = LocalDateTime.now();
            //Get the last time the database was updated
            LocalDateTime updatedHistoryTime = garden.getUpdatedHasRained();

            //If the data in the repository does not exist or is old call the API
            if (updatedHistoryTime == null || localDateTime.minusDays(1).isAfter(updatedHistoryTime)) {

                LocalDateTime previousDate = localDateTime.minusDays(1);


                WeatherHistory yesterday = callHistoryApi(lat, lng, previousDate);
                if (yesterday == null || yesterday.getResult() == null ) {
                    return false;
                }

                WeatherStats yesterdaysWeather = yesterday.getResult().getPrecipitation();

                float yesterdaysRain = yesterdaysWeather.getMean() * yesterdaysWeather.getNum();

                if (yesterdaysRain > 2) {
                    garden.setHasRained(true);
                    garden.setUpdatedHasRained(LocalDateTime.now());
                    gardenRepository.save(garden);
                    return true;
                } else {
                    garden.setHasRained(false);
                    garden.setUpdatedHasRained(LocalDateTime.now());
                    gardenRepository.save(garden);
                    return false;
                }
            } else{
                return garden.getHasRained();
            }
        } else {
            // If the garden cannot be found
            return false;
        }
    }

    /**
    * Clears the weather forecast data for a garden
    * This fixes the issue with the weather forecast not updating when the location is changed
    *
    * @param gardenId the gardenid of the garden to clear the forecast data for
    */
    @Transactional
    public void clearForecastData(Long gardenId) {
        Garden garden = gardenRepository.findById(gardenId).orElse(null);
        if (garden != null) {
            garden.setWeatherForecast(null);
            garden.setUpdatedWeather(null);
            garden.setUpdatedHasRained(null);
            gardenRepository.save(garden);
        }
    }

    /**
     * Takes a garden's location and makes a call to OpenWeatherAPI for the weather on that day, returns it as a weatherHistory object
     * @param lat latitude of the garden
     * @param lng longitude of the garden
     * @param date of the history to pull
     * @return Weather forecast for the garden
     */
    public WeatherHistory callHistoryApi(Double lat, Double lng, LocalDateTime date) {
        // Create the HTTP request

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://history.openweathermap.org/data/2.5/aggregated/day?lat=" + lat + LON_STRING+ lng + "&month="+date.getMonthValue()+"&day="+date.getDayOfMonth()+"&units=metric&appid="+WEATHER_KEY))
                .build();

        HttpResponse<String> response = null;

        LOG.info("run api call");

        try (HttpClient historyClient = HttpClient.newHttpClient()){ //Send the HTTP request and get the response
            response = historyClient.send(request, HttpResponse.BodyHandlers.ofString());
            // Convert the JSON response to a WeatherForecast object
            return Converter.convertWeatherJsonToObject(response.body(), WeatherHistory.class);
        } catch (IOException | InterruptedException e) {
            LOG.error("Failed to call forecast API.");
            if(e.getClass() == InterruptedException.class){
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

    /**
     * Dismisses incorrect alerts for a garden
     *
     * @param givenGarden The garden to dismiss alerts for
     */
    public void dismissIncorrectAlerts(Garden givenGarden){
        boolean isRaining = true;
        if(!getCurrentWeather(givenGarden.getLocation().getLat(), givenGarden.getLocation().getLon()).isRaining()) {
            isRaining = false;
            Alert alert = alertService.getAlertByType(givenGarden.getId(), Alert.DO_NOT_WATER);
            alert.dismissUntilTomorrow();
            alertService.setAlert(alert);
        }
        if(isRaining || getHasRained(givenGarden.getLocation().getLat(), givenGarden.getLocation().getLon(), givenGarden.getId())) {
            Alert alert = alertService.getAlertByType(givenGarden.getId(), Alert.NEED_WATER);
            alert.dismissUntilTomorrow();
            alertService.setAlert(alert);
        }
    }
}
