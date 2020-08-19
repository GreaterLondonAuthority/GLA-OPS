/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.state;

/**
 * Created by chris on 13/03/2017.
 */
public class StateTransitionResult {

    public enum Status {
        SUCCESS, INVALID, ILLEGAL_TRANSITION
    }

    public Status status;
    public String failureMessage;

    public StateTransitionResult(Status status) {
        this.status = status;
    }

    public StateTransitionResult(Status status, String failureMessage) {
        this.status = status;
        this.failureMessage = failureMessage;
    }

    public StateTransitionResult(Status status, ProjectState oldState, ProjectState newState, int projectID) {
        this.status = status;
        this.failureMessage = wasSuccessful() ? "moved" : "cannot move"
                + " project " + projectID
                + " with status "
                + oldState.getStatus()
                + " to status "
                + newState.getStatus();
    }

    public boolean wasSuccessful() {
        return Status.SUCCESS.equals(status);
    }

    public Status getStatus() {
        return status;
    }

    public String getFailureMessage() {
        return failureMessage;
    }
}
