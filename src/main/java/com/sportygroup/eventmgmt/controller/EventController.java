package com.sportygroup.eventmgmt.controller;

import com.sportygroup.eventmgmt.model.Event;
import com.sportygroup.eventmgmt.service.EventManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling event-related operations.
 * Provides endpoints to manage event status and lifecycle.
 */
@RestController
@RequestMapping("/events")
public class EventController {

    /** Logger instance for this class */
    private static final Logger LOGGER = LoggerFactory.getLogger(EventController.class);

    /** Service for event management operations */
    @Autowired
    private EventManagementService eventManagementService;

    /**
     * Updates the status of an event.
     * This endpoint handles the transition of events between live and non-live states.
     *
     * @param event the event with updated status information
     * @return ResponseEntity with appropriate status code and body:
     *         - 200 OK if the update was successful
     *         - 400 Bad Request with error details if the request was invalid
     *         - 500 Internal Server Error with error details if an unexpected error occurred
     */
    @PutMapping
    public ResponseEntity updateEventStatus(@RequestBody Event event) {
        try {
            LOGGER.info("Updating event status");
            eventManagementService.updateEventStatus(event);
            LOGGER.debug("Event status updated for event_id: {}", event.getEventId());
            return ResponseEntity.ok().build();
        } catch (HttpClientErrorException e) {
            // Handle validation errors from the service layer
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "E1001");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            // Handle unexpected errors
            LOGGER.error("Error while updating event status for event_id: {}", event.getEventId(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "E1002");
            errorResponse.put("error", "Error while updating event status. Please try again later");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
