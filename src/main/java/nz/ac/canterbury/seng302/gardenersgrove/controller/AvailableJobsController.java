package nz.ac.canterbury.seng302.gardenersgrove.controller;


import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Contractor;
import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import nz.ac.canterbury.seng302.gardenersgrove.utility.LocaleUtils;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ServiceRequestValidation;
import nz.ac.canterbury.seng302.gardenersgrove.service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

import static java.lang.Float.parseFloat;
import static nz.ac.canterbury.seng302.gardenersgrove.utility.DistanceUtil.calculateDistance;

/**
 * Controller class to handle mapping functions for the available jobs page
 */
@Controller
public class AvailableJobsController {


    private final ServiceRequestService serviceRequestService;
    private final MessageSource messageSource;

    @Autowired
    AvailableJobsController(ServiceRequestService serviceRequestService, MessageSource messageSource) {
        this.serviceRequestService = serviceRequestService;
        this.messageSource = messageSource;
    }


    /**
     * Get mapping to prepare information for the available jobs page
     * @param orderPrompt the sort order for the results
     * @param page the current page the user is on
     * @param size the size of the page the user is on
     * @param model the model to pass information to the template
     * @return the available jobs template
     */
    @GetMapping("/availableJobs")
    public String showAvailableJobs(@RequestParam(name="orderPrompt", defaultValue = "latestRelease") String orderPrompt,
                                    @RequestParam(name="maxDistance", defaultValue = "") String maxDistance,
                                    @RequestParam(name="dateMin", defaultValue = "") String dateMin,
                                    @RequestParam(name="dateMax", defaultValue = "") String dateMax,
                                    @RequestParam(name="priceMin", defaultValue = "") String minBudget,
                                    @RequestParam(name="priceMax", defaultValue = "") String maxBudget,
                                    @RequestParam(name="filtersOpen", defaultValue = "false") Boolean filtersOpen,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model,
                                    HttpServletRequest request){

        if (page < 0 || size < 0) {
            return "redirect:/availableJobs?page=0&size=10&orderPrompt=%s".formatted(orderPrompt);
        }

        AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(currentUser instanceof Contractor contractor)) {
            return "redirect:/homepage";
        }

        int globalModelSize = model.asMap().size();

        Locale locale = LocaleUtils.getLocaleFromSession(request);


        LocalDate dateMinParsed = validateDateMin(dateMin, model, locale);
        LocalDate dateMaxParsed = validateDateMax(dateMax, model, locale);
        validateDateOrder(dateMinParsed, dateMaxParsed, model, locale);

        Double minimumPrice = validateMinBudget(minBudget, model, locale);
        Double maximumPrice = validateMaxBudget(minBudget, maxBudget, model, locale);

        // Adds an error to the model if the max distance is invalid
        validateMaxDistanceAllowNull(maxDistance, model, locale);

        if (model.asMap().size() - globalModelSize > 0) {
            List<ServiceRequest> jobsNew = serviceRequestService.getAvailableJobs(orderPrompt, null, null, null, null);

            // don't show them their own requests as available jobs
            jobsNew = jobsNew.stream().filter(job -> !job.getOwner().getUserId().equals(contractor.getUserId())).toList();

            Map<Long, Double> jobDistancesNew = getJobDistances(jobsNew, contractor);

            int start = Math.min(page * size, jobsNew.size());
            int end = Math.min((page + 1) * size, jobsNew.size());

            List<ServiceRequest> paginatedJobs = jobsNew.subList(start, end);
            Page<ServiceRequest> jobsPage = new PageImpl<>(paginatedJobs, PageRequest.of(page, size), jobsNew.size());
            model.addAttribute("jobs", jobsPage.getContent());
            model.addAttribute("currentPage", jobsPage.getNumber());
            model.addAttribute("jobDistances", jobDistancesNew);
            model.addAttribute("totalItems", jobsPage.getTotalElements());
            model.addAttribute("totalPages", jobsPage.getTotalPages());
            model.addAttribute("maxDistance", maxDistance);
            model.addAttribute("orderPrompt", orderPrompt);
            return handleValidationFailure(model, filtersOpen, minBudget, maxBudget, dateMin, dateMax);
        }
        List<ServiceRequest> jobs = serviceRequestService.getAvailableJobs(orderPrompt,
                dateMinParsed,
                dateMaxParsed,
                minimumPrice,
                maximumPrice);

