package com.sportygroup.eventmgmt.service.impl;

import com.sportygroup.eventmgmt.model.Event;
import com.sportygroup.eventmgmt.model.EventScore;
import com.sportygroup.eventmgmt.processor.EventScoreProcessor;
import com.sportygroup.eventmgmt.service.EventManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Implementation of the EventManagementService interface.
 * This service manages the lifecycle of events, including scheduling and cancelling
 * periodic score processing tasks for live events.
 */
@Service
public class EventManagementServiceImpl implements EventManagementService {

    /** Logger instance for this class */
    private static final Logger LOGGER = LoggerFactory.getLogger(EventManagementServiceImpl.class);

    /** REST template for making API calls */
    @Autowired
    private RestTemplate restTemplate;
    
    /** Kafka template for publishing event scores */
    @Autowired
    private KafkaTemplate<Integer, EventScore> kafkaTemplate;

    /** URL of the external API to fetch event scores */
    @Value("${api.event-score.url}")
    private String apiUrl;
    
    /** Thread pool executor for scheduling periodic tasks */
    private final ScheduledThreadPoolExecutor scheduleExecutionService = new ScheduledThreadPoolExecutor(10);
    
    /** Map to track scheduled tasks by event ID */
    private final Map<Integer, ScheduledFuture> eventFutures = new ConcurrentHashMap<>();

    /**
     * Updates the status of an event and manages its score processing schedule.
     * If the event is set to live, it schedules a periodic task to fetch and publish scores.
     * If the event is set to not live, it cancels any existing scheduled tasks.
     *
     * @param event the event with updated status information
     * @throws HttpClientErrorException if the event is null or has an invalid ID
     */
    @Override
    public void updateEventStatus(Event event) {

        // Validate the event before processing
        validate(event);

        LOGGER.info("Processing event status update for event_id: {} live: {}", event.isLive(), event.getEventId());

        if(event.isLive()) {
            if(!eventFutures.containsKey(event.getEventId())) {
                // Schedule a new periodic task for the event
                LOGGER.debug("Scheduling the event score processor for event_id: {}", event.getEventId());
                EventScoreProcessor eventScoreProcessor = new EventScoreProcessor(kafkaTemplate, restTemplate, apiUrl, event.getEventId());
                ScheduledFuture future = scheduleExecutionService.scheduleAtFixedRate(eventScoreProcessor,
                        10,  // initial delay in seconds
                        10,  // period in seconds
                        TimeUnit.SECONDS);
                eventFutures.put(event.getEventId(), future);
            } else {
                LOGGER.info("Event score processor is already scheduled for event_id: {}", event.getEventId());
            }
        } else if(eventFutures.containsKey(event.getEventId())) {
            // Cancel the scheduled task if the event is no longer live
            LOGGER.info("Cancelling the scheduled tasks for event_id: {}", event.getEventId());
            eventFutures.get(event.getEventId()).cancel(true);
            eventFutures.remove(event.getEventId());
        } else {
            LOGGER.info("No scheduled tasks found for event_id: {}", event.getEventId());
        }
    }

    /**
     * Validates the event object to ensure it meets the required criteria.
     * 
     * @param event the event to validate
     * @throws HttpClientErrorException if the event is null or has an invalid ID
     */
    private void validate(Event event) {
        if (event == null) {
            throw new HttpClientErrorException("Null event request received", BAD_REQUEST, BAD_REQUEST.getReasonPhrase(), null, null, StandardCharsets.UTF_8);
        }
        if(event.getEventId() <= 0) {
            throw new HttpClientErrorException("Invalid event_id request received", BAD_REQUEST, BAD_REQUEST.getReasonPhrase(), null, null, StandardCharsets.UTF_8);
        }
    }
}
