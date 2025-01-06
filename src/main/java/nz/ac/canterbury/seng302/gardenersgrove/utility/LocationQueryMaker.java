package nz.ac.canterbury.seng302.gardenersgrove.utility;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for making a query string from a Location
 * object for use with the Geoapify API.
 * The query string is built using only the available location parameters.
 */
public class LocationQueryMaker {

    private static final Logger LOG = LoggerFactory.getLogger(LocationQueryMaker.class);

    /**
     * Generates a query string from the given Location object, that can be
     * used in a Geoapify API request.
     * @param location The Location object containing the address details.
     * @return A query string with URL-encoded parameters
     */
    public static String generateQuery(Location location) {
        // Geoapify API's accepted request parameters can be viewed here:
        // https://apidocs.geoapify.com/docs/geocoding/forward-geocoding/
        // NOTE: although suburb is not listed, it does work
        String street = location.getStreet();
        String suburb = location.getSuburb();
        String postcode = location.getPostcode();
        String city = location.getCity();
        String country = location.getCountry();

        // StringBuilder provides an efficient way to build strings
        StringBuilder stringBuilder = new StringBuilder();

        // Try to add each possible field/parameter to the query
        // Any which have no value will not be added
        // (the keys here match Geoapify API's request parameters)
        appendParameter(stringBuilder, "street", street);
        appendParameter(stringBuilder, "suburb", suburb);
        appendParameter(stringBuilder, "postcode", postcode);
        appendParameter(stringBuilder, "city", city);
        appendParameter(stringBuilder, "country", country);

        LOG.info(stringBuilder.toString());

        // Convert the final query to a string and return
        return stringBuilder.toString();
    }

    /**
     * Appends a key-value pair to the query string, in a way that can be
     * used in a Geoapify API request. The key is the field to add to the query
     * and the value is the field's value. Only appends if
     * the field has a value (not null or empty string).
     * @param stringBuilder The StringBuilder used to construct the query string.
     * @param key     The location field name/column.
     * @param value   The location field value.
     */
    private static void appendParameter(StringBuilder stringBuilder, String key, String value) {
        // only append if the field value is not empty
        if (value != null && !value.trim().isEmpty()) {
            // if the stringBuilder is not empty (i.e. the query is empty,
            // meaning that another parameter has already been appended)
            if (!stringBuilder.isEmpty()) {
                // then first put an & before appending this parameter
                stringBuilder.append("&");
            }
            // append the parameter to the query using the format key=value
            stringBuilder.append(String.format("%s=%s", key, URLEncoder.encode(value, StandardCharsets.UTF_8)));
        }
    }
}
