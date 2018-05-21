/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.organisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.template.Contract;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.service.ManagedEntityInterface;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;
import uk.gov.london.ops.util.jpajoins.NonJoin;
import uk.gov.london.ops.web.model.ContractModel;
import uk.gov.london.ops.web.model.UserModel;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
public class Organisation implements Serializable, ManagedEntityInterface {

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

    @Column(name="email")
    private String email;

    @Column(name="ceo_title")
    private String ceoTitle;

    @Column(name="ceo_name")
    private String ceoName;

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
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = OrganisationContract.class)
    @JoinColumn(name = "organisation_id", nullable = false)
    private List<OrganisationContract> contractEntities = new ArrayList<>();

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private OrganisationStatus status = OrganisationStatus.Pending;

    @Transient
    private List<UserModel> users;

    @Transient
    private Set<ContractModel> contracts;

    @Transient
    private List<OrganisationProgrammeSummary> programmes;

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

    public OrganisationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganisationStatus status) {
        this.status = status;
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

    public List<User> getUsers(String ... roles) {
        List<User> users = new ArrayList<>();
        for (User user: userEntities) {
            for (String role: roles) {
                if (user.getRole(this).getName().equals(role)) {
                    users.add(user);
                }
            }
        }
        return users;
    }

    public List<String> getUsernames(String ... roles) {
        return getUsers(roles).stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
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
}
