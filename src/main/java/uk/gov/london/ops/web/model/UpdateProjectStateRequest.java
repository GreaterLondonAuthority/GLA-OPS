/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model;

import uk.gov.london.ops.service.project.state.ProjectState;

public class UpdateProjectStateRequest {

    private ProjectState from;
    private ProjectState to;

    public UpdateProjectStateRequest() {}

    public UpdateProjectStateRequest(ProjectState from, ProjectState to) {
        this.from = from;
        this.to = to;
    }

    public ProjectState getFrom() {
        return from;
    }

    public void setFrom(ProjectState from) {
        this.from = from;
    }

    public ProjectState getTo() {
        return to;
    }

    public void setTo(ProjectState to) {
        this.to = to;
    }

}
