/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project.state;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static uk.gov.london.ops.service.project.state.ProjectStateMachine.ALL_MATCH;
import static uk.gov.london.ops.service.project.state.ProjectStateMachine.ANY_MATCH;

public class StateTransition {

    private ProjectState from;
    private ProjectState to;
    private List<String> roles;
    private String programmeStatus;
    private String projectCompletion;
    private String approvalWillGeneratePayments;

    public StateTransition() {}

    public StateTransition(ProjectState from, ProjectState to, List<String> roles, String programmeStatus,
                           String projectCompletion, String approvalWillGeneratePayments) {
        this.from = from;
        this.to = to;
        this.roles = roles;
        this.programmeStatus = programmeStatus;
        this.projectCompletion = projectCompletion;
        this.approvalWillGeneratePayments = approvalWillGeneratePayments;
    }

    public StateTransition(ProjectState from, ProjectState to, String[] roles, String programmeStatus,
                           String projectCompletion, String approvalWillGeneratePayments) {
        this(from, to, Arrays.asList(roles), programmeStatus, projectCompletion, approvalWillGeneratePayments);
    }

    public ProjectState getFrom() {
        return from;
    }

    public void setFrom(ProjectState from) {
        this.from = from;
    }

    public boolean isFrom(ProjectState state) {
        return (state != null) && state.equals(this.from);
    }

    public ProjectState getTo() {
        return to;
    }

    public void setTo(ProjectState to) {
        this.to = to;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getProgrammeStatus() {
        return programmeStatus;
    }

    public void setProgrammeStatus(String programmeStatus) {
        this.programmeStatus = programmeStatus;
    }

    public boolean matchesProgrammeStatus(boolean programmeIsOpen) {
        return ALL_MATCH.equals(programmeStatus) || ("OPEN".equals(programmeStatus) && programmeIsOpen);
    }

    public String getProjectCompletion() {
        return projectCompletion;
    }

    public void setProjectCompletion(String projectCompletion) {
        this.projectCompletion = projectCompletion;
    }

    public boolean matchesProjectCompletion(boolean allBlocksComplete) {
        return ANY_MATCH.equals(projectCompletion) || ("COMPLETE".equals(projectCompletion) && allBlocksComplete);
    }

    public String getApprovalWillGeneratePayments() {
        return approvalWillGeneratePayments;
    }

    public void setApprovalWillGeneratePayments(String approvalWillGeneratePayments) {
        this.approvalWillGeneratePayments = approvalWillGeneratePayments;
    }

    public boolean matchesPaymentGenerationStatus(Boolean willGeneratePayments) {
        return ANY_MATCH.equals(approvalWillGeneratePayments) ||
                willGeneratePayments.equals(Boolean.parseBoolean(approvalWillGeneratePayments));
    }

    /**
     * Returns true if any of the specified roles are in the list of allowed roles for this transition.
     */
    public boolean matchesRoles(Collection<String> rolesToMatch) {
        return rolesToMatch.stream().anyMatch(roles::contains);
    }

    /**
     * Returns true if the transition requires comments and they are not provided.
     *
     * If it is not known whether comments are provided, pass null, in which case this returns true.
     */
    public boolean matchesCommentRequirement(Boolean commentsProvided) {
        if (commentsProvided == null) {
            return true;
        }
        return (!to.isCommentsRequired()) || commentsProvided.booleanValue();
    }
}
