/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

import java.util.Set;

public class ProjectBroadcastDetails extends BroadcastDetail {

    Integer programmeId;
    Set<Integer> templateIds;
    String projectStatus;
    Boolean mainProjectContacts;
    Boolean secondaryProjectContacts;
    Boolean organisationAdmins;

    public ProjectBroadcastDetails(Integer id, Integer programmeId, Set<Integer> templateIds, String projectStatus,
                                   Boolean mainProjectContacts, Boolean secondaryProjectContacts, Boolean organisationAdmins) {
        super(id);
        this.programmeId = programmeId;
        this.templateIds = templateIds;
        this.projectStatus = projectStatus;
        this.mainProjectContacts = mainProjectContacts;
        this.secondaryProjectContacts = secondaryProjectContacts;
        this.organisationAdmins = organisationAdmins;
    }

    public Integer getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(Integer programmeId) {
        this.programmeId = programmeId;
    }

    public Set<Integer> getTemplateIds() {
        return templateIds;
    }

    public void setTemplateIds(Set<Integer> templateIds) {
        this.templateIds = templateIds;
    }

    public String getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(String projectStatus) {
        this.projectStatus = projectStatus;
    }

    public Boolean getMainProjectContacts() {
        return mainProjectContacts;
    }

    public void setMainProjectContacts(Boolean mainProjectContacts) {
        this.mainProjectContacts = mainProjectContacts;
    }

    public Boolean getSecondaryProjectContacts() {
        return secondaryProjectContacts;
    }

    public void setSecondaryProjectContacts(Boolean secondaryProjectContacts) {
        this.secondaryProjectContacts = secondaryProjectContacts;
    }

    public Boolean getOrganisationAdmins() {
        return organisationAdmins;
    }

    public void setOrganisationAdmins(Boolean organisationAdmins) {
        this.organisationAdmins = organisationAdmins;
    }
}
