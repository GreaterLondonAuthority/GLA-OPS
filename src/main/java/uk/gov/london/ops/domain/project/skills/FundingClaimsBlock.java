/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.skills;

import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.domain.template.*;
import uk.gov.london.ops.payment.FundingClaimsVariation;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity(name = "funding_claims_block")
@DiscriminatorValue("FundingClaims")
@JoinData(sourceTable = "funding_claims_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the funding claims block is a subclass of the project block and shares a common key")
public class FundingClaimsBlock extends NamedProjectBlock {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = FundingClaimsEntry.class)
    @JoinColumn(name = "block_id")
    private List<FundingClaimsEntry> fundingClaimsEntries = new ArrayList<>();

    @Column(name = "variation_requested")
    private Boolean variationRequested = false;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = FundingClaimsVariation.class)
    @JoinColumn(name="block_id")
    private Set<FundingClaimsVariation> fundingClaimsVariations = new HashSet<>();

    @Transient
    public Map<Integer, FundingClaimsTotals> totals = new HashMap<>();

    public List<FundingClaimsEntry> getFundingClaimsEntries() {
        return fundingClaimsEntries;
    }

    public void setFundingClaimsEntries(List<FundingClaimsEntry> fundingClaimsEntries) {
        this.fundingClaimsEntries = fundingClaimsEntries;
    }

    public Boolean getVariationRequested() {
        return variationRequested;
    }

    public void setVariationRequested(Boolean variationRequested) {
        this.variationRequested = variationRequested;
    }

    public Set<FundingClaimsVariation> getFundingClaimsVariations() {
        return fundingClaimsVariations;
    }

    public void setFundingClaimsVariations(Set<FundingClaimsVariation> fundingClaimsVariations) {
        this.fundingClaimsVariations = fundingClaimsVariations;
    }

    @Override
    public boolean isComplete() {
        if (!variationRequested) {
            return true;
        }

        if(this.fundingClaimsVariations.size() > 0){
            FundingClaimsVariation variation = this.fundingClaimsVariations.iterator().next();
            return variation.getAllocation() != null && variation.getDescription() != null && variation.getDescription().length() > 0;
        }

        return false;
    }

    @Override
    protected void generateValidationFailures() {}

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        if (templateBlock instanceof FundingClaimsTemplateBlock) {
            FundingClaimsTemplateBlock fundingClaimsTemplateBlock = (FundingClaimsTemplateBlock) templateBlock;

            LearningGrantTemplateBlock learningGrantTemplateBlock = (LearningGrantTemplateBlock) getProject().getTemplate().getSingleBlockByType(ProjectBlockType.LearningGrant);
            Integer startYear = learningGrantTemplateBlock.getStartYear();
            Integer numberOfYears = learningGrantTemplateBlock.getNumberOfYears();

            for (Integer year = startYear; year < startYear + numberOfYears; year++) {
                if (fundingClaimsTemplateBlock.getPeriods() != null) {
                    for (FundingClaimPeriod period: fundingClaimsTemplateBlock.getPeriods()) {
                        for (FundingClaimCategory category: fundingClaimsTemplateBlock.getCategories()) {
                            this.getFundingClaimsEntries().add(new FundingClaimsEntry(year, period.getPeriod(), category.getId(), category.getName(), category.getDisplayOrder()));
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        FundingClaimsBlock clonedBlock = (FundingClaimsBlock) target;
        clonedBlock.setVariationRequested(getVariationRequested());

        for (FundingClaimsEntry entry: fundingClaimsEntries) {
            clonedBlock.getFundingClaimsEntries().add(entry.clone());
        }

        for (FundingClaimsVariation entry: fundingClaimsVariations) {
            clonedBlock.getFundingClaimsVariations().add(entry.clone());
        }
    }

    @Override
    public void merge(NamedProjectBlock block) {
        FundingClaimsBlock updated = (FundingClaimsBlock) block;
        this.setVariationRequested(updated.getVariationRequested());
        this.getFundingClaimsVariations().clear();
        for(FundingClaimsVariation fcv : updated.getFundingClaimsVariations()){
            fcv.setProjectId(this.getProjectId());
            this.getFundingClaimsVariations().add(fcv);
        }
    }

    public Map<Integer, FundingClaimsTotals> calculateTotals(Integer period) {
        totals.put(period, getFundingClaimsTotals(period));
        return totals;
    }

    public FundingClaimsTotals getFundingClaimsTotals(Integer period) {
        return new FundingClaimsTotals(getActualTotal(period), getForecastTotal(period), getTotalDelivery(period));
    }

    private BigDecimal getActualTotal(Integer period) {
        return fundingClaimsEntries.stream().filter(fc -> Objects.equals(period, fc.getPeriod()))
            .map(FundingClaimsEntry::getActualDelivery).reduce(BigDecimal.ZERO, GlaUtils::nullSafeAdd);
    }

    private BigDecimal getForecastTotal(Integer period) {
        return fundingClaimsEntries.stream().filter(fc -> Objects.equals(period, fc.getPeriod()))
            .map(FundingClaimsEntry::getForecastDelivery).reduce(BigDecimal.ZERO, GlaUtils::nullSafeAdd);
    }

    private BigDecimal getTotalDelivery(Integer period) {
        return fundingClaimsEntries.stream().filter(fc -> Objects.equals(period, fc.getPeriod()))
            .map(FundingClaimsEntry::getTotalDelivery).reduce(BigDecimal.ZERO, GlaUtils::nullSafeAdd);
    }
}
