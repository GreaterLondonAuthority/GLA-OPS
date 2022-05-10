/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.userdetails.UserDetails;
import uk.gov.london.common.user.BaseUser;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.notification.NotificationTargetEntity;
import uk.gov.london.ops.organisation.Organisation;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.role.model.Role;
import uk.gov.london.ops.user.User;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.nullSafeConcat;
import static uk.gov.london.common.user.BaseRole.*;

@Entity(name = "users")
public class UserEntity extends BaseUser implements User, UserDetails, Serializable, NotificationTargetEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column
    private Boolean enabled;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "deactivated_by")
    private String deactivatedBy;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = Role.class)
    @JoinColumn(name = "username")
    private Set<Role> roles = new HashSet<>();

    @Column(name = "registered_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date registeredOn;

    @Column(name = "last_logged_on")
    private OffsetDateTime lastLoggedOn;

    @Column(name = "password_expiry")
    private OffsetDateTime passwordExpiry;

    @Column(name = "user_id")
    private Integer userId;

    public UserEntity() {
    }

    public UserEntity(String username) {
        this.username = username;
        this.userId = getUserIdFromUserName(username);
    }

    public UserEntity(String username, String password) {
        this.username = username;
        this.password = password;
        this.enabled = true;
        this.userId = getUserIdFromUserName(username);
    }

    public UserEntity(String username, String firstName, String lastName) {
        this(username);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public Integer getId() {
        return userId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    private Integer getUserIdFromUserName(String userName) {
        return (userName == null) ? null : (int) (userName.hashCode() * Math.random());
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
        if (registeredOn != null) {
            return passwordExpiry != null && passwordExpiry.isAfter(OffsetDateTime.now());
        }
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
    public Set<OrganisationEntity> getOrganisations() {
        return getApprovedRoles().stream().map(Role::getOrganisation).collect(Collectors.toSet());
    }

    /*
    TODO : rename this to getOrganisations and UserEntity getOrganisations to getOrganisationEntities
     */
    public Set<Organisation> getOrgs() {
        return new HashSet<>(getOrganisations());
    }

    public List<Integer> getOrganisationIds() {
        return getOrganisations().stream().map(OrganisationEntity::getId).collect(Collectors.toList());
    }

    public Set<OrganisationEntity> getManagingOrganisations() {
        Set<OrganisationEntity> managingOrganisations = new HashSet<>();
        for (OrganisationEntity organisation : getOrganisations()) {
            managingOrganisations.add(organisation.isManaging()
                    ? organisation
                    : organisation.getManagingOrganisation());
        }
        return managingOrganisations;
    }

    public Set<Integer> getManagingOrganisationsIds() {
        return getManagingOrganisations().stream().map(OrganisationEntity::getId).collect(Collectors.toSet());
    }

    public void addApprovedRole(String roleName, OrganisationEntity organisation) {
        Role role = new Role(roleName, getUsername(), organisation);
        role.approve();
        getRoles().add(role);
    }

    public void addApprovedRoleAndPrimaryOrganisationForUser(String roleName, OrganisationEntity organisation, boolean primaryOrg) {
        Role role = new Role(roleName, getUsername(), organisation);
        role.approve();
        getRoles().add(role);
        role.setPrimaryOrganisationForUser(primaryOrg);
    }

    public void addUnapprovedRole(String roleName, OrganisationEntity organisation) {
        getRoles().add(new Role(roleName, getUsername(), organisation));
    }

    public boolean hasRoleInOrganisation(String role, Integer organisationId) {
        return getApprovedRoles()
                .stream()
                .anyMatch(r -> r.getName().equals(role) && r.getOrganisation().getId().equals(organisationId));
    }

    public boolean canAssignRolesInOrganisation(Integer organisationId, Integer managingOrgId) {

        // user roles allowed to assign users
        List<String> rolesAbleToAssign = Arrays.asList(GLA_ORG_ADMIN, OPS_ADMIN, ORG_ADMIN);

        Set<Role> rolesInOrganisation = getRolesInOrganisation(organisationId);

        boolean actualOrgRole = rolesInOrganisation.stream().map(Role::getName).distinct().anyMatch(rolesAbleToAssign::contains);

        if (actualOrgRole) {
            return true;
        }

        rolesInOrganisation = getRolesInOrganisation(managingOrgId);
        return rolesInOrganisation.stream().map(Role::getName).distinct().anyMatch(rolesAbleToAssign::contains);
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
    public Role getRole(OrganisationEntity organisation) {
        return getRole(organisation.getId());
    }

    public Set<Role> getRolesInOrganisation(OrganisationEntity organisation) {
        return getRolesInOrganisation(organisation.getId());
    }

    public Set<Role> getRolesInOrganisation(Integer organisationId) {
        return roles.stream()
                .filter(r -> r.getOrganisation() != null && r.getOrganisation().getId().equals(organisationId))
                .collect(Collectors.toSet());
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
     * @return true if the user has an tech admin role.
     */
    public boolean isTechAdmin() {
        return hasRole(TECH_ADMIN);
    }

    /**
     * @return true if the user has an org admin role.
     */
    public boolean isOrgAdmin() {
        return hasRole(ORG_ADMIN);
    }

    /**
     * @return true if the user has PM role.
     */
    public boolean isPm() {
        return hasRole(GLA_PM);
    }

    /**
     * @return true if the user has SPM role.
     */
    public boolean isSpm() {
        return hasRole(GLA_SPM);
    }

    /**
     * @return true if the user has an org admin role for the given organisation.
     */
    public boolean isOrgAdmin(OrganisationEntity organisation) {
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
        for (Role role : roles) {
            if (role.isApproved()) {
                return true;
            }
        }
        return false;
    }

    public OrganisationEntity getPrimaryOrganisation() {
        return this.getApprovedRoles().stream()
                .filter(r -> r.isPrimaryOrganisationForUser() == null ? false : r.isPrimaryOrganisationForUser())
                .map(Role::getOrganisation).findFirst()
                .orElse(null);
    }

    @JsonIgnore
    public Integer getPrimaryOrganisationId() {
        OrganisationEntity primaryOrganisation = getPrimaryOrganisation();
        return primaryOrganisation != null ? primaryOrganisation.getId() : null;
    }

    public boolean inOrganisation(Integer organisationId) {
        return CollectionUtils.isNotEmpty(getApprovedRolesForOrg(organisationId));
    }

    public boolean inOrganisation(OrganisationEntity organisation) {
        return CollectionUtils.isNotEmpty(getApprovedRolesForOrgs(organisation));
    }

    public boolean isManagedBy(OrganisationEntity managingOrganisation) {
        return getOrganisations().stream().anyMatch(o -> managingOrganisation.equals(o.getManagingOrganisation()));
    }

    public String getFullName() {
        return nullSafeConcat(firstName, lastName);
    }

    public OffsetDateTime getPasswordExpiry() {
        return passwordExpiry;
    }

    public void setPasswordExpiry(OffsetDateTime passwordExpiry) {
        this.passwordExpiry = passwordExpiry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserEntity user = (UserEntity) o;

        return !(username != null ? !username.equals(user.username) : user.username != null);
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

    /**
     * Returns the names of the roles the user has in the specified organisation.
     */
    public Set<String> getApprovedRolesForOrgs(OrganisationEntity... organisations) {
        Set<String> result = new HashSet<>();
        for (OrganisationEntity organisation : organisations) {
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
        return roles.stream()
                .filter(role -> role.getOrganisation() != null)
                .filter(role -> role.getOrganisation().getId().equals(organisationId))
                .filter(Role::isApproved)
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the names of the roles the user has.
     */
    public Set<String> getApprovedRolesNames() {
        return getApprovedRoles().stream().map(Role::getName).collect(Collectors.toSet());
    }

    public Set<Role> getApprovedRoles() {
        return roles.stream()
                .filter(r -> r.isApproved() && (r.getOrganisation() != null && r.getOrganisation().isApproved()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getIdAsString() {
        return username;
    }

    public String getDeactivatedBy() {
        return deactivatedBy;
    }

    public void setDeactivatedBy(String deactivedBy) {
        this.deactivatedBy = deactivedBy;
    }

    public boolean wasDeactivatedBySystem() {
        return SYSTEM_DEACTIVATED_USERNAME.equals(getDeactivatedBy());
    }

    @Override
    public List<Integer> getAccessibleOrganisationIds() {
        List<Integer> organisationIds = new ArrayList<>();
        for (Role role: getRoles()) {
            if (role.isApproved() || ORG_ADMIN.equals(role.getName())) {
                organisationIds.add(role.getOrganisation().getId());
            }
        }
        return organisationIds;
    }

}
