/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops;

public class OpsEvent {

    private EventType eventType;
    private String message;
    private Integer externalId;

    public OpsEvent() {}

    public OpsEvent(EventType eventType, String message) {
        this.eventType = eventType;
        this.message = message;
    }

    public OpsEvent(EventType eventType, String message, Integer externalId) {
        this.eventType = eventType;
        this.message = message;
        this.externalId = externalId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }
}
