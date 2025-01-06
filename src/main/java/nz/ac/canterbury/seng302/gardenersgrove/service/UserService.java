package nz.ac.canterbury.seng302.gardenersgrove.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.utility.LocaleUtils;
import nz.ac.canterbury.seng302.gardenersgrove.validation.PasswordChangeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


/**
 * UserService class to handle the backend during frontend dev.
 */
@Service
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private final MailService mailService;
    private final UserRepository userRepository;
    private final VerificationTokenService verificationTokenService;
    private final PasswordEncoder passwordEncoder;
    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final WidgetPreferencesService widgetPreferencesService;
    private final MessageSource messageSource;


    @Autowired
    public UserService(UserRepository userRepository,
                       VerificationTokenService verificationTokenService,
                       MailService mailService,
                       @Lazy PasswordEncoder passwordEncoder,
                       FriendshipRepository friendshipRepository,
                       FriendRequestRepository friendRequestRepository,
                       WidgetPreferencesService widgetPreferencesService, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.verificationTokenService = verificationTokenService;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.friendshipRepository = friendshipRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.widgetPreferencesService = widgetPreferencesService;
        this.messageSource = messageSource;
    }

    /**
     * Find the user based off of a given email.
     * @param email the email of the user that is being found.
     */
    public AbstractUser getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase());
    }

    /**
     * Add a new user alongside a token that will be required for verification.
     * Then send the email alongside the token.
     * @param user the user that is being added to the database
     */
    public AbstractUser addUser(AbstractUser user) {
        AbstractUser saved = userRepository.save(user);
        String token = UUID.randomUUID().toString().substring(0, 6);
        verificationTokenService.save(saved, token);
        return saved;
    }

    /**
     * Update the user with the given ID
     *
     * @param user        User to update
     */
    public AbstractUser updateUserDetails(AbstractUser user) {
        return userRepository.save(user);
    }

    /**
     * Enable a user's account
     *
     * @param userId the id of the user to enable
     */
    public void enableUser(long userId) {
        AbstractUser user = userRepository.findByUserId(userId).orElseThrow(() ->
                new IllegalArgumentException("Requested user does not exist"));
        user.removeAuthority("ROLE_USER");
        user.grantAuthority("ROLE_USER_VERIFIED");
        userRepository.save(user);
        // set up the user's widget preferences (what widgets will be shown on their main page)
        widgetPreferencesService.initialisePreferences(user.getUserId());
    }

    /**
     * Update the specified user's profile image path
     *
     * @param userId   id of the user
     * @param filename path to the file from the UPLOAD_ROOT_PATH.
     *                 Should be in the format <br><code>/{subdirectory}/{filename}.{extension}</code>
     * @throws IllegalArgumentException if the userId does not exist in the database
     */
    public void updateUserImage(long userId, String filename) {
        AbstractUser user = getUserByUserId(userId).orElseThrow(() ->
                new IllegalArgumentException("Requested user does not exist"));
        user.setProfilePicture(filename);
        userRepository.save(user);
    }

    /**
     * Deletes a user by userId
     * @param userId the id for the user to delete
     * @return the deleted user
     */
    public Integer deleteUserByUserId(long userId) {
        return userRepository.deleteUserByUserId(userId);
    }

    /**
     * Update the user's password using an already hashed password.
     * @param userId the ID of the user whose password is to be updated.
     * @param hashedPassword the already hashed new password that will be set for the user.
     */
    public void updatePassword(long userId, String hashedPassword) {
        AbstractUser user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }
    public String createPasswordResetTokenForUser(AbstractUser user) {
        String token = UUID.randomUUID().toString();
        verificationTokenService.save(user, token);
        return token;
    }

    /**
     * Changes the password of the connected user. This method performs several checks:
     * Verifies that the old password is correct.
     * Checks if the new password and the retyped password are the same.
     * If these checks pass, the password of the user is updated and saved.
     * An email is sent to the user to confirm the password change.
     * @param oldPassword the old password of the user
     * @param newPassword the new password the user wants to set
     * @param retypePassword the new password retyped for confirmation
     * @param connectedUser the currently connected user
     * @throws IllegalStateException if the old password is incorrect or the new passwords do not match
     */
    public void changePassword(String oldPassword, String newPassword, String retypePassword, Principal connectedUser, HttpServletRequest request) {
        var user = (AbstractUser) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalStateException("Your old password is incorrect");
        }

        if (!newPassword.equals(retypePassword)) {
            throw new IllegalStateException("The new passwords do not match");
        }
        if (!PasswordChangeValidator.isValidPassword(newPassword, user)) {
            throw new IllegalStateException("New password is too weak");
        }


        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        // Email the user a confirmation of their password change
        // Send locale to ensure that the email is in the correct language
        mailService.sendPasswordChangeEmail(user, locale);
    }

    /**
     * Get all friends of a given user
     * @param user the user to get the friends of
     * @return a list containing all the user's friends
     */
    public List<AbstractUser> getFriendsOfUser(AbstractUser user) {
        // get the user's id to search with
        Long userId = user.getUserId();

        // In order to get all the user's friends, we need to get all records
        // from the friendships table which the user is in (they could be either in
        // the user1 column or the user2 column)
        // (This assumes that any given pair will only appear once in the table,
        // regardless of how they are ordered as user1/user2)

        // Retrieve all friendships where the user's id appears in user1Id column
        List<Friendship> friendshipsPart1 = friendshipRepository.findByUser1Id(userId);
        // Retrieve all friendships where the user's id appears in user2Id column
        List<Friendship> friendshipsPart2 = friendshipRepository.findByUser2Id(userId);

        // Concatenate both lists into one, for a complete list of friendship records
        // which the user is part of
        List<Friendship> friendshipList = new ArrayList<>();
        friendshipList.addAll(friendshipsPart1);
        friendshipList.addAll(friendshipsPart2);

        // Create a list to hold the friends (as User entities)
        List<AbstractUser> friendsList = new ArrayList<>();

        // For each friendship record which includes the user, we need to extract
        // the ID of the other user in the record, and add that user to the list
        for (Friendship friendship : friendshipList) {
            // Determine the ID of the other user in the record
            // (If the given user's id is in the User1Id column, then take the id in
            // the User2Id column, and vice versa)
            Long friendId = (userId.equals(friendship.getUser1Id()))
                    ? friendship.getUser2Id()
                    : friendship.getUser1Id();

            // get the User entity of the friend using their id, and if they exist,
            // add them to friendsList
            userRepository.findById(friendId).ifPresent(friendsList::add);
        }

        return friendsList;
    }

    /**
     * A function to check whether two given users are friends
     */
    public boolean areUsersFriends(AbstractUser user1, AbstractUser user2) {
        // Get user1's list of friends using the getFriendsOfUser function
        List<AbstractUser> user1Friends = getFriendsOfUser(user1);

        // returns true if user2 is in user1's friend list, otherwise returns false
        return user1Friends.contains(user2);
    }


    /**
     * Retrieves a list of users who have sent friend requests to the specified user,
     * that have not been declined
     */
    public List<AbstractUser> getUsersWithPendingRequestsToUser(AbstractUser user) {
        // get the user's id to search with
        Long userId = user.getUserId();

        // get friend requests where the given user is the receiver
        List<FriendRequest> requestsToUser = friendRequestRepository.findByReceiverId(userId);

        // Initialize a list to hold the users who have sent pending requests to the user
        List<AbstractUser> senderList = new ArrayList<>();

        // Go through each request
        for (FriendRequest request : requestsToUser) {
            // If the user has not declined the request
            if (!request.getStatus().equals("DECLINED")) {
                // get the sender and add them to the list
                userRepository.findById(request.getSenderId()).ifPresent(senderList::add);
            }
        }
        return senderList;
    }

    /**
     * Retrieves a list of users who the given user has sent requests to
     */
    public List<AbstractUser> getUsersWithSentRequestsFromUser(AbstractUser user) {
        // get the user's id to search with
        Long userId = user.getUserId();

        // get friend requests where the given user is the sender
        List<FriendRequest> requestsToUser = friendRequestRepository.findBySenderId(userId);

        // Initialize a list to hold the users who the given user has sent requests to
        List<AbstractUser> recipientList = new ArrayList<>();

        // Go through each request
        for (FriendRequest request : requestsToUser) {
            // get the recipient user and add them to the list
            // (Status of the request does not matter)
            userRepository.findById(request.getReceiverId()).ifPresent(recipientList::add);
        }
        return recipientList;
    }

    /**
     * Gets a list of users matching the given name.
     * The name is treated as both a first name and a full name this is to ensure
     * they are exact matches.
     * @param name the name search query
     * @return a list of matching users
     */
    public List<AbstractUser> getUsersByName(String name) {
        List<AbstractUser> users = new ArrayList<>(userRepository.findUsersByFirstName(name.toLowerCase()));
        List<AbstractUser> fullNameUsers = userRepository.findUsersByFullName(name.toLowerCase());

        for (AbstractUser user : fullNameUsers) {
            if (!users.contains(user)) {
                users.add(user);
            }
        }
        return users;
    }

    /**
     * Sends a friend request from the sender to the receiver.
     * @param sender the user sending the request
     * @param receiver the user to whom the sender wants to send the request
     */
    public void sendFriendRequest(AbstractUser sender, AbstractUser receiver){
        FriendRequest req = new FriendRequest(sender.getUserId(), receiver.getUserId(), "PENDING");
        friendRequestRepository.save(req);
        LOG.info("Sent friend request from {} to {}", sender.getEmail(), receiver.getEmail());
    }

    /**
     * Gets the status of a request from the given sender to the given receiver
     * @param sender the sender of the request
     * @param receiver the receiver of the request
     * @return the status of the request, if found, null otherwise
     */
    public String getStatus(AbstractUser sender, AbstractUser receiver){
        FriendRequest req = friendRequestRepository.findFriendRequestBySenderIdAndReceiverId(sender.getUserId(), receiver.getUserId());
        return req != null ? req.getStatus(): null;
    }
    /**
     * Gets a user given a userId
     *
     * @param userId the userId to match
     * @return the user matching the given userId, or Optional.empty() if the user is not found.
     */
    public Optional<AbstractUser> getUserByUserId(Long userId) {
        return userRepository.findByUserId(userId);
    }

    /**
     * Returns the most recent version of the authenticated user from the database.
     * @param authentication Authentication object from the session to get the updated details of.
     * @return User object with details from persistence
     */
    public AbstractUser getUserFromAuthentication(Authentication authentication) {
        AbstractUser oldUser = (AbstractUser) authentication.getPrincipal();
        return getUserByUserId(oldUser.getUserId()).orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Add a strike to the supplied user.
     * @param user user to strike.
     */
    public void addStrike(AbstractUser user) {
        user.incrementStrikes();
        userRepository.save(user);
    }

    /**
     * Ban the user for a specific amount, floored to the nearest day + 1.
     * If the user is already banned, the amount is reset to the provided value
     * i.e. banning the user for one day will ban the user until the end of tomorrow.
     * @param user User to ban
     * @param amount Combined with the period, gives the amount of time to ban the user for (i.e. *2* DAYS)
     */
    public void banUserForDays(AbstractUser user, int amount) {
        // Bans for 7 days + 1 is needed for ceiling
        user.setAccountDisabledUntil(Instant.now().truncatedTo(ChronoUnit.DAYS).plus(amount + 1L, ChronoUnit.DAYS));
        userRepository.save(user);
    }

    /**
     * Converts a standard user to a contractor by updating the user type and adding contractor-specific information.
     *
     * @param user         The user to be converted.
     * @param aboutMe      The description about the contractor.
     * @param workPictures The URL or path to the contractor's work pictures.
     * @param location     The contractor's location information.
     */
    @Transactional
    public void convertUserToContractor(User user, String aboutMe, List<String>  workPictures, Location location) {
        userRepository.convertUsertype(user.getUserId());
        // Detach the current user entity (optional)
        entityManager.detach(user);
        // Fetch the updated contractor entity
        Contractor contractor = entityManager.find(Contractor.class, user.getUserId());
        // Update contractor-specific fields
        contractor.setAboutMe(aboutMe);
        contractor.setWorkPictures(workPictures);
        contractor.setLocation(location);
        // Save the updated contractor entity
        entityManager.merge(contractor);
    }

    /**
     * Retrieves a contractor by their user ID.
     *
     * @param userId The ID of the user to retrieve as a contractor.
     * @return An {@link Optional} containing the contractor if found, or empty if not found.
     */
    public Optional<Contractor> getContractorByUserId(Long userId) {
        return userRepository.findContractorByUserId(userId);
    }

    /**
     * Given the id of a contractor user, returns the filepath to their contractor flair, or null if they are not a contractor
     * @param userId the id of the user to retrieve the flair for
     * @return the filepath to the user's contractor flair, or null if they are not a contractor
     */
    public List<String> getContractorFlair(Long userId, Locale locale) {
        Optional<Contractor> optionalContractor = getContractorByUserId(userId);
        List<String> list = new ArrayList<>();
        if (optionalContractor.isEmpty()) {
            list.add(null);
            list.add(null);
            return list;
        } else {
            Contractor contractor = optionalContractor.get();
            Integer numRatings = contractor.getNumRatings();

            if (numRatings <= 0) {
                list.add("/images/flair_leaf.png");
                list.add(messageSource.getMessage("tooltip.leaf_flair", null, null, locale));
                return list;
            }
            else if (numRatings <= 9) {
                list.add("/images/flair_straw.png");
                list.add(messageSource.getMessage("tooltip.straw_flair", null, null, locale));
                return list;

            }
            else if (numRatings <= 49) {
                list.add("/images/flair_cowboy.png");
                list.add(messageSource.getMessage("tooltip.cowboy_flair", null, null, locale));
                return list;
            }
            else if (numRatings <= 99) {
                list.add("/images/flair_archer.png");
                list.add(messageSource.getMessage("tooltip.archer_flair", null, null, locale));
                return list;
            }
            else if (numRatings <= 499) {
                list.add("/images/flair_crown.png");
                list.add(messageSource.getMessage("tooltip.crown_flair", null, null, locale));
                return list;
            }
            else { // number of ratings is 500 or more
                list.add("/images/flair_flower_crown.png");
                list.add(messageSource.getMessage("tooltip.flower_crown_flair", null, null, locale));
                return list;
            }
        }
    }

}
