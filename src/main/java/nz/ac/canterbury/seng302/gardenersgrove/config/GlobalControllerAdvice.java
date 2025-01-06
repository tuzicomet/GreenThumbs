package nz.ac.canterbury.seng302.gardenersgrove.config;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Contractor;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * This class is used to perform actions that are required for all controller mapping functions
 * This is useful for information we need to be accessible to templates universally such as in the navbar
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    private final GardenRepository gardenRepository;

    @Autowired
    public GlobalControllerAdvice(GardenRepository gardenRepository) {
        this.gardenRepository = gardenRepository;
    }

    /**
     * Adds a variable to the model to determine if the user is a contractor or not
     * @param model the model for each template
     */
    @ModelAttribute
    public void addIsContractorToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AbstractUser currentUser) {
            model.addAttribute("isContractor", currentUser instanceof Contractor);

        }
    }

    /**
     * If the user is logged in, adds a variable named usersGardens to the model, which contains a
     * list of the user's gardens, in descending order of id.
     * @param model the model for each template
     */
    @ModelAttribute
    public void addUsersGardensToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AbstractUser currentUser) {
            model.addAttribute("usersGardens", gardenRepository.findAllByOwnderIdOrderByIdDesc(currentUser.getUserId()));
        }
    }
}
