package nz.ac.canterbury.seng302.gardenersgrove.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.io.Serializable;

/**
 * Entity class representing a Location.
 * This class is mapped to the "LOCATION" table in the database and includes various fields such as postcode, state, country, etc.
 * It is also serialized to JSON with non-null properties included.
 */
@Entity
@Table(name = "LOCATION")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Location implements Serializable {

    /**
     * The unique identifier for the location.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long locationId;

    /**
     * The postcode of the location.
     */
    @Column
    @JsonProperty("postcode")
    private String postcode;

    /**
     * The state of the location.
     */
    @Column
    @JsonProperty("state")
    private String state;

    /**
     * The country of the location. This field is mandatory.
     */
    @Column(nullable = false)
    @JsonProperty("country")
    private String country;

    /**
     * The city of the location. This field is mandatory.
     */
    @Column(nullable = false)
    @JsonProperty("city")
    private String city;

    /**
     * The suburb of the location.
     */
    @Column
    @JsonProperty("suburb")
    private String suburb;

    /**
     * The street of the location.
     */
    @Column
    @JsonProperty("street")
    private String street;

    /**
     * The house number of the location.
     */
    @Column
    @JsonProperty("housenumber")
    private String houseNumber;

    /**
     * The latitude of the location.
     */
    @Column
    @JsonProperty("lat")
    private Double lat;

    /**
     * The longitude of the location.
     */
    @Column
    @JsonProperty("lon")
    private Double lon;

    /**
     * The formatted address of the location.
     */
    @Column
    @JsonProperty("formatted")
    private String formatted;

    /**
     * The first line of the address.
     */
    @Column
    @JsonProperty("address_line1")
    private String address_line1;

    /**
     * The second line of the address.
     */
    @Column
    @JsonProperty("address_line2")
    private String address_line2;



    /**
     * Default constructor.
     */
    public Location() {}

    /**
     * Parameterized constructor to create a Location instance with the specified details.
     *
     * @param formatted     formatted representation of the location
     * @param country The country of the location.
     * @param city The city of the location.
     * @param suburb The suburb of the location.
     * @param street The street of the location.
     * @param postcode The postcode of the location.
     */
    public Location(String formatted, String country, String city, String suburb, String street, String postcode) {
        this.formatted = formatted;
        this.country = country;
        this.city = city;
        this.suburb = suburb;
        this.street = street;
        this.postcode = postcode;
    }

    // Getters and setters with Javadoc comments

    /**
     * Gets the unique identifier for the location.
     *
     * @return The location ID.
     */
    public long getLocationId() {
        return locationId;
    }

    /**
     * Sets the unique identifier for the location.
     *
     * @param locationId The location ID to set.
     */
    public void setLocationId(long locationId) {
        this.locationId = locationId;
    }

    /**
     * Gets the postcode of the location.
     *
     * @return The postcode.
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Sets the postcode of the location.
     *
     * @param postcode The postcode to set.
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * Gets the state of the location.
     *
     * @return The state.
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state of the location.
     *
     * @param state The state to set.
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Gets the country of the location.
     *
     * @return The country.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country of the location.
     *
     * @param country The country to set.
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets the city of the location.
     *
     * @return The city.
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city of the location.
     *
     * @param city The city to set.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Gets the suburb of the location.
     *
     * @return The suburb.
     */
    public String getSuburb() {
        return suburb;
    }

    /**
     * Sets the suburb of the location.
     *
     * @param suburb The suburb to set.
     */
    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    /**
     * Gets the street of the location.
     *
     * @return The street.
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the street of the location.
     *
     * @param street The street to set.
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Gets the house number of the location.
     *
     * @return The house number.
     */
    public String getHouseNumber() {
        return houseNumber;
    }

    /**
     * Sets the house number of the location.
     *
     * @param houseNumber The house number to set.
     */
    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    /**
     * Gets the latitude of the location.
     *
     * @return The latitude.
     */
    public Double getLat() {
        return lat;
    }

    /**
     * Sets the latitude of the location.
     *
     * @param lat The latitude to set.
     */
    public void setLat(Double lat) {
        this.lat = lat;
    }

    /**
     * Gets the longitude of the location.
     *
     * @return The longitude.
     */
    public Double getLon() {
        return lon;
    }

    /**
     * Sets the longitude of the location.
     *
     * @param lon The longitude to set.
     */
    public void setLon(Double lon) {
        this.lon = lon;
    }

    /**
     * Gets the formatted address of the location.
     *
     * @return The formatted address.
     */
    public String getFormatted() {
        return formatted;
    }

    /**
     * Sets the formatted address of the location.
     *
     * @param formatted The formatted address to set.
     */
    public void setFormatted(String formatted) {
        this.formatted = formatted;
    }

    /**
     * Gets the first line of the address.
     *
     * @return The first line of the address.
     */
    public String getAddress_line1() {
        return address_line1;
    }

    /**
     * Sets the first line of the address.
     *
     * @param address_line1 The first line of the address to set.
     */
    public void setAddress_line1(String address_line1) {
        this.address_line1 = address_line1;
    }

    /**
     * Gets the second line of the address.
     *
     * @return The second line of the address.
     */
    public String getAddress_line2() {
        return address_line2;
    }

    /**
     * Sets the second line of the address.
     *
     * @param address_line2 The second line of the address to set.
     */
    public void setAddress_line2(String address_line2) {
        this.address_line2 = address_line2;
    }

    @Override
    public String toString() {
        String string = "";
        if ((houseNumber != null) && (!houseNumber.isEmpty()) && (!houseNumber.equals("N/A"))) {
            string = string + houseNumber + ", ";
        }
        if ((street != null) && (!street.isEmpty())) {
            string = string + street + ", ";
        }
        if ((suburb != null) && (!suburb.isEmpty())) {
            string = string + suburb + ", ";
        }
        if ((city != null) && (!city.isEmpty())) {
            string = string + city + ", ";
        }
        if((country != null) && (!country.isEmpty()))  {
            string = string + country;
        }
        return string;
    }
}
