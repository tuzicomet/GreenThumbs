package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.FileSizeException;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.ImageTypeException;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.RecentPlantsService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.FileValidation;
import nz.ac.canterbury.seng302.gardenersgrove.validation.FormValidation;
import nz.ac.canterbury.seng302.gardenersgrove.utility.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import static nz.ac.canterbury.seng302.gardenersgrove.controller.ImageController.USER_UPLOAD_MAPPING;
import static nz.ac.canterbury.seng302.gardenersgrove.validation.FormValidation.parseDate;

/**
 * Controller for Add plant, edit plant, and garden details
 */
@Controller
public class PlantFormController {
    private static final Logger LOG = LoggerFactory.getLogger(PlantFormController.class);

    private final PlantService plantService;
    private final GardenService gardenService;
    private final RecentPlantsService recentPlantsService;
    private final FileService fileService;
    private String referer;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    public PlantFormController(PlantService plantService, GardenService gardenService, FileService fileService, RecentPlantsService recentPlantsService) {
        this.plantService = plantService;
        this.gardenService = gardenService;
        this.fileService = fileService;
        this.recentPlantsService = recentPlantsService;
    }

    /**
     * Gets form to be displayed, includes the ability to display results of previous form
     * when linked to from POST form
     *
     * @param name                  name of the Plant
     * @param count                 total count of this plant for the given garden
     * @param description           description of the plant
     * @param date                  date the plant was planted
     * @param lastValidImagePath    web path to the last valid image uploaded by the user, or the placeholder if empty
     * @param referer               url of previous page when displaying form
     * @param model                 (map-like) representation of name, count, description and date for use
     *                              in thymeleaf.
     * @return                      thymeleaf plantFormTemplate
     */
    @GetMapping("/garden/{gardenId}/plant")
    public String form(@PathVariable Long gardenId,
                       @RequestParam(name = "name", required = false, defaultValue = "") String name,
                       @RequestParam(name = "count", required = false, defaultValue = "") String count,
                       @RequestParam(name = "description", required = false, defaultValue = "") String description,
                       @RequestParam(name = "date", required = false, defaultValue = "") LocalDate date,
                       @RequestParam(name = "imagePath", required = false, defaultValue = Plant.DEFAULT_IMAGE_PATH) String lastValidImagePath,
                       @RequestHeader(required = false) String referer,
                       Model model) {
        LOG.info("GET /garden/{}/plant", gardenId);

        // if a previous page exists, then the referer is set to the url of previous page
        this.referer = FormValidation.processRefererWithId(referer, gardenId);

        // Get the garden with the given id, and validate that it exists.
        FormValidation.validateGardenExists(gardenService.getGarden(gardenId));

        AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Get the garden with the given id, and validate that it exists.
        Garden garden = FormValidation.validateGardenExists(gardenService.getGarden(gardenId));

        // Check if the garden belongs to the user, otherwise disable the checkbox
        boolean isOwner = garden.getOwner().getUserId().equals(currentUser.getUserId());

        if (!isOwner) {
            return "redirect:/garden/{gardenId}";
        }

        // Add attributes to the model, so they can be rendered in the template
        model.addAttribute("referer", this.referer);
        model.addAttribute("name", name);
        model.addAttribute("count", count);
        model.addAttribute("description", description);
        model.addAttribute("date", date);
        model.addAttribute("gardens", gardenService.getGardens());
        model.addAttribute("imagePath", lastValidImagePath);

        return "plantFormTemplate";
    }

