package com.sportygroup.eventmgmt.processor;

import com.sportygroup.eventmgmt.model.EventScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Processor responsible for fetching event scores from an external API and publishing them to a Kafka topic.
 * Implements Runnable to be executed as a scheduled task.
 * Includes retry logic to handle intermittent failures when communicating with external systems.
 */
public class EventScoreProcessor implements Runnable {

    /** Kafka topic name for publishing event scores */
    private static final String TOPIC_NAME = "event-score-processor";
    
    /** Logger instance for this class */
    private static final Logger LOGGER = LoggerFactory.getLogger(EventScoreProcessor.class);
    
    /** Maximum number of retry attempts for API calls */
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    /** Base delay in milliseconds between retry attempts */
    private static final long RETRY_DELAY_MS = 1000;

    /** Kafka template for publishing messages */
    private final KafkaTemplate<Integer, EventScore> kafkaTemplate;
    
    /** REST template for making API calls */
    private final RestTemplate restTemplate;
    
    /** URL of the external API to fetch event scores */
    private final String apiUrl;
    
    /** ID of the event to process */
    private final Integer eventId;

    /**
     * Constructs a new EventScoreProcessor with the required dependencies.
     *
     * @param kafkaTemplate Kafka template for publishing messages
     * @param restTemplate REST template for making API calls
     * @param apiUrl URL of the external API to fetch event scores
     * @param eventId ID of the event to process
     */
    public EventScoreProcessor(KafkaTemplate<Integer, EventScore> kafkaTemplate, RestTemplate restTemplate, String apiUrl, Integer eventId) {
        this.kafkaTemplate = kafkaTemplate;
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.eventId = eventId;
    }

    /**
     * Executes the event score processing task with retry logic.
     * Fetches the event score from the external API and publishes it to the Kafka topic.
     * Implements exponential backoff for retries to avoid overwhelming the external system.
     */
    @Override
    public void run() {
        int retryCount = 0;
        boolean success = false;

        while (!success && retryCount < MAX_RETRY_ATTEMPTS) {
            try {
                if (retryCount > 0) {
                    LOGGER.info("Retry attempt {} for event id {}", retryCount, eventId);
                    // Exponential backoff: wait longer with each retry
                    Thread.sleep(RETRY_DELAY_MS * retryCount);
                }
                
                LOGGER.info("Invoking external api to get score for event id {} for url {}", eventId, apiUrl);
                // Make API call to fetch event score
                EventScore eventScore = restTemplate.getForObject(apiUrl, EventScore.class, eventId);
                LOGGER.debug("Score for event id {} is {}", eventId, eventScore.getScore());
                
                // Publish event score to Kafka topic
                kafkaTemplate.send(TOPIC_NAME, eventScore);
                LOGGER.info("Event score sent to kafka topic for event id {}", eventId);
                success = true;
            } catch (RestClientException e) {
                // These exceptions are typically related to network issues or temporary service unavailability
                retryCount++;
                if (retryCount >= MAX_RETRY_ATTEMPTS) {
                    LOGGER.error("Failed to process event score after {} retry attempts for event id {}", MAX_RETRY_ATTEMPTS, eventId, e);
                } else {
                    LOGGER.warn("Temporary error while processing event score for event id {}. Will retry. Error: {}", eventId, e.getMessage());
                }
            } catch (Exception e) {
                // For other exceptions, don't retry
                LOGGER.error("Unrecoverable error while processing event score for event id {}", eventId, e);
                break;
            }
        }
    }
}
