package uk.gov.london.ops.project;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QProjectSummary is a Querydsl query type for ProjectSummary
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QProjectSummary extends EntityPathBase<ProjectSummary> {

    private static final long serialVersionUID = -212311172L;

    public static final QProjectSummary projectSummary = new QProjectSummary("projectSummary");

    public final StringPath aclUser = createString("aclUser");

    public final NumberPath<java.math.BigDecimal> allocationTotal = createNumber("allocationTotal", java.math.BigDecimal.class);

    public final StringPath assignee = createString("assignee");

    public final StringPath assigneeName = createString("assigneeName");

    public final DateTimePath<java.time.OffsetDateTime> createdOn = createDateTime("createdOn", java.time.OffsetDateTime.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isProgrammeAllocation = createBoolean("isProgrammeAllocation");

    public final DateTimePath<java.time.OffsetDateTime> lastModified = createDateTime("lastModified", java.time.OffsetDateTime.class);

    public final NumberPath<Integer> leadOrgId = createNumber("leadOrgId", Integer.class);

    public final NumberPath<Integer> managingOrganisationId = createNumber("managingOrganisationId", Integer.class);

    public final StringPath organisationGroupName = createString("organisationGroupName");

    public final NumberPath<Integer> orgGroupId = createNumber("orgGroupId", Integer.class);

    public final NumberPath<Integer> orgId = createNumber("orgId", Integer.class);

    public final StringPath orgName = createString("orgName");

    public final BooleanPath programmeAllocation = createBoolean("programmeAllocation");

    public final NumberPath<Integer> programmeId = createNumber("programmeId", Integer.class);

    public final StringPath programmeName = createString("programmeName");

    public final EnumPath<Project.Recommendation> recommendation = createEnum("recommendation", Project.Recommendation.class);

    public final StringPath state = createString("state");

    public final StringPath statusName = createString("statusName");

    public final StringPath subscriptions = createString("subscriptions");

    public final StringPath subStatusName = createString("subStatusName");

    public final NumberPath<Integer> templateId = createNumber("templateId", Integer.class);

    public final StringPath templateName = createString("templateName");

    public final StringPath title = createString("title");

    public final BooleanPath unapprovedChanges = createBoolean("unapprovedChanges");

    public QProjectSummary(String variable) {
        super(ProjectSummary.class, forVariable(variable));
    }

    public QProjectSummary(Path<? extends ProjectSummary> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProjectSummary(PathMetadata metadata) {
        super(ProjectSummary.class, metadata);
    }

}

