package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.FileSizeException;
import nz.ac.canterbury.seng302.gardenersgrove.exceptions.ImageTypeException;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.utility.LocaleUtils;
import nz.ac.canterbury.seng302.gardenersgrove.validation.FormValidation;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ServiceRequestValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static nz.ac.canterbury.seng302.gardenersgrove.validation.FileValidation.validateImage;
import static nz.ac.canterbury.seng302.gardenersgrove.validation.ServiceRequestValidation.validateServiceRequestExists;
import static nz.ac.canterbury.seng302.gardenersgrove.validation.FormValidation.parseDate;

@Controller
public class MyServiceRequestsController {
    private final UserService userService;
    private final ServiceRequestService serviceRequestService;
    private final MessageSource messageSource;
    private static final Logger LOG = LoggerFactory.getLogger(MyServiceRequestsController.class);
    private final GardenService gardenService;
    private final JobApplicationService jobApplicationService;
    private final FileService fileService;
    private final QuestionAnswerService questionAnswerService;
    private final MailService mailService;

    public static final String MALICIOUS_ERROR_REDIRECT = "redirect:/myServiceRequests";
    private static final String CONTAINS_LETTER_REGEX = "^[^\\p{L}]*\\p{L}.*$";
    private static final int MAX_UNANSWERED_QUESTIONS = 3;

    @Autowired
    public MyServiceRequestsController(UserService userService, ServiceRequestService serviceRequestService,
                                       GardenService gardenService, JobApplicationService jobApplicationService,
                                       MessageSource messageSource, FileService fileService, QuestionAnswerService questionAnswerService,
                                       MailService mailService) {
        this.userService = userService;
        this.serviceRequestService = serviceRequestService;
        this.gardenService = gardenService;
        this.jobApplicationService = jobApplicationService;
        this.messageSource = messageSource;
        this.fileService = fileService;
        this.questionAnswerService = questionAnswerService;
        this.mailService = mailService;
    }

