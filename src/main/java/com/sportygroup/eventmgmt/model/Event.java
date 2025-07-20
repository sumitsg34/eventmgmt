package com.sportygroup.eventmgmt.model;

/**
 * Represents a sports event entity with its status information.
 * This model is used to track events and their live status throughout the system.
 */
public class Event {

    /** Unique identifier for the event */
    private int eventId;
    
    /** Flag indicating whether the event is currently live */
    private boolean live;

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
     * Checks if the event is currently live.
     * 
     * @return true if the event is live, false otherwise
     */
    public boolean isLive() {
        return live;
    }

    /**
     * Sets the live status of the event.
     * 
     * @param live the live status to set
     */
    public void setLive(boolean live) {
        this.live = live;
    }
}
