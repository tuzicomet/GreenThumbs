package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.ManageFriendsController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.FriendableUsersFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests to test functionality for manage friends controller
 */
@WebMvcTest(ManageFriendsController.class)
class ManageFriendsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    UserService userService;
    @MockBean
    FriendshipService friendshipService;
    @MockBean
    FriendableUsersFilter friendableUsersFilter;
    // GardenRepository is required by GlobalControllerAdvice
    @MockBean
    private GardenRepository gardenRepository;
    ManageFriendsController controller;

    @BeforeEach
    void setup() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        User user = new User(
                "Mock",
                "User",
                "test@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        userRepository.save(user);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        when(userService.getUserFromAuthentication(Mockito.any(Authentication.class))).thenReturn(user);
        when(userService.getUserByUserId(Mockito.any())).thenReturn(Optional.of(user));

        SecurityContextHolder.setContext(securityContext);

        this.controller = new ManageFriendsController(userService, friendshipService, friendableUsersFilter);
    }

    /**
     * Test to check that GET requests to the manage friends endpoint returns a 200 OK status
     * @throws Exception
     */
    @Test
    void GetManageFriendsPage_StatusOk() throws Exception {
        mockMvc.perform(get("/friends"))
                .andExpect(status().isOk()); // check status is 200 OK
    }
    @Test
    void AddFriend_RequestExists_AcceptsRequest() throws Exception {
        User currentUser = new User();
        currentUser.setUserId(1L);
        User userToAdd = new User();
        userToAdd.setUserId(2L);
        userToAdd.setEmail("email@email.com");

        when(userService.getUserFromAuthentication(Mockito.any())).thenReturn(currentUser);
        when(userService.getUserByEmail("email@email.com")).thenReturn(userToAdd);
        when(userService.getUsersWithPendingRequestsToUser(currentUser))
                .thenReturn(List.of(userToAdd));
        when(friendshipService.acceptRequest(userToAdd, currentUser)).thenReturn(false);

        mockMvc.perform(post("/friends/add")
                        .with(csrf())
                        .param("userToAddEmail", "email@email.com"))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUsersWithPendingRequestsToUser(currentUser);
        verify(friendshipService, times(1)).acceptRequest(userToAdd, currentUser);
        verify(userService, never()).sendFriendRequest(Mockito.any(), Mockito.any());
    }
    @Test
    void AddFriend_NoRequestExists_AddsFriend() throws Exception {
        User currentUser = new User();
        currentUser.setUserId(1L);
        User userToAdd = new User();
        userToAdd.setUserId(2L);
        User otherUser = new User();
        otherUser.setUserId(3L);
        userToAdd.setEmail("email@email.com");

        when(userService.getUserFromAuthentication(Mockito.any())).thenReturn(currentUser);
        when(userService.getUserByEmail("email@email.com")).thenReturn(userToAdd);
        when(userService.getUsersWithPendingRequestsToUser(currentUser))
                .thenReturn(List.of(otherUser));
        when(friendshipService.acceptRequest(userToAdd, currentUser)).thenReturn(false);

        mockMvc.perform(post("/friends/add")
                        .with(csrf())
                        .param("userToAddEmail", "email@email.com"))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUsersWithPendingRequestsToUser(currentUser);
        verify(friendshipService, never()).acceptRequest(userToAdd, currentUser);
        verify(userService, times(1)).sendFriendRequest(Mockito.any(), Mockito.any());
    }
    @Test
    void DeclineFriendRequest_UserExists_DeclinesRequest() throws Exception {
        User currentUser = new User();
        currentUser.setUserId(1L);
        User userToDecline = new User();
        userToDecline.setUserId(2L);
        userToDecline.setEmail("email@email.com");

        when(userService.getUserFromAuthentication(Mockito.any())).thenReturn(currentUser);
        when(userService.getUserByEmail("email@email.com")).thenReturn(userToDecline);
        when(friendshipService.declineRequest(userToDecline, currentUser)).thenReturn(false);
        when(userService.getUsersWithPendingRequestsToUser(currentUser))
                .thenReturn(List.of(userToDecline));

        mockMvc.perform(post("/friends/decline")
                        .with(csrf())
                        .param("userToDeclineEmail", "email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeTab", "pendingInvitesTab"));

        verify(friendshipService, times(1)).declineRequest(userToDecline, currentUser);
        verify(userService, times(1)).getUsersWithPendingRequestsToUser(currentUser);
    }
    @Test
    void AcceptFriendRequest_RequestExists_AcceptsRequest() throws Exception {
        User currentUser = new User();
        currentUser.setUserId(1L);
        User userToAccept = new User();
        userToAccept.setUserId(2L);
        userToAccept.setEmail("accept@email.com");

        when(userService.getUserFromAuthentication(Mockito.any())).thenReturn(currentUser);
        when(userService.getUserByEmail("accept@email.com")).thenReturn(userToAccept);
        when(friendshipService.acceptRequest(userToAccept, currentUser)).thenReturn(false);
        when(userService.getUsersWithPendingRequestsToUser(currentUser))
                .thenReturn(List.of(userToAccept));

        mockMvc.perform(post("/friends/accept")
                        .with(csrf())
                        .param("userToAcceptEmail", "accept@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeTab", "pendingInvitesTab"));

        verify(userService, times(1)).getUsersWithPendingRequestsToUser(currentUser);
        verify(friendshipService, times(1)).acceptRequest(userToAccept, currentUser);
    }
    @Test
    void CancelFriendInvite_InviteExists_CancelsInvite() throws Exception {
        User currentUser = new User();
        currentUser.setUserId(1L);
        User userToCancelInvite = new User();
        userToCancelInvite.setUserId(2L);
        userToCancelInvite.setEmail("cancelinvite@email.com");

        when(userService.getUserFromAuthentication(Mockito.any())).thenReturn(currentUser);
        when(userService.getUserByEmail("cancelinvite@email.com")).thenReturn(userToCancelInvite);
        when(userService.getUsersWithSentRequestsFromUser(currentUser))
                .thenReturn(List.of(userToCancelInvite));
        when(userService.getStatus(currentUser, userToCancelInvite)).thenReturn("Pending");

        mockMvc.perform(post("/friends/cancel")
                        .with(csrf())
                        .param("userToCancelInviteEmail", "cancelinvite@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeTab", "sentRequestsTab"));

        verify(friendshipService, times(1)).cancelRequest(currentUser, userToCancelInvite);
        verify(userService, times(1)).getUsersWithSentRequestsFromUser(currentUser);
        verify(userService, times(1)).getStatus(currentUser, userToCancelInvite);
    }
    @Test
    void RemoveFriend_FriendExists_RemovesFriend() throws Exception {
        User currentUser = new User();
        currentUser.setUserId(1L);
        User userToRemove = new User();
        userToRemove.setUserId(2L);
        userToRemove.setEmail("removefriend@email.com");

        when(userService.getUserFromAuthentication(Mockito.any())).thenReturn(currentUser);
        when(userService.getUserByEmail("removefriend@email.com")).thenReturn(userToRemove);
        when(userService.getFriendsOfUser(currentUser)).thenReturn(List.of(userToRemove));

        mockMvc.perform(post("/friends/remove")
                        .with(csrf())
                        .param("userToRemoveAsFriendEmail", "removefriend@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeTab", "friendsTab"));

        verify(friendshipService, times(1)).removeFriendship(currentUser, userToRemove);
        verify(userService, times(1)).getFriendsOfUser(currentUser);
    }






}