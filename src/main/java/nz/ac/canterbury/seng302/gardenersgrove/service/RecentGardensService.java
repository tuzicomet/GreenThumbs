package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.RecentGardens;
import nz.ac.canterbury.seng302.gardenersgrove.repository.RecentGardensRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecentGardensService {
    @Autowired
    private RecentGardensRepository recentGardensRepository;

    /**
     * RecentGardens constructor
     * 
     * @param recentGardensRepository the repository used
     */
    public RecentGardensService(RecentGardensRepository recentGardensRepository) {
        this.recentGardensRepository = recentGardensRepository;
    }

    /**
     * Saves a garden visit for a user
     * if the user has visited the garden before, the visit date is updated
     * 
     * @param user the user visiting the garden
     * @param garden the garden being visited
     */
    public void saveGardenVisit(AbstractUser user, Garden garden) {
    Optional<RecentGardens> existingVisit = recentGardensRepository.findByUserAndGarden(user, garden);
    if (existingVisit.isPresent()) {
        RecentGardens recentGarden = existingVisit.get();
        recentGarden.setVisitDate(LocalDateTime.now());
        recentGardensRepository.save(recentGarden);
    } else {
        RecentGardens recentGarden = new RecentGardens();
        recentGarden.setUser(user);
        recentGarden.setGarden(garden);
        recentGarden.setVisitDate(LocalDateTime.now());
        recentGardensRepository.save(recentGarden);
    }
    }

    /**
     * Retrieves the top 10 most recent gardens visited by a user
     * 
     * @param user the user whose recent gardens are to be retrieved
     * @return a list of the top 10 most recent gardens visited by the user
     */
    public List<Garden> getRecentGardens(AbstractUser user) {
        List<RecentGardens> recentGardensList = recentGardensRepository.findTop10ByUserOrderByVisitDateDesc(user);
        List<Garden> gardens = new ArrayList<>();
        for (RecentGardens recentGarden : recentGardensList) {
            gardens.add(recentGarden.getGarden());
        }
        return gardens;
    }
}
