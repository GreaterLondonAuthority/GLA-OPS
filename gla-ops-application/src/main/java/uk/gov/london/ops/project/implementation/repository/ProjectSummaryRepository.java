/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.gov.london.ops.project.ProjectSummary;
import uk.gov.london.ops.user.domain.UserEntity;

import java.util.List;

/**
 * Read-only JPA repository for project summaries.
 *
 * @author Steve Leach
 */
public interface ProjectSummaryRepository extends JpaRepository<ProjectSummary, Integer>,
        QuerydslPredicateExecutor<ProjectSummary> {

    default Page<ProjectSummary> findAll(UserEntity currentUser,
                                         Integer projectId,
                                         String projectName,
                                         Integer organisationId,
                                         String organisationName,
                                         Integer programmeId,
                                         String programmeName,
                                         String assignee,
                                         List<Integer> programmes,
                                         List<Integer> templates,
                                         List<String> states,
                                         boolean watchingProject,
                                         Boolean isProgrammeAllocation,
                                         Pageable pageable) {
        Predicate predicate = new ProjectSummaryQueryBuilder()
                .withAclUser(currentUser.getUsername())
                .withProjectIdOrName(projectId, projectName)
                .withOrganisationIdOrName(organisationId, organisationName)
                .withProgrammeIdOrName(programmeId, programmeName)
                .withAssignee(assignee)
                .withProgrammes(programmes)
                .withTemplates(templates)
                .withStates(states)
                .withUserWatchingProject(watchingProject ? currentUser.getUsername() : null)
                .withIsProgrammeAllocation(isProgrammeAllocation)
                .getPredicate();
        if (predicate != null) {
            return findAll(predicate, pageable);
        } else {
            return findAll(pageable);
        }
    }
}
