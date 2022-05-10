/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.contracts;

import uk.gov.london.ops.framework.enums.ContractWorkflowType;
import uk.gov.london.ops.framework.enums.OrganisationContractStatus;

import java.util.List;

public class ContractActionRule {
    private ContractAction contractAction;
    private ContractWorkflowType contractWorkflowType;
    private boolean variation;
    private OrganisationContractStatus contractStatus;
    private String projectStatus;
    private List<String> userRoles;
    private String permissions;

    public ContractActionRule() {
    }

    public ContractActionRule(ContractAction contractAction,
                              ContractWorkflowType contractWorkflowType,
                              boolean variation,
                              OrganisationContractStatus contractStatus, String projectStatus,
                              List<String> userRoles, String permissions) {
        this.contractAction = contractAction;
        this.contractWorkflowType = contractWorkflowType;
        this.variation = variation;
        this.contractStatus = contractStatus;
        this.projectStatus = projectStatus;
        this.userRoles = userRoles;
        this.permissions = permissions;
    }

    public ContractAction getContractAction() {
        return contractAction;
    }

    public void setContractAction(ContractAction contractAction) {
        this.contractAction = contractAction;
    }

    public OrganisationContractStatus getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(OrganisationContractStatus contractStatus) {
        this.contractStatus = contractStatus;
    }

    public List<String> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<String> userRoles) {
        this.userRoles = userRoles;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public ContractWorkflowType getContractWorkflowType() {
        return contractWorkflowType;
    }

    public void setContractWorkflowType(ContractWorkflowType contractWorkflowType) {
        this.contractWorkflowType = contractWorkflowType;
    }
    public boolean isVariation() {
        return variation;
    }

    public void setVariation(boolean variation) {
        this.variation = variation;
    }

    public String getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(String projectStatus) {
        this.projectStatus = projectStatus;
    }
}
