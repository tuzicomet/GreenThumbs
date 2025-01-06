package nz.ac.canterbury.seng302.gardenersgrove.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Location;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LocationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.utility.Converter;
import nz.ac.canterbury.seng302.gardenersgrove.utility.LocationQueryMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling operations related to {@link Location}.
 * This class interacts with external geocoding APIs and the location repository
 * to fetch, save, and manipulate location data.
 */
@Service
public class LocationService {
    private static final Logger LOG = LoggerFactory.getLogger(LocationService.class);
    private final LocationRepository locationRepository;
    private static final String LOCATION_KEY = System.getenv("LOCATION_KEY");
    private static final String LOCATION_URL = "https://api.geoapify.com/v1/geocode/autocomplete?";
    private static final String SEARCH_URL = "https://api.geoapify.com/v1/geocode/search?";

    /**
     * Constructor for {@code LocationService}.
     *
     * @param locationRepository the repository to interact with location data
     */
    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    /**
     * Sends an HTTP GET request to the specified URL with the provided query parameters and returns the response body as a string.
     * This method constructs an HTTP request with a query parameter for the location and sends it to the specified URL.
     * The response from the server is then returned as a string. If an exception occurs during the request, it will be printed to the standard error stream,
     * and the method will return {@code null}.
     *
     * @param query The query parameter for the location to be included in the request. It will be URL-encoded and included in the request URL.
     * @param url The base URL to which the request is sent. The URL should be formatted such that it includes placeholders for the query parameter and other necessary parts.
     * @return The body of the HTTP response as a string. If an error occurs during the request, {@code null} is returned.
     */
    public String sendRequest(String query, String url) {
        try (HttpClient client = HttpClient.newHttpClient()){
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%stext=%s&format=json&apiKey=%s&limit=5", url, query, LOCATION_KEY)))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.body();
        } catch (IOException | InterruptedException e) {
            LOG.info("Failed to send address auto-complete request");
            if(e.getClass() == InterruptedException.class){
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

    /**
     * Fetches a list of locations matching the given text query from the external geocoding API.
     *
     * @param query the text query to search for locations
     * @return a list of {@code Location} objects matching the query, or {@code null} if an error occurs
     */
    public List<Location> fetchLocations(String query) {
        String res = sendRequest(query, LOCATION_URL);
        if (res != null) {
            return Converter.convertJsonToLocation(res);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Fetches the coordinates (latitude and longitude) for the given location.
     *
     * @param location the location object for which coordinates are to be fetched
     * @return the updated {@code Location} object with coordinates set, or the original location if an error occurs
     */
    public Map<String, Object> fetchCoordinate(Location location) {
        String query = "";

        // Determine if the input contains a street name
        boolean isBroadSearch = (location.getStreet() == null || location.getStreet().isEmpty());

        if ((location.getCity() == null || location.getCity().isEmpty()) || (location.getCountry() == null || location.getCountry().isEmpty())) {
            return null;
        }

        if (location.getFormatted() != null && !location.getFormatted().isEmpty()) {
            String formattedLocation = location.getFormatted();
            String encodedLocation = URLEncoder.encode(formattedLocation, StandardCharsets.UTF_8);

            if (isBroadSearch) {
                // Broad search (e.g., city, town)
                query = String.format("%s&type=city", encodedLocation);
            } else {
                // Specific search (e.g., street-level)
                query = encodedLocation;
            }
        } else {
            // Fallback if formatted address isn't available
            query = LocationQueryMaker.generateQuery(location);
        }

        // Send the request to the API
        String res = sendRequest(query, SEARCH_URL);

        if (res == null) {
            return null;
        }

        // Parse the API response
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> jsonMap = mapper.readValue(res, Map.class);
            List<Map<String, Object>> results = (List<Map<String, Object>>) jsonMap.get("results");

            if (results.isEmpty()) {
                return null;
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("lat", results.get(0).get("lat"));
                result.put("lon", results.get(0).get("lon"));
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Saves the given location to the repository.
     *
     * @param location the location object to save
     * @return the saved {@code Location} object
     */
    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }
}
