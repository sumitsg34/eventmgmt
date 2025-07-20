package com.sportygroup.eventmgmt.service;

import com.sportygroup.eventmgmt.model.Event;

/**
 * Service interface for managing sports events.
 * Provides operations for updating event status and handling event lifecycle.
 */
public interface EventManagementService {

    /**
     * Updates the status of an event.
     * If the event is set to live, it will schedule score processing.
     * If the event is set to not live, it will cancel any scheduled score processing.
     *
     * @param event the event with updated status information
     * @throws org.springframework.web.client.HttpClientErrorException if the event is invalid
     */
    void updateEventStatus(Event event);

}
