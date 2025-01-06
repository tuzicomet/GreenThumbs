package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Controller
public class BrowseGardensController {
    private final GardenService gardenService;
    private final UserService userService;
    private static final Logger LOG = LoggerFactory.getLogger(BrowseGardensController.class);

    @Autowired
    GardenRepository gardenRepository;

    @Autowired
    private MessageSource messageSource;

    public BrowseGardensController(GardenService gardenService, UserService userService) {
        this.gardenService = gardenService;
        this.userService = userService;
    }

    /**
     * Handles the sorting for the results and searching, not necessary but nice to have.
     *
     * @param direction String indicating which direction it should be sorted ie ASC
     * @return Sort direction indicating how it should be sorted
     */
    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }

    /**
     * Get request for the browse gardens page, takes the inputs of the search bar
     * and uses them to search and display paginated gardens. The search term lower cases
     * and searches all public gardens and plant names that match.
     *
     * Reference: https://www.bezkoder.com/spring-boot-pagination-sorting-example/
     * 
     * @param model    model for the form
     * @param search   search string that is in the search bar
     * @param page     page number indicating the current page being displayed
     * @param size     size of the pages being displayed in terms of items shown
     * @param sort     array indicating the order of sorting (not needed but nice)
     * @param request  HttpServletRequest object which contains information about the request.
     *                 Used here to get the locale from the request.
     * @return browseGardens thymeleaf page
     */
    @GetMapping("/browseGardens")
    public String getAllGardensPage(
            Model model,
            @RequestParam(value = "tab", required = false, defaultValue = "current") String tab,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tagFilterString,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            HttpServletRequest request) {

            LOG.info("/GET /browseGardens");

        if (page < 0 || size < 0) {
            return "redirect:/browseGardens?tab=" + tab + "page=0&size=10";
        }

           List<String> tags = new ArrayList<>();
            if(tagFilterString != null && !tagFilterString.isEmpty()){
                tags = Arrays.stream(tagFilterString.split(";")).toList();
            }
            model.addAttribute("tags", tags);
            model.addAttribute("tagFilterString", tagFilterString == null ? "" : tagFilterString);

            try {
            // Retrieve the currently logged-in user's authentication details from the security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Get the current locale from the request
            Locale locale = LocaleUtils.getLocaleFromSession(request);

            // Check if the user is authenticated
            if (authentication != null && authentication.isAuthenticated()) {
                // Retrieve the currently authenticated user's details
                AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
                // Add the user object to the model attribute, under the name 'user'
                model.addAttribute("user", currentUser);
                // Add the user's gardens to the model so they can be shown on the navbar
                model.addAttribute("gardens", currentUser.getOwnedGardens());
            }

            List<Order> orders = new ArrayList<>();
            for (String sortOrder : sort) {
                String[] sortTerm = sortOrder.split(",");
                if (sortTerm.length == 2) {
                    orders.add(new Order(getSortDirection(sortTerm[1]), sortTerm[0]));
                }
            }

            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));
            List<Garden> browseGardens;
            Page<Garden> pageGarden;

            if (search == null || search.isEmpty()) {
                pageGarden = gardenService.getRecentPublicGardens(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")), tags);
                int totalPages = pageGarden.getTotalPages();
                if (page > totalPages) {
                    return "redirect:/browseGardens?page=0&size=" + size;
                }
                browseGardens = pageGarden.getContent();
                model.addAttribute("currentPage", pageGarden.getNumber());
                model.addAttribute("size", pageGarden.getSize());
                model.addAttribute("totalItems", pageGarden.getTotalElements());
                model.addAttribute("totalPages", pageGarden.getTotalPages());
                model.addAttribute("browseGardens", browseGardens);

                return "browseGardensTemplate";
            } else {
                pageGarden = gardenService.getAllPublicGardensByName(search, pagingSort, tags);
                int totalPages = pageGarden.getTotalPages();
                if (page > totalPages) {
                    return "redirect:/browseGardens?page=0&size=" + size;
                }
                if (pageGarden.isEmpty()) {
                    pageGarden = gardenService.getAllPublicGardensByPlantName(search, pagingSort, tags);
                }

                browseGardens = pageGarden.getContent();
                if (browseGardens.isEmpty()) {
                    // Get the no matching gardens message and add it to the model as "message"
                    String noMatchingGardensMessage = messageSource.getMessage("browseGardens.noMatchingGardens", null, locale);
                    model.addAttribute("message", noMatchingGardensMessage);
                } else {
                    model.addAttribute("browseGardens", browseGardens);
                    model.addAttribute("currentPage", pageGarden.getNumber());
                    model.addAttribute("totalItems", pageGarden.getTotalElements());
                    model.addAttribute("totalPages", pageGarden.getTotalPages());
                    model.addAttribute("search", search);
                    model.addAttribute("size", size);
                    model.addAttribute("sort", sort);
                }
                return "browseGardensTemplate";
            }
        } catch (Exception e) {
            model.addAttribute("message", "An error occurred: " + e.getMessage());
            LOG.error(e.getMessage());
            return "error";
        }
    }
}
