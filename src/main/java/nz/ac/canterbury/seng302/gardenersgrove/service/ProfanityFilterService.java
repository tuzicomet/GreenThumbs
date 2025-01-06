package nz.ac.canterbury.seng302.gardenersgrove.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Service class that handles interaction with our Azure Content Moderator endpoint
 * Contains functionality for checking strings for profanity
 */
@Service
public class ProfanityFilterService {
    private static final Logger LOG = LoggerFactory.getLogger(ProfanityFilterService.class);
    private final String apiUrl = System.getenv("AZURE_ENDPOINT");
    private final String apiKey = System.getenv("AZURE_KEY");

    /**
     * Checks if the given string contains any inappropriate words
     * @param text The text to be checked
     * @return true if the given text contains inappropriate words, false otherwise.
     */
    public boolean containsProfanity(String text) {
        // if text is empty, or contains only spaces
        if (text.isBlank()) {
            return false;
        }

        // Send a POST request to the API
        try {
            return sendProfanityRequest(text);
        } catch (HttpClientErrorException e){
            LOG.info("Content Moderator Request Failed - Trying again");
            // If the request fails, wait 1 second and try again
            try{
                Thread.sleep(1010);
                return sendProfanityRequest(text);
            } catch(HttpClientErrorException | InterruptedException err) {
                LOG.info("Profanity Filter Unsuccessful");
                if (err instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return true;
    }

    /**
     * Send a request to the profanity API to see if a set of text contains profanity
     * @param text the text that will be moderated
     * @return true if the text contains profanity, false otherwise
     * @throws HttpClientErrorException if the API connection failed
     */
    public boolean sendProfanityRequest(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "text/plain");
        headers.set("Ocp-Apim-Subscription-Key", apiKey);
        HttpEntity<String> requestEntity = new HttpEntity<>(text, headers);

        String reqUrl = apiUrl + "/contentmoderator/moderate/v1.0/ProcessText/Screen/";
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject(reqUrl, requestEntity, String.class);
        if (response == null) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(400));
        }
        return !response.contains("\"Terms\":null");
    }

    /**
     * Verifies the content of a tag with azure content moderator
     * @param content the string to verify
     * @return true if the tag is valid, false if it is profane
     * @throws HttpClientErrorException thrown if the request fails, and caught in the gardenService method addTag
     */
    public boolean verifyTag(String content) throws HttpClientErrorException {
        // if text is empty, or contains only spaces
        if (content.isBlank()) {
            return true;
        }
        boolean containsProfanity = sendProfanityRequest(content);
        return !containsProfanity;
    }
}