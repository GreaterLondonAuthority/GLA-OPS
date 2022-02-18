/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.state;

import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.project.ProjectTransition;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static uk.gov.london.ops.project.state.ProjectStateMachine.*;
import static uk.gov.london.ops.project.state.ProjectStatus.Submitted;

public class StateTransition {

    private ProjectState from;
    private ProjectState to;
    private List<String> roles;
    private List<String> programmeStatuses;
    private String projectCompletion;
    private StateTransitionType transitionType;
    private String approvalWillGeneratePayments;
    private ProjectTransition projectHistoryTransition;
    private String projectHistoryDescription;
    private String actionName;
    private String notifcationKey;
    private boolean clearNewLabel;

    public StateTransition() {
    }

    public StateTransition(ProjectState from,
            ProjectState to,
            List<String> roles,
            List<String> programmeStatuses,
            String projectCompletion,
            String transitionType,
            String approvalWillGeneratePayments,
            String projectHistoryTransition,
            String projectHistoryDescription,
            String actionName,
            String notificationKey,
            boolean clearNewLabel) {
        this.from = from;
        this.to = to;
        this.roles = roles;
        this.programmeStatuses = programmeStatuses;
        this.projectCompletion = projectCompletion;
        this.transitionType = StringUtils.isNotEmpty(transitionType) ? StateTransitionType.valueOf(transitionType) : null;
        this.approvalWillGeneratePayments = approvalWillGeneratePayments;
        this.projectHistoryTransition =
                StringUtils.isNotEmpty(projectHistoryTransition) ? ProjectTransition.valueOf(projectHistoryTransition)
                        : null;
        this.projectHistoryDescription = projectHistoryDescription;
        this.actionName = actionName;
        this.notifcationKey = notificationKey;
        this.clearNewLabel = clearNewLabel;
    }

    public StateTransition(ProjectState from,
            ProjectState to,
            String[] roles,
            String[] programmeStatuses,
            String projectCompletion,
            String transitionType,
            String approvalWillGeneratePayments,
            String projectHistoryTransition,
            String projectHistoryDescription,
            String actionName,
            String notificationKey,
            boolean clearNewLabel) {
        this(from, to, Arrays.asList(roles), Arrays.asList(programmeStatuses), projectCompletion, transitionType,
                approvalWillGeneratePayments, projectHistoryTransition, projectHistoryDescription, actionName, notificationKey,
                clearNewLabel);
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

    public boolean isTo(ProjectState state) {
        return (state != null) && state.equals(this.to);
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getProgrammeStatuses() {
        return programmeStatuses;
    }

    public void setProgrammeStatuses(List<String> programmeStatuses) {
        this.programmeStatuses = programmeStatuses;
    }

    public boolean matchesProgrammeStatus(boolean programmeIsOpen, boolean programmeInAssessment, boolean previouslySubmitted) {

        if (programmeStatuses.contains(ALL_MATCH)) {
            return true;
        }

        if (to.getStatusType().equals(Submitted) && previouslySubmitted) {
            return true;
        }

        if (!programmeIsOpen && programmeStatuses.contains(OPEN)) {
            return false;
        }

        return !programmeInAssessment || !programmeStatuses.contains(NOT_IN_ASSESS);
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

    public StateTransitionType getTransitionType() {
        return transitionType;
    }

    public void setTransitionType(StateTransitionType transitionType) {
        this.transitionType = transitionType;
    }

    public String getApprovalWillGeneratePayments() {
        return approvalWillGeneratePayments;
    }

    public void setApprovalWillGeneratePayments(String approvalWillGeneratePayments) {
        this.approvalWillGeneratePayments = approvalWillGeneratePayments;
    }

    public ProjectTransition getProjectHistoryTransition() {
        return projectHistoryTransition;
    }

    public void setProjectHistoryTransition(ProjectTransition projectHistoryTransition) {
        this.projectHistoryTransition = projectHistoryTransition;
    }

    public String getProjectHistoryDescription() {
        return projectHistoryDescription;
    }

    public void setProjectHistoryDescription(String projectHistoryDescription) {
        this.projectHistoryDescription = projectHistoryDescription;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getNotifcationKey() {
        return notifcationKey;
    }

    public void setNotifcationKey(String notifcationKey) {
        this.notifcationKey = notifcationKey;
    }

    public boolean isClearNewLabel() {
        return clearNewLabel;
    }

    public void setClearNewLabel(boolean clearNewLabel) {
        this.clearNewLabel = clearNewLabel;
    }

    public boolean matchesPaymentGenerationStatus(Boolean willGeneratePayments) {
        return ANY_MATCH.equals(approvalWillGeneratePayments)
                || willGeneratePayments.equals(Boolean.parseBoolean(approvalWillGeneratePayments));
    }

    /**
     * Returns true if any of the specified roles are in the list of allowed roles for this transition.
     */
    public boolean matchesRoles(Collection<String> rolesToMatch) {
        return rolesToMatch != null && rolesToMatch.stream().anyMatch(roles::contains);
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
