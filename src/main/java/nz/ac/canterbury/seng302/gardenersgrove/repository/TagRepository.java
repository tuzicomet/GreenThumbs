package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Tag repository accessor using Spring's @link{CrudRepository}.
 * These (basic) methods are provided for us without the need to write our own implementations
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    /**
     * Searches repository for tag with a specific content
     * @param content Text to search for
     * @return an Optional, containing the tag if found.
     */
    Optional<Tag> findByContent(String content);

    /**
     * Gets the five closest matches to the input string from existing tags in the repository.
     * @param input query for which matches must be found, must be in lower case
     * @return The five closest matches (not case-sensitive) to the input string from existing tags, in order of length ascending
     */
    @Query(value = "SELECT t.content FROM tag t WHERE t.verified = TRUE AND LOWER(t.content) LIKE :input% ORDER BY LENGTH(t.content) ASC LIMIT 5", nativeQuery = true)
    List<String> findAutocompleteTags(@Param("input") String input);

    /**
     * Gets the oldest non moderated tag
     * @return optional of the tag
     */
    @Query(value = "SELECT * FROM tag t WHERE t.verified = FALSE ORDER BY t.tag_id ASC LIMIT 1", nativeQuery = true)
    Optional<Tag> getUnverifiedTag();

}
