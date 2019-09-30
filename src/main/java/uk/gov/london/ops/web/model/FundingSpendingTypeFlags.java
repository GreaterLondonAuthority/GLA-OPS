/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model;

/**
 * API value object for updating flags in Funding template block.
 *
 * @author Chris Melville
 */
public class FundingSpendingTypeFlags {
    public Boolean showCapitalGLA = null;
    public Boolean showCapitalOther = null;
    public Boolean showRevenueGLA = null;
    public Boolean showRevenueOther = null;

    @Override
    public String toString() {
        return "FundingSpendingTypeFlags{" +
                "showCapitalGLA=" + showCapitalGLA +
                ", showCapitalOther=" + showCapitalOther +
                ", showRevenueGLA=" + showRevenueGLA +
                ", showRevenueOther=" + showRevenueOther +
                '}';
    }
}
