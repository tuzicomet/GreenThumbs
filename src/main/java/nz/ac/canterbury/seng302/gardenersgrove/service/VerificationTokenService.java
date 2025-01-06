package nz.ac.canterbury.seng302.gardenersgrove.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import nz.ac.canterbury.seng302.gardenersgrove.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

/**
 * Service class for verification tokens, defined by the @link{Service} annotation.
 * This class links automatically with @link{VerificationTokenRepository}, see the @link{Autowired} annotation below
 */
@Service
public class VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;
    @Autowired
    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    /**
     * finds a verification token given the unique token
     * @param token unique token to search for
     * @return the VerificationToken found
     */
    public VerificationToken findByToken(String token){
        return verificationTokenRepository.findByToken(token);
    }
    /**
     * finds a verification token given the user
     * @param user user to search for
     * @return the VerificationToken found
     */
    public VerificationToken findByUser(AbstractUser user){
        return verificationTokenRepository.findByUser(user);
    }

    /**
     * Saves a verification token to the db, and sets the expiry timestamp for 10 minutes from creation
     * @param user the user which can be activated by the token
     * @param token the unique activation token
     */
    public void save(AbstractUser user, String token){
        VerificationToken verificationToken = new VerificationToken(token, user);

        //set expiry timestamp for 10 minutes from token creation
        verificationToken.setExpiryDate(calculateExpiryDate(10));

        verificationTokenRepository.save(verificationToken);
    }

    /**
     * Gets a timestamp that is a given number of minutes from the current time
     * @param expiryTimeMinutes the number of minutes before the token should expire
     * @return a Timestamp representing when the token should expire
     */
    private Timestamp calculateExpiryDate(int expiryTimeMinutes){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, expiryTimeMinutes);
        return new Timestamp(cal.getTime().getTime());
    }

    /**
     * Deletes a token by user
     * @param token to delete
     * @return the deleted token
     */
    @Transactional
    public Integer deleteVerificationToken(VerificationToken token){
        return verificationTokenRepository.deleteVerificationTokenById(token.getId());
    }

    /**
     * Gets a list of token objects that have expired
     * @return a list of expired verification tokens.
     */
    public List<VerificationToken> getExpiredTokens(){
        Calendar cal = Calendar.getInstance();
        return verificationTokenRepository.findByExpiryDateIsBefore(new Timestamp(cal.getTime().getTime()));
    }
}
