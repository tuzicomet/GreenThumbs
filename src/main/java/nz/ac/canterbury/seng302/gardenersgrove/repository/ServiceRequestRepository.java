package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.Instant;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    /**
     * Gets the current service requests
     * @param pageable
     * @return Pageable list of service requests
     */
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.garden.owner = :user AND ((sr.dateMin <= :currentDate AND sr.dateMax >= :currentDate) OR (sr.dateMin > :currentDate)) AND NOT sr.completed ORDER BY sr.id DESC")
    Page<ServiceRequest> findCurrentServiceRequests(@Param("user") AbstractUser user, @Param("currentDate") Instant currentDate, Pageable pageable);

    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.garden.owner = :user AND sr.dateMax < :currentDate OR sr.completed ORDER BY sr.dateMax DESC")
    Page<ServiceRequest> findPastServiceRequests(@Param("user") AbstractUser user, @Param("currentDate") Instant currentDate, Pageable pageable);
    List<ServiceRequest> findAll();

    /**
     * Gets all service requests that do not have an assigned contractor
     * Conditional JPQL was something I found on ChatGPT
     * @param sort a Sort object that defines the sort column and direction
     * @return the list of available jobs
     */
    @Query("SELECT r FROM ServiceRequest r WHERE r.contractor IS NULL " +
            "AND (:dateMax IS NULL OR r.dateMin <= :dateMax) " +
            "AND (:dateMin IS NULL OR r.dateMax >= :dateMin) " +
            "AND (:priceMax IS NULL OR r.priceMin <= :priceMax) " +
            "AND (:priceMin IS NULL OR r.priceMax >= :priceMin)")
    List<ServiceRequest> getAllAvailable(
            @Param("dateMax") Instant dateMax,
            @Param("dateMin") Instant dateMin,
            @Param("priceMax") Double priceMax,
            @Param("priceMin") Double priceMin,
            Sort sort);

    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.contractor = :contractor AND sr.completed = FALSE ORDER BY sr.agreedDate DESC")
    Page<ServiceRequest> findCurrentAssignedJobs(@Param("contractor") AbstractUser contractor, Pageable pageable);

    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.contractor = :contractor AND sr.completed = TRUE ORDER BY sr.agreedDate DESC")
    Page<ServiceRequest> findPastAssignedJobs(@Param("contractor") AbstractUser contractor, Pageable pageable);
}
