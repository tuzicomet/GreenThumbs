package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.WidgetPreferences;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.RecentGardensService;
import nz.ac.canterbury.seng302.gardenersgrove.service.RecentPlantsService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.WidgetPreferencesService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.FormValidation;
import nz.ac.canterbury.seng302.gardenersgrove.validation.UserInformationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


/**
 * This is a basic spring boot controller, note the @link{Controller} annotation which defines this.
 * This controller defines endpoints as functions with specific HTTP mappings
 */
@Controller
public class HomepageController {

    private static final Logger LOG = LoggerFactory.getLogger(HomepageController.class);
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserInformationValidator userInformationValidator;
    private final WidgetPreferencesService widgetPreferencesService;
    private final RecentGardensService recentGardensService;
    private final RecentPlantsService recentPlantsService;
    private String referer;

    @Autowired
    public HomepageController(UserRepository userRepository, UserService userService, UserInformationValidator userInformationValidator, WidgetPreferencesService widgetPreferencesService, RecentGardensService recentGardensService, RecentPlantsService recentPlantsService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.userInformationValidator = userInformationValidator;
        this.widgetPreferencesService = widgetPreferencesService;
        this.recentGardensService = recentGardensService;
        this.recentPlantsService = recentPlantsService;
    }


    @GetMapping(value = "/")
    public String toHomepage() {
        return "redirect:/homepage";
    }


    @GetMapping(value = "/homepage")
    public String homepage(Model model, RedirectAttributes redirectAttributes) {
        LOG.info("/GET /homepage");

        model.addAttribute("user", null);
        model.addAttribute("isUserVerified", false);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

            AbstractUser user = userService.getUserFromAuthentication(authentication);

            if (!user.isEnabled()) {
                // if the user is not yet enabled, redirect to activation page
                redirectAttributes.addFlashAttribute("user", user);
                redirectAttributes.addFlashAttribute("profile", "false");
                return "redirect:/activate";
            }

            WidgetPreferences widgetPreferences = widgetPreferencesService.findByUserId(user.getUserId());

            // Add friends components if friends widget is enabled
            boolean friendWidgetEnabled = widgetPreferences.getFriends();
            model.addAttribute("friendWidgetEnabled", friendWidgetEnabled);
            if (friendWidgetEnabled) {
                List<AbstractUser> pendingInvites = userService.getUsersWithPendingRequestsToUser(user);
                List<AbstractUser> friends = userService.getFriendsOfUser(user);

                model.addAttribute("pendingInvites", pendingInvites);
                model.addAttribute("friends", friends);
            }

            // Add recent garden components if gardens widget is enabled
            boolean recentGardensWidgetEnabled = widgetPreferences.getRecentGardens();
            model.addAttribute("recentGardensWidgetEnabled", recentGardensWidgetEnabled);
            if (recentGardensWidgetEnabled) {
                model.addAttribute("recentGardens", recentGardensService.getRecentGardens(user));
            }

            // Add recent plant components if plants widget is enabled
            boolean recentPlantsWidgetEnabled = widgetPreferences.getRecentPlants();
            model.addAttribute("recentPlantsWidgetEnabled", recentPlantsWidgetEnabled);
            if (recentPlantsWidgetEnabled) {
                model.addAttribute("recentPlants", recentPlantsService.getRecentPlants(user));
            }

            model.addAttribute("user", user);
            model.addAttribute("gardens", user.getOwnedGardens());
        }

        return "homepageTemplate";
    }

    /**
     * Gets form to be displayed, includes the ability to display results of
     * previous form when linked to from POST form
     * @param model       (map-like) representation of recentGardens, recentPlants, and friends for use
     *                    in thymeleaf
     * @return thymeleaf editMainPageFormTemplate
     */
    @GetMapping("/homepage/edit")
    public String editForm(@RequestHeader(required = false) String referer,
                           Model model) {
        LOG.info("GET /homepage/edit");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser user = userService.getUserFromAuthentication(authentication);

        WidgetPreferences widgetPreferences = widgetPreferencesService.findByUserId(user.getUserId());

        // if a previous page exists, then the referer is set to the url of previous page
        this.referer = FormValidation.processRefererWithoutId(referer);
        model.addAttribute("referer", this.referer);

        model.addAttribute("userId", user.getUserId());
        model.addAttribute("recentGardens", widgetPreferences.getRecentGardens());
        model.addAttribute("recentPlants", widgetPreferences.getRecentPlants());
        model.addAttribute("friends", widgetPreferences.getFriends());

        model.addAttribute("widgetPreferences", widgetPreferencesService.getWidgetPreferences());
        return "editMainPageFormTemplate";
    }

    @PostMapping(value = "/homepage")
    public String toProfile() {
        LOG.info("POST /homepage");
        return "redirect:/profile";
    }

    /**
     * Updates main page widgets visibility
     *a
     * @param recentGardens boolean value of whether the recently accessed gardens are visible
     * @param recentPlants boolean value of whether the recently accessed plants are visible
     * @param friends boolean value of whether my friends list is visible
     * @param model (map-like) representation of garden to be used by thymeleaf
     * @return thymeleaf editMainPageFormTemplate
     */
    @PostMapping("/homepage/edit")
    public String submitEditForm(@RequestParam(name="recentGardens", required = false) Boolean recentGardens,
                                 @RequestParam(name="recentPlants", required = false) Boolean recentPlants,
                                 @RequestParam(name="friends", required = false) Boolean friends,
                                 Model model) {
        LOG.info("POST /homepage/edit");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser user = userService.getUserFromAuthentication(authentication);

        WidgetPreferences widgetPreferences = widgetPreferencesService.findByUserId(user.getUserId());

        widgetPreferences.setRecentGardens(recentGardens != null);
        widgetPreferences.setRecentPlants(recentPlants != null);
        widgetPreferences.setFriends(friends != null);
        widgetPreferencesService.addWidgetPreference(widgetPreferences);

        model.addAttribute("referer", this.referer);
        model.addAttribute("widgetPreferences", widgetPreferencesService.getWidgetPreferences());

        return "redirect:/homepage";
    }
}
