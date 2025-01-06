package nz.ac.canterbury.seng302.gardenersgrove.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Converter {
    public static <T> T convertWeatherJsonToObject(String theJson, Class<T> clazz) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        T weather = null;

        try {
            weather = objectMapper.readValue(theJson, clazz);
        } catch (Exception e){
            e.printStackTrace();
        }

        return weather;
    }


    /**
     * Converts a JSON string to a list of {@link Location} objects.
     *
     * @param json the JSON string containing location data
     * @return a list of {@link Location} objects
     */
    public static List<Location> convertJsonToLocation(String json) {
        ObjectMapper mapper = new ObjectMapper();
        List<Location> locations = new ArrayList<>();
        try {
            Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
            List<Map<String, Object>> results = (List<Map<String, Object>>) jsonMap.get("results");

            for (Map<String, Object> result : results) {
                Location location = new Location();
                location.setCountry((String) result.get("country"));
                location.setPostcode((String) result.get("postcode"));
                location.setCity((String) (result.get("county") != null ? result.get("county") : result.get("state")));
                location.setStreet((result.get("housenumber") != null ? result.get("housenumber") + " ": "" ) + (result.get("street") != null ? result.get("street") : ""));
                location.setLat((Double) result.get("lat"));
                location.setLon((Double) result.get("lon"));
                location.setFormatted((String) result.get("formatted"));
                location.setAddress_line1((String) result.get("address_line1"));
                location.setAddress_line2((String) result.get("address_line2"));
                location.setSuburb((String) result.get("suburb"));
                locations.add(location);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return locations;
    }
}

