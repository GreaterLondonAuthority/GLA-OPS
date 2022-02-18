/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block;

import uk.gov.london.ops.framework.enums.GrantType;

import java.math.BigDecimal;
import java.util.Map;

/**
 * used to calculate maximum payment amount permitted on a project, so payments never exceed this amount.
 * It's cumulative over all blocks of this type.
 */
public interface FundingSourceProvider {

    Map<GrantType, BigDecimal> getFundingRequested();

    default BigDecimal getTotalGrantRequested() {
        return getFundingRequested().values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    default BigDecimal getGrantAdjustmentAmount(FundingSourceProvider previousVersion) {
        BigDecimal currentGrant = this.getFundingRequested().getOrDefault(GrantType.Grant, BigDecimal.ZERO);
        BigDecimal previousGrant = previousVersion.getFundingRequested().getOrDefault(GrantType.Grant, BigDecimal.ZERO);
        return currentGrant.subtract(previousGrant);
    }

}
