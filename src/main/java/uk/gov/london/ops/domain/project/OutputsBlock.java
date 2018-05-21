/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.template.OutputsTemplateBlock;
import uk.gov.london.ops.domain.template.TemplateBlock;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.*;
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

    @JsonIgnore
    @OneToMany(cascade = {}, targetEntity = OutputTableSummaryRecord.class)
    @JoinColumn(name = "block_id", insertable = false, updatable = false)
    private Set<OutputTableSummaryRecord> summaryRecords;

    @Transient
    private Set<OutputTableEntry> tableData;

    @Transient
    private Set<Integer> populatedYears = new HashSet<>();

    public OutputsBlock() {
    }

    public OutputsBlock(Project project) {
        super(project);
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.Outputs;
    }

    @Override
    public boolean isComplete() {
        return isVisited();
    }

    @Override
    protected void generateValidationFailures() {

    }

    public Set<OutputTableEntry> getTableData() {
        return tableData;
    }

    public void setTableData(Set<OutputTableEntry> tableData) {
        this.tableData = tableData;
    }

    public Set<Integer> getPopulatedYears() {
        return populatedYears;
    }

    public void setPopulatedYears(Set<Integer> populatedYears) {
        this.populatedYears = populatedYears;
    }

    public boolean allowMultipleVersions() {
        return true;
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

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        super.copyBlockContentInto(target);
        if (ProjectBlockType.Outputs.equals(target.getBlockType())) {
            OutputsBlock otb = (OutputsBlock) target;
            otb.setConfigGroupId(this.getConfigGroupId());
        }
    }

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
        return record.getOutputType() + ":" +  record.getValueType().name() + ":" + record.getCategory();
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

        // added in this unnaproved version
        for (OutputTableSummaryRecord record : thisSummary.values()) {
            differences.add(new ProjectDifference(record, ProjectDifference.DifferenceType.Addition));
        }
        // deleted by this unapproved version
        for (OutputTableSummaryRecord record : otherSummary.values()) {
            differences.add(new ProjectDifference(record, ProjectDifference.DifferenceType.Deletion));
        }
    }
}