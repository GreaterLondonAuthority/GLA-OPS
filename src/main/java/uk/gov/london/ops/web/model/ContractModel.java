/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model;

import uk.gov.london.ops.domain.organisation.OrganisationContract;
import uk.gov.london.ops.domain.organisation.OrganisationGroup;

public class ContractModel {

    private Integer id;
    private Integer contractId;
    private String name;
    private OrganisationContract.Status status;
    private OrganisationGroup.Type orgGroupType;

    public ContractModel() {}

    public ContractModel(String name) {
        this.name = name;
    }

    public ContractModel(String name, OrganisationGroup.Type orgGroupType) {
        this.name = name;
        this.orgGroupType = orgGroupType;
    }

    public ContractModel(Integer contractId, OrganisationContract.Status status) {
        this.contractId = contractId;
        this.status = status;
    }

    public ContractModel(Integer id, Integer contractId, String name, OrganisationContract.Status status, OrganisationGroup.Type orgGroupType) {
        this.id = id;
        this.contractId = contractId;
        this.name = name;
        this.status = status;
        this.orgGroupType = orgGroupType;
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

    public OrganisationContract.Status getStatus() {
        return status;
    }

    public void setStatus(OrganisationContract.Status status) {
        this.status = status;
    }

    public OrganisationGroup.Type getOrgGroupType() {
        return orgGroupType;
    }

    public void setOrgGroupType(OrganisationGroup.Type orgGroupType) {
        this.orgGroupType = orgGroupType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContractModel that = (ContractModel) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return orgGroupType == that.orgGroupType;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (orgGroupType != null ? orgGroupType.hashCode() : 0);
        return result;
    }

}
