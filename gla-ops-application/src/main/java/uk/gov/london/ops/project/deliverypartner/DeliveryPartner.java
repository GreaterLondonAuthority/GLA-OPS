/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.deliverypartner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import org.springframework.util.StringUtils;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.ComparableItem;
import uk.gov.london.ops.project.block.ProjectDifference;

@Entity(name = "delivery_partner")
public class DeliveryPartner implements ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subcontractor_seq_gen")
    @SequenceGenerator(name = "subcontractor_seq_gen", sequenceName = "subcontractor_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "original_id")
    @JoinData(targetTable = "delivery_partner", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "A reference to a previous version of this item if this is a cloned via a new block version.")
    private Integer originalId;

    @Column(name = "identifier")
    private Integer identifier;

    @Column(name = "organisation_name")
    private String organisationName;

    @Column(name = "organisation_type")
    private String organisationType;

    @Column(name = "role")
    private String role;

    @Column(name = "contract_value")
    private BigDecimal contractValue;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "delivery_partner_id")
    @JoinData(sourceTable = "delivery_partner", targetTable = "deliverable",
            joinType = Join.JoinType.OneToMany, comment = "delivery partner can have many deliverables")
    private Set<Deliverable> deliverables = new HashSet<>();

    public DeliveryPartner() {
    }

    public DeliveryPartner(String organisationName) {
        this.organisationName = organisationName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOriginalId() {
        if (originalId == null) {
            return id;
        }
        return originalId;
    }

    public void setOriginalId(Integer originalId) {
        this.originalId = originalId;
    }

    public Integer getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Integer identifier) {

        this.identifier = identifier;
    }

    public String getOrganisationType() {
        return organisationType;
    }

    public void setOrganisationType(String organisationType) {
        this.organisationType = organisationType;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public BigDecimal getContractValue() {
        return contractValue;
    }

    public void setContractValue(BigDecimal contractValue) {
        this.contractValue = contractValue;
    }

    public Set<Deliverable> getDeliverables() {
        return deliverables;
    }

    public void setDeliverables(Set<Deliverable> deliverables) {
        this.deliverables = deliverables;
    }

    public BigDecimal getDeliverableContractValue() {
        return deliverables.stream()
                .filter(p -> p.getValue() != null)
                .map(Deliverable::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public DeliveryPartner copy() {
        DeliveryPartner copy = new DeliveryPartner();
        copy.setOriginalId(getOriginalId());
        copy.setIdentifier(getIdentifier());
        copy.setOrganisationName(getOrganisationName());
        copy.setOrganisationType(getOrganisationType());
        copy.setRole(getRole());
        copy.setContractValue(getContractValue());
        copy.setDeliverables(getDeliverables());
        return copy;
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(getOriginalId());
    }

    List<ProjectDifference> compareWith(DeliveryPartner deliveryPartner) {
        List<ProjectDifference> differences = new ArrayList<>();

        if (!Objects.equals(StringUtils.trimAllWhitespace(this.getOrganisationName()),
                StringUtils.trimAllWhitespace(deliveryPartner.getOrganisationName()))) {
            differences.add(new ProjectDifference(this, "organisationName"));
        }

        if (!Objects.equals(this.getIdentifier(), deliveryPartner.getIdentifier())) {
            differences.add(new ProjectDifference(this, "identifier"));
        }

        if (!Objects.equals(this.getDeliverableContractValue(), deliveryPartner.getDeliverableContractValue())) {
            differences.add(new ProjectDifference(this, "deliverableContractValue"));
        }

        return differences;
    }
}
