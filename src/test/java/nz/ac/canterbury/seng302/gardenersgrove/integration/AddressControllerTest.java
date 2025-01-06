package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.AddressController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Location;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    // GardenRepository is required by GlobalControllerAdvice
    @MockBean
    private GardenRepository gardenRepository;

    @BeforeEach
    void setup() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        List<Location> locations = new ArrayList<>();
        locations.add(new Location(
                "Engineering Road, Riccarton, Christchurch 8041, New Zealand",
                "New Zealand",
                "Christchurch City",
                "Riccarton",
                "Engineering Road",
                "8041"));

        locations.add(new Location(
                "Engineering Road, Riccarton, Ilam 8041, New Zealand",
                "New Zealand",
                "Christchurch",
                "Riccarton",
                "Engineering Road",
                "8041"
        ));

        locations.add(new Location(
                "9 Engineering Road, Riccarton, Halswell-Hornby-Riccarton Community 8041, New Zealand",
                "New Zealand",
                "Christchurch City",
                "Riccarton",
                "9 Engineering Road",
                "8041"
        ));

        locations.add(new Location(
                "Len Lye, 9 Engineering Road, Riccarton, Halswell-Hornby-Riccarton Community 8041, New Zealand",
                "New Zealand",
                "Christchurch City",
                "Riccarton",
                "9 Engineering Road",
                "8041"
        ));

        when(locationService.fetchLocations(URLEncoder.encode("engineering road", StandardCharsets.UTF_8))).thenReturn(locations);
        when(locationService.fetchLocations("NotAnAddress")).thenReturn(new ArrayList<>());
    }

    @Test
    void GetAddress_ValidQuery_ReturnLocations() throws Exception {
        mockMvc.perform(get("/address/engineering road")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].formatted", is("Engineering Road, Riccarton, Christchurch 8041, New Zealand")))
                .andExpect(jsonPath("$[1].formatted", is("Engineering Road, Riccarton, Ilam 8041, New Zealand")))
                .andExpect(jsonPath("$[2].formatted", is("9 Engineering Road, Riccarton, Halswell-Hornby-Riccarton Community 8041, New Zealand")))
                .andExpect(jsonPath("$[3].formatted", is("Len Lye, 9 Engineering Road, Riccarton, Halswell-Hornby-Riccarton Community 8041, New Zealand")));
    }

    @Test
    void GetAddress_InvalidQuery_ReturnEmptyList() throws Exception {
        mockMvc.perform(get("/address/NotAnAddress")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void GetAddress_EmptyQuery_ReturnEmptyList() throws Exception {
        mockMvc.perform(get("/address/ ")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
