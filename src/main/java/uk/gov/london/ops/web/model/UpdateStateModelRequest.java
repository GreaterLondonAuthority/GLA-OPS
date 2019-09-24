/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model;

import uk.gov.london.ops.service.project.state.ProjectState;
import uk.gov.london.ops.service.project.state.StateModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UpdateStateModelRequest {

    private StateModel stateModel;

    private List<UpdateProjectStateRequest> stateUpdates = new ArrayList<>();

    public StateModel getStateModel() {
        return stateModel;
    }

    public void setStateModel(StateModel stateModel) {
        this.stateModel = stateModel;
    }

    public List<UpdateProjectStateRequest> getStateUpdates() {
        return stateUpdates;
    }

    public void setStateUpdates(List<UpdateProjectStateRequest> stateUpdates) {
        this.stateUpdates = stateUpdates;
    }

    public ProjectState getToState(ProjectState fromState) {
        return stateUpdates.stream().filter(su ->
                Objects.equals(su.getFrom().getStatus(), fromState.getStatus()) && Objects.equals(su.getFrom().getSubStatus(), fromState.getSubStatus())
        ).map(UpdateProjectStateRequest::getTo).findFirst().orElse(null);
    }

}
