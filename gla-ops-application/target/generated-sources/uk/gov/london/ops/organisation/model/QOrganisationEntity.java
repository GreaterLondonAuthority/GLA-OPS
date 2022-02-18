package uk.gov.london.ops.organisation.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganisationEntity is a Querydsl query type for OrganisationEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganisationEntity extends EntityPathBase<OrganisationEntity> {

    private static final long serialVersionUID = -1215202788L;

    public static final QOrganisationEntity organisationEntity = new QOrganisationEntity("organisationEntity");

    public final uk.gov.london.common.organisation.QBaseOrganisation _super = new uk.gov.london.common.organisation.QBaseOrganisation(this);

    public final SetPath<OrganisationAction, EnumPath<OrganisationAction>> allowedActions = this.<OrganisationAction, EnumPath<OrganisationAction>>createSet("allowedActions", OrganisationAction.class, EnumPath.class, PathInits.DIRECT2);

    public final BooleanPath annualReturnsEnabled = createBoolean("annualReturnsEnabled");

    public final ListPath<uk.gov.london.ops.annualsubmission.AnnualSubmission, SimplePath<uk.gov.london.ops.annualsubmission.AnnualSubmission>> annualSubmissions = this.<uk.gov.london.ops.annualsubmission.AnnualSubmission, SimplePath<uk.gov.london.ops.annualsubmission.AnnualSubmission>>createList("annualSubmissions", uk.gov.london.ops.annualsubmission.AnnualSubmission.class, SimplePath.class, PathInits.DIRECT2);

    public final BooleanPath approved = createBoolean("approved");

    public final StringPath approvedBy = createString("approvedBy");

    public final StringPath approvedByName = createString("approvedByName");

    public final DateTimePath<java.time.OffsetDateTime> approvedOn = createDateTime("approvedOn", java.time.OffsetDateTime.class);

    public final StringPath bankAccount = createString("bankAccount");

    public final StringPath ceoName = createString("ceoName");

    public final StringPath ceoTitle = createString("ceoTitle");

    public final EnumPath<uk.gov.london.ops.organisation.OrganisationChangeStatusReason> changeStatusReason = createEnum("changeStatusReason", uk.gov.london.ops.organisation.OrganisationChangeStatusReason.class);

    public final StringPath changeStatusReasonDetails = createString("changeStatusReasonDetails");

    public final StringPath charityNumber = createString("charityNumber");

    public final StringPath companyCode = createString("companyCode");

    public final StringPath contactEmail = createString("contactEmail");

    public final StringPath contactNumber = createString("contactNumber");

    public final SetPath<uk.gov.london.ops.contracts.ContractSummary, SimplePath<uk.gov.london.ops.contracts.ContractSummary>> contracts = this.<uk.gov.london.ops.contracts.ContractSummary, SimplePath<uk.gov.london.ops.contracts.ContractSummary>>createSet("contracts", uk.gov.london.ops.contracts.ContractSummary.class, SimplePath.class, PathInits.DIRECT2);

    public final BooleanPath corporateOrganisation = createBoolean("corporateOrganisation");

    public final StringPath createdBy = createString("createdBy");

    public final StringPath createdByName = createString("createdByName");

    public final DateTimePath<java.time.OffsetDateTime> createdOn = createDateTime("createdOn", java.time.OffsetDateTime.class);

    public final NumberPath<Integer> defaultProgrammeId = createNumber("defaultProgrammeId", Integer.class);

    public final StringPath defaultSapVendorId = createString("defaultSapVendorId");

    public final NumberPath<Integer> duplicateOrganisationId = createNumber("duplicateOrganisationId", Integer.class);

    public final StringPath email = createString("email");

    public final NumberPath<Integer> entityType = createNumber("entityType", Integer.class);

    public final StringPath externalReference = createString("externalReference");

    public final StringPath financeContactEmail = createString("financeContactEmail");

    public final StringPath glaContactFullName = createString("glaContactFullName");

    public final StringPath governance = createString("governance");

    public final NumberPath<Integer> iconAttachmentId = createNumber("iconAttachmentId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idAsString = createString("idAsString");

    public final StringPath inactivatedBy = createString("inactivatedBy");

    public final StringPath inactivatedByName = createString("inactivatedByName");

    public final DateTimePath<java.time.OffsetDateTime> inactivatedOn = createDateTime("inactivatedOn", java.time.OffsetDateTime.class);

    public final BooleanPath inactive = createBoolean("inactive");

    public final BooleanPath internalOrganisation = createBoolean("internalOrganisation");

    public final BooleanPath isCharityCommission = createBoolean("isCharityCommission");

    public final BooleanPath isLearningProvider = createBoolean("isLearningProvider");

    public final StringPath knownAs = createString("knownAs");

    public final StringPath legalStatus = createString("legalStatus");

    public final BooleanPath managing = createBoolean("managing");

    public final StringPath modifiedBy = createString("modifiedBy");

    public final DateTimePath<java.time.OffsetDateTime> modifiedOn = createDateTime("modifiedOn", java.time.OffsetDateTime.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> parentOrganisationId = createNumber("parentOrganisationId", Integer.class);

    public final StringPath parentOrganisationName = createString("parentOrganisationName");

    public final StringPath primaryContactEmail = createString("primaryContactEmail");

    public final StringPath primaryContactFirstName = createString("primaryContactFirstName");

    public final StringPath primaryContactLastName = createString("primaryContactLastName");

    public final StringPath primaryContactNumber = createString("primaryContactNumber");

    public final ListPath<OrganisationProgrammeSummary, SimplePath<OrganisationProgrammeSummary>> programmes = this.<OrganisationProgrammeSummary, SimplePath<OrganisationProgrammeSummary>>createList("programmes", OrganisationProgrammeSummary.class, SimplePath.class, PathInits.DIRECT2);

    public final StringPath providerNumber = createString("providerNumber");

    public final BooleanPath registrationAllowed = createBoolean("registrationAllowed");

    public final StringPath registrationKey = createString("registrationKey");

    public final BooleanPath regulated = createBoolean("regulated");

    public final BooleanPath rejected = createBoolean("rejected");

    public final StringPath rejectedBy = createString("rejectedBy");

    public final StringPath rejectedByName = createString("rejectedByName");

    public final DateTimePath<java.time.OffsetDateTime> rejectedOn = createDateTime("rejectedOn", java.time.OffsetDateTime.class);

    public final SetPath<SapIdEntity, SimplePath<SapIdEntity>> sapIds = this.<SapIdEntity, SimplePath<SapIdEntity>>createSet("sapIds", SapIdEntity.class, SimplePath.class, PathInits.DIRECT2);

    public final BooleanPath skillsGatewayAccessAllowed = createBoolean("skillsGatewayAccessAllowed");

    public final StringPath societyNumber = createString("societyNumber");

    public final StringPath sortCode = createString("sortCode");

    public final EnumPath<uk.gov.london.ops.organisation.OrganisationStatus> status = createEnum("status", uk.gov.london.ops.organisation.OrganisationStatus.class);

    public final BooleanPath teamOrganisation = createBoolean("teamOrganisation");

    public final BooleanPath techSupportOrganisation = createBoolean("techSupportOrganisation");

    public final EnumPath<uk.gov.london.ops.organisation.OrganisationType> type = createEnum("type", uk.gov.london.ops.organisation.OrganisationType.class);

    public final NumberPath<Integer> ukprn = createNumber("ukprn", Integer.class);

    public final ListPath<uk.gov.london.ops.user.domain.UserModel, SimplePath<uk.gov.london.ops.user.domain.UserModel>> users = this.<uk.gov.london.ops.user.domain.UserModel, SimplePath<uk.gov.london.ops.user.domain.UserModel>>createList("users", uk.gov.london.ops.user.domain.UserModel.class, SimplePath.class, PathInits.DIRECT2);

    public final StringPath vatNumber = createString("vatNumber");

    public final StringPath viability = createString("viability");

    public final StringPath website = createString("website");

    public QOrganisationEntity(String variable) {
        super(OrganisationEntity.class, forVariable(variable));
    }

    public QOrganisationEntity(Path<? extends OrganisationEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrganisationEntity(PathMetadata metadata) {
        super(OrganisationEntity.class, metadata);
    }

}

