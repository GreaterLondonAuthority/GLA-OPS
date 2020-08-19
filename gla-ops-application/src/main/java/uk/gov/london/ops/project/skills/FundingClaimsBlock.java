/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.skills;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.payment.FundingClaimsVariation;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.block.ProjectDifference;
import uk.gov.london.ops.project.block.ProjectDifferences;
import uk.gov.london.ops.project.template.domain.FundingClaimCategory;
import uk.gov.london.ops.project.template.domain.FundingClaimPeriod;
import uk.gov.london.ops.project.template.domain.FundingClaimsTemplateBlock;
import uk.gov.london.ops.project.template.domain.LearningGrantTemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateBlock;

@Entity(name = "funding_claims_block")
@DiscriminatorValue("FundingClaims")
@JoinData(sourceTable = "funding_claims_block", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the funding claims block is a subclass of the project block and shares a common key")
public class FundingClaimsBlock extends NamedProjectBlock {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = FundingClaimsEntry.class)
    @JoinColumn(name = "block_id")
    private List<FundingClaimsEntry> fundingClaimsEntries = new ArrayList<>();

    @Column(name = "variation_requested")
    private Boolean variationRequested = false;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = FundingClaimsVariation.class)
    @JoinColumn(name = "block_id")
    private Set<FundingClaimsVariation> fundingClaimsVariations = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = ContractTypeSelection.class)
    @JoinColumn(name = "block_id")
    private Set<ContractTypeSelection> contractTypes = new HashSet<>();

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Map<Integer, Map<Integer, FundingClaimsTotals>> totals = new HashMap<>();


    /**
     * Fields populated from learning grant block
     */
    @Transient
    private Integer startYear;

    @Transient
    private Integer numberOfYears;

    @Transient
    private BigDecimal totalAllocation;

    @Transient
    private List<LearningGrantAllocation> allocations = new ArrayList<>();

    @Transient
    private SkillsGrantType grantType;


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
        if (this.getContractTypes().size() > 0 && this.contractTypes.stream().anyMatch(ct -> ct.getSelected() == null)) {
            return false;
        }

        Set<Integer> allYears = this.fundingClaimsEntries.stream().map(fce -> fce.getAcademicYear()).collect(Collectors.toSet());
        Set<Integer> allPeriods = this.fundingClaimsEntries.stream().map(fce -> fce.getPeriod()).collect(Collectors.toSet());

        //If there is modified data in some period but not all entries are complete then block is incomplete
        for (Integer year : allYears) {
            for (Integer period : allPeriods) {
                List<FundingClaimsEntry> entriesInPeriod = this.fundingClaimsEntries.stream()
                        .filter(fce -> fce.getAcademicYear().equals(year) && fce.getPeriod().equals(period))
                        .collect(Collectors.toList());
                boolean hasModifiedData = entriesInPeriod.stream()
                        .anyMatch(fce -> fce.getContractTypeFundingEntries().stream().anyMatch(ct -> ct.isModified()));
                if (hasModifiedData && entriesInPeriod.stream().anyMatch(
                        fce -> fce.getContractTypeFundingEntries().isEmpty() || fce.getContractTypeFundingEntries().stream()
                                .anyMatch(ct -> !ct.isComplete()))) {
                    return false;
                }
            }
        }

        if (!variationRequested) {
            return true;
        }

        if (this.fundingClaimsVariations.size() > 0) {
            FundingClaimsVariation variation = this.fundingClaimsVariations.iterator().next();
            return variation.getAllocation() != null && variation.getDescription() != null
                    && variation.getDescription().length() > 0;
        }

        return false;
    }

    public boolean hasPeriodData(Integer year, Integer period) {
        List<FundingClaimsEntry> entriesInPeriod = this.fundingClaimsEntries.stream()
                .filter(fce -> fce.getAcademicYear().equals(year) && fce.getPeriod().equals(period)).collect(Collectors.toList());
        return entriesInPeriod.stream().anyMatch(fce -> {
            if (this.grantType.equals(SkillsGrantType.AEB_PROCURED)) {
                return fce.getContractTypeFundingEntries().stream().anyMatch(ct -> ct.isModified());
            } else {
                return fce.getActualDelivery() != null || fce.getForecastDelivery() != null;
            }
        });
    }

    public FundingClaimsEntry getFundingClaimEntry(Integer year, Integer period, Integer categoryId) {
        return this.fundingClaimsEntries.stream()
                .filter(fce -> fce.getAcademicYear().equals(year) && fce.getPeriod().equals(period) && fce.getCategoryId()
                        .equals(categoryId))
                .findFirst()
                .orElse(null);
    }

    public ContractTypeFundingEntry getContractTypeEntry(Integer year, Integer period, Integer categoryId, String contractType) {
        FundingClaimsEntry fce = this.getFundingClaimEntry(year, period, categoryId);
        if (fce != null) {
            return fce.getContractTypeFundingEntries().stream()
                    .filter(ctfe -> ctfe.getContractType().equals(contractType))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    protected void generateValidationFailures() {
    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        if (templateBlock instanceof FundingClaimsTemplateBlock) {
            FundingClaimsTemplateBlock fundingClaimsTemplateBlock = (FundingClaimsTemplateBlock) templateBlock;

            LearningGrantTemplateBlock learningGrantTemplateBlock = (LearningGrantTemplateBlock) getProject().getTemplate()
                    .getSingleBlockByType(ProjectBlockType.LearningGrant);
            Integer startYear = learningGrantTemplateBlock.getStartYear();
            Integer numberOfYears = learningGrantTemplateBlock.getNumberOfYears();

            for (Integer year = startYear; year < startYear + numberOfYears; year++) {
                if (fundingClaimsTemplateBlock.getPeriods() != null) {
                    for (FundingClaimPeriod period : fundingClaimsTemplateBlock.getPeriods()) {
                        for (FundingClaimCategory category : fundingClaimsTemplateBlock.getCategories()) {
                            this.getFundingClaimsEntries()
                                    .add(new FundingClaimsEntry(year, period.getPeriod(), category.getId(), category.getName(),
                                            category.getDisplayOrder()));
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

        for (FundingClaimsEntry entry : fundingClaimsEntries) {
            clonedBlock.getFundingClaimsEntries().add(entry.clone());
        }

        for (FundingClaimsVariation entry : fundingClaimsVariations) {
            clonedBlock.getFundingClaimsVariations().add(entry.clone());
        }

        for (ContractTypeSelection entry : contractTypes) {
            clonedBlock.getContractTypes().add(entry.clone());
        }
    }

    @Override
    public void merge(NamedProjectBlock block) {
        FundingClaimsBlock updated = (FundingClaimsBlock) block;
        this.setVariationRequested(updated.getVariationRequested());
        this.getFundingClaimsVariations().clear();
        for (FundingClaimsVariation fcv : updated.getFundingClaimsVariations()) {
            fcv.setProjectId(this.getProjectId());
            this.getFundingClaimsVariations().add(fcv);
        }
        List<ContractTypeSelection> removedContractTypes = this.getContractTypes().stream()
                .filter(existingCt -> Boolean.TRUE.equals(existingCt.getSelected())
                        && updated.getContractTypes().stream()
                        .noneMatch(newCt -> existingCt.getId().equals(newCt.getId())
                                && existingCt.getSelected().equals(newCt.getSelected())))
                .collect(Collectors.toList());
        this.getContractTypes().clear();
        this.getContractTypes().addAll(updated.getContractTypes());
        for (ContractTypeSelection removedContractType : removedContractTypes) {
            for (FundingClaimsEntry fundingClaimsEntry : this.getFundingClaimsEntries()) {
                fundingClaimsEntry.removeContractTypeFundingEntriesFor(removedContractType.getName());
            }
        }
    }


    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {
        FundingClaimsBlock otherFundingClaimsBlock = (FundingClaimsBlock) other;

        Map<Integer, Map<Integer, FundingClaimsTotals>> thisTotals = this.getTotals();
        Map<Integer, Map<Integer, FundingClaimsTotals>> otherTotals = otherFundingClaimsBlock.getTotals();

        for (Integer year : thisTotals.keySet()) {
            for (Integer period : thisTotals.get(year).keySet()) {
                String comparisonId = String.format("totals:%d:%d", year, period);

                if (otherTotals.get(year) == null || otherTotals.get(year).get(period) == null) {
                    differences.add(new ProjectDifference(comparisonId, ProjectDifference.DifferenceType.Addition));
                    continue;
                }

                FundingClaimsTotals thisPeriodTotals = thisTotals.get(year).get(period);
                FundingClaimsTotals otherPeriodTotals = otherTotals.get(year).get(period);

                if (SkillsGrantType.AEB_PROCURED == otherFundingClaimsBlock.getGrantType() && otherFundingClaimsBlock
                        .getContractTypes().isEmpty()) {
                    differences.add(new ProjectDifference(comparisonId, ProjectDifference.DifferenceType.Addition));
                    continue;
                }

                if (thisPeriodTotals != null) {

                    if (otherPeriodTotals == null) {
                        differences.add(new ProjectDifference(comparisonId, ProjectDifference.DifferenceType.Addition));
                        continue;
                    }

                    if (!Objects.equals(thisPeriodTotals.getActualTotal(), otherPeriodTotals.getActualTotal())) {
                        differences.add(new ProjectDifference(comparisonId, "actualTotal"));
                    }

                    if (!Objects.equals(thisPeriodTotals.getForecastTotal(), otherPeriodTotals.getForecastTotal())) {
                        differences.add(new ProjectDifference(comparisonId, "forecastTotal"));
                    }

                    if (!Objects.equals(thisPeriodTotals.getDeliveryTotal(), otherPeriodTotals.getDeliveryTotal())) {
                        differences.add(new ProjectDifference(comparisonId, "deliveryTotal"));
                    }

                    //Procured

                    if (!Objects.equals(thisPeriodTotals.getContractValueTotal(), otherPeriodTotals.getContractValueTotal())) {
                        differences.add(new ProjectDifference(comparisonId, "contractValueTotal"));
                    }

                    if (!Objects.equals(thisPeriodTotals.getFlexibleTotal(), otherPeriodTotals.getFlexibleTotal())) {
                        differences.add(new ProjectDifference(comparisonId, "flexibleTotal"));
                    }

                    if (!Objects.equals(thisPeriodTotals.getPercentage(), otherPeriodTotals.getPercentage())) {
                        differences.add(new ProjectDifference(comparisonId, "percentage"));
                    }

                    Set<FundingClaimsTotals.ContractTypeTotal> thisContractTotals = thisPeriodTotals.getContractTypeTotals();
                    Set<FundingClaimsTotals.ContractTypeTotal> otherContractTotals = otherPeriodTotals.getContractTypeTotals();

                    //If contract type existed in approved version and doesn't exist in unapproved, record the change for the value
                    for (FundingClaimsTotals.ContractTypeTotal otherContractTypeTotal : otherContractTotals) {
                        FundingClaimsTotals.ContractTypeTotal thisContractTypeTotals = thisContractTotals.stream()
                                .filter(ctt -> ctt.getContractType().equals(otherContractTypeTotal.getContractType())).findFirst()
                                .orElse(null);
                        if (thisContractTypeTotals == null) {
                            String cttComparisonId = comparisonId + ":" + otherContractTypeTotal.getContractType();
                            differences.add(new ProjectDifference(cttComparisonId, "funding"));
                            differences.add(new ProjectDifference(cttComparisonId, "flexibleFunding"));
                        }
                    }

                    for (FundingClaimsTotals.ContractTypeTotal thisContractTypeTotal : thisContractTotals) {
                        FundingClaimsTotals.ContractTypeTotal otherContractTypeTotals = otherContractTotals.stream()
                                .filter(ctt -> ctt.getContractType().equals(thisContractTypeTotal.getContractType())).findFirst()
                                .orElse(null);
                        String cttComparisonId = comparisonId + ":" + thisContractTypeTotal.getContractType();

                        //If contract type exists in unapproved version and didn't exist in approved, record the change for the value
                        if (otherContractTypeTotals == null) {
                            differences.add(new ProjectDifference(cttComparisonId, "funding"));
                            differences.add(new ProjectDifference(cttComparisonId, "flexibleFunding"));
                            continue;
                        }

                        if (!Objects.equals(thisContractTypeTotal.getFunding(), otherContractTypeTotals.getFunding())) {
                            differences.add(new ProjectDifference(cttComparisonId, "funding"));
                        }

                        if (!Objects.equals(thisContractTypeTotal.getFlexibleFunding(),
                                otherContractTypeTotals.getFlexibleFunding())) {
                            differences.add(new ProjectDifference(cttComparisonId, "flexibleFunding"));
                        }

                        if (!Objects.equals(thisContractTypeTotal.getPercentage(), otherContractTypeTotals.getPercentage())) {
                            differences.add(new ProjectDifference(cttComparisonId, "percentage"));
                        }
                    }
                }
            }
        }
    }


    public void calculateTotals() {
        FundingClaimsTemplateBlock templateBlock = (FundingClaimsTemplateBlock) project.getTemplate()
                .getSingleBlockByType(ProjectBlockType.FundingClaims);
        List<FundingClaimPeriod> periods = templateBlock.getPeriods();
        for (int i = 0; i < this.getNumberOfYears(); i++) {
            for (FundingClaimPeriod period : periods) {
                this.calculateTotals(this.getStartYear() + i, period.getPeriod());
            }
        }
    }

    public void calculateTotals(Integer year, Integer period) {
        Map<Integer, FundingClaimsTotals> integerFundingClaimsTotalsMap = totals.get(year);
        if (integerFundingClaimsTotalsMap == null) {
            integerFundingClaimsTotalsMap = new HashMap<>();
            totals.put(year, integerFundingClaimsTotalsMap);
        }
        integerFundingClaimsTotalsMap.put(period, this.hasPeriodData(year, period) ? getFundingClaimsTotals(year, period) : null);
    }

    public FundingClaimsTotals getFundingClaimsTotals(Integer year, Integer period) {
        if (this.grantType.equals(SkillsGrantType.AEB_PROCURED)) {
            return getProcuredTotals(year, period);
        } else {
            return new FundingClaimsTotals(getActualTotal(period), getForecastTotal(period), getTotalDelivery(period));
        }
    }

    private FundingClaimsTotals getProcuredTotals(Integer year, Integer period) {
        FundingClaimsTotals total = new FundingClaimsTotals();

        Set<FundingClaimsEntry> matches = fundingClaimsEntries.stream()
                .filter(fc -> Objects.equals(year, fc.getAcademicYear()))
                .filter(fc -> Objects.equals(period, fc.getPeriod()))
                .collect(Collectors.toSet());

        Set<String> types = this.getContractTypes().stream().filter(t -> t.getSelected() != null && t.getSelected())
                .map(ContractTypeSelection::getName).collect(Collectors.toSet());

        BigDecimal contractValueTotal = BigDecimal.ZERO;
        BigDecimal flexValueTotal = BigDecimal.ZERO;
        for (String type : types) {
            BigDecimal contractValue = matches.stream().map(t -> t.getContracTypeTotal(type))
                    .reduce(BigDecimal.ZERO, GlaUtils::nullSafeAdd);
            contractValueTotal = contractValueTotal.add(contractValue);
            BigDecimal flexValue = matches.stream().map(t -> t.getFlexibleAllocationTotal(type))
                    .reduce(BigDecimal.ZERO, GlaUtils::nullSafeAdd);
            flexValueTotal = flexValueTotal.add(flexValue);
            total.addContractTypeTotal(type, contractValue, flexValue);
        }

        total.setContractValueTotal(contractValueTotal);
        total.setFlexibleTotal(flexValueTotal);

        return total;
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


    @Override
    public void enrichFromBlock(NamedProjectBlock otherBlock) {
        if (otherBlock instanceof LearningGrantBlock) {
            LearningGrantBlock learning = (LearningGrantBlock) otherBlock;
            this.setAllocations(learning.getAllocations());
            this.setTotalAllocation(learning.getTotalAllocation());
            this.setNumberOfYears(learning.getNumberOfYears());
            this.setStartYear(learning.getStartYear());
            this.setGrantType(learning.getGrantType());
            this.calculateTotals();
        }
    }

    public SkillsGrantType getGrantType() {
        return grantType;
    }

    public void setGrantType(SkillsGrantType grantType) {
        this.grantType = grantType;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Integer getNumberOfYears() {
        return numberOfYears;
    }

    public void setNumberOfYears(Integer numberOfYears) {
        this.numberOfYears = numberOfYears;
    }

    public BigDecimal getTotalAllocation() {
        return totalAllocation;
    }

    public void setTotalAllocation(BigDecimal totalAllocation) {
        this.totalAllocation = totalAllocation;
    }

    public List<LearningGrantAllocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<LearningGrantAllocation> allocations) {
        this.allocations = allocations;
    }

    public boolean isSelfContained() {
        return false;
    }

    public Map<Integer, Map<Integer, FundingClaimsTotals>> getTotals() {
        return totals;
    }

    public void setTotals(Map<Integer, Map<Integer, FundingClaimsTotals>> totals) {
        this.totals = totals;
    }

    public Set<ContractTypeSelection> getContractTypes() {
        return contractTypes;
    }

    public void setContractTypes(Set<ContractTypeSelection> contractTypes) {
        this.contractTypes = contractTypes;
    }
}
