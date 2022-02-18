/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user.domain;

import uk.gov.london.ops.organisation.dto.OrganisationModel;
import uk.gov.london.ops.role.model.RoleModel;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Restricted version of the User entity for use in the API and web app.
 *
 * This ensures that any sensitive attributes of the User entity are
 * protected by default, and only available in the API by explicit design.
 */
public class UserModel implements Serializable {

    private String username;
    private String firstName;
    private String lastName;
    private String primaryRole;
    private List<RoleModel> roles = new ArrayList<>();
    private Set<OrganisationModel> organisations = new HashSet<>();
    private Date registeredOn;
    private OffsetDateTime lastLoggedOn;
    private Set<String> permissions = new HashSet<>();
    private boolean approved;
    private String fullName;

    // Durations in seconds
    private Integer idleDuration = 1;
    private Integer timeoutDuration = 5;
    private Integer keepAliveInterval = 5 * 60;

    public static final String PRIMARY_ROLE_OPS_ADMIN = "Admin";
    public static final String PRIMARY_ROLE_USER = "Unapproved User";
    public static final String PRIMARY_ROLE_PARTNER = "GLA Partner";

    public UserModel() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {

        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPrimaryRole() {
        return primaryRole;
    }

    public void setPrimaryRole(String primaryRole) {
        this.primaryRole = primaryRole;
    }

    public List<RoleModel> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleModel> roles) {
        this.roles = roles;
    }

    public Date getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(Date registeredOn) {
        this.registeredOn = registeredOn;
    }

    public OffsetDateTime getLastLoggedOn() {
        return lastLoggedOn;
    }

    public void setLastLoggedOn(OffsetDateTime lastLoggedOn) {
        this.lastLoggedOn = lastLoggedOn;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Set<OrganisationModel> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(Set<OrganisationModel> organisations) {
        this.organisations = organisations;
    }

    public Integer getIdleDuration() {
        return idleDuration;
    }

    public void setIdleDuration(Integer idleDuration) {
        this.idleDuration = idleDuration;
    }

    public Integer getTimeoutDuration() {
        return timeoutDuration;
    }

    public void setTimeoutDuration(Integer timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
    }

    public Integer getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public void setKeepAliveInterval(Integer keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }
}
