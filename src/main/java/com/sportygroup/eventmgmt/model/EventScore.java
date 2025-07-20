package com.sportygroup.eventmgmt.model;

/**
 * Represents the score information for a sports event.
 * This model is used to track and transmit score data throughout the system.
 */
public class EventScore {

    /** Unique identifier for the event */
    private int eventId;
    
    /** Current score value for the event */
    private float score;

    /**
     * Default constructor for EventScore.
     */
    public EventScore() {
    }

    /**
     * Parameterized constructor for EventScore.
     * 
     * @param eventId the event identifier
     * @param score the current score value
     */
    public EventScore(int eventId, float score) {
        this.eventId = eventId;
        this.score = score;
    }

    /**
     * Gets the event identifier.
     * 
     * @return the event ID
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * Sets the event identifier.
     * 
     * @param eventId the event ID to set
     */
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the current score value.
     * 
     * @return the score value
     */
    public float getScore() {
        return score;
    }

    /**
     * Sets the score value.
     * 
     * @param score the score value to set
     */
    public void setScore(float score) {
        this.score = score;
    }
}
