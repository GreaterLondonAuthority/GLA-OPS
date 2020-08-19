/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.internalblock;

import uk.gov.london.ops.project.template.domain.RiskRating;

public class InternalProjectBlockOverview extends InternalProjectBlock {

    private final RiskRating rating;

    public InternalProjectBlockOverview(InternalProjectBlockSummary projectBlockSummary) {
        this.id = projectBlockSummary.getId();
        this.type = projectBlockSummary.getType();
        this.displayOrder = projectBlockSummary.getDisplayOrder();
        this.blockDisplayName = projectBlockSummary.getBlockDisplayName();
        this.rating = projectBlockSummary.getRating();
    }

    public RiskRating getRating() {
        return rating;
    }
}
