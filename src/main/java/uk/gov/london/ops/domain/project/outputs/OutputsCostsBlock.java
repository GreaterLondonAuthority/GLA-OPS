/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project.outputs;

import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.template.OutputsCostsTemplateBlock;
import uk.gov.london.ops.domain.template.TemplateBlock;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity(name = "outputs_costs_block")
@DiscriminatorValue("OUTPUTS_COSTS")
@JoinData(sourceTable = "outputs_costs_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the outputs costs block is a subclass of the project block and shares a common key")
public class OutputsCostsBlock extends NamedProjectBlock implements FundingSourceProvider {

    @Column(name = "total_project_spend")
    private BigDecimal totalProjectSpend;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = OutputCategoryCost.class)
    @JoinColumn(name = "block_id")
    private List<OutputCategoryCost> categoriesCosts = new ArrayList<>();

    @Column(name = "hide_advance_payment")
    private boolean hideAdvancePayment;

    @Column(name = "advance_payment")
    private Integer advancePayment;

    @Column(name = "selected_recovery_output_id")
    private Integer selectedRecoveryOutputId;


    public OutputsCostsBlock() {}

    public OutputsCostsBlock(Project project) {
        super(project);
    }

    public BigDecimal getTotalProjectSpend() {
        return totalProjectSpend;
    }

    public void setTotalProjectSpend(BigDecimal totalProjectSpend) {
        this.totalProjectSpend = totalProjectSpend;
    }

    public List<OutputCategoryCost> getCategoriesCosts() {
        return categoriesCosts;
    }

    public OutputCategoryCost getCategoriesCost(Integer id) {
        return categoriesCosts.stream().filter(c -> c.getOutputCategoryConfigurationId().equals(id)).findFirst().orElse(null);
    }

    public void setCategoriesCosts(List<OutputCategoryCost> categoriesCosts) {
        this.categoriesCosts = categoriesCosts;
    }

    public boolean isHideAdvancePayment() {
        return hideAdvancePayment;
    }

    public void setHideAdvancePayment(boolean hideAdvancePayment) {
        this.hideAdvancePayment = hideAdvancePayment;
    }

    public Integer getAdvancePayment() {
        return advancePayment;
    }

    public void setAdvancePayment(Integer advancePayment) {
        this.advancePayment = advancePayment;
    }

    public Integer getSelectedRecoveryOutputId() {
        return selectedRecoveryOutputId;
    }

    public void setSelectedRecoveryOutputId(Integer selectedRecoveryOutputId) {
        this.selectedRecoveryOutputId = selectedRecoveryOutputId;
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.OutputsCosts;
    }

    @Override
    public boolean isComplete() {

        if ((totalProjectSpend == null) || (!hideAdvancePayment && advancePayment == null) || (selectedRecoveryOutputId == null)) {
            return false;
        }

        boolean hasAtLeastOneUnitCost = categoriesCosts.stream().anyMatch(occ -> occ.getUnitCost() != null
                && occ.getUnitCost().compareTo(BigDecimal.ZERO) > 0);

        return isVisited() && hasAtLeastOneUnitCost;
    }

    @Override
    protected void generateValidationFailures() {}

    @Override
    public void merge(NamedProjectBlock block) {
        OutputsCostsBlock other = (OutputsCostsBlock) block;
        this.setTotalProjectSpend(other.getTotalProjectSpend());
        this.getCategoriesCosts().clear();
        this.getCategoriesCosts().addAll(other.getCategoriesCosts());
        this.setHideAdvancePayment(other.isHideAdvancePayment());
        this.setAdvancePayment(other.getAdvancePayment());
        this.setSelectedRecoveryOutputId(other.getSelectedRecoveryOutputId());
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        OutputsCostsBlock clone = (OutputsCostsBlock) target;
        clone.setTotalProjectSpend(this.getTotalProjectSpend());
        for (OutputCategoryCost occ: this.getCategoriesCosts()) {
            clone.getCategoriesCosts().add(occ.clone());
        }
        clone.setHideAdvancePayment(this.isHideAdvancePayment());
        clone.setAdvancePayment(this.getAdvancePayment());
        clone.setSelectedRecoveryOutputId(this.getSelectedRecoveryOutputId());
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {
        OutputsCostsBlock otherBlock = (OutputsCostsBlock) other;

        if (!Objects.equals(this.getTotalProjectSpend(), otherBlock.getTotalProjectSpend())) {
            differences.add(new ProjectDifference(this, "totalProjectSpend"));
        }

        if (!Objects.equals(this.getAdvancePayment(), otherBlock.getAdvancePayment())) {
            differences.add(new ProjectDifference(this, "advancePayment"));
        }

        for (OutputCategoryCost occ: this.getCategoriesCosts()) {
            OutputCategoryCost otherOcc = otherBlock.getCategoriesCost(occ.getOutputCategoryConfigurationId());
            if (!Objects.equals(occ.getUnitCost(), otherOcc.getUnitCost())) {
                differences.add(new ProjectDifference(occ, "unitCost"));
            }
        }
    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        if(templateBlock instanceof OutputsCostsTemplateBlock) {
            this.setHideAdvancePayment(((OutputsCostsTemplateBlock) templateBlock).isHideAdvancePayment());
            this.setSelectedRecoveryOutputId(((OutputsCostsTemplateBlock) templateBlock).getDefaultRecoveryOutputId());
        }
    }

    public BigDecimal getUnitCostForOutput(Integer outputCategoryConfigurationId) {
        OutputCategoryCost outputCategoryCost = categoriesCosts.stream().filter(a -> outputCategoryConfigurationId.equals(a.getOutputCategoryConfigurationId())).findFirst().orElse(null);
        return outputCategoryCost == null ? null : outputCategoryCost.getUnitCost();
    }

    @Override
    public Map<GrantType, Long> getFundingRequested() {
        Map<GrantType, Long> grantsRequested = new HashMap<>();
        grantsRequested.put(GrantType.Grant, this.getTotalProjectSpend() == null ? 0 : this.getTotalProjectSpend().longValue());
        return grantsRequested;

    }

    public boolean isEditable() {
        boolean editable = super.isEditable();

        OutputsBlock outputs = (OutputsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Outputs);
        if (outputs != null && outputs.getAdvancePaymentClaim() != null && ClaimStatus.Approved.equals(outputs.getAdvancePaymentClaim().getClaimStatus())) {
            return false;
        }
        return editable;
    }
}
