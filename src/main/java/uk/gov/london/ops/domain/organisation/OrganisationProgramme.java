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
import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.domain.ProgrammeOrganisationID;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.util.GlaOpsUtils;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static uk.gov.london.ops.domain.organisation.OrganisationBudgetEntry.Type.Initial;
import static uk.gov.london.ops.domain.project.GrantType.DPF;
import static uk.gov.london.ops.domain.project.GrantType.Grant;
import static uk.gov.london.ops.domain.project.GrantType.RCGF;

@Entity(name = "organisation_programme")
public class OrganisationProgramme implements OpsEntity<ProgrammeOrganisationID> {

    @EmbeddedId
    @JsonIgnore
    private ProgrammeOrganisationID id;

    @Column(name = "is_strategic_partnership")
    private boolean strategicPartnership;

    @Column(name="created_by", updatable = false)
    private String createdBy;

    @Column(name="created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name="modified_by")
    private String modifiedBy;

    @Column(name="modified_on")
    private OffsetDateTime modifiedOn;

    @Transient
    private List<OrganisationBudgetEntry> budgetEntries;

    @Transient
    private Programme programme;

    @Override
    public ProgrammeOrganisationID getId() {
        return id;
    }

    public void setId(ProgrammeOrganisationID id) {
        this.id = id;
    }

    public boolean isStrategicPartnership() {
        return strategicPartnership;
    }

    public void setStrategicPartnership(boolean strategicPartnership) {
        this.strategicPartnership = strategicPartnership;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    public List<OrganisationBudgetEntry> getBudgetEntries() {
        return budgetEntries;
    }

    public void setBudgetEntries(List<OrganisationBudgetEntry> budgetEntries) {
        this.budgetEntries = budgetEntries;
    }

    public void setProgramme(Programme programme) {
        this.programme = programme;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Set<String> getGrantTypes() {
        return programme != null ? programme.getGrantTypes() : null;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean hasIndicativeTemplate() {
        return programme != null && programme.hasIndicativeTemplate();
    }

    public Map<String, BigDecimal> getTotals() {
        HashMap<String, BigDecimal> totals = new HashMap<>();
        totals.put("initialNonStrategicTotal",  sum(e -> !e.isStrategic() && Initial.equals(e.getType())));
        totals.put("initialStrategicTotal",     sum(e -> e.isStrategic() && Initial.equals(e.getType())));
        totals.put("initialTotal",              sum(e -> Initial.equals(e.getType())));
        totals.put("nonStrategicGrantTotal",    sum(e -> !e.isStrategic() && Grant.equals(e.getGrantType())));
        totals.put("nonStrategicRCGFTotal",     sum(e -> !e.isStrategic() && RCGF.equals(e.getGrantType())));
        totals.put("nonStrategicDPFTotal",      sum(e -> !e.isStrategic() && DPF.equals(e.getGrantType())));
        totals.put("strategicGrantTotal",       sum(e -> e.isStrategic() && Grant.equals(e.getGrantType())));
        totals.put("strategicRCGFTotal",        sum(e -> e.isStrategic() && RCGF.equals(e.getGrantType())));
        totals.put("strategicDPFTotal",         sum(e -> e.isStrategic() && DPF.equals(e.getGrantType())));
        totals.put("nonStrategicTotal",         sum(e -> !e.isStrategic()));
        totals.put("strategicTotal",            sum(e -> e.isStrategic()));
        totals.put("total",                     sum(budgetEntries.stream()));
        return totals;
    }

    private BigDecimal sum(Predicate<OrganisationBudgetEntry> predicate) {
        return sum(budgetEntries.stream().filter(predicate));
    }

    private BigDecimal sum(Stream<OrganisationBudgetEntry> stream) {
        return stream.map(OrganisationBudgetEntry::getAmount).reduce(BigDecimal.ZERO, GlaOpsUtils::addBigDecimals);
    }

}
