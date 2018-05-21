/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

import java.util.ArrayList;
import java.util.List;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import static uk.gov.london.ops.util.GlaOpsUtils.parseInt;

// This HAS to be called QProjectSummary otherwise the JPA repository will not instantiate
public class QProjectSummary extends EntityPathBase<ProjectSummary> {

    // This HAS to exist, otherwise the JPA repository will not instantiate
    public static final QProjectSummary projectSummary = new QProjectSummary();

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath title = createString("title");

    public final NumberPath<Integer> orgId = createNumber("orgId", Integer.class);

    public final NumberPath<Integer> programmeId = createNumber("programmeId", Integer.class);

    public final StringPath programmeName = createString("programmeName");

    public final EnumPath<Project.Status> status = createEnum("status", Project.Status.class);

    public final EnumPath<Project.SubStatus> subStatus = createEnum("subStatus", Project.SubStatus.class);

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    public QProjectSummary() {
        super(ProjectSummary.class, forVariable("projectSummary"));
    }

    public void withOrganisations(List<Integer> organisations) {
        predicateBuilder.and(this.orgId.in(organisations));
    }

    public void andSearch(String project, Integer organisationId, Integer programmeId, String programmeName) {
        List<Predicate> predicates = new ArrayList<>();
        if (project != null) {
            predicates.add(this.title.containsIgnoreCase(project));
            Integer projectId = getProjectId(project);
            if (projectId != null) {
                predicates.add(this.id.eq(projectId));
            }
        }
        else if (organisationId != null) {
            predicates.add(this.orgId.eq(organisationId));
        }
        else if (programmeId != null) {
            predicates.add(this.programmeId.eq(programmeId));
        }
        else if (programmeName != null) {
            predicates.add(this.programmeName.containsIgnoreCase(programmeName));
        }
        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    private Integer getProjectId(String project) {
        if (project.startsWith("P") || project.startsWith("p")) {
            project = project.substring(1);
        }
        return parseInt(project);
    }

    public void andStatuses(List<Project.Status> statuses, List<Project.SubStatus> subStatuses) {
        List<Predicate> predicates = new ArrayList<>();

        if (statuses != null) {
            predicates.add(this.status.in(statuses));
        }

        if (subStatuses != null) {
            predicates.add(this.subStatus.in(subStatuses));
        }

        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

    public Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

}
