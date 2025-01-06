package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Alert;
import nz.ac.canterbury.seng302.gardenersgrove.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AlertService {
    private final AlertRepository alertRepository;

    @Autowired
    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
    * Saves the given alert to the repository
    *
    * @param alert The alert to be saved
    */
    public void setAlert(Alert alert) {
        alertRepository.save(alert);
    }

    /**
    * Retrieves all active alerts for a garden
    *
    * @param gardenId The ID of the garden
    * @return A list of active alerts for the specified garden
    */
    public List<Alert> getAllActiveAlertsFromGarden(long gardenId) {
        return alertRepository.findByGardenIdAndDismissedUntilBefore(gardenId, Instant.now());
    }

    /**
    * Retrieves an alert by its ID
    *
    * @param alertId The ID of the alert
    * @return The alert with the specified ID
    */
    public Alert getAlertById(long alertId){
        return alertRepository.findAlertById(alertId);
    }
    
    /**
    * Retrieves an alert by its type for a garden
    *
    * @param gardenId The ID of the garden
    * @param type The type of the alert
    * @return The alert with the specified type for the garden
    */
    public Alert getAlertByType(long gardenId, int type){
        return alertRepository.findByGardenIdAndType(gardenId, type);
    }

    /**
     * For each alert in a garden, reset the alert's dismiss time.
     *
     * @param gardenId garden to reset the alerts on
     */
    public void resetAllAlertsOfGarden(long gardenId) {
        alertRepository.findByGardenId(gardenId).forEach(alert -> {
            alert.resetDismissal();
            alertRepository.save(alert);
        });
    }
}
