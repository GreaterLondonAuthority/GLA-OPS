/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.contracts;

import uk.gov.london.ops.framework.enums.OrganisationContractStatus;

public enum ContractAction {
    REVERT_TO_PENDING("Revert to Pending", OrganisationContractStatus.Blank, false, false),
    MARK_AS_SIGNED("Mark as 'Signed'", OrganisationContractStatus.Signed, false, false),
    MARK_AS_NOT_REQUIRED("Mark as 'Not Required'", OrganisationContractStatus.NotRequired, false, false),
    VIEW_DETAILS("View Details", null, true, false),
    MAKE_OFFER("View Details", OrganisationContractStatus.Offered, true, false),
    WITHDRAW_OFFER("View Details", OrganisationContractStatus.PendingOffer, true, false),
    ACCEPT_OFFER("View Details", OrganisationContractStatus.Accepted, true, false),
    VARIATION_OFFER("Add Variation", OrganisationContractStatus.PendingOffer, true, true);

    public final String text;
    public final OrganisationContractStatus  nextStatus;
    public final Boolean doViewDetails;
    public final boolean newVariationEntry;

    private ContractAction(String text, OrganisationContractStatus nextStatus, Boolean doViewDetails,
                           boolean newVariationEntry) {
        this.text = text;
        this.nextStatus = nextStatus;
        this.doViewDetails = doViewDetails;
        this.newVariationEntry = newVariationEntry;
    }
}
