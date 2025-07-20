package com.sportygroup.eventmgmt.controller;

import com.sportygroup.eventmgmt.model.EventScore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EventScoreControllerTest {

    @Test
    void testGetEventScore_ShouldReturnValidScore() {
        EventScoreController controller = new EventScoreController();

        int eventId = 100;
        EventScore score = controller.getEventScore(eventId);

        assertNotNull(score);
        assertEquals(eventId, score.getEventId());
        assertTrue(score.getScore() >= 0 && score.getScore() <= 1000.0f,
                "Score should be in range 0 to 1000");
    }

    @Test
    void testGetEventScore_WithDifferentEventIds() {
        EventScoreController controller = new EventScoreController();

        int[] eventIds = {1, 50, 999};
        for (int id : eventIds) {
            EventScore score = controller.getEventScore(id);
            assertEquals(id, score.getEventId());
            assertTrue(score.getScore() >= 0 && score.getScore() <= 1000.0f);
        }
    }

}