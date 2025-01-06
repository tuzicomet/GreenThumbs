package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.entity.dto.WeatherDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities.Temp;
import nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities.WeatherCurrent;
import nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities.WeatherEntry;
import nz.ac.canterbury.seng302.gardenersgrove.entity.weatherEntities.WeatherForecast;
import org.springframework.web.server.ResponseStatusException;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.validation.FormValidation;
import nz.ac.canterbury.seng302.gardenersgrove.utility.LocaleUtils;
import nz.ac.canterbury.seng302.gardenersgrove.validation.TagValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Controller for Add garden, edit garden, and my gardens
 */
@Controller
public class GardenFormController {
    private static final Logger LOG = LoggerFactory.getLogger(GardenFormController.class);

    private final GardenService gardenService;
    private final UserService userService;
    private final LocationService locationService;
    private String referer;
    private final PlantService plantService;
    private final ProfanityFilterService profanityFilterService;
    private final WeatherService weatherService;
    private final AlertService alertService;
    private final MessageSource messageSource;

    private final MailService mailService;
    private final RecentGardensService recentGardensService;

    @Autowired
    public GardenFormController(GardenService gardenService, UserService userService, PlantService plantService,
                                WeatherService weatherService, LocationService locationService,
                                ProfanityFilterService profanityFilterService, AlertService alertService, MailService mailService, MessageSource messageSource, RecentGardensService recentGardensService) {
        this.gardenService = gardenService;
        this.userService = userService;
        this.plantService = plantService;
        this.weatherService = weatherService;
        this.profanityFilterService = profanityFilterService;
        this.locationService = locationService;
        this.alertService = alertService;
        this.mailService = mailService;
        this.messageSource = messageSource;
        this.recentGardensService = recentGardensService;
    }

    /**
     * Gets form to be displayed, includes the ability to display results of
     * previous form when linked to from POST form
     *
     * @param name        previous name entered into the form
     * @param location    previous location entered into form
     * @param size        previous size entered into form (m^2)
     * @param description previous description entered into form
     * @param publicised  previous publicised (true or false) entered into form
     * @param referer     url of previous page when displaying form
     * @param model       (map-like) representation of name, location, and size for use
     *                    in thymeleaf
     * @return thymeleaf GardenFormTemplate
     */
    @GetMapping("/garden")
    public String form(@RequestParam(name = "name", required = false, defaultValue = "") String name,
                       @RequestParam(name = "location", required = false, defaultValue = "") String location,
                       @RequestParam(name = "size", required = false, defaultValue = "") Float size,
                       @RequestParam(name = "description", required = false, defaultValue = "") String description,
                       @RequestParam(name = "publicised", required = false, defaultValue = "false") Boolean publicised,
                       @RequestHeader(required = false) String referer,
                       Model model) {
        LOG.info("GET /garden");

        // if a previous page exists, then the referer is set to the url of previous page
        this.referer = FormValidation.processRefererWithoutId(referer);
        model.addAttribute("referer", this.referer);
        model.addAttribute("name", name);
        model.addAttribute("location", location);
        model.addAttribute("size", size);
        model.addAttribute("description", description);
        model.addAttribute("publicised", publicised);
        model.addAttribute("gardens", gardenService.getGardens());
        return "gardenFormTemplate";
    }

