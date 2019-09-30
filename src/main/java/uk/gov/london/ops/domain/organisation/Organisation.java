/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.organisation;

import static javax.persistence.CascadeType.ALL;
import static uk.gov.london.common.GlaUtils.generateRandomId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import org.apache.commons.lang3.StringUtils;
import uk.gov.london.common.organisation.BaseOrganisation;
import uk.gov.london.common.organisation.OrganisationType;
import uk.gov.london.ops.annualsubmission.AnnualSubmission;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.template.Contract;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.notification.NotificationTargetEntity;
import uk.gov.london.ops.service.ManagedEntityInterface;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.jpa.NonJoin;
import uk.gov.london.ops.web.model.ContractModel;
import uk.gov.london.ops.web.model.UserModel;

/**
 * A business or organised group of people with a particular purpose.
 *
 * Examples include GLA itself, registered providers and local authorities.
 *
 * Every user will belong to an organisation.
 *
 * Created by sleach on 17/08/2016.
 */
@Entity
public class Organisation extends BaseOrganisation implements Serializable, ManagedEntityInterface, NotificationTargetEntity {

    public static final Integer GLA_OPS_ID = 8000;
    public static final Integer GLA_HNL_ID = 10000;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organisation_seq_gen")
    @SequenceGenerator(name = "organisation_seq_gen", sequenceName = "organisation_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name="name")
    private String name;

    @Column(name="website")
    private String website;

    @Column(name="contact_number")
    private String contactNumber;

    @Column(name="ims_number")
    private String imsNumber;

    @Column(name="registration_key")
    private String registrationKey;

    private String createdBy;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Transient
    private String createdByName;

    @Transient
    private String approvedByName;

    @Transient
    private String rejectedByName;

    @Transient
    private String inactivatedByName;

    @Column(name="email")
    private String email;

    @Column(name="change_status_reason")
    @Enumerated(EnumType.STRING)
    private OrganisationChangeStatusReason changeStatusReason;

    @Column(name="change_status_reason_details")
    private String changeStatusReasonDetails;

    private String approvedBy;

    @Column(name = "approved_on")
    private OffsetDateTime approvedOn;

    private String rejectedBy;

    @Column(name = "rejected_on")
    private OffsetDateTime rejectedOn;

    private String inactivatedBy;

    @Column(name = "inactivated_on")
    private OffsetDateTime inactivatedOn;

    @Column(name="ceo_title")
    private String ceoTitle;

    @Column(name="ceo_name")
    private String ceoName;

    @Column(name="contact")
    private String contactEmail;

    @Transient
    private String glaContactFullName;

    @Column(name="primary_contact_first_name")
    private String primaryContactFirstName;

    @Column(name="primary_contact_last_name")
    private String primaryContactLastName;

    @Column(name="primary_contact_email")
    private String primaryContactEmail;

    @Column(name="primary_contact_number")
    private String primaryContactNumber;

    @Column(name="entity_type")
    private Integer entityType = OrganisationType.OTHER.id();

    @Column(name="regulated")
    private Boolean regulated = false;

    @Column(name="viability")
    private String viability;

    @Column(name="governance")
    private String governance;

    @Column(name="sap_vendor_id")
    @NonJoin("This is the id of the vendor in SAP, an external system to OPS")
    private String sapVendorId;

    @Column(name="ukprn")
    private Integer ukprn;

    @Column(name="registration_allowed")
    private Boolean registrationAllowed;

    @Embedded
    private Address address;


    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    private Organisation managingOrganisation;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(cascade = {})
    @JoinColumn(name = "parent_organisation_id")
    private Organisation parentOrganisation;


    @Column(name = "duplicate_organisation_id")
    private Integer duplicateOrganisationId;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = {}) // do not change to EAGER as it will destroy org list loading performance
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "organisation_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "username", referencedColumnName = "username"))
    private List<User> userEntities;

    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "org_id", targetTable = "project",
            comment = "")
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "organisation", targetEntity = Project.class)
    @OrderBy("createdOn DESC" )
    private List<Project> projects;

    @Column(name="user_reg_status")
    @Enumerated(EnumType.STRING)
    private RegistrationStatus userRegStatus;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = ALL, orphanRemoval = true, targetEntity = OrganisationContract.class)
    @JoinColumn(name = "organisation_id", nullable = false)
    private List<OrganisationContract> contractEntities = new ArrayList<>();

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private OrganisationStatus status = OrganisationStatus.Pending;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "team_id")
    private Team team;


    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "organisation_id", targetTable = "team",  comment = "")
    @OneToMany(fetch = FetchType.LAZY,  cascade = ALL, orphanRemoval = true, mappedBy = "organisation", targetEntity = Team.class)
    private Set<Team> managedTeams = new HashSet<>();

    @Transient
    private List<UserModel> users;

    @Transient
    private Set<ContractModel> contracts;

    @Transient
    private List<OrganisationProgrammeSummary> programmes;

    @Transient
    private List<AnnualSubmission> annualSubmissions;

    @Transient
    private Set<OrganisationAction> allowedActions = new HashSet<>();

    public Organisation() {
        // Empty
    }

    public Organisation(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Organisation IDs start with 10000 (GLA) and go up from there.
     *
     * @return
     */
    public Integer getId() {
        return id;
    }

    @Override
    public String getIdAsString() {
        return id != null ? id.toString() : null;
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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    /**
     * Returns the IMS number for the organisation.
     *
     * IMS is one of the legacy GLA systems and this code is used
     * to link the organisation in OPS to the organisation record in IMS.
     */
    public String getImsNumber() {
        return imsNumber;
    }

    public void setImsNumber(String imsNumber) {
        this.imsNumber = imsNumber;
    }

    public String getRegistrationKey() {
        return registrationKey;
    }

    public void setRegistrationKey(String registrationKey) {
        this.registrationKey = registrationKey;
    }

    public void populateRegistrationKey() {
        setRegistrationKey(generateRandomId());
    }


    public OffsetDateTime getApprovedOn() {
        return approvedOn;
    }

    public void setApprovedOn(OffsetDateTime approvedOn) {
        this.approvedOn = approvedOn;
    }



    public OffsetDateTime getRejectedOn() {
        return rejectedOn;
    }

    public void setRejectedOn(OffsetDateTime rejectedOn) {
        this.rejectedOn = rejectedOn;
    }


    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCeoTitle() {
        return ceoTitle;
    }

    public void setCeoTitle(String ceoTitle) {
        this.ceoTitle = ceoTitle;
    }

    public String getCeoName() {
        return ceoName;
    }

    public void setCeoName(String ceoName) {
        this.ceoName = ceoName;
    }

    public String getPrimaryContactFirstName() {
        return primaryContactFirstName;
    }

    public void setPrimaryContactFirstName(String primaryContactFirstName) {
        this.primaryContactFirstName = primaryContactFirstName;
    }

    public String getPrimaryContactLastName() {
        return primaryContactLastName;
    }

    public void setPrimaryContactLastName(String primaryContactLastName) {
        this.primaryContactLastName = primaryContactLastName;
    }

    public String getPrimaryContactEmail() {
        return primaryContactEmail;
    }

    public void setPrimaryContactEmail(String primaryContactEmail) {
        this.primaryContactEmail = primaryContactEmail;
    }

    public String getPrimaryContactNumber() {
        return primaryContactNumber;
    }

    public void setPrimaryContactNumber(String primaryContactNumber) {
        this.primaryContactNumber = primaryContactNumber;
    }

    public Integer getEntityType() {
        return entityType;
    }

    public void setEntityType(Integer entityType) {
        this.entityType = entityType;
    }

    public boolean isRegulated() {
        return Boolean.TRUE.equals(regulated);
    }

    public void setRegulated(boolean regulated) {
        this.regulated = regulated;
    }

    public String getViability() {
        return viability;
    }

    public void setViability(String viability) {
        this.viability = viability;
    }

    public String getGovernance() {
        return governance;
    }

    public void setGovernance(String governance) {
        this.governance = governance;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setSapVendorId(String sapVendorId) {
        this.sapVendorId = sapVendorId;
    }

    public String getsapVendorId() {
        return sapVendorId;
    }

    public Integer getUkprn() {
        return ukprn;
    }

    public void setUkprn(Integer ukprn) {
        this.ukprn = ukprn;
    }

    public Organisation getManagingOrganisation() {
        return managingOrganisation;
    }

    public void setManagingOrganisation(Organisation managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
    }

    public Organisation getParentOrganisation() {
        return parentOrganisation;
    }

    public void setParentOrganisation(Organisation parentOrganisation) {
        this.parentOrganisation = parentOrganisation;
    }

    public Integer getParentOrganisationId() {
        return parentOrganisation == null ? null : parentOrganisation.getId();
    }

    public String getParentOrganisationName() {
        return parentOrganisation == null ? null : parentOrganisation.getName();
    }

    public Integer getDuplicateOrganisationId() {
        return duplicateOrganisationId;
    }

    public void setDuplicateOrganisationId(Integer duplicateOrganisationId) {
        this.duplicateOrganisationId = duplicateOrganisationId;
    }

    /**
     * Returns the registration status of the users in the organisation.
     *
     * Approved - if all user registrations are approved
     * Pending - if any user registrations are pending
     *
     * If there are no user registrations then it will be blank
     */
    public RegistrationStatus getUserRegStatus() {
        return userRegStatus;
    }

    public void setUserRegStatus(RegistrationStatus userRegStatus) {
        this.userRegStatus = userRegStatus;
    }

    public List<User> getUserEntities() {
        return userEntities;
    }

    public void setUserEntities(List<User> userEntities) {
        this.userEntities = userEntities;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<UserModel> getUsers() {
        return users;
    }

    public void setUsers(List<UserModel> users) {
        this.users = users;
    }

    public List<OrganisationContract> getContractEntities() {
        return contractEntities;
    }

    public void setContractEntities(List<OrganisationContract> contractEntities) {
        this.contractEntities = contractEntities;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getGlaContactFullName() {
        return glaContactFullName;
    }

    public void setGlaContactFullName(String glaContactFullName) {
        this.glaContactFullName = glaContactFullName;
    }

    public OrganisationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganisationStatus status) {
        this.status = status;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Set<Team> getManagedTeams() {
        return managedTeams;
    }

    public void setManagedTeams(Set<Team> managedTeams) {
        this.managedTeams = managedTeams;
    }

    public void addManagedTeam(Team team) {
        if (managedTeams == null) {
            managedTeams = new HashSet<>();
        }
        team.setOrganisation(this);
        this.managedTeams.add(team);
    }

    public Set<ContractModel> getContracts() {
        return contracts;
    }

    public void setContracts(Set<ContractModel> contracts) {
        this.contracts = contracts;
    }

    public List<OrganisationProgrammeSummary> getProgrammes() {
        return programmes;
    }

    public void setProgrammes(List<OrganisationProgrammeSummary> programmes) {
        this.programmes = programmes;
    }

    public List<AnnualSubmission> getAnnualSubmissions() {
        return annualSubmissions;
    }

    public void setAnnualSubmissions(List<AnnualSubmission> annualSubmissions) {
        this.annualSubmissions = annualSubmissions;
    }

    public Set<OrganisationAction> getAllowedActions() {
        return allowedActions;
    }


    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(String rejectedBy) {
        this.rejectedBy = rejectedBy;
    }


    public OrganisationChangeStatusReason getChangeStatusReason() {
        return changeStatusReason;
    }

    public void setChangeStatusReason(OrganisationChangeStatusReason changeStatusReason) {
        this.changeStatusReason = changeStatusReason;
    }

    public String getChangeStatusReasonDetails() {
        if (StringUtils.isNotEmpty(changeStatusReasonDetails)) {
            return changeStatusReasonDetails;
        }
        else if (changeStatusReason != null && changeStatusReason == OrganisationChangeStatusReason.Duplicate) {
            return "Organisation is duplicate of " + duplicateOrganisationId;
        }
        else if (changeStatusReason != null) {
            return changeStatusReason.getDescription();
        }
        else {
            return null;
        }
    }

    public void setChangeStatusReasonDetails(String changeStatusReasonDetails) {
        this.changeStatusReasonDetails = changeStatusReasonDetails;
    }

    public String getInactivatedBy() {
        return inactivatedBy;
    }

    public void setInactivatedBy(String inactivatedBy) {
        this.inactivatedBy = inactivatedBy;
    }

    public OffsetDateTime getInactivatedOn() {
        return inactivatedOn;
    }

    public void setInactivatedOn(OffsetDateTime inactivatedOn) {
        this.inactivatedOn = inactivatedOn;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByByName) {
        this.approvedByName = approvedByByName;
    }

    public String getRejectedByName() {
        return rejectedByName;
    }

    public void setRejectedByName(String rejectedByName) {
        this.rejectedByName = rejectedByName;
    }

    public String getInactivatedByName() {
        return inactivatedByName;
    }

    public void setInactivatedByName(String inactivatedByName) {
        this.inactivatedByName = inactivatedByName;
    }

    public void setAllowedActions(Set<OrganisationAction> allowedActions) {
        this.allowedActions = allowedActions;
    }

    public boolean isPendingContractSignature(Contract contract, OrganisationGroup.Type orgGroupType) {
        for (OrganisationContract entity: contractEntities) {
            if (entity.matches(contract, orgGroupType)) {
                return entity.isPendingSignature();
            }
        }

        return true;
    }

    public boolean isManagingOrganisation() {
        return entityType != null && entityType == OrganisationType.MANAGING_ORGANISATION.id();
    }

    public boolean isTechSupportOrganisation() {
        return entityType != null && entityType == OrganisationType.TECHNICAL_SUPPORT.id();
    }

    public boolean isApproved() {
        return OrganisationStatus.Approved.equals(status);
    }

    public boolean isRejected() {
        return OrganisationStatus.Rejected.equals(status);
    }

    public boolean isInactive() {
        return OrganisationStatus.Inactive.equals(status);
    }

    public Set<User> getUsers(String ... roles) {
        Set<User> users = new HashSet<>();
        if (userEntities != null) {
            for (User user: userEntities) {
                for (String role: roles) {
                    if (user.getRole(this).getName().equals(role)) {
                        users.add(user);
                    }
                }
            }
        }
        return users;
    }

    public Set<String> getUsernames(String ... roles) {
        return getUsers(roles).stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());
    }

    public Boolean getRegistrationAllowed() {
        return registrationAllowed;
    }

    public void setRegistrationAllowed(Boolean registrationAllowed) {
        this.registrationAllowed = registrationAllowed;
    }

    public boolean isCorporateOrganisation() {
        return name != null && name.equalsIgnoreCase("GLA Corporate Governance");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Organisation that = (Organisation) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public void changeStatus(OrganisationStatus status, String username, OffsetDateTime dateTime) {
        switch (status) {
            case Approved:
                setApprovedBy(username);
                setApprovedOn(dateTime);
                break;
            case Rejected:
                setRejectedBy(username);
                setRejectedOn(dateTime);
                break;
            case Inactive:
                setInactivatedBy(username);
                setInactivatedOn(dateTime);
                break;
        }
        this.setStatus(status);
    }

    @Override
    @JsonIgnore
    public OrganisationType getType() {
        return OrganisationType.fromId(entityType);
    }

    @Override
    public void setType(OrganisationType organisationType) {
        setEntityType(organisationType.id());
    }

    public boolean isAnnualReturnsEnabled() {
        return getType() != null && getType().isAnnualReturnsEnabled();
    }


}
