package com.sportygroup.eventmgmt.service.impl;

import com.sportygroup.eventmgmt.model.Event;
import com.sportygroup.eventmgmt.model.EventScore;
import com.sportygroup.eventmgmt.processor.EventScoreProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventManagementServiceImplTest {


    @InjectMocks
    private EventManagementServiceImpl eventService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KafkaTemplate<Integer, EventScore> kafkaTemplate;

    @Mock
    private ScheduledFuture<?> mockFuture;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    @BeforeEach
    void setup() throws Exception {
        // Set apiUrl manually since it's @Value injected
        Field apiUrlField = EventManagementServiceImpl.class.getDeclaredField("apiUrl");
        apiUrlField.setAccessible(true);
        apiUrlField.set(eventService, "http://mock-api.com/scores");
    }

    @Test
    void testUpdateEventStatus_NullEvent_ShouldThrowException() {
        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.class,
                () -> eventService.updateEventStatus(null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Null event request received"));
    }

    @Test
    void testUpdateEventStatus_InvalidEventId_ShouldThrowException() {
        Event invalidEvent = new Event();
        invalidEvent.setEventId(-1);
        invalidEvent.setLive(true);

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.class,
                () -> eventService.updateEventStatus(invalidEvent)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Invalid event_id request received"));
    }

    @Test
    void testUpdateEventStatus_LiveEvent_ShouldScheduleTask() {
        Event event = new Event();
        event.setEventId(101);
        event.setLive(true);

        ScheduledThreadPoolExecutor executorSpy = Mockito.spy(new ScheduledThreadPoolExecutor(1));
        injectExecutor(eventService, executorSpy);

        eventService.updateEventStatus(event);

        verify(executorSpy, times(1)).scheduleAtFixedRate(
                runnableCaptor.capture(),
                eq(10L),
                eq(10L),
                eq(TimeUnit.SECONDS)
        );

        assertTrue(runnableCaptor.getValue() instanceof EventScoreProcessor);
        verifyNoMoreInteractions(restTemplate, kafkaTemplate);
    }

    @Test
    void testUpdateEventStatus_LiveEvent_AlreadyScheduled_ShouldSkip() {
        Event event = new Event();
        event.setEventId(202);
        event.setLive(true);

        injectFutureManually(eventService, 202, mockFuture);
        eventService.updateEventStatus(event);

        verify(mockFuture, never()).cancel(anyBoolean());
        verifyNoMoreInteractions(restTemplate, kafkaTemplate);
    }

    @Test
    void testUpdateEventStatus_NotLiveEvent_WithScheduledTask_ShouldCancel() {
        Event event = new Event();
        event.setEventId(303);
        event.setLive(false);

        injectFutureManually(eventService, 303, mockFuture);
        eventService.updateEventStatus(event);

        verify(mockFuture).cancel(true);
        // Internal future map should remove it
        assertFalse(getFutureMap(eventService).containsKey(303));
    }

    @Test
    void testUpdateEventStatus_NotLiveEvent_NoTask_ShouldDoNothing() {
        Event event = new Event();
        event.setEventId(404);
        event.setLive(false);

        eventService.updateEventStatus(event);

        // No scheduling or cancel should happen
        verify(mockFuture, never()).cancel(anyBoolean());
        verifyNoInteractions(restTemplate, kafkaTemplate);
    }

    // === Utility Methods ===
    private void injectExecutor(EventManagementServiceImpl service, ScheduledThreadPoolExecutor customExecutor) {
        try {
            Field executorField = EventManagementServiceImpl.class.getDeclaredField("scheduleExecutionService");
            executorField.setAccessible(true);
            executorField.set(service, customExecutor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject custom executor", e);
        }
    }

    private void injectFutureManually(EventManagementServiceImpl service, int eventId, ScheduledFuture<?> future) {
        try {
            Field field = EventManagementServiceImpl.class.getDeclaredField("eventFutures");
            field.setAccessible(true);
            Map<Integer, ScheduledFuture> map = (Map<Integer, ScheduledFuture>) field.get(service);
            map.put(eventId, future);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<Integer, ScheduledFuture> getFutureMap(EventManagementServiceImpl service) {
        try {
            Field field = EventManagementServiceImpl.class.getDeclaredField("eventFutures");
            field.setAccessible(true);
            return (Map<Integer, ScheduledFuture>) field.get(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}