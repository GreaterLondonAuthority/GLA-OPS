package uk.gov.london.ops.organisation.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QTeamEntity is a Querydsl query type for TeamEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTeamEntity extends EntityPathBase<TeamEntity> {

    private static final long serialVersionUID = -1189839777L;

    public static final QTeamEntity teamEntity = new QTeamEntity("teamEntity");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.time.OffsetDateTime> createdOn = createDateTime("createdOn", java.time.OffsetDateTime.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> members = createNumber("members", Integer.class);

    public final StringPath modifiedBy = createString("modifiedBy");

    public final DateTimePath<java.time.OffsetDateTime> modifiedOn = createDateTime("modifiedOn", java.time.OffsetDateTime.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> organisationId = createNumber("organisationId", Integer.class);

    public final StringPath organisationName = createString("organisationName");

    public final BooleanPath registrationAllowed = createBoolean("registrationAllowed");

    public final BooleanPath skillsGatewayAccessAllowed = createBoolean("skillsGatewayAccessAllowed");

    public final EnumPath<uk.gov.london.ops.organisation.OrganisationStatus> status = createEnum("status", uk.gov.london.ops.organisation.OrganisationStatus.class);

    public QTeamEntity(String variable) {
        super(TeamEntity.class, forVariable(variable));
    }

    public QTeamEntity(Path<? extends TeamEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTeamEntity(PathMetadata metadata) {
        super(TeamEntity.class, metadata);
    }

}

