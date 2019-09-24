/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.template.IndicativeGrantTemplateBlock;
import uk.gov.london.ops.domain.template.TemplateBlock;
import uk.gov.london.ops.domain.template.TemplateTenureType;
import uk.gov.london.ops.domain.template.TenureYear;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.nullSafeAdd;

/**
 * Created by chris on 13/10/2016.
 */
@Entity(name = "tenure_block")
@DiscriminatorValue("Indicative")
@JoinData(sourceTable = "tenure_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the indicative grant block is a subclass of the project block and shares a common key")
public class
IndicativeGrantBlock extends BaseGrantBlock {


    public IndicativeGrantBlock() {
        setBlockType(ProjectBlockType.IndicativeGrant);
    }

    public IndicativeGrantBlock(Project project) {
        this.project = project;
    }

    @Transient
    public Long getTotalGrantEligibility() {
        long total = 0L;
        for (ProjectTenureDetails projectTenureDetails : getTenureTypeAndUnitsEntries()) {
            for (IndicativeTenureValue indicativeTenureValue : projectTenureDetails.getIndicativeTenureValues()) {
                if (indicativeTenureValue.getUnits() != null) {
                    total += indicativeTenureValue.getUnits() * getTariffRate(projectTenureDetails, indicativeTenureValue);
                }
            }
        }
        return total;
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.IndicativeGrant;
    }

    @Override
    public List<TenureSummaryDetails> getTenureSummaryDetails() {
        List<TenureSummaryDetails> details = new ArrayList<>();

        List<ProjectTenureDetails> list = getTenureTypeAndUnitsEntriesSorted();

        for (ProjectTenureDetails projectTenureDetails : list) {

            for (IndicativeTenureValue indicativeTenureValue : projectTenureDetails.getIndicativeTenureValuesSorted()) {
                TenureSummaryDetails tsd = new TenureSummaryDetails();
                tsd.setName(projectTenureDetails.getTenureType().getName());
                tsd.setYear(indicativeTenureValue.getYear());

                if (indicativeTenureValue.getUnits() != null && indicativeTenureValue.getUnits() > 0) {
                    tsd.setGrantEligibleUnits(indicativeTenureValue.getUnits());
                    tsd.setGrantRate(getTariffRate(projectTenureDetails, indicativeTenureValue));
                    tsd.setTotalGrant(indicativeTenureValue.getUnits() * tsd.getGrantRate().longValue());
                } else {
                    tsd.setGrantEligibleUnits(0);
                    tsd.setGrantRate(0);
                    tsd.setTotalGrant(0L);
                }

                details.add(tsd);
            }

        }
        return details;
    }



    @JsonIgnore
    @Transient
    protected boolean isRowValid(ProjectTenureDetails tenureTypeAndUnit) {
        ProjectStatus status = this.project.getStatusType();
        boolean oneValid = false;
        for (IndicativeTenureValue indicativeTenureValue : tenureTypeAndUnit.getIndicativeTenureValues()) {
            if (indicativeTenureValue.getUnits() != null) {
                int units = indicativeTenureValue.getUnits();
                if (units < 0) {
                    return false;
                } else if (units > 0) {
                    oneValid = true;
                } else if(units == 0) {
                    if(!(status == ProjectStatus.Draft || status == ProjectStatus.Returned)) {
                        oneValid = true;
                    }
                }
            }
        }

        return oneValid;
    }

    @Transient
    @Override
    public void merge(BaseGrantBlock newValue) {
        IndicativeGrantBlock block = (IndicativeGrantBlock) newValue;
        super.merge(block);
    }

    @Override
    public void calculateTotals(ProjectTenureDetails tenureInfo) {
        Long eligibleGrant = 0L;
        for (IndicativeTenureValue indicativeTenureValue: tenureInfo.getIndicativeTenureValues()) {
            eligibleGrant += getTariffRate(tenureInfo, indicativeTenureValue);
        }
        tenureInfo.setEligibleUnits(totalIndicativeUnits(tenureInfo));
        tenureInfo.setGrantPerUnit(tenureInfo.getTenureType().getTariffRate());
        tenureInfo.setEligibleGrant(eligibleGrant);
    }

    private int getTariffRate(ProjectTenureDetails projectTenureDetails, IndicativeTenureValue indicativeTenureValue) {
        for (TenureYear tenureYear: getProject().getTemplate().getTenureYears()) {
            if (Objects.equals(tenureYear.getExternalId(), projectTenureDetails.getTenureType().getExternalId()) && Objects.equals(tenureYear.getYear(), indicativeTenureValue.getYear())) {
                return tenureYear.getTariffRate();
            }
        }
        return projectTenureDetails.getTenureType().getTariffRate();
    }

    private int totalIndicativeUnits(ProjectTenureDetails tenureInfo) {
        int totalUnits = 0;
        for (IndicativeTenureValue indicativeTenureValue : tenureInfo.getIndicativeTenureValues()) {
//            Integer units = indicativeTenureValue.getUnits();
//            if(units != null){
                totalUnits = nullSafeAdd(totalUnits, indicativeTenureValue.getUnits());
//                totalUnits += units;
//            }
        }
        return totalUnits;
    }

    @Override
    protected void generateValidationFailures() {
        super.generateValidationFailures();

        validateRows(this.getTenureTypeAndUnitsEntries());

    }

    private void validateRows(Set<ProjectTenureDetails> projectTenureTypeAndUnits) {
        boolean oneRowValid = false;

        IndicativeGrantTemplateBlock indicativeTemplateBlock = (IndicativeGrantTemplateBlock) project.getTemplate().getSingleBlockByType(ProjectBlockType.IndicativeGrant);
        if (indicativeTemplateBlock.isAllowZeroUnits()) {
            return;
        }

        for (ProjectTenureDetails tenureTypeAndUnit : projectTenureTypeAndUnits) {

            if (isRowValid(tenureTypeAndUnit)) {
                oneRowValid = true;
            } else {

                boolean nullPresent = false;

                for (IndicativeTenureValue indicativeTenureValue : tenureTypeAndUnit.getIndicativeTenureValues()) {
                    if (indicativeTenureValue.getUnits() == null) {
                        nullPresent = true;
                    } else {
                        if (indicativeTenureValue.getUnits() < 0)  {
                            this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()), String.valueOf(indicativeTenureValue.getYear()),
                                    "Only positive numbers are permitted");
                        }
                    }
                }
            }
        }


        if (!oneRowValid) {
            this.addErrorMessage("Block1", "", "At least one valid row must exist");
        }


    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        Map<Integer, ProjectTenureDetails> tenureTypeAndUnitsEntries = new HashMap<>();

        Map<Integer, TemplateTenureType> tenureTypes = getProject().getTemplate().getTenureTypes().stream().collect(Collectors.toMap(TemplateTenureType::getExternalId, Function.identity()));

        for (TenureYear tenureYear: getProject().getTemplate().getTenureYears()) {
            ProjectTenureDetails tenureEntry = tenureTypeAndUnitsEntries.get(tenureYear.getExternalId());
            if (tenureEntry == null) {
                tenureEntry = new ProjectTenureDetails(project);
                tenureEntry.setTenureType(tenureTypes.get(tenureYear.getExternalId()));
                tenureTypeAndUnitsEntries.put(tenureYear.getExternalId(), tenureEntry);
            }

            IndicativeTenureValue itv = new IndicativeTenureValue(tenureYear.getYear());
            tenureEntry.getIndicativeTenureValues().add(itv);
        }

        this.setTenureTypeAndUnitsEntries(new HashSet<>(tenureTypeAndUnitsEntries.values()));
    }

    @Transient
    public Map<Integer, Integer> getTotals() {
        Map<Integer, Integer> totals = new HashMap<>();

        for (ProjectTenureDetails projectTenureDetails : this.getTenureTypeAndUnitsEntries()) {
            for (IndicativeTenureValue value: projectTenureDetails.getIndicativeTenureValuesSorted()) {
                totals.putIfAbsent(value.getYear(), 0);
                totals.put(value.getYear(), nullSafeAdd(totals.get(value.getYear()), value.getUnits()));
            }
        }

        return totals;
    }


    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock otherBlock, ProjectDifferences differences) {
        super.compareBlockSpecificContent(otherBlock, differences);

        IndicativeGrantBlock otherIndicativeGrantBlock = (IndicativeGrantBlock) otherBlock;

        List<ProjectTenureDetails> thisTenure = this.getTenureTypeAndUnitsEntriesSorted();
        List<ProjectTenureDetails> otherTenure = otherIndicativeGrantBlock.getTenureTypeAndUnitsEntriesSorted();

        // compare each tenure type
        for (int i = 0; i < thisTenure.size(); i++) {
            List<IndicativeTenureValue> thisUnits = thisTenure.get(i).getIndicativeTenureValuesSorted();
            List<IndicativeTenureValue> otherUnits = otherTenure.get(i).getIndicativeTenureValuesSorted();


            for (int j = 0; j < thisUnits.size(); j++) {
                if (!Objects.equals(thisUnits.get(j).getUnits(), otherUnits.get(j).getUnits())) {
                    differences.add(new ProjectDifference(thisTenure.get(i) , thisUnits.get(j), "units"));
                }
            }

        }

        Map<Integer, Integer> thisTotals = this.getTotals();
        Map<Integer, Integer> otherTotals = otherIndicativeGrantBlock.getTotals();

        for (Integer year: thisTotals.keySet()) {
            // compare totals row
            if (!Objects.equals(thisTotals.get(year), otherTotals.get(year))) {
                differences.add(new ProjectDifference(String.valueOf(year), "totals"));
            }
        }

        List<TenureSummaryDetails> thisSummary = this.getTenureSummaryDetails();
        List<TenureSummaryDetails> otherSummary = otherIndicativeGrantBlock.getTenureSummaryDetails();

        // compare tenure summary tiles
        for (int i = 0; i < thisSummary.size(); i++) {

            String compID = thisSummary.get(i).getComparisonId() + ":" + thisSummary.get(i).getYear();

            if (!Objects.equals(thisSummary.get(i).getGrantEligibleUnits(), otherSummary.get(i).getGrantEligibleUnits())) {
                differences.add(new ProjectDifference(compID, "grantEligibleUnits"));
            }

            if (!Objects.equals(thisSummary.get(i).getGrantRate(), otherSummary.get(i).getGrantRate())) {
                differences.add(new ProjectDifference(compID, "grantRate"));
            }

            if (!Objects.equals(thisSummary.get(i).getTotalGrant(), otherSummary.get(i).getTotalGrant())) {
                differences.add(new ProjectDifference(compID, "totalGrant"));
            }
        }

        // compare total eligibilty
        if (!Objects.equals(this.getTotalGrantEligibility(), otherIndicativeGrantBlock.getTotalGrantEligibility())) {
            differences.add(new ProjectDifference(this,"totalGrantEligibility"));
        }
    }

    @Override
    public boolean isBlockRevertable() {
        return true;
    }

}
