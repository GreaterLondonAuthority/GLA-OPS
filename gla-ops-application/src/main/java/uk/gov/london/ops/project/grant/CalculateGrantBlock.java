/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.grant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.block.ProjectDifference;
import uk.gov.london.ops.project.block.ProjectDifferences;

/**
 * Created by chris on 13/10/2016.
 */
@Entity
@Table(name = "tenure_block")
@DiscriminatorValue("CALCULATE")
@JoinData(sourceTable = "tenure_block", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
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

            for (ProjectTenureDetails projectTenureDetailsEntry : getTenureTypeAndUnitsEntries()) {
                if (isRowValid(projectTenureDetailsEntry)) {
                    totalGrant += this.getTotalGrant(projectTenureDetailsEntry);
                }
            }
        }
        return totalGrant;
    }


    @Transient
    public Long getTotalGrant(ProjectTenureDetails typeAndUnits) {
        Long eligibleUnits = getTotalGrantEligibleUnits(typeAndUnits).longValue();
        return eligibleUnits * typeAndUnits.getTenureType().getTariffRate();
    }

    @Override
    public List<TenureSummaryDetails> getTenureSummaryDetails() {
        List<TenureSummaryDetails> details = new ArrayList<>();

        List<ProjectTenureDetails> list = getTenureTypeAndUnitsEntriesSorted();

        for (ProjectTenureDetails projectTenureDetails : list) {
            TenureSummaryDetails tsd = new TenureSummaryDetails();
            tsd.setName(projectTenureDetails.getTenureType().getName());
            if (isRowValid(projectTenureDetails)) {
                tsd.setGrantEligibleUnits(getTotalGrantEligibleUnits(projectTenureDetails));
                tsd.setGrantRate(projectTenureDetails.getTenureType().getTariffRate());
                tsd.setTotalGrant(tsd.getGrantEligibleUnits() * tsd.getGrantRate().longValue());
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
    protected boolean isRowValid(ProjectTenureDetails tenureItem) {
        if (tenureItem.getTotalCost() != null && tenureItem.getTotalUnits() != null && tenureItem.getTotalUnits() != 0
                && tenureItem.getTotalCost() != 0) {
            return tenureItem.getTotalUnits() - (tenureItem.getS106Units() == null ? 0 : tenureItem.getS106Units()) >= 0;
        }
        return false;
    }


    @JsonIgnore
    @Transient
    public Integer getTotalGrantEligibleUnits(ProjectTenureDetails tenureItem) {
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
        for (ProjectTenureDetails projectTenureDetails : this.getTenureTypeAndUnitsEntries()) {

            if (isRowValid(projectTenureDetails)) {
                atLeastOneValidRow = true;
            } else {
                long totalCost = projectTenureDetails.getTotalCost() == null ? 0 : projectTenureDetails
                        .getTotalCost();
                long s106 = projectTenureDetails.getS106Units() == null ? 0 : projectTenureDetails.getS106Units();
                long units = projectTenureDetails.getTotalUnits() == null ? 0 : projectTenureDetails
                        .getTotalUnits();

                if (totalCost == 0 && units != 0) {
                    this.addErrorMessage(String.valueOf(projectTenureDetails.getId()), "totalUnits",
                            "Total cost cannot be zero if units are entered");
                }

                if (units == 0 && totalCost != 0) {
                    this.addErrorMessage(String.valueOf(projectTenureDetails.getId()), "totalCost",
                            "Total units cannot be zero if cost is provided");
                }

                if (units - s106 < 0) {
                    this.addErrorMessage(String.valueOf(projectTenureDetails.getId()), "s106Units",
                            "The number of nil grant units must not exceed the total number of units");
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

        List<ProjectTenureDetails> thisTenure = this.getTenureTypeAndUnitsEntriesSorted();
        List<ProjectTenureDetails> otherTenure = otherCalculateGrantBlock.getTenureTypeAndUnitsEntriesSorted();

        // compare each tenure type
        for (int i = 0; i < thisTenure.size(); i++) {
            ProjectTenureDetails thisUnits = thisTenure.get(i);
            ProjectTenureDetails otherUnits = otherTenure.get(i);

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
            differences.add(new ProjectDifference(this, "totals.totalCost"));
        }

        if (!Objects.equals(this.getTotals().getTotalUnits(), otherCalculateGrantBlock.getTotals().getTotalUnits())) {
            differences.add(new ProjectDifference(this, "totals.totalUnits"));
        }

        if (!Objects.equals(this.getTotals().getTotalS106Units(), otherCalculateGrantBlock.getTotals().getTotalS106Units())) {
            differences.add(new ProjectDifference(this, "totals.totalS106Units"));
        }

        List<TenureSummaryDetails> thisSummary = this.getTenureSummaryDetails();
        List<TenureSummaryDetails> otherSummary = otherCalculateGrantBlock.getTenureSummaryDetails();

        // compare tenure summary tiles
        for (int i = 0; i < thisSummary.size(); i++) {
            if (!Objects.equals(thisSummary.get(i).getGrantEligibleUnits(), otherSummary.get(i).getGrantEligibleUnits())) {
                differences.add(new ProjectDifference(thisSummary.get(i), "grantEligibleUnits"));
            }

            if (!Objects.equals(thisSummary.get(i).getGrantRate(), otherSummary.get(i).getGrantRate())) {
                differences.add(new ProjectDifference(thisSummary.get(i), "grantRate"));
            }

            if (!Objects.equals(thisSummary.get(i).getTotalGrant(), otherSummary.get(i).getTotalGrant())) {
                differences.add(new ProjectDifference(thisSummary.get(i), "totalGrant"));
            }
        }

        // compare total eligibilty
        if (!Objects.equals(this.getTotalGrantEligibility(), otherCalculateGrantBlock.getTotalGrantEligibility())) {
            differences.add(new ProjectDifference(this, "totalGrantEligibility"));
        }
    }


    @Override
    public Integer calculateTotalUnits(ProjectTenureDetails tenure) {
        return tenure.getTotalUnits();
    }

    @Override
    public Integer calculateNilGrantUnits(ProjectTenureDetails tenure) {
        return tenure.getS106Units();
    }

    @Override
    public Long calculateDevCosts(ProjectTenureDetails tenure) {
        return tenure.getTotalCost();
    }

    @Override
    public Integer calculateGrantPerUnitCost(ProjectTenureDetails tenure) {
        return tenure.getTenureType().getTariffRate();
    }

    @Override
    public void calculateTotals(ProjectTenureDetails tenureInfo) {
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
                for (ProjectTenureDetails projectTenureDetailsEntry : getTenureTypeAndUnitsEntries()) {
                    if (projectTenureDetailsEntry.getTotalUnits() != null) {
                        totalUnits += projectTenureDetailsEntry.getTotalUnits();
                    }
                    if (projectTenureDetailsEntry.getS106Units() != null) {
                        totalS106Units += projectTenureDetailsEntry.getS106Units();
                    }
                    if (projectTenureDetailsEntry.getTotalCost() != null) {
                        totalCost += projectTenureDetailsEntry.getTotalCost();
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

    @Override
    public boolean isBlockRevertable() {
        return true;
    }

}
