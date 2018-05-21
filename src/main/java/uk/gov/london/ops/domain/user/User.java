/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.userdetails.UserDetails;
import uk.gov.london.ops.domain.organisation.Organisation;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity(name="users")
public class User implements UserDetails, Serializable {

    @Id
    @Column(nullable = false, updatable = false)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column
    private Boolean enabled;

    @Column(name="firstname")
    private String firstName;

    @Column(name="lastname")
    private String lastName;

    @Column(name="phone_number")
    private String phoneNumber;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = Role.class)
    @JoinColumn(name="username")
    private Set<Role> roles = new HashSet<>();

    @Column(name="registered_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date registeredOn;

    @Column(name="last_logged_on")
    private OffsetDateTime lastLoggedOn;

    public User() {}

    public User(String username) {
        this.username = username;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.enabled = true;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public Set<Role> getAuthorities() {
        return roles;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return the list of organisations the user is linked and where he have been approved.
     */
    public Set<Organisation> getOrganisations() {
        return getApprovedRoles().stream().map(Role::getOrganisation).collect(Collectors.toSet());
    }

    public List<Integer> getOrganisationIds() {
        return getOrganisations().stream().map(Organisation::getId).collect(Collectors.toList());
    }

    public void addApprovedRole(String roleName, Organisation organisation) {
        Role role = new Role(roleName, getUsername(), organisation);
        role.approve();
        getRoles().add(role);
    }

    public void addUnapprovedRole(String roleName, Organisation organisation) {
        getRoles().add(new Role(roleName, getUsername(), organisation));
    }

    public Role getRole(Integer organisationId) {
        Role role = null;
        for (Role r: roles) {
            if (r.getOrganisation() != null && r.getOrganisation().getId().equals(organisationId)) {
                role = r;
            }
        }
        return role;
    }

    public Role getRole(Organisation organisation) {
        return getRole(organisation.getId());
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

    /**
     * @return true if the user has the GLA role (Admin, SPM or PM).
     */
    public boolean isGla() {
        return hasRole(Role.OPS_ADMIN) || hasRole(Role.GLA_ORG_ADMIN) || hasRole(Role.GLA_SPM) || hasRole(Role.GLA_PM) || hasRole(Role.GLA_FINANCE) || hasRole(Role.GLA_READ_ONLY);
    }

    /**
     * @return true if the user has the OPS admin role.
     */
    public boolean isOpsAdmin() {
        return hasRole(Role.OPS_ADMIN);
    }

    /**
     * @return true if the user has the GLA admin role.
     */
    public boolean isGlaOrgAdmin() {
        return hasRole(Role.GLA_ORG_ADMIN);
    }

    /**
     * @return true if the user has an org admin role.
     */
    public boolean isOrgAdmin() {
        return hasRole(Role.ORG_ADMIN);
    }

    /**
     * @return true if the user has an org admin role for the given organisation.
     */
    public boolean isOrgAdmin(Organisation organisation) {
        Role role = getRole(organisation);
        return role != null && Role.ORG_ADMIN.equals(role.getName());
    }

    public boolean hasRole(String role) {
        return roles.stream().filter(Role::isApproved).map(Role::getName).collect(Collectors.toList()).contains(role);
    }

    public boolean isApproved() {
        for (Role role: roles) {
            if (role.isApproved()) {
                return true;
            }
        }
        return false;
    }

    public boolean inOrganisation(Integer organisationId) {
        return CollectionUtils.isNotEmpty(getApprovedRolesForOrg(organisationId));
    }

    public boolean inOrganisation(Organisation organisation) {
        return CollectionUtils.isNotEmpty(getApprovedRolesForOrgs(organisation));
    }

    public String getFullName() {
        return firstName+" "+lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return !(username != null ? !username.equals(user.username) : user.username != null);
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

    /**
     * Returns the names of the roles the user has in the specified organisation.
     */
    public Set<String> getApprovedRolesForOrgs(Organisation... organisations) {
        Set<String> result = new HashSet<>();
        for (Organisation organisation : organisations) {
            if (organisation != null) {
                result.addAll(getApprovedRolesForOrg(organisation.getId()));
            }
        }
        return result;
    }

    /**
     * Returns the names of the roles the user has in the specified organisation.
     */
    public Set<String> getApprovedRolesForOrg(int organisationId) {
        return  roles.stream()
                .filter(role -> role.getOrganisation() != null)
                .filter(role -> role.getOrganisation().getId().equals(organisationId))
                .filter(Role::isApproved)
                .map(Role::getSimpleName)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the names of the roles the user has.
     */
    public Set<String> getApprovedRolesNames() {
        return getApprovedRoles().stream().map(Role::getSimpleName).collect(Collectors.toSet());
    }

    public Set<Role> getApprovedRoles() {
        return roles.stream().filter(r -> r.isApproved() && r.getOrganisation().isApproved()).collect(Collectors.toSet());
    }

}
