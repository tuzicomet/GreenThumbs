package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Location;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LocationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Unit tests for the LocationService class.
 */
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationService locationService;

    /**
     * Sets up the mocks and initializes the LocationService before each test.
     */
    @BeforeEach
    void setUp() {
        locationRepository = Mockito.mock(LocationRepository.class);
        locationService = new LocationService(locationRepository);
    }

    /**
     * Tests fetching coordinates with a valid result.
     * Verifies that the returned coordinates match the expected values.
     */
    @Test
    void fetchCoordinate_validResultWithBroadSearch_returnCoordinate() {
        String mockResponseBody = "{\"results\": [" +
                "{\"lat\": 1.0, \"lon\": 2.0}" +
                "]}";

        LocationService spyLocationService = spy(locationService);
        doReturn(mockResponseBody).when(spyLocationService).sendRequest(any(), any());

        Location location = new Location("Methven 7730, New Zealand", "New Zealand", "Ashburton District", "", "", "");
        Map<String, Object> coordinate = spyLocationService.fetchCoordinate(location);
        assertEquals(2.0, coordinate.get("lon"));
        assertEquals(1.0, coordinate.get("lat"));
    }

    @Test
    void fetchCoordinate_validResultWithSpecificSearch_returnCoordinate() {
        String mockResponseBody = "{\"results\": [" +
                "{\"lat\": 1.0, \"lon\": 2.0}" +
                "]}";

        LocationService spyLocationService = spy(locationService);
        doReturn(mockResponseBody).when(spyLocationService).sendRequest(any(), any());

        Location location = new Location("123 Main Street, Methven 7730, New Zealand", "New Zealand", "Ashburton District", "Methven", "Main Street", "7730");
        Map<String, Object> coordinate = spyLocationService.fetchCoordinate(location);
        assertEquals(2.0, coordinate.get("lon"));
        assertEquals(1.0, coordinate.get("lat"));
    }

    /**
     * Tests fetching coordinates with a null result.
     * Verifies that the returned coordinates are null.
     */
    @Test
    void fetchCoordinate_nullResult_returnNull() {
        LocationService spyLocationService = spy(locationService);
        doReturn(null).when(spyLocationService).sendRequest(any(), any());

        Location location = new Location("Methven 7730, New Zealand", "New Zealand", "Ashburton District", "", "", "");
        Map<String, Object> coordinate = spyLocationService.fetchCoordinate(location);
        assertNull(coordinate);
    }

    /**
     * Tests fetching coordinates with an empty result.
     * Verifies that the returned coordinates are null.
     */
    @Test
    void fetchCoordinate_emptyResult_returnNull() {
        String mockResponseBody = "{\"results\": []}";

        LocationService spyLocationService = spy(locationService);
        doReturn(mockResponseBody).when(spyLocationService).sendRequest(any(), any());

        Location location = new Location("Methven 7730, New Zealand", "New Zealand", "Ashburton District", "", "", "");
        Map<String, Object> coordinate = spyLocationService.fetchCoordinate(location);
        assertNull(coordinate);
    }

    /**
     * Tests fetching coordinates with missing city or country.
     * Verifies that the returned coordinates are null.
     */
    @Test
    void fetchCoordinate_missingCityOrCountry_returnNull() {
        LocationService spyLocationService = spy(locationService);

        Location locationWithoutCity = new Location("Methven 7730, New Zealand", "New Zealand", "", "", "", "");
        Map<String, Object> coordinateWithoutCity = spyLocationService.fetchCoordinate(locationWithoutCity);
        assertNull(coordinateWithoutCity);

        Location locationWithoutCountry = new Location("Methven 7730, New Zealand", "", "Ashburton District", "", "", "");
        Map<String, Object> coordinateWithoutCountry = spyLocationService.fetchCoordinate(locationWithoutCountry);
        assertNull(coordinateWithoutCountry);
    }

    /**
     * Tests fetching locations with a valid response.
     * Verifies that the returned list of locations is correct.
     */
    @Test
    void fetchLocations_validResponse_returnListOfLocations() {
        String mockResponseBody = "{\"results\": [" +
                "{\"lat\": 1.0, \"lon\": 2.0, \"formatted\": \"Methven 7730, New Zealand\"}," +
                "{\"lat\": 3.0, \"lon\": 4.0, \"formatted\": \"Ashburton District, New Zealand\"}" +
                "]}";

        LocationService spyLocationService = spy(locationService);
        doReturn(mockResponseBody).when(spyLocationService).sendRequest(any(), any());

        List<Location> locations = spyLocationService.fetchLocations("Methven");
        assertEquals(2, locations.size());
        assertEquals("Methven 7730, New Zealand", locations.get(0).getFormatted());
        assertEquals("Ashburton District, New Zealand", locations.get(1).getFormatted());
    }

    /**
     * Tests fetching locations with an empty response.
     * Verifies that the returned list of locations is empty.
     */
    @Test
    void fetchLocations_emptyResponse_returnListOfLocations() {
        String mockResponseBody = "{\"results\": []}";

        LocationService spyLocationService = spy(locationService);
        doReturn(mockResponseBody).when(spyLocationService).sendRequest(any(), any());

        List<Location> locations = spyLocationService.fetchLocations("Methven");
        assertEquals(0, locations.size());
    }

    /**
     * Tests fetching locations with a null response.
     * Verifies that the returned list of locations is empty.
     */
    @Test
    void fetchLocations_nullResponse_returnListOfLocations() {
        LocationService spyLocationService = spy(locationService);
        doReturn(null).when(spyLocationService).sendRequest(any(), any());

        List<Location> locations = spyLocationService.fetchLocations("Methven");
        assertEquals(0, locations.size());
    }
}

