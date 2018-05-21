/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model;

import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.organisation.OrganisationStatus;

/**
 * Created by chris on 30/11/2016.
 */
public class OrganisationModel {

    private Integer id;

    private String name;

    private Integer managingOrganisationId;

    private OrganisationStatus status;

    private boolean isTechOrg;

    private boolean isManagingOrganisation;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getManagingOrganisationId() {
        return managingOrganisationId;
    }

    public void setManagingOrganisationId(Integer managingOrganisationId) {
        this.managingOrganisationId = managingOrganisationId;
    }

    public OrganisationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganisationStatus status) {
        this.status = status;
    }

    public void setIsTechOrg(boolean isTechOrg) {
        this.isTechOrg = isTechOrg;
    }

    public void setIsManagingOrganisation(boolean isManagingOrganisation) {
        this.isManagingOrganisation = isManagingOrganisation;
    }

    public boolean getisTechOrg() {
        return isTechOrg;
    }
    public boolean getIsManagingOrganisation() {return isManagingOrganisation;}

    public static OrganisationModel from(Organisation organisation) {
        OrganisationModel model = new OrganisationModel();
        model.setId(organisation.getId());
        model.setName(organisation.getName());
        model.setManagingOrganisationId(organisation.getManagingOrganisationId());
        model.setStatus(organisation.getStatus());
        model.setIsTechOrg(organisation.isTechSupportOrganisation());
        model.setIsManagingOrganisation(organisation.isManagingOrganisation());
        return model;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrganisationModel that = (OrganisationModel) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