    /**
     * Creates a Plant with the specified name, count, description and date.
     *
     * @param name                  name of the Plant
     * @param count                 total count of this plant for the given garden
     * @param description           description of the plant
     * @param dateString            the plant was planted
     * @param image                 the MultipartFile uploaded by the user, represents the plant's profile image
     * @param lastValidImagePath    web path to the last valid image uploaded by the user
     * @param model                 (map-like) representation of name, count, description and date for use in thymeleaf,
     *                              with values being set to relevant parameters provided
     * @param request               HttpServletRequest object which contains information about the request.
     *                              Used here to get the locale from the request.
     * @return thymeleaf samplePlantFormTemplate
     */
    @PostMapping("/garden/{gardenId}/plant")
    public String submitForm(
            @PathVariable Long gardenId,
            @RequestParam(name = "name", defaultValue = "") String name,
            @RequestParam(name = "count", defaultValue = "") String count,
            @RequestParam(name = "description", defaultValue = "") String description,
            @RequestParam(name = "date", required = false, defaultValue = "") String dateString,
            @RequestParam(name = "image", required = false) MultipartFile image,
            @RequestParam(name = "imagePath") String lastValidImagePath,
            Model model, HttpServletRequest request) {
        LOG.info("POST /garden/{}/plant", gardenId);

        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        // Validate fields against regex rules
        String[] errors = FormValidation.validatePlantPost(name, count, description, dateString, messageSource, locale);
        String errorName = errors[0];
        String errorCount = errors[1];
        String errorDescription = errors[2];
        String errorDate = errors[3];
        String errorImage = null;

        // if an image was submitted
        if (!image.isEmpty()) {
            try {
                // validate the image to check that it meets requirements
                FileValidation.validateImage(image);
                lastValidImagePath = uploadImage(image);
            } catch (ImageTypeException | FileSizeException e) {
                // If submitted image is not valid
                errorImage = e.getMessage();
            }
        }

        LocalDate date;
        Garden garden = FormValidation.validateGardenExists(gardenService.getGarden(gardenId));

        // If there are no errors
        if (errorName.isEmpty() && errorCount.isEmpty() && errorDescription.isEmpty() && errorDate.isEmpty() && errorImage == null) {
            // convert the inputted date string into a date, so it can be saved in the database
            date = parseDate(dateString);
            // add the plant to the database
            Plant newPlant = plantService.addPlant(new Plant(garden, name, count, description, date, lastValidImagePath));
            recentPlantsService.savePlantVisit((AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), newPlant);
        }

        // Add attributes to the model, so they can be rendered in the template
        model.addAttribute("referer", this.referer);
        model.addAttribute("garden", garden);
        model.addAttribute("name", name);
        model.addAttribute("count", count);
        model.addAttribute("description", description);
        model.addAttribute("date", dateString);
        model.addAttribute("imagePath", lastValidImagePath);
        model.addAttribute("errorName", errorName);
        model.addAttribute("errorCount", errorCount);
        model.addAttribute("errorDescription", errorDescription);
        model.addAttribute("errorImage", errorImage);
        model.addAttribute("errorDate", errorDate);
        model.addAttribute("gardens", gardenService.getGardens());

        // Redirect to the garden's page if no errors, otherwise reload the add plant form
        if (errorName.isEmpty() && errorCount.isEmpty() && errorDescription.isEmpty() && errorDate.isEmpty() && errorImage == null){
            return "redirect:/garden/{gardenId}";
        } else {
            return "plantFormTemplate";
        }
    }

