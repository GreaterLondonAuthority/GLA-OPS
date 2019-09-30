/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Map;

/**
 * Created by chris on 27/06/2017.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StateModel {
    AutoApproval("Draft", null, false, false, false, true, true),

    ChangeControlled("Draft", null, true, true, true, false, false),

    MultiAssessment("Stage 1", "Draft", true, true, true, false, false);

    String initialStatus;
    String initialSubStatus;

    /** If GLA approval is needed for modifications to this project */
    boolean approvalRequired;

    /** If report version is restricted to last approved block only */
    boolean reportOnLastApproved;

    /** if milestone forecast / actual status is automatically calculated */
    boolean automaticallyCalculateMilestoneState;

    /** If the closure of this project is permitted if blocks are unapproved */
    boolean allowClosureWithUnapprovedBlocks;

    /** If the closure of this project is if mandatory milestones are unapproved */
    boolean allowClosureWithUnapprovedMandatoryMilestones;

    StateModel(String initialStatus, String initialSubStatus, boolean approvalRequired, boolean reportOnLastApproved, boolean automaticallyCalculateMilestoneState, boolean allowClosureWithUnapprovedBlocks, boolean allowClosureWithUnapprovedMandatoryMilestones) {
        this.initialStatus = initialStatus;
        this.initialSubStatus = initialSubStatus;
        this.approvalRequired = approvalRequired;
        this.reportOnLastApproved = reportOnLastApproved;
        this.automaticallyCalculateMilestoneState = automaticallyCalculateMilestoneState;
        this.allowClosureWithUnapprovedBlocks = allowClosureWithUnapprovedBlocks;
        this.allowClosureWithUnapprovedMandatoryMilestones = allowClosureWithUnapprovedMandatoryMilestones;
    }

    @JsonCreator
    public static StateModel fromObject(Object obj) {
        if (obj instanceof Map) {
            return StateModel.valueOf(String.valueOf(((Map)obj).get("name")));
        }
        if (obj instanceof String) {
            return StateModel.valueOf(String.valueOf(obj));
        }
        return null;
    }

    public String getInitialStatus() {
        return initialStatus;
    }

    public String getInitialSubStatus() {
        return initialSubStatus;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public boolean isReportOnLastApproved() {
        return reportOnLastApproved;
    }

    public boolean isAutomaticallyCalculateMilestoneState() {
        return automaticallyCalculateMilestoneState;
    }

    public boolean isAllowClosureWithUnapprovedBlocks() {
        return allowClosureWithUnapprovedBlocks;
    }

    public boolean isAllowClosureWithUnapprovedMandatoryMilestones() {
        return allowClosureWithUnapprovedMandatoryMilestones;
    }

    public String getName() {
        return this.name();
    }

}
