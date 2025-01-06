package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Location repository accessor using Spring's @Link{CurdRepository}
 *
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

}
