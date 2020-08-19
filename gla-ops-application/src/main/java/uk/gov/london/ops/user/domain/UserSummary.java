/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.common.organisation.OrganisationType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "UserSummary")
@Table(name = "users")
public class UserSummary {

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Id
    @Column(name = "username")
    private String username;

    @Column
    private Boolean enabled;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = UserRoleSummary.class)
    @JoinColumn(name = "username")
    private Set<UserRoleSummary> roles = new HashSet<>();

    @Transient
    private Set<UserRoleSummary> accessibleRoles = new HashSet<>();

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @JsonIgnore
    public Set<UserRoleSummary> getRoles() {
        return roles;
    }

    @JsonProperty(value = "roles", access = JsonProperty.Access.READ_ONLY)
    public Set<UserRoleSummary> getAccessibleRoles() {
        return accessibleRoles;
    }

    public void setAccessibleRoles(Set<UserRoleSummary> accessibleRoles) {
        this.accessibleRoles = accessibleRoles;
    }

    public void setRoles(Set<UserRoleSummary> roles) {
        this.roles = roles;
    }

    public boolean getHasRoleInManagingOrg() {
        return roles.stream()
                .filter(r -> r.getEntityTypeId() != null) // should just be an issue in tests
                .anyMatch(r -> OrganisationType.MANAGING_ORGANISATION.id() == r.getEntityTypeId());
    }
}
