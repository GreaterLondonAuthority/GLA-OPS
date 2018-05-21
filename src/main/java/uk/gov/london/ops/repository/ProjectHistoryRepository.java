/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.domain.project.ProjectHistory;

import java.util.List;

/**
 * @deprecated use the history list in the project entity instead.
 */
public interface ProjectHistoryRepository extends JpaRepository<ProjectHistory, Integer> {

    void deleteByProjectId(Integer projectId);

    List<ProjectHistory> findAllByProjectIdOrderByCreatedOnDesc(Integer projectId);

}
