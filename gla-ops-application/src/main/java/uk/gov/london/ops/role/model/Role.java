/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.role.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.london.common.user.BaseRole;
import uk.gov.london.ops.framework.OpsEntity;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.organisation.model.OrganisationEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity(name = "user_roles")
public class Role extends BaseRole implements GrantedAuthority, Serializable, OpsEntity<Integer> {

    // if changing this, change v_user_role_summaries case statement
    private static final List<String> THRESHOLD_ROLES = Arrays.asList(
            OPS_ADMIN,
            GLA_ORG_ADMIN,
            GLA_SPM
    );

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_roles_seq_gen")
    @SequenceGenerator(name = "user_roles_seq_gen", sequenceName = "user_roles_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String username;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "organisation_id", nullable = false)
    private OrganisationEntity organisation;

    @Column(name = "approved")
    private Boolean approved;

    @Column(name = "org_admin_requested")
    private Boolean orgAdminRequested;

    @Column(name = "primary_org_for_user")
    private Boolean primaryOrganisationForUser;

    @Column(name = "authorised_signatory")
    private Boolean authorisedSignatory;

    @Column(name = "approved_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvedOn;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    public Role() {}

    public Role(String name, String username, OrganisationEntity organisation) {
        this.name = name;
        this.username = username;
        this.organisation = organisation;
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
        if (!Role.availableRoles().contains(name)) {
            throw new ValidationException("Unknown role: " + name);
        }
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public OrganisationEntity getOrganisation() {
        return organisation;
    }

    public void setOrganisation(OrganisationEntity organisation) {
        this.organisation = organisation;
    }

    public boolean isApproved() {
        return Boolean.TRUE.equals(approved);
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Date getApprovedOn() {
        return approvedOn;
    }

    public void setApprovedOn(Date approvedOn) {
        this.approvedOn = approvedOn;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public String getDescription() {
        return BaseRole.getDescription(this.name);
    }

    public Boolean isPrimaryOrganisationForUser() {
        return primaryOrganisationForUser == null ? false : primaryOrganisationForUser;
    }

    public void setPrimaryOrganisationForUser(Boolean primaryOrganisationForUser) {
        this.primaryOrganisationForUser = primaryOrganisationForUser;
    }

    public Boolean getOrgAdminRequested() {
        return orgAdminRequested;
    }

    public void setOrgAdminRequested(Boolean orgAdminRequested) {
        this.orgAdminRequested = orgAdminRequested;
    }

    public Boolean getAuthorisedSignatory() {
        return authorisedSignatory;
    }

    public void setAuthorisedSignatory(Boolean authorisedSignatory) {
        this.authorisedSignatory = authorisedSignatory;
    }

    public void approve() {
        this.approved = true;
        this.approvedOn = new Date();
        this.approvedBy = SecurityContextHolder.getContext().getAuthentication() != null
            ? SecurityContextHolder.getContext().getAuthentication().getName() : null;
    }

    public void unapprove() {
        this.approved = false;
        this.approvedOn = null;
        this.approvedBy = null;
    }

    @Override
    public String getAuthority() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Role role = (Role) o;
        return Objects.equals(name, role.name)
            && Objects.equals(username, role.username)
            && Objects.equals(organisation, role.organisation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, username, organisation);
    }

    public static String getDefaultForOrganisation(OrganisationEntity organisation) {
        if (organisation.isManaging() || organisation.isTeamOrganisation()) {
            return GLA_PM;
        } else {
            // if no users on this org then must be first reg
            if (organisation.getUserEntities() == null || organisation.getUserEntities().size() == 0) {
                return ORG_ADMIN;
            }
            if (organisation.isTechSupportOrganisation()) {
                return TECH_ADMIN;
            }
            return PROJECT_EDITOR;
        }
    }

    public boolean isThresholdRole() {
        return THRESHOLD_ROLES.contains(this.getName());
    }
}
