package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.WidgetPreferences;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.WidgetPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * WidgetPreferencesService class to handle services for widget preferences.
 */
@Service
public class WidgetPreferencesService {

    private final WidgetPreferencesRepository widgetPreferencesRepository;
    private final UserRepository userRepository;

    @Autowired
    public WidgetPreferencesService(WidgetPreferencesRepository widgetPreferencesRepository, UserRepository userRepository) {
        this.widgetPreferencesRepository = widgetPreferencesRepository;
        this.userRepository = userRepository;
    }

    /**
     * Given a user's userId, returns their WidgetPreferences
     * @param userId the ID of the user to find WidgetPreferences for
     * @return the user's WidgetPreferences, if found
     */
    public WidgetPreferences findByUserId(long userId) {
        return widgetPreferencesRepository.findByUserId(userId);
    }

    /**
     * initialises widget preferences for a user, given by their user ID
     * @param userId the ID of the user
     */
    public void initialisePreferences(Long userId) {
        // Only if user does not already have preferences
        if (widgetPreferencesRepository.findByUserId(userId) == null) {
            // Initialise widget preferences for the user, with all options as true
            // (i.e. default to all options being shown on the main page)
            WidgetPreferences widgetPreferences = new WidgetPreferences(userId,
                    true, true, true, true
            );
            widgetPreferencesRepository.save(widgetPreferences);
        }
    }

    /**
     * Gets all Plants from persistence
     * @return all Plants currently saved in persistence
     */
    public List<WidgetPreferences> getWidgetPreferences() {
        return widgetPreferencesRepository.findAll();
    }

    /**
     * Adds a widgetPreferences to persistence
     * @param widgetPreferences object to persist
     * @return the saved formResult object
     */
    public WidgetPreferences addWidgetPreference(WidgetPreferences widgetPreferences) {
        WidgetPreferences result =  widgetPreferencesRepository.save(widgetPreferences);
        return result;
    }
}
