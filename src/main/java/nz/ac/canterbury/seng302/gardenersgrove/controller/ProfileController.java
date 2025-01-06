package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Contractor;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Location;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.FileSizeException;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.ImageTypeException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.FileValidation;
import nz.ac.canterbury.seng302.gardenersgrove.utility.LocaleUtils;
import nz.ac.canterbury.seng302.gardenersgrove.validation.FormValidation;
import nz.ac.canterbury.seng302.gardenersgrove.validation.PasswordChangeValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.UserInformationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.security.Principal;
import java.util.*;

import static nz.ac.canterbury.seng302.gardenersgrove.validation.UserInformationValidator.*;

/**
 * Controller class for the user profile pages
 * This controller manages displaying profile pages for the current user, or for
 * other users, and the submission of the edit user details form.
 */
@Controller
public class ProfileController {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileController.class);

    private final UserRepository userRepository;
    private String referer;
    private final UserService userService;
    private final UserInformationValidator userInformationValidator;
    private final FriendshipService friendshipService;
    private final FileService fileService;
    private final MessageSource messageSource;
    private final LocationService locationService;

    @Autowired
    public ProfileController(UserRepository userRepository, UserService userService,
                             UserInformationValidator userInformationValidator, FileService fileService,
                             FriendshipService friendshipService, MessageSource messageSource, LocationService locationService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.userInformationValidator = userInformationValidator;
        this.fileService = fileService;
        this.friendshipService = friendshipService;
        this.messageSource = messageSource;
        this.locationService = locationService;
    }

    /**
     * Route for the viewing the currently logged-in user's profile page
     * @param edit a boolean to determine whether the user is editing or viewing their profile
     * @param model the model containing data which will be shown on the profile page
     * @param redirectAttributes the attributes to pass on to the page which the user is being redirected to
     * @return the view for the page to render: either the user's profile page or a redirect away if not permitted
     */
    @GetMapping(value = "/profile")
    public String ownProfile(@RequestParam(name = "edit", defaultValue = "false") boolean edit,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request) {

        Locale locale = LocaleUtils.getLocaleFromSession(request);
        LOG.info("Return profile");

        // Retrieve the currently logged-in user's authentication details from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Check that the currently logged-in user's authentication details are available
        // make sure that they are authenticated
        if (authentication.isAuthenticated()) {

            // Retrieve the authenticated user's details
            AbstractUser user = userService.getUserFromAuthentication(authentication);
            if(!user.isEnabled()) {
                return "redirect:/activate";
            }

            String contractorFlair = userService.getContractorFlair(user.getUserId(), locale).get(0);
            model.addAttribute("contractorFlair", contractorFlair);

            String flairToolTip = userService.getContractorFlair(user.getUserId(), locale).get(1);
            model.addAttribute("flairToolTip", flairToolTip);


            // Add the user object to the model attribute, under the name 'user'
            // (this makes it so that we can access the user object and their attributes
            // within the html we are passing to, e.g. by calling user or user.attribute)
            model.addAttribute("user", user); // Get user from repo to get the most recent user information
            // Return the profileTemplate.html template
            model.addAttribute("authenticatedAsUser", true);
            model.addAttribute("firstName", user.getFirstName());
            model.addAttribute("lastName", user.getLastName());
            model.addAttribute("noSurname", Objects.equals(user.getLastName(), ""));
            model.addAttribute("email", user.getEmail());
            model.addAttribute("dateOfBirth", user.getDateOfBirth());
            model.addAttribute("edit", edit);
            model.addAttribute("profilePicture", user.getProfilePicture());
            model.addAttribute("user", user);
            model.addAttribute("gardens", user.getOwnedGardens());
            model.addAttribute("isUser", user instanceof User);
            model.addAttribute("isViewedUserContractor", user instanceof Contractor);

            if(user.isEnabled()) {
                return "profileTemplate";
            }
            // if the user is not yet enabled, redirect to activation page
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/activate";

        } else {
            // Redirect to the login page if the user is not authenticated
            return "redirect:/login";
        }
    }

    /**
     * Route for viewing other users' profile pages
     * @param userId the id of the user whose profile page is being viewed
     * @param model the model containing data which will be shown on the profile page
     * @return either the view to render the user's profile page or a redirect away if not permitted
     */
    @GetMapping(value = "/profile/user/{userId}")
    public String getUserProfile(@PathVariable Long userId,
                                 Model model,
                                 HttpServletRequest request) {

        Locale locale = LocaleUtils.getLocaleFromSession(request);

        // Retrieve the currently logged-in user's authentication details from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check that the currently logged-in user's authentication details are available
        // make sure that they are authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            // Retrieve the authenticated user's details
            AbstractUser currentUser = userService.getUserFromAuthentication(authentication);

            // Retrieve the user whose page the current user is trying to access
            AbstractUser targetUser = userRepository.findById(userId).orElse(null);
            // If the user with the given id does not exist
            if (targetUser == null) {
                return "redirect:/friends";
            }

            // If the currently logged in user is friends with the target user
            if (userService.areUsersFriends(currentUser, targetUser)) {
                // Add the user object to the model attribute
                model.addAttribute("user", targetUser);
                // Return the profileTemplate.html template
                model.addAttribute("authenticatedAsUser", false);

                String contractorFlair = userService.getContractorFlair(targetUser.getUserId(), locale).get(0);
                model.addAttribute("contractorFlair", contractorFlair);

                String flairToolTip = userService.getContractorFlair(targetUser.getUserId(), locale).get(1);
                model.addAttribute("flairToolTip", flairToolTip);

                model.addAttribute("firstName", targetUser.getFirstName());
                model.addAttribute("lastName", targetUser.getLastName());
                model.addAttribute("noSurname", Objects.equals(targetUser.getLastName(), ""));
                model.addAttribute("email", targetUser.getEmail());
                model.addAttribute("dateOfBirth", targetUser.getDateOfBirth());
                model.addAttribute("edit", false);
                model.addAttribute("profilePicture", targetUser.getProfilePicture());
                model.addAttribute("gardens", targetUser.getOwnedGardens());
                model.addAttribute("isViewedUserContractor", targetUser instanceof Contractor);

                // Return the profileTemplate.html template
                return "profileTemplate";
            } else {
                return "redirect:/friends";
            }

        } else {
            // Redirect to the login page if the user is not authenticated
            return "redirect:/login";
        }

    }

    /**
     * Route for viewing individual users' garden list page
     * @param userId the id of the user whose profile page is being viewed
     * @param model the model containing data which will be shown on the profile page
     * @param request HttpServletRequest object which contains information about the request.
     *                Used here to get the locale from the request.
     * @return either the view to render the user's profile page or a redirect away if not permitted
     */
    @GetMapping(value = {"/profile/{userId}", "/profile/{userId}/"})
    public String profile(@PathVariable Long userId, Model model, HttpServletRequest request) {

        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser currentUser = userService.getUserFromAuthentication(authentication);

        if (currentUser.getUserId().equals(userId)) {
            return "redirect:/profile";
        }

        model.addAttribute("gardens", currentUser.getOwnedGardens());
        Optional<AbstractUser> requestedUser = userService.getUserByUserId(userId);

        if (requestedUser.isPresent()) {
            if (friendshipService.areFriends(currentUser, requestedUser.get())) {
                model.addAttribute("friend", requestedUser.get());
            } else {
                // Get the friend not found error message and add it to the model
                String message = messageSource.getMessage("error.friendNotFound", null, locale);
                model.addAttribute("error", message);
            }
        } else {
            // Get the friend not found error message and add it to the model
            String message = messageSource.getMessage("error.friendNotFound", null, locale);
            model.addAttribute("error", message);
        }

        return "userDetailTemplate";
    }

    /**
     * Handles the cancel button on the edit user profile page
     * @return an instruction to redirect the user to the profile page
     */
    @PostMapping(value = "/profile", params = "cancel")
    public String cancelEdit() {
        // on pressing the cancel button, redirects to the regular profile page
        return "redirect:/profile";
    }

    /**
     * Handles POST requests for saving changes made to the logged-in user on the edit user profile page
     * @param firstName the first name to update the current user details with
     * @param lastName the last name to update the current user details with
     * @param noSurname a boolean which is set to true if the user is submitting that they have no last name
     * @param email the email to update the current user details with
     * @param dateOfBirth the date of birth to update the current user details with
     * @param model the model containing data which will be shown on the profile page
     * @param request  HttpServletRequest object which contains information about the request.
     *                 Used here to get the locale from the request.
     * @return the view to render the profile page with errors if any, or a redirect to the profile page if successful
     */
    @PostMapping(value = "/profile", params = "submit")
    public String saveChanges(@RequestParam(name = "firstName") String firstName,
                              @RequestParam(name = "lastName", required = false) String lastName,
                              @RequestParam(name = "noSurname", defaultValue = "false") boolean noSurname,
                              @RequestParam(name = "email") String email,
                              @RequestParam(name = "dateOfBirth", required = false) String dateOfBirth,
                              Model model, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser user = userService.getUserFromAuthentication(authentication);

        if (noSurname) {
            lastName = "";
        }

        if (dateOfBirth.isEmpty()) {
            // if so, convert it to null (as empty string cannot be parsed to DATE in database)
            dateOfBirth = null;
        }

        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        boolean firstNameError = firstNameValidation(firstName, model, false, messageSource, locale);
        boolean lastNameError = !noSurname && lastNameValidation(lastName, model, false, messageSource, locale);
        boolean emailError = emailValidation(email, model, messageSource, locale);
        boolean dobError = dobValidation(dateOfBirth, model, messageSource, locale);
        boolean emailExistsError = userInformationValidator.validateEmailExists(email, model, user.getEmail(), messageSource, locale);

        if (firstNameError || lastNameError || emailError || dobError || emailExistsError) {
            model.addAttribute("user", user);
            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            model.addAttribute("noSurname", noSurname);
            model.addAttribute("email", email);
            model.addAttribute("dateOfBirth", dateOfBirth);
            model.addAttribute("edit", true);
            model.addAttribute("gardens", user.getOwnedGardens());
            return "profileTemplate";
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setDateOfBirth(dateOfBirth);
        AbstractUser updatedUser = userService.updateUserDetails(user);
        updateAuthentication(updatedUser);
        LOG.info("updated User");

        model.addAttribute("user", updatedUser);
        model.addAttribute("edit", false);

        return "redirect:/profile";
    }

    /**
     * Method for updating a given user's authentication
     * @param user the user to update authentication for
     */
    private void updateAuthentication(AbstractUser user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    /**
     * Handles POST requests for saving newly uploaded images for the logged-in user
     * @param multipartFile the uploaded image file
     * @param redirectAttributes the attributes to pass on to the page which the user is being redirected to
     * @param request  HttpServletRequest object which contains information about the request.
     *                 Used here to get the locale from the request.
     * @return a redirect instruction to the profile page
     */
    @PostMapping(value = "/profile")
    public String saveProfilePic(@RequestParam(value = "file", required = false) MultipartFile multipartFile,
                                 RedirectAttributes redirectAttributes, HttpServletRequest request) {

        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser user = userService.getUserFromAuthentication(authentication);
        try {
            FileValidation.validateImage(multipartFile);
            Path fileName = fileService.addFile(multipartFile, "images").orElseThrow(IOException::new);
            String imageRelativePath = "/%s/%s".formatted(
                    ImageController.USER_UPLOAD_MAPPING,
                    fileName.getFileName().toString()
            );
            userService.updateUserImage(user.getUserId(), imageRelativePath);
        } catch (ImageTypeException e) {
            // 'Image must be of type png, jpg, or svg' error
            String errorMessage = messageSource.getMessage("error.imageFormat", null, locale);
            redirectAttributes.addFlashAttribute("error", errorMessage);
        } catch (FileSizeException e) {
            // 'Image must be less than 10MB' error
            String errorMessage = messageSource.getMessage("error.imageTooLarge", null, locale);
            redirectAttributes.addFlashAttribute("error", errorMessage);
        } catch (IOException e) {
            // error with upload
            String errorMessage = messageSource.getMessage("error.uploadError", null, locale);
            redirectAttributes.addFlashAttribute("error", errorMessage);
        }
        return "redirect:/profile";
    }

    /**
    * Handles the GET request to show the form for editing the password.
    * If the user is authenticated, it adds the user to the model and returns the name of the view to show the form.
    * If the user is not authenticated, it redirects to the login page.
    *
    * @param model the model to add attributes to for the view
    * @return the name of the view to show, or a redirect instruction
    */
    @GetMapping("/editpassword")
    public String showEditPasswordForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            AbstractUser user = userService.getUserFromAuthentication(authentication);
            model.addAttribute("user", user);
            model.addAttribute("gardens", user.getOwnedGardens());
            return "editPasswordTemplate";
        } else {
            return "redirect:/login";
        }
    }

    /**
    * Handles the POST request to edit the password of the connected user.
    * This method performs several checks:
    * Verifies that the new password and the retyped password are the same.
    * Checks if the new password is valid according to the rules defined in PasswordChangeValidator.
    * If these checks pass, the password of the user is updated and the user is redirected to the profile page.
    * If any check fails or an exception occurs, an error message is added to the redirect attributes and the user is redirected back to the edit password page.
    *
    * @param oldPassword the old password of the user
    * @param newPassword the new password the user wants to set
    * @param retypePassword the new password retyped for confirmation
    * @param connectedUser the currently connected user
    * @param redirectAttributes the attributes to add to the redirect
    * @param request  HttpServletRequest object which contains information about the request.
    *                 Used here to get the locale from the request.
    * @return a redirect instruction
    */
    @PostMapping("/editpassword")
    public String editPassword(@RequestParam String oldPassword, @RequestParam String newPassword,
                               @RequestParam String retypePassword, Principal connectedUser,
                               RedirectAttributes redirectAttributes, Model model, HttpServletRequest request) {
        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        try {
            boolean passwordError = passwordsMatch(newPassword, retypePassword, model, messageSource, locale);
            if(passwordError){
                // 'Passwords do not match' error
                String errorMessage = messageSource.getMessage("error.passwordDontMatch", null, locale);
                redirectAttributes.addFlashAttribute("matchError", errorMessage);
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AbstractUser user = userService.getUserFromAuthentication(authentication);
            if (!PasswordChangeValidator.isValidPassword(newPassword, user)) {
                // Password strength error
                String errorMessage = messageSource.getMessage("error.passwordStrength", null, locale);
                redirectAttributes.addFlashAttribute("strengthError", errorMessage);
            }
            userService.changePassword(oldPassword, newPassword, retypePassword, connectedUser, request);
            return "redirect:/profile";
        } catch (Exception e) {
            if(e.getMessage().equals("Your old password is incorrect")){
                String errorMessage = messageSource.getMessage("error.oldPasswordIncorrect", null, locale);
                redirectAttributes.addFlashAttribute("incorrectError", errorMessage);
            }else{
                LOG.info(e.getMessage());
            }
        }
        return "redirect:/editpassword";
    }


    /**
     * GET mapping for the 'register as contractor' page. Only accessible to non-contractor users.
     * @return The contractor register page if visitor is not a contractor
     */
    @GetMapping("/profile/contractor")
    public String contractorRegistrationPage(
            @RequestParam(required = false, defaultValue = "") String description,
            @RequestHeader(required = false, value = "referer") String refererHeader,
            Model model
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (userService.getUserFromAuthentication(authentication) instanceof Contractor) {
            return "redirect:/profile";
        }

        // Check if referer points to "/profile/contractor" and fall back to "/profile" if it does
        if (refererHeader != null && refererHeader.contains("/profile/contractor")) {
            this.referer = "/profile";
        } else {
            this.referer = refererHeader != null ? FormValidation.processRefererWithoutIdContractor(refererHeader) : "/profile";
        }
        model.addAttribute("referer", this.referer);
        model.addAttribute("description", description);
        return "contractorRegisterTemplate";
    }

    /**
     * Convert existing user entity to a contractor entity
     * New contractor entity will inherent part of the user authentication
     * @param files List of user previous work pictures
     * @param formatted formatted string of the location
     * @param country country of the location
     * @param city city of the location
     * @param suburb suburb of the location
     * @param street street of the location
     * @param postcode postcode of the location
     * @param redirectAttributes add to the model for error messages
     * @param model the model
     * @param request used to get the locale
     * @return Redirect user back to profile page after a successful registration
     */
    @PostMapping("/profile/contractor")
    @Transactional
    public String registerContractor(@RequestParam String description,
                                     @RequestPart(name="validUpload", required = false) List<MultipartFile> files,
                                     @RequestParam(name="location") String formatted,
                                     @RequestParam(name="country") String country,
                                     @RequestParam(name="city") String city,
                                     @RequestParam(name="suburb", required = false, defaultValue = "") String suburb,
                                     @RequestParam(name="street", required = false, defaultValue = "") String street,
                                     @RequestParam(name="postcode", required = false, defaultValue = "") String postcode,
                                     @RequestParam(name="imagesPath", required = false, defaultValue = "") String imagesPath,
                                     RedirectAttributes redirectAttributes,
                                     Model model, HttpServletRequest request) {
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser user = userService.getUserFromAuthentication(authentication);
        if (user instanceof Contractor) {
            return "redirect:/profile";
        }

        boolean hasErrors = false;
        List<String> validImagePaths = new ArrayList<>();

        List<String> listOfPaths = imagesPath.isEmpty() ? new ArrayList<>() : Arrays.stream(imagesPath.trim().substring(1, imagesPath.length() - 1).split(", ")).filter(path -> !path.isEmpty()).toList();

        if (files.size() == 1 && files.getFirst().isEmpty()) {
            files.clear(); // Multipart file attached a random empty file when user don't upload any files
        }

        if (files.size() + listOfPaths.size() > 5) {
            String errorMessage = messageSource.getMessage("error.moreThanFive", null, locale);
            model.addAttribute("errorImagesMoreThanFive", errorMessage);
            hasErrors = true;
        } else {
            try {
                validImagePaths.addAll(fileService.uploadFiles(files));
                validImagePaths.addAll(listOfPaths);
            } catch (ImageTypeException e) {
                // 'Image must be of type png, jpg, or svg' error
                String errorMessage = messageSource.getMessage("error.invalidFileType", null, locale);
                model.addAttribute("errorImageFormat", errorMessage);
                hasErrors = true;
            } catch (FileSizeException e) {
                // 'Image must be less than 10MB' error
                String errorMessage = messageSource.getMessage("error.imageTooLargeContractor", null, locale);
                model.addAttribute("errorImageSize", errorMessage);
                hasErrors = true;
            }
        }

        // Validate details are correct before converting the user
        if (description.trim().isEmpty()) {
            String errorMessage = messageSource.getMessage("contractor.noDescriptionProvided", null, locale);
            model.addAttribute("errorDescription", errorMessage);
            hasErrors = true;
        }

        if (description.trim().length() > 1024) {
            String errorMessage = messageSource.getMessage("contractor.descriptionTooLong", null, locale);
            model.addAttribute("errorDescription", errorMessage);
            hasErrors = true;
        }

        // Attempt to fetch coordinates based on the provided location
        Location location = new Location(formatted, country, city, suburb, street, postcode);
        Map<String, Object> cord = locationService.fetchCoordinate(location);

        // If no coordinates are found, redirect with an error message
        if (cord == null || !cord.containsKey("lon") || !cord.containsKey("lat")) {
            String errorMessage = messageSource.getMessage("error.locationNotFound", null, locale);
            redirectAttributes.addFlashAttribute("locationError", errorMessage);
            model.addAttribute("locationError", errorMessage);
            hasErrors = true;
        }

        // If any errors, short circuits and sends user back to form
        if (hasErrors) {
            model.addAttribute("description", description);
            model.addAttribute("street", street);
            model.addAttribute("suburb", suburb);
            model.addAttribute("city", city);
            model.addAttribute("postcode", postcode);
            model.addAttribute("country", country);
            model.addAttribute("location", formatted);
            model.addAttribute("imagesPaths", validImagePaths);
            model.addAttribute("referer", this.referer);
            return "contractorRegisterTemplate";
        }

        // Save coordinates and proceed with user registration
        location.setLon((Double) cord.get("lon"));
        location.setLat((Double) cord.get("lat"));
        // save the location to the database
        location = locationService.saveLocation(location);

        model.addAttribute("location", formatted);
        model.addAttribute("referer", this.referer);

        // If all valid, convert user to contractor
        userService.convertUserToContractor((User) user, description, validImagePaths, location);

        // Fetch the updated contractor details
        Optional<Contractor> optional = userService.getContractorByUserId(user.getUserId());


        // Re-authenticate as a contractor if the conversion is successful
        if (optional.isPresent()) {
            Contractor contractor = optional.get();
            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                    contractor,
                    authentication.getCredentials(),
                    contractor.getAuthorities()
            );

            // Update the security context with the new authentication token
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }
        return "redirect:/profile";
    }

}