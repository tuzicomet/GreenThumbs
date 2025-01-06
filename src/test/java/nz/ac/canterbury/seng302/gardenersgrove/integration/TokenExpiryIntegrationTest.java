package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.VerificationTokenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.ScheduledTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test class to ensure the correct tokens and users are being deleted from the database when the scheduled db cleaning task runs.
 * Using the Transactional tag ensures all changes to the database as a result of these tests are rolled back.
 */
@SpringBootTest
@Transactional
class TokenExpiryIntegrationTest {

    @Autowired
    VerificationTokenRepository verificationTokenRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ScheduledTask scheduledTask;
    @Autowired
    UserService userService;

    @Test
    @Transactional
    void deleteExpiredRecords_UserIsDisabled_UserAndTokenDeleted() {
        //create users for to use for creating tokens
        User userOne = new User("test", "user", "mailOne@mail.mail", "Password1!", null, null);
        User userTwo = new User("test", "user", "mailTwo@mail.mail", "Password1!", null, null);

        // save users to db
        userRepository.save(userOne);
        userRepository.save(userTwo);

        // Create expired tokens
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1); // One day ago
        Timestamp expiredTimestamp = new Timestamp(cal.getTime().getTime());

        VerificationToken expiredToken = new VerificationToken("tokenString", userOne);
        expiredToken.setExpiryDate(expiredTimestamp);


        // Create non-expired token
        VerificationToken nonExpiredToken = new VerificationToken("tokenString", userTwo);
        nonExpiredToken.setExpiryDate(new Timestamp(System.currentTimeMillis() + 120000)); // Two hours in the future

        // Save tokens to the database
        verificationTokenRepository.save(expiredToken);
        verificationTokenRepository.save(nonExpiredToken);

        //confirm the tokens and users are in the db
        assertEquals(verificationTokenRepository.findByUser(userOne), expiredToken);
        assertEquals(verificationTokenRepository.findByUser(userTwo), nonExpiredToken);
        assertEquals(userRepository.findByEmail("mailOne@mail.mail"), userOne);
        assertEquals(userRepository.findByEmail("mailTwo@mail.mail"), userTwo);

        // Call the method to test
        scheduledTask.deleteExpiredRecords();

        // Verify the user and token for the expired token have been deleted
        assertNull(verificationTokenRepository.findByUser(userOne));
        assertNull(userRepository.findByEmail("mailOne@mail.mail"));

