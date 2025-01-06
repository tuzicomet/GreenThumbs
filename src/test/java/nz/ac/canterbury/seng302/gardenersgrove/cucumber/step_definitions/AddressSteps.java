package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Location;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class AddressSteps {
    @Autowired
    MockMvc mockMvc;
    private MvcResult result;
    @Autowired
    MessageSource messageSource;

    // Autowired will pull the mock service setup in RunCucumberTests
    @Autowired
    LocationService locationService;

    @Before
    public void setupMocks() {
        List<Location> suggestions = List.of(
                new Location("123 Test St, Test City, Test Country", "Test Country", "Test City", "Test Suburb", "Test St", "123"),
                new Location("456 Example Rd, Example City, Example Country", "Example Country", "Example City", "Example Suburb", "Example Rd", "456")
        );
        when(locationService.fetchLocations(anyString())).thenReturn(suggestions);
    }

    @Given("I am on the create new garden form")
    public void iAmOnTheCreateNewGardenForm() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get("/garden"))
                .andExpect(status().isOk())
                .andExpect(view().name("gardenFormTemplate"))
                .andReturn();
    }

    @When("I submit the form without providing a city and country")
    public void iSubmitTheFormWithoutProvidingCityAndCountry() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.post("/garden")
                        .param("name", "Test Garden")
                        .param("size", "100")
                        .param("description", "A test garden")
                        .param("location", "")
                        .param("country", "")
                        .param("city", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("an error message tells me \"City and Country are required\"")
    public void anErrorMessageTellsMeCityAndCountryAreRequired() {
        List<String> errors = (List<String>) result.getModelAndView().getModel().get("errors");
        assertEquals("City cannot be empty", errors.get(2));
        assertEquals("Country cannot be empty", errors.get(1));

    }

    @When("I start typing a location {string}")
    public void iStartTypingALocation(String locationQuery) throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get("/address/" + locationQuery))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I receive reasonable suggestions of locations matching the current entry I have provided")
    public void iReceiveReasonableSuggestionsOfLocationsMatchingTheCurrentEntryIHaveProvided() throws Exception {
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("123 Test St, Test City, Test Country"));
        assertTrue(content.contains("456 Example Rd, Example City, Example Country"));
    }

    @Given("there are no matching address suggestions for my current entry")
    public void thereAreNoMatchingAddressSuggestionsForMyCurrentEntry() throws Exception {
        // Simulate a delay to avoid hitting the rate limit
        Thread.sleep(1000); // sleep for 1 second
        when(locationService.fetchLocations(anyString())).thenReturn(Collections.emptyList());

        result = mockMvc.perform(MockMvcRequestBuilders.get("/address/NoMatchlkjjlk"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I am shown the message \"No matching location found, location-based services may not work\"")
    public void iAmShownTheMessageNoMatchingLocationFoundLocationBasedServicesMayNotWork() throws Exception {
        String content = result.getResponse().getContentAsString();
        // This being [] means the front end will display the message, not sure how to check that.
        assertTrue(content.contains("[]"));
    }

}
