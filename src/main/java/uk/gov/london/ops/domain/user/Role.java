/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.london.ops.domain.organisation.Organisation;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity(name="user_roles")
public class Role implements GrantedAuthority, Serializable {

    public static final String OPS_ADMIN        = "ROLE_OPS_ADMIN";
    public static final String GLA_ORG_ADMIN    = "ROLE_GLA_ORG_ADMIN";
    public static final String GLA_SPM          = "ROLE_GLA_SPM";
    public static final String GLA_PM           = "ROLE_GLA_PM";
    public static final String GLA_FINANCE      = "ROLE_GLA_FINANCE";
    public static final String GLA_READ_ONLY    = "ROLE_GLA_READ_ONLY";
    public static final String ORG_ADMIN        = "ROLE_ORG_ADMIN";
    public static final String PROJECT_EDITOR   = "ROLE_PROJECT_EDITOR";
    public static final String TECH_ADMIN       = "ROLE_TECH_ADMIN";

    public static final String OPS_ADMIN_DESC       = "OPS Admin";
    public static final String GLA_ORG_ADMIN_DESC   = "GLA Organisation Admin";
    public static final String GLA_SPM_DESC         = "Senior Project Manager";
    public static final String GLA_PM_DESC          = "Project Manager";
    public static final String GLA_FINANCE_DESC     = "GLA Finance";
    public static final String GLA_READ_ONLY_DESC   = "GLA Read Only";
    public static final String ORG_ADMIN_DESC       = "Organisation Admin";
    public static final String PROJECT_EDITOR_DESC  = "Project Editor";
    public static final String TECH_ADMIN_DESC      = "Technical Admin";

    public static final List<String> ALL_ROLES = Arrays.asList(
            OPS_ADMIN,
            GLA_ORG_ADMIN,
            GLA_SPM,
            GLA_PM,
            GLA_FINANCE,
            GLA_READ_ONLY,
            ORG_ADMIN,
            PROJECT_EDITOR,
            TECH_ADMIN
    );

    private static final Map<String, String> ROLES_DESCRIPTIONS = new HashMap<String, String>() {{
        put(OPS_ADMIN, OPS_ADMIN_DESC);
        put(GLA_ORG_ADMIN, GLA_ORG_ADMIN_DESC);
        put(GLA_SPM, GLA_SPM_DESC);
        put(GLA_PM, GLA_PM_DESC);
        put(GLA_FINANCE, GLA_FINANCE_DESC);
        put(GLA_READ_ONLY, GLA_READ_ONLY_DESC);
        put(ORG_ADMIN, ORG_ADMIN_DESC);
        put(PROJECT_EDITOR, PROJECT_EDITOR_DESC);
        put(TECH_ADMIN, TECH_ADMIN_DESC);
    }};

    // if changing this, change v_user_summaries case statement
    public static final List<String> THRESHOLD_ROLES =  Arrays.asList(
            OPS_ADMIN,
            GLA_ORG_ADMIN,
            GLA_SPM
    );

    public boolean isThresholdRole() {
        return THRESHOLD_ROLES.contains(this.getName());
    }

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
    private Organisation organisation;

    @Column(name="approved")
    private Boolean approved;

    @Column(name="approved_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvedOn;

    @Column(name="approved_by")
    private String approvedBy;

    public Role() {}

    public Role(String name, String username, Organisation organisation) {
        this.name = name;
        this.username = username;
        this.organisation = organisation;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
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

    public String getDescription() {
        return getDescription(this.name);
    }

    public static String getDescription(String role) {
        return ROLES_DESCRIPTIONS.get(role);
    }

    public void approve() {
        this.approved = true;
        this.approvedOn = new Date();
        this.approvedBy = SecurityContextHolder.getContext().getAuthentication() != null ? SecurityContextHolder.getContext().getAuthentication().getName() : null;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        if (username != null ? !username.equals(role.username) : role.username != null) return false;
        return !(organisation != null ? !organisation.equals(role.organisation) : role.organisation != null);

    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (organisation != null ? organisation.hashCode() : 0);
        return result;
    }

    /**
     * @return the complete list of valid assignable user roles.
     */
    public static final List<String> availableRoles() {
        return ALL_ROLES;
    }

    public static String getDefaultForOrganisation(Organisation organisation) {
        if (organisation.isManagingOrganisation()) {
            return Role.GLA_PM;
        }
        else {
            return Role.PROJECT_EDITOR;
        }
    }

    public String getSimpleName() {
        return name.replace("ROLE_", "");
    }
}
