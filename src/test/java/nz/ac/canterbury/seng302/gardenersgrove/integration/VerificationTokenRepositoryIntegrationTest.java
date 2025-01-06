package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.VerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to ensure the correct list of expired tokens is being retrieved from the database.
 * Using the Transactional tag ensures all changes to the database as a result of these tests are rolled back.
 */
@SpringBootTest
@Transactional
class VerificationTokenRepositoryIntegrationTest {

    @Autowired
    VerificationTokenRepository verificationTokenRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    @Transactional
    void testFindByExpiryDateIsBefore() {
        //create users for to use for creating tokens
        User userOne = new User("test", "user", "mailOne@mail.mail", "Password1!", null, null);
        User userTwo = new User("test", "user", "mailTwo@mail.mail", "Password1!", null, null);
        User userThree = new User("test", "user", "mailThree@mail.mail", "Password1!", null, null);

        // save users to db
        userRepository.save(userOne);
        userRepository.save(userTwo);
        userRepository.save(userThree);

        // Create expired tokens
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1); // One day ago
        Timestamp expiredTimestamp = new Timestamp(cal.getTime().getTime());

        VerificationToken expiredTokenOne = new VerificationToken("tokenString", userOne);
        expiredTokenOne.setExpiryDate(expiredTimestamp);

        VerificationToken expiredTokenTwo = new VerificationToken("tokenString", userTwo);
        expiredTokenTwo.setExpiryDate(expiredTimestamp);

        // Create non-expired token
        VerificationToken nonExpiredToken = new VerificationToken("tokenString", userThree);
        nonExpiredToken.setExpiryDate(new Timestamp(System.currentTimeMillis() + 120000)); // Two hours in the future

        // Save tokens to the database
        verificationTokenRepository.save(expiredTokenOne);
        verificationTokenRepository.save(expiredTokenTwo);
        verificationTokenRepository.save(nonExpiredToken);

        // Fetch expired tokens from the repository
        List<VerificationToken> expiredTokens = verificationTokenRepository.findByExpiryDateIsBefore(new Timestamp(System.currentTimeMillis()));

        // Verify that the correct number of expired tokens is fetched
        assertEquals(2, expiredTokens.size());

        //verify that the expired tokens are fetched
        assertTrue(expiredTokens.contains(expiredTokenOne));
        assertTrue(expiredTokens.contains(expiredTokenTwo));

        //verify the non-expired token is not fetched
        assertFalse(expiredTokens.contains(nonExpiredToken));
    }
}
