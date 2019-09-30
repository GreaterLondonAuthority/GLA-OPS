/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.london.ops.domain.organisation.Organisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QProgrammeSummary extends EntityPathBase<ProgrammeSummary> {

    // This HAS to exist, otherwise the JPA repository will not instantiate
    public static final QProgrammeSummary programmeSummary = new QProgrammeSummary();

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final BooleanPath enabled = createBoolean("enabled");

    public final BooleanPath includeRestricted = createBoolean("restricted");

    public final SimplePath<Organisation> managingOrganisation = createSimple("managingOrganisation", Organisation.class);

    public final EnumPath<Programme.Status> status = createEnum("status", Programme.Status.class);

    private BooleanBuilder predicateBuilder = new BooleanBuilder();

    public QProgrammeSummary() {
        super(ProgrammeSummary.class, forVariable("programmeSummary"));
    }

    public void andSearch(String programmeText,
                          Integer programmeId,
                          Collection<Organisation> organisations,
                          Collection<Programme.Status> statuses,
                          boolean includeRestricted) {


        if (CollectionUtils.isNotEmpty(organisations)) {
            predicateBuilder.and(this.managingOrganisation.in(organisations));
        }

        if (CollectionUtils.isNotEmpty(statuses)) {
            predicateBuilder.and(this.status.in(statuses));
        }

        if (!includeRestricted) predicateBuilder.and(this.includeRestricted.eq(false));

        List<Predicate> programmePredicates = new ArrayList<>();

        if (programmeId != null) {
            programmePredicates.add(this.id.eq(programmeId));
        }

        if (programmeText != null) {
            programmePredicates.add(this.name.containsIgnoreCase(programmeText));
        }

        predicateBuilder.andAnyOf(programmePredicates.toArray(new Predicate[programmePredicates.size()]));

    }
    public void andSearch(boolean enabled, boolean restricted, Collection<Programme.Status> statuses) {
        if (!restricted) predicateBuilder.and(this.includeRestricted.eq(false));
        predicateBuilder.and(this.enabled.eq(enabled));
        if (CollectionUtils.isNotEmpty(statuses)) {
            predicateBuilder.and(this.status.in(statuses));
        }
        List<Predicate> programmePredicates = new ArrayList<>();
        predicateBuilder.andAnyOf(programmePredicates.toArray(new Predicate[programmePredicates.size()]));
    }

    public Predicate getPredicate() {
        return predicateBuilder.getValue();
    }
}
