/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.subcontracting;

import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "subcontractor")
public class Subcontractor {

    public enum IdentifierType { UKPRN, ORG_ID }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subcontractor_seq_gen")
    @SequenceGenerator(name = "subcontractor_seq_gen", sequenceName = "subcontractor_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "identifier_type")
    private IdentifierType identifierType = IdentifierType.UKPRN;

    @Column(name = "identifier")
    private Integer identifier;

    @Column(name = "organisation_name")
    private String organisationName;

    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "subcontractor_id")
    @JoinData(sourceTable = "subcontractor", targetTable = "deliverable", joinType = Join.JoinType.OneToMany, comment = "subcontractor can have many deliverables")
    private Set<Deliverable> deliverables = new HashSet<>();

    public Subcontractor() {
    }

    public Subcontractor(String organisationName) {
        this.organisationName = organisationName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public IdentifierType getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(IdentifierType identifierType) {
        this.identifierType = identifierType;
    }

    public Integer getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Integer identifier) {

        this.identifier = identifier;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public Set<Deliverable> getDeliverables() {
        return deliverables;
    }

    public void setDeliverables(Set<Deliverable> deliverables) {
        this.deliverables = deliverables;
    }

    public BigDecimal getContractValue() {
        return deliverables.stream()
                .filter(p -> p.getValue() != null)
                .map(Deliverable::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
