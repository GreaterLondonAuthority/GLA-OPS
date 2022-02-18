package uk.gov.london.ops.assessment;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QAssessmentSummary is a Querydsl query type for AssessmentSummary
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QAssessmentSummary extends EntityPathBase<AssessmentSummary> {

    private static final long serialVersionUID = -922065838L;

    public static final QAssessmentSummary assessmentSummary = new QAssessmentSummary("assessmentSummary");

    public final StringPath assessmentTemplate = createString("assessmentTemplate");

    public final DateTimePath<java.time.OffsetDateTime> createdOn = createDateTime("createdOn", java.time.OffsetDateTime.class);

    public final StringPath creator = createString("creator");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> managingOrgId = createNumber("managingOrgId", Integer.class);

    public final DateTimePath<java.time.OffsetDateTime> modifiedOn = createDateTime("modifiedOn", java.time.OffsetDateTime.class);

    public final NumberPath<Integer> programmeId = createNumber("programmeId", Integer.class);

    public final StringPath programmeName = createString("programmeName");

    public final NumberPath<Integer> projectId = createNumber("projectId", Integer.class);

    public final StringPath projectStatus = createString("projectStatus");

    public final StringPath projectSubStatus = createString("projectSubStatus");

    public final StringPath projectTitle = createString("projectTitle");

    public final EnumPath<AssessmentStatus> status = createEnum("status", AssessmentStatus.class);

    public final StringPath usersPrimaryOrganisation = createString("usersPrimaryOrganisation");

    public QAssessmentSummary(String variable) {
        super(AssessmentSummary.class, forVariable(variable));
    }

    public QAssessmentSummary(Path<? extends AssessmentSummary> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAssessmentSummary(PathMetadata metadata) {
        super(AssessmentSummary.class, metadata);
    }

}

