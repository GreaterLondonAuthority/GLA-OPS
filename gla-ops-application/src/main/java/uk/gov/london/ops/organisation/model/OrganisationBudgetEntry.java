/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.grant.GrantType;
import uk.gov.london.ops.user.domain.User;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity(name = "organisation_budget_entry")
public class OrganisationBudgetEntry implements OpsEntity<Integer> {

    public enum Type {
        Initial, Additional
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organisation_budget_entry_seq_gen")
    @SequenceGenerator(name = "organisation_budget_entry_seq_gen", sequenceName = "organisation_budget_entry_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "organisation_id")
    @JoinData(targetTable = "organisation", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The organisation this budget item relates to")
    private Integer organisationId;

    @Column(name = "programme_id")
    @JoinData(targetTable = "programme", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The programme this budget item relates to")
    private Integer programmeId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "grant_type")
    @Enumerated(EnumType.STRING)
    private GrantType grantType;

    @Column(name = "strategic")
    private boolean strategic;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "created_by")
    private User creator;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_on")
    private LocalDate approvedOn;

    @Column(name = "comments")
    private String comments;

    public OrganisationBudgetEntry() {}

    public OrganisationBudgetEntry(Integer organisationId, Integer programmeId, BigDecimal amount, Type type, GrantType grantType) {
        this(organisationId, programmeId, amount, type, grantType, false);
    }

    public OrganisationBudgetEntry(Integer organisationId, Integer programmeId, BigDecimal amount, Type type, GrantType grantType,
                                   boolean strategic) {
        this.organisationId = organisationId;
        this.programmeId = programmeId;
        this.amount = amount;
        this.type = type;
        this.grantType = grantType;
        this.strategic = strategic;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Integer organisationId) {
        this.organisationId = organisationId;
    }

    public Integer getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(Integer programmeId) {
        this.programmeId = programmeId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public GrantType getGrantType() {
        return grantType;
    }

    public void setGrantType(GrantType grantType) {
        this.grantType = grantType;
    }

    public boolean isStrategic() {
        return strategic;
    }

    public void setStrategic(boolean strategic) {
        this.strategic = strategic;
    }

    @Override
    public String getCreatedBy() {
        return creator != null ? creator.getUsername() : null;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.creator = new User(createdBy);
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

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDate getApprovedOn() {
        return approvedOn;
    }

    public void setApprovedOn(LocalDate approvedOn) {
        this.approvedOn = approvedOn;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getCreatorName() {
        return creator != null ? creator.getFullName() : null;
    }

}
