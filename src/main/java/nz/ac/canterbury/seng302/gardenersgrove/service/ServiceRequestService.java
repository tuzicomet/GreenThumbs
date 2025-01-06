package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ServiceRequestRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import java.util.Optional;

/**
 * Service class for handling service requests.
 */
@Service
public class ServiceRequestService {
    private final ServiceRequestRepository serviceRequestRepository;

    /**
     * Constructs a new ServiceRequestService with the repository.
     *
     * @param serviceRequestRepository the repository to be used for service requests
     */
    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    /**
     * Saves the service request.
     *
     * @param serviceRequest the service request to be saved
     */
    public void saveServiceRequest(ServiceRequest serviceRequest) {
        serviceRequestRepository.save(serviceRequest);
    }

    /**
     * Gets the current service requests
     * @param user the user that has the requests
     * @param pageable the page for pagination
     * @return list of service requests
     */
    public Page<ServiceRequest> getCurrentServiceRequests(AbstractUser user, Pageable pageable) {
        Instant currentDate = Instant.now();
        return serviceRequestRepository.findCurrentServiceRequests(user, currentDate, pageable);
    }

    /**
     * Gets the past service requests
     * @param user the user that has the requests
     * @param pageable the page for pagination
     * @return list of service requests
     */
    public Page<ServiceRequest> getPastServiceRequests(AbstractUser user, Pageable pageable) {
        Instant currentDate = Instant.now();
        return serviceRequestRepository.findPastServiceRequests(user, currentDate, pageable);
    }

    /**
     * Gets all service requests with no assigned contractor
     * @return the list of available jobs
     */
    public List<ServiceRequest> getAvailableJobs(String orderPrompt, LocalDate dateMin, LocalDate dateMax, Double priceMin, Double priceMax){
        String column = "id";
        boolean asc = false;

        switch (orderPrompt) {
            case "earliestAvailable":
                column = "dateMin";
                asc = true;
                break;
            case "latestAvailable":
                column = "dateMax";
                break;
            case "lowestBudget":
                column = "priceMin";
                asc = true;
                break;
            case "highestBudget":
                column = "priceMax";
                break;
            case "earliestRelease":
                asc = true;
                break;
            default:
                break;
        }
        Instant dateMinInstant = dateMin == null ? null : dateMin.atStartOfDay().toInstant(OffsetDateTime.now().getOffset());
        Instant dateMaxInstant = dateMax == null ? null : dateMax.atStartOfDay().toInstant(OffsetDateTime.now().getOffset());

        Sort sort = asc ? Sort.by(Sort.Order.asc(column)) : Sort.by(Sort.Order.desc(column));
        return serviceRequestRepository.getAllAvailable(dateMaxInstant, dateMinInstant, priceMax, priceMin, sort);
    }
    /**
     * Finds a service request by its ID.
     *
     * @param id the ID of the service request
     * @return an Optional containing the service request if found, or empty if not found
     */
    public Optional<ServiceRequest> findById(Long id) {
        return serviceRequestRepository.findById(id);
    }
    /**
     * Gets the current assigned jobs
     * @param contractor the contractor that has assigned jobs
     * @param pageable the page for pagination
     * @return list of service requests
     */
    public Page<ServiceRequest> getCurrentAssignedJobs(AbstractUser contractor, Pageable pageable) {
        return serviceRequestRepository.findCurrentAssignedJobs(contractor, pageable);
    }

    /**
     * Gets the past assigned jobs
     * @param contractor the contractor that has the requests
     * @param pageable the page for pagination
     * @return list of service requests
     */
    public Page<ServiceRequest> getPastAssignedJobs(AbstractUser contractor, Pageable pageable) {
        return serviceRequestRepository.findPastAssignedJobs(contractor, pageable);
    }

}
