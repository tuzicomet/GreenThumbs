package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.FriendableUsersFilter;
import nz.ac.canterbury.seng302.gardenersgrove.utility.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class ManageFriendsController {

    private final UserService userService;
    private final FriendshipService friendshipService;
    private final FriendableUsersFilter friendableUsersFilter;

    private static final Logger LOG = LoggerFactory.getLogger(ManageFriendsController.class);

    @Autowired
    private MessageSource messageSource;

    /**
     * Constructor for the ManageFriendsController
     * @param userService
     */
    public ManageFriendsController(UserService userService, FriendshipService friendshipService, FriendableUsersFilter friendableUsersFilter) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.friendableUsersFilter = friendableUsersFilter;
    }

    /**
     * Handles the GET request for the manage friends page
     * @param model
     * @return the manageFriends page
     */
    @GetMapping(value = "/friends")
    public String manageFriends(@RequestParam(required = false) String pending,
                                @RequestParam(required = false) String requests,
                                @RequestParam(name = "userQuery", required = false) String userQuery,
                                Model model) {
        LOG.info("/GET /friends");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
            model.addAttribute("user", currentUser);
            model.addAttribute("gardens", currentUser.getOwnedGardens());

            List<AbstractUser> userList = new ArrayList<>();
            Map<AbstractUser, String> statusMap = new HashMap<>();
            if (pending != null) {
                userList = userService.getUsersWithPendingRequestsToUser(currentUser);
                model.addAttribute("activeTab", "pendingInvitesTab");
            } else if (requests != null) {
                userList = userService.getUsersWithSentRequestsFromUser(currentUser);
                for (AbstractUser user : userList) {
                    statusMap.put(user, userService.getStatus(currentUser, user));
                }
                model.addAttribute("statusMap", statusMap);
                model.addAttribute("activeTab", "sentRequestsTab");
            } else if (userQuery != null && !userQuery.isEmpty()) {
                if (userQuery.contains("@")) {
                    AbstractUser foundUser = userService.getUserByEmail(userQuery);
                    if (foundUser != null) {
                        userList = List.of(foundUser);
                    }
                } else {
                    userList = userService.getUsersByName(userQuery);
                }
                userList = friendableUsersFilter.getFriendAbleUsers(userList, currentUser);
                model.addAttribute("activeTab", "search");
            } else {
                userList = userService.getFriendsOfUser(currentUser);
                model.addAttribute("activeTab", "friendsTab");
            }

            model.addAttribute("userList", userList);
            model.addAttribute("listIsEmpty", userList.isEmpty());


            return "manageFriendsTemplate";
        } else {
            return "redirect:/login";
        }
    }

    /**
     * Handles the POST request when the "send invite" button is clicked
     * @param userToAddEmail email of the user to send an invite to
     * @param request  HttpServletRequest object which contains information about the request.
     *                 Used here to get the locale from the request.
     * @param model
     * @return the manage friends page
     */
    @PostMapping(value = "/friends/add")
    public String addFriend(@RequestParam(name  = "userToAddEmail", required = false) String userToAddEmail,
                            HttpServletRequest request,
                            Model model){
        LOG.info("/POST /friends/add");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        if (authentication != null && authentication.isAuthenticated()) {
            AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
            model.addAttribute("user", currentUser);
            model.addAttribute("gardens", currentUser.getOwnedGardens());

            List<AbstractUser> userList = new ArrayList<>();
            Map<AbstractUser, String> statusMap = new HashMap<>();
            String activeTab = "friendsTab";
            if (userToAddEmail != null && !userToAddEmail.isEmpty()) {
                AbstractUser userToAdd = userService.getUserByEmail(userToAddEmail);
                if (userToAdd != null) {
                    if (userService.getUsersWithPendingRequestsToUser(currentUser).stream()
                            .anyMatch(user -> user.getUserId().equals(userToAdd.getUserId()))) {
                        if (friendshipService.acceptRequest(userToAdd, currentUser)) {
                            model.addAttribute(
                                    "errorMessage",
                                    messageSource.getMessage("manageFriends.requestNoLongerExists", null, locale)
                            );
                        }
                        userList = userService.getFriendsOfUser(currentUser);
                        activeTab = "friendsTab";
                    } else {
                        userService.sendFriendRequest(currentUser, userToAdd);
                        userList = userService.getUsersWithSentRequestsFromUser(currentUser);
                        for (AbstractUser user : userList) {
                            statusMap.put(user, userService.getStatus(currentUser, user));
                        }
                        model.addAttribute("statusMap", statusMap);
                        activeTab = "sentRequestsTab";
                    }
                }
            } else {
                userList = userService.getFriendsOfUser(currentUser);
                activeTab = "friendsTab";
            }
            model.addAttribute("userList", userList);
            model.addAttribute("listIsEmpty", userList.isEmpty());
            model.addAttribute("activeTab", activeTab);

            return "manageFriendsTemplate";

        } else {
            return "redirect:/login";
        }

    }
    /**
     * Handles the POST request when the "accept" button is clicked
     * @param userToAcceptEmail email of the user to accept the invite from
     * @param request  HttpServletRequest object which contains information about the request.
     *                 Used here to get the locale from the request.
     * @param model
     * @return the manage friends page
     */
    @PostMapping(value = "/friends/accept")
    public String acceptFriend(
            @RequestParam(name  = "userToAcceptEmail", required = false) String userToAcceptEmail,
            HttpServletRequest request,
            Model model
    ){
        LOG.info("/POST /friends/accept");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        if (authentication != null && authentication.isAuthenticated()) {
            AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
            model.addAttribute("user", currentUser);
            model.addAttribute("gardens", currentUser.getOwnedGardens());

            List<AbstractUser> userList = new ArrayList<>();
            String activeTab = "friendsTab";
            if (userToAcceptEmail != null && !userToAcceptEmail.isEmpty()) {
                AbstractUser userToAccept = userService.getUserByEmail(userToAcceptEmail);
                if (userToAccept != null) {
                    if (friendshipService.acceptRequest(userToAccept, currentUser)) {
                        model.addAttribute(
                                "errorMessage",
                                messageSource.getMessage("manageFriends.requestNoLongerExists", null, locale)
                        );
                    }
                    userList = userService.getUsersWithPendingRequestsToUser(currentUser);
                    activeTab = "pendingInvitesTab";
                }
            } else {
                userList = userService.getFriendsOfUser(currentUser);
                activeTab = "friendsTab";
            }
            model.addAttribute("userList", userList);
            model.addAttribute("listIsEmpty", userList.isEmpty());
            model.addAttribute("activeTab", activeTab);

            return "manageFriendsTemplate";

        } else {
            return "redirect:/login";
        }

    }
    /**
     * Handles the POST request when the "decline" button is clicked
     * @param userToDeclineEmail email of the user to decline the invite from
     * @param request  HttpServletRequest object which contains information about the request.
     *                 Used here to get the locale from the request.
     * @param model
     * @return the manage friends page
     */
    @PostMapping(value = "/friends/decline")
    public String declineFriend(
            @RequestParam(name  = "userToDeclineEmail",required = false) String userToDeclineEmail,
            HttpServletRequest request,
            Model model
    ) {
        LOG.info("/POST /friends/decline");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        if (authentication != null && authentication.isAuthenticated()) {
            AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
            model.addAttribute("user", currentUser);
            model.addAttribute("gardens", currentUser.getOwnedGardens());

            List<AbstractUser> userList = new ArrayList<>();
            String activeTab = "friendsTab";
            if (userToDeclineEmail != null && !userToDeclineEmail.isEmpty()) {
                AbstractUser userToDecline = userService.getUserByEmail(userToDeclineEmail);
                if (userToDecline != null) {
                    if (friendshipService.declineRequest(userToDecline, currentUser)) {
                        model.addAttribute(
                                "errorMessage",
                                messageSource.getMessage("manageFriends.requestNoLongerExists", null, locale)
                        );
                    }
                    userList = userService.getUsersWithPendingRequestsToUser(currentUser);
                    activeTab = "pendingInvitesTab";
                }
            } else  {
                userList = userService.getFriendsOfUser(currentUser);
                activeTab = "friendsTab";
            }
            model.addAttribute("userList", userList);
            model.addAttribute("listIsEmpty", userList.isEmpty());
            model.addAttribute("activeTab", activeTab);

            return "manageFriendsTemplate";

        } else {
            return "redirect:/login";
        }

    }
    /**
     * Handles the POST request when the "cancel" button is clicked
     * @param userToCancelInviteEmail email of the user to cancel the invite to
     *                                     * @param request  HttpServletRequest object which contains information about the request.
     *      *                 Used here to get the locale from the request.
     * @param model
     * @return the manage friends page
     */
    @PostMapping(value = "/friends/cancel")
    public String cancelFriendRequest(@RequestParam(name = "userToCancelInviteEmail", required = false) String userToCancelInviteEmail,
                                Model model){
        LOG.info("/POST /friends/cancel");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
            model.addAttribute("user", currentUser);
            model.addAttribute("gardens", currentUser.getOwnedGardens());

            List<AbstractUser> userList = new ArrayList<>();
            Map<AbstractUser, String> statusMap = new HashMap<>();
            String activeTab = "friendsTab";
            if (userToCancelInviteEmail != null && !userToCancelInviteEmail.isEmpty()) {
                AbstractUser userToCancelInvite = userService.getUserByEmail(userToCancelInviteEmail);
                if (userToCancelInvite != null) {
                    friendshipService.cancelRequest(currentUser, userToCancelInvite);
                    userList = userService.getUsersWithSentRequestsFromUser(currentUser);
                    for (AbstractUser user : userList) {
                        statusMap.put(user, userService.getStatus(currentUser, user));
                    }
                    model.addAttribute("statusMap", statusMap);
                    activeTab = "sentRequestsTab";
                }
            } else  {
                userList = userService.getFriendsOfUser(currentUser);
                activeTab = "friendsTab";
            }
            model.addAttribute("userList", userList);
            model.addAttribute("listIsEmpty", userList.isEmpty());
            model.addAttribute("activeTab", activeTab);

            return "manageFriendsTemplate";

        } else {
            return "redirect:/login";
        }

    }
    /**
     * Handles the POST request when the "remove" button is clicked and confirmed
     * @param userToRemoveAsFriendEmail email of the user to remove as friend
     * @param model
     * @return the manage friends page
     */
    @PostMapping(value = "/friends/remove")
    public String removeFriend(@RequestParam(name  = "userToRemoveAsFriendEmail", required = false) String userToRemoveAsFriendEmail,
                                Model model){
        LOG.info("/POST /friends/remove");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            AbstractUser currentUser = userService.getUserFromAuthentication(authentication);
            model.addAttribute("user", currentUser);
            model.addAttribute("gardens", currentUser.getOwnedGardens());

            List<AbstractUser> userList = new ArrayList<>();
            String activeTab = "friendsTab";
            if (userToRemoveAsFriendEmail != null && !userToRemoveAsFriendEmail.isEmpty()) {
                AbstractUser userToRemove = userService.getUserByEmail(userToRemoveAsFriendEmail);
                if (userToRemove != null) {
                    friendshipService.removeFriendship(currentUser, userToRemove);
                    userList = userService.getFriendsOfUser(currentUser);
                    activeTab = "friendsTab";
                }
            } else {
                userList = userService.getFriendsOfUser(currentUser);
                activeTab = "friendsTab";
            }
            model.addAttribute("userList", userList);
            model.addAttribute("listIsEmpty", userList.isEmpty());
            model.addAttribute("activeTab", activeTab);

            return "manageFriendsTemplate";

        } else {
            return "redirect:/login";
        }

    }

}
