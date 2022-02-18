/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

public enum ProjectTransition {
    Unconfirmed,
    Created,
    Submitted,
    Withdrawn,
    Returned,
    Assess,
    Initial_Assessment,
    Resubmitted,
    Approved,
    Closed,
    Completed,
    Abandoned,
    Amend,
    ApprovalRequested,
    PaymentAuthorisationRequested,
    DeletedUnapprovedChanges,
    AbandonRequested,
    AbandonRejected,
    Reinstated
}
