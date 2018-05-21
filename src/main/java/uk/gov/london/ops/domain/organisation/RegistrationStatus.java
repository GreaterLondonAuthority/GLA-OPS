/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.organisation;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RegistrationStatus {

    @JsonProperty("Approved")
    Approved,
    @JsonProperty("Pending")
    Pending

}
