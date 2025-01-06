package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Contractor;
import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import nz.ac.canterbury.seng302.gardenersgrove.service.ServiceRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@Controller
public class AssignedJobsController {
    private final ServiceRequestService serviceRequestService;
    private static final Logger LOG = LoggerFactory.getLogger(AssignedJobsController.class);
    @Autowired
    public AssignedJobsController(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }
    /**
     * Handles GET requests for displaying the 'my service requests' page
     *
     * @param tab the tab to be displayed, defaults to current
     * @param model the model to be used
     * @return the myServiceRequests page
     */
    @GetMapping(value = "/myJobs")
    public String myServiceRequests(@RequestParam(value = "tab", required = false, defaultValue = "current") String tab,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model) {
        LOG.info("GET /myJobs");

        AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(currentUser instanceof Contractor contractor)) {
            return "redirect:/homepage";
        }

        Pageable pagingSort = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "agreedDate"));
        Page<ServiceRequest> pageRequests;

        if (tab.equals("current")) {
            pageRequests = serviceRequestService.getCurrentAssignedJobs(contractor, pagingSort);
        } else {
            pageRequests = serviceRequestService.getPastAssignedJobs(contractor, pagingSort);
        }

        List<ServiceRequest> jobs = pageRequests.getContent();
        model.addAttribute("activeTab", tab);
        model.addAttribute("currentPage", pageRequests.getNumber());
        model.addAttribute("size", pageRequests.getSize());
        model.addAttribute("totalItems", pageRequests.getTotalElements());
        model.addAttribute("totalPages", pageRequests.getTotalPages());
        model.addAttribute("jobs", jobs);
        model.addAttribute("listIsEmpty", jobs.isEmpty());
        return "assignedJobsTemplate";
    }
}
