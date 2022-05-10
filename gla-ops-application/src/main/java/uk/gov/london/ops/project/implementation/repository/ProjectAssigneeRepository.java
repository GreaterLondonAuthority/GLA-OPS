/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.project.accesscontrol.ProjectAssignee;

import java.util.List;

public interface ProjectAssigneeRepository extends JpaRepository<ProjectAssignee, Integer> {

    List<ProjectAssignee> findAllByProjectId(Integer projectId);

    @Modifying
    List<ProjectAssignee> deleteAllByProjectId(Integer projectId);

    @Modifying
    @Query(value = "delete from project_assignee where project_id in (?1) and username in (?2)")
    int deleteAllByProjectIdsAndUsernames(List<Integer> projectIds, List<String> usernames);
}
