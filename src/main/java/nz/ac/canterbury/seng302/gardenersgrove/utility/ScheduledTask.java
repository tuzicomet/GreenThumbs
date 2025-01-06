package nz.ac.canterbury.seng302.gardenersgrove.utility;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.VerificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * class for scheduled db cleaning.
 */
@Component
public class ScheduledTask {
    @Autowired
    VerificationTokenService verificationTokenService;
    @Autowired
    UserService userService;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    ProfanityFilterService profanityFilterService;
    @Autowired
    GardenRepository gardenRepository;
    @Autowired
    GardenService gardenService;
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledTask.class);

    /**
     * delete users and tokens, where the token is expired and the user is not enabled
     * runs every 30 seconds
     */
    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    @Transactional
    public void deleteExpiredRecords() {
        LOG.info("CLEARING EXPIRED RECORDS...");

        //get a list of all expired tokens
        List<VerificationToken> tokens = verificationTokenService.getExpiredTokens();
        Integer count = tokens.size();
        for (VerificationToken token : tokens) {
            AbstractUser user = token.getUser();

            // delete the token and user (only delete the user if it is not enabled)
            verificationTokenService.deleteVerificationToken(token);
            if(!user.isEnabled()){
                userService.deleteUserByUserId(user.getUserId());
            }
        }
        if (count == 1) {
            LOG.info(count + " RECORD REMOVED");
        } else {
            LOG.info(count + " RECORDS REMOVED");
        }
    }

    /**
     * Get the first unverified tag from the database and attempt to moderate it
     * If the tag is profane then it will be removed from the database and add a strike to the user
     * Runs approximately every 25 seconds
     */
    @Scheduled(fixedRate = 25105) // Run every 25 (plus a little bit) seconds
    @Transactional
    public void moderateWaitlistItem() {
        LOG.info("Checking waitlist item");

        Optional<Tag> tagToCheck = tagRepository.getUnverifiedTag();

        if(!tagToCheck.isPresent()){
            return;
        }

        Tag tag = tagToCheck.get();
        boolean valid = true;
        boolean moderated = true;
        try{
            valid = profanityFilterService.verifyTag(tag.getContent());
        } catch (Exception e){
            moderated = false;
        }

        if (!moderated) {
            return;
        }
        if(valid){
            tag.setVerified(true);
            tagRepository.save(tag);
        } else{
            List<AbstractUser> users = gardenRepository.findUserIdsByTag(tag.getId());
            for (AbstractUser user: users) {
                userService.addStrike(user);
            }
            gardenService.deleteAttachedTag(tag);
        }
    }
}