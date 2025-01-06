package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.BrowseGardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BrowseGardensController.class)
class BrowseGardensControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GardenService gardenService;

    @MockBean
    private UserService userService;

    @MockBean
    private GardenRepository gardenRepository;

    private BrowseGardensController controller;

    @BeforeEach
    void setup() {
        // Spring security linking mainly sourced from https://stackoverflow.com/questions/360520/unit-testing-with-spring-security
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        User user = new User(
                "Mock",
                "User",
                "test@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        user.setUserId(1L);

        Garden garden = new Garden(
                "Garden",
                "10",
                user,
                "Description",
                false,
                null,
                null,
                true,
                null
        );

        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(user);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        this.controller = new BrowseGardensController(gardenService, userService);
    }

    @Test
    void getAllGardensPage_NoSearchTerm_ReturnsGardens() throws Exception {
        List<Garden> gardens = new ArrayList<>();
        gardens.add(new Garden("Garden1", "100", null, "Description1", true, null, null, true, null));
        Page<Garden> gardenPage = new PageImpl<>(gardens);

        when(gardenService.getRecentPublicGardens(any(Pageable.class), any())).thenReturn(gardenPage);

        mockMvc.perform(get("/browseGardens")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("browseGardensTemplate"))
                .andExpect(model().attributeExists("browseGardens"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalItems"))
                .andExpect(model().attributeExists("totalPages"));
    }

    @Test
    void getAllGardensPage_WithSearchTerm_FindsGardens() throws Exception {
        List<Garden> gardens = new ArrayList<>();
        gardens.add(new Garden("Garden1", "100", null, "Description1", true, null, null, true, null));
        Page<Garden> gardenPage = new PageImpl<>(gardens);

        when(gardenService.getAllPublicGardensByName(eq("Garden1"), any(Pageable.class), any())).thenReturn(gardenPage);

        mockMvc.perform(get("/browseGardens")
                        .param("search", "Garden1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("browseGardensTemplate"))
                .andExpect(model().attributeExists("browseGardens"))
                .andExpect(model().attribute("browseGardens", gardens));
    }

    @Test
    void getAllGardensPage_WithSearchTerm_NoMatchingGardens() throws Exception {
        List<Garden> gardens = new ArrayList<>();
        Page<Garden> gardenPage = new PageImpl<>(gardens);

        when(gardenService.getAllPublicGardensByName(eq("NoMatch"), any(Pageable.class), any())).thenReturn(gardenPage);
        when(gardenService.getAllPublicGardensByPlantName(eq("NoMatch"), any(Pageable.class), any())).thenReturn(gardenPage);

        mockMvc.perform(get("/browseGardens")
                        .param("search", "NoMatch")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("browseGardensTemplate"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("message", "No gardens match your search"));
    }

    @Test
    void getAllGardensPage_ExceptionThrown_ReturnsErrorPage() throws Exception {
        when(gardenService.getRecentPublicGardens(any(Pageable.class), any())).thenThrow(new RuntimeException("Test exception"));


        mockMvc.perform(get("/browseGardens")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("message", "An error occurred: Test exception"));
    }

    @Test
    void getAllGardensPage_PageLessThanZero_RedirectsToFirstPage() throws Exception {
        mockMvc.perform(get("/browseGardens")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/browseGardens?tab=currentpage=0&size=10"));
    }

    @Test
    void getAllGardensPage_PageGreaterThanTotalPages_RedirectsToFirstPage() throws Exception {
        List<Garden> gardens = new ArrayList<>();
        Page<Garden> gardenPage = new PageImpl<>(gardens);

        when(gardenService.getRecentPublicGardens(any(Pageable.class), any())).thenReturn(gardenPage);

        mockMvc.perform(get("/browseGardens")
                        .param("page", "10")
                        .param("size", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/browseGardens?page=0&size=10"));
    }
}
