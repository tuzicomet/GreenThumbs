package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities.WeatherForecast;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class WeatherServiceTest {
    @MockBean
    private PlantService plantService;
    @MockBean
    private GardenService gardenService;
    @MockBean
    private FileService fileService;
    @MockBean
    private static WeatherService weatherService;
    @MockBean
    private UserService userService;

    final Double VALID_LAT = 42.0;
    final Double VALID_LON = 172.0;

    @Test
    void getWeather_GardenIdDoesNotExist_ReturnsNull() {
        Mockito.when(gardenService.getGarden(Mockito.anyLong())).thenReturn(Optional.empty());
        assertNull(weatherService.getWeather(VALID_LAT, VALID_LON, 1, Locale.getDefault()));
    }

    @Test
    void getWeather_GardenLatIsInvalid_ReturnsNull() {
        WeatherForecast result = weatherService.getWeather(null, VALID_LON, 1, Locale.getDefault());
        assertNull(result);
    }

    @Test
    void getWeather_GardenLonIsInvalid_ReturnsNull() {
        WeatherForecast result = weatherService.getWeather(VALID_LAT, null, 1, Locale.getDefault());
        assertNull(result);
    }
}
