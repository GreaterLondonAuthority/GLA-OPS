/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

// This HAS to be called QProjectSummary otherwise the JPA repository will not instantiate
public class QProjectSummary extends EntityPathBase<ProjectSummary> {

    // This HAS to exist, otherwise the JPA repository will not instantiate
    public static final QProjectSummary projectSummary = new QProjectSummary();

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath title = createString("title");

    public final NumberPath<Integer> orgId = createNumber("orgId", Integer.class);

    public final NumberPath<Integer> managingOrganisationId = createNumber("managingOrganisationId", Integer.class);

    public final StringPath orgName = createString("orgName");

    public final NumberPath<Integer> programmeId = createNumber("programmeId", Integer.class);

    public final NumberPath<Integer> templateId = createNumber("templateId", Integer.class);

    public final StringPath programmeName = createString("programmeName");

    public final StringPath state = createString("state");

    public final StringPath subscriptions = createString("subscriptions");

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    public QProjectSummary() {
        super(ProjectSummary.class, forVariable("projectSummary"));
    }

    public void withOrganisations(List<Integer> organisations) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(this.managingOrganisationId.in(organisations));
        predicates.add(this.orgId.in(organisations));
        predicateBuilder.andAnyOf(predicates.toArray(new Predicate[predicates.size()]));
    }

  public void andSearch(Integer projectId,
                        String projectName,
                        Integer organisationId,
                        String organisationName,
                        Integer programmeId,
                        String programmeName,
                        List<Integer> programmes,
                        List<Integer> templates,
                        List<String> states,
                        String watchingProjectUsername) {

        if (projectId != null || projectName != null) {
            List<Predicate> projectPredicates = new ArrayList<>();

            if (projectId != null) {
                projectPredicates.add(this.id.eq(projectId));
            }

            if (projectName != null) {
                projectPredicates.add(this.title.containsIgnoreCase(projectName));
            }

            predicateBuilder.andAnyOf(projectPredicates.toArray(new Predicate[projectPredicates.size()]));
        }

        if (watchingProjectUsername != null) {
          List<Predicate> watchingPredicates = new ArrayList<>();
          watchingPredicates.add(this.subscriptions.containsIgnoreCase("|" + watchingProjectUsername));
          predicateBuilder.andAnyOf(watchingPredicates.toArray(new Predicate[watchingPredicates.size()]));
        }


        if (organisationId != null || organisationName != null) {
            List<Predicate> organisationPredicates = new ArrayList<>();

            if (organisationId != null) {
                organisationPredicates.add(this.orgId.eq(organisationId));
            }

            if (organisationName != null) {
                organisationPredicates.add(this.orgName.containsIgnoreCase(organisationName));
            }

            predicateBuilder.andAnyOf(organisationPredicates.toArray(new Predicate[organisationPredicates.size()]));
        }

        if (programmeId != null || programmeName != null) {
            List<Predicate> programmePredicate = new ArrayList<>();

            if (programmeId != null) {
                programmePredicate.add(this.programmeId.eq(programmeId));
            }

            if (programmeName != null) {
                programmePredicate.add(this.programmeName.containsIgnoreCase(programmeName));
            }

            predicateBuilder.andAnyOf(programmePredicate.toArray(new Predicate[programmePredicate.size()]));
        }



        if (CollectionUtils.isNotEmpty(programmes)) {
            predicateBuilder.and(this.programmeId.in(programmes));
        }

        if (CollectionUtils.isNotEmpty(templates)) {
            predicateBuilder.and(this.templateId.in(templates));
        }

        if (CollectionUtils.isNotEmpty(states)) {
            predicateBuilder.and(this.state.in(states));
        }
    }

    public Predicate getPredicate() {
        return predicateBuilder.getValue();
    }

}
