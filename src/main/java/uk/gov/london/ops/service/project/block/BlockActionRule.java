/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project.block;

import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.Project;

import java.util.List;

public class BlockActionRule {

    private NamedProjectBlock.Action action;
    private Project.Status status;
    private String subStatus;
    private String blockType;
    private String blockStatus;
    private String blockCompletion;
    private List<String> userRoles;
    private String programmeState;
    private String permissions;

    public BlockActionRule(NamedProjectBlock.Action action, Project.Status status, String subStatus, String blockType, String blockStatus, String blockCompletion, List<String> userRoles, String programmeState, String permissions) {
        this.action = action;
        this.status = status;
        this.subStatus = subStatus;
        this.blockType = blockType;
        this.blockStatus = blockStatus;
        this.blockCompletion = blockCompletion;
        this.userRoles = userRoles;
        this.programmeState = programmeState;
        this.permissions = permissions;
    }

    public NamedProjectBlock.Action getAction() {
        return action;
    }

    public void setAction(NamedProjectBlock.Action action) {
        this.action = action;
    }

    public Project.Status getStatus() {
        return status;
    }

    public void setStatus(Project.Status status) {
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

    public String getProgrammeState() {
        return programmeState;
    }

    public void setProgrammeState(String programmeState) {
        this.programmeState = programmeState;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

}
