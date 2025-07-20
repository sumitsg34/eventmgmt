package com.sportygroup.eventmgmt.controller;

import com.sportygroup.eventmgmt.model.Event;
import com.sportygroup.eventmgmt.service.EventManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @InjectMocks
    private EventController eventController;

    @Mock
    private EventManagementService eventManagementService;


    @Test
    void testUpdateEventStatus_Success() {
        Event event = new Event();
        event.setEventId(123);
        event.setLive(true);

        ResponseEntity<?> response = eventController.updateEventStatus(event);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(eventManagementService, times(1)).updateEventStatus(event);
        verifyNoMoreInteractions(eventManagementService);
    }

    @Test
    void testUpdateEventStatus_ValidationError() {
        Event event = new Event();
        event.setEventId(0); // invalid ID

        HttpClientErrorException exception = new HttpClientErrorException(
                "Invalid event ID", HttpStatus.BAD_REQUEST, "Bad Request",
                null, null, StandardCharsets.UTF_8
        );

        doThrow(exception).when(eventManagementService).updateEventStatus(event);

        ResponseEntity<?> response = eventController.updateEventStatus(event);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        Map<?, ?> errorBody = (Map<?, ?>) response.getBody();
        assertEquals("E1001", errorBody.get("errorCode"));
        assertEquals("Invalid event ID", errorBody.get("error"));

        verify(eventManagementService).updateEventStatus(event);
        verifyNoMoreInteractions(eventManagementService);
    }

    @Test
    void testUpdateEventStatus_InternalServerError() {
        Event event = new Event();
        event.setEventId(456);
        event.setLive(false);

        doThrow(new RuntimeException("Unexpected DB issue")).when(eventManagementService).updateEventStatus(event);

        ResponseEntity<?> response = eventController.updateEventStatus(event);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        Map<?, ?> errorBody = (Map<?, ?>) response.getBody();
        assertEquals("E1002", errorBody.get("errorCode"));
        assertEquals("Error while updating event status. Please try again later", errorBody.get("error"));

        verify(eventManagementService).updateEventStatus(event);
        verifyNoMoreInteractions(eventManagementService);
    }

}