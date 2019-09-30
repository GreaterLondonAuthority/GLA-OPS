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
import uk.gov.london.common.user.BaseUser;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.notification.NotificationTargetEntity;
import uk.gov.london.ops.framework.exception.ValidationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.nullSafeConcat;
import static uk.gov.london.common.user.BaseRole.*;

@Entity(name="users")
public class User extends BaseUser implements UserDetails, Serializable, NotificationTargetEntity {

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

    public void setUsername(String username) {
        this.username = username;
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

    public void addApprovedRoleAndPrimaryOrganisationForUser(String roleName, Organisation organisation, boolean primaryOrg) {
        Role role = new Role(roleName, getUsername(), organisation);
        role.approve();
        getRoles().add(role);
        role.setPrimaryOrganisationForUser(primaryOrg);
    }

    public void addUnapprovedRole(String roleName, Organisation organisation) {
        getRoles().add(new Role(roleName, getUsername(), organisation));
    }

    public boolean hasRoleInOrganisation(String role, Integer organisationId) {
        return getApprovedRoles().stream().anyMatch(r -> r.getName().equals(role) && r.getOrganisation().getId().equals(organisationId));
    }

    public Set<Role> getRolesInOrganisation(Integer organisationId) {
        return roles.stream().filter(r -> r.getOrganisation() != null && r.getOrganisation().getId().equals(organisationId)).collect(Collectors.toSet());
    }

    @Deprecated
    public Role getRole(Integer organisationId) {
        Set<Role> rolesInOrganisation = getRolesInOrganisation(organisationId);
        if (rolesInOrganisation.isEmpty()) {
            return null;
        }
        Set<String> roleNames = rolesInOrganisation.stream().map(Role::getName).collect(Collectors.toSet());
        String highestPriorityRole = getHighestPriorityRole(roleNames);

        return rolesInOrganisation.stream().filter(r -> r.getName().equals(highestPriorityRole))
                .findFirst().orElseThrow(() -> new ValidationException("Unable to find matching role: " + highestPriorityRole));
    }

    @Deprecated
    public Role getRole(Organisation organisation) {
        return getRole(organisation.getId());
    }

    public Set<Role> getRolesInOrganisation(Organisation organisation) {
        return getRolesInOrganisation(organisation.getId());
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
     * @return true if the user has the OPS admin role.
     */
    public boolean isOpsAdmin() {
        return hasRole(OPS_ADMIN);
    }

    /**
     * @return true if the user has the GLA admin role.
     */
    public boolean isGlaOrgAdmin() {
        return hasRole(GLA_ORG_ADMIN);
    }

    /**
     * @return true if the user has an org admin role.
     */
    public boolean isOrgAdmin() {
        return hasRole(ORG_ADMIN);
    }

    /**
     * @return true if the user has an org admin role for the given organisation.
     */
    public boolean isOrgAdmin(Organisation organisation) {
        Role role = getRole(organisation);
        return role != null && ORG_ADMIN.equals(role.getName());
    }

    /**
     * @return true if the user is a read only user in the given organisation.
     */
    public boolean isReadOnly(Integer organisationId) {
        Role role = getRole(organisationId);
        return role != null && (GLA_READ_ONLY.equals(role.getName()) || PROJECT_READER.equals(role.getName()));
    }

    public boolean isApproved() {
        for (Role role: roles) {
            if (role.isApproved()) {
                return true;
            }
        }
        return false;
    }


    public Organisation getPrimaryOrganisation() {
        return this.getApprovedRoles().stream()
                .filter(r -> r.isPrimaryOrganisationForUser() == null ? false : r.isPrimaryOrganisationForUser())
                .map(Role::getOrganisation).findFirst()
                .orElse(null);

    }

    public boolean inOrganisation(Integer organisationId) {
        return CollectionUtils.isNotEmpty(getApprovedRolesForOrg(organisationId));
    }

    public boolean inOrganisation(Organisation organisation) {
        return CollectionUtils.isNotEmpty(getApprovedRolesForOrgs(organisation));
    }

    public boolean isManagedBy(Organisation managingOrganisation){
        return getOrganisations().stream().anyMatch(o -> managingOrganisation.equals(o.getManagingOrganisation()));
    }

    public String getFullName() {
        return nullSafeConcat(firstName, lastName);
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
        return roles.stream().filter(r -> r.isApproved() && (r.getOrganisation() != null && r.getOrganisation().isApproved())).collect(Collectors.toSet());
    }


    @Override
    public String getIdAsString() {
        return this.username != null ? username : null;
    }

}
