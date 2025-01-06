package nz.ac.canterbury.seng302.gardenersgrove.integration;


import nz.ac.canterbury.seng302.gardenersgrove.controller.PdfController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.PdfService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ServiceRequestService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PdfController.class)
class PdfControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    PdfService pdfService;
    @MockBean
    ServiceRequestService serviceRequestService;
    @MockBean
    UserService userService;
    @MockBean
    GardenRepository gardenRepository;
    User owner;
    Contractor contractor;
    Contractor assignedContractor;
    Garden garden;
    Location location;
    Authentication authentication;
    ServiceRequest serviceRequest;

    @BeforeEach
    void setup() {
        authentication = Mockito.mock(Authentication.class);

        owner = new User(
                "Mock",
                "User",
                "test@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );
        owner.setUserId(1L);

        // garden 1
        garden = new Garden(
                "Garden 1",
                "1.0",
                owner,
                "Valid",
                true,
                null,
                null,
                true,
                null
        );

        location = new Location(
                "Engineering Road, Riccarton, Christchurch 8041, New Zealand",
                "New Zealand",
                "Christchurch City",
                "Riccarton",
                "Engineering Road",
                "8041"
        );

        User user = new User(
                "Mock",
                "User",
                "contractor@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );

        contractor = new Contractor(user, "Hi", new ArrayList<>(), location);
        contractor.setUserId(2L);


        User user2 = new User(
                "Mock",
                "User",
                "assigned@gmail.com",
                "Testp4$$",
                "1990-01-01",
                null
        );

        assignedContractor = new Contractor(user2, "Hi", new ArrayList<>(), location);
        assignedContractor.setUserId(3L);

        location.setLon(1.0);
        location.setLat(2.0);
        garden.setGardenId(1L);
        garden.setLocation(location);

        serviceRequest = new ServiceRequest(
                "Service Request Title",
                "Description",
                LocalDate.now().atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                LocalDate.now().plusDays(1).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
                "/images/PlantPlaceholder.jpg",
                50.0,
                100.0,
                garden
        );
        serviceRequest.setCompleted(true);
        serviceRequest.setContractor(assignedContractor);
        serviceRequest.setId(1L);
        when(serviceRequestService.findById(1L)).thenReturn(Optional.of(serviceRequest));
        when(pdfService.getInvoice(any())).thenReturn(new byte[0]);
    }

    @Test
    void GetInvoice_AsOwnerIncompleteRequest_ExpectStatusIs400()  throws Exception {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(owner);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(owner);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);
        serviceRequest.setCompleted(false);

        mockMvc.perform(get("/serviceRequest/1/invoice")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GetInvoice_AsOwnerUnassignedRequest_ExpectStatusIs400()  throws Exception {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(owner);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(owner);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);
        serviceRequest.setContractor(null);

        mockMvc.perform(get("/serviceRequest/1/invoice")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GetInvoice_NotOwnerAndNotAssignedContractor_ExpectStatusIs400()  throws Exception {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(contractor);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(contractor);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/serviceRequest/1/invoice")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GetInvoice_AsOwner_ExpectStatusIsOk()  throws Exception {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(owner);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(owner);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/serviceRequest/1/invoice")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void GetInvoice_AsAssignedContractor_ExpectStatusIsOk()  throws Exception {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(userService.getUserFromAuthentication(authentication)).thenReturn(owner);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(owner);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/serviceRequest/1/invoice")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
