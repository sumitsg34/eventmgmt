package com.sportygroup.eventmgmt.processor;

import com.sportygroup.eventmgmt.model.EventScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

class EventScoreProcessorTest {

    @Mock
    private KafkaTemplate<Integer, EventScore> kafkaTemplate;

    @Mock
    private RestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<EventScore> eventScoreCaptor;

    private final Integer eventId = 42;
    private final String apiUrl = "http://api.example.com/event/{eventId}";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRun_SuccessOnFirstTry_ShouldCallRestApiAndSendToKafka() {
        EventScore mockScore = new EventScore();
        mockScore.setEventId(eventId);
        mockScore.setScore(20.1f);

        when(restTemplate.getForObject(apiUrl, EventScore.class, eventId)).thenReturn(mockScore);

        EventScoreProcessor processor = new EventScoreProcessor(kafkaTemplate, restTemplate, apiUrl, eventId);
        processor.run();

        verify(restTemplate, times(1)).getForObject(apiUrl, EventScore.class, eventId);
        verify(kafkaTemplate, times(1)).send("event-score-processor", mockScore);
        verifyNoMoreInteractions(restTemplate, kafkaTemplate);
    }

    @Test
    void testRun_TemporaryFailuresAndSuccessOnThirdTry() {
        EventScore mockScore = new EventScore();
        mockScore.setEventId(eventId);
        mockScore.setScore(45.2f);

        when(restTemplate.getForObject(apiUrl, EventScore.class, eventId))
                .thenThrow(new RestClientException("Service down"))
                .thenThrow(new RestClientException("Timeout"))
                .thenReturn(mockScore);

        EventScoreProcessor processor = new EventScoreProcessor(kafkaTemplate, restTemplate, apiUrl, eventId);
        processor.run();

        verify(restTemplate, times(3)).getForObject(apiUrl, EventScore.class, eventId);
        verify(kafkaTemplate).send("event-score-processor", mockScore);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    void testRun_AllRetriesFail_ShouldNotSendToKafka() {
        when(restTemplate.getForObject(apiUrl, EventScore.class, eventId))
                .thenThrow(new RestClientException("Server overload"));

        EventScoreProcessor processor = new EventScoreProcessor(kafkaTemplate, restTemplate, apiUrl, eventId);
        processor.run();

        verify(restTemplate, times(3)).getForObject(apiUrl, EventScore.class, eventId);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void testRun_UnrecoverableException_ShouldNotRetry() {
        when(restTemplate.getForObject(apiUrl, EventScore.class, eventId))
                .thenThrow(new NullPointerException("Unexpected null"));

        EventScoreProcessor processor = new EventScoreProcessor(kafkaTemplate, restTemplate, apiUrl, eventId);
        processor.run();

        verify(restTemplate, times(1)).getForObject(apiUrl, EventScore.class, eventId);
        verifyNoInteractions(kafkaTemplate);
    }

}