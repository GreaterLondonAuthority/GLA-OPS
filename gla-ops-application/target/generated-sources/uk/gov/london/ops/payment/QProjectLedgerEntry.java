package uk.gov.london.ops.payment;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProjectLedgerEntry is a Querydsl query type for ProjectLedgerEntry
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QProjectLedgerEntry extends EntityPathBase<ProjectLedgerEntry> {

    private static final long serialVersionUID = 1336743250L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProjectLedgerEntry projectLedgerEntry = new QProjectLedgerEntry("projectLedgerEntry");

    public final DateTimePath<java.time.OffsetDateTime> acknowledgedOn = createDateTime("acknowledgedOn", java.time.OffsetDateTime.class);

    public final StringPath authorisedBy = createString("authorisedBy");

    public final DateTimePath<java.time.OffsetDateTime> authorisedOn = createDateTime("authorisedOn", java.time.OffsetDateTime.class);

    public final StringPath authorisor = createString("authorisor");

    public final NumberPath<Integer> blockId = createNumber("blockId", Integer.class);

    public final StringPath category = createString("category");

    public final NumberPath<Integer> categoryId = createNumber("categoryId", Integer.class);

    public final StringPath ceCode = createString("ceCode");

    public final NumberPath<Integer> claimId = createNumber("claimId", Integer.class);

    public final DateTimePath<java.time.OffsetDateTime> clearedOn = createDateTime("clearedOn", java.time.OffsetDateTime.class);

    public final StringPath comments = createString("comments");

    public final StringPath companyName = createString("companyName");

    public final StringPath costCentreCode = createString("costCentreCode");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.time.OffsetDateTime> createdOn = createDateTime("createdOn", java.time.OffsetDateTime.class);

    public final StringPath creator = createString("creator");

    public final StringPath description = createString("description");

    public final NumberPath<Integer> externalId = createNumber("externalId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<java.math.BigDecimal> interest = createNumber("interest", java.math.BigDecimal.class);

    public final NumberPath<Integer> interestForPaymentId = createNumber("interestForPaymentId", Integer.class);

    public final BooleanPath interestPayment = createBoolean("interestPayment");

    public final StringPath invoiceDate = createString("invoiceDate");

    public final StringPath invoiceFileName = createString("invoiceFileName");

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

    public final NumberPath<Integer> originalId = createNumber("originalId", Integer.class);

    public final StringPath paymentSource = createString("paymentSource");

    public final SimplePath<uk.gov.london.ops.refdata.PaymentSource> paymentSourceDetails = createSimple("paymentSourceDetails", uk.gov.london.ops.refdata.PaymentSource.class);

    public final StringPath pcsPhaseNumber = createString("pcsPhaseNumber");

    public final NumberPath<Integer> pcsProjectNumber = createNumber("pcsProjectNumber", Integer.class);

    public final StringPath programmeName = createString("programmeName");

    public final NumberPath<Integer> projectId = createNumber("projectId", Integer.class);

    public final StringPath projectName = createString("projectName");

    public final NumberPath<Integer> quarter = createNumber("quarter", Integer.class);

    public final BooleanPath reclaim = createBoolean("reclaim");

    public final NumberPath<Integer> reclaimOfPaymentId = createNumber("reclaimOfPaymentId", Integer.class);

    public final BooleanPath reconcilliationPayment = createBoolean("reconcilliationPayment");

    public final StringPath reference = createString("reference");

    public final BooleanPath resendable = createBoolean("resendable");

    public final StringPath resender = createString("resender");

    public final StringPath resenderName = createString("resenderName");

    public final DateTimePath<java.time.OffsetDateTime> resentOn = createDateTime("resentOn", java.time.OffsetDateTime.class);

    public final StringPath sapCategoryCode = createString("sapCategoryCode");

    public final NumberPath<Integer> sapDataId = createNumber("sapDataId", Integer.class);

    public final StringPath sapVendorId = createString("sapVendorId");

    public final DateTimePath<java.time.OffsetDateTime> sentOn = createDateTime("sentOn", java.time.OffsetDateTime.class);

    public final EnumPath<SpendType> spendType = createEnum("spendType", SpendType.class);

    public final StringPath subCategory = createString("subCategory");

    public final StringPath supplierProductCode = createString("supplierProductCode");

    public final NumberPath<Integer> thresholdOrganisation = createNumber("thresholdOrganisation", Integer.class);

    public final NumberPath<Long> thresholdValue = createNumber("thresholdValue", Long.class);

    public final NumberPath<java.math.BigDecimal> totalIncludingInterest = createNumber("totalIncludingInterest", java.math.BigDecimal.class);

    public final StringPath transactionDate = createString("transactionDate");

    public final StringPath transactionNumber = createString("transactionNumber");

    public final NumberPath<java.math.BigDecimal> value = createNumber("value", java.math.BigDecimal.class);

    public final StringPath vendorName = createString("vendorName");

    public final StringPath wbsCode = createString("wbsCode");

    public final StringPath xmlFile = createString("xmlFile");

    public final NumberPath<Integer> year = createNumber("year", Integer.class);

    public final NumberPath<Integer> yearMonth = createNumber("yearMonth", Integer.class);

    public QProjectLedgerEntry(String variable) {
        this(ProjectLedgerEntry.class, forVariable(variable), INITS);
    }

    public QProjectLedgerEntry(Path<? extends ProjectLedgerEntry> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProjectLedgerEntry(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProjectLedgerEntry(PathMetadata metadata, PathInits inits) {
        this(ProjectLedgerEntry.class, metadata, inits);
    }

    public QProjectLedgerEntry(Class<? extends ProjectLedgerEntry> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.managingOrganisation = inits.isInitialized("managingOrganisation") ? new uk.gov.london.ops.organisation.model.QOrganisationEntity(forProperty("managingOrganisation")) : null;
    }

}

