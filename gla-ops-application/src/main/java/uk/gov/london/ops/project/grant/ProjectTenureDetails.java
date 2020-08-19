/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.grant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.framework.ComparableItem;
import uk.gov.london.ops.project.template.domain.TemplateTenureType;

/**
 * Created by chris on 13/10/2016.
 */
@Entity(name = "tenure_and_units")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectTenureDetails implements ComparableItem {
    // TODO: Rename ProjectTenureDetails to ProjectTenureDetails

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tenure_and_units_seq_gen")
    @SequenceGenerator(name = "tenure_and_units_seq_gen", sequenceName = "tenure_and_units_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "total_units")
    private Integer totalUnits;

    @Column(name = "s106_units")
    private Integer s106Units;

    @Column(name = "total_cost")
    private Long totalCost;

    @Column(name = "supported_units")
    private Integer supportedUnits; // for exception block

    @Column(name = "grant_requested")
    private Long grantRequested; // for exception block

    @Column(name = "additional_units")
    private Integer additionalAffordableUnits; // for developer-led grant block

    @Column(name = "eligible_units")
    private Integer eligibleUnits;

    // TODO:  private Integer tariffRate;
    @Column(name = "grant_per_unit")
    private Integer grantPerUnit;

    @Column(name = "eligible_grant")
    private Long eligibleGrant;

    @Column(name = "total_units_at_sos")
    private Integer totalUnitsAtStartOnSite;

    @Column(name = "total_units_at_completion")
    private Integer totalUnitsAtCompletion;

    @JsonIgnore
    @OneToOne
    @JoinData(targetTable = "project", targetColumn = "id", comment = "join to relevant project")
    @JoinColumn(name = "project_id")
    private Project project; // related project ID for this tenure row

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = IndicativeTenureValue.class)
    @JoinColumn(name = "tenure_units_id")
    private Set<IndicativeTenureValue> indicativeTenureValues = new HashSet<>();

    // TODO: Switch tenureType field from TemplateTenureType to TenureType
    @OneToOne(cascade = {})
    @JoinColumn(name = "tenure_type_id", referencedColumnName = "id")
    private TemplateTenureType tenureType;

    public ProjectTenureDetails() {
    }

    public ProjectTenureDetails(Project project) {
        this.project = project;
    }

    public BigDecimal getPercentageOfTotalCost() {
        BigDecimal response = null;

        if (grantRequested != null && totalCost != null) {
            if (grantRequested > 0 && totalCost > 0) {
                response = new BigDecimal(grantRequested / ((double) totalCost) * 100).setScale(1, BigDecimal.ROUND_HALF_UP);
            }
        }
        return response;
    }


    public Integer getId() {
        return id;
    }

    public Integer getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(Integer totalUnits) {
        this.totalUnits = totalUnits;
    }

    public Integer getS106Units() {
        return s106Units;
    }

    public void setS106Units(Integer s106Units) {
        this.s106Units = s106Units;
    }

    public Long getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Long totalCost) {
        this.totalCost = totalCost;
    }

    public TemplateTenureType getTenureType() {
        return tenureType;
    }

    public void setTenureType(TemplateTenureType tenureType) {
        this.tenureType = tenureType;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSupportedUnits() {
        return supportedUnits;
    }

    public void setSupportedUnits(Integer supportedUnits) {
        this.supportedUnits = supportedUnits;
    }

    public Long getGrantRequested() {
        return grantRequested;
    }

    public void setGrantRequested(Long grantRequested) {
        this.grantRequested = grantRequested;
    }

    public Integer getAdditionalAffordableUnits() {
        return additionalAffordableUnits;
    }

    public void setAdditionalAffordableUnits(Integer additionalAffordableUnits) {
        this.additionalAffordableUnits = additionalAffordableUnits;
    }

    @JsonIgnore
    public Set<IndicativeTenureValue> getIndicativeTenureValues() {
        return indicativeTenureValues;
    }

    public void setIndicativeTenureValues(Set<IndicativeTenureValue> values) {
        this.indicativeTenureValues = values;
    }

    @Transient
    public List<IndicativeTenureValue> getIndicativeTenureValuesSorted() {
        if (this.indicativeTenureValues == null || indicativeTenureValues.size() == 0) {
            return null; // to prevent field showing in JSON for not applicable types
        }
        return this.indicativeTenureValues.stream().sorted().collect(Collectors.toList());
    }

    public void setIndicativeTenureValuesSorted(List<IndicativeTenureValue> values) {
        this.indicativeTenureValues.clear();
        this.indicativeTenureValues.addAll(values);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Integer getEligibleUnits() {
        return eligibleUnits;
    }

    public void setEligibleUnits(Integer eligibleUnits) {
        this.eligibleUnits = eligibleUnits;
    }

    public Integer getGrantPerUnit() {
        return grantPerUnit;
    }

    public void setGrantPerUnit(Integer tariffRate) {
        this.grantPerUnit = tariffRate;
    }

    public Long getEligibleGrant() {
        return eligibleGrant;
    }

    public void setEligibleGrant(Long eligibleGrant) {
        this.eligibleGrant = eligibleGrant;
    }


    public Integer getTotalUnitsAtStartOnSite() {
        return totalUnitsAtStartOnSite;
    }

    public void setTotalUnitsAtStartOnSite(Integer totalUnitsAtStartOnSite) {
        this.totalUnitsAtStartOnSite = totalUnitsAtStartOnSite;
    }

    public Integer getTotalUnitsAtCompletion() {
        return totalUnitsAtCompletion;
    }

    public void setTotalUnitsAtCompletion(Integer totalUnitsAtCompletion) {
        this.totalUnitsAtCompletion = totalUnitsAtCompletion;
    }

    public ProjectTenureDetails copy() {
        ProjectTenureDetails clone = new ProjectTenureDetails();
        clone.setTotalUnits(this.totalUnits);
        clone.setS106Units(this.s106Units);
        clone.setTotalCost(this.totalCost);
        clone.setSupportedUnits(this.supportedUnits);
        clone.setGrantRequested(this.grantRequested);
        clone.setAdditionalAffordableUnits(this.additionalAffordableUnits);
        clone.setEligibleUnits(this.eligibleUnits);
        clone.setGrantPerUnit(this.grantPerUnit);
        clone.setEligibleGrant(this.eligibleGrant);
        clone.setProject(this.project);
        clone.setTenureType(this.tenureType);
        clone.setTotalUnitsAtCompletion(this.totalUnitsAtCompletion);
        clone.setTotalUnitsAtStartOnSite(this.totalUnitsAtStartOnSite);
        clone.setIndicativeTenureValues(
                this.indicativeTenureValues.stream().map(IndicativeTenureValue::copy).collect(Collectors.toSet()));
        return clone;
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(tenureType.getExternalId());
    }
}
