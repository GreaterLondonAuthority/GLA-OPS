/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model;

/**
 * Created by Saud on 03/02/2017.
 */
public class OrganisationGroupModel {

    private Integer id;
    private String name;
    private String leadOrgName;

    public OrganisationGroupModel() {
    }

    public OrganisationGroupModel(Integer id, String name, String leadOrgName) {
        this.id = id;
        this.name = name;
        this.leadOrgName = leadOrgName;
    }

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

    public String getLeadOrgName() {
        return leadOrgName;
    }

    public void setLeadOrgName(String leadOrgName) {
        this.leadOrgName = leadOrgName;
    }

}
