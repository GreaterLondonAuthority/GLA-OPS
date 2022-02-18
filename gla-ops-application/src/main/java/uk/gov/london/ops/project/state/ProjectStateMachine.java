/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.state;

import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectTransition;
import uk.gov.london.ops.project.block.NamedProjectBlock;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.london.ops.project.ProjectTransition.*;
import static uk.gov.london.ops.project.block.ProjectBlockStatus.UNAPPROVED;
import static uk.gov.london.ops.project.state.ProjectStatus.Active;

/**
 * State machine for GLA OPS projects.
 */
public abstract class ProjectStateMachine {

    private static final String LIST_DELIMITER = " \\| ";
    static final String ALL_MATCH = "ALL";
    static final String OPEN = "OPEN";
    static final String NOT_IN_ASSESS = "NOT_IN_ASSESS";
    static final String ANY_MATCH = "ANY";
    private static final Boolean UNKNOWN = null;
    /**
     * Maps rows from the CSV file into StateTransition objects.
     */
    final CSVFile.CSVMapper<StateTransition> csvMapper = csvRow -> new StateTransition(
            ProjectState.parse(csvRow.getString("STATUS_FROM")),
            ProjectState
                    .parse(csvRow.getString("STATUS_TO"), csvRow.getString("COMMENTS_REQUIRED"), csvRow.getString("ACTION_NAME")),
            csvRow.getString("ROLES").split(LIST_DELIMITER),
            csvRow.getString("PROGRAMME_STATE").split(LIST_DELIMITER),
            csvRow.getString("PROJECT_COMPLETION"),
            csvRow.getString("TRANSITION_TYPE"),
            csvRow.getString("APPROVAL_WILL_CREATE_PAYMENTS"),
            csvRow.getString("PROJECT_HISTORY_TRANSITION"),
            csvRow.getString("PROJECT_HISTORY_DESCRIPTION"),
            csvRow.getString("ACTION_NAME"),
            csvRow.getString("NOTIFICATION_KEY"),
            Boolean.parseBoolean(csvRow.getString("CLEAR_NEW_LABEL"))
    );
    List<StateTransition> allowedTransitions;

    @PostConstruct
    abstract void loadAllowedTransitions() throws IOException;

    /**
     * Returns the set of all state transitions allowed by the specified user from the specified project state.
     *
     * This version of this method is for situations when it is not known whether comments will be provided.
     */
    public Set<ProjectState> getAllowedTransitionsFor(ProjectState currentState, Set<String> userRoles, boolean programmeEnabled,
            boolean programmeInAssessment, boolean isProjectSubmitted,
            boolean projectComplete, boolean approvalWillCreatePendingPayment) {
        return getAllowedTransitionsFor(currentState, userRoles, programmeEnabled, programmeInAssessment, isProjectSubmitted,
                UNKNOWN, projectComplete, approvalWillCreatePendingPayment)
                .stream().map(StateTransition::getTo).collect(Collectors.toSet());
    }

    /**
     * Returns the set of all state transitions allowed by the specified user from the specified project state.
     *
     * This version of this method is for situations when it is known whether comments have been provided.
     */
    public Set<StateTransition> getAllowedTransitionsFor(ProjectState currentState, Set<String> userRoles,
            boolean programmeEnabled,
            boolean programmeInAssessment, boolean previouslySubmitted,
            Boolean commentsProvided, boolean projectComplete, boolean approvalWillCreatePendingPayment) {
        return allowedTransitions.stream()
                .filter(transition -> transition.isFrom(currentState))
                .filter(transition -> transition.matchesRoles(userRoles))
                .filter(transition -> transition
                        .matchesProgrammeStatus(programmeEnabled, programmeInAssessment, previouslySubmitted))
                .filter(transition -> transition.matchesProjectCompletion(projectComplete))
                .filter(transition -> transition.matchesPaymentGenerationStatus(approvalWillCreatePendingPayment))
                .filter(transition -> transition.matchesCommentRequirement(commentsProvided))
                .collect(Collectors.toSet());
    }

