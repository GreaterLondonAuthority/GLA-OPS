/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

import org.springframework.stereotype.Service;

@Service
/**
 * service for managing Project Objectives
 */
public class ProjectObjectiveService extends RepeatingEntityService<ProjectObjective> {

    @Override
    public Class<ProjectObjective> getEntityType() {
        return ProjectObjective.class;
    }
}
