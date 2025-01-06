package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.RecentPlants;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.RecentPlantsRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.RecentPlantsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RecentPlantsServiceTest {

    @Mock
    private RecentPlantsRepository recentPlantsRepository;
    @Mock
    private PlantService plantService;
    @Mock
    private GardenService gardenService;
    @InjectMocks
    private RecentPlantsService recentPlantsService;
    private User user;
    private Plant plant;
    private Plant plant2;
    private Garden garden;

    @BeforeEach
    void setUpEach() {
        MockitoAnnotations.openMocks(this);

        user = new User(
                "Mock",
                "User",
                "test@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        user.setUserId(1L);

        garden = new Garden(
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

        plant = new Plant(
                garden,
                "Carrot",
                "1",
                "Certainly orange",
                LocalDate.parse("1990-01-01"),
                null
        );

        plant2 = new Plant(
            garden,
            "tomato",
            "1",
            "Certainly red",
            LocalDate.parse("1990-01-01"),
            null
        );

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        when(gardenService.getGarden(1L)).thenReturn(Optional.of(garden));
        when(plantService.getPlantsInGarden(1L)).thenReturn(List.of(plant, plant2));
        when(plantService.getPlant(1L)).thenReturn(Optional.of(plant));
    }

    @Test
    void testSavePlantVisit_NewVisit() {
        when(recentPlantsRepository.findByUserAndPlant(user, plant)).thenReturn(Optional.empty());
        recentPlantsService.savePlantVisit(user, plant);
        verify(recentPlantsRepository, times(1)).save(any(RecentPlants.class));
    }

    @Test
    void testSavePlantVisit_ExistingVisit() {
        RecentPlants existingVisit = new RecentPlants();
        existingVisit.setUser(user);
        existingVisit.setPlant(plant);
        existingVisit.setVisitDate(LocalDateTime.now().minusDays(1));
        when(recentPlantsRepository.findByUserAndPlant(user, plant)).thenReturn(Optional.of(existingVisit));
        recentPlantsService.savePlantVisit(user, plant);
        verify(recentPlantsRepository, times(1)).save(existingVisit);
    }

    @Test
    void testGetRecentPlants() {
        RecentPlants recentPlant1 = new RecentPlants();
        recentPlant1.setPlant(plant);
        RecentPlants recentPlant2 = new RecentPlants();
        recentPlant2.setPlant(plant2);
        when(recentPlantsRepository.findTop10ByUserOrderByVisitDateDesc(user))
                .thenReturn(Arrays.asList(recentPlant1, recentPlant2));
        List<Plant> result = recentPlantsService.getRecentPlants(user);
        assertEquals(2, result.size());
        assertEquals("Carrot", result.get(0).getName());
        assertEquals("tomato", result.get(1).getName());
    }
}