package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.RecentGardens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RecentGardens entity
 * 
 */
@Repository
public interface RecentGardensRepository extends JpaRepository<RecentGardens, Long> {

    List<RecentGardens>findTenRecentGardensByUser(AbstractUser user);

    Optional<RecentGardens> findByUserAndGarden(AbstractUser user, Garden garden);

    @Query("SELECT rg FROM RecentGardens rg WHERE rg.user = :user ORDER BY rg.visitDate DESC")
    List<RecentGardens> findTop10ByUserOrderByVisitDateDesc(@Param("user") AbstractUser user);

}
