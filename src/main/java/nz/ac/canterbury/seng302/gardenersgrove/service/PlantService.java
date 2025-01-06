package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Plants, defined by the @link{Service} annotation.
 * This class links automatically with @link{PlantRepository}, see the @link{Autowired} annotation below
 */
@Service
public class PlantService {
    private PlantRepository plantRepository;

    // @Autowired
    public PlantService(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
    }
    /**
     * Gets all Plants from persistence
     * @return all Plants currently saved in persistence
     */
    public List<Plant> getPlants() {
        return plantRepository.findAll();
    }
    /**
     * Gets all Plants in a single Garden from persistence
     * @return all Plants currently saved in persistence where gardenId matches the parent garden's ID
     */
    public List<Plant> getPlantsInGarden(Long gardenId) {
        return plantRepository.findByGardenId(gardenId);
    }

    /**
     * Adds a Plant to persistence
     * @param plant object to persist
     * @return the saved Plant object
     */
    public Plant addPlant(Plant plant) {
        return plantRepository.save(plant);
    }

    /**
     * deletes a Plant from persistence
     * @param plant object to delete
     */
    public void removePlant(Plant plant) {
         plantRepository.delete(plant);
    }
    /**
     * deletes a Plant from persistence by ID
     * @param id ID of the Plant to delete
     */
    public void removePlantById(Long id) {
        plantRepository.deleteById(id);
    }

    /**
     * Retrieves a plant with a specific ID
     * @return all plants currently saved in persistence
     */
    public Optional<Plant> getPlant(Long id) {
        return plantRepository.findById(id);
    }
}