    /**
     * Gets all plant entries from the database and adds them to the model
     *
     * @param model (map-like) representation of results to be used by thymeleaf
     * @return a redirect to garden if the user is not the owner of the garden
     */
    @GetMapping("/garden/{gardenId}")
    public String responses(@PathVariable Long gardenId,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes,
                            @RequestParam(name = "publicised", required = false, defaultValue = "false") Boolean publicised,
                            Model model) {
        LOG.info("GET /garden/{}", gardenId);
        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        // get the currently logged-in user
        AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Get the garden with the given id, and validate that it exists.
        Garden garden;
        try {
            garden = FormValidation.validateGardenExists(gardenService.getGarden(gardenId));
            } catch (ResponseStatusException e) {
                String message = messageSource.getMessage("error.cannotViewPrivateGarden", null, locale);
                redirectAttributes.addFlashAttribute("gardenViewError", message);
                return "redirect:/garden";
        }

        // Check if the garden belongs to the user, otherwise disable the checkbox
        boolean isOwner = garden.getOwner().getUserId().equals(currentUser.getUserId());
        boolean isInUse = gardenService.isInUseForUnassignedServiceRequest(garden);

        // if the currently logged-in user is trying to own a private garden they do not own
        if (!isOwner && !garden.isPublicised()) {
            // Get the gardenViewError message and add it as a flash attribute
            String message = messageSource.getMessage("error.cannotViewPrivateGarden", null, locale);
            redirectAttributes.addFlashAttribute("gardenViewError", message);
            return "redirect:/garden";
        }
        recentGardensService.saveGardenVisit(currentUser, garden);
        Location location = garden.getLocation();
        if (location == null || location.getLat() == null || location.getLon() == null) {
            model.addAttribute("errorWeather", messageSource.getMessage("error.locationNotFoundForWeather", null, locale));
            model.addAttribute("responses", plantService.getPlantsInGarden(gardenId));
            model.addAttribute("gardens", gardenService.getGardens());
            model.addAttribute("garden", garden);
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("isInUse", isInUse);
            model.addAttribute("alerts", new ArrayList<Alert>());
            return "gardenDetailsTemplate";
        } else {
            WeatherForecast weatherForecast = weatherService.getWeather(
                    location.getLat(),
                    location.getLon(),
                    gardenId,
                    locale
            );
            WeatherCurrent currentWeather = weatherService.getCurrentWeather(
                    location.getLat(),
                    location.getLon(),
                    locale
            );
            if (weatherForecast != null && currentWeather != null) {
                WeatherEntry currentConditions = weatherForecast.getList().getFirst();
                LocalDateTime currentDate = LocalDateTime.now();
                WeatherDTO currentWeatherDisplay = new WeatherDTO(
                        currentDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale),
                        "%s %s".formatted(
                                currentDate.getDayOfMonth(),
                                currentDate.getMonth().getDisplayName(TextStyle.SHORT, locale)
                        ),
                        currentWeather.getWeather().getDescription(),
                        "https://openweathermap.org/img/wn/%s@4x.png".formatted(currentWeather.getWeather().getIcon()),
                        // Cast temperature to int, rounds to whole number
                        new int[]{(int) currentWeather.getMain().getTemp()},
                        currentConditions.getHumidity()
                );

                List<WeatherDTO> futureWeatherDisplay = weatherForecast
                        .getList()
                        .stream()
                        .skip(1) // forecast include current day, skip
                        .map(forecast -> {
                            Instant dateTimestamp = Instant.ofEpochSecond(forecast.getDt());
                            ZoneId zoneId = ZoneId.systemDefault();
                            LocalDateTime dateTime = dateTimestamp.atZone(zoneId).toLocalDateTime();
                            Temp temperature = forecast.getTemp();
                            return new WeatherDTO(
                                    dateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale),
                                    "%s %s".formatted(
                                            dateTime.getDayOfMonth(),
                                            dateTime.getMonth().getDisplayName(TextStyle.SHORT, locale)
                                    ),
                                    forecast.getWeather().getFirst().getDescription(),
                                    "https://openweathermap.org/img/wn/%s@4x.png".formatted(forecast.getWeather().getFirst().getIcon()),
                                    new int[]{(int) temperature.getMax(), (int) temperature.getMin()},
                                    forecast.getHumidity()
                            );
                        }).toList();

                model.addAttribute("currentWeather", currentWeatherDisplay);
                model.addAttribute("futureWeather", futureWeatherDisplay);
            } else {
                // Get the 'unable to retrieve weather' message and add it to the model as errorWeather
                String message = messageSource.getMessage("error.cannotRetrieveWeather", null, locale);
                model.addAttribute("errorWeather", message);            }
        }


        if (garden.getId() != null) {
            // this exists to deal with gardens that have been created without initialising alerts (for whatever reason, e.g. created before alerts were added, added to db manually)
            if(alertService.getAlertByType(garden.getId(), Alert.NEED_WATER) == null){
                Alert rainAlert = new Alert(garden.getId(), Instant.now(), Alert.NEED_WATER);
                alertService.setAlert(rainAlert);
            }
            if(alertService.getAlertByType(garden.getId(), Alert.DO_NOT_WATER) == null){
                Alert noRainAlert = new Alert(garden.getId(), Instant.now(), Alert.DO_NOT_WATER);
                alertService.setAlert(noRainAlert);
            }
            if(!alertService.getAllActiveAlertsFromGarden(garden.getId()).isEmpty()){
                weatherService.dismissIncorrectAlerts(garden);
            }
        }
        // Retrieve any existing flash attributes and add them to the model.
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        String errorImage = inputFlashMap != null ? (String) inputFlashMap.get("errorImage") : null;
        // If there was an error with the image
        if (errorImage != null) {
            model.addAttribute("errorImage", errorImage);
        }

        // Add attributes to the model so they can be rendered in the template
        model.addAttribute("responses", plantService.getPlantsInGarden(gardenId));
        model.addAttribute("gardens", gardenService.getGardens());
        model.addAttribute("garden", garden);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isInUse", isInUse);
        model.addAttribute("alerts", garden.getId() != null ? alertService.getAllActiveAlertsFromGarden(garden.getId()) : new ArrayList<Alert>());
        return "gardenDetailsTemplate";
    }

    /**
     * Gets form to be displayed, includes the ability to display results of
     * previous form when linked to from POST form
     *
     * @param name      previous name entered into the form
     * @param size      previous size entered into form (m^2)
     * @param description   previous description entered into form
     * @param publicised    previous publicised (true or false) entered into form
     * @param referer   url of previous page when displaying form
     * @param model     (map-like) representation of name, location, and size for use
     *                  in thymeleaf
     * @param request  HttpServletRequest object which contains information about the request.
     *                 Used here to get the locale from the request.
     * @return thymeleaf GardenFormTemplate
     */
    @GetMapping("/garden/{gardenId}/edit")
    public String editForm(RedirectAttributes redirectAttributes,
                           @RequestParam(name="name", required = false, defaultValue = "") String name,
                           @RequestParam(name="size", required = false, defaultValue = "") String size,
                           @RequestParam(name="description", required = false, defaultValue = "") String description,
                           @RequestParam(name="publicised", required = false, defaultValue = "false") Boolean publicised,
                           @PathVariable Long gardenId,
                           @RequestHeader(required = false) String referer,
                           Model model, HttpServletRequest request) {
        LOG.info("GET /garden/{}/edit", gardenId);

        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        // if a previous page exists, then the referer is set to the url of previous page
        this.referer = FormValidation.processRefererWithId(referer, gardenId);

        // get the currently logged-in user
        AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Garden garden;
        try {
            garden = FormValidation.validateGardenExists(gardenService.getGarden(gardenId));
            } catch (ResponseStatusException e) {
                String message = messageSource.getMessage("error.cannotViewPrivateGarden", null, locale);
                redirectAttributes.addFlashAttribute("gardenViewError", message);
                return "redirect:/garden";
        }

        // Check if the garden belongs to the user
        boolean isOwner = garden.getOwner().getUserId().equals(currentUser.getUserId());

        if (!isOwner) {
            // Get the 'cannot edit other users' gardens' message and add it to the model as gardenViewError
            String message = messageSource.getMessage("error.cannotEditOthersGardens", null, locale);
            redirectAttributes.addFlashAttribute("gardenViewError", message);
            return "redirect:/garden";
        }

        Location location = garden.getLocation();

        // If values haven't been given in request (first time on page), fill with OG data
        if (name.isEmpty()) {
            name = garden.getName();
            size = garden.getSize();
            description = garden.getDescription();
        }

        model.addAttribute("referer", this.referer);
        model.addAttribute("gardenId", gardenId);
        model.addAttribute("name", name);
        model.addAttribute("size", size);
        model.addAttribute("description", description);
        model.addAttribute("publicised", garden.isPublicised());

        // if the location isn't null, add the details to the model
        if (location != null) {
            model.addAttribute("location", location.getFormatted());
            model.addAttribute("country", location.getCountry());
            model.addAttribute("city", location.getCity());
            model.addAttribute("suburb", location.getSuburb());
            model.addAttribute("street", location.getStreet());
            model.addAttribute("postcode", location.getPostcode());
        } else {
        // if the location is null, set each attribute to be empty, resolves issue with location being null
            model.addAttribute("location", "");
            model.addAttribute("country", "");
            model.addAttribute("city", "");
            model.addAttribute("suburb", "");
            model.addAttribute("street", "");
            model.addAttribute("postcode", "");
        }

        model.addAttribute("gardens", gardenService.getGardens());
        return "editGardenFormTemplate";
    }

    /**
     * Gets all garden entries from the database and adds them to the model
     *
     * @param model (map-like) representation of results to be used by thymeleaf
     * @return thymeleaf myGardensTemplate
     */
    @GetMapping("/garden/responses")
    public String responses(Model model) {
        LOG.info("GET /garden/responses");
        model.addAttribute("responses", gardenService.getGardens());
        model.addAttribute("gardens", gardenService.getGardens());
        return "myGardensTemplate";
    }

    /**
     * Creates a garden with the specified name, location, size, description and if publicised.
     *
     * @param name          name of the garden
     * @param size          size of the garden in m^2
     * @param description   description of the garden
     * @param publicised    publicised (true or false) of the garden
     * @param formatted     formatted representation of the location
     * @param country       country of the garden location
     * @param city          city of the garden location
     * @param suburb        suburb of the garden location
     * @param street        street name of the garden location
     * @param postcode      postcode of the garden location
     * @param model         (map-like) representation of name, location, and size for use
     *                      in thymeleaf,
     *                      with values being set to relevant parameters provided.
     * @param request       HttpServletRequest object which contains information about the request.
     *                      Used here to get the locale from the request.
     * @return thymeleaf GardenFormTemplate
     */
    @PostMapping("/garden")
    public String submitForm(@RequestParam(name="name", required = true, defaultValue = "") String name,
                             @RequestParam(name="size", required = false, defaultValue = "") String size,
                             @RequestParam(name="description", required = false, defaultValue = "") String description,
                             @RequestParam(name="publicised", required = false, defaultValue = "false") Boolean publicised,
                             @RequestParam(name="location") String formatted,
                             @RequestParam(name="country") String country,
                             @RequestParam(name="city") String city,
                             @RequestParam(name="suburb", required = false, defaultValue = "") String suburb,
                             @RequestParam(name="street", required = false, defaultValue = "") String street,
                             @RequestParam(name="postcode", required = false, defaultValue = "") String postcode,
                             Model model, HttpServletRequest request) {
        LOG.info("POST /garden");

        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        LOG.info("CURRENT LOCALE: {}", locale);

        // Validate fields against regex rules
        List<String> errors = FormValidation.validateGardenPost(name, country, city, suburb, street, postcode, size, description, messageSource, locale, profanityFilterService);
        boolean errorExists = errors.stream()
                .anyMatch(s -> !s.isEmpty());

        Garden createdGarden = null;
        // If there aren't any errors, add to DB
        if (!errorExists) {
            AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Garden garden = new Garden(name, size, currentUser, description, publicised, null, null, true, null);

            Location location = new Location(formatted, country, city, suburb, street, postcode);
            Map<String, Object> cord =  locationService.fetchCoordinate(location);
            if (cord != null) {
                location.setLon((Double) cord.get("lon"));
                location.setLat((Double) cord.get("lat"));
            }
            garden.setLocation(location);
            locationService.saveLocation(location);
            createdGarden = gardenService.addGarden(garden);
        }

        model.addAttribute("referer", this.referer);
        model.addAttribute("name", name);
        model.addAttribute("location", formatted);
        model.addAttribute("country", country);
        model.addAttribute("city", city);
        model.addAttribute("suburb", suburb);
        model.addAttribute("street", street);
        model.addAttribute("postcode", postcode);
        model.addAttribute("size", size);
        model.addAttribute("description", description);
        model.addAttribute("publicised", publicised);
        model.addAttribute("errors", errors);
        model.addAttribute("gardens", gardenService.getGardens());

        if (!errorExists && createdGarden != null) {
            return "redirect:/garden/%d".formatted(createdGarden.getId());
        } else {
            return "gardenFormTemplate";
        }
    }

    /**
     * Creates a garden with the specified name, location, size, description and if publicised.
     *
     * @param name          name of the garden
     * @param size          size of the garden in m^2
     * @param description   description of the garden
     * @param publicised    publicised (true or false) of the garden
     * @param country       country of the garden location
     * @param city          city of the garden location
     * @param suburb        suburb of the garden location
     * @param street        street name of the garden location
     * @param postcode      postcode of the garden location
     * @param gardenId      ID of the garden being edited
     * @param model         (map-like) representation of name, location, and size for use
     *                      in thymeleaf,
     *                      with values being set to relevant parameters provided.
     * @param request       HttpServletRequest object which contains information about the request.
     *                      Used here to get the locale from the request.
     * @return thymeleaf GardenFormTemplate
     */
    @PostMapping("/garden/{gardenId}/edit")
    public String submitEditForm(@RequestParam(name="name", required = true, defaultValue = "") String name,
                                 @RequestParam(name="size", required = false, defaultValue = "") String size,
                                 @RequestParam(name="description", required = false, defaultValue = "") String description,
                                 @RequestParam(name="publicised", required = false, defaultValue = "false") Boolean publicised,
                                 @RequestParam(name="location", required = false) String formatted,
                                 @RequestParam(name="country") String country,
                                 @RequestParam(name="city") String city,
                                 @RequestParam(name="suburb", required = false, defaultValue = "") String suburb,
                                 @RequestParam(name="street", required = false, defaultValue = "") String street,
                                 @RequestParam(name="postcode", required = false, defaultValue = "") String postcode,
                                 @PathVariable Long gardenId,
                                 RedirectAttributes redirectAttributes,
                                 Model model, HttpServletRequest request) {
        LOG.info("POST /garden/{}/edit", gardenId);

        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        // get the currently logged-in user
        AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Garden garden = FormValidation.validateGardenExists(gardenService.getGarden(gardenId));

        // Check if the garden belongs to the user
        boolean isOwner = garden.getOwner().getUserId().equals(currentUser.getUserId());

        if (!isOwner) {
            // Get the 'cannot edit other users' gardens' message and add it to the model as gardenViewError
            String message = messageSource.getMessage("error.cannotEditOthersGardens", null, locale);
            redirectAttributes.addFlashAttribute("gardenViewError", message);
            return "redirect:/garden";
        }

        // Validate fields against regex rules
        List<String> errors = FormValidation.validateGardenPost(name, country, city, suburb, street, postcode, size, description, messageSource, locale, profanityFilterService);
        boolean errorExists = errors.stream()
                .anyMatch(s -> !s.isEmpty());

        // If there aren't any errors, edit the garden, and write it to DB
        if (!errorExists) {
            garden.setName(name);
            garden.setSize(size);
            garden.setDescription(description);
            garden.setPublicised(publicised != null && publicised); // Short circuits if null
            gardenService.addGarden(garden);

            Location location = garden.getLocation();
            if (location == null) {
                location = new Location();
            }
            location.setFormatted(formatted);
            location.setCountry(country);
            location.setCity(city);
            location.setSuburb(suburb);
            location.setStreet(street);
            location.setPostcode(postcode);
            Map<String, Object> cord =  locationService.fetchCoordinate(location);
            if (cord != null) {
                location.setLon((Double) cord.get("lon"));
                location.setLat((Double) cord.get("lat"));
            } else {
                location.setLon(null);
                location.setLat(null);
            }
            locationService.saveLocation(location);
            // Clear the forecast data when a location is changed, forcing an update from the API, fixes the issue of the forecast not updating when location is changed
            weatherService.clearForecastData(gardenId);
            alertService.resetAllAlertsOfGarden(gardenId);
            if (location.getLat() != null && location.getLon() != null) {
                weatherService.callForecastApi(location.getLat(), location.getLon(), garden, locale);
            }
        }

        model.addAttribute("referer", this.referer);
        model.addAttribute("name", name);
        model.addAttribute("location", formatted);
        model.addAttribute("country", country);
        model.addAttribute("city", city);
        model.addAttribute("suburb", suburb);
        model.addAttribute("street", street);
        model.addAttribute("postcode", postcode);
        model.addAttribute("size", size);
        model.addAttribute("description", description);
        model.addAttribute("publicised", publicised);
        model.addAttribute("errors", errors);
        model.addAttribute("gardens", gardenService.getGardens());

        if (!errorExists) {
            return "redirect:" + referer;
        } else {
            return "editGardenFormTemplate";
        }
    }


    /**
     * Updates garden publicity
     *
     * @param publicised boolean value of whether the garden is publicised
     * @param model (map-like) representation of garden to be used by thymeleaf
     * @param gardenId id of the garden to be deleted
     * @return thymeleaf myGardensTemplate
     */
    @PostMapping("/garden/{gardenId}/updateGardenPublicity")
    public String updateGardenPublicity(@RequestParam(name="publicised", required = true, defaultValue = "false") Boolean publicised,
                                        @PathVariable Long gardenId,
                                        Model model) {
        LOG.info("GardenPublicityUpdateTriggered");

        // Get the garden, set publicised value and update the database (only if it is not in use by an unassigned service request)
        Garden garden = FormValidation.validateGardenExists(gardenService.getGarden(gardenId));
        if(!gardenService.isInUseForUnassignedServiceRequest(garden)){
            garden.setPublicised(publicised);
            gardenService.addGarden(garden);
        }

        // Add the updated garden to the model
        Garden updatedGarden = FormValidation.validateGardenExists(gardenService.getGarden(gardenId));
        model.addAttribute("garden", updatedGarden);
        model.addAttribute("responses", plantService.getPlantsInGarden(gardenId));

        return "redirect:/garden/{gardenId}";
    }

    /**
     * Add a tag to a garden.
     * @param tagString Content of the tag
     * @param gardenId id of the garden the tag is being added to
     * @return a redirect to the garden details page
     */
    @PostMapping("/garden/{gardenId}/tag")
    public String addTag(@RequestParam(name = "tag") String tagString, @PathVariable Long gardenId, RedirectAttributes redirectAttributes, Model model, HttpServletRequest request) {
        AbstractUser currentUser = userService.getUserFromAuthentication(SecurityContextHolder.getContext().getAuthentication());
        Locale locale = LocaleUtils.getLocaleFromSession(request);
        Garden garden = FormValidation.validateGardenExists(gardenService.getGarden(gardenId));
        boolean isOwner = garden.getOwner().getUserId().equals(currentUser.getUserId());

        if (isOwner) {
            if (!TagValidator.isTagInvalid(tagString.strip(), model)) {
                boolean added = gardenService.addTagToGarden(garden, tagString.strip());
                if (!added) {
                    String message = messageSource.getMessage("error.tagNotAppropriate", null, locale);
                    model.addAttribute("tagError", message);
                    redirectAttributes.addFlashAttribute("tagError", model.getAttribute("tagError"));

                    if (currentUser.getNumberOfStrikes() == 5) {
                        mailService.sendFifthStrikesEmail(currentUser, locale);
                        redirectAttributes.addFlashAttribute("fifthStrike", true);
                    }

                    if (currentUser.getNumberOfStrikes() == 6) {
                        currentUser.setNumberOfStrikes(0);
                        userService.banUserForDays(currentUser, 7);

                        mailService.sendAccountBlockedEmail(currentUser, locale);

                        try {
                            request.logout();
                        } catch (Exception e) {
                            LOG.error("Error logging out user: {}", e.getMessage());
                        }

                        String blockedMessage = messageSource.getMessage("blockedMessage.text", null, locale);
                        redirectAttributes.addFlashAttribute("blockedMessage", blockedMessage);
                        return "redirect:/login";
                    }
                }
            } else {
                redirectAttributes.addFlashAttribute("tagError", model.getAttribute("tagError"));
                redirectAttributes.addFlashAttribute("tagString", tagString);
            }
        }
        return "redirect:/garden/{gardenId}";
    }

}
