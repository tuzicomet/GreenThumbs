package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.RecentPlants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentPlantsRepository extends JpaRepository<RecentPlants, Long> {

    List<RecentPlants>findTenRecentPlantsByUser(AbstractUser user);

    Optional<RecentPlants> findByUserAndPlant(AbstractUser user, Plant plant);

    @Query("SELECT rg FROM RecentPlants rg WHERE rg.user = :user ORDER BY rg.visitDate DESC")
    List<RecentPlants> findTop10ByUserOrderByVisitDateDesc(@Param("user") AbstractUser user);

}
