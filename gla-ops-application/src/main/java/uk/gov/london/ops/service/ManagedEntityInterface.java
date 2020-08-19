/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import uk.gov.london.ops.organisation.model.Organisation;

/**
 * Interface for Managed Entities (anything with a managing organisation)
 * Default implementations of get id/name
 */
public interface ManagedEntityInterface  {

    Organisation getManagingOrganisation();

    default Integer getManagingOrganisationId() {
        return getManagingOrganisation() == null ? null : getManagingOrganisation().getId();
    }

    default String getManagingOrganisationName() {
        return getManagingOrganisation() == null ? null : getManagingOrganisation().getName();
    }

    default Integer getManagingOrganisationIconAttachmentId() {
        return getManagingOrganisation() == null ? null : getManagingOrganisation().getIconAttachmentId();
    }

}
