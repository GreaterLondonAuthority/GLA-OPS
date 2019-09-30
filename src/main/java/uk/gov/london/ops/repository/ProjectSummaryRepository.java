/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.ops.domain.project.ProjectSummary;
import uk.gov.london.ops.domain.project.QProjectSummary;
import uk.gov.london.ops.domain.user.User;

/**
 * Read-only JPA repository for project summaries.
 *
 * @author Steve Leach
 */
public interface ProjectSummaryRepository extends JpaRepository<ProjectSummary,Integer>, QuerydslPredicateExecutor<ProjectSummary> {

    default Page<ProjectSummary> findAll(User currentUser,
                                         Integer projectId,
                                         String projectName,
                                         Integer organisationId,
                                         String organisationName,
                                         Integer programmeId,
                                         String programmeName,
                                         List<Integer> programmes,
                                         List<Integer> templates,
                                         List<String> states,
                                         boolean watchingProject,
                                         Pageable pageable) {
        QProjectSummary query = new QProjectSummary();
        if (!currentUser.isOpsAdmin()) {
            query.withOrganisations(currentUser.getOrganisationIds());
        }

        query.andSearch(projectId, projectName, organisationId, organisationName, programmeId, programmeName, programmes,
                templates, states, watchingProject ? currentUser.getUsername() : null);

        return findAll(query.getPredicate(), pageable);
    }

}