        // don't show them their own requests as available jobs
        jobs = new ArrayList<>(
                jobs.stream().filter(job -> !job.getOwner().getUserId().equals(contractor.getUserId())).toList()
        );

        Map<Long, Double> jobDistances = getJobDistances(jobs, contractor);
        if (orderPrompt.equals("closestDistance")) {
            Map<Long, Double> temp = jobDistances;
            jobs.sort(Comparator.comparingDouble(job -> temp.get(job.getId())));
        } else if (orderPrompt.equals("furthestDistance")) {
            Map<Long, Double> anotherTemp = jobDistances;
            jobs.sort(Comparator.comparingDouble(job -> anotherTemp.get(job.getId())));
            jobs = jobs.reversed(); //reverse jobs order for furthest distance
        }

        if (jobDistances.isEmpty()) {
            jobDistances = getJobDistances(jobs, contractor);
        }
        if(!maxDistance.isEmpty()){
            Map<Long, Double> temp3 = jobDistances;
            jobs = jobs.stream().filter(job -> temp3.get(job.getId()) < parseFloat(maxDistance)).collect(Collectors.toList());
        }
        int start = Math.min(page * size, jobs.size());
        int end = Math.min((page + 1) * size, jobs.size());

        List<ServiceRequest> paginatedJobs = jobs.subList(start, end);
        Page<ServiceRequest> jobsPage = new PageImpl<>(paginatedJobs, PageRequest.of(page, size), jobs.size());

