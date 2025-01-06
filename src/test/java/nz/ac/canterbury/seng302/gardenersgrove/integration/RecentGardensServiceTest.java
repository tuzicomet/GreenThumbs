package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.RecentGardens;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.RecentGardensRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.RecentGardensService;
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

class RecentGardensServiceTest {

    @Mock
    private RecentGardensRepository recentGardensRepository;
    @Mock
    private PlantService plantService;
    @Mock
    private GardenService gardenService;
    @InjectMocks
    private RecentGardensService recentGardensService;
    private User user;
    private Plant plant;
    private Plant plant2;
    private Garden garden;
    private Garden garden2;

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

        garden2 = new Garden(
            "Garden2",
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
                "Tomato",
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
    void userVisitsGarden_GardenHasNotBeenVisitedRecently_GardenSavedToRecentGardens() {
        when(recentGardensRepository.findByUserAndGarden(user, garden)).thenReturn(Optional.empty());
        recentGardensService.saveGardenVisit(user, garden);
        verify(recentGardensRepository, times(1)).save(any(RecentGardens.class));
    }

    @Test
    void userVisitsGarden_GardenHasBeenVisitedRecently_GardenNotSavedToRecentGardens() {
        RecentGardens existingVisit = new RecentGardens();
        existingVisit.setUser(user);
        existingVisit.setGarden(garden);
        existingVisit.setVisitDate(LocalDateTime.now().minusDays(1));
        when(recentGardensRepository.findByUserAndGarden(user, garden)).thenReturn(Optional.of(existingVisit));
        recentGardensService.saveGardenVisit(user, garden);
        verify(recentGardensRepository, times(1)).save(existingVisit);
    }

    @Test
    void userVisitsTwoGardens_UserHasNotVisitedGardensPreviously_BothGardensInRecentGardens() {
        RecentGardens recentGarden1 = new RecentGardens();
        recentGarden1.setGarden(garden);
        RecentGardens recentGarden2 = new RecentGardens();
        recentGarden2.setGarden(garden2);
        when(recentGardensRepository.findTop10ByUserOrderByVisitDateDesc(user))
                .thenReturn(Arrays.asList(recentGarden1, recentGarden2));
        List<Garden> result = recentGardensService.getRecentGardens(user);
        assertEquals(2, result.size());
        assertEquals("Garden", result.get(0).getName());
        assertEquals("Garden2", result.get(1).getName());
    }
}