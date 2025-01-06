package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.ServiceRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class ServiceRequestDateStringTest {

    @Test
    void getAgreedDateString_HasAgreedDate_ReturnsCorrectString() {
        ServiceRequest serviceRequest = new ServiceRequest();
        Instant agreedDate = ZonedDateTime.of(2024, 9, 18, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        serviceRequest.setAgreedDate(agreedDate);
        String result = serviceRequest.getAgreedDateString();
        String expectedDate = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault()).format(agreedDate);
        assertEquals(expectedDate, result);
    }

    @Test
    void getAgreedDateString_NoAgreedDate_ReturnsNull() {
        ServiceRequest serviceRequest = new ServiceRequest();
        String result = serviceRequest.getAgreedDateString();
        assertNull(result);
    }
}
