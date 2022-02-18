package uk.gov.london.ops.programme.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProgrammeSummary is a Querydsl query type for ProgrammeSummary
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QProgrammeSummary extends EntityPathBase<ProgrammeSummary> {

    private static final long serialVersionUID = -1767954872L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProgrammeSummary programmeSummary = new QProgrammeSummary("programmeSummary");

    public final DateTimePath<java.time.OffsetDateTime> createdOn = createDateTime("createdOn", java.time.OffsetDateTime.class);

    public final BooleanPath enabled = createBoolean("enabled");

    public final NumberPath<Integer> financialYear = createNumber("financialYear", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath inAssessment = createBoolean("inAssessment");

    public final uk.gov.london.ops.organisation.model.QOrganisationEntity managingOrganisation;

    public final StringPath modifiedBy = createString("modifiedBy");

    public final DateTimePath<java.time.OffsetDateTime> modifiedOn = createDateTime("modifiedOn", java.time.OffsetDateTime.class);

    public final StringPath modifierName = createString("modifierName");

    public final StringPath name = createString("name");

    public final BooleanPath restricted = createBoolean("restricted");

    public final EnumPath<Programme.Status> status = createEnum("status", Programme.Status.class);

    public final ListPath<String, StringPath> supportedReports = this.<String, StringPath>createList("supportedReports", String.class, StringPath.class, PathInits.DIRECT2);

    public final StringPath supportedReportsString = createString("supportedReportsString");

    public final SetPath<uk.gov.london.ops.project.template.domain.TemplateSummary, SimplePath<uk.gov.london.ops.project.template.domain.TemplateSummary>> templates = this.<uk.gov.london.ops.project.template.domain.TemplateSummary, SimplePath<uk.gov.london.ops.project.template.domain.TemplateSummary>>createSet("templates", uk.gov.london.ops.project.template.domain.TemplateSummary.class, SimplePath.class, PathInits.DIRECT2);

    public QProgrammeSummary(String variable) {
        this(ProgrammeSummary.class, forVariable(variable), INITS);
    }

    public QProgrammeSummary(Path<? extends ProgrammeSummary> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProgrammeSummary(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProgrammeSummary(PathMetadata metadata, PathInits inits) {
        this(ProgrammeSummary.class, metadata, inits);
    }

    public QProgrammeSummary(Class<? extends ProgrammeSummary> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.managingOrganisation = inits.isInitialized("managingOrganisation") ? new uk.gov.london.ops.organisation.model.QOrganisationEntity(forProperty("managingOrganisation")) : null;
    }

}

