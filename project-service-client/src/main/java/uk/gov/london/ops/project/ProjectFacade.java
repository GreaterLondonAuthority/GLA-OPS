/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

import java.util.Set;

/**
 * Provides a unified and higher level interface to the project module services to makes it easier to use by the other
 * modules. This will avoid having to inject multiple project module services in another module but just having other
 * modules depend on this interface.
 */
public interface ProjectFacade {

    ProjectDetailsSummary getProjectDetailsSummary(Integer projectId);

    ProjectDetailsSummary getProjectDetailsSummary(String projectTitle);

    ProjectBlockSummary getInternalAssessmentBlockSummary(Integer projectId);

    void updateAssumptionsAffectedByCategoryChange(Integer groupId, String oldName, String newName);

    Set<String> getProjectAssignees(Integer projectId);

    Set<String> getProjectAssignees(Integer projectId, Set<String> roles);

}
