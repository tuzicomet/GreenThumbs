package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TagController {

    private final GardenService gardenService;
    private static final Logger LOG = LoggerFactory.getLogger(TagController.class);

    public TagController(GardenService gardenService) {
        this.gardenService = gardenService;
    }

    /**
     * Get mapping to retrieve the autocomplete suggestions for a given input
     * Used to provide a connection between our javascript and the backend
     * @param query the current input in the tag field, for which matches must be found
     * @return The five closest matches to the input string from existing tags, in order of length ascending
     */
    @GetMapping("/tags/autocomplete")
    public List<String> getAutocompleteSuggestions(@RequestParam String query) {
        LOG.info("/GET /tags/autocomplete");
        return gardenService.getAutocompleteTags(query);
    }
}