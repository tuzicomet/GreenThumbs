package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.RecentPlants;
import nz.ac.canterbury.seng302.gardenersgrove.repository.RecentPlantsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecentPlantsService {
    @Autowired
    private RecentPlantsRepository recentPlantsRepository;

    /**
     * RecentPlants constructor
     * 
     * @param recentPlantsRepository the repository to be used by this service
     */
    public RecentPlantsService(RecentPlantsRepository recentPlantsRepository) {
        this.recentPlantsRepository = recentPlantsRepository;
    }

    /**
     * Saves a plant visit for a user
     * If the user has already visited the plant, the visit date is updated
     * 
     * @param user the user visiting the plant
     * @param plant the plant being visited
     */
    public void savePlantVisit(AbstractUser user, Plant plant) {
    Optional<RecentPlants> existingVisit = recentPlantsRepository.findByUserAndPlant(user, plant);
    if (existingVisit.isPresent()) {
        RecentPlants recentPlant = existingVisit.get();
        recentPlant.setVisitDate(LocalDateTime.now());
        recentPlantsRepository.save(recentPlant);
    } else {
        RecentPlants recentPlant = new RecentPlants();
        recentPlant.setUser(user);
        recentPlant.setPlant(plant);
        recentPlant.setVisitDate(LocalDateTime.now());
        recentPlantsRepository.save(recentPlant);
    }
    }

    /**
     * Retrieves the top 10 most recent plants visited by a user
     * 
     * @param user the user whose recent plants are to be retrieved
     * @return a list of the top 10 most recent plants visited by the user
     */
    public List<Plant> getRecentPlants(AbstractUser user) {
        List<RecentPlants> recentPlantsList = recentPlantsRepository.findTop10ByUserOrderByVisitDateDesc(user);
        List<Plant> plants = new ArrayList<>();
        for (RecentPlants recentPlant : recentPlantsList) {
            plants.add(recentPlant.getPlant());
        }
        return plants;
    }
}
