/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.contracts;

import uk.gov.london.ops.framework.enums.ContractWorkflowType;

public class ContractModel {

    private Integer id;

    private String name;

    private ContractWorkflowType contractWorkflowType = ContractWorkflowType.SIGNED_TO_AUTHORISE_PAYMENTS;

    public ContractModel() {
    }

    public ContractModel(String name) {
        this.name = name;
    }

    public ContractModel(Integer id, String name, ContractWorkflowType contractWorkflowType) {
        this.id = id;
        this.name = name;
        this.contractWorkflowType = contractWorkflowType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContractWorkflowType getContractWorkflowType() {
        return contractWorkflowType;
    }

    public void setContractWorkflowType(ContractWorkflowType contractWorkflowType) {
        this.contractWorkflowType = contractWorkflowType;
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (id != null) {
            result = 31 * result + id.hashCode();
        }
        if (name != null) {
            result = 31 * result + name.hashCode();
        }
        if (contractWorkflowType != null) {
            result = 31 * result + contractWorkflowType.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (! (obj instanceof ContractModel)) {
            return false;
        }
        ContractModel contractModel = (ContractModel) obj;
        return contractModel.id != null && contractModel.id.equals(this.id)
                && contractModel.name != null && contractModel.name.equals(this.name)
                && contractModel.contractWorkflowType != null && contractModel.contractWorkflowType.equals(this.contractWorkflowType);
    }
}
