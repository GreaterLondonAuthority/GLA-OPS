/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.template.TemplateTenureType;
import uk.gov.london.ops.exception.ApiErrorItem;
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
@DiscriminatorValue("CALCULATE")
@JoinData(sourceTable = "tenure_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the calculate grant block is a subclass of the project block and shares a common key")
public class CalculateGrantBlock extends BaseGrantBlock {

    public CalculateGrantBlock() {
        setBlockType(ProjectBlockType.CalculateGrant);
    }

    public CalculateGrantBlock(Project project) {
        this();
        this.project = project;

    }

    @Transient
    public Long getTotalGrantEligibility() {
        long totalGrant = 0;
        if (getTenureTypeAndUnitsEntries() != null) {

            for (TenureTypeAndUnits tenureTypeAndUnitsEntry : getTenureTypeAndUnitsEntries()) {
                if (isRowValid(tenureTypeAndUnitsEntry)) {
                    totalGrant += this.getTotalGrant(tenureTypeAndUnitsEntry);
                }
            }
        }
        return totalGrant;
    }


    @Transient
    public Long getTotalGrant(TenureTypeAndUnits typeAndUnits) {
        Long eligibleUnits = new Long(getTotalGrantEligibleUnits(typeAndUnits));
        return eligibleUnits * typeAndUnits.getTenureType().getTariffRate();
    }

    @Override
    public List<TenureSummaryDetails> getTenureSummaryDetails() {
        List<TenureSummaryDetails> details = new ArrayList<>();

        List<TenureTypeAndUnits> list = getTenureTypeAndUnitsEntriesSorted();

        for (TenureTypeAndUnits tenureTypeAndUnits : list) {
            TenureSummaryDetails tsd = new TenureSummaryDetails();
            tsd.setName(tenureTypeAndUnits.getTenureType().getName());
            if (isRowValid(tenureTypeAndUnits)) {
                tsd.setGrantEligibleUnits(getTotalGrantEligibleUnits(tenureTypeAndUnits));
                tsd.setGrantRate(tenureTypeAndUnits.getTenureType().getTariffRate());
                tsd.setTotalGrant(tsd.getGrantEligibleUnits() * new Long(tsd.getGrantRate()));
            } else {
                tsd.setGrantEligibleUnits(0);
                tsd.setGrantRate(0);
                tsd.setTotalGrant(0L);
            }
            details.add(tsd);
        }
        return details;
    }


    @JsonIgnore
    @Transient
    protected boolean isRowValid(TenureTypeAndUnits tenureItem) {
        if (tenureItem.getTotalCost() != null && tenureItem.getTotalUnits() != null && tenureItem.getTotalUnits() != 0 && tenureItem.getTotalCost() != 0) {
            if (tenureItem.getTotalUnits() - (tenureItem.getS106Units() == null ? 0 : tenureItem.getS106Units()) >= 0) {
                return true;
            }
        }
        return false;
    }


    @JsonIgnore
    @Transient
    public Integer getTotalGrantEligibleUnits(TenureTypeAndUnits tenureItem) {
        if (tenureItem.getTenureType().isZeroUnitEntry()) {
            return 0;
        }
        if (isRowValid(tenureItem)) {
            return tenureItem.getTotalUnits() - (tenureItem.getS106Units() == null ? 0 : tenureItem.getS106Units());
        }
        return 0;
    }

    @Transient
    public Totals getTotals() {
        return new Totals();
    }

