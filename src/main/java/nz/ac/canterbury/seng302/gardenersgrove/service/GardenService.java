package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Gardens, defined by the @link{Service} annotation.
 * This class links automatically with @link{GardenRepository}, see the @link{Autowired} annotation below
 */
@Service
public class GardenService {
    private final GardenRepository gardenRepository;
    private final TagRepository tagRepository;

    private static final Logger LOG = LoggerFactory.getLogger(GardenService.class);
    private final AlertService alertService;
    private final ProfanityFilterService profanityFilterService;
    @Autowired
    private MessageSource messageSource;
    private final UserService userService;


    public GardenService(GardenRepository gardenRepository, TagRepository tagRepository, AlertService alertService, ProfanityFilterService profanityFilterService, UserService userService) {
        this.gardenRepository = gardenRepository;
        this.tagRepository = tagRepository;
        this.alertService = alertService;
        this.profanityFilterService = profanityFilterService;
        this.userService = userService;
    }
    /**
     * Gets all FormResults from persistence that are owned by the current user.
     * @return all FormResults currently saved in persistence that are owned by the current user.
     */
    public List<Garden> getGardens() {
        AbstractUser currentUser = (AbstractUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return gardenRepository.findAllByOwnerId(currentUser.getUserId());
    }

    /**
     * Adds a garden to persistence
     * @param garden object to persist
     * @return the saved formResult object
     */
    public Garden addGarden(Garden garden) {
        boolean edit = garden.getId() != null;
        Garden result =  gardenRepository.save(garden);
        // the alert should only be added when we are first making a garden
        if(!edit){
            Alert rainAlert = new Alert(garden.getId(), Instant.now(), Alert.NEED_WATER);
            alertService.setAlert(rainAlert);
            Alert noRainAlert = new Alert(garden.getId(), Instant.now(), Alert.DO_NOT_WATER);
            alertService.setAlert(noRainAlert);
        }
        return result;
    }

    /**
     * Retrieves a Garden with a specific ID
     * @return all FormResults currently saved in persistence
     */
    public Optional<Garden> getGarden(Long id) {
        return gardenRepository.findById(id);
    }

    /**
     * Retrieves a Garden Page containing all information for all public gardens
     * @param pagingSort object used for pagination
     * @return Page<Garden>
     */
    public Page<Garden> getAllPublicGardens(Pageable pagingSort) {return gardenRepository.findAllPublicGardens(pagingSort);}

    /**
     * Retrieves a Garden Page containing all information for public gardens matching the search term
     * @param search search for the name of the garden
     * @param pagingSort object used for pagination
     * @return Page<Garden>
     */
    public Page<Garden> getAllPublicGardensByName(String search, Pageable pagingSort, List<String> tags) {
        if(tags.isEmpty()){
            return gardenRepository.findPublicGardensByName(search, pagingSort);
        } else{
            return gardenRepository.findPublicGardensByNameWithTagFilter(search, pagingSort, tags);
        }
    }

    /**
     * Retrieves a Garden Page containing all information for public gardens with plant names matching the search term
     * @param search for the name of the plant and or garden
     * @param pagingSort object used for pagination
     * @return Page<Garden>
     */
    public Page<Garden> getAllPublicGardensByPlantName(String search, Pageable pagingSort, List<String> tags) {
        if(tags.isEmpty()){
            return gardenRepository.findPublicGardensByPlantName(search, pagingSort);
        } else{
            return gardenRepository.findPublicGardensByPlantNameWithTagFilter(search, pagingSort, tags);
        }

    }

    /**
     * Retrieves recent public gardens, used when the search term is empty
     * @param pagingSort object used for pagination
     * @param tags a list of tags to find matches for, ignored if empty
     * @return Page<Garden> the results
     */
    public Page<Garden> getRecentPublicGardens(Pageable pagingSort, List<String> tags) {
        if(tags.isEmpty()){
            return gardenRepository.findRecentPublicGardens(pagingSort);
        } else{
            return gardenRepository.findRecentPublicGardensWithTagFilter(pagingSort, tags);
        }

    }

    /**
     * Adds a tag to a garden. Searches the repository for an identical tag, and adds to garden if found.
     * This method also attempts to moderate the tags content, and sets the "verified" to indicate if this was successful
     * Otherwise, create a new tag and add it.
     * @param garden Garden to add the tag to
     * @param content The text content of the tag
     */
    public boolean addTagToGarden(Garden garden, String content) {
        boolean valid = true;
        boolean moderated = true;
        try{
            valid = profanityFilterService.verifyTag(content);
        } catch (Exception e){
            moderated = false;
        }
        if(!valid){
            AbstractUser currentUser = userService.getUserFromAuthentication(SecurityContextHolder.getContext().getAuthentication());
            userService.addStrike(currentUser);
            return false;
        }
        boolean finalModerated = moderated;
        Tag tag = tagRepository.findByContent(content).orElseGet(() -> new Tag(content, finalModerated));
        garden.addTag(tag);
        gardenRepository.save(garden);
        return true;
    }

    /**
     * Gets the five closest matches to the input string from existing tags, with an exact match appearing first
     * @param input the current input in the tag field, for which matches must be found
     * @return The five closest matches (not case-sensitive) to the input string from existing tags, in order of length ascending
     */
    public List<String> getAutocompleteTags(String input){
            List<String> tags = tagRepository.findAutocompleteTags(input.toLowerCase());
            Optional<Tag> exactMatch = tagRepository.findByContent(input);
            // This logic exists to ensure we always show an exact match at the top
            if(exactMatch.isPresent() && exactMatch.get().isVerified()){
                if(tags.contains(input)){
                    tags.remove(input);
                }
                tags.add(0, exactMatch.get().getContent());
                if(tags.size() > 5){
                    tags.remove(tags.size() - 1);
                }
            }
            return tags;
    }

    /**
     * Delete tag that is attached to gardens
     * @param tag The tag to delete
     */
    public void deleteAttachedTag(Tag tag) {
        Long tagId = tag.getId();
        List<Garden> gardens = gardenRepository.findByTagId(tagId);
        for (Garden garden : gardens) {
            garden.removeTag(tag);
            gardenRepository.save(garden);
        }
        tagRepository.delete(tag);
    }

    /**
     * Checks if the given garden is in use by an unnasigned service request
     * we don't want to allow edit in this case
     * @param garden the garden
     * @return true if the garden is in use
     */
    public boolean isInUseForUnassignedServiceRequest(Garden garden){
        return gardenRepository.countInUseForUnassignedServiceRequest(garden) > 0;
    }
}
