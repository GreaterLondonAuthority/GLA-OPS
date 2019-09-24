/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.organisation;

import java.util.Objects;

public class OrganisationTeam {

    private Integer organisationId;

    private String organisationName;

    private Integer teamId;

    private String teamName;

    public OrganisationTeam() {
    }

    public OrganisationTeam(Integer organisationId, Integer teamId) {
        this.organisationId = organisationId;
        this.teamId = teamId;
    }

    public OrganisationTeam(Integer organisationId, String organisationName, Integer teamId, String teamName) {
        this.organisationId = organisationId;
        this.organisationName = organisationName;
        this.teamId = teamId;
        this.teamName = teamName;
    }

    public OrganisationTeam(Integer organisationId, String organisationName) {
        this.organisationId = organisationId;
        this.organisationName = organisationName;
    }

    public Integer getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Integer organisationId) {
        this.organisationId = organisationId;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganisationTeam that = (OrganisationTeam) o;
        return Objects.equals(organisationId, that.organisationId) &&
                Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(organisationId, teamId);
    }
}
