package nz.ac.canterbury.seng302.gardenersgrove.repository;


import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * VerificationToken repository accessor using Spring's @link{CrudRepository}.
 * These (basic) methods are provided for us without the need to write our own implementations
 */
@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);
    VerificationToken findByUser(AbstractUser user);
    Integer deleteVerificationTokenById(Long id);

    /**
     * Pass the current timestamp into this function for a list of expired VerificationTokens
     * @param timestamp timestamp to check expiry times against
     * @return a list of expired VerificationTokens
     */
    List<VerificationToken> findByExpiryDateIsBefore(Timestamp timestamp);
}
