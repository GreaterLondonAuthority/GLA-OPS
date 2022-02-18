/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation;

import java.util.List;

public interface OrganisationService {

    Organisation findOne(Integer organisationId);

    void validateContractUsage(Integer contractId);

    String getOrganisationName(Integer orgId);

    boolean isManagingOrganisation(Integer orgId);

    boolean isTeamOrganisation(Integer orgId);

    boolean isTechSupportOrganisation(Integer orgId);

}
