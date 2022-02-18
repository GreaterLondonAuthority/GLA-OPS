/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.contracts;

import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.framework.enums.ContractWorkflowType;
import uk.gov.london.ops.framework.enums.OrganisationContractStatus;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ContractSummary {
    private Integer id;
    private Integer contractId;
    private String name;
    private OrganisationContractStatus status;
    private String orgGroupType;
    private ContractWorkflowType contractWorkflowType;
    private List<ContractActionDetails> availableActions;
    private Set<AttachmentFile> contractFiles = new HashSet<>();
    private OffsetDateTime acceptedOn;
    private String acceptedBy;
    private String acceptedByFullName;
    private String acceptedByJobTitle;
    private String withdrawReason;
    private String variationName;
    private String variationReason;
    private boolean variation;

    public ContractSummary() {}

    public ContractSummary(String name) {
        this.name = name;
    }

    public ContractSummary(String name, String orgGroupType) {
        this.name = name;
        this.orgGroupType = orgGroupType;
    }

    public ContractSummary(Integer contractId, OrganisationContractStatus status) {
        this.contractId = contractId;
        this.status = status;
    }

    public ContractSummary(Integer id, Integer contractId, String name, OrganisationContractStatus status,
                           String orgGroupType, ContractWorkflowType contractWorkflowType) {
        this.id = id;
        this.contractId = contractId;
        this.name = name;
        this.status = status;
        this.orgGroupType = orgGroupType;
        this.contractWorkflowType = contractWorkflowType;
    }

    public ContractSummary(Integer id, Integer contractId, String name, OrganisationContractStatus status,
                           String orgGroupType, ContractWorkflowType contractWorkflowType,
                           List<ContractActionDetails> availableActions, OffsetDateTime acceptedOn, String acceptedBy,
                           String acceptedByFullName, String acceptedByJobTitle, String variationName, String variationReason,
                           boolean variation) {
        this.id = id;
        this.contractId = contractId;
        this.name = name;
        this.status = status;
        this.orgGroupType = orgGroupType;
        this.contractWorkflowType = contractWorkflowType;
        this.availableActions = availableActions;
        this.acceptedOn = acceptedOn;
        this.acceptedBy = acceptedBy;
        this.acceptedByFullName = acceptedByFullName;
        this.acceptedByJobTitle = acceptedByJobTitle;
        this.variationName = variationName;
        this.variationReason = variationReason;
        this.variation = variation;
    }

    public ContractSummary(Integer id, Integer contractId, String name, OrganisationContractStatus status,
                           String orgGroupType, ContractWorkflowType contractWorkflowType,
                           List<ContractActionDetails> availableActions, Set<AttachmentFile> contractFiles) {
        this.id = id;
        this.contractId = contractId;
        this.name = name;
        this.status = status;
        this.orgGroupType = orgGroupType;
        this.contractWorkflowType = contractWorkflowType;
        this.availableActions = availableActions;
        this.contractFiles = contractFiles;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getContractId() {
        return contractId;
    }

    public void setContractId(Integer contractId) {
        this.contractId = contractId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrganisationContractStatus getStatus() {
        return status;
    }

    public void setStatus(OrganisationContractStatus status) {
        this.status = status;
    }

    public String getOrgGroupType() {
        return orgGroupType;
    }

    public void setOrgGroupType(String orgGroupType) {
        this.orgGroupType = orgGroupType;
    }

    public ContractWorkflowType getContractWorkflowType() {
        return contractWorkflowType;
    }

    public void setContractWorkflowType(ContractWorkflowType contractWorkflowType) {
        this.contractWorkflowType = contractWorkflowType;
    }

    public OffsetDateTime getAcceptedOn() {
        return acceptedOn;
    }

    public void setAcceptedOn(OffsetDateTime acceptedOn) {
        this.acceptedOn = acceptedOn;
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

    public String getAcceptedByFullName() {
        return acceptedByFullName;
    }

    public void setAcceptedByFullName(String acceptedByFullName) {
        this.acceptedByFullName = acceptedByFullName;
    }

    public String getWithdrawReason() {
        return withdrawReason;
    }

    public void setWithdrawReason(String withdrawReason) {
        this.withdrawReason = withdrawReason;
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

    public List<ContractActionDetails> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<ContractActionDetails> availableActions) {
        this.availableActions = availableActions;
    }

    public Set<AttachmentFile> getContractFiles() {
        return contractFiles;
    }

    public void setContractFiles(Set<AttachmentFile> contractFiles) {
        this.contractFiles = contractFiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContractSummary that = (ContractSummary) o;

        return Objects.equals(name, that.name) &&
                Objects.equals(orgGroupType, that.orgGroupType) &&
                Objects.equals(variationName, that.variationName);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((orgGroupType == null) ? 0 : orgGroupType.hashCode());
        result = prime * result + ((variationName == null) ? 0 : variationName.hashCode());
        return result;
    }
}
