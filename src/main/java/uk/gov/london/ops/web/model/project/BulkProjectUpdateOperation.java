/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model.project;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 10/03/2017.
 */
public class BulkProjectUpdateOperation {

    public enum Operation {ASSESS, REVERT}

    private List<Integer> projects = new ArrayList<>();

    private Operation operation;

    public BulkProjectUpdateOperation() {
    }

    public BulkProjectUpdateOperation(Operation operation, List<Integer> projects) {
        this.operation = operation;
        this.projects = projects;
    }

    public List<Integer> getProjects() {
        return projects;
    }

    public void setProjects(List<Integer> projects) {
        this.projects = projects;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}
