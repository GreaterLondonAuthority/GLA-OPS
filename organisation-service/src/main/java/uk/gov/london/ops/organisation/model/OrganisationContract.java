/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.model;

import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.contracts.ContractModel;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.framework.enums.OrganisationContractStatus;
import uk.gov.london.ops.organisation.OrganisationGroupType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "organisation_contract")
public class OrganisationContract implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organisation_contract_seq_gen")
    @SequenceGenerator(name = "organisation_contract_seq_gen", sequenceName = "organisation_contract_seq", initialValue = 1000,
            allocationSize = 1)
    private Integer id;

    @Column(name = "contract_id")
    private Integer contractId;

    @Transient
    private ContractModel contractModel;

    @Column
    @Enumerated(EnumType.STRING)
    private OrganisationContractStatus status = OrganisationContractStatus.Blank;

    @Column
    @Enumerated(EnumType.STRING)
    private OrganisationGroupType orgGroupType;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Column(name = "accepted_by")
    private String acceptedBy;

    @Column(name = "accepted_by_job_title")
    private String acceptedByJobTitle;

    @Column(name = "accepted_on")
    private OffsetDateTime acceptedOn;

    @Column(name = "variation")
    private boolean variation = false;

    @Column(name = "variation_name")
    private String variationName;

    @Column(name = "variation_reason")
    private String variationReason;

    @Transient
    private String withdrawReason;

    @Column(name = "organisation_id", insertable=false, updatable=false)
    private Integer organisationId;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "organisation_contract_attachment",
        joinColumns = @JoinColumn(name = "organisation_contract_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "attachment_id", referencedColumnName = "id"))
    private Set<AttachmentFile> contractFiles = new HashSet<>();

    public OrganisationContract() {}

    public OrganisationContract(ContractModel contractModel, OrganisationContractStatus status,
                                String orgGroupType) {
        this.contractModel = contractModel;
        this.contractId = contractModel.getId();
        this.status = status;
        this.orgGroupType = orgGroupType != null ? OrganisationGroupType.valueOf(orgGroupType): null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ContractModel getContract() {
        return contractModel;
    }

    public void setContract(ContractModel contractModel) {
        this.contractModel = contractModel;
    }

    public Integer getContractId() {
        return contractId;
    }

    public void setContractId(Integer contractId) {
        this.contractId = contractId;
    }

    public OrganisationContractStatus getStatus() {
        return status;
    }

    public void setStatus(OrganisationContractStatus status) {
        this.status = status;
    }

    public OrganisationGroupType getOrgGroupType() {
        return orgGroupType;
    }

    public void setOrgGroupType(OrganisationGroupType orgGroupType) {
        this.orgGroupType = orgGroupType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public String getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public String getAcceptedByJobTitle() {
        return acceptedByJobTitle;
    }

    public void setAcceptedByJobTitle(String acceptedByJobTitle) {
        this.acceptedByJobTitle = acceptedByJobTitle;
    }

    public OffsetDateTime getAcceptedOn() {
        return acceptedOn;
    }

    public void setAcceptedOn(OffsetDateTime acceptedOn) {
        this.acceptedOn = acceptedOn;
    }

    public String getVariationName() {
        return variationName;
    }

    public void setVariationName(String variationName) {
        this.variationName = variationName;
    }

    public String getVariationReason() {
        return variationReason;
    }

    public void setVariationReason(String variationReason) {
        this.variationReason = variationReason;
    }

    public boolean isVariation() {
        return variation;
    }

    public void setVariation(boolean variation) {
        this.variation = variation;
    }

    public String getWithdrawReason() {
        return withdrawReason;
    }

    public void setWithdrawReason(String withdrawReason) {
        this.withdrawReason = withdrawReason;
    }

    public boolean matches(ContractModel contractModel, OrganisationGroupType orgGroupType) {
        return this.contractModel.equals(contractModel) && Objects.equals(this.orgGroupType, orgGroupType);
    }

    public boolean isPendingSignature() {
        return status == null || status.equals(OrganisationContractStatus.Blank);
    }

    public Set<AttachmentFile> getContractFiles() {
        return contractFiles;
    }

    public void setContractFiles(Set<AttachmentFile> contractFiles) {
        this.contractFiles = contractFiles;
    }

    public Integer getOrganisationId() {
        return organisationId;
    }

    public long getTotalAttachmentsSize() {
        if (contractFiles != null) {
            return contractFiles.stream().map(AttachmentFile::getFileSize).reduce(0L, GlaUtils::nullSafeAdd);
        } else {
            return 0L;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrganisationContract that = (OrganisationContract) o;
        return Objects.equals(id, that.id)
                && Objects.equals(contractModel, that.contractModel)
                && orgGroupType == that.orgGroupType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, contractModel, orgGroupType);
    }

}
