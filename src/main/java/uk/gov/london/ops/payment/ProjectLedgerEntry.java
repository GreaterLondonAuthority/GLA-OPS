/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.SpendType;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.service.ManagedEntityInterface;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.jpa.NonJoin;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.london.ops.payment.LedgerType.PAYMENT;
import static uk.gov.london.ops.service.PermissionType.ORG_VIEW_VENDOR_SAP_ID;

/**
 *
 *
 * Created by chris on 11/01/2017.
 */
@Entity(name = "PROJECT_LEDGER_ENTRY")
@JsonFilter("roleBasedFilter")
public class ProjectLedgerEntry implements OpsEntity<Integer>, ManagedEntityInterface {


    public static final String INVOICE_TRANSACTION = "INV";
    public static final String CREDIT_TRANSACTION = "CRN";
    public static final String DEFAULT_LEDGER_CE_CODE = "544076";
    public static final String SUPPLEMENTARY_PAYMENT = "Supplementary";
    public static final String RECLAIMED_PAYMENT = "Reclaimed";

    public static final Set<LedgerType> PAYMENT_LEDGER_TYPES =
            Stream.of(PAYMENT,LedgerType.DPF,LedgerType.RCGF).collect(Collectors.toSet());

    public static final String MATCH_FUND_CATEGORY = "MatchFund";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ledger_seq_gen")
    @SequenceGenerator(name = "ledger_seq_gen", sequenceName = "ledger_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name="project_id")
    @JoinData(targetTable = "project", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The project for this payment")
    protected Integer projectId;

    @Column(name="reclaim_of_payment_id")
    @JoinData(targetTable = "project_ledger_entry", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "If this is a reclaim then this references the payment this is a reclaim of. ")
    protected Integer reclaimOfPaymentId;

    @Column(name="interest_for_payment_id")
    @JoinData(targetTable = "project_ledger_entry", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "If this is an interest payment then this references the payment this is the interest is for. ")
    protected Integer interestForPaymentId;

    @Column(name="block_id")
    @JoinData(targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The block id for this record. These records will usually be duplicated per block except for payment/reclaim details.")
    protected Integer blockId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Column(name = "quarter")
    private Integer quarter;

    @Column(name = "year_month")
    @JsonIgnore
    private Integer yearMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_status")
    private LedgerStatus ledgerStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_type")
    private LedgerType ledgerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_source")
    private PaymentSource paymentSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "spend_type")
    private SpendType spendType;

    @Column(name = "category")
    private String category;

    @Column(name = "amount")
    // column called amount in DB due to reserved word value
    private BigDecimal value;

    @Column(name = "interest")
    private BigDecimal interest;

    @Column(name = "interestPayment")
    private Boolean interestPayment = Boolean.FALSE;

    @Column(name = "reference")
    private String reference;

    @Column(name = "pcs_phase_number")
    private String pcsPhaseNumber;

    @Column(name="organisation_id")
    @JoinData(targetTable = "organisation", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The organisation paid by this payment")
    private Integer organisationId;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "transaction_date")
    private String transactionDate;

    @Column(name = "sap_category_code")
    @NonJoin("Code from SAP")
    private String sapCategoryCode;

    @Column(name = "description")
    private String description;

    @Column(name = "cost_centre_code")
    @NonJoin("Code from SAP")
    private String costCentreCode;

    @Column(name = "transaction_number")
    private String transactionNumber;

    @Column(name = "invoice_date")
    private String invoiceDate;

    @Column(name="pcs_project_number")
    private Integer pcsProjectNumber;

    @Column(name="claim_id")
    private Integer claimId;

    @Column(name="category_id")
    @JoinData(targetTable = "finance_category", targetColumn = "id", joinType = Join.JoinType.OneToOne, comment = "This is the finance category code for the record")
    private Integer categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_source")
    private LedgerSource ledgerSource;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;


    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "modified_by")
    private User modifiedByUser;

    @Column(name = "sub_category")
    private String subCategory;

    @Column(name = "authorised_on")
    private OffsetDateTime authorisedOn;

    @JoinData(joinType = Join.JoinType.ManyToOne, sourceTable = "project_ledger_entry", targetColumn = "username", targetTable = "users",
            comment = "")
    @Column(name = "authorised_by")
    private String authorisedBy;

    @Column(name = "sent_on")
    private OffsetDateTime sentOn;

    @Column(name = "acknowledged_on")
    private OffsetDateTime acknowledgedOn;

    @Column(name = "cleared_on")
    private OffsetDateTime clearedOn;

    @Column(name = "wbs_code")
    @NonJoin("Code from SAP")
    private String wbsCode;

    @Column(name = "invoice_filename")
    private String invoiceFileName;

    @Column(name = "sap_vendor_id")
    @PermissionRequired({ORG_VIEW_VENDOR_SAP_ID})
    @NonJoin("Not a join")
    private String sapVendorId;

    @Column(name = "external_id")
    @NonJoin("External ID would indicate which milestone this payment relates to but is not joinable directly")
    private Integer externalId;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "programme_name")
    private String programmeName;