    public StateTransition getTransition(ProjectState currentState, ProjectState targetState, Set<String> userRoles,
            boolean programmeEnabled,
            boolean programmeInAssessment, boolean previouslySubmitted, Boolean commentsProvided,
            boolean projectComplete, boolean approvalWillCreatePendingPayment) {
        return allowedTransitions.stream()
                .filter(transition -> transition.isFrom(currentState))
                .filter(transition -> transition.isTo(targetState))
                .filter(transition -> transition.matchesRoles(userRoles))
                .filter(transition -> transition
                        .matchesProgrammeStatus(programmeEnabled, programmeInAssessment, previouslySubmitted))
                .filter(transition -> transition.matchesProjectCompletion(projectComplete))
                .filter(transition -> transition.matchesPaymentGenerationStatus(approvalWillCreatePendingPayment))
                .filter(transition -> transition.matchesCommentRequirement(commentsProvided))
                .findFirst().orElse(null);
    }

    public StateTransition getTransition(ProjectState currentState, StateTransitionType transitionType) {
        return allowedTransitions.stream()
                .filter(transition -> transition.isFrom(currentState))
                .filter(transition -> transitionType.equals(transition.getTransitionType()))
                .findFirst().orElse(null);
    }

    public ProjectTransition getProjectHistoryTransition(ProjectState current, ProjectState target) {
        if (ProjectStatus.Draft.equals(target.getStatusType()) && ProjectStatus.Submitted.equals(current.getStatusType())) {
            return Withdrawn;
        }
        if (ProjectStatus.Returned.equals(target.getStatusType())) {
            return Returned;
        }
        if (ProjectStatus.Closed.equals(target.getStatusType())) {
            if (ProjectSubStatus.Abandoned.equals(target.getSubStatusType())) {
                return Abandoned;
            } else if (ProjectSubStatus.Completed.equals(target.getSubStatusType())) {
                return Completed;
            } else {
                return Closed;
            }
        }

        if (ProjectStatus.Submitted.equals(target.getStatusType()) && ProjectStatus.Assess.equals(current.getStatusType())) {
            return null;
        }
        if (ProjectStatus.Submitted.equals(target.getStatusType())) {
            return Submitted;
        }

        if (ProjectStatus.Assess.equals(target.getStatusType()) && ProjectStatus.Submitted.equals(current.getStatusType())) {
            return Assess;
        }
        if (ProjectStatus.Assess.equals(target.getStatusType()) && ProjectStatus.Returned.equals(current.getStatusType())) {
            return Resubmitted;
        }
        if (ProjectStatus.Assess.equals(target.getStatusType())) {
            // Should never see this
            throw new RuntimeException("Illegal state transition: " + current.getStatus() + " to Assess");
        }

        if (ProjectStatus.Active.equals(target.getStatusType()) && ProjectStatus.Active.equals(current.getStatusType())) {
            if (ProjectSubStatus.ApprovalRequested.equals(target.getSubStatusType()) && ProjectSubStatus.UnapprovedChanges
                    .equals(current.getSubStatusType())) {
                return ApprovalRequested;
            }
            if (ProjectSubStatus.UnapprovedChanges.equals(target.getSubStatusType()) && ProjectSubStatus.ApprovalRequested
                    .equals(current.getSubStatusType())) {
                return Returned;
            }
            if (ProjectSubStatus.PaymentAuthorisationPending.equals(target.getSubStatusType())
                    && ProjectSubStatus.ApprovalRequested.equals(current.getSubStatusType())) {
                return PaymentAuthorisationRequested;
            }
            if (target.getSubStatus() == null && ProjectSubStatus.AbandonPending.equals(current.getSubStatusType())) {
                return AbandonRejected;
            }
        }

        if (ProjectStatus.Active.equals(target.getStatusType()) && target.getSubStatus() == null) {
            return Approved;
        }

        if (ProjectSubStatus.AbandonPending.equals(target.getSubStatusType())) {
            return AbandonRequested;
        }

        return null;
    }

    public String getProjectHistoryDescription(Project project, ProjectTransition historyTransition) {
        String historyDescription = null;
        if (ProjectTransition.ApprovalRequested.equals(historyTransition)) {
            historyDescription = "Approval requested for unapproved blocks " + unapprovedBlockDisplayNames(project);
        }
        if (ProjectTransition.Returned.equals(historyTransition) && project.getStatusType().equals(Active)) {
            historyDescription = "Returned to organisation";
        }
        if (ProjectTransition.Approved.equals(historyTransition) && !project.getStateModel().isApprovalRequired()) {
            historyDescription = "Project saved to active";
        }
        return historyDescription;
    }

    private String unapprovedBlockDisplayNames(Project project) {
        return project.getLatestProjectBlocks()
                .stream()
                .filter(b -> UNAPPROVED.equals(b.getBlockStatus()))
                .map(NamedProjectBlock::getBlockDisplayName)
                .collect(Collectors.joining(","));
    }

}
