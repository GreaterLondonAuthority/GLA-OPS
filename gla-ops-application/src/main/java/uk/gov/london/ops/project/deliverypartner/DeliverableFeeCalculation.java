/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.deliverypartner;

import java.math.BigDecimal;

public class DeliverableFeeCalculation {

    private BigDecimal feePercentage;

    private boolean feePercentageExceeded;

    public BigDecimal getFeePercentage() {
        return feePercentage;
    }

    public void setFeePercentage(BigDecimal feePercentage) {
        this.feePercentage = feePercentage;
    }

    public boolean isFeePercentageExceeded() {
        return feePercentageExceeded;
    }

    public void setFeePercentageExceeded(boolean feePercentageExceeded) {
        this.feePercentageExceeded = feePercentageExceeded;
    }

}
