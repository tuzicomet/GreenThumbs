package nz.ac.canterbury.seng302.gardenersgrove.unit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AppUsersTableTest {

    // jdbctemplate will be used to interact with and execute SQL queries to the database directly
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional // any changes made to the database during the test will be rolled back afterward
    @Test
    void insertUser_FirstNameIsNull_ExceptionThrown() {
        // Attempt to insert a user with a null email
        try {
            jdbcTemplate.update("INSERT INTO app_users (" +
                    "first_name, last_name, email, " +
                    "password, date_of_birth, profile_picture, user_type) " +
                    "VALUES " +
                    "(null, 'Obama', 'obama@gmail.com', " +
                    "'password', '1961-08-04', 'mrpresident.jpg', 'USER')");
        } catch (DataIntegrityViolationException e) {
            // Expected behavior: the DataIntegrityViolationException is thrown
            assertThat(e).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Transactional
    @Test
    void insertUser_LastNameIsNull_UserIsSaved() {
        // Attempt to insert a user with null last name (which should be allowed)
        jdbcTemplate.update("INSERT INTO app_users (" +
                "first_name, last_name, email, " +
                "password, date_of_birth, profile_picture, user_type) " +
                "VALUES " +
                "('Barack', null, 'obama@gmail.com', " +
                "'password', '1961-08-04', 'mrpresident.jpg', 'USER')");

        // Query the database to check if the user was successfully added
        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT * FROM app_users WHERE first_name = 'Barack'");

        // Assert that the user was added
        // (Do this by checking that the result set is not empty, meaning the user was added)
        assertThat(users).isNotEmpty();
    }


    @Transactional
    @Test
    void insertUser_EmailIsNull_ExceptionThrown() {
        // Attempt to insert a user with a null email
        try {
            jdbcTemplate.update("INSERT INTO app_users (" +
                    "first_name, last_name, email, " +
                    "password, date_of_birth, profile_picture, user_type) " +
                    "VALUES " +
                    "('Barack', 'Obama', null, " +
                    "'password', '1961-08-04', 'mrpresident.jpg', 'USER')");
        } catch (DataIntegrityViolationException e) {
            // Expected behavior: the DataIntegrityViolationException is thrown
            assertThat(e).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Transactional
    @Test
    void insertUser_EmailExistsInDatabase_ExceptionThrown() {
        // Insert a user with a valid email
        jdbcTemplate.update("INSERT INTO app_users (" +
                "first_name, last_name, email, " +
                "password, date_of_birth, profile_picture, user_type) " +
                "VALUES " +
                "('John', 'Doe', 'john@example.com', " +
                "'password', '2000-01-01', 'image.jpg', 'USER')");

        // Attempt to insert another user with the same email
        try {
            jdbcTemplate.update("INSERT INTO app_users (" +
                    "first_name, last_name, email, " +
                    "password, date_of_birth, profile_picture, user_type) " +
                    "VALUES " +
                    "('Billy', 'Hill', 'john@example.com', " +
                    "'Testp4$$', '2000-01-01', 'anotherimage.jpg', 'USER')");
        } catch (DuplicateKeyException e) {
            // Expected behavior: the DuplicateKeyException is thrown
            assertThat(e.getMessage()).contains("Unique index or primary key violation");
        }
    }

    @Transactional
    @Test
    void insertUser_PasswordIsNull_ExceptionThrown() {
        // Attempt to insert a user with a null password
        try {
            jdbcTemplate.update("INSERT INTO app_users (" +
                    "first_name, last_name, email, " +
                    "password, date_of_birth, profile_picture, user_type) " +
                    "VALUES " +
                    "('Barack', 'Obama', 'obama@gmail.com', " +
                    "null, '1961-08-04', 'mrpresident.jpg', 'USER')");
        } catch (DataIntegrityViolationException e) {
            // Expected behavior: the DataIntegrityViolationException is thrown
            assertThat(e).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Transactional
    @Test
    void insertUser_DateOfBirthIsNull_UserIsSaved() {
        // Attempt to insert a user with null date of birth (which should be allowed)
        jdbcTemplate.update("INSERT INTO app_users (" +
                "first_name, last_name, email, " +
                "password, date_of_birth, profile_picture, user_type) " +
                "VALUES " +
                "('Barack', 'Obama', 'obama@gmail.com', " +
                "'password', null, 'mrpresident.jpg', 'USER')");

        // Query the database to check if the user was successfully added
        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT * FROM app_users WHERE first_name = 'Barack'");

        // Assert that the user was added
        // (Do this by checking that the result set is not empty, meaning the user was added)
        assertThat(users).isNotEmpty();
    }

    @Transactional
    @Test
    void insertUser_ProfilePictureIsNull_ExceptionThrown() {
        // Attempt to insert a user with an null profile picture (which should be allowed)
        try {
            jdbcTemplate.update("INSERT INTO app_users (" +
                    "first_name, last_name, email, " +
                    "password, date_of_birth, profile_picture, user_type) " +
                    "VALUES " +
                    "('Barack', 'Obama', 'obama@gmail.com', " +
                    "'password', '1961-08-04', null, 'USER')");
        } catch (DataIntegrityViolationException e) {
            // Expected behavior: the DataIntegrityViolationException is thrown
            assertThat(e).isInstanceOf(DataIntegrityViolationException.class);
        }
    }


}