/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectSummary;
import uk.gov.london.ops.domain.project.QProjectSummary;
import uk.gov.london.ops.domain.user.User;

import java.util.List;

/**
 * Read-only JPA repository for project summaries.
 *
 * @author Steve Leach
 */
public interface ProjectSummaryRepository extends JpaRepository<ProjectSummary,Integer>, QueryDslPredicateExecutor<ProjectSummary> {

    // Base query to get the entities from the view
    String BASE_QUERY = "SELECT p FROM uk.gov.london.ops.domain.project.ProjectSummary p ";

    // Add filter for restricting to organisations that the user has access to
    String ORG_QUERY  = BASE_QUERY + "WHERE ((p.managingOrganisationId IN ?1) OR (p.orgId IN ?1)) ";

    String SORT = "ORDER BY lastModified DESC";

    @Query(ORG_QUERY + SORT)
    List<ProjectSummary> findAll(List<Integer> userOrgIDs);

    @Query(ORG_QUERY + " AND (p.orgId IN (?2) OR p.leadOrgId IN (?2)) " + SORT)
    List<ProjectSummary> findProjectsByOrgID(List<Integer> userOrgIDs, List<Integer> orgIDs);

    @Query(ORG_QUERY + " AND (p.id IN (?2)) " + SORT)
    List<ProjectSummary> findProjectsByOrgAndId(List<Integer> userOrgIDs, List<Integer> projectIDs);

    @Query(ORG_QUERY + " AND (p.programmeId IN (?2)) " + SORT)
    List<ProjectSummary> findProjectsByOrgAndProgramme(List<Integer> userOrgIDs, List<Integer> programmeIDs);

    @Query(ORG_QUERY + " AND (LOWER(p.title) LIKE (%?2%)) " + SORT)
    List<ProjectSummary> findProjectsByOrgAndTitle(List<Integer> userOrgIDs, String keywordsLowerCase);

    default Page<ProjectSummary> findAll(User currentUser, String project, Integer organisationId, Integer programmeId, String programmeName,
                                         List<Project.Status> statuses, List<Project.SubStatus> subStatuses, Pageable pageable) {
        QProjectSummary query = new QProjectSummary();
        if (!currentUser.isGla()) {
            query.withOrganisations(currentUser.getOrganisationIds());
        }
        query.andSearch(project, organisationId, programmeId, programmeName);
        query.andStatuses(statuses, subStatuses);

        return findAll(query.getPredicate(), pageable);
    }

}
