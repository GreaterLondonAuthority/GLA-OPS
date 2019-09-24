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

/**
 * List of filter options for the payment details screens
 *
 * Created by chris on 17/05/2017.
 */
public enum LedgerTypeFilterOption {




    ALL(PaymentSource.values());


    private PaymentSource [] sources;

    LedgerTypeFilterOption(PaymentSource... sources) {
        this.sources = sources;
    }

    public PaymentSource[] getRelevantSources() {
        return sources;
    }
    public List<PaymentSource> getRelevantSourcesAsList() {
        return Arrays.asList(sources);
    }
}
