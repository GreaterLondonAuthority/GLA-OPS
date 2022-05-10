/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.london.ops.payment.LedgerStatus.*;

/**
 * List of filter options for the payment details screens
 *
 * Created by chris on 17/05/2017.
 */
public enum PaymentFilterOption {
    PENDING(Pending),
    AUTHORISED(Authorised, Sent, UnderReview, SupplierError, Acknowledged, Cleared),
    DECLINED(Declined),
    //RECLAIMS(),
    ALL_PAYMENTS(Pending, Authorised, Declined, Sent, UnderReview, SupplierError, Acknowledged, Cleared),
    ALL(LedgerStatus.values());

    private final LedgerStatus [] statuses;

    PaymentFilterOption(LedgerStatus... statuses) {
        this.statuses = statuses;
    }

    public LedgerStatus[] getRelevantStatuses() {
        return statuses;
    }

    public List<LedgerStatus> getRelevantStatusesAsList() {
        return Arrays.asList(statuses);
    }
    public List<String> getRelevantStatusesAsString() {
        return Arrays.stream(statuses).map(Enum::name).collect(Collectors.toList());
    }
}
