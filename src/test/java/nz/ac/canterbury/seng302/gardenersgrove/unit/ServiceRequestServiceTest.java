package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ServiceRequestRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.ServiceRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

class ServiceRequestServiceTest {

    private ServiceRequestService serviceRequestService;

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private AbstractUser user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        serviceRequestService = new ServiceRequestService(serviceRequestRepository);
    }

    @Test
    void getCurrentServiceRequests_ReturnsCurrentRequests() {
        Pageable pageable = PageRequest.of(0, 10);
        ServiceRequest serviceRequest = new ServiceRequest();
        Page<ServiceRequest> expectedPage = new PageImpl<>(Collections.singletonList(serviceRequest));

        when(serviceRequestRepository.findCurrentServiceRequests(any(AbstractUser.class), any(Instant.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<ServiceRequest> result = serviceRequestService.getCurrentServiceRequests(user, pageable);

        assertEquals(expectedPage, result);
        verify(serviceRequestRepository).findCurrentServiceRequests(any(AbstractUser.class), any(Instant.class), eq(pageable));
    }

    @Test
    void getPastServiceRequests_ReturnsPastRequests() {
        Pageable pageable = PageRequest.of(0, 10);
        ServiceRequest serviceRequest = new ServiceRequest();
        Page<ServiceRequest> expectedPage = new PageImpl<>(Collections.singletonList(serviceRequest));

        when(serviceRequestRepository.findPastServiceRequests(any(AbstractUser.class), any(Instant.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<ServiceRequest> result = serviceRequestService.getPastServiceRequests(user, pageable);

        assertEquals(expectedPage, result);
        verify(serviceRequestRepository).findPastServiceRequests(any(AbstractUser.class), any(Instant.class), eq(pageable));
    }

}
