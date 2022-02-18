package uk.gov.london.ops.payment;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPaymentSummary is a Querydsl query type for PaymentSummary
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPaymentSummary extends EntityPathBase<PaymentSummary> {

    private static final long serialVersionUID = -1649800862L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentSummary paymentSummary = new QPaymentSummary("paymentSummary");

    public final DateTimePath<java.time.OffsetDateTime> authorisedOn = createDateTime("authorisedOn", java.time.OffsetDateTime.class);

    public final StringPath authorisor = createString("authorisor");

    public final StringPath authorisorFirstName = createString("authorisorFirstName");

    public final StringPath authorisorLastName = createString("authorisorLastName");

    public final NumberPath<Integer> blockId = createNumber("blockId", Integer.class);

    public final StringPath category = createString("category");

    public final StringPath ceCode = createString("ceCode");

    public final StringPath comments = createString("comments");

    public final StringPath companyName = createString("companyName");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.time.OffsetDateTime> createdOn = createDateTime("createdOn", java.time.OffsetDateTime.class);

    public final StringPath creator = createString("creator");

    public final StringPath creatorName = createString("creatorName");

    public final DateTimePath<java.time.OffsetDateTime> displayDate = createDateTime("displayDate", java.time.OffsetDateTime.class);

    public final NumberPath<Integer> externalId = createNumber("externalId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<java.math.BigDecimal> interest = createNumber("interest", java.math.BigDecimal.class);

    public final NumberPath<Integer> interestForPaymentId = createNumber("interestForPaymentId", Integer.class);

    public final BooleanPath interestPayment = createBoolean("interestPayment");

    public final StringPath lastModifierName = createString("lastModifierName");

    public final EnumPath<LedgerSource> ledgerSource = createEnum("ledgerSource", LedgerSource.class);

    public final EnumPath<LedgerStatus> ledgerStatus = createEnum("ledgerStatus", LedgerStatus.class);

    public final EnumPath<LedgerType> ledgerType = createEnum("ledgerType", LedgerType.class);

    public final uk.gov.london.ops.organisation.model.QOrganisationEntity managingOrganisation;

    public final StringPath modifiedBy = createString("modifiedBy");

    public final DateTimePath<java.time.OffsetDateTime> modifiedOn = createDateTime("modifiedOn", java.time.OffsetDateTime.class);

    public final NumberPath<Integer> month = createNumber("month", Integer.class);

    public final StringPath opsInvoiceNumber = createString("opsInvoiceNumber");

    public final NumberPath<Integer> organisationId = createNumber("organisationId", Integer.class);

    public final StringPath paymentSource = createString("paymentSource");

    public final SimplePath<uk.gov.london.ops.refdata.PaymentSource> paymentSourceDetails = createSimple("paymentSourceDetails", uk.gov.london.ops.refdata.PaymentSource.class);

    public final NumberPath<Integer> programmeId = createNumber("programmeId", Integer.class);

    public final StringPath programmeName = createString("programmeName");

    public final NumberPath<Integer> projectId = createNumber("projectId", Integer.class);

    public final StringPath projectName = createString("projectName");

    public final NumberPath<Integer> quarter = createNumber("quarter", Integer.class);

    public final BooleanPath reclaim = createBoolean("reclaim");

    public final NumberPath<Integer> reclaimOfPaymentId = createNumber("reclaimOfPaymentId", Integer.class);

    public final BooleanPath resendable = createBoolean("resendable");

    public final StringPath resender = createString("resender");

    public final StringPath resenderName = createString("resenderName");

    public final DateTimePath<java.time.OffsetDateTime> resentOn = createDateTime("resentOn", java.time.OffsetDateTime.class);

    public final StringPath sapVendorId = createString("sapVendorId");

    public final EnumPath<SpendType> spendType = createEnum("spendType", SpendType.class);

    public final StringPath subCategory = createString("subCategory");

    public final NumberPath<Integer> templateId = createNumber("templateId", Integer.class);

    public final NumberPath<java.math.BigDecimal> totalIncludingInterest = createNumber("totalIncludingInterest", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> value = createNumber("value", java.math.BigDecimal.class);

    public final StringPath vendorName = createString("vendorName");

    public final StringPath wbsCode = createString("wbsCode");

    public final StringPath xmlFile = createString("xmlFile");

    public final BooleanPath xmlFileAvailable = createBoolean("xmlFileAvailable");

    public final NumberPath<Integer> year = createNumber("year", Integer.class);

    public QPaymentSummary(String variable) {
        this(PaymentSummary.class, forVariable(variable), INITS);
    }

    public QPaymentSummary(Path<? extends PaymentSummary> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPaymentSummary(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPaymentSummary(PathMetadata metadata, PathInits inits) {
        this(PaymentSummary.class, metadata, inits);
    }

    public QPaymentSummary(Class<? extends PaymentSummary> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.managingOrganisation = inits.isInitialized("managingOrganisation") ? new uk.gov.london.ops.organisation.model.QOrganisationEntity(forProperty("managingOrganisation")) : null;
    }

}

