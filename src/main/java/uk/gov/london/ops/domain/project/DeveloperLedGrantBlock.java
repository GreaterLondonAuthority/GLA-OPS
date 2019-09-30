/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConfig;
import uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.*;

/**
 * Created by chris on 13/10/2016.
 */
@Entity(name = "tenure_block")
@DiscriminatorValue("DEVELOPER-LED")
@JoinData(sourceTable = "tenure_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the dev led grant block is a subclass of the project block and shares a common key")
public class DeveloperLedGrantBlock extends BaseGrantBlock {


    @Column(name = "affordable_criteria_met")
    private Boolean affordableCriteriaMet;

    public DeveloperLedGrantBlock() {
        setBlockType(ProjectBlockType.DeveloperLedGrant);
    }

    public DeveloperLedGrantBlock(Project project) {
        this();
        this.project = project;
    }

    @Transient
    public Long getTotalGrantEligibility() {
        long total = 0L;
        if (affordableCriteriaMet != null) {
            for (ProjectTenureDetails projectTenureDetails : this.getTenureTypeAndUnitsEntries()) {
                if (isRowValid(projectTenureDetails)) {
                    Integer units  = projectTenureDetails.getAdditionalAffordableUnits();
                    if (affordableCriteriaMet) {
                        units = GlaUtils.nullSafeAdd(projectTenureDetails.getS106Units() , units);
                    }
                    if (units != null && units > 0){
                        total += units * projectTenureDetails.getTenureType().getTariffRate();
                    }
                }
            }
        }
        return total;
    }


    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.DeveloperLedGrant;
    }

    @Override
    public List<TenureSummaryDetails> getTenureSummaryDetails() {
        List<TenureSummaryDetails> details = new ArrayList<>();

        List<ProjectTenureDetails> list = getTenureTypeAndUnitsEntriesSorted();

        for (ProjectTenureDetails projectTenureDetails : list) {
            TenureSummaryDetails tsd = new TenureSummaryDetails();
            tsd.setName(projectTenureDetails.getTenureType().getName());
            if (affordableCriteriaMet != null && isRowValid(projectTenureDetails)) {
                if (affordableCriteriaMet) {
                    tsd.setGrantEligibleUnits(GlaUtils.nullSafeAdd(projectTenureDetails.getS106Units() , projectTenureDetails
                        .getAdditionalAffordableUnits()));
                } else {
                    tsd.setGrantEligibleUnits(
                        projectTenureDetails.getAdditionalAffordableUnits() == null ? 0 : projectTenureDetails
                            .getAdditionalAffordableUnits() );
                }
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
    protected boolean isRowValid(ProjectTenureDetails tenureTypeAndUnit) {
        boolean validUnits = GlaUtils.nullSafeAdd(tenureTypeAndUnit.getS106Units(),tenureTypeAndUnit.getAdditionalAffordableUnits() ) > 0;
        return  validUnits && (tenureTypeAndUnit.getTotalCost() != null && tenureTypeAndUnit.getTotalCost() > 0);
    }

    @Transient
    @Override
    public void merge(BaseGrantBlock newValue) {
        DeveloperLedGrantBlock block = (DeveloperLedGrantBlock) newValue;
        super.merge(block);
        this.setAffordableCriteriaMet(block.getAffordableCriteriaMet());
    }

    @Override
    public void calculateTotals(ProjectTenureDetails tenureInfo) {
        if (checkAffordableCriteriaMet()) {
            if (tenureInfo.getAdditionalAffordableUnits() != null && tenureInfo.getS106Units() != null) {
                tenureInfo.setEligibleUnits(tenureInfo.getAdditionalAffordableUnits() + tenureInfo.getS106Units());
            }
        } else {
            if (tenureInfo.getAdditionalAffordableUnits() != null) {
                tenureInfo.setEligibleUnits(tenureInfo.getAdditionalAffordableUnits());
            }
        }
        if (tenureInfo.getTenureType().getTariffRate() != null) {
            tenureInfo.setGrantPerUnit(tenureInfo.getTenureType().getTariffRate());
        }
        if (tenureInfo.getEligibleUnits() != null && tenureInfo.getGrantPerUnit() != null) {
            tenureInfo.setEligibleGrant((long) (tenureInfo.getEligibleUnits() * tenureInfo.getGrantPerUnit()));
        }
    }

    private boolean checkAffordableCriteriaMet() {
        return (affordableCriteriaMet != null) && affordableCriteriaMet.booleanValue();
    }

    @Override
    protected void generateValidationFailures() {
        super.generateValidationFailures();

        if (affordableCriteriaMet == null) {
            this.addErrorMessage("Block1", "affordableCriteriaMet", "Specify whether 40% or more habitable rooms are affordable");
        }

        validateRows(this.getTenureTypeAndUnitsEntries());

    }

    private void validateRows(Set<ProjectTenureDetails> projectTenuresDetails) {
        boolean oneRowValid = false;


        for (ProjectTenureDetails tenureTypeAndUnit : projectTenuresDetails) {

            if (isRowValid(tenureTypeAndUnit)) {
                oneRowValid = true;
            } else {
                int s106Units = tenureTypeAndUnit.getS106Units() == null ? 0 : tenureTypeAndUnit.getS106Units();
                int affordables = tenureTypeAndUnit.getAdditionalAffordableUnits() == null ? 0 : tenureTypeAndUnit.getAdditionalAffordableUnits();
                long devCost = tenureTypeAndUnit.getTotalCost() == null ? 0L : tenureTypeAndUnit.getTotalCost();

                if (s106Units == 0 && affordables == 0 && devCost == 0) {
                    // not valid but not an error
                } else if (devCost == 0) {
                    this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()), "totalCost", "Development costs must be provided for this project");
                } else if (s106Units == 0 && affordables == 0) {
                    this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()), "s106Units", "At least one unit must be specified");
                }
            }
        }

        if (!oneRowValid) {
            this.addErrorMessage("Block2", "", "At least one valid row must exist");
        }


    }



    @Override
    public Integer calculateTotalUnits(ProjectTenureDetails tenure) {
        if(tenure.getS106Units() == null) {
            return tenure.getAdditionalAffordableUnits();
        } else if(tenure.getAdditionalAffordableUnits() == null) {
            return tenure.getS106Units();
        } else {
            return tenure.getS106Units() + tenure.getAdditionalAffordableUnits();
        }
    }

    @Override
    public Integer calculateS106Units(ProjectTenureDetails tenure) {
        return  tenure.getS106Units();
    }

    @Override
    public Long calculateDevCosts(ProjectTenureDetails tenure) {
        return tenure.getTotalCost();
    }

    @Override
    public Integer calculateGrantPerUnitCost(ProjectTenureDetails tenure) {
        return  tenure.getTenureType().getTariffRate();
    }

    @Transient
    public Totals getTotals() {
        return new Totals();
    }

    public class Totals {

        private Integer totalS106Units = 0;
        private Integer totalAdditionalUnits = 0;
        private Long totalCost = 0L;

        private Totals() {
            calculateTotals();
        }

        private void calculateTotals() {
            if (getTenureTypeAndUnitsEntries() != null) {
                for (ProjectTenureDetails projectTenureDetailsEntry : getTenureTypeAndUnitsEntries()) {
                    if (projectTenureDetailsEntry.getS106Units() != null) {
                        totalS106Units += projectTenureDetailsEntry.getS106Units();
                    }
                    if (projectTenureDetailsEntry.getAdditionalAffordableUnits() != null) {
                        totalAdditionalUnits += projectTenureDetailsEntry.getAdditionalAffordableUnits();
                    }
                    if (projectTenureDetailsEntry.getTotalCost() != null) {
                        totalCost += projectTenureDetailsEntry.getTotalCost();
                    }
                }
            }
        }

        public Integer getTotalS106Units() {
            return totalS106Units;
        }

        public Integer getTotalAdditionalUnits() {
            return totalAdditionalUnits;
        }

        public Long getTotalCost() {
            return totalCost;
        }
    }

    public Boolean getAffordableCriteriaMet() {
        return affordableCriteriaMet;
    }

    public void setAffordableCriteriaMet(Boolean affordableCriteriaMet) {
        this.affordableCriteriaMet = affordableCriteriaMet;
    }


    @Override
    public Map<String, Object> simpleDataExtract(
            final SimpleProjectExportConfig simpleProjectExportConfig) {
        final Map<String, Object> map = super
                .simpleDataExtract(simpleProjectExportConfig);
        final SimpleProjectExportConstants.ReportPrefix prefix =
                SimpleProjectExportConstants.ReportPrefix.eg_;
        map.put(prefix + "aff_criteria_met",
                affordableCriteriaMet != null && affordableCriteriaMet
                ? "YES" : "NO");
        return map;
    }


    @Override
    protected void copyBlockContentInto(final NamedProjectBlock target) {
        super.copyBlockContentInto(target);
        ((DeveloperLedGrantBlock)target)
                .setAffordableCriteriaMet(this.getAffordableCriteriaMet());
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock otherBlock, ProjectDifferences differences) {
        super.compareBlockSpecificContent(otherBlock, differences);

        DeveloperLedGrantBlock otherDevLedGrantBlock = (DeveloperLedGrantBlock) otherBlock;

        List<ProjectTenureDetails> thisTenure = this.getTenureTypeAndUnitsEntriesSorted();
        List<ProjectTenureDetails> otherTenure = otherDevLedGrantBlock.getTenureTypeAndUnitsEntriesSorted();

        if (!Objects.equals(this.getAffordableCriteriaMet(), otherDevLedGrantBlock.getAffordableCriteriaMet())) {
            differences.add(new ProjectDifference(this, "affordableCriteriaMet"));
        }

        // compare each tenure type
        for (int i = 0; i < thisTenure.size(); i++) {
            ProjectTenureDetails thisUnits = thisTenure.get(i);
            ProjectTenureDetails otherUnits = otherTenure.get(i);

            if (!Objects.equals(thisUnits.getAdditionalAffordableUnits(), otherUnits.getAdditionalAffordableUnits())) {
                differences.add(new ProjectDifference(thisUnits, "additionalAffordableUnits"));
            }
            if (!Objects.equals(thisUnits.getS106Units(), otherUnits.getS106Units())) {
                differences.add(new ProjectDifference(thisUnits, "s106Units"));
            }
            if (!Objects.equals(thisUnits.getTotalCost(), otherUnits.getTotalCost())) {
                differences.add(new ProjectDifference(thisUnits, "totalCost"));
            }
        }

        // compare totals row
        if (!Objects.equals(this.getTotals().getTotalCost(), otherDevLedGrantBlock.getTotals().getTotalCost())) {
            differences.add(new ProjectDifference(this,"totals.totalCost"));
        }

        if (!Objects.equals(this.getTotals().getTotalAdditionalUnits(), otherDevLedGrantBlock.getTotals().getTotalAdditionalUnits())) {
            differences.add(new ProjectDifference(this,"totals.totalAdditionalUnits"));
        }

        if (!Objects.equals(this.getTotals().getTotalS106Units(), otherDevLedGrantBlock.getTotals().getTotalS106Units())) {
            differences.add(new ProjectDifference(this,"totals.totalS106Units"));
        }

        List<TenureSummaryDetails> thisSummary = this.getTenureSummaryDetails();
        List<TenureSummaryDetails> otherSummary = otherDevLedGrantBlock.getTenureSummaryDetails();

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
        if (!Objects.equals(this.getTotalGrantEligibility(), otherDevLedGrantBlock.getTotalGrantEligibility())) {
            differences.add(new ProjectDifference(this,"totalGrantEligibility"));
        }
    }

    @Override
    public boolean isBlockRevertable() {
        return true;
    }

}
