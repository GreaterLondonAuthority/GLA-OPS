/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.dto;

import uk.gov.london.ops.organisation.OrganisationStatus;

/**
 * Created by chris on 30/11/2016.
 */
public class OrganisationModel {

    public Integer id;
    public String name;
    public Integer managingOrganisationId;
    public OrganisationStatus status;
    public boolean isTechOrg;
    public boolean isManagingOrganisation;
    public Boolean registrationAllowed;
    public boolean skillsGatewayAccessAllowed;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrganisationModel that = (OrganisationModel) o;
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        return !(name != null ? !name.equals(that.name) : that.name != null);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

}