package uk.gov.london.ops.organisation.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrganisationSummary is a Querydsl query type for OrganisationSummary
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganisationSummary extends EntityPathBase<OrganisationSummary> {

    private static final long serialVersionUID = 717609101L;

    public static final QOrganisationSummary organisationSummary = new QOrganisationSummary("organisationSummary");

    public final StringPath defaultSapVendorId = createString("defaultSapVendorId");

    public final NumberPath<Integer> entityType = createNumber("entityType", Integer.class);

    public final NumberPath<Integer> iconAttachmentId = createNumber("iconAttachmentId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isGlaHNL = createBoolean("isGlaHNL");

    public final StringPath managingOrganisationIconAttachmentId = createString("managingOrganisationIconAttachmentId");

    public final NumberPath<Integer> managingOrganisationId = createNumber("managingOrganisationId", Integer.class);

    public final StringPath managingOrganisationName = createString("managingOrganisationName");

    public final StringPath name = createString("name");

    public final BooleanPath registrationAllowed = createBoolean("registrationAllowed");

    public final StringPath sapVendorIds = createString("sapVendorIds");

    public final EnumPath<uk.gov.london.ops.organisation.OrganisationStatus> status = createEnum("status", uk.gov.london.ops.organisation.OrganisationStatus.class);

    public final NumberPath<Integer> teamId = createNumber("teamId", Integer.class);

    public final StringPath teamName = createString("teamName");

    public QOrganisationSummary(String variable) {
        super(OrganisationSummary.class, forVariable(variable));
    }

    public QOrganisationSummary(Path<? extends OrganisationSummary> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrganisationSummary(PathMetadata metadata) {
        super(OrganisationSummary.class, metadata);
    }

}