    @Column(name = "ce_code")
    private String ceCode;

    @Column(name = "sap_data_id")
    @JoinData(targetTable = "sap_data", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "If this is a payment/reclaim or data received from SAP This will contain the data sent/received.")
    private Integer sapDataId;

    @Transient
    private String authorisor;

    @Transient
    private String creator;


    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    private Organisation managingOrganisation;

    @Column(name = "original_id")
    @JoinData(targetTable = "project_ledger_entry", targetColumn = "id", joinType = Join.JoinType.ManyToOne, comment = "The original version of this row")
    private Integer originalId;

    /** xml file content to be sent to SAP */
    @Column(name = "xml_file")
    private String xmlFile;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "resent_by")
    private User resender;

    @Column(name = "resent_on")
    private OffsetDateTime resentOn;

    public ProjectLedgerEntry() {
    }

    public ProjectLedgerEntry(final Integer projectId,
                              final Integer year,
                              final Integer month,
                              final String category,
                              final String paymentSubType,
                              final BigDecimal value,
                              final LedgerStatus status) {
        this.projectId = projectId;
        this.category = category;
        this.value = value;
        this.ledgerStatus = status;
        this.subCategory = paymentSubType;

        this.year = year;
        this.month = month;
        updateYearMonth();
    }


    public ProjectLedgerEntry(Project project, NamedProjectBlock block, Integer year, LedgerType ledgerType, SpendType spendType, BigDecimal value) {
        this(project.getId(), block.getId(), year, 4, null, ledgerType, spendType, null, value);
        if(this.ledgerType==LedgerType.BUDGET) {
            this.ledgerStatus=LedgerStatus.BUDGET;
        }
    }

    public ProjectLedgerEntry(Project project, NamedProjectBlock block, Integer year, LedgerType ledgerType, SpendType spendType, BigDecimal value, String category) {
        this(project, block, year, ledgerType, spendType, value);
        this.category = category;
    }

    public ProjectLedgerEntry(Project project, NamedProjectBlock block, Integer year, Integer month, LedgerStatus ledgerStatus, LedgerType ledgerType, SpendType spendType, Integer categoryId, String category, BigDecimal value) {
        this(project.getId(), block.getId(), year, month, ledgerStatus, ledgerType, spendType, categoryId, category, value);
    }

    public ProjectLedgerEntry(Project project, NamedProjectBlock block, Integer year, Integer month, LedgerStatus ledgerStatus, LedgerType ledgerType, SpendType spendType, Integer categoryId, BigDecimal value) {
        this(project.getId(), block.getId(), year, month, ledgerStatus, ledgerType, spendType, null, categoryId, value);
    }

    public ProjectLedgerEntry(Integer projectId, Integer blockId, Integer year, Integer month, LedgerStatus ledgerStatus, LedgerType ledgerType, SpendType spendType, Integer categoryId, BigDecimal value) {
        this(projectId, blockId, year, month, ledgerStatus, ledgerType, spendType, null, categoryId, value);
    }

    public ProjectLedgerEntry(Integer projectId, Integer blockId, Integer year, Integer month, LedgerStatus ledgerStatus, LedgerType ledgerType, SpendType spendType, Integer categoryId, String category, BigDecimal value) {
        this(projectId, blockId, year, month, ledgerStatus, ledgerType, spendType, category, categoryId, value);
    }

    public ProjectLedgerEntry(Integer projectId, Integer blockId, Integer year, Integer month, LedgerStatus ledgerStatus,
                               LedgerType ledgerType, SpendType spendType, String category, Integer categoryId, BigDecimal value) {
        this.projectId = projectId;
        this.blockId = blockId;
        this.year = year;
        this.month = month;
        this.ledgerStatus = ledgerStatus;
        this.setLedgerType(ledgerType);
        this.spendType = spendType;
        this.category = category;
        this.categoryId = categoryId;
        this.value = value;
        this.modifiedOn = OffsetDateTime.now();
        updateYearMonth();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
        updateYearMonth();
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
        updateYearMonth();
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    private void updateYearMonth() {
        if (getYear() != null && getMonth() != null) {
            yearMonth = Integer.valueOf(String.format("%d%02d", getYear(), getMonth()));
        } else if (getYear() != null) {
            // if no month then assume start of financial year
            yearMonth = Integer.valueOf(String.format("%d04", getYear()));
        }
    }

    public Integer getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(Integer yearMonth) {
        this.yearMonth = yearMonth;
    }

    public LedgerStatus getLedgerStatus() {
        return ledgerStatus;
    }

    public void setLedgerStatus(LedgerStatus ledgerStatus) {
        this.ledgerStatus = ledgerStatus;
    }

    public LedgerType getLedgerType() {
        return ledgerType;
    }

    public void setLedgerType(LedgerType ledgerType) {
        this.ledgerType = ledgerType;
        this.paymentSource = getPaymentSourceFromLedgerType(ledgerType);
    }

    public static PaymentSource getPaymentSourceFromLedgerType(LedgerType ledgerType) {
        if (LedgerType.PAYMENT.equals(ledgerType)) {
            return PaymentSource.Grant;
        }
        else if (LedgerType.DPF.equals(ledgerType)) {
            return PaymentSource.DPF;
        }
        else if (LedgerType.RCGF.equals(ledgerType)) {
            return PaymentSource.RCGF;
        }
        else {
            return null;
        }
    }

    public PaymentSource getPaymentSource() {
        return paymentSource;
    }

    public void setPaymentSource(PaymentSource paymentSource) {
        this.paymentSource = paymentSource;
    }

    public SpendType getSpendType() {
        return spendType;
    }

    public void setSpendType(SpendType spendType) {
        this.spendType = spendType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getValue() {
        if (value != null) {
            return value.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return null;
    }

    public void updateValue(BigDecimal value) {
        this.value = value;
        this.modifiedOn = OffsetDateTime.now();
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPcsPhaseNumber() {
        return pcsPhaseNumber;
    }

    public void setPcsPhaseNumber(String pcsPhaseNumber) {
        this.pcsPhaseNumber = pcsPhaseNumber;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getSapCategoryCode() {
        return sapCategoryCode;
    }

    public void setSapCategoryCode(String sapCategoryCode) {
        this.sapCategoryCode = sapCategoryCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCostCentreCode() {
        return costCentreCode;
    }

    public void setCostCentreCode(String costCentreCode) {
        this.costCentreCode = costCentreCode;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Integer getPcsProjectNumber() {
        return pcsProjectNumber;
    }

    public void setPcsProjectNumber(Integer pcsProjectNumber) {
        this.pcsProjectNumber = pcsProjectNumber;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public LedgerSource getLedgerSource() {
        return ledgerSource;
    }

    public void setLedgerSource(LedgerSource ledgerSource) {
        this.ledgerSource = ledgerSource;
    }

    public BigDecimal getTotalIncludingInterest() {
        if (interest == null) {
            return value;
        } else if (value != null) {
            return interest.add(value);
        }
        return null;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }



    @Override
    public Organisation getManagingOrganisation() {
        return managingOrganisation;
    }

    public void setManagingOrganisation(Organisation managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public User getModifiedByUser() {
        return modifiedByUser;
    }

    public void setModifiedByUser(User modifiedByUser) {
        this.modifiedByUser = modifiedByUser;
    }

    @Override
    public String getModifiedBy() {
        return modifiedByUser == null? null : modifiedByUser.getUsername();
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedByUser = new User(modifiedBy);
    }

    //======================
    // Pack various values into the description field.
    // Short term hack to avoid database changes.
    // TODO - What to do with the other fields in PaymentRequest?

    public void setWbsCode(String wbsCode) {
        this. wbsCode = wbsCode;
    }

    public String getWbsCode() {
        return wbsCode;
    }

    public OffsetDateTime getAuthorisedOn() {
        return authorisedOn;
    }

    public void setAuthorisedOn(OffsetDateTime authorisedOn) {
        this.authorisedOn = authorisedOn;
    }

    public String getAuthorisedBy() {
        return authorisedBy;
    }

    public void setAuthorisedBy(String authorisedBy) {
        this.authorisedBy = authorisedBy;
    }

    public OffsetDateTime getSentOn() {
        return sentOn;
    }

    public void setSentOn(OffsetDateTime sentOn) {
        this.sentOn = sentOn;
    }

    public OffsetDateTime getAcknowledgedOn() {
        return acknowledgedOn;
    }

    public void setAcknowledgedOn(OffsetDateTime acknowledgedOn) {
        this.acknowledgedOn = acknowledgedOn;
    }

    public OffsetDateTime getClearedOn() {
        return clearedOn;
    }

    public void setClearedOn(OffsetDateTime clearedOn) {
        this.clearedOn = clearedOn;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }


    public String getInvoiceFileName() {
        return invoiceFileName;
    }

    public void setInvoiceFileName(String invoiceFileName) {
        this.invoiceFileName = invoiceFileName;
    }

    public String getSapVendorId() {
        return sapVendorId;
    }

    public void setSapVendorId(String sapVendorId) {
        this.sapVendorId = sapVendorId;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }


    public Integer getInterestForPaymentId() {
        return interestForPaymentId;
    }

    public void setInterestForPaymentId(Integer interestForPaymentId) {
        this.interestForPaymentId = interestForPaymentId;
    }

    public void setOrganisationId(Integer organisationId) {
        this.organisationId = organisationId;
    }

    public Integer getOrganisationId() {
        return organisationId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProgrammeName() {
        return programmeName;
    }

    public void setProgrammeName(String programmeName) {
        this.programmeName = programmeName;
    }

    public Integer getSapDataId() {
        return sapDataId;
    }

    public void setSapDataId(Integer sapDataId) {
        this.sapDataId = sapDataId;
    }

    public String getAuthorisor() {
        return authorisor;
    }

    public void setAuthorisor(String authorisor) {
        this.authorisor = authorisor;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getLastModifierName() {
        return modifiedByUser != null ? modifiedByUser.getFullName() : null;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Integer getReclaimOfPaymentId() {
        return reclaimOfPaymentId;
    }

    public void setReclaimOfPaymentId(Integer reclaimOfPaymentId) {
        this.reclaimOfPaymentId = reclaimOfPaymentId;
    }

    public boolean isReclaim() {
        return this.reclaimOfPaymentId != null;
    }

    public Integer getOriginalId() {
        if (originalId == null) {
            return id;
        }
        return originalId;
    }

    public void setOriginalId(Integer originalId) {
        this.originalId = originalId;
    }

    public String getXmlFile() {
        return xmlFile;
    }

    public void setXmlFile(String xmlFile) {
        this.xmlFile = xmlFile;
    }

    public User getResender() {
        return resender;
    }

    public void setResender(User resender) {
        this.resender = resender;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getResenderName() {
        return resender != null ? resender.getFullName() : null;
    }

    public OffsetDateTime getResentOn() {
        return resentOn;
    }

    public void setResentOn(OffsetDateTime resentOn) {
        this.resentOn = resentOn;
    }

    public String buildInvoiceFileName() {
        String dateString = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(authorisedOn);
        return String.format("TFLeBIS%010d%s.xml", getId(), dateString);
    }

    public Boolean isInterestPayment() {
        return interestPayment;
    }

    public void setInterestPayment(Boolean interestPayment) {
        this.interestPayment = interestPayment;
    }

    public String getOpsInvoiceNumber() {
        return "P" + getProjectId() + "-" + getId();
    }

    public String getCeCode() {
        return ceCode;
    }

    public void setCeCode(String ceCode) {
        this.ceCode = ceCode;
    }

    public Integer getClaimId() {
        return claimId;
    }

    public void setClaimId(Integer claimId) {
        this.claimId = claimId;
    }

    public boolean matchesOriginal(ProjectLedgerEntry entry) {
        return Objects.equals(this.getProjectId(), entry.getProjectId())
                && this.getBlockId() > entry.getBlockId()
                && Objects.equals(this.getYear(), entry.getYear())
                && Objects.equals(this.getMonth(), entry.getMonth())
                && Objects.equals(this.getLedgerStatus(), entry.getLedgerStatus())
                && Objects.equals(this.getLedgerType(), entry.getLedgerType())
                && Objects.equals(this.getSpendType(), entry.getSpendType())
                && Objects.equals(this.getCategory(), entry.getCategory())
                && Objects.equals(this.getValue(), entry.getValue())
                && Objects.equals(this.getCategoryId(), entry.getCategoryId())
                && Objects.equals(this.getClaimId(), entry.getClaimId());
    }

    public ProjectLedgerEntry clone(Integer clonedProjectId, Integer clonedBlockId) {
        ProjectLedgerEntry clone = new ProjectLedgerEntry(
                clonedProjectId != null ? clonedProjectId : this.getProjectId(),
                clonedBlockId,
                this.getYear(),
                this.getMonth(),
                this.getLedgerStatus(),
                this.getLedgerType(),
                this.getSpendType(),
                this.getCategoryId(),
                this.getCategory(),
                this.getValue());
        clone.setCategoryId(this.getCategoryId());
        clone.setClaimId(this.getClaimId());
        clone.updateDetailsFrom(this);
        return clone;
    }

    public void createInterestRecordFrom(ProjectLedgerEntry entry) {
        this.updateDetailsFrom(entry);
        this.setYear(entry.getYear());
        this.setYear(entry.getYear());
        this.setCreatedOn(null);
        this.setCreatedBy(null);
        this.setAuthorisedBy(null);
        this.setAuthorisor(null);
        this.setModifiedByUser(null);
        this.setModifiedOn(null);
        this.setWbsCode(null);
        this.setReclaimOfPaymentId(entry.getReclaimOfPaymentId());
        this.setInterestForPaymentId(entry.getId());
        this.setProjectId(entry.getProjectId());
        this.setBlockId(entry.getBlockId());
        this.setLedgerStatus(LedgerStatus.Pending);
        this.setInterestPayment(true);
        this.setLedgerType(entry.getLedgerType());
        this.setLedgerSource(entry.getLedgerSource());
        this.setCategory(entry.getCategory());
    }

    public void updateDetailsFrom(ProjectLedgerEntry entry) {
        this.setReference(entry.getReference());
        this.setPcsPhaseNumber(entry.getPcsPhaseNumber());
        this.setOrganisationId(entry.getOrganisationId());
        this.setVendorName(entry.getVendorName());
        this.setTransactionDate(entry.getTransactionDate());
        this.setSapCategoryCode(entry.getSapCategoryCode());
        this.setDescription(entry.getDescription());
        this.setCostCentreCode(entry.getCostCentreCode());
        this.setTransactionNumber(entry.getTransactionNumber());
        this.setInterestPayment(entry.isInterestPayment());
        this.setInvoiceDate(entry.getInvoiceDate());
        this.setPcsProjectNumber(entry.getPcsProjectNumber());
        this.setLedgerSource(entry.getLedgerSource());
        this.setModifiedOn(entry.getModifiedOn());
        this.setModifiedByUser(entry.getModifiedByUser());
        this.setSubCategory(entry.getSubCategory());
        this.setAuthorisedOn(entry.getAuthorisedOn());
        this.setAuthorisedBy(entry.getAuthorisedBy());
        this.setSentOn(entry.getSentOn());
        this.setAcknowledgedOn(entry.getAcknowledgedOn());
        this.setClearedOn(entry.getClearedOn());
        this.setWbsCode(entry.getWbsCode());
        this.setInvoiceFileName(entry.getInvoiceFileName());
        this.setCreatedOn(entry.getCreatedOn());
        this.setCreatedBy(entry.getCreatedBy());
        this.setSapVendorId(entry.getSapVendorId());
        this.setExternalId(entry.getExternalId());
        this.setProjectName(entry.getProjectName());
        this.setProgrammeName(entry.getProgrammeName());
        this.setManagingOrganisation(entry.getManagingOrganisation());
        this.setOriginalId(entry.getOriginalId());
        this.setSapDataId(entry.getSapDataId());
        this.setInterest(entry.getInterest());
        this.setReclaimOfPaymentId(entry.getReclaimOfPaymentId());
        this.setQuarter(entry.getQuarter());
        this.setCeCode(entry.getCeCode());
        this.setClaimId(entry.getClaimId());
    }

    public boolean isResendable() {
        return StringUtils.isNotEmpty(xmlFile) &&
                PAYMENT.equals(ledgerType) &&
                LedgerStatus.getApprovedPaymentStatuses().contains(ledgerStatus) &&
                !LedgerStatus.Cleared.equals(ledgerStatus) &&
                authorisedOn != null;
    }

}
