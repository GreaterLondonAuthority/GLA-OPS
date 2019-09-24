/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import uk.gov.london.ops.refdata.OutputConfigurationGroup;
import uk.gov.london.ops.domain.project.ProjectBlockType;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;

/**
 * empty subclass as no specific information is currently required.
 * Created by chris on 16/02/2017.
 */
@Entity
@DiscriminatorValue("OUTPUTS")
public class OutputsTemplateBlock extends TemplateBlock {

    public enum OutputGroupType {ByQuarter, ByCategory};

    @JoinData(joinType = Join.JoinType.OneToOne, sourceTable = "template_block", sourceColumn = "output_config_group_id",
            targetColumn = "id", targetTable = "output_config_group", comment = "")
    @OneToOne
    @JoinColumn(name =  "output_config_group_id")
    private OutputConfigurationGroup outputConfigurationGroup;

    @Column(name="show_baseline_for_outputs")
    private Boolean showBaselines = Boolean.FALSE;

    @Column(name="show_assumptions_for_outputs")
    private Boolean showAssumptions = Boolean.FALSE;

    @Column(name="show_total_project_outputs_table")
    private Boolean showTotalProjectOutputsTable = Boolean.TRUE;

    @Column(name="show_input_value_column")
    private Boolean showValueColumn;

    @Column(name="show_output_type_column")
    private Boolean showOutputTypeColumn;

    @Column(name="output_group_type")
    @Enumerated(EnumType.STRING)
    private OutputGroupType outputGroupType;

    @Column(name="show_forecast_total_column")
    private Boolean showForecastTotalColumn;

    @Column(name="show_claimable_amount_column")
    private Boolean showClaimableAmountColumn;

    @Column(name="show_actual_total_column")
    private Boolean showActualTotalColumn;

    @Column(name = "show_advanced_payment_balance_column")
    private boolean showAdvancedPaymentColumn;

    public OutputsTemplateBlock() {
        super(ProjectBlockType.Outputs);
    }

    public OutputsTemplateBlock(Integer displayOrder) {
        super(displayOrder, ProjectBlockType.Outputs);
    }

    public OutputsTemplateBlock(Integer displayOrder,String blockDisplayName) {
        super(displayOrder,  ProjectBlockType.Outputs, blockDisplayName);
    }

    public OutputConfigurationGroup getOutputConfigurationGroup() {
        return outputConfigurationGroup;
    }

    public void setOutputConfigurationGroup(OutputConfigurationGroup outputConfigurationGroup) {
        this.outputConfigurationGroup = outputConfigurationGroup;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        OutputsTemplateBlock block = (OutputsTemplateBlock) clone;
        block.setOutputConfigurationGroup(this.getOutputConfigurationGroup());
        block.setShowBaselines(this.isShowBaselines());
        block.setShowAssumptions(this.isShowAssumptions());
        block.setShowTotalProjectOutputsTable(this.showTotalProjectOutputsTable);
        block.setShowValueColumn(this.showValueColumn);
        block.setShowOutputTypeColumn(this.showOutputTypeColumn);
        block.setOutputGroupType(this.getOutputGroupType());
        block.setShowForecastTotalColumn(this.getShowForecastTotalColumn());
        block.setShowClaimableAmountColumn(this.getShowClaimableAmountColumn());
        block.setShowActualTotalColumn(this.getShowActualTotalColumn());
        block.setShowAdvancedPaymentColumn(this.getShowAdvancedPaymentColumn());
    }

    public Boolean isShowBaselines() {
        return showBaselines;
    }

    public void setShowBaselines(Boolean showBaselines) {
        this.showBaselines = showBaselines;
    }

    public Boolean isShowAssumptions() {
        return showAssumptions;
    }

    public void setShowAssumptions(Boolean showAssumptions) {
        this.showAssumptions = showAssumptions;
    }

    public Boolean getShowTotalProjectOutputsTable() {
        return showTotalProjectOutputsTable;
    }

    public void setShowTotalProjectOutputsTable(Boolean showTotalProjectOutputsTable) {
        this.showTotalProjectOutputsTable = showTotalProjectOutputsTable;
    }

    public Boolean getShowValueColumn() {
        return showValueColumn;
    }

    public void setShowValueColumn(Boolean showValueColumn) {
        this.showValueColumn = showValueColumn;
    }

    public Boolean getShowOutputTypeColumn() {
        return showOutputTypeColumn;
    }

    public void setShowOutputTypeColumn(Boolean showOutputTypeColumn) {
        this.showOutputTypeColumn = showOutputTypeColumn;
    }

    public OutputGroupType getOutputGroupType() {
        return outputGroupType;
    }

    public void setOutputGroupType(OutputGroupType outputGroupType) {
        this.outputGroupType = outputGroupType;
    }

    public Boolean getShowForecastTotalColumn() {
        return showForecastTotalColumn;
    }

    public void setShowForecastTotalColumn(Boolean showForecastTotalColumn) {
        this.showForecastTotalColumn = showForecastTotalColumn;
    }

    public Boolean getShowClaimableAmountColumn() {
        return showClaimableAmountColumn;
    }

    public void setShowClaimableAmountColumn(Boolean showClaimableAmountColumn) {
        this.showClaimableAmountColumn = showClaimableAmountColumn;
    }

    public Boolean getShowActualTotalColumn() {
        return showActualTotalColumn;
    }

    public void setShowActualTotalColumn(Boolean showActualTotalColumn) {
        this.showActualTotalColumn = showActualTotalColumn;
    }

    public boolean getShowAdvancedPaymentColumn() {
        return showAdvancedPaymentColumn;
    }

    public void setShowAdvancedPaymentColumn(boolean showAdvancedPaymentColumn) {
        this.showAdvancedPaymentColumn = showAdvancedPaymentColumn;
    }

}
