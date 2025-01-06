package nz.ac.canterbury.seng302.gardenersgrove.cucumber;


import io.cucumber.junit.platform.engine.Constants;
import io.cucumber.spring.CucumberContextConfiguration;
import nz.ac.canterbury.seng302.gardenersgrove.GardenersGroveApplication;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Location;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities.WeatherForecast;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.MailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.service.WeatherService;
import org.junit.platform.suite.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameters({
        @ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "nz.ac.canterbury.seng302.gardenersgrove.cucumber"),
        @ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,value = "pretty, html:target/cucumber-report/cucumber.html"),
        @ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
})
@ContextConfiguration(classes = GardenersGroveApplication.class)
@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("cucumber")
@AutoConfigureMockMvc
@MockBean(WeatherService.class)
@MockBean(MailService.class)
@MockBean(LocationService.class)
@MockBean(ProfanityFilterService.class)
public class RunCucumberTest {
    @Autowired
    public RunCucumberTest(WeatherService weatherService, MailService mailService, ProfanityFilterService profanityFilterService, LocationService locationService) {
        /*
         This constructor is run before every FEATURE, use it to set up mocks with their default behaviour.
         While the behaviour of the mocks can be adapted per test (see MockConfigurationSteps), creating the mocks
         initially should be done in this class, and their default behaviour configured here (see @MockBean above).

         Additionally, you can do other setup here that should be done the same for all tests, such as adding default
         users. If you want to get rid of any sample data in some cases, you can always write a Cucumber step to delete
         it, e.g., `Given no users already exist in the database` if you wanted to make sure there were no existing
         users for some particular feature.
        */
        Mockito.when(weatherService.getWeather(Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyLong(), Mockito.any(Locale.class)))
                .thenReturn(new WeatherForecast());

        // Make all the mail sender functions do nothing
        Mockito.doNothing().when(mailService).sendRegistrationEmail(Mockito.any(User.class), Mockito.any(Locale.class));
        Mockito.doNothing().when(mailService).sendResetTokenEmail(Mockito.anyString(), Mockito.anyString(), Mockito.any(Locale.class));
        Mockito.doNothing().when(mailService).sendConfirmationEmail(Mockito.anyString(), Mockito.any(Locale.class));
        Mockito.doNothing().when(mailService).sendPasswordChangeEmail(Mockito.any(User.class), Mockito.any(Locale.class));

        // Bypass the profanity check
        Mockito.when(profanityFilterService.containsProfanity(Mockito.anyString())).thenReturn(false);

        //Mock location service behaviour
        Map<String, Object> result = new HashMap<>();
        result.put("lon", -122.4194);
        result.put("lat", 37.7749);

        Mockito.when(locationService.fetchCoordinate(Mockito.any(Location.class))).thenReturn(result);
    }

}
