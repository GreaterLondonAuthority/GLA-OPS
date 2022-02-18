package uk.gov.london.ops.audit;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QAuditableActivityEntity is a Querydsl query type for AuditableActivityEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QAuditableActivityEntity extends EntityPathBase<AuditableActivityEntity> {

    private static final long serialVersionUID = -1233140006L;

    public static final QAuditableActivityEntity auditableActivityEntity = new QAuditableActivityEntity("auditableActivityEntity");

    public final StringPath affectedUserName = createString("affectedUserName");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final NumberPath<Integer> entityId = createNumber("entityId", Integer.class);

    public final EnumPath<uk.gov.london.ops.framework.EntityType> entityType = createEnum("entityType", uk.gov.london.ops.framework.EntityType.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> organisationId = createNumber("organisationId", Integer.class);

    public final StringPath summary = createString("summary");

    public final DateTimePath<java.time.OffsetDateTime> timestamp = createDateTime("timestamp", java.time.OffsetDateTime.class);

    public final EnumPath<ActivityType> type = createEnum("type", ActivityType.class);

    public final StringPath userName = createString("userName");

    public QAuditableActivityEntity(String variable) {
        super(AuditableActivityEntity.class, forVariable(variable));
    }

    public QAuditableActivityEntity(Path<? extends AuditableActivityEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAuditableActivityEntity(PathMetadata metadata) {
        super(AuditableActivityEntity.class, metadata);
    }

}

