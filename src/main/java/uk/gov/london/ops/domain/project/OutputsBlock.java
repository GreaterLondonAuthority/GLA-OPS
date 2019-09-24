/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.project.outputs.OutputsCostsBlock;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.template.OutputsCostsTemplateBlock;
import uk.gov.london.ops.domain.template.OutputsTemplateBlock;
import uk.gov.london.ops.domain.template.TemplateBlock;
import uk.gov.london.ops.payment.PaymentSource;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The Outputs block in a Project.
 *
 * @author Chris Melville
 */
@Entity(name = "outputs")
@DiscriminatorValue("OUTPUTS")
@JoinData(sourceTable = "outputs", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the outputs block is a subclass of the project block and shares a common key")
public class OutputsBlock extends NamedProjectBlock {

    @Column(name = "config_group_id")
    @JoinData(targetTable = "output_config_group", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The config group for this block, contains reference data for the billing type and some other config data")
    private Integer configGroupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_source")
    private PaymentSource paymentSource;

    @JsonIgnore
    @OneToMany(cascade = {}, targetEntity = OutputTableSummaryRecord.class)
    @JoinColumn(name = "block_id", insertable = false, updatable = false)
    private Set<OutputTableSummaryRecord> summaryRecords = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinData(joinType = Join.JoinType.OneToMany, comment = "join to outputs_claim")
    @JoinColumn(name = "block_id")
    private Set<Claim> outputsClaims = new HashSet<>();

    @Transient
    private Set<OutputCategoryAssumption> assumptions;

    @Transient
    private Set<OutputTableEntry> tableData;

    @Transient
    private List<OutputsQuarter> quarters;

    @Transient
    private Set<Integer> populatedYears = new HashSet<>();

    @Transient
    private OutputsQuarter nextClaimableQuarter;

    @Transient
    private boolean projectBudgetExceeded;

    @Transient
    private boolean forecastsExceedingProjectBudget;

    public OutputsBlock() {
    }

    public OutputsBlock(Project project) {
        super(project);
    }

    public boolean isClaimable() {
        boolean paymentsEnabled = project.getProgrammeTemplate().isPaymentsEnabled();
        Integer advancePaymentAmount = project.getAdvancePaymentAmount();
        return paymentsEnabled && project.getStatusType().equals(ProjectStatus.Active) && advancePaymentAmount != null
                && advancePaymentAmount > 0 && !isAdvancePaymentClaimed();
    }

    public boolean getShowAdvancePaymentColumn() {
        OutputsTemplateBlock templateBlock = (OutputsTemplateBlock) project.getTemplate().getSingleBlockByType(ProjectBlockType.Outputs);
        OutputsCostsTemplateBlock outputCostTemplateBlock = (OutputsCostsTemplateBlock) project.getTemplate().getSingleBlockByType(ProjectBlockType.OutputsCosts);

        return templateBlock.getShowAdvancedPaymentColumn() && !outputCostTemplateBlock.isHideAdvancePayment();
    }

    public boolean isAdvancePaymentClaimed() {
        return outputsClaims.stream().anyMatch(a -> Claim.ClaimType.ADVANCE.equals(a.getClaimType()));
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.Outputs;
    }

    @Override
    public boolean isComplete() {
        boolean visited = isVisited();
        OutputsTemplateBlock templateBlock = (OutputsTemplateBlock) project.getTemplate().getSingleBlockByType(ProjectBlockType.Outputs);
        boolean populated = checkAllBaselineDataIsPopulated(templateBlock);
        boolean overspend = checkBudgetExceeded(templateBlock);


        return visited && populated && !overspend;

    }


    public boolean isProjectBudgetExceeded() {
        return projectBudgetExceeded;
    }

    public void setProjectBudgetExceeded(boolean projectBudgetExceeded) {
        this.projectBudgetExceeded = projectBudgetExceeded;
    }

    public boolean isForecastsExceedingProjectBudget() {
        return forecastsExceedingProjectBudget;
    }

    public void setForecastsExceedingProjectBudget(boolean forecastsExceedingProjectBudget) {
        this.forecastsExceedingProjectBudget = forecastsExceedingProjectBudget;
    }

    private boolean checkBudgetExceeded(OutputsTemplateBlock templateBlock) {
        if (OutputsTemplateBlock.OutputGroupType.ByQuarter.equals(templateBlock.getOutputGroupType())) {
            OutputsCostsBlock costs = (OutputsCostsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.OutputsCosts);
            if (costs != null) {
                BigDecimal totalProjectSpend = costs.getTotalProjectSpend();
                return totalProjectSpend != null && getTotalClaimed().compareTo(totalProjectSpend) > 0;
            }
        }
        return false;
    }

    private boolean checkAllBaselineDataIsPopulated(OutputsTemplateBlock templateBlock) {
        if (templateBlock.isShowBaselines() != null && templateBlock.isShowBaselines()) {
            for (OutputTableSummaryRecord summaryRecord : summaryRecords) {
                if (summaryRecord.getBaseline() == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public BigDecimal getTotalClaimed() {
        return outputsClaims.stream()
                .filter(c -> Claim.ClaimType.QUARTER.equals(c.getClaimType()))
                .map(Claim::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Claim getAdvancePaymentClaim() {
        return outputsClaims.stream().filter(c -> Claim.ClaimType.ADVANCE.equals(c.getClaimType())).findFirst().orElse(null);
    }

    @Override
    protected void generateValidationFailures() {
        OutputsTemplateBlock templateBlock = (OutputsTemplateBlock) project.getTemplate().getSingleBlockByType(ProjectBlockType.Outputs);

        if (!checkAllBaselineDataIsPopulated(templateBlock)) {
            this.addErrorMessage("Baselines", "Baselines", "A baseline value must be entered for each category and sub category you have included in your financial year breakdown");
        }
        if (checkBudgetExceeded(templateBlock)) {
            this.addErrorMessage("Budget", "quarterly", "The claimed amount has exceeded the total project budget, please reduce claimed amount before submitting.");
        }
    }

    public Set<OutputTableEntry> getTableData() {
        return tableData;
    }

    public void setTableData(Set<OutputTableEntry> tableData) {
        this.tableData = tableData;
    }

    public List<OutputsQuarter> getQuarters() {
        return quarters;
    }

    public void setQuarters(List<OutputsQuarter> quarters) {
        this.quarters = quarters;
    }

    public Set<Integer> getPopulatedYears() {
        return populatedYears;
    }

    public void setPopulatedYears(Set<Integer> populatedYears) {
        this.populatedYears = populatedYears;
    }

    public Integer getConfigGroupId() {
        return configGroupId;
    }

    public void setConfigGroupId(Integer configGroupId) {
        this.configGroupId = configGroupId;
    }


    public Set<OutputTableSummaryRecord> getSummaryRecords() {
        return summaryRecords;
    }

    public void setSummaryRecords(Set<OutputTableSummaryRecord> summaryRecords) {
        this.summaryRecords = summaryRecords;
    }

    public Set<OutputCategoryAssumption> getAssumptions() {
        return assumptions;
    }

    public void setAssumptions(Set<OutputCategoryAssumption> assumptions) {
        this.assumptions = assumptions;
    }

    public Set<Claim> getOutputsClaims() {
        return outputsClaims;
    }

    public void setOutputsClaims(Set<Claim> outputsClaims) {
        this.outputsClaims = outputsClaims;
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        super.copyBlockContentInto(target);
        if (ProjectBlockType.Outputs.equals(target.getBlockType())) {
            OutputsBlock otb = (OutputsBlock) target;
            otb.setConfigGroupId(this.getConfigGroupId());

            if (otb.getAssumptions() == null) {
                otb.setAssumptions(new HashSet<>());
            }
            for (Claim claim : this.getOutputsClaims()) {
                Claim clonedClaim = claim.clone(otb.getId());
                otb.getOutputsClaims().add(clonedClaim);
            }
        }

    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        if (ProjectBlockType.Outputs.equals(templateBlock.getBlock())) {
            OutputsTemplateBlock otb = (OutputsTemplateBlock) templateBlock;
            this.setConfigGroupId(otb.getOutputConfigurationGroup().getId());
        }
    }

    public List<OutputSummaryGroup> getOutputSummaries() {

        if (summaryRecords == null || summaryRecords.size() == 0) {
            return Collections.emptyList();
        }

        Map<String, OutputSummaryGroup> responseMap = new HashMap<>();

        for (OutputTableSummaryRecord summaryRecord : summaryRecords) {
            OutputSummaryGroup toUse;
            toUse = responseMap.get(getKeyFromSummaryRecord(summaryRecord));
            if (toUse == null) {
                toUse = new OutputSummaryGroup(summaryRecord.getOutputType(), summaryRecord.getValueType(), summaryRecord.getCategory());
                responseMap.put(getKeyFromSummaryRecord(summaryRecord), toUse);
            }
            toUse.addOutputTableSummaryRecord(summaryRecord);
        }
        return responseMap.values().stream().sorted().collect(Collectors.toList());
    }

    private String getKeyFromSummaryRecord(OutputTableSummaryRecord record) {
        return record.getOutputType() + ":" + record.getValueType().name() + ":" + record.getCategory();
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock approved, ProjectDifferences differences) {
        super.compareBlockSpecificContent(approved, differences);

        OutputsBlock approvedOutputs = (OutputsBlock) approved;

        Map<String, OutputTableSummaryRecord> otherSummary = approvedOutputs.getSummaryRecords().stream().
                collect(Collectors.toMap(OutputTableSummaryRecord::getComparisonId, Function.identity()));
        Map<String, OutputTableSummaryRecord> thisSummary = this.getSummaryRecords().stream().
                collect(Collectors.toMap(OutputTableSummaryRecord::getComparisonId, Function.identity()));

        // compare both items first
        for (Iterator<String> iterator = thisSummary.keySet().iterator(); iterator.hasNext(); ) {
            String key = iterator.next();

            OutputTableSummaryRecord thisRecord = thisSummary.get(key);
            OutputTableSummaryRecord otherRecord = otherSummary.remove(key);
            if (otherRecord != null) {
                iterator.remove();
                if (!Objects.equals(thisRecord.getActual(), otherRecord.getActual())) {
                    differences.add(new ProjectDifference(thisRecord, "actual"));
                }
                if (!Objects.equals(thisRecord.getForecast(), otherRecord.getForecast())) {
                    differences.add(new ProjectDifference(thisRecord, "forecast"));
                }
                if (!Objects.equals(thisRecord.getTotal(), otherRecord.getTotal())) {
                    differences.add(new ProjectDifference(thisRecord, "total"));
                }
            }
        }

        // added in this unapproved version
        for (OutputTableSummaryRecord record : thisSummary.values()) {
            differences.add(new ProjectDifference(record, ProjectDifference.DifferenceType.Addition));
        }
        // deleted by this unapproved version
        for (OutputTableSummaryRecord record : otherSummary.values()) {
            differences.add(new ProjectDifference(record, ProjectDifference.DifferenceType.Deletion));
        }

        Claim thisAdvanceClaim = getAdvancePaymentClaim();
        Claim otherAdvanceClaim = approvedOutputs.getAdvancePaymentClaim();

        if (thisAdvanceClaim != null && otherAdvanceClaim != null) {
            if (!Objects.equals(thisAdvanceClaim.getAmount(), otherAdvanceClaim.getAmount())) {
                differences.add(new ProjectDifference(thisAdvanceClaim, "amount"));
            }

            if (!Objects.equals(thisAdvanceClaim.getClaimedOn(), otherAdvanceClaim.getClaimedOn())) {
                differences.add(new ProjectDifference(thisAdvanceClaim, "claimedOn"));
            }

            if (!Objects.equals(thisAdvanceClaim.getClaimStatus(), otherAdvanceClaim.getClaimStatus())) {
                differences.add(new ProjectDifference(thisAdvanceClaim, "claimStatus"));
            }
        }
    }

    protected void performPostApprovalActions(String username, OffsetDateTime approvalTime) {
        Set<Claim> outputsClaims = this.getOutputsClaims();
        if (outputsClaims != null) {
            for (Claim outputsClaim : this.getOutputsClaims()) {
                if (Claim.ClaimType.ADVANCE.equals(outputsClaim.getClaimType())) {
                    OutputsCostsBlock costs = (OutputsCostsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.OutputsCosts);
                    outputsClaim.setAmount(new BigDecimal(costs.getAdvancePayment()));
                }
                outputsClaim.setClaimStatus(ClaimStatus.Approved);
            }
        }
    }

    @Override
    public boolean getApprovalWillCreatePendingPayment() {
        return getOutputsClaims().stream().anyMatch(c -> ClaimStatus.Claimed.equals(c.getClaimStatus()));
    }

    public OutputsQuarter getNextClaimableQuarter() {
        return nextClaimableQuarter;
    }

    public void setNextClaimableQuarter(OutputsQuarter nextClaimableQuarter) {
        this.nextClaimableQuarter = nextClaimableQuarter;
    }
}