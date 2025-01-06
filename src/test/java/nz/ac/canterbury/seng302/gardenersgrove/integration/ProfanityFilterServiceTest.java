package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.mockito.Mockito;
import org.springframework.web.client.HttpClientErrorException;

import static org.mockito.Mockito.*;

class ProfanityFilterServiceTest {
    ProfanityFilterService profanityFilterService;
    static final String PROFANITY = "fuck";
    static final String CLEAN = "frick";
    @BeforeEach
    void setup() {
        profanityFilterService = Mockito.spy(ProfanityFilterService.class);
        doReturn(true).when(profanityFilterService).sendProfanityRequest(PROFANITY);
        doReturn(false).when(profanityFilterService).sendProfanityRequest(CLEAN);
    }

    @Test
    void containsProfanity_NoSwearWords_ReturnsFalse() {
        boolean result = profanityFilterService.containsProfanity(CLEAN);
        Assertions.assertFalse(result);
    }

    @Test
    void containsProfanity_HasSwearWords_ReturnsTrue() {
        boolean result = profanityFilterService.containsProfanity(PROFANITY);
        Assertions.assertTrue(result);
    }

    @ParameterizedTest
    @EmptySource
    void containsProfanity_IsEmpty_ReturnsFalse(String content) {
        boolean result = profanityFilterService.containsProfanity(content);
        Assertions.assertFalse(result);
    }

    @Test
    void containsProfanity_FirstRequestFails_SendsAnotherRequest() {
        doThrow(HttpClientErrorException.class).when(profanityFilterService).sendProfanityRequest(anyString());
        profanityFilterService.containsProfanity(CLEAN);
        verify(profanityFilterService, times(2)).sendProfanityRequest(anyString());
    }

    @Test
    void containsProfanity_SecondRequestFails_ReturnsTrue() {
        doThrow(HttpClientErrorException.class).when(profanityFilterService).sendProfanityRequest(anyString());
        boolean result = profanityFilterService.containsProfanity(CLEAN);
        Assertions.assertTrue(result);
    }

    @Test
    void verifyTag_NoSwearWords_ReturnsTrue() {
        boolean result = profanityFilterService.verifyTag(CLEAN);
        Assertions.assertTrue(result);
    }

    @Test
    void verifyTag_HasSwearWords_ReturnsFalse() {
        boolean result = profanityFilterService.verifyTag(PROFANITY);
        Assertions.assertFalse(result);
    }

    @ParameterizedTest
    @EmptySource
    void verifyTag_IsEmpty_ReturnsTrue(String content) {
        boolean result = profanityFilterService.verifyTag(content);
        Assertions.assertTrue(result);
    }
}
