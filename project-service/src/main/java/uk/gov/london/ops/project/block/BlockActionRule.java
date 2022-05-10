/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block;

import uk.gov.london.ops.project.state.ProjectStatus;

import java.util.List;

public class BlockActionRule {

    private ProjectBlockAction action;
    private ProjectStatus status;
    private String subStatus;
    private String blockType;
    private String blockStatus;
    private String paymentsOnlyLifecycle;
    private String blockCompletion;
    private List<String> userRoles;
    private String permissions;

    public BlockActionRule(ProjectBlockAction action, ProjectStatus status, String subStatus, String blockType,
            String blockStatus, String blockCompletion, String paymentsOnlyLifecycle, List<String> userRoles, String permissions) {
        this.action = action;
        this.status = status;
        this.subStatus = subStatus;
        this.blockType = blockType;
        this.blockStatus = blockStatus;
        this.paymentsOnlyLifecycle = paymentsOnlyLifecycle;
        this.blockCompletion = blockCompletion;
        this.userRoles = userRoles;
        this.permissions = permissions;
    }

    public ProjectBlockAction getAction() {
        return action;
    }

    public void setAction(ProjectBlockAction action) {
        this.action = action;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(String subStatus) {
        this.subStatus = subStatus;
    }

    public String getBlockType() {
        return blockType;
    }

    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public String getBlockStatus() {
        return blockStatus;
    }

    public void setBlockStatus(String blockStatus) {
        this.blockStatus = blockStatus;
    }

    public String getBlockCompletion() {
        return blockCompletion;
    }

    public void setBlockCompletion(String blockCompletion) {
        this.blockCompletion = blockCompletion;
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

    public String getPaymentsOnlyLifecycle() {
        return paymentsOnlyLifecycle;
    }

    public void setPaymentsOnlyLifecycle(String paymentsOnlyLifecycle) {
        this.paymentsOnlyLifecycle = paymentsOnlyLifecycle;
    }
}