    /**
     * Gets form to be displayed, includes the ability to display results of previous form
     * when linked to from POST form
     *
     * @param name          previous name entered into the form
     * @param count         previous count entered into form
     * @param description   previous description entered
     * @param date          previous date entered into the form
     * @param referer       url of previous page when displaying form
     * @param model         (map-like) representation of name, count, and description for use
     *                      in thymeleaf
     * @return thymeleaf samplePlantFormTemplate
     */
    @GetMapping("/garden/{gardenId}/plant/{plantId}/edit")
    public String editForm(@RequestParam(name = "name", required = false, defaultValue = "") String name,
                           @RequestParam(name = "count", required = false, defaultValue = "") String count,
                           @RequestParam(name = "description", required = false, defaultValue = "") String description,
                           @RequestParam(name = "date", required = false, defaultValue = "") LocalDate date,
                           @RequestParam(name = "image", required = false) MultipartFile image,
                           @RequestParam(name = "imagePath", required = false) String lastValidImagePath,
                           @PathVariable Long plantId,
                           @PathVariable Long gardenId,
                           @RequestHeader(required = false) String referer,
                           Model model) {
        LOG.info("GET /garden/{}/plant/{}/edit", gardenId, plantId);

        // if a previous page exists, then the referer is set to the url of previous page
        this.referer = FormValidation.processRefererWithId(referer, gardenId);

        // Get the plant with the given id, and validate that it exists.
        Plant plant = FormValidation.validatePlantExists(plantService.getPlant(plantId));
        recentPlantsService.savePlantVisit((AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), plant);

        // If values haven't been given in request (first time on page),
        // fill with form's fields with the plant's current attributes
        if (name.isEmpty()) {
            name = plant.getName();
            count = plant.getCount();
            description = plant.getDescription();
            date = plant.getDate();
            lastValidImagePath = plant.getImagePath();
        }

        // Add attributes to the model, so they can be rendered in the template
        model.addAttribute("referer", this.referer);
        model.addAttribute("gardenId", gardenId);
        model.addAttribute("plantId", plantId);
        model.addAttribute("name", name);
        model.addAttribute("count", count);
        model.addAttribute("description", description);
        if (date == null) {
            model.addAttribute("date", "");
        } else {
            model.addAttribute("date", date.format(DateTimeFormatter.ofPattern(FormValidation.DATE_FORMAT)));
        }
        if (image != null) {
            model.addAttribute("image", image);
        }
        model.addAttribute("imagePath", lastValidImagePath);
        model.addAttribute("gardens", gardenService.getGardens());

        return "editPlantFormTemplate";
    }

    /**
     * Creates a garden with the specified name, location, and size.
     *
     * @param name                  name of the garden
     * @param count                 count of the garden
     * @param description           description of the plant
     * @param dateString            String representation of the plant's creation date in yyyy-MM-dd
     * @param image                 the MultipartFile uploaded by the user, represents the plant's profile image
     * @param lastValidImagePath    web path to the last valid image uploaded by the user
     * @param model                 (map-like) representation of parameters for use
     *                              in thymeleaf, with values being set to relevant parameters provided.
     * @param request               HttpServletRequest object which contains information about the request.
     *                              Used here to get the locale from the request.
     * @return thymeleaf samplePlantFormTemplate
     */
    @PostMapping("/garden/{gardenId}/plant/{plantId}/edit")
    public String submitEditForm(@RequestParam(name = "name", defaultValue = "") String name,
                                 @RequestParam(name = "count", required = false, defaultValue = "") String count,
                                 @RequestParam(name = "description", required = false, defaultValue = "") String description,
                                 @RequestParam(name = "date", required = false, defaultValue = "") String dateString,
                                 @RequestParam(name = "image", required = false) MultipartFile image,
                                 @RequestParam(name = "imagePath") String lastValidImagePath,
                                 @PathVariable Long plantId,
                                 @PathVariable Long gardenId,
                                 Model model, HttpServletRequest request) {
        LOG.info("POST /garden/{}/plant/{}/edit", gardenId, plantId);

        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        Plant plant = FormValidation.validatePlantExists(plantService.getPlant(plantId));
        Garden garden = FormValidation.validateGardenExists(gardenService.getGarden(gardenId));

        // Validate fields against regex rules
        String[] errors = FormValidation.validatePlantPost(name, count, description, dateString, messageSource, locale);
        String errorName = errors[0];
        String errorCount = errors[1];
        String errorDescription = errors[2];
        String errorDate = errors[3];
        String errorImage = null;

        // if an image was submitted
        if (!image.isEmpty()) {
            try {
                // validate the image to check that it meets requirements. if valid, upload so can be shown if errors
                FileValidation.validateImage(image);
                lastValidImagePath = uploadImage(image);
            } catch (ImageTypeException | FileSizeException e) {
                // If submitted image is not valid
                errorImage = e.getMessage();
            }
        }

        LocalDate date;

        // If there aren't any errors
        if (errorName.isEmpty() && errorCount.isEmpty() && errorDescription.isEmpty() && errorDate.isEmpty() && errorImage == null) {
            // convert the inputted date string into a date, so it can be saved in the database
            date = parseDate(dateString);
            // update the existing plant's fields with the updated details
            plant.setName(name);
            plant.setCount(count);
            plant.setDescription(description);
            plant.setDate(date);
            plant.setImagePath(lastValidImagePath);
            // save the updated plant to the database
            plantService.addPlant(plant);
        }

        // Add attributes to the model, so they can be rendered in the template
        model.addAttribute("referer", this.referer);
        model.addAttribute("garden", garden);
        model.addAttribute("name", name);
        model.addAttribute("count", count);
        model.addAttribute("description", description);
        model.addAttribute("date", dateString);
        model.addAttribute("gardens", gardenService.getGardens());

        // only used if redirects back to edit form - if errors
        model.addAttribute("imagePath", lastValidImagePath);
        model.addAttribute("errorName", errorName);
        model.addAttribute("errorCount", errorCount);
        model.addAttribute("errorDescription", errorDescription);
        model.addAttribute("errorDate", errorDate);
        model.addAttribute("errorImage", errorImage);

        // Redirect to the garden's page if no errors, otherwise reload the edit plant form
        if (errorName.isEmpty() && errorCount.isEmpty() && errorDescription.isEmpty() && errorDate.isEmpty() && errorImage == null) {
            return "redirect:/garden/{gardenId}";
        } else {
            return "editPlantFormTemplate";
        }
    }

