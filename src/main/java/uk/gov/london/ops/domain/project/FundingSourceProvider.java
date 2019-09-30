/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import java.util.Map;

/**
 * used to calculate maximum payment amount permitted on a project, so payments never exceed this amount.
 * It's cumulative over all blocks of this type.
 */
public interface FundingSourceProvider {

    Map<GrantType, Long> getFundingRequested();

}

