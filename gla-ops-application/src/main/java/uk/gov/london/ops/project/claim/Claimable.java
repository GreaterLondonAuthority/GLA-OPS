/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.project.claim;

import static uk.gov.london.ops.payment.LedgerStatus.Cleared;

import java.math.BigDecimal;
import java.util.List;
import uk.gov.london.ops.payment.PaymentSummary;

public interface Claimable {

    String CLAIM_STATUS_PROCESSING = "Processing";
    String CLAIM_STATUS_PAID = "Paid";
    String CLAIM_STATUS_PARTLY_PAID = "Partly Paid";
    String CLAIM_STATUS_OVER_PAID = "Overpaid";

    boolean isClaimable();

    List<PaymentSummary> getPayments();

    BigDecimal getClaimableAmount();

    default String getPaymentStatus() {
        if (hasPaymentStatus()) {
            if (getClaimableAmount() != null && getClaimableAmount().compareTo(getPaymentsTotal()) > 0) {
                return CLAIM_STATUS_PARTLY_PAID;
            } else if (getClaimableAmount() != null && getClaimableAmount().compareTo(getPaymentsTotal()) < 0) {
                return CLAIM_STATUS_OVER_PAID;
            } else if (hasPayments() && getPayments().stream().allMatch(p -> Cleared.equals(p.getLedgerStatus()))) {
                return CLAIM_STATUS_PAID;
            } else if (hasPayments()) {
                return CLAIM_STATUS_PROCESSING;
            }
        }

        return null;
    }

    default BigDecimal getPaymentsTotal() {
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (PaymentSummary payment : getPayments()) {
            if (!payment.getInterestPayment()) {
                totalPaid = totalPaid.add(payment.getValue());
            }
        }

        return totalPaid;
    }

    default boolean hasPayments() {
        return getPayments() != null && !getPayments().isEmpty();
    }

    default boolean hasPaymentStatus() {
        return hasPayments();
    }

}