    /**
     * Adds the supplied image to the filesystem and returns the path
     * @param image the image to be added
     * @return the path of the image relative to the upload directory, or null if failed
     */
    public String uploadImage(MultipartFile image) throws ResponseStatusException {
        // add the image to the file system
        Optional<Path> path = fileService.addFile(image, "images");
        // If the image was successfully uploaded, return its path. Otherwise, return null
        return path.map(value -> "/user_uploads/" + value.getFileName()).orElse(null);
    }

    /**
     * Adds the supplied image to the filesystem and returns the path
     * Overloaded version for forms that only edit the image
     * @param image the image to be added
     * @return the path of the image relative to the upload directory, or null if failed
     */
    @PostMapping("/garden/{gardenId}/plant/{plantId}")
    public RedirectView uploadImage(@RequestParam(name = "image") MultipartFile image,
                              @PathVariable Long gardenId,
                              @PathVariable Long plantId,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request
    ) throws ResponseStatusException {

        AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Get the garden with the given id, and validate that it exists.
        Garden garden = FormValidation.validateGardenExists(gardenService.getGarden(gardenId));

        // Check if the garden belongs to the user, otherwise disable the checkbox
        boolean isOwner = garden.getOwner().getUserId().equals(currentUser.getUserId());

        if(isOwner) {
            try {
                Plant plant = FormValidation.validatePlantExists(plantService.getPlant(plantId));
                // validate that the uploaded image meets requirements
                FileValidation.validateImage(image);
                // add the image to the filesystem
                Optional<Path> path = fileService.addFile(image, "images");
                // get a relative path to the uploaded image
                String imageRelativePath = path.map(value -> "/%s/%s".formatted(USER_UPLOAD_MAPPING, value.getFileName())).orElse(null);
                // update the plant's image path with the path to their newly uploaded image
                plant.setImagePath(imageRelativePath);
                // save the changes made to the plant
                plantService.addPlant(plant);
            } catch (ResponseStatusException | ImageTypeException | FileSizeException e) {
                // if an error occurred with uploading the image, add error message to flash attributes
                redirectAttributes.addFlashAttribute("errorImage", e.getMessage());
            }
        }

        // Redirect back to the page that called it
        String previousPage = request.getHeader("Referer");
        return new RedirectView(previousPage);
    }
}