    @Override
    protected void generateValidationFailures() {
        super.generateValidationFailures();

        // must be at leave one row correctly filled, and other rows empty
        boolean atLeastOneValidRow = false;
        for (TenureTypeAndUnits tenureTypeAndUnits : this.getTenureTypeAndUnitsEntries()) {

            if (isRowValid(tenureTypeAndUnits)) {
                atLeastOneValidRow = true;
            } else {
                long totalCost = tenureTypeAndUnits.getTotalCost() == null ? 0 : tenureTypeAndUnits.getTotalCost();
                long s106 = tenureTypeAndUnits.getS106Units() == null ? 0 : tenureTypeAndUnits.getS106Units();
                long units = tenureTypeAndUnits.getTotalUnits() == null ? 0 : tenureTypeAndUnits.getTotalUnits();


                if (totalCost == 0 && units != 0) {
                    this.addErrorMessage(String.valueOf(tenureTypeAndUnits.getId()), "totalUnits", "Total cost cannot be zero if units are entered");
                }

                if (units == 0 && totalCost != 0) {
                    this.addErrorMessage(String.valueOf(tenureTypeAndUnits.getId()), "totalCost", "Total units cannot be zero if cost is provided");
                }

                if (units - s106 < 0) {
                    this.addErrorMessage(String.valueOf(tenureTypeAndUnits.getId()), "s106Units", "The number of nil grant units must not exceed the total number of units");
                }
            }
        }

        if (!atLeastOneValidRow) {
            this.addErrorMessage("Block1", "", "At least one valid row must exist");
        }

    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock otherBlock, ProjectDifferences differences) {
        super.compareBlockSpecificContent(otherBlock, differences);

        CalculateGrantBlock otherCalculateGrantBlock = (CalculateGrantBlock) otherBlock;

        List<TenureTypeAndUnits> thisTenure = this.getTenureTypeAndUnitsEntriesSorted();
        List<TenureTypeAndUnits> otherTenure = otherCalculateGrantBlock.getTenureTypeAndUnitsEntriesSorted();

        // compare each tenure type
        for (int i = 0; i < thisTenure.size(); i++) {
            TenureTypeAndUnits thisUnits = thisTenure.get(i);
            TenureTypeAndUnits otherUnits = otherTenure.get(i);

            if (!Objects.equals(thisUnits.getTotalUnits(), otherUnits.getTotalUnits())) {
                differences.add(new ProjectDifference(thisUnits, "totalUnits"));
            }
            if (!Objects.equals(thisUnits.getS106Units(), otherUnits.getS106Units())) {
                differences.add(new ProjectDifference(thisUnits, "s106Units"));
            }
            if (!Objects.equals(thisUnits.getTotalCost(), otherUnits.getTotalCost())) {
                differences.add(new ProjectDifference(thisUnits, "totalCost"));
            }
        }

        // compare totals row
        if (!Objects.equals(this.getTotals().getTotalCost(), otherCalculateGrantBlock.getTotals().getTotalCost())) {
            differences.add(new ProjectDifference(this,"totals.totalCost"));
        }

        if (!Objects.equals(this.getTotals().getTotalUnits(), otherCalculateGrantBlock.getTotals().getTotalUnits())) {
            differences.add(new ProjectDifference(this,"totals.totalUnits"));
        }

        if (!Objects.equals(this.getTotals().getTotalS106Units(), otherCalculateGrantBlock.getTotals().getTotalS106Units())) {
            differences.add(new ProjectDifference(this,"totals.totalS106Units"));
        }

        List<TenureSummaryDetails> thisSummary = this.getTenureSummaryDetails();
        List<TenureSummaryDetails> otherSummary = otherCalculateGrantBlock.getTenureSummaryDetails();

        // compare tenure summary tiles
        for (int i = 0; i < thisSummary.size(); i++) {
            if (!Objects.equals(thisSummary.get(i).getGrantEligibleUnits(), otherSummary.get(i).getGrantEligibleUnits())) {
                differences.add(new ProjectDifference(thisSummary.get(i),"grantEligibleUnits"));
            }

            if (!Objects.equals(thisSummary.get(i).getGrantRate(), otherSummary.get(i).getGrantRate())) {
                differences.add(new ProjectDifference(thisSummary.get(i),"grantRate"));
            }

            if (!Objects.equals(thisSummary.get(i).getTotalGrant(), otherSummary.get(i).getTotalGrant())) {
                differences.add(new ProjectDifference(thisSummary.get(i),"totalGrant"));
            }
        }

        // compare total eligibilty
        if (!Objects.equals(this.getTotalGrantEligibility(), otherCalculateGrantBlock.getTotalGrantEligibility())) {
            differences.add(new ProjectDifference(this,"totalGrantEligibility"));
        }
    }

    @Transient
    public void initialiseFromTenureTypes(Set<TemplateTenureType> tenureTypes) {
        HashSet<TenureTypeAndUnits> tenureTypeAndUnitsEntries = new HashSet<>();
        this.setTenureTypeAndUnitsEntries(tenureTypeAndUnitsEntries);
        if (tenureTypes != null) {
            for (TemplateTenureType tenureType : tenureTypes) {
                TenureTypeAndUnits tenureEntry = new TenureTypeAndUnits(project);
                tenureTypeAndUnitsEntries.add(tenureEntry);
                tenureEntry.setTenureType(tenureType);
                tenureEntry.setS106Units(0);
                tenureEntry.setTotalUnits(0);
                tenureEntry.setTotalCost(0L);
            }
        }
    }

    @Override
    public Integer calculateTotalUnits(TenureTypeAndUnits tenure) {
        return tenure.getTotalUnits();
    }

    @Override
    public Integer calculateNilGrantUnits(TenureTypeAndUnits tenure) {
        return tenure.getS106Units();
    }

    @Override
    public Long calculateDevCosts(TenureTypeAndUnits tenure) {
        return tenure.getTotalCost();
    }

    @Override
    public Integer calculateGrantPerUnitCost(TenureTypeAndUnits tenure) {
        return tenure.getTenureType().getTariffRate();
    }

    @Override
    public void calculateTotals(TenureTypeAndUnits tenureInfo) {
        if (tenureInfo.getTotalUnits() != null) {
            tenureInfo.setEligibleUnits(getTotalGrantEligibleUnits(tenureInfo));
        }
        if (tenureInfo.getTenureType().getTariffRate() != null) {
            tenureInfo.setGrantPerUnit(tenureInfo.getTenureType().getTariffRate());
        }
        if (tenureInfo.getEligibleUnits() != null && tenureInfo.getGrantPerUnit() != null) {
            tenureInfo.setEligibleGrant((long) (tenureInfo.getEligibleUnits() * tenureInfo.getGrantPerUnit()));
        }
    }

    public class Totals {

        private Integer totalUnits = 0;
        private Integer totalS106Units = 0;
        private Long totalCost = 0L;

        private Totals() {
            calculateTotals();
        }

        private void calculateTotals() {
            if (getTenureTypeAndUnitsEntries() != null) {
                for (TenureTypeAndUnits tenureTypeAndUnitsEntry : getTenureTypeAndUnitsEntries()) {
                    if (tenureTypeAndUnitsEntry.getTotalUnits() != null) {
                        totalUnits += tenureTypeAndUnitsEntry.getTotalUnits();
                    }
                    if (tenureTypeAndUnitsEntry.getS106Units() != null) {
                        totalS106Units += tenureTypeAndUnitsEntry.getS106Units();
                    }
                    if (tenureTypeAndUnitsEntry.getTotalCost() != null) {
                        totalCost += tenureTypeAndUnitsEntry.getTotalCost();
                    }
                }
            }
        }

        public Integer getTotalUnits() {
            return totalUnits;
        }

        public Integer getTotalS106Units() {
            return totalS106Units;
        }

        public Long getTotalCost() {
            return totalCost;
        }
    }


}
