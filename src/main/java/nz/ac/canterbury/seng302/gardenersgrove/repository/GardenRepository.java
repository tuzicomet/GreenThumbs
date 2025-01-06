package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities.WeatherForecast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Garden repository accessor using Spring's @link{CrudRepository}.
 * These (basic) methods are provided for us without the need to write our own implementations
 */
@Repository
public interface GardenRepository extends JpaRepository<Garden, Long> {
    Optional<Garden> findById(long id);

    /**
     * Gets a list of the gardens owned by the user.
     * @param userId
     * @return list of gardens from that owner
     */
    @Query("SELECT o.gardens FROM AbstractUser o WHERE o.userId = ?1")
    List<Garden> findAllByOwnerId(Long userId);

    /**
     * Gets a list of the gardens owned by the user, ordered by garden id, in descending order (newest first)
     * @param userId the id of the user to retrieve the list of gardens for
     * @return list of gardens from that owner, ordered by descending id
     */
    @Query("SELECT o.gardens FROM AbstractUser o JOIN o.gardens g WHERE o.userId = ?1 ORDER BY g.id DESC")

    List<Garden> findAllByOwnderIdOrderByIdDesc(Long userId);


    /**
     * Gets the weatherforcast object from the garden ID
     * @param id
     * @return the weatherforcast for that garden
     */
    @Query("SELECT g.weatherForecast FROM Garden g WHERE g.id = ?1")
    WeatherForecast findWeatherById(long id);

    /**
     * Finds gardens that have rained by their ID
     * @param id
     * @return boolean indicating if it has rained.
     */
    @Query("SELECT g.hasRained FROM Garden g WHERE g.id = ?1")
    boolean findHasRainedById(long id);


    /**
     * Gets all of the public gardens
     * @param pageable
     * @return A list of the gardens that is pageable.
     */
    @Query("SELECT g FROM Garden g WHERE g.publicised = true")
    Page<Garden> findAllPublicGardens(Pageable pageable);

    /**
     * Finds the public gardens by their name
     * @param name
     * @param pageable
     * @return returns a list of the gardens that is pageable.
     */
    @Query("SELECT g FROM Garden g WHERE g.publicised = true AND LOWER(g.name) LIKE LOWER(CONCAT('%', ?1, '%'))")
    Page<Garden> findPublicGardensByName(String name, Pageable pageable);

    /**
     * Gets all gardens by the names of the plants in them.
     * @param plantName
     * @param pageable
     * @return Pageable list of the gardens.
     */
    @Query("SELECT g FROM Garden g JOIN g.plants p WHERE g.publicised = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', ?1, '%'))")
    Page<Garden> findPublicGardensByPlantName(String plantName, Pageable pageable);
    /**
     * finds a page of the most recent public gardens matching the name, filtered by inclusion of ANY of the given tags
     * @param name the search term to use for garden names
     * @param pageable pagination object
     * @param tags list of tags to filter by
     * @return
     */
    @Query("SELECT g FROM Garden g JOIN g.tags t WHERE t.content IN :tags AND g.publicised = true AND LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Garden> findPublicGardensByNameWithTagFilter(@Param("name")String name, Pageable pageable, @Param("tags") List<String> tags);
    /**
     * finds a page of the most recent public gardens matching the plant name for any plant, filtered by inclusion of ANY of the given tags
     * @param plantName the search term to use for plant names
     * @param pageable pagination object
     * @param tags list of tags to filter by
     * @return
     */
    @Query("SELECT g FROM Garden g JOIN g.plants p JOIN g.tags t WHERE t.content IN :tags AND g.publicised = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :plantName, '%'))")
    Page<Garden> findPublicGardensByPlantNameWithTagFilter(@Param("plantName") String plantName, Pageable pageable, @Param("tags") List<String> tags);


    Page<Garden> findByPublicisedTrueAndName(String name, Pageable pageable);
    
    Page<Garden> findByPublicisedTrue(Pageable pageable);

    /**
     * Gets the recent public gardens for the hompage.
     * @param pageable
     * @return Pageable list of gardens.
     */
    @Query("SELECT g FROM Garden g WHERE g.publicised = true ORDER BY g.id DESC")
    Page<Garden> findRecentPublicGardens(Pageable pageable);

    /**
     * Gets a list of users from their tags, ie gets all users associated with the tag "fuck"
     * @param tagId
     * @return List of users.
     */
    @Query("SELECT DISTINCT g.owner FROM Garden g JOIN g.tags t WHERE t.tagId = :tagId")
    List<AbstractUser> findUserIdsByTag(Long tagId);

    /**
     * finds a page of the most recent public gardens, filtered by inclusion of ANY of the given tags
     * @param pageable pagination object
     * @param tags list of tags to filter by
     * @return
     */
    @Query("SELECT g FROM Garden g JOIN g.tags t WHERE t.content IN :tags AND g.publicised = true ORDER BY g.id DESC")
    Page<Garden> findRecentPublicGardensWithTagFilter(Pageable pageable, @Param("tags") List<String> tags);

    /**
     * Gets gardens by the tag Id to remove foreign key constraints for deleting a tag.
     * @param tagId
     * @return List of gardens
     */
    @Query("SELECT DISTINCT g FROM Garden g JOIN g.tags t WHERE t.tagId = :tagId")
    List<Garden> findByTagId(Long tagId);

    /**
     * Gets the number of service requests that are unassigned and use this garden.
     * @param garden the garden
     * @return the count
     */
    @Query("SELECT COUNT(s) from ServiceRequest s WHERE s.contractor IS NULL AND s.garden = :garden")
    int countInUseForUnassignedServiceRequest(Garden garden);


}
