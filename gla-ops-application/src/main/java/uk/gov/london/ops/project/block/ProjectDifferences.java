/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block;

import java.util.LinkedList;

/**
 * Lists the differences between the data in two versions of a project block.
 *
 * @author Steve Leach
 */

public class ProjectDifferences extends LinkedList<ProjectDifference> {
    private boolean differentBlockTypes = false;
    private boolean differentVersions = false;
    private boolean differentProjects = false;

    public boolean isDifferentProjects() {
        return differentProjects;
    }

    public void setDifferentProjects(boolean differentProjects) {
        this.differentProjects = differentProjects;
    }

    public boolean isDifferentBlockTypes() {
        return differentBlockTypes;
    }

    public void setDifferentBlockTypes(boolean differentBlockTypes) {
        this.differentBlockTypes = differentBlockTypes;
    }

    public boolean isDifferentVersions() {
        return differentVersions;
    }

    public void setDifferentVersions(boolean differentVersions) {
        this.differentVersions = differentVersions;
    }

    public boolean shouldCompareProperties() {
        return !differentBlockTypes && !differentProjects && differentVersions;
    }
}
