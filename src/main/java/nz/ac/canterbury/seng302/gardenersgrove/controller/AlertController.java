package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Alert;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.service.AlertService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;


@Controller
public class AlertController {

    private static final Logger LOG = LoggerFactory.getLogger(AlertController.class);
    private final AlertService alertService;
    private final GardenService gardenService;
    private final UserService userService;

    @Autowired
    AlertController(GardenService gardenService, AlertService alertService,
                    UserService userService) {
        this.gardenService = gardenService;
        this.alertService = alertService;
        this.userService = userService;
    }

    /**
    * Handles the users ability to dismiss weather alerts.
    * Alerts will be dismissed until the next day, if the conditions are met
    *
    * @param alertId The ID of the alert to be closed
    * @param gardenId The ID of the garden with the alert
    * @return A redirect to the garden page
    */    
    @PostMapping(value = "/closeAlert")
    public String weatherAlert(
            @RequestParam(name="alertId", defaultValue = "null") long alertId,
            @RequestParam(name="gardenId", defaultValue = "null") long gardenId
    ) {
        LOG.info("/closeAlert");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
        Alert alertToDismiss = alertService.getAlertById(alertId);

        // Check the supplied alert ID matches an existing alert
        if (alertToDismiss == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // Check the supplied garden ID matches the alert
        if (gardenId != alertToDismiss.getGardenId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // Check the supplied garden exists
        Garden garden = gardenService.getGarden(gardenId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST)
        );

        // Check the requesting user owns the garden the alert relates to
        if (!currentUser.getUserId().equals(garden.getOwner().getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // If everything is correct, dismiss the alert
        alertToDismiss.dismissUntilTomorrow();
        alertService.setAlert(alertToDismiss);

        return "redirect:/garden/" + gardenId;
    }
}