        // Verify the non-expired token still exists, and so does the associated user
        assertEquals(verificationTokenRepository.findByUser(userTwo), nonExpiredToken);
        assertEquals(userRepository.findByEmail("mailTwo@mail.mail"), userTwo);
    }
    @Test
    @Transactional
    void deleteExpiredRecords_UserIsEnabled_OnlyTokenDeleted() {
        //create users for to use for creating tokens
        User userOne = new User("test", "user", "mailOne@mail.mail", "Password1!", null, null);
        User userTwo = new User("test", "user", "mailTwo@mail.mail", "Password1!", null, null);

        // save users to db
        userRepository.save(userOne);
        userRepository.save(userTwo);

        // enable a user
        userService.enableUser(userOne.getUserId());

        // Create expired tokens
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1); // One day ago
        Timestamp expiredTimestamp = new Timestamp(cal.getTime().getTime());

        VerificationToken expiredToken = new VerificationToken("tokenString", userOne);
        expiredToken.setExpiryDate(expiredTimestamp);


        // Create non-expired token
        VerificationToken nonExpiredToken = new VerificationToken("tokenString", userTwo);
        nonExpiredToken.setExpiryDate(new Timestamp(System.currentTimeMillis() + 120000)); // Two hours in the future

        // Save tokens to the database
        verificationTokenRepository.save(expiredToken);
        verificationTokenRepository.save(nonExpiredToken);

        //confirm the tokens and users are in the db
        assertEquals(verificationTokenRepository.findByUser(userOne), expiredToken);
        assertEquals(verificationTokenRepository.findByUser(userTwo), nonExpiredToken);
        assertEquals(userRepository.findByEmail("mailOne@mail.mail"), userOne);
        assertEquals(userRepository.findByEmail("mailTwo@mail.mail"), userTwo);

        // Call the method to test
        scheduledTask.deleteExpiredRecords();

        // Verify the expired token has been deleted
        assertNull(verificationTokenRepository.findByUser(userOne));

        // Verify the user attached to the expired token still exists
        assertEquals(userRepository.findByEmail("mailOne@mail.mail"), userOne);

        // Verify the non-expired token still exists, and so does the associated user
        assertEquals(verificationTokenRepository.findByUser(userTwo), nonExpiredToken);
        assertEquals(userRepository.findByEmail("mailTwo@mail.mail"), userTwo);
    }
    @Test
    @Transactional
    void deleteExpiredRecords_MixOfEnabledAndExpired_CorrectDeletion() {
        //create users for to use for creating tokens
        User userOne = new User("test", "user", "mailOne@mail.mail", "Password1!", null, null);
        User userTwo = new User("test", "user", "mailTwo@mail.mail", "Password1!", null, null);
        User userThree = new User("test", "user", "mailThree@mail.mail", "Password1!", null, null);
        User userFour = new User("test", "user", "mailFour@mail.mail", "Password1!", null, null);

        // save users to db
        userRepository.save(userOne);
        userRepository.save(userTwo);
        userRepository.save(userThree);
        userRepository.save(userFour);

        // enable some users
        userService.enableUser(userOne.getUserId());
        userService.enableUser(userThree.getUserId());


        // Create expired tokens
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1); // One day ago
        Timestamp expiredTimestamp = new Timestamp(cal.getTime().getTime());

        VerificationToken expiredTokenEnabled = new VerificationToken("tokenString", userOne);
        expiredTokenEnabled.setExpiryDate(expiredTimestamp);
        VerificationToken expiredTokenDisabled = new VerificationToken("tokenString", userTwo);
        expiredTokenDisabled.setExpiryDate(expiredTimestamp);


        // Create non-expired tokens
        VerificationToken nonExpiredTokenEnabled = new VerificationToken("tokenString", userThree);
        nonExpiredTokenEnabled.setExpiryDate(new Timestamp(System.currentTimeMillis() + 120000));
        VerificationToken nonExpiredTokenDisabled = new VerificationToken("tokenString", userFour);
        nonExpiredTokenDisabled.setExpiryDate(new Timestamp(System.currentTimeMillis() + 120000)); // Two hours in the future

        // Save tokens to the database
        verificationTokenRepository.save(expiredTokenEnabled);
        verificationTokenRepository.save(expiredTokenDisabled);
        verificationTokenRepository.save(nonExpiredTokenEnabled);
        verificationTokenRepository.save(nonExpiredTokenDisabled);

        //confirm the tokens and users are in the db
        assertEquals(verificationTokenRepository.findByUser(userOne), expiredTokenEnabled);
        assertEquals(verificationTokenRepository.findByUser(userTwo), expiredTokenDisabled);
        assertEquals(verificationTokenRepository.findByUser(userThree), nonExpiredTokenEnabled);
        assertEquals(verificationTokenRepository.findByUser(userFour), nonExpiredTokenDisabled);
        assertEquals(userRepository.findByEmail("mailOne@mail.mail"), userOne);
        assertEquals(userRepository.findByEmail("mailTwo@mail.mail"), userTwo);
        assertEquals(userRepository.findByEmail("mailThree@mail.mail"), userThree);
        assertEquals(userRepository.findByEmail("mailFour@mail.mail"), userFour);

        // Call the method to test
        scheduledTask.deleteExpiredRecords();

        // Verify the expired tokens have been deleted
        assertNull(verificationTokenRepository.findByUser(userOne));
        assertNull(verificationTokenRepository.findByUser(userTwo));

        // Verify the enabled user attached to the expired token has been deleted
        assertNull(userRepository.findByEmail("mailTwo@mail.mail"));

        // Verify the enabled user attached to the expired token still exists
        assertEquals(userRepository.findByEmail("mailOne@mail.mail"), userOne);

        // Verify the non-expired tokens still exists, and so do the associated users
        assertEquals(verificationTokenRepository.findByUser(userThree), nonExpiredTokenEnabled);
        assertEquals(userRepository.findByEmail("mailThree@mail.mail"), userThree);
        assertEquals(verificationTokenRepository.findByUser(userFour), nonExpiredTokenDisabled);
        assertEquals(userRepository.findByEmail("mailFour@mail.mail"), userFour);
    }
}
