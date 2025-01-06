package nz.ac.canterbury.seng302.gardenersgrove.repository;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Contractor;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for performing CRUD (Create, Read, Update, Delete)
 * operations on User entities.
 * Extends JpaRepository to inherit methods for database interaction.
 * This interface acts as a bridge between the application code and the database,
 * and provides a way to perform database operations on User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<AbstractUser, Long> {
    // retrieve a user from the database by their id.
    Optional<AbstractUser> findByUserId(Long userId);

    @Query("SELECT u FROM Contractor u WHERE u.userId = ?1")

    Optional<Contractor> findContractorByUserId(Long userId);

    // retrieve a user from the database by their email address, case insensitive.
    @Query("SELECT u FROM AbstractUser u WHERE LOWER(u.email) = LOWER(?1)")
    AbstractUser findByEmail(String email);

    // retrieve a user from the database by their email and password, case insensitive.
    @Query("SELECT u FROM AbstractUser u WHERE LOWER(u.email) = LOWER(?1) AND u.password = ?2")
    AbstractUser findByEmailAndPassword(String email, String password);

    // retrieve users from the database by their first name, case insensitive.
    @Query("SELECT u FROM AbstractUser u WHERE LOWER(u.firstName) = LOWER(?1) AND (u.lastName IS NULL OR u.lastName = '')")
    List<AbstractUser> findUsersByFirstName(String firstName);

    // retrieve users from the database by their full name, case insensitive.
    @Query("SELECT u FROM AbstractUser u WHERE LOWER(CONCAT(u.firstName, ' ', u.lastName)) = LOWER(?1)")
    List<AbstractUser> findUsersByFullName(String fullName);

    Integer deleteUserByUserId(Long userId);

    // Update target user type to CONTRACTOR
    @Modifying
    @Query("UPDATE AbstractUser u SET u.userType = 'CONTRACTOR' WHERE u.userId = :userId")
    void convertUsertype(@Param("userId") Long userId);

    // Update target contractor properties
    @Modifying
    @Transactional
    @Query("UPDATE Contractor c SET c.aboutMe = :aboutMe, c.workPictures = :workPictures, c.location = :location WHERE c.userId = :userId")
    void updateContractorProperties(@Param("userId") Long userId,
                                    @Param("aboutMe") String aboutMe,
                                    @Param("workPictures") String workPictures,
                                    @Param("location") Location location);

  }
