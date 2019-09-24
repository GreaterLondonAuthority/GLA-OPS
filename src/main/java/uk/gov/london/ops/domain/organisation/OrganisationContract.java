/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.organisation;

import uk.gov.london.ops.domain.template.Contract;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity(name="organisation_contract")
public class OrganisationContract implements Serializable {

    public enum Status { Blank, Signed, NotRequired }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organisation_contract_seq_gen")
    @SequenceGenerator(name = "organisation_contract_seq_gen", sequenceName = "organisation_contract_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @Column
    @Enumerated(EnumType.STRING)
    private Status status = Status.Blank;

    @Column
    @Enumerated(EnumType.STRING)
    private OrganisationGroup.Type orgGroupType;

    @Column(name="created_by")
    private String createdBy;

    @Column(name="created_on")
    private OffsetDateTime createdOn;

    @Column(name="modified_by")
    private String modifiedBy;

    @Column(name="modified_on")
    private OffsetDateTime modifiedOn;

    public OrganisationContract() {}

    public OrganisationContract(Contract contract, Status status, OrganisationGroup.Type orgGroupType) {
        this.contract = contract;
        this.status = status;
        this.orgGroupType = orgGroupType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public OrganisationGroup.Type getOrgGroupType() {
        return orgGroupType;
    }

    public void setOrgGroupType(OrganisationGroup.Type orgGroupType) {
        this.orgGroupType = orgGroupType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public boolean matches(Contract contract, OrganisationGroup.Type orgGroupType) {
        return this.contract.equals(contract) && Objects.equals(this.orgGroupType, orgGroupType);
    }

    public boolean isPendingSignature() {
        return status == null || status.equals(OrganisationContract.Status.Blank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganisationContract that = (OrganisationContract) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(contract, that.contract) &&
                orgGroupType == that.orgGroupType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, contract, orgGroupType);
    }

}
