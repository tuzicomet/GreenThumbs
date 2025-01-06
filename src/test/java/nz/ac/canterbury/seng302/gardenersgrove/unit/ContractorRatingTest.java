package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Contractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ContractorRatingTest {

    @Test
    void getNumRatings_UninitialisedContractor_returnsZero() {
        Contractor contractor = new Contractor();
        assertEquals(0, contractor.getNumRatings());
    }
    @Test
    void getNumRatings_setNull_returnsZero() {
        Contractor contractor = new Contractor();
        contractor.setNumRatings(null);
        assertEquals(0, contractor.getNumRatings());
    }

    @Test
    void getRatingTotal_UninitialisedContractor_returnsZero() {
        Contractor contractor = new Contractor();
        assertEquals(0, contractor.getRatingTotal());
    }
    @Test
    void getRatingTotal_setNull_returnsZero() {
        Contractor contractor = new Contractor();
        contractor.setRatingTotal(null);
        assertEquals(0, contractor.getRatingTotal());
    }


    @Test
    void getAverageRating_ContractorHasBeenRated_ReturnsAverage(){
        Contractor contractor = new Contractor();
        contractor.setRatingTotal(31);
        contractor.setNumRatings(8);
        assertEquals(3.875, contractor.getAverageRating());
    }
    @Test
    void getAverageRating_ContractorHasNotBeenRated_ReturnsNull(){
        Contractor contractor = new Contractor();
        contractor.setRatingTotal(0);
        contractor.setNumRatings(0);
        assertNull(contractor.getAverageRating());
    }

    @Test
    void getRoundedAverageRating_NoRatings_returnsNull() {
        Contractor contractor = new Contractor();
        contractor.setNumRatings(0);
        assertNull(contractor.getAverageRatingRounded());
    }

    @Test
    void getRoundedAverageRating_ZeroRatng_returnsZero() {
        Contractor contractor = new Contractor();
        contractor.setNumRatings(1);
        contractor.setRatingTotal(0);
        assertEquals(0, contractor.getAverageRatingRounded());
    }

    @Test
    void getRoundedAverageRating_lotsOfDecimals_returnsOneDP() {
        Contractor contractor = new Contractor();
        contractor.setNumRatings(3);
        contractor.setRatingTotal(10);
        assertEquals(3.3f, contractor.getAverageRatingRounded());
    }

    @Test
    void getRoundedAverageRating_lotsOfDecimals_notLotsDecimals() {
        Contractor contractor = new Contractor();
        contractor.setNumRatings(3);
        contractor.setRatingTotal(10);
        assertNotEquals((float) 10/3, contractor.getAverageRatingRounded());
    }

    @Test
    void addRating_ValidRating_AddsRating(){
        Contractor contractor = new Contractor();
        contractor.setRatingTotal(0);
        contractor.setNumRatings(0);
        assertDoesNotThrow(() -> {contractor.addRating(4);});
        assertEquals(4, contractor.getAverageRating());
    }
    @ParameterizedTest
    @ValueSource(ints = {-1, 7, 0})
    void addRating_InvalidRating_ThrowsException(){
        Contractor contractor = new Contractor();
        contractor.setRatingTotal(0);
        contractor.setNumRatings(0);
        assertThrows(IllegalArgumentException.class, () -> {contractor.addRating(7);});
        assertNull(contractor.getAverageRating());
    }


}
