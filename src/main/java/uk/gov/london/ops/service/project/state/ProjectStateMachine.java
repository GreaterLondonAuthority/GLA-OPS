/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project.state;

import uk.gov.london.ops.aop.LogMetrics;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectHistory;
import uk.gov.london.ops.util.CSVFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.london.ops.domain.project.ProjectHistory.Transition.*;

/**
 * State machine for GLA OPS projects.
 */
public abstract class ProjectStateMachine {
    static final String ROLE_DELIMITER = " \\| ";
    static final String ALL_MATCH = "ALL";
    static final String ANY_MATCH = "ANY";
    private static final Boolean UNKNOWN = null;
    /**
     * Maps rows from the CSV file into StateTransition objects.
     */
    final CSVFile.CSVMapper<StateTransition> csvMapper = csvRow -> new StateTransition(
            ProjectState.parse(csvRow.getString("STATUS_FROM")),
            ProjectState.parse(csvRow.getString("STATUS_TO"),
                    csvRow.getString("COMMENTS_REQUIRED")),
            csvRow.getString("ROLES").split(ROLE_DELIMITER),
            csvRow.getString("PROGRAMME_STATE"),
            csvRow.getString("PROJECT_COMPLETION"),
            csvRow.getString("APPROVAL_WILL_CREATE_PAYMENTS")
    );
    List<StateTransition> allowedTransitions;

    @PostConstruct
    abstract void loadAllowedTransitions() throws IOException;

    /**
     * Returns the set of all state transitions allowed by the specified user from the specified project state.
     *
     * This version of this method is for situations when it is not known whether comments will be provided.
     */
    @LogMetrics
    public Set<ProjectState> getAllowedTransitionsFor(ProjectState currentState, Set<String> userRoles, boolean programmeEnabled,
                                                      boolean projectComplete, boolean approvalWillCreatePendingPayment) {
        return getAllowedTransitionsFor(currentState, userRoles, programmeEnabled, UNKNOWN, projectComplete, approvalWillCreatePendingPayment);
    }

    /**
     * Returns the set of all state transitions allowed by the specified user from the specified project state.
     *
     * This version of this method is for situations when it is known whether comments have been provided.
     */
    public Set<ProjectState> getAllowedTransitionsFor( ProjectState currentState, Set<String> userRoles, boolean programmeEnabled,
                                               Boolean commentsProvided, boolean projectComplete, boolean approvalWillCreatePendingPayment) {
        return allowedTransitions.stream()
                .filter(transition -> transition.isFrom(currentState))
                .filter(transition -> transition.matchesRoles(userRoles))
                .filter(transition -> transition.matchesProgrammeStatus(programmeEnabled))
                .filter(transition -> transition.matchesProjectCompletion(projectComplete))
                .filter(transition -> transition.matchesPaymentGenerationStatus(approvalWillCreatePendingPayment))
                .filter(transition -> transition.matchesCommentRequirement(commentsProvided))
                .map(StateTransition::getTo)
                .collect(Collectors.toSet());
    }

    public ProjectHistory.Transition transitionFor(ProjectState current, ProjectState target) {
        if (target.getStatus().equals(Project.Status.Draft)) {
            return Withdrawn;
        }
        if (target.getStatus().equals(Project.Status.Returned)) {
            return Returned;
        }
        if (target.getStatus().equals(Project.Status.Closed)) {
            if (Project.SubStatus.Abandoned.equals(target.getSubStatus())) {
                return Abandoned;
            } else if (Project.SubStatus.Completed.equals(target.getSubStatus())) {
                return Completed;
            }
            else {
                return Closed;
            }
        }

        if (target.getStatus().equals(Project.Status.Submitted) && current.getStatus().equals(Project.Status.Assess)) {
            return null;
        }
        if (target.getStatus().equals(Project.Status.Submitted)) {
            return Submitted;
        }

        if (target.getStatus().equals(Project.Status.Assess) && current.getStatus().equals(Project.Status.Submitted)) {
            return Assess;
        }
        if (target.getStatus().equals(Project.Status.Assess) && current.getStatus().equals(Project.Status.Returned)) {
            return Resubmitted;
        }
        if (target.getStatus().equals(Project.Status.Assess)) {
            // Should never see this
            throw new RuntimeException("Illegal state transition: " + current.getStatus() + " to Assess");
        }

        if (target.getStatus().equals(Project.Status.Active) && current.getStatus().equals(Project.Status.Active)) {
            if (Project.SubStatus.ApprovalRequested.equals(target.getSubStatus()) && Project.SubStatus.UnapprovedChanges.equals(current.getSubStatus())) {
                return ApprovalRequested;
            }
            if (Project.SubStatus.UnapprovedChanges.equals(target.getSubStatus()) && Project.SubStatus.ApprovalRequested.equals(current.getSubStatus())) {
                return Returned;
            }
            if (Project.SubStatus.PaymentAuthorisationPending.equals(target.getSubStatus()) && Project.SubStatus.ApprovalRequested.equals(current.getSubStatus())) {
                return PaymentAuthorisationRequested;
            }
            if (target.getSubStatus() == null && Project.SubStatus.AbandonPending.equals(current.getSubStatus())) {
                return AbandonRejected;
            }
        }

        if (target.getStatus().equals(Project.Status.Active) && target.getSubStatus() == null) {
            return Approved;
        }

        if (target.getSubStatus().equals(Project.SubStatus.AbandonPending)) {
            return AbandonRequested;
        }

        return null;
    }
}
