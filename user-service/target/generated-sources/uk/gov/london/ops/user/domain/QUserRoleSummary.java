package uk.gov.london.ops.user.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserRoleSummary is a Querydsl query type for UserRoleSummary
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QUserRoleSummary extends EntityPathBase<UserRoleSummary> {

    private static final long serialVersionUID = -1968062364L;

    public static final QUserRoleSummary userRoleSummary = new QUserRoleSummary("userRoleSummary");

    public final BooleanPath approved = createBoolean("approved");

    public final NumberPath<Long> approvedThreshold = createNumber("approvedThreshold", Long.class);

    public final ListPath<uk.gov.london.ops.role.model.RoleNameAndDescription, SimplePath<uk.gov.london.ops.role.model.RoleNameAndDescription>> assignableRoles = this.<uk.gov.london.ops.role.model.RoleNameAndDescription, SimplePath<uk.gov.london.ops.role.model.RoleNameAndDescription>>createList("assignableRoles", uk.gov.london.ops.role.model.RoleNameAndDescription.class, SimplePath.class, PathInits.DIRECT2);

    public final BooleanPath authorisedSignatory = createBoolean("authorisedSignatory");

    public final BooleanPath canHaveThreshold = createBoolean("canHaveThreshold");

    public final DateTimePath<java.util.Date> createdOn = createDateTime("createdOn", java.util.Date.class);

    public final StringPath entityType = createString("entityType");

    public final NumberPath<Integer> entityTypeId = createNumber("entityTypeId", Integer.class);

    public final StringPath firstName = createString("firstName");

    public final StringPath lastName = createString("lastName");

    public final NumberPath<Integer> managingOrganisationId = createNumber("managingOrganisationId", Integer.class);

    public final NumberPath<Integer> organisationId = createNumber("organisationId", Integer.class);

    public final StringPath orgName = createString("orgName");

    public final BooleanPath pending = createBoolean("pending");

    public final NumberPath<Long> pendingThreshold = createNumber("pendingThreshold", Long.class);

    public final StringPath role = createString("role");

    public final StringPath roleDescription = createString("roleDescription");

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final StringPath username = createString("username");

    public QUserRoleSummary(String variable) {
        super(UserRoleSummary.class, forVariable(variable));
    }

    public QUserRoleSummary(Path<? extends UserRoleSummary> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserRoleSummary(PathMetadata metadata) {
        super(UserRoleSummary.class, metadata);
    }

}

