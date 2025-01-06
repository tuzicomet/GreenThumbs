package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Alert;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for performing CRUD operations on Alert entities.
 */
@Repository
public interface AlertRepository extends CrudRepository<Alert, Long> {

    List<Alert> findByGardenIdAndDismissedUntilBefore(long gardenId, Instant queryTime);

    List<Alert> findByGardenId(long gardenId);

    Alert findAlertById(long alertId);
    
    Alert findByGardenIdAndType(long gardenId, int type);
}
