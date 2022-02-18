/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.framework.jpa.ReadOnlyRepository;
import uk.gov.london.ops.project.ProjectOverview;

public interface ProjectOverviewRepository extends ReadOnlyRepository<ProjectOverview, Integer> {

    /**
     * This method joins with the project permissions view, to check whether the give username is allowed to access the project.
     */
    @Query(value = "select vpo.* "
            + "       from v_project_overview vpo "
            + "       inner join v_project_permissions vpp on vpo.project_id = vpp.project_id "
            + "       where vpo.project_id = ?1 "
            + "       and vpp.username = ?2", nativeQuery = true)
    ProjectOverview findByIdForUser(Integer id, String username);

}