        int totalPages = jobsPage.getTotalPages();
        if (page > totalPages) {
            return "redirect:/availableJobs?page=0&size=%d&orderPrompt=%s".formatted(size, orderPrompt);
        }
        model.addAttribute("currentPage", jobsPage.getNumber());
        model.addAttribute("jobDistances", jobDistances);
        model.addAttribute("totalItems", jobsPage.getTotalElements());
        model.addAttribute("totalPages", jobsPage.getTotalPages());
        model.addAttribute("jobs", jobsPage.getContent());
        model.addAttribute("orderPrompt", orderPrompt);
        model.addAttribute("filtersOpen", false);
        model.addAttribute("dateMin", dateMin);
        model.addAttribute("dateMax", dateMax);
        model.addAttribute("priceMin", minBudget);
        model.addAttribute("priceMax", maxBudget);
        model.addAttribute("maxDistance", maxDistance);
        model.addAttribute("orderPrompt", orderPrompt);
        return "availableJobsTemplate";

    }

    /**
     * Extracted helper method
     * Calculates the distances between a list of service requests and a contractor.
     *
     * @param jobs List of ServiceRequest which distance need to be calculated
     * @param contractor The contractor whom distance need to be calculated
     * @return a map contain job's ID and distance from contractor's location
     */
    public Map<Long, Double> getJobDistances(List<ServiceRequest> jobs, Contractor contractor) {
        Map<Long, Double> jobDistances = new HashMap<>();
        for (ServiceRequest job : jobs) {
            double jobLat = job.getLocation().getLat();
            double jobLon = job.getLocation().getLon();
            double distance = calculateDistance(contractor.getLocation().getLat(), contractor.getLocation().getLon(), jobLat, jobLon);
            jobDistances.put(job.getId(), distance);
        }
        return jobDistances;
    }

    /**
     * Validates if the minimum date is before the maximum date. Adds an error message
     * to the model if the maximum date is before the minimum date.
     *
     * @param dateMinParsed The optional parsed minimum date.
     * @param dateMaxParsed The optional parsed maximum date.
     * @param model The model to store any error messages.
     * @param locale The locale used for retrieving localized error messages.
     */
    private void validateDateOrder(LocalDate dateMinParsed, LocalDate dateMaxParsed, Model model, Locale locale) {
        if (dateMinParsed != null && dateMaxParsed != null && dateMaxParsed.isBefore(dateMinParsed)) {
            String message = messageSource.getMessage("errorDateMax.latestBeforeEarliest", null, locale);
            model.addAttribute("errorDateMax", message);
        }
    }



    /**
     * Validates the minimum budget. If the input is empty, returns null. Otherwise,
     * validates and returns the parsed minimum price. Adds an error message to the model
     * if validation fails.
     *
     * @param minBudget The minimum budget input as a string.
     * @param model The model to store any error messages.
     * @param locale The locale used for retrieving localized error messages.
     * @return The validated minimum budget as a Double, or null if input was empty or invalid.
     */
    private Double validateMinBudget(String minBudget, Model model, Locale locale) {
        return minBudget.trim().isEmpty() ? null : ServiceRequestValidation.validatePriceMin(minBudget, "errorPriceMin", model, messageSource, locale);
    }

    /**
     * Validates the maximum budget. If the input is empty, returns null. Otherwise,
     * it ensures that the minimum budget is set to a default value of 0 if it's empty.
     * Then, it validates and returns the parsed maximum price. Adds an error message
     * to the model if validation fails.
     *
     * @param minBudget The minimum budget input as a string. Defaulted to "0" if empty.
     * @param maxBudget The maximum budget input as a string.
     * @param model The model to store any error messages.
     * @param locale The locale used for retrieving localized error messages.
     * @return The validated maximum budget as a Double, or null if input was empty or invalid.
     */
    private Double validateMaxBudget(String minBudget, String maxBudget, Model model, Locale locale) {
        if(minBudget.trim().isEmpty()) {
            minBudget = "0";
        }
        return maxBudget.trim().isEmpty() ? null : ServiceRequestValidation.validatePriceMax(minBudget, maxBudget, "errorPriceMax", model, messageSource, locale);
    }

    /**
     * Validates the maximum distance input.
     * Adds an error message to the model if validation fails.
     *
     * @param maxDistance The maximum distance input as a string.
     * @param model The model to store any error messages.
     * @param locale The locale used for retrieving localized error messages.
     */
    private void validateMaxDistanceAllowNull(String maxDistance, Model model, Locale locale) {
        if(!maxDistance.trim().isEmpty()){
            ServiceRequestValidation.validateMaxDistance(maxDistance, "errorMaxDistance", model, messageSource, locale);
        }
    }

    /**
     * Updates the model and returns in the case of invalid filters
     * @param model the model
     * @param filtersOpen a boolean indicating if the filtering pop-up is open
     * @param minBudget the previous input for the minimum budget field
     * @param maxBudget the previous input for the maximum budget field
     * @param dateMin the previous input for the minimum date field
     * @param dateMax the previous input for the maximum date field
     * @return the template
     */
    private String handleValidationFailure(Model model, Boolean filtersOpen,
                                           String minBudget, String maxBudget, String dateMin, String dateMax) {
        model.addAttribute("dateMin", dateMin);
        model.addAttribute("dateMax", dateMax);
        model.addAttribute("priceMin", minBudget);
        model.addAttribute("priceMax", maxBudget);
        model.addAttribute("filtersOpen", filtersOpen);

        return "availableJobsTemplate";
    }

    /**
     * Validates the minimum date.
     * Returns the parsed date if valid, or null if the input is empty or invalid.
     * Adds an error message to the model if validation fails.
     *
     * @param dateMin The minimum date input as a string.
     * @param model The model to store any error messages.
     * @param locale The locale used for retrieving localized error messages.
     * @return A parsed LocalDate, or null if the filter can be ignored (it is invalid or empty)
     */
    private LocalDate validateDateMin(String dateMin, Model model, Locale locale) {
        return dateMin.trim().isEmpty()
                ? null
                : ServiceRequestValidation.validateDate(dateMin, "errorDateMin", model, locale, messageSource).orElse(null);
    }

    /**
     * Validates the maximum date.
     * Returns  the parsed date if valid, or null if the input is empty or invalid.
     * Adds an error message to the model if validation fails.
     *
     * @param dateMax The maximum date input as a string.
     * @param model The model to store any error messages.
     * @param locale The locale used for retrieving localized error messages.
     * @return A parsed LocalDate, or null if the filter can be ignored (it is invalid or empty)
     */
    private LocalDate validateDateMax(String dateMax, Model model, Locale locale) {
        return dateMax.trim().isEmpty()
                ? null
                : ServiceRequestValidation.validateDate(dateMax, "errorDateMax", model, locale, messageSource).orElse(null);
    }

}

