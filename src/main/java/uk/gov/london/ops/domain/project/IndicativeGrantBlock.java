/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.template.IndicativeTenureConfiguration;
import uk.gov.london.ops.domain.template.TemplateTenureType;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.*;

/**
 * Created by chris on 13/10/2016.
 */
@Entity(name = "tenure_block")
@DiscriminatorValue("Indicative")
@JoinData(sourceTable = "tenure_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the indicative grant block is a subclass of the project block and shares a common key")
public class IndicativeGrantBlock extends BaseGrantBlock {


    public IndicativeGrantBlock() {
        setBlockType(ProjectBlockType.IndicativeGrant);
    }

    public IndicativeGrantBlock(Project project) {
        this.project = project;
    }

    @Transient
    public Long getTotalGrantEligibility() {
        long total = 0L;
        for (TenureTypeAndUnits tenureTypeAndUnits : getTenureTypeAndUnitsEntries()) {
            for (IndicativeTenureValue indicativeTenureValue : tenureTypeAndUnits.getIndicativeTenureValues()) {
                if (indicativeTenureValue.getUnits() != null) {
                    total += indicativeTenureValue.getUnits() * tenureTypeAndUnits.getTenureType().getTariffRate();
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

        List<TenureTypeAndUnits> list = getTenureTypeAndUnitsEntriesSorted();

        for (TenureTypeAndUnits tenureTypeAndUnits : list) {

            for (IndicativeTenureValue indicativeTenureValue : tenureTypeAndUnits.getIndicativeTenureValuesSorted()) {
                TenureSummaryDetails tsd = new TenureSummaryDetails();
                tsd.setName(tenureTypeAndUnits.getTenureType().getName());
                tsd.setYear(indicativeTenureValue.getYear());

                if (indicativeTenureValue.getUnits() != null && indicativeTenureValue.getUnits() > 0) {
                    tsd.setGrantEligibleUnits(indicativeTenureValue.getUnits());
                    tsd.setGrantRate(tenureTypeAndUnits.getTenureType().getTariffRate());
                    tsd.setTotalGrant(indicativeTenureValue.getUnits() * new Long(tenureTypeAndUnits.getTenureType().getTariffRate()));
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
    protected boolean isRowValid(TenureTypeAndUnits tenureTypeAndUnit) {
        Project.Status status = this.project.getStatus();
        boolean oneValid = false;
        for (IndicativeTenureValue indicativeTenureValue : tenureTypeAndUnit.getIndicativeTenureValues()) {
            if (indicativeTenureValue.getUnits() != null) {
                int units = indicativeTenureValue.getUnits();
                if (units < 0) {
                    return false;
                } else if (units > 0) {
                    oneValid = true;
                } else if(units == 0) {
                    if(!(status == Project.Status.Draft || status == Project.Status.Returned)) {
                        oneValid = true;
                    }
                }
            } else {
                return false;
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
    public void calculateTotals(TenureTypeAndUnits tenureInfo) {
        tenureInfo.setEligibleUnits(totalIndicativeUnits(tenureInfo));
        tenureInfo.setGrantPerUnit(tenureInfo.getTenureType().getTariffRate());
        tenureInfo.setEligibleGrant((long) (tenureInfo.getEligibleUnits() * tenureInfo.getGrantPerUnit()));
    }

    private int totalIndicativeUnits(TenureTypeAndUnits tenureInfo) {
        int totalUnits = 0;
        for (IndicativeTenureValue indicativeTenureValue : tenureInfo.getIndicativeTenureValues()) {
            totalUnits += indicativeTenureValue.getUnits();
        }
        return totalUnits;
    }

    @Override
    protected void generateValidationFailures() {
        super.generateValidationFailures();

        validateRows(this.getTenureTypeAndUnitsEntries());

    }

    private void validateRows(Set<TenureTypeAndUnits> tenureTypeAndUnits) {
        boolean oneRowValid = false;

        for (TenureTypeAndUnits tenureTypeAndUnit : tenureTypeAndUnits) {

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
                if (nullPresent) {
                    this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()), "Row",
                            "No empty values are permitted for this row");
                }
            }
        }


        if (!oneRowValid) {
            this.addErrorMessage("Block1", "", "At least one valid row must exist");
        }


    }



    @Transient
    public void initialiseFromTenureTypes(Set<TemplateTenureType> tenureTypes) {
        HashSet<TenureTypeAndUnits> tenureTypeAndUnitsEntries = new HashSet<>();
        this.setTenureTypeAndUnitsEntries(tenureTypeAndUnitsEntries);
        if (tenureTypes !=null) {
            for (TemplateTenureType tenureType : tenureTypes) {
                TenureTypeAndUnits tenureEntry = new TenureTypeAndUnits(project);
                tenureTypeAndUnitsEntries.add(tenureEntry);
                tenureEntry.setTenureType(tenureType);
                HashSet<IndicativeTenureValue> indicativeTenureValues = new HashSet<>();
                tenureEntry.setIndicativeTenureValues(indicativeTenureValues);

                int numberOfYears = Math.min(IndicativeTenureConfiguration.MAX_NUMBER_OF_TENURE_YEARS, getProject().getTemplate().getIndicativeTenureConfiguration().getIndicativeTenureNumberOfYears());
                for (int i=0 ; i < numberOfYears ;  i++) {
                    IndicativeTenureValue itv = new IndicativeTenureValue(getProject().getTemplate().getIndicativeTenureConfiguration().getIndicativeTenureStartYear() + i, 0);
                    indicativeTenureValues.add(itv);
                }

            }
        }
    }

    @Transient
    public Totals getTotals() {

        int [] results = null;


        for (TenureTypeAndUnits tenureTypeAndUnits : this.getTenureTypeAndUnitsEntries()) {
            List<IndicativeTenureValue> valuesSorted = tenureTypeAndUnits.getIndicativeTenureValuesSorted();
            if (results == null) {
                results = new int[valuesSorted.size()];
            }

            for (int i = 0; i < valuesSorted.size(); i++) {
                if (valuesSorted.get(i) != null && valuesSorted.get(i).getUnits() != null) {
                    results[i] += valuesSorted.get(i).getUnits();
                }
            }

        }

        return new Totals(results);
    }


    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock otherBlock, ProjectDifferences differences) {
        super.compareBlockSpecificContent(otherBlock, differences);

        IndicativeGrantBlock otherIndicativeGrantBlock = (IndicativeGrantBlock) otherBlock;

        List<TenureTypeAndUnits> thisTenure = this.getTenureTypeAndUnitsEntriesSorted();
        List<TenureTypeAndUnits> otherTenure = otherIndicativeGrantBlock.getTenureTypeAndUnitsEntriesSorted();

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

        Totals thisTotals = this.getTotals();
        Totals otherTotals = otherIndicativeGrantBlock.getTotals();
        List<IndicativeTenureValue> thisUnits = thisTenure.get(0).getIndicativeTenureValuesSorted();

        for (int i = 0; i < thisTotals.getResultsByYear().length; i++) {
            // compare totals row
            if (!Objects.equals(thisTotals.getResultsByYear()[i], otherTotals.getResultsByYear()[i])) {
                differences.add(new ProjectDifference(String.valueOf(thisUnits.get(i).getYear()), "totals"));
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




    public class Totals {

        int [] resultsByYear = null;

        public Totals(int [] results) {
           this.resultsByYear = results;
        }

        public int[] getResultsByYear() {
            return resultsByYear;
        }
    }


}
