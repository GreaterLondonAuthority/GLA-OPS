/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.finance;

import java.util.HashSet;
import java.util.Set;

/**
 * States that a PaymentRequest can be in.
 */
public enum LedgerStatus {

    Pending,        // Payment request made but not yet authorised
    Authorised,     // Payment request has been authorised but not sent to SAP
    Declined,       // Payment request has been declined
    Sent,           // Invoice file has been sent to SAP
    UnderReview,    // Invoice not processed, but can potentially be resolved in SAP
    SupplierError,  // Invoice cannot be processed due to error that cannot be resolved in SAP
    Acknowledged,   // Invoice has been posted in SAP
    Cleared,        // Invoice has been included in SAP payment run
    BUDGET,         // From ProjectLedgerEntry
    FORECAST,
    ACTUAL;

    public static LedgerStatus parseSapStatus(String sapStatusText) {

        final String status = sapStatusText.toLowerCase();
        switch(status) {
            case "acknowledged":
                return Acknowledged;
            case "cleared":
                return Cleared;
            case "supplier error":
                return SupplierError;
            case "under review":
                return UnderReview;
            default: return null;
        }
    }

    public static Set<LedgerStatus> getApprovedPaymentStatuses() {

        return new HashSet<LedgerStatus>() {{ add(Authorised); add(Sent); add(UnderReview); add(SupplierError); add(Acknowledged); add(Cleared);}};
    }
}
