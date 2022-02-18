/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.grant;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.gov.london.ops.framework.OPSUtils.calculatePercentage;
import static uk.gov.london.ops.project.block.ProjectBlockType.AffordableHomes;
import static uk.gov.london.ops.project.grant.AffordableHomesType.Completion;
import static uk.gov.london.ops.project.grant.AffordableHomesType.StartOnSite;
import static uk.gov.london.ops.project.grant.IndicativeGrantRequestedEntryKt.TOTAL_SCHEME_COST;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.OpsEvent;
import uk.gov.london.ops.framework.enums.GrantType;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.block.FundingSourceProvider;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectDifference;
import uk.gov.london.ops.project.block.ProjectDifferences;
import uk.gov.london.ops.project.template.domain.AffordableHomesCostsAndContributionsEntry;
import uk.gov.london.ops.project.template.domain.AffordableHomesTemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateTenureType;
import uk.gov.london.ops.refdata.TenureType;

@Entity
@Table(name = "tenure_block")
@DiscriminatorValue("AFFORDABLE_HOMES")
@JoinData(sourceTable = "tenure_block", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the affordable homes block is a subclass of the project block and shares a common key")
public class AffordableHomesBlock extends NamedProjectBlock implements FundingSourceProvider {

    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "block_id", targetTable = "tenure_block",
            comment = "")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "block_id")
    private Set<AffordableHomesEntry> entries = new HashSet<>();

    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "block_id", targetTable = "tenure_block",
            comment = "")
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "block_id")
    private Set<IndicativeGrantRequestedEntry> grantRequestedEntries = new HashSet<>();

    @JsonIgnore
    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "block_id",
            targetTable = "costs_and_contributions", comment = "project costs and contributions")
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "block_id")
    private Set<AffordableHomesCostsAndContributions> costsAndContributions = new HashSet<>();

    @Column(name = "justification")
    private String grantRequestedAnswer;

    @Column(name = "sos_milestone_authorised")
    protected OffsetDateTime startOnSiteMilestoneAuthorised;

    @Column(name = "completion_milestone_authorised")
    protected OffsetDateTime completionMilestoneAuthorised;

    @Column(name = "zero_grant_requested")
    private Boolean zeroGrantRequested;

    @Transient
    private Map<AffordableHomesType, AffordableHomesTotals> totals;

    @Column(name = "completion_only")
    private Boolean completionOnly = false;

    public AffordableHomesBlock() {
        setBlockType(AffordableHomes);
    }

    public Set<AffordableHomesEntry> getEntries() {
        return entries;
    }

    public void setEntries(Set<AffordableHomesEntry> entries) {
        this.entries = entries;
    }

    public AffordableHomesEntry findEntry(Integer year, int tenureTypeId, AffordableHomesType type) {
        return this.entries.stream().filter(e -> {
            boolean isYearMatching = year == null ? e.getYear() == null : e.getYear() != null && e.getYear().equals(year);
            return isYearMatching && e.getTenureTypeId() == tenureTypeId && e.getType().equals(type);
        }).findFirst().orElse(null);
    }

    public Set<IndicativeGrantRequestedEntry> getGrantRequestedEntries() {
        return grantRequestedEntries;
    }

    public void setGrantRequestedEntries(Set<IndicativeGrantRequestedEntry> grantRequestedEntries) {
        this.grantRequestedEntries = grantRequestedEntries;
    }

    public IndicativeGrantRequestedEntry findGrantRequestedEntry(int tenureTypeId, String type) {
        return this.grantRequestedEntries.stream()
                .filter(e -> e.getTenureTypeId() == tenureTypeId && e.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

    public String getGrantRequestedAnswer() {
        return grantRequestedAnswer;
    }

    public void setGrantRequestedAnswer(String grantRequestedAnswer) {
        this.grantRequestedAnswer = grantRequestedAnswer;
    }

    public Boolean getZeroGrantRequested() {
        return zeroGrantRequested;
    }

    public void setZeroGrantRequested(Boolean zeroGrantRequested) {
        this.zeroGrantRequested = zeroGrantRequested;
    }

    public Boolean getCompletionOnly() {
        return completionOnly;
    }

    public void setCompletionOnly(Boolean completionOnly) {
        this.completionOnly = completionOnly;
    }

    public Set<AffordableHomesCostsAndContributions> getCosts() {
        return costsAndContributions.stream()
                .filter(c -> c.getEntryType().equals(EntryType.Cost))
                .collect(Collectors.toSet());
    }

    public void setCosts(Set<AffordableHomesCostsAndContributions> costs) {
        costsAndContributions.removeIf(c -> c.getEntryType().equals(EntryType.Cost));
        costsAndContributions.addAll(costs);
    }

    public void setContributions(Set<AffordableHomesCostsAndContributions> contributions) {
        costsAndContributions.removeIf(c -> c.getEntryType().equals(EntryType.Contribution));
        costsAndContributions.addAll(contributions);
    }

    public Set<AffordableHomesCostsAndContributions> getContributions() {
        return costsAndContributions.stream()
                .filter(c -> c.getEntryType().equals(EntryType.Contribution))
                .collect(Collectors.toSet());
    }

    public BigDecimal getTotalCostsPercentage() {
        BigDecimal grantRequested = getTotalGrantRequested();

        BigDecimal totalCosts = getTotalCosts();

        if (grantRequested.compareTo(BigDecimal.ZERO) != 0
                && totalCosts.compareTo(BigDecimal.ZERO) != 0) {
            return calculatePercentage(grantRequested, totalCosts, 2, BigDecimal.ROUND_HALF_UP);
        } else {
            return null;
        }
    }

    public OffsetDateTime getStartOnSiteMilestoneAuthorised() {
        return startOnSiteMilestoneAuthorised;
    }

    public void setStartOnSiteMilestoneAuthorised(OffsetDateTime startOnSiteMilestoneAuthorised) {
        this.startOnSiteMilestoneAuthorised = startOnSiteMilestoneAuthorised;
    }

    public OffsetDateTime getCompletionMilestoneAuthorised() {
        return completionMilestoneAuthorised;
    }

    public void setCompletionMilestoneAuthorised(OffsetDateTime completionMilestoneAuthorised) {
        this.completionMilestoneAuthorised = completionMilestoneAuthorised;
    }

    private BigDecimal getTotalCosts() {
        return getCosts().stream()
                .filter(i -> i.getEntryType().equals(EntryType.Cost))
                .map(AffordableHomesCostsAndContributions::getValue)
                .reduce(BigDecimal.ZERO, GlaUtils::addBigDecimals);
    }


    public BigDecimal getTotalGrantRequested() {
        return grantRequestedEntries.stream()
                .filter(i -> !i.getType().equals(TOTAL_SCHEME_COST))
                .map(IndicativeGrantRequestedEntry::getValue)
                .reduce(BigDecimal.ZERO, GlaUtils::addBigDecimals).setScale(0, BigDecimal.ROUND_HALF_UP);
    }

    public Set<AffordableHomesCostsAndContributions> getCostsAndContributions() {
        return costsAndContributions;
    }

    public void setCostsAndContributions(Set<AffordableHomesCostsAndContributions> costsAndContributions) {
        this.costsAndContributions = costsAndContributions;
    }

    @JsonIgnore
    protected Set<TenureType> getTenureTypes() {
        return project.getTemplate().getTenureTypes().stream().map(TemplateTenureType::getTenureType).collect(toSet());
    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock tb) {
        AffordableHomesTemplateBlock templateBlock = (AffordableHomesTemplateBlock) tb;
        Integer startYear = 0;
        Integer endYear = 0;
        if (!templateBlock.isYearsDisabled() && project.getProgramme().isYearlyDataValid()) {
            startYear = project.getProgramme().getStartYear();
            endYear = project.getProgramme().getEndYear();
        }

        for (TemplateTenureType tenureType : project.getTemplate().getTenureTypes()) {
            initStartsAndCompletionEntries(templateBlock, tenureType.getTenureType(), startYear, endYear);
            initGrantRequestedEntries(templateBlock, tenureType.getTenureType());
        }

        for (AffordableHomesCostsAndContributionsEntry costCategory : templateBlock.getCostCategories()) {
            this.getCostsAndContributions().add(
                    new AffordableHomesCostsAndContributions(
                            EntryType.Cost, costCategory.getName(), costCategory.getDisplayOrder()));
        }

        for (AffordableHomesCostsAndContributionsEntry contributionCategory :
                templateBlock.getContributionCategories()) {
            this.getCostsAndContributions().add(
                    new AffordableHomesCostsAndContributions(
                            EntryType.Contribution, contributionCategory.getName(),
                            contributionCategory.getDisplayOrder()));
        }
    }

    private void initStartsAndCompletionEntries(AffordableHomesTemplateBlock templateBlock,
            TenureType tenureType, Integer startYear, Integer endYear) {
        for (int year = startYear; year <= endYear; year++) {
            entries.add(new AffordableHomesEntry(year, tenureType.getId(), StartOnSite));
            entries.add(new AffordableHomesEntry(year, tenureType.getId(), Completion));
        }
        for (int year = endYear + 1; year <= endYear + templateBlock.getAdditionalCompletionYears(); year++) {
            entries.add(new AffordableHomesEntry(year, tenureType.getId(), Completion));
        }

        for (AffordableHomesOfWhichCategory ofWhichCategory : templateBlock.getOfWhichCategories()) {
            entries.add(new AffordableHomesEntry(tenureType.getId(), StartOnSite, ofWhichCategory));
            entries.add(new AffordableHomesEntry(tenureType.getId(), Completion, ofWhichCategory));
        }
    }

    private void initGrantRequestedEntries(AffordableHomesTemplateBlock templateBlock,
            TenureType tenureType) {
        for (String grantType : templateBlock.getGrantTypes()) {
            grantRequestedEntries.add(new IndicativeGrantRequestedEntry(tenureType.getId(), grantType));
        }
        grantRequestedEntries.add(new IndicativeGrantRequestedEntry(tenureType.getId(), TOTAL_SCHEME_COST));
    }

    @Override
    public void merge(NamedProjectBlock block) {
        AffordableHomesBlock updatedBlock = (AffordableHomesBlock) block;

        for (AffordableHomesEntry updatedEntry : updatedBlock.getEntries()) {
            AffordableHomesEntry existingEntry = getEntryById(updatedEntry.getId());
            existingEntry.setUnits(updatedEntry.getUnits());
        }

        for (IndicativeGrantRequestedEntry updatedEntry : updatedBlock.getGrantRequestedEntries()) {
            IndicativeGrantRequestedEntry existingEntry = getGrantRequestedEntryById(updatedEntry.getId());
            existingEntry.setValue(updatedEntry.getValue());
        }

        for (AffordableHomesCostsAndContributions costsAndContribution :
                updatedBlock.costsAndContributions) {
            AffordableHomesCostsAndContributions entry =
                    getCostOrContributionByDescription(costsAndContribution.getEntryType(),
                            costsAndContribution.getDescription());
            entry.setValue(costsAndContribution.getValue());
        }

        setGrantRequestedAnswer(updatedBlock.getGrantRequestedAnswer());
        setZeroGrantRequested(updatedBlock.getZeroGrantRequested());
        setCompletionOnly(updatedBlock.getCompletionOnly());

        if (this.completionOnly) {
            getEntries().stream().filter(entry -> entry.getType().equals(StartOnSite)).forEach(entry -> entry.setUnits(null));
        }
    }

    private AffordableHomesCostsAndContributions getCostOrContributionByDescription(
            EntryType type, String desc) {
        return costsAndContributions.stream()
                .filter(c -> c.getEntryType().equals(type) && c.getDescription().equals(desc))
                .findFirst()
                .orElse(null);
    }


    private AffordableHomesEntry getEntryById(Integer id) {
        return entries.stream().filter(entry -> Objects.equals(entry.getId(), id)).findFirst().orElse(null);
    }

    private IndicativeGrantRequestedEntry getGrantRequestedEntryById(Integer id) {
        return grantRequestedEntries.stream()
                .filter(entry -> Objects.equals(entry.getId(), id)).findFirst().orElse(null);
    }

    @Override
    public boolean isComplete() {
        return isVisited() && getValidationFailures().size() == 0;
    }

    @Override
    protected void generateValidationFailures() {
        if (!this.completionOnly) {
            validateAtLeastOneEntryPopulated(StartOnSite, "startOnSiteUnits");
        }
        validateAtLeastOneEntryPopulated(Completion, "completionUnits");

        calculateTotalsIfNecessary();
        if (!this.completionOnly) {
            AffordableHomesTotals startOnSiteTotals = totals.get(StartOnSite);
            AffordableHomesTotals completionTotals = totals.get(Completion);
            for (Integer tenureTypeId : startOnSiteTotals.getTotalsByTenure().keySet()) {
                Integer sosTotalByTenure = startOnSiteTotals.getTotalsByTenure().get(tenureTypeId);
                Integer completionTotalByTenure = completionTotals.getTotalsByTenure().get(tenureTypeId);
                if (!sosTotalByTenure.equals(completionTotalByTenure)) {
                    addErrorMessage("totalUnits", "totalUnits", "Total units for each tenure type at start on site "
                            + "must match total units for each tenure type at completion");
                }
                checkOfWhichEntriesDoNotExceedTotals(StartOnSite, tenureTypeId, sosTotalByTenure, "ofWhichTotalSosUnits");
                checkOfWhichEntriesDoNotExceedTotals(Completion, tenureTypeId, completionTotalByTenure,
                        "ofWhichTotalCompletionUnits");
            }
        }

        BigDecimal totalSchemeCosts = getGrantRequestedTotals().getTotalsByType().get(TOTAL_SCHEME_COST);
        BigDecimal costs = getTotalCosts();
        if (!checkEqual(totalSchemeCosts, costs)) {
            addErrorMessage("costs", "costs",
                    "Scheme costs must be equal to total scheme costs by tenure type");
        }

        BigDecimal eligibleGrant = getSummaryTotals().getTotalEligibleGrant();
        BigDecimal grantRequested = getTotalGrantRequested();
        if (!checkLessThanOrEqualTo(grantRequested, eligibleGrant)) {
            addErrorMessage("amount", "amount",
                    "Amount requested must not exceed total eligible grant");
        }
    }

    private boolean checkEqual(BigDecimal a, BigDecimal b) {
        a = a == null ? BigDecimal.ZERO : a;
        b = b == null ? BigDecimal.ZERO : b;
        return a.compareTo(b) == 0;

    }

    private boolean checkLessThanOrEqualTo(BigDecimal a, BigDecimal b) {
        a = a == null ? BigDecimal.ZERO : a;
        b = b == null ? BigDecimal.ZERO : b;
        return a.compareTo(b) <= 0;
    }


    private void checkOfWhichEntriesDoNotExceedTotals(AffordableHomesType type, Integer tenureTypeId,
            Integer totalByTenure, String errorKey) {
        List<AffordableHomesEntry> ofWhichEntries = entries.stream()
                .filter(e -> e.getType().equals(type) && e.getTenureTypeId() == tenureTypeId && e.getOfWhichCategory() != null)
                .collect(toList());
        for (AffordableHomesEntry ofWhichEntry : ofWhichEntries) {
            if (ofWhichEntry.getUnits() != null && ofWhichEntry.getUnits() > totalByTenure) {
                addErrorMessage(errorKey, errorKey,
                        "OF WHICH category total must not exceed the corresponding tenure type total");
            }
        }
    }

    private void validateAtLeastOneEntryPopulated(AffordableHomesType type, String key) {
        if (entries.stream().noneMatch(entry -> entry.getUnits() != null && entry.getType().equals(type))) {
            addErrorMessage(key, "total", "You must enter at least one unit");
        }
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        AffordableHomesBlock clone = (AffordableHomesBlock) target;

        Set<AffordableHomesEntry> clonedEntries = this.entries.stream()
                .map(AffordableHomesEntry::copy)
                .collect(toSet());
        clone.setEntries(clonedEntries);

        Set<IndicativeGrantRequestedEntry> clonedGrantRequestedEntries = this.grantRequestedEntries.stream()
                .map(IndicativeGrantRequestedEntry::copy)
                .collect(toSet());
        clone.setGrantRequestedEntries(clonedGrantRequestedEntries);

        clone.setGrantRequestedAnswer(getGrantRequestedAnswer());
        clone.setZeroGrantRequested(getZeroGrantRequested());
        clone.setCompletionOnly(getCompletionOnly());

        Set<AffordableHomesCostsAndContributions> cloneCosts = this.costsAndContributions.stream()
                .map(AffordableHomesCostsAndContributions::copy)
                .collect(toSet());
        clone.setCostsAndContributions(cloneCosts);
        clone.setStartOnSiteMilestoneAuthorised(this.getStartOnSiteMilestoneAuthorised());
        clone.setCompletionMilestoneAuthorised(this.getCompletionMilestoneAuthorised());

    }

    public Map<AffordableHomesType, AffordableHomesTotals> getTotals() {
        calculateTotalsIfNecessary();
        return totals;
    }

    private void calculateTotalsIfNecessary() {
        if (totals == null) {
            this.totals = new HashMap<>();
            totals.put(StartOnSite, new AffordableHomesTotals());
            totals.put(Completion, new AffordableHomesTotals());
            for (AffordableHomesEntry entry : entries) {
                totals.get(entry.getType()).processEntry(entry);
            }
        }
    }

    public IndicativeCostsAndContributionsSummaryTotals getSummaryTotals() {
        return new IndicativeCostsAndContributionsSummaryTotals(getCosts(), getContributions());
    }

    public IndicativeGrantRequestedTotals getGrantRequestedTotals() {
        IndicativeGrantRequestedTotals grantRequestedTotals = new IndicativeGrantRequestedTotals();
        AffordableHomesType type = StartOnSite;
        if (this.completionOnly) {
            type = Completion;
        }
        for (AffordableHomesEntry entry : entries) {
            grantRequestedTotals.processEntry(entry, type);
        }
        for (IndicativeGrantRequestedEntry entry : grantRequestedEntries) {
            grantRequestedTotals.processEntry(entry);
        }
        return grantRequestedTotals;
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock otherBlock, ProjectDifferences differences) {
        super.compareBlockSpecificContent(otherBlock, differences);

        AffordableHomesBlock otherIndicativeStartAndCompletionBlock =
                (AffordableHomesBlock) otherBlock;

        if (!Objects.equals(this.getTotalCostsPercentage(),
                otherIndicativeStartAndCompletionBlock.getTotalCostsPercentage())) {
            differences.add(new ProjectDifference("totalCostsPercentage"));
        }

        Set<IndicativeGrantRequestedEntry> thisGrantRequestedEntries = this.getGrantRequestedEntries();
        Set<IndicativeGrantRequestedEntry> otherGrantRequestedEntries =
                otherIndicativeStartAndCompletionBlock.getGrantRequestedEntries();

        for (IndicativeGrantRequestedEntry thisEntry : thisGrantRequestedEntries) {
            IndicativeGrantRequestedEntry otherEntry = otherGrantRequestedEntries.stream()
                    .filter(gre -> gre.getComparisonId().equals(thisEntry.getComparisonId()))
                    .findFirst()
                    .orElse(new IndicativeGrantRequestedEntry());
            if (!Objects.equals(thisEntry.getValue(), otherEntry.getValue())) {
                differences.add(new ProjectDifference(thisEntry.getComparisonId(), "value"));
            }
        }

        IndicativeGrantRequestedTotals thisGrantRequestedTotals = this.getGrantRequestedTotals();
        IndicativeGrantRequestedTotals otherGrantRequestedTotals =
                otherIndicativeStartAndCompletionBlock.getGrantRequestedTotals();

        for (Integer tenureTypeId : thisGrantRequestedTotals.getTotalsByTenure().keySet()) {
            IndicativeGrantRequestedTenureTotals thisTenureTypeTotals =
                    thisGrantRequestedTotals.getTotalsByTenure().get(tenureTypeId);
            IndicativeGrantRequestedTenureTotals otherTenureTypeTotals =
                    otherGrantRequestedTotals.getTotalsByTenure().get(tenureTypeId);
            String comparisonId = tenureTypeId.toString() + ":totals";

            if (!Objects.equals(thisTenureTypeTotals.getTotalUnits(), otherTenureTypeTotals.getTotalUnits())) {
                differences.add(new ProjectDifference(comparisonId, "totalUnits"));
            }

            if (!Objects.equals(thisTenureTypeTotals.getGrantPerUnit(), otherTenureTypeTotals.getGrantPerUnit())) {
                differences.add(new ProjectDifference(comparisonId, "grantPerUnit"));
            }

            if (!Objects.equals(thisTenureTypeTotals.getTscPerUnit(), otherTenureTypeTotals.getTscPerUnit())) {
                differences.add(new ProjectDifference(comparisonId, "tscPerUnit"));
            }

            if (!Objects.equals(thisTenureTypeTotals.getGrantAsPercentageOfTsc(),
                    otherTenureTypeTotals.getGrantAsPercentageOfTsc())) {
                differences.add(new ProjectDifference(comparisonId, "grantAsPercentageOfTsc"));
            }
        }

        for (String grantType : thisGrantRequestedTotals.getTotalsByType().keySet()) {
            BigDecimal thisGrantTypeTotal = thisGrantRequestedTotals.getTotalsByType().get(grantType);
            BigDecimal otherGrantTypeTotal = otherGrantRequestedTotals.getTotalsByType().get(grantType);

            if (!Objects.equals(thisGrantTypeTotal, otherGrantTypeTotal)) {
                differences.add(new ProjectDifference("totalsByType:" + grantType));
            }
        }

        IndicativeCostsAndContributionsSummaryTotals thisSummaryTotals = this.getSummaryTotals();
        IndicativeCostsAndContributionsSummaryTotals otherSummaryTotals = otherIndicativeStartAndCompletionBlock
                .getSummaryTotals();
        if (!Objects.equals(thisSummaryTotals.getTotalCosts(), otherSummaryTotals.getTotalCosts())) {
            differences.add(new ProjectDifference("totalCosts"));
        }
        if (!Objects.equals(thisSummaryTotals.getTotalContributions(), otherSummaryTotals.getTotalContributions())) {
            differences.add(new ProjectDifference("totalContributions"));
        }
        if (!Objects.equals(thisSummaryTotals.getTotalEligibleGrant(), otherSummaryTotals.getTotalEligibleGrant())) {
            differences.add(new ProjectDifference("totalEligibleGrant"));
        }
    }

    @Override
    public Map<GrantType, BigDecimal> getFundingRequested() {
        IndicativeGrantRequestedTotals grantRequestedTotals = getGrantRequestedTotals();
        Map<GrantType, BigDecimal> requested = new HashMap<>();
        for (IndicativeGrantRequestedEntry grantRequestedEntry : grantRequestedEntries) {
            String type = grantRequestedEntry.getType();
            if (!TOTAL_SCHEME_COST.equals(type)) {
                BigDecimal value = grantRequestedTotals.getTotalsByType().get(type);
                requested.put(GrantType.valueOf(type), value == null ? BigDecimal.ZERO : value);
            }
        }
        return requested;
    }

    @Override
    public void handleEvent(OpsEvent opsEvent) {
        super.handleEvent(opsEvent);

        switch (opsEvent.getEventType()) {
            case MilestoneApproval:
                handleMilestoneApproval(opsEvent.getExternalId());
                break;
            case MilestoneCancel:
                resetKeyEventFigures(opsEvent.getExternalId());
                break;
            default:
                // no op
        }
    }

    public void resetKeyEventFigures(int externalId) {
        if (START_ON_SITE_EXTERNAL_IDS.contains(externalId)) {
            this.startOnSiteMilestoneAuthorised = null;
        } else if (COMPLETION_MILESTONE_EXTERNAL_IDS.contains(externalId)) {
            this.completionMilestoneAuthorised = null;
        }
    }

    public void handleMilestoneApproval(int externalId) {
        if (START_ON_SITE_EXTERNAL_IDS.contains(externalId)) {
            this.setStartOnSiteMilestoneAuthorised(OffsetDateTime.now());
        } else if (COMPLETION_MILESTONE_EXTERNAL_IDS.contains(externalId)) {
            this.setCompletionMilestoneAuthorised(OffsetDateTime.now());
        }

    }

    /**
     * @return true if approving this block on an Active project will result in a pending reclaim payment being generated.
     */
    public boolean getApprovalWillCreatePendingReclaim() {
        return this.project.getMilestonesBlock() != null && this.project.getGrantSourceAdjustmentAmount().signum() == -1;
    }
}
