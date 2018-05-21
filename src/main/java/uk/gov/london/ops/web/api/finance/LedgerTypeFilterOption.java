/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api.finance;

import uk.gov.london.ops.domain.finance.LedgerType;

import java.util.Arrays;
import java.util.List;

/**
 * List of filter options for the payment details screens
 *
 * Created by chris on 17/05/2017.
 */
public enum LedgerTypeFilterOption {




    ALL_PAYMENTS(LedgerType.RCGF, LedgerType.DPF, LedgerType.PAYMENT),
    ALL(LedgerType.values());


    private LedgerType [] statuses;

    LedgerTypeFilterOption(LedgerType... statuses) {
        this.statuses = statuses;
    }

    public LedgerType[] getRelevantStatuses() {
        return statuses;
    }
    public List<LedgerType> getRelevantStatusesAsList() {
        return Arrays.asList(statuses);
    }
}