    /**
    * Handles GET requests for displaying the 'my service requests' page
    *
    * @param tab the tab to be displayed, defaults to current
    * @param model the model to be used
    * @return the myServiceRequests page
    */
    @GetMapping(value = "/myServiceRequests")
    public String myServiceRequests(@RequestParam(value = "tab", required = false, defaultValue = "current") String tab, Model model,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(defaultValue = "id,desc") String[] sort) {
        LOG.info("GET /myServiceRequests");

        if (page < 0 || size < 0) {
            return "redirect:/myServiceRequests?tab=" + tab + "&page=0&size=10";
        }

        // Retrieve the currently logged-in user's authentication details from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        // Check if the user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            // Retrieve the currently authenticated user's details
            AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
            // Add the user object to the model attribute, under the name 'user'
            model.addAttribute("user", currentUser);
            // Add the user's gardens to the model, so they can be shown on the navbar
            model.addAttribute("gardens", currentUser.getOwnedGardens());
            model.addAttribute("activeTab", tab);

            List<Order> orders = new ArrayList<>();
            for (String sortOrder : sort) {
                String[] sortArray = sortOrder.split(",");
                if (sortArray.length == 2) {
                    orders.add(new Order(getSortDirection(sortArray[1]), sortArray[0]));
                }
            }

            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));
            Page<ServiceRequest> pageRequests;

            if (tab.equals("current")) {
                pageRequests = serviceRequestService.getCurrentServiceRequests(currentUser, pagingSort);
            } else {
                pageRequests = serviceRequestService.getPastServiceRequests(currentUser, pagingSort);
            }

            int totalPages = pageRequests.getTotalPages();
            if (page > totalPages) {
                return "redirect:/myServiceRequests?tab=" + tab + "&page=0&size=" + size;
            }

            List<ServiceRequest> requests = pageRequests.getContent();
            model.addAttribute("currentPage", pageRequests.getNumber());
            model.addAttribute("size", pageRequests.getSize());
            model.addAttribute("totalItems", pageRequests.getTotalElements());
            model.addAttribute("totalPages", pageRequests.getTotalPages());
            model.addAttribute("requests", requests);
            model.addAttribute("listIsEmpty", requests.isEmpty());
            model.addAttribute("page", page);
    }
        return "myServiceRequestsTemplate";
    }

    /**
     * Handles the sorting for the results and searching, not necessary but nice to have.
     *
     * @param direction String indicating which direction it should be sorted ie ASC
     * @return Sort direction indicating how it should be sorted
     */
    private Sort.Direction getSortDirection(String direction ) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }

    /**
    * Handles the GET request for the newServiceRequest page
    *
    * @return The newServiceRequest page
    */
    @GetMapping(value = "/newServiceRequest")
    public String newServiceRequest(
            Model model,
            HttpServletRequest request) {
        LOG.info("GET /newServiceRequest");

        // get the user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser user = userService.getUserFromAuthentication(authentication);
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        model.addAttribute("gardens", user.getOwnedPublicGardens());
        model.addAttribute("imagePath", Plant.DEFAULT_IMAGE_PATH);
        model.addAttribute("frontEndErrorImageTooLarge", messageSource.getMessage("error.imageTooLarge", null, locale));
        model.addAttribute("frontEndErrorImageFormat", messageSource.getMessage("error.imageFormat", null, locale));

        return "newServiceRequestTemplate";
    }

    /**
     * Handles the GET request for the editServiceRequest page
     * @param model model for the form
     * @param title title that the user enters
     * @param description description that the user enters
     * @param dateMin minimum start date that the user enters
     * @param dateMax maximum end date that the user enters
     * @param priceMin minimum price that the user enters
     * @param priceMax maximum price that the user enters
     * @param gardenId Id of the garden the user selects from the dropdown
     * @return The editServiceRequest page
     */
    @GetMapping(value = "/serviceRequest/{serviceRequestId}/edit")
    public String editServiceRequest(Model model,
                                     @RequestParam(name = "title", required = false, defaultValue = "") String title,
                                     @RequestParam(name = "description", required = false, defaultValue = "") String description,
                                     @RequestParam(name = "dateMin", required = false, defaultValue = "") Instant dateMin,
                                     @RequestParam(name = "dateMax", required = false, defaultValue = "") Instant dateMax,
                                     @RequestParam(name = "priceMin", required = false, defaultValue = "0") double priceMin,
                                     @RequestParam(name = "priceMax", required = false, defaultValue = "0") double priceMax,
                                     @PathVariable Long serviceRequestId,
                                     // Backend handles all errors, so a missing garden must be allowed to show the error
                                     @RequestParam(name = "gardenId", required = false) Long gardenId,
                                     @RequestParam(name = "imagePath", required = false) String imagePath,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        LOG.info("GET /editServiceRequest/{}", serviceRequestId);
        Locale locale = LocaleUtils.getLocaleFromSession(request);
        model.addAttribute("frontEndErrorImageTooLarge", messageSource.getMessage("error.imageTooLarge", null, locale));
        model.addAttribute("frontEndErrorImageFormat", messageSource.getMessage("error.imageFormat", null, locale));

        // get the user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser currentUser = userService.getUserFromAuthentication(authentication);

        ServiceRequest serviceRequest = validateServiceRequestExists(serviceRequestService.findById(serviceRequestId));

        boolean isOwner = serviceRequest.getOwner().getUserId().equals(currentUser.getUserId());

        if (!isOwner) {
            String message = messageSource.getMessage("error.cannotAccessServiceRequest", null, locale);
            redirectAttributes.addFlashAttribute("accessError", message);
            return MALICIOUS_ERROR_REDIRECT;
        }

        //Verify service request has not been assigned to a contractor
        if (serviceRequest.getContractor() != null) {
            String message = messageSource.getMessage("error.cannotEditServiceRequest", null, locale);
            redirectAttributes.addFlashAttribute("accessError", message);
            return MALICIOUS_ERROR_REDIRECT;
        }

        //Verify if the service request is expired
        if (serviceRequest.getDateMax().isBefore(Instant.now())) {
            String message = messageSource.getMessage("error.expiredServiceRequest", null, locale);
            redirectAttributes.addFlashAttribute("accessError", message);
            return MALICIOUS_ERROR_REDIRECT;
        }

        if (title.isEmpty()) {
            title = serviceRequest.getTitle();
            description = serviceRequest.getDescription();
            dateMin = serviceRequest.getDateMin();
            dateMax = serviceRequest.getDateMax();
            priceMin = serviceRequest.getPriceMin();
            priceMax = serviceRequest.getPriceMax();
            gardenId = serviceRequest.getGarden().getId();
        }

        imagePath = imagePath == null ? serviceRequest.getImagePath() : imagePath;

        model.addAttribute("title", title);
        model.addAttribute("description", description);
        model.addAttribute("dateMin", LocalDate.ofInstant(dateMin, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(FormValidation.DATE_FORMAT)));
        model.addAttribute("dateMax", LocalDate.ofInstant(dateMax, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(FormValidation.DATE_FORMAT)));
        model.addAttribute("priceMin", priceMin);
        model.addAttribute("priceMax", priceMax);
        model.addAttribute("imagePath", imagePath);
        model.addAttribute("gardenId", gardenId);
        model.addAttribute("serviceRequestId", serviceRequestId);
        model.addAttribute("gardens", currentUser.getOwnedPublicGardens());
        model.addAttribute("prevDateMin", LocalDate.ofInstant(dateMin, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(FormValidation.DATE_FORMAT)));

        return "editServiceRequestTemplate";
    }

    /**
     * Get request for the service details, will show all of the information about a service.
     * @param id id of the serviceRequest
     * @param model model for the template
     * @return the template or redirect if the service request doesnt exist.
     */
    @GetMapping("/serviceRequest/{id}")
    public String serviceRequestDetails(@PathVariable Long id,
                                        Model model,
                                        @RequestHeader(value = "Referer", required = false) String referer,
                                        RedirectAttributes redirectAttributes,
                                        HttpServletRequest request) {
        // Check if the requested service request exists
        Optional<ServiceRequest> serviceRequestOptional = serviceRequestService.findById(id);
        if (serviceRequestOptional.isEmpty()) {
            return MALICIOUS_ERROR_REDIRECT;
        }

        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        // get the currently logged-in user
        AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean isOwner = serviceRequestOptional.get().getOwner().getUserId().equals(currentUser.getUserId());
        boolean isAssigned = serviceRequestOptional.get().getContractor() != null;
        boolean isAssignedContractor = false;
        if (serviceRequestOptional.get().getContractor() != null) {
             isAssignedContractor = serviceRequestOptional.get().getContractor().getUserId().equals(currentUser.getUserId());
        }

        if (!isOwner && !(currentUser instanceof Contractor)) {
            String message = messageSource.getMessage("error.cannotAccessServiceRequest", null, locale);
            redirectAttributes.addFlashAttribute("accessError", message);
            return MALICIOUS_ERROR_REDIRECT;
        }

        ServiceRequest serviceRequest = serviceRequestOptional.get();
        model.addAttribute("serviceRequest", serviceRequest);
        String refererProcessed = FormValidation.processRefererWithoutId(referer);

        List<JobApplication> jobApplications = jobApplicationService.getActiveJobApplicationsByRequestId(id);

        if (isOwner) {
            model.addAttribute("jobApplications", jobApplications);
        }
        List<QuestionAnswer> questionAnswers = questionAnswerService.findQuestionAnswersByServiceRequest(id);
        model.addAttribute("questionAnswerList", questionAnswers);

        // If there was an error from the question submission, add old input and error to model.
        Map<String, ?> flashAttributes = RequestContextUtils.getInputFlashMap(request);
        updateModelWithQAErrors(flashAttributes, model);

        List<Contractor> appliedContractors = jobApplications.stream().map(JobApplication::getContractor).toList();
        List<Long> appliedContractorsIds = appliedContractors.stream().map(Contractor::getUserId).toList();
        model.addAttribute("appliedContractorsIds", appliedContractorsIds);

        if ((isOwner || isAssignedContractor) && isAssigned) {
            String agreedDate = serviceRequest.getAgreedDateString();
            String agreedPrice = String.valueOf(serviceRequest.getAgreedPrice());
            Contractor assignedContractor = serviceRequest.getContractor();
            model.addAttribute("agreedDate", agreedDate);
            model.addAttribute("agreedPrice", agreedPrice);
            model.addAttribute("assignedContractor", assignedContractor);
        }

        boolean hasMaxUnansweredQuestions = !isOwner && questionAnswerService.getNumberOfUnansweredQuestions(serviceRequest, (Contractor) currentUser)
                >= MAX_UNANSWERED_QUESTIONS;

        if (isOwner && questionAnswerService.getTotalNumberOfUnansweredQuestions(serviceRequest) == 0) {
            model.addAttribute("noUnansweredQuestions", true);
        }

        model.addAttribute("userId", currentUser.getUserId());
        model.addAttribute("referer", refererProcessed);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isAssigned", isAssigned);
        model.addAttribute("isAssignedContractor", isAssignedContractor);
        model.addAttribute("isExpired", serviceRequest.getDateMax().isBefore(Instant.now()));
        model.addAttribute("hasMaxUnansweredQuestions", hasMaxUnansweredQuestions);
        model.addAttribute("placeholderText", hasMaxUnansweredQuestions ?
                messageSource.getMessage("message.tooManyQuestions", null, locale)
                : messageSource.getMessage("serviceDetails.AskAQuestion", null, locale));
        return "serviceRequestDetailsTemplate";

    }

    /**
     * Post request to accept a job application for a service request.
     * Sets the contractor of the service request and updates job applications status.
     *
     * @param id id of the service request
     * @param applicationId id of the job application to accept
     * @return the template or redirect if the service request or application doesn't exist
     */
    @PostMapping("/serviceRequest/{id}/accept")
    public String acceptJobApplication(@PathVariable Long id,
                                       @RequestParam("applicationId") Long applicationId) {

        // Check if the specified service request exists
        Optional<ServiceRequest> serviceRequestOptional = serviceRequestService.findById(id);
        if (serviceRequestOptional.isEmpty()) {
            return MALICIOUS_ERROR_REDIRECT;
        }
        ServiceRequest serviceRequest = serviceRequestOptional.get();

        // Check if the service request has already been assigned to a contractor
        if (serviceRequest.getContractor() != null) {
            return MALICIOUS_ERROR_REDIRECT;
        }

        // Check if the requesting user is the owner of the service request
        AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isOwner = serviceRequest.getOwner().getUserId().equals(currentUser.getUserId());
        if (!isOwner) {
            return MALICIOUS_ERROR_REDIRECT;
        }

        // Check if the job application exists
        Optional<JobApplication> jobApplicationOptional = jobApplicationService.findById(applicationId);
        if (jobApplicationOptional.isEmpty()) {
            return MALICIOUS_ERROR_REDIRECT;
        }
        JobApplication acceptedApplication = jobApplicationOptional.get();

        // Everything correct, update service request with confirmed details
        serviceRequest.setContractor(acceptedApplication.getContractor());
        serviceRequest.setAgreedDate(acceptedApplication.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        serviceRequest.setAgreedPrice(acceptedApplication.getPrice());

        // Accept the specified contractor's application
        acceptedApplication.setStatus("ACCEPTED");
        jobApplicationService.saveJobApplication(acceptedApplication);

        // Decline all other contractor's applications
        List<JobApplication> allApplications = jobApplicationService.getJobApplicationsByRequestId(id);
        for (JobApplication application : allApplications) {
            if (!application.getId().equals(acceptedApplication.getId())) {
                application.setStatus("DECLINED");
                jobApplicationService.saveJobApplication(application);
            }
        }

        serviceRequestService.saveServiceRequest(serviceRequest);

        return "redirect:/serviceRequest/" + id;

    }

    /**
     * Post request to decline a job application for a service request.
     * @param id id of the service request
     * @param applicationId id of the job application to decline
     * @return the template or redirect if the service request or application doesn't exist
     */
    @PostMapping("/serviceRequest/{id}/decline")
    public String declineJobApplication(@PathVariable Long id,
                                       @RequestParam("applicationId") Long applicationId) {

        // First, perform checks to make sure that the request is valid

        // Check if the specified service request exists
        Optional<ServiceRequest> serviceRequestOptional = serviceRequestService.findById(id);
        if (serviceRequestOptional.isEmpty()) {
            return MALICIOUS_ERROR_REDIRECT;
        }
        ServiceRequest serviceRequest = serviceRequestOptional.get();

        // Check if the service request has already been assigned to a contractor
        if (serviceRequest.getContractor() != null) {
            return MALICIOUS_ERROR_REDIRECT;
        }

        // Check if the requesting user is the owner of the service request
        AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isOwner = serviceRequest.getOwner().getUserId().equals(currentUser.getUserId());
        if (!isOwner) {
            return MALICIOUS_ERROR_REDIRECT;
        }

        // Check if the job application exists
        Optional<JobApplication> jobApplicationOptional = jobApplicationService.findById(applicationId);
        if (jobApplicationOptional.isEmpty()) {
            return MALICIOUS_ERROR_REDIRECT;
        }

        // If everything is valid

        // get the job application, set it as declined and save it to the database
        JobApplication applicationToDecline = jobApplicationOptional.get();
        applicationToDecline.setStatus("DECLINED");
        jobApplicationService.saveJobApplication(applicationToDecline);

        // redirect back to the service request page
        return "redirect:/serviceRequest/" + id;

    }

    /**
     * Controller handling the creation of service requests.
     *
     * @param model model for the form
     * @param title title that the user enters
     * @param description description that the user enters
     * @param dateMin minimum start date that the user enters
     * @param dateMax maximum end date that the user enters
     * @param priceMin minimum price that the user enters
     * @param priceMax maximum price that the user enters
     * @param gardenId Id of the garden the user selects from the dropdown
     * @return the page the user should be on
     */
    @PostMapping(value = "/newServiceRequest")
    public String newServiceRequestSubmitted(
            Model model,
            @RequestParam(name = "title") String title,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "dateMin") String dateMin,
            @RequestParam(name = "dateMax") String dateMax,
            @RequestParam(name = "priceMin") String priceMin,
            @RequestParam(name = "priceMax") String priceMax,
            // Backend handles all errors, so a missing garden must be allowed to show the error
            @RequestParam(name = "garden", required = false) Long gardenId,
            @RequestParam(name = "imagePath", required = false) String lastValidImagePath,
            @RequestParam(name = "image", required = false) MultipartFile image,
            HttpServletRequest request
    ) {
        LOG.info("POST /serviceRequests");

        Locale locale = LocaleUtils.getLocaleFromSession(request);
        // Error messages for i18n front end
        model.addAttribute("frontEndErrorImageTooLarge", messageSource.getMessage("error.imageTooLarge", null, locale));
        model.addAttribute("frontEndErrorImageFormat", messageSource.getMessage("error.imageFormat", null, locale));
        int modelStartSize = model.asMap().size();

        boolean hasErrors = false;

        if (image!= null && !image.isEmpty()) {
            try {
                validateImage(image);
                lastValidImagePath = uploadImage(image);
            } catch (ImageTypeException e) {
                // 'Image must be of type png, jpg, or svg' error
                String errorMessage = messageSource.getMessage("error.invalidFileType", null, locale);
                model.addAttribute("errorImage", errorMessage);
                hasErrors = true;
            } catch (FileSizeException e) {
                // 'Image must be less than 10MB' error
                String errorMessage = messageSource.getMessage("error.imageTooLargeContractor", null, locale);
                model.addAttribute("errorImage", errorMessage);
                hasErrors = true;
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser user = userService.getUserFromAuthentication(authentication);

        // Garden validation
        Optional<Garden> garden = Optional.empty();
        if (gardenId == null) {
            // If a garden selection wasn't made
            String message = messageSource.getMessage("errorGarden.empty", null, locale);
            model.addAttribute("errorGarden", message);
            hasErrors = true;
        } else {
            garden = gardenService.getGarden(gardenId);
            if (garden.isEmpty() || !garden.get().isPublicised() || !garden.get().getOwner().getUserId().equals(user.getUserId())) {
                String message = messageSource.getMessage("errorGarden.garden", null, locale);
                model.addAttribute("errorGarden", message);
                hasErrors = true;
            } else if (garden.get().getLocation() == null || garden.get().getLocation().getLat() == null || garden.get().getLocation().getLon() == null){
                String message = messageSource.getMessage("errorGarden.location", null, locale);
                model.addAttribute("errorGarden", message);
                hasErrors = true;
            }
        }

        // Validate the given dates, Optional is so dates with valid formats can be returned while still returning errors
        String titleParsed = ServiceRequestValidation.validateTitle(
                title,
                "errorTitle",
                model,
                locale,
                messageSource
        );
        String descriptionParsed = ServiceRequestValidation.validateDescription(
                description,
                "errorDescription",
                model,
                locale,
                messageSource
        );
        Optional<LocalDate> dateMinParsed = ServiceRequestValidation.validateDate(
                dateMin,
                "errorDateMin",
                model,
                locale,
                messageSource
        );
        Optional<LocalDate> dateMaxParsed = ServiceRequestValidation.validateDate(
                dateMax,
                "errorDateMax",
                model,
                locale,
                messageSource
        );

        // If the 'latest' date is before the 'earliest' date, show an error
        if (dateMinParsed.isPresent() && dateMaxParsed.isPresent() && dateMaxParsed.get().isBefore(dateMinParsed.get())) {
            String message = messageSource.getMessage("errorDateMax.latestBeforeEarliest", null, locale);
            model.addAttribute("errorDateMax", message);
        }

        // Try to validate and get the formatted minimum and maximum price values.
        // if they were not valid, then they will be null
        Double minimumPrice = ServiceRequestValidation.validatePriceMin(priceMin, "errorPriceMin", model, messageSource, locale);
        Double maximumPrice = ServiceRequestValidation.validatePriceMax(priceMin, priceMax, "errorPriceMax", model, messageSource, locale);

        // If there are errors, add values back to model and show form again.
        // First two conditions just to stop the .get() in the request creation from complaining
        // If minimumPrice or maximumPrice have any issues, they will be null.
        if (dateMinParsed.isEmpty() || dateMaxParsed.isEmpty() || model.asMap().size() - modelStartSize > 0 || descriptionParsed.isEmpty() || titleParsed.isEmpty() ||
                minimumPrice == null || maximumPrice == null || hasErrors || gardenService.getGarden(gardenId).isEmpty()) {
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            model.addAttribute("dateMin", dateMin);
            model.addAttribute("dateMax", dateMax);
            model.addAttribute("priceMin", priceMin);
            model.addAttribute("priceMax", priceMax);
            model.addAttribute("gardens", user.getOwnedPublicGardens());
            model.addAttribute("gardenId", gardenId);
            model.addAttribute("imagePath", lastValidImagePath);
            return "newServiceRequestTemplate";
        } else {
            // If not, create ServiceRequest and save it
            ServiceRequest newRequest = new ServiceRequest(
                titleParsed,
                descriptionParsed,
                dateMinParsed.get().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                dateMaxParsed.get().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                lastValidImagePath,
                minimumPrice,
                maximumPrice,
                garden.get()
            );
            serviceRequestService.saveServiceRequest(newRequest);
            return "redirect:/myServiceRequests";
        }
    }

    /**
     * Controller handling the editing of service requests.
     *
     * @param model model for the form
     * @param title title that the user enters
     * @param description description that the user enters
     * @param dateMin minimum start date that the user enters
     * @param dateMax maximum end date that the user enters
     * @param priceMin minimum price that the user enters
     * @param priceMax maximum price that the user enters
     * @param gardenId ID of the garden the user selects from the dropdown
     * @return the page the user should be on
     */
    @PostMapping(value = "/serviceRequest/{serviceRequestId}/edit")
    public String editServiceRequestSubmitted(
            Model model,
            @RequestParam(name = "title") String title,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "dateMin") String dateMin,
            @RequestParam(name = "dateMax") String dateMax,
            @RequestParam(name = "priceMin") String priceMin,
            @RequestParam(name = "priceMax") String priceMax,
            @PathVariable Long serviceRequestId,
            // Backend handles all errors, so a missing garden must be allowed to show the error
            @RequestParam(name = "garden", required = false) Long gardenId,
            @RequestParam(name = "imagePath", required = false) String lastValidImagePath,
            @RequestParam(name = "image", required = false) MultipartFile image,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        LOG.info("POST /editServiceRequest/{}", serviceRequestId);


        Locale locale = LocaleUtils.getLocaleFromSession(request);
        model.addAttribute("frontEndErrorImageTooLarge", messageSource.getMessage("error.imageTooLarge", null, locale));
        model.addAttribute("frontEndErrorImageFormat", messageSource.getMessage("error.imageFormat", null, locale));
        int modelStartSize = model.asMap().size();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
        ServiceRequest serviceRequest = validateServiceRequestExists(serviceRequestService.findById(serviceRequestId));

        boolean isOwner = serviceRequest.getOwner().getUserId().equals(currentUser.getUserId());

        if (!isOwner) {
            String message = messageSource.getMessage("error.cannotAccessServiceRequest", null, locale);
            redirectAttributes.addFlashAttribute("accessError", message);
            return "redirect:/myServiceRequests";
        }

        //Verify service request has not been assigned to a contractor
        if (serviceRequest.getContractor() != null) {
            String message = messageSource.getMessage("error.cannotEditServiceRequest", null, locale);
            redirectAttributes.addFlashAttribute("accessError", message);
            return MALICIOUS_ERROR_REDIRECT;
        }

        //Verify if the service request is expired
        if (serviceRequest.getDateMax().isBefore(Instant.now())) {
            String message = messageSource.getMessage("error.expiredServiceRequest", null, locale);
            redirectAttributes.addFlashAttribute("accessError", message);
            return MALICIOUS_ERROR_REDIRECT;
        }

        boolean hasErrors = false;

        if (image!= null && !image.isEmpty()) {
            try {
                validateImage(image);
                lastValidImagePath = uploadImage(image);
            } catch (ImageTypeException e) {
                // 'Image must be of type png, jpg, or svg' error
                String errorMessage = messageSource.getMessage("error.invalidFileType", null, locale);
                model.addAttribute("errorImage", errorMessage);
                hasErrors = true;
            } catch (FileSizeException e) {
                // 'Image must be less than 10MB' error
                String errorMessage = messageSource.getMessage("error.imageTooLargeContractor", null, locale);
                model.addAttribute("errorImage", errorMessage);
                hasErrors = true;
            }
        }

        // Garden validation
        Optional<Garden> garden = Optional.empty();
        if (gardenId == null) {
            // If a garden selection wasn't made
            String message = messageSource.getMessage("errorGarden.empty", null, locale);
            model.addAttribute("errorGarden", message);
            hasErrors = true;
        } else {
            garden = gardenService.getGarden(gardenId);
            if (garden.isEmpty() || !garden.get().isPublicised() || !garden.get().getOwner().getUserId().equals(currentUser.getUserId())) {
                String message = messageSource.getMessage("errorGarden.garden", null, locale);
                model.addAttribute("errorGarden", message);
                hasErrors = true;
            } else if (garden.get().getLocation() == null || garden.get().getLocation().getLat() == null || garden.get().getLocation().getLon() == null){
                String message = messageSource.getMessage("errorGarden.location", null, locale);
                model.addAttribute("errorGarden", message);
                hasErrors = true;
            }
        }

        // Validate the given dates, Optional is so dates with valid formats can be returned while still returning errors
        String titleParsed = ServiceRequestValidation.validateTitle(
                title,
                "errorTitle",
                model,
                locale,
                messageSource
        );
        String descriptionParsed = ServiceRequestValidation.validateDescription(
                description,
                "errorDescription",
                model,
                locale,
                messageSource
        );

        // If dateMin is the exact same as it was previously, don't do any validation.
        // Stops the case where a valid date becomes invalid and stops the form from submitting
        // This code is bad, I'll refactor at some point :)
        Optional<LocalDate> dateMinParsed;
        LocalDate parsedDateMin;
        try {
            parsedDateMin = parseDate(dateMin);
        } catch (DateTimeParseException e) {
            parsedDateMin = null;
        }
        if (parsedDateMin == null || !serviceRequest.getDateMin().equals(
                parsedDateMin.atStartOfDay().toInstant(OffsetDateTime.now().getOffset()))
        ) {
            dateMinParsed = ServiceRequestValidation.validateDate(
                    dateMin,
                    "errorDateMin",
                    model,
                    locale,
                    messageSource
            );
        } else {
            // If they are the same, set the parsed date to whatever it was before
            dateMinParsed = Optional.of(parsedDateMin);
        }

        Optional<LocalDate> dateMaxParsed = ServiceRequestValidation.validateDate(
                dateMax,
                "errorDateMax",
                model,
                locale,
                messageSource
        );

        // If the 'latest' date is before the 'earliest' date, show an error
        if (dateMinParsed.isPresent() && dateMaxParsed.isPresent() && dateMaxParsed.get().isBefore(dateMinParsed.get())) {
            String message = messageSource.getMessage("errorDateMax.latestBeforeEarliest", null, locale);
            model.addAttribute("errorDateMax", message);
        }

        // Try to validate and get the formatted minimum and maximum price values.
        // if they were not valid, then they will be null
        Double minimumPrice = ServiceRequestValidation.validatePriceMin(priceMin, "errorPriceMin", model, messageSource, locale);
        Double maximumPrice = ServiceRequestValidation.validatePriceMax(priceMin, priceMax, "errorPriceMax", model, messageSource, locale);

        // If there are errors, add values back to model and show form again.
        // First two conditions just to stop the .get() in the request creation from complaining
        // If minimumPrice or maximumPrice have any issues, they will be null.
        if (dateMinParsed.isEmpty() || dateMaxParsed.isEmpty() || model.asMap().size() - modelStartSize > 0 || descriptionParsed.isEmpty() || titleParsed.isEmpty() ||
                minimumPrice == null || maximumPrice == null || hasErrors || gardenService.getGarden(gardenId).isEmpty()) {
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            model.addAttribute("dateMin", dateMin);
            model.addAttribute("dateMax", dateMax);
            model.addAttribute("priceMin", priceMin);
            model.addAttribute("priceMax", priceMax);
            model.addAttribute("gardens", currentUser.getOwnedPublicGardens());
            model.addAttribute("gardenId", gardenId);
            model.addAttribute("imagePath", lastValidImagePath);
            model.addAttribute("prevDateMin",
                    LocalDate.ofInstant(serviceRequest.getDateMin(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(FormValidation.DATE_FORMAT))
            );
            return "editServiceRequestTemplate";
        } else {
            serviceRequest.setTitle(title);
            serviceRequest.setDescription(description);
            serviceRequest.setDateMin(dateMin);
            serviceRequest.setDateMax(dateMax);
            serviceRequest.setImagePath(lastValidImagePath);
            serviceRequest.setPriceMin(minimumPrice);
            serviceRequest.setPriceMax(maximumPrice);
            serviceRequest.setGarden(FormValidation.validateGardenExists(garden));
            serviceRequestService.saveServiceRequest(serviceRequest);
            return "redirect:/myServiceRequests";
        }
    }

    /**
     * Post-mapping route for submitting a job application for a given service request.
     * @param id the id of the service request/job being applied to
     * @param date the date the applicant proposes to perform the job.
     * @param price the price the applicant proposes to charge for completing the job.
     * @param model the model containing data needed to be shown
     * @param request HttpServletRequest object which contains information about the request.
     * @return the serviceRequestDetailsTemplate
     */
    @PostMapping("/serviceRequest/{id}")
    public String submitJobApplication(
            @PathVariable Long id,
            @RequestParam(name = "date") String date,
            @RequestParam(name = "price") String price,
            @RequestParam(name = "status", defaultValue = "PENDING") String status,
            Model model,
            HttpServletRequest request
    ) {
        try {
            int globalModelSize = model.asMap().size();

            Locale locale = LocaleUtils.getLocaleFromSession(request);


            // Attempt to fetch the service request being applied for
            ServiceRequest serviceRequest = serviceRequestService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Service request ID is invalid"));

            // Retrieve the logged-in contractor
            AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Contractor currentContractor = userService.getContractorByUserId(currentUser.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("The logged in user is not a contractor"));

            // Don't allow application to your own service request
            if (currentContractor.getUserId().equals(serviceRequest.getOwner().getUserId())){
                throw new IllegalArgumentException("The applying contractor owns the service request");
            }

            // Don't allow contractor to re-apply


            List<JobApplication> jobApplications = jobApplicationService.getActiveJobApplicationsByRequestId(id);
            List<Contractor> appliedContractors = jobApplications.stream().map(JobApplication::getContractor).toList();
            List<Long> appliedContractorsIds = appliedContractors.stream().map(Contractor::getUserId).toList();

            if (appliedContractorsIds.contains(currentContractor.getUserId())){
                throw new IllegalArgumentException("The applying contractor has already applied to this request");
            }
            model.addAttribute("appliedContractorsIds", appliedContractorsIds);


            // Validate parameters and parse them into usable values
            Optional<LocalDate> optionalParsedDate = ServiceRequestValidation.validateApplicationDate(date, "errorDate", model, messageSource, locale, serviceRequest);
            //price validation
            Double parsedPrice = ServiceRequestValidation.validateApplicationPrice(price, "errorPrice", model, messageSource, locale, serviceRequest);

            // check for errors
            if (model.asMap().size() - globalModelSize > 1 || parsedPrice == null || optionalParsedDate.isEmpty()) {
                model.addAttribute("date", date);
                model.addAttribute("price", price);
                model.addAttribute("serviceRequest", serviceRequest);
                model.addAttribute("modalOpen", true);
                model.addAttribute("isOwner", false);
                model.addAttribute("isAssigned", serviceRequest.getContractor() != null);
                model.addAttribute("questionAnswerList", questionAnswerService.findQuestionAnswersByServiceRequest(id));
                model.addAttribute("question", "");
                model.addAttribute("isAssignedContractor", false);
                return "serviceRequestDetailsTemplate";
            } else {
                // If no errors, Create a job application entity using the submitted values
                LocalDate parsedDate = optionalParsedDate.get();
                JobApplication jobApplication = new JobApplication(serviceRequest, currentContractor, parsedDate, parsedPrice, status);

                // Submit the job application by saving it to the database
                jobApplicationService.saveJobApplication(jobApplication);

                return "redirect:/serviceRequest/" + id;

            }
        } catch(IllegalArgumentException e){
                // If either the service request id is invalid, or the user is not a contractor

                // redirect to the homepage
                return "redirect:/";
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
     * Handles the completion of a service request by the owner.
     *
     * This method validates that the service request exists, that the current user
     * is the owner of the service request, and that the service request is not
     * already completed and has been assigned to a contractor. If these conditions
     * are met, the service request is marked as completed and saved.
     *
     * @param serviceRequestId the ID of the service request to be completed
     * @return a redirect to the service request details page if successful,
     *         or a redirect to the error page if the request is invalid or malicious
     *            model.addAttribute("errorQuestion", message);
     * @throws ResponseStatusException if the service request does not exist
     */
    @PostMapping("/serviceRequest/{serviceRequestId}/complete")
    public String completeRequest(
        @PathVariable Long serviceRequestId
    ) {
        LOG.info("Completing request");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
        ServiceRequest serviceRequest;
        try {
            serviceRequest = validateServiceRequestExists(serviceRequestService.findById(serviceRequestId));
        } catch (ResponseStatusException e) {
            return MALICIOUS_ERROR_REDIRECT;
        }

        boolean isOwner = serviceRequest.getOwner().getUserId().equals(currentUser.getUserId());

        //If the user is not owner or the request is already completed or the request has not been assigned to a contractor
        if (!isOwner || serviceRequest.isCompleted() || serviceRequest.getContractor() == null) {
            return MALICIOUS_ERROR_REDIRECT;
        }

        serviceRequest.setCompleted(true);
        serviceRequestService.saveServiceRequest(serviceRequest);
        mailService.sendInvoiceEmail(serviceRequest.getContractor(), serviceRequest.getOwner(), LocaleContextHolder.getLocale(), serviceRequest);

        return "redirect:/serviceRequest/" + serviceRequestId;
    }

    /**
     * Handles the submission of a rating for a contractor after a service request has been completed
     *
    * @param id the ID of the service request
    * @param rating the rating value for the service request
    *
    * @return a redirect to the service request page or an error redirect if validation fails
    */
    @PostMapping("/serviceRequest/{id}/rating")
    public String submitRating(
            @PathVariable Long id,
            @RequestParam(name = "rating") String rating,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        LOG.info("POST /serviceRequest/{}/rating", id);

        Locale locale = LocaleUtils.getLocaleFromSession(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
        ServiceRequest serviceRequest = validateServiceRequestExists(serviceRequestService.findById(id));

        boolean isOwner = serviceRequest.getOwner().getUserId().equals(currentUser.getUserId());
        Contractor contractor = serviceRequest.getContractor();

        if (!isOwner || contractor == null || !serviceRequest.isCompleted()) {
            String message = messageSource.getMessage("error.cannotRateServiceRequest", null, locale);
            redirectAttributes.addFlashAttribute("accessError", message);
            return MALICIOUS_ERROR_REDIRECT;
        }

        try{
            int ratingValue = Integer.parseInt(rating);
            contractor.addRating(ratingValue);
            userService.updateUserDetails(contractor);
            serviceRequest.setRating(ratingValue);
            serviceRequestService.saveServiceRequest(serviceRequest);
        } catch (IllegalArgumentException e){
            LOG.error("Illegal argument for contractor rating");
        }

        return String.format("redirect:/serviceRequest/%d", id);

    }

    /**
     * Submits a question to a service request by a contractor.
     *
     * This method handles the submission of a question related to a specific service request.
     * It validates that the service request exists, checks if the current user is authorized
     * to submit a question (i.e., the user is a contractor and not the owner of the request),
     * and then saves the question to the database.
     *
     * @param id       The ID of the service request to which the question is being submitted.
     * @param question The content of the question submitted by the contractor.
     * @param model    The model to add attributes for rendering the view (not currently used in this method).
     * @param request  The HTTP request object for accessing request-specific information (not currently used in this method).
     * @return A redirect URL back to the service request page if successful, or a redirect to an error page if the submission is invalid or malicious.
     *
     * @throws ResponseStatusException if the service request does not exist.
     */
    @PostMapping("serviceRequest/{id}/question")
    public String submitQuestion(
            @PathVariable Long id,
            @RequestParam(name = "question") String question,
            Model model,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Locale locale
    ) {
        LOG.info("Submitting question");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
        ServiceRequest serviceRequest;
        try {
            serviceRequest = validateServiceRequestExists(serviceRequestService.findById(id));
        } catch (ResponseStatusException e) {
            return MALICIOUS_ERROR_REDIRECT;
        }
        boolean isOwner = serviceRequest.getOwner().getUserId().equals(currentUser.getUserId());

        if (isOwner || !(currentUser instanceof Contractor contractor)) {
            return MALICIOUS_ERROR_REDIRECT;
        }

        if (question.trim().isEmpty() || question.length() > 512 || !question.matches(CONTAINS_LETTER_REGEX)) {
            String message = messageSource.getMessage("error.question", null, locale);
            redirectAttributes.addFlashAttribute("question", question);
            redirectAttributes.addFlashAttribute("errorQuestion", message);
            return "redirect:/serviceRequest/" + id;
        }
        int numUnansweredQuestions = questionAnswerService.getNumberOfUnansweredQuestions(serviceRequest, contractor);
        if (numUnansweredQuestions >= MAX_UNANSWERED_QUESTIONS) {
            return MALICIOUS_ERROR_REDIRECT;
        }

        QuestionAnswer questionAnswer = new QuestionAnswer(serviceRequest, (Contractor) currentUser, question);

        questionAnswerService.saveQuestionAnswer(questionAnswer);
        return "redirect:/serviceRequest/" + id;
    }

    /**
    * Handles the submission of an answer to a service request question.
    *
    * @param id the ID of the service request
    * @param answer the answer to the question
    * @param questionId the ID of the question being answered
    *
    * @return a redirect to the service request page or an error redirect if validation fails
    */
    @PostMapping("serviceRequest/{id}/answer")
    public String submitAnswer(
            @PathVariable Long id,
            @RequestParam(name = "answer") String answer,
            @RequestParam(name = "questionId") Long questionId,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
        ServiceRequest serviceRequest;
        Locale locale = request.getLocale();
        try {
            serviceRequest = validateServiceRequestExists(serviceRequestService.findById(id));
        } catch (ResponseStatusException e) {
            return MALICIOUS_ERROR_REDIRECT;
        }
        boolean isOwner = serviceRequest.getOwner().getUserId().equals(currentUser.getUserId());

        if (!isOwner) {
            return MALICIOUS_ERROR_REDIRECT;
        }
        if (answer.trim().isEmpty() || answer.length() > 512 || !answer.matches(CONTAINS_LETTER_REGEX)) {
            String message = messageSource.getMessage("error.answer", null, locale);
            redirectAttributes.addFlashAttribute("answer", answer);
            redirectAttributes.addFlashAttribute("errorAnswer", message);
            redirectAttributes.addFlashAttribute("dropdownOpen", true);
            redirectAttributes.addFlashAttribute("answerOpen", true);
            redirectAttributes.addFlashAttribute("prevQuestionId", questionId);
            return "redirect:/serviceRequest/" + id;
        }

        Optional<QuestionAnswer> question = questionAnswerService.getQuestionAnswerById(questionId);
        if (question.isEmpty()) {
            return MALICIOUS_ERROR_REDIRECT;
        }

        question.get().setAnswer(answer);
        questionAnswerService.saveQuestionAnswer(question.get());
        redirectAttributes.addFlashAttribute("dropdownOpen", true);

        return "redirect:/serviceRequest/" + id;
    }


    /**
     * Retrieves the details of a contractor who has applied for a specific service request.
     *
     * @param requestId   the ID of the service request.
     * @param contractorId the ID of the contractor whose details are being retrieved.
     * @param request     the HTTP request containing session information, used to extract the locale.
     * @return a {@link ResponseEntity} containing a map with the contractor's details, or a bad request response if any validation fails.
     */
    @GetMapping("/serviceRequest/{requestId}/applicant/{contractorId}")
    public ResponseEntity<Map<String, String>> getApplicantDetails(
            @PathVariable Long requestId,
            @PathVariable Long contractorId,
            HttpServletRequest request
    ) {
        LOG.info("retrieving applicant details");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser currentUser = userService.getUserFromAuthentication(authentication);

        //Validate service request exist
        Optional<ServiceRequest> optionalRequest = serviceRequestService.findById(requestId);
        if (optionalRequest.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ServiceRequest serviceRequest = optionalRequest.get();
        //Validate service request owner
        if (!Objects.equals(serviceRequest.getOwner().getUserId(), currentUser.getUserId())) {
            return ResponseEntity.badRequest().build();
        }
        //Validate contractor exist
        Optional<Contractor> optionalContractor = userService.getContractorByUserId(contractorId);
        if (optionalContractor.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Contractor contractor = optionalContractor.get();
        List<JobApplication> applications = jobApplicationService.getJobApplicationsByRequestId(requestId);
        //Validate contractor is applying this service request

        boolean contractorExists = applications.stream()
                .anyMatch(application -> application.getContractor().equals(contractor));
        if (!contractorExists) {
            return ResponseEntity.badRequest().build();
        }

        Locale locale = LocaleUtils.getLocaleFromSession(request);
        String noJobs = messageSource.getMessage("myJobs.unemployed", null, locale);
        String completedJobs = messageSource.getMessage("myJobs.jobCompleted", null, locale);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("name", contractor.getFirstName());
        responseBody.put("profilePicture", contractor.getProfilePicture());
        responseBody.put("flair", userService.getContractorFlair(contractor.getUserId(), locale).get(0));
        responseBody.put("flairToolTip", userService.getContractorFlair(contractor.getUserId(), locale).get(1));
        responseBody.put("aboutMe", contractor.getAboutMe());
        responseBody.put("numRatings", contractor.getNumRatings() == null ? noJobs : completedJobs + contractor.getNumRatings());
        responseBody.put("avgRating", String.valueOf(contractor.getAverageRating()));
        responseBody.put("workPictures", String.valueOf(contractor.getWorkPictures()));
        return ResponseEntity.ok(responseBody);
    }

    /**
     * Adds them previous input and the error message to the model
     * If there has been a redirection with flashattributes indicating a question or answer error.
     * @param flashAttributes the redirect attributes
     * @param model the model to update
     */
    public static void updateModelWithQAErrors(Map<String, ?> flashAttributes, Model model){
        if (flashAttributes != null && flashAttributes.containsKey("question")) {
            model.addAttribute("question", flashAttributes.get("question"));
            model.addAttribute("errorQuestion", flashAttributes.get("errorQuestion"));
        } else {
            model.addAttribute("question", "");
        }
        if (flashAttributes != null && flashAttributes.containsKey("answer")) {
            model.addAttribute("answer", flashAttributes.get("answer"));
            model.addAttribute("errorAnswer", flashAttributes.get("errorAnswer"));
        } else {
            model.addAttribute("answer", "");
        }

    }
}
