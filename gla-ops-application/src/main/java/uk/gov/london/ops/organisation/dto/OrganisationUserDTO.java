/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.dto;

import uk.gov.london.ops.organisation.OrganisationType;
import uk.gov.london.ops.organisation.SapId;
import uk.gov.london.ops.organisation.model.Address;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.user.domain.UserRegistration;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

public class OrganisationUserDTO extends OrganisationModel {

    private Integer entityType = OrganisationType.OTHER.getId();

    private Set<SapId> sapIds = new HashSet<>();

    private Boolean isLearningProvider = false;
    private Integer ukprn;

    private String website;

    private String contactNumber;

    private Address address;

    private Boolean regulated = false;

    private String contactEmail;

    private UserRegistration userRegistration;

    private String email;

    private String ceoTitle;

    private String ceoName;

    private Integer defaultProgrammeId;

    private String companyCode;

    private String vatNumber;

    private String sortCode;

    private String bankAccount;

    private String providerNumber;

    private OrganisationEntity team;

    private OrganisationEntity parentOrganisation;

    private String viability;

    private String governance;

    private String knownAs;
    private String societyNumber;

    private Boolean isCharityCommission = false;
    private String charityNumber;

    public Integer getEntityType() {
        return entityType;
    }

    public void setEntityType(Integer entityType) {
        this.entityType = entityType;
    }

    public Set<SapId> getSapIds() {
        return sapIds;
    }

    public void setSapIds(Set<SapId> sapIds) {
        this.sapIds = sapIds;
    }

    // TODO : remove getter and setter below
    public String getSapVendorId() {
        if (sapIds != null && !sapIds.isEmpty()) {
            return sapIds.iterator().next().getSapId();
        }
        return null;
    }

    public void setSapVendorId(String sapVendorId) {
        if (sapVendorId != null) {
            sapIds.clear();
            sapIds.add(new SapId(sapVendorId, null, null, OffsetDateTime.now(), true));
        }
    }

    public Integer getUkprn() {
        return ukprn;
    }

    public void setUkprn(Integer ukprn) {
        this.ukprn = ukprn;
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Boolean getRegulated() {
        return regulated;
    }

    public void setRegulated(Boolean regulated) {
        this.regulated = regulated;
    }

    public UserRegistration getUserRegistration() {
        return userRegistration;
    }

    public void setUserRegistration(UserRegistration userRegistration) {
        this.userRegistration = userRegistration;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
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

    public Integer getDefaultProgrammeId() {
        return defaultProgrammeId;
    }

    public void setDefaultProgrammeId(Integer defaultProgrammeId) {
        this.defaultProgrammeId = defaultProgrammeId;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getSortCode() {
        return sortCode;
    }

    public void setSortCode(String sortCode) {
        this.sortCode = sortCode;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getProviderNumber() {
        return providerNumber;
    }

    public void setProviderNumber(String providerNumber) {
        this.providerNumber = providerNumber;
    }

    public OrganisationEntity getTeam() {
        return team;
    }

    public void setTeam(OrganisationEntity team) {
        this.team = team;
    }

    public OrganisationEntity getParentOrganisation() {
        return parentOrganisation;
    }

    public void setParentOrganisation(OrganisationEntity parentOrganisation) {
        this.parentOrganisation = parentOrganisation;
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

    public Boolean getIsLearningProvider() {
        return isLearningProvider;
    }

    public void setIsLearningProvider(Boolean learningProvider) {
        isLearningProvider = learningProvider;
    }

    public String getKnownAs() {
        return knownAs;
    }

    public void setKnownAs(String knownAs) {
        this.knownAs = knownAs;
    }

    public String getSocietyNumber() {
        return societyNumber;
    }

    public void setSocietyNumber(String societyNumber) {
        this.societyNumber = societyNumber;
    }

    public Boolean getIsCharityCommission() {
        return isCharityCommission;
    }

    public void setIsCharityCommission(Boolean charityCommission) {
        isCharityCommission = charityCommission;
    }

    public String getCharityNumber() {
        return charityNumber;
    }

    public void setCharityNumber(String charityNumber) {
        this.charityNumber = charityNumber;
    }
}
