/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import uk.gov.london.ops.framework.jpa.ReadOnlyRepository;
import uk.gov.london.ops.project.block.ProjectBlockOverview;
import uk.gov.london.ops.project.block.ProjectBlockType;

import java.util.List;

public interface ProjectBlockOverviewRepository extends ReadOnlyRepository<ProjectBlockOverview, Integer> {

    List<ProjectBlockOverview> findAllByProjectIdOrderByDisplayOrder(Integer projectId);

    ProjectBlockOverview findByProjectIdAndBlockType(Integer projectId, ProjectBlockType blockType);

}
