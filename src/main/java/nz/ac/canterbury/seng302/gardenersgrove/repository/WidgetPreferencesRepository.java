package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.WidgetPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository class for performing database CRUD operations on WidgetPreferences entities.
 */
@Repository
public interface WidgetPreferencesRepository extends JpaRepository<WidgetPreferences, Long> {

    /**
     * Given a user's userId, returns their WidgetPreferences
     * @param userId the ID of the user to find WidgetPreferences for
     * @return the user's widget preferences as a WidgetPreferences entity, or null if not found
     */
    @Query("SELECT p FROM WidgetPreferences p WHERE p.userId = ?1")
    WidgetPreferences findByUserId(Long userId);

    List<WidgetPreferences> findAll();

    /**
     * deletes widget preferences for a user, given by their user ID.
     * @param userId the ID of the user whose widget preferences should be deleted
     */
    void deleteByUserId(Long userId);
}

