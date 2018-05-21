/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api.finance;

import uk.gov.london.ops.domain.finance.LedgerStatus;

import java.util.Arrays;
import java.util.List;

import static uk.gov.london.ops.domain.finance.LedgerStatus.Acknowledged;
import static uk.gov.london.ops.domain.finance.LedgerStatus.Authorised;
import static uk.gov.london.ops.domain.finance.LedgerStatus.Cleared;
import static uk.gov.london.ops.domain.finance.LedgerStatus.Declined;
import static uk.gov.london.ops.domain.finance.LedgerStatus.Pending;
import static uk.gov.london.ops.domain.finance.LedgerStatus.Sent;
import static uk.gov.london.ops.domain.finance.LedgerStatus.SupplierError;
import static uk.gov.london.ops.domain.finance.LedgerStatus.UnderReview;

/**
 * List of filter options for the payment details screens
 *
 * Created by chris on 17/05/2017.
 */
public enum PaymentFilterOption {



    PENDING(Pending) ,
    AUTHORISED(Authorised, Sent, UnderReview, SupplierError, Acknowledged, Cleared),
    DECLINED(Declined),
//    RECLAIMS(),
    ALL_PAYMENTS(Pending, Authorised, Declined, Sent, UnderReview, SupplierError, Acknowledged, Cleared),
    ALL(LedgerStatus.values());


    private LedgerStatus [] statuses;

    PaymentFilterOption(LedgerStatus... statuses) {
        this.statuses = statuses;
    }

    public LedgerStatus[] getRelevantStatuses() {
        return statuses;
    }
    public List<LedgerStatus> getRelevantStatusesAsList() {
        return Arrays.asList(statuses);
    }
}
