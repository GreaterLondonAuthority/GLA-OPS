/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

public abstract class BroadcastDetail  {

    Integer broadcastId;

    public BroadcastDetail(Integer broadcastId) {
        this.broadcastId = broadcastId;
    }

    public Integer getBroadcastId() {
        return broadcastId;
    }

    public void setBroadcastId(Integer broadcastId) {
        this.broadcastId = broadcastId;
    }
}
