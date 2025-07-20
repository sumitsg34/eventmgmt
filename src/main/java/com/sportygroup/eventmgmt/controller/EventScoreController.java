package com.sportygroup.eventmgmt.controller;

import com.sportygroup.eventmgmt.model.EventScore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * REST controller for handling event score related operations.
 * Provides endpoints to retrieve score information for events.
 */
@RestController
@RequestMapping("/event-score")
public class EventScoreController {

    /** Random generator for creating mock event scores */
    private final Random randomScoreGenerator = new Random();

    /**
     * Retrieves the score for a specific event.
     * This endpoint generates a random score for demonstration purposes.
     *
     * @param eventId the ID of the event to retrieve the score for
     * @return an EventScore object containing the event ID and its score
     */
    @GetMapping("/{eventId}")
    public EventScore getEventScore(@PathVariable("eventId") int eventId) {
        return new EventScore(eventId, randomScoreGenerator.nextFloat(0, 1001.0f));
    }
}
