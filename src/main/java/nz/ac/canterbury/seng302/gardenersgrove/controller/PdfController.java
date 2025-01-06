package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static nz.ac.canterbury.seng302.gardenersgrove.validation.ServiceRequestValidation.validateServiceRequestExists;

/**
 * Sample controller to serve the PDF test endpoint. Should not be here by the time the story is done.
 */
@Controller
public class PdfController {
    private final PdfService pdfService;
    private final ServiceRequestService serviceRequestService;
    private final UserService userService;
    private static final Logger LOG = LoggerFactory.getLogger(PdfController.class);

    /**
     * Autowired constructor for the PdfController
     * @param pdfService Service class that handles all the actual PDF operations
     */
    @Autowired
    public PdfController(PdfService pdfService, ServiceRequestService serviceRequestService, UserService userService) {
        this.pdfService = pdfService;
        this.serviceRequestService = serviceRequestService;
        this.userService = userService;
    }

    /**
     * Get the pdf invoice for the given service request (if it is complete)
     * @return Redirect to the service request details page
     */
    @GetMapping("/serviceRequest/{id}/invoice")
    public ResponseEntity<Resource> getServiceRequestPdf(@PathVariable Long id) {
        LOG.info("GET /serviceRequest/{}/invoice", id);


        // get the user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
        ServiceRequest serviceRequest = validateServiceRequestExists(serviceRequestService.findById(id));

        if(serviceRequest.getContractor() == null || !serviceRequest.isCompleted()){
            LOG.error("Attempt to retrieve invoice for service request not yet marked as complete");
            return ResponseEntity.badRequest().build();
        }

        boolean isOwner = serviceRequest.getOwner().getUserId().equals(currentUser.getUserId());
        boolean isAssignedContractor = serviceRequest.getContractor().getUserId().equals(currentUser.getUserId());
        if(!isOwner && !isAssignedContractor){
            LOG.error("Malicious attempt to retrieve invoice");
            return ResponseEntity.badRequest().build();
        }


        byte[] pdfContent = pdfService.getInvoice(serviceRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice.pdf")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .contentLength(pdfContent.length)
                .body(new ByteArrayResource(pdfContent));  // Used ChatGpt for this one
    }
}