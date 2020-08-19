/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.service.ManagedEntityInterface;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a consortium or a partnership. Essentially a set of organisations and a programme.
 */
@Entity
public class OrganisationGroup implements OpsEntity<Integer>, ManagedEntityInterface {

    public enum Type {
        Consortium,
        Partnership
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organisation_group_seq_gen")
    @SequenceGenerator(name = "organisation_group_seq_gen", sequenceName = "organisation_group_seq", initialValue = 10000,
            allocationSize = 1)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "programme_id")
    private Programme programme;

    @Column(name = "lead_organisation_id")
    @JoinData(targetTable = "organisation", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The lead organisation for this consortium/partnership")
    private Integer leadOrganisationId;

    @ManyToMany
    @JoinTable(name = "organisation_group_organisation",
            joinColumns = @JoinColumn(name = "organisation_group_id", referencedColumnName = "id"),
                inverseJoinColumns = @JoinColumn(name = "organisation_id", referencedColumnName = "id"))
    private Set<Organisation> organisations;


    @JoinData(joinType = Join.JoinType.ManyToOne, sourceTable = "organisation_group", targetColumn = "username",
            targetTable = "users",
            comment = "")
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    private Organisation managingOrganisation;

    public OrganisationGroup() {}

    public OrganisationGroup(Integer id, String name, Type type) {
        this.id = id;
        this.name = name;
        this.type = type;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Programme getProgramme() {
        return programme;
    }

    public void setProgramme(Programme programme) {
        this.programme = programme;
    }

    public Integer getLeadOrganisationId() {
        return leadOrganisationId;
    }

    public void setLeadOrganisationId(Integer leadOrganisationId) {
        this.leadOrganisationId = leadOrganisationId;
    }

    public Set<Organisation> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(Set<Organisation> organisations) {
        this.organisations = organisations;
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

    @Override
    public Organisation getManagingOrganisation() {
        return managingOrganisation;
    }

    public void setManagingOrganisation(Organisation managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
    }

    @JsonIgnore
    public List<Integer> getAllOrganisationIds() {
        List<Integer> ids = new ArrayList<>();
        ids.add(leadOrganisationId);
        ids.addAll(organisations.stream().map(Organisation::getId).collect(Collectors.toList()));
        return ids;
    }

    public Organisation getLeadOrganisation() {
        for (Organisation org: organisations) {
            if (org.getId().equals(leadOrganisationId)) {
                return org;
            }
        }
        return null;
    }

    public boolean isConsortium() {
        return Type.Consortium.equals(type);
    }

    public boolean isPartnership() {
        return Type.Partnership.equals(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrganisationGroup that = (OrganisationGroup) o;
        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
