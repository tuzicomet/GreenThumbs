package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Location;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * This is a Rest Controller that handle HTTP requests for location suggestions by act as a bridge
 * between the front-end of this application and the API
 *
 */
@RestController
public class AddressController {
    private static final Logger LOG = LoggerFactory.getLogger(AddressController.class);

    private final LocationService locationService;

    @Autowired
    public AddressController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Listen the HTTP get request for fetching location suggestions
     * Then send the query to @locationService and fetching the suggestions from Geoapify API
     *
     * @param q The query for location suggestions
     * @return List of location entities as a Json object
     */
    @GetMapping("/address/{q}")
    @RateLimiter
    public List<Location> fetchAddress(@PathVariable("q") String q) {
        LOG.info("Fetching address");
        if (q.trim().isEmpty()) {
            return Collections.emptyList();
        }
        q = q.replace("thisisaslash123", "%2F");
        List<Location> location = locationService.fetchLocations(URLEncoder.encode(q, StandardCharsets.UTF_8));
        if (location.isEmpty()) {
            return Collections.emptyList();
        }
        return location;
    }
}
