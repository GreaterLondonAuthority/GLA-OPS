/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.grant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.StringUtils;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.block.ProjectDifference;
import uk.gov.london.ops.project.block.ProjectDifferences;
import uk.gov.london.ops.project.template.domain.NegotiatedGrantTemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateBlock;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by chris on 13/10/2016.
 */
@Entity
@Table(name = "tenure_block")
@DiscriminatorValue("NEGOTIATED")
@JoinData(sourceTable = "tenure_block", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the negotiated grant block is a subclass of the project block and shares a common key")
public class NegotiatedGrantBlock extends BaseGrantBlock {

    @Column(name = "justification")
    private String justification;

    @Column(name = "show_specialised_units")
    private boolean showSpecialisedUnits;

    @Column(name = "show_development_cost")
    private boolean showDevelopmentCost;

    @Column(name = "show_percentage_costs")
    private boolean showPercentageCosts;

    public NegotiatedGrantBlock() {
        setBlockType(ProjectBlockType.NegotiatedGrant);
    }

    public NegotiatedGrantBlock(Project project) {
        this();
        this.project = project;
    }

    @Transient
    public Long getTotalGrantEligibility() {
        long total = 0L;
        for (ProjectTenureDetails tenureTypeAndUnit : this.getTenureTypeAndUnitsEntries()) {
            total += tenureTypeAndUnit.getGrantRequested() == null ? 0L : tenureTypeAndUnit.getGrantRequested();
        }

        return total;
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.NegotiatedGrant;
    }

    @Override
    public List<TenureSummaryDetails> getTenureSummaryDetails() {
        List<TenureSummaryDetails> details = new ArrayList<>();

        List<ProjectTenureDetails> list = getTenureTypeAndUnitsEntriesSorted();

        for (ProjectTenureDetails projectTenureDetails : list) {
            TenureSummaryDetails tsd = new TenureSummaryDetails();
            tsd.setName(projectTenureDetails.getTenureType().getName());
            tsd.setGrantPerUnit(calculateGrantPerUnit(projectTenureDetails));
            tsd.setUnitDevelopmentCost(calculateUnitDevelopmentCost(projectTenureDetails));
            details.add(tsd);
        }
        return details;
    }

    private Integer calculateGrantPerUnit(ProjectTenureDetails projectTenureDetails) {
        if (isRowValid(projectTenureDetails)) {
            return new Long(Math.round(
                    projectTenureDetails.getGrantRequested() / (double) projectTenureDetails.getTotalUnits())).intValue();
        } else {
            return 0;
        }
    }

    private Integer calculateUnitDevelopmentCost(ProjectTenureDetails projectTenureDetails) {
        if (showDevelopmentCost && isRowValid(projectTenureDetails)) {
            return new Long(Math.round(
                    projectTenureDetails.getTotalCost() / (double) projectTenureDetails.getTotalUnits())).intValue();
        } else {
            return 0;
        }
    }

    @JsonIgnore
    @Transient
    protected boolean isRowValid(ProjectTenureDetails tenureTypeAndUnit) {
        boolean valid = false;
        int totalAffordableUnits = tenureTypeAndUnit.getTotalUnits() == null ? 0 : tenureTypeAndUnit.getTotalUnits();
        int supportedUnits = tenureTypeAndUnit.getSupportedUnits() == null ? 0 : tenureTypeAndUnit.getSupportedUnits();
        long totalDevCosts = tenureTypeAndUnit.getTotalCost() == null ? 0L : tenureTypeAndUnit.getTotalCost();

        boolean basicValidation = tenureTypeAndUnit.getGrantRequested() != null && totalAffordableUnits != 0;
        boolean devCostsValid = !this.showDevelopmentCost || totalDevCosts != 0L;
        boolean supportedUnitsValid = !this.showSpecialisedUnits || (tenureTypeAndUnit.getSupportedUnits() != null && totalAffordableUnits >= supportedUnits);

        if (basicValidation && supportedUnitsValid && devCostsValid ) {
            valid = true;
        }

        return valid;
    }

    @Transient
    @Override
    public void merge(BaseGrantBlock newValue) {
        NegotiatedGrantBlock block = (NegotiatedGrantBlock) newValue;
        super.merge(block);
        this.setJustification(block.getJustification());
    }

    @Override
    public void calculateTotals(ProjectTenureDetails tenureInfo) {
        if (tenureInfo.getTotalUnits() != null) {
            tenureInfo.setEligibleUnits(tenureInfo.getTotalUnits());
        }
        tenureInfo.setGrantPerUnit(0);
        if (tenureInfo.getGrantRequested() != null) {
            tenureInfo.setEligibleGrant(tenureInfo.getGrantRequested());
        }
    }

    @Override
    public void generateValidationFailures() {
        super.generateValidationFailures();

        if (StringUtils.isEmpty(justification)) {
            this.addErrorMessage("Block1", "justification", "Justification is required for your grant request on this project");
        } else if (justification.length() > 1000) {
            this.addErrorMessage("Block1", "justification", "Maximum length for justification field is 10,000 characters");
        }

        validateRows(this.getTenureTypeAndUnitsEntries());
    }

    private void validateRows(Set<ProjectTenureDetails> projectTenuresDetails) {
        boolean oneRowValid = false;

        for (ProjectTenureDetails tenureTypeAndUnit : projectTenuresDetails) {
            long grantRequested = tenureTypeAndUnit.getGrantRequested() == null ? 0L : tenureTypeAndUnit.getGrantRequested();
            int totalAffordableUnits = tenureTypeAndUnit.getTotalUnits() == null ? 0 : tenureTypeAndUnit.getTotalUnits();
            int supportedUnits = tenureTypeAndUnit.getSupportedUnits() == null ? 0 : tenureTypeAndUnit.getSupportedUnits();
            long totalDevCosts = tenureTypeAndUnit.getTotalCost() == null ? 0L : tenureTypeAndUnit.getTotalCost();

            if (grantRequested == 0L && totalAffordableUnits == 0 && supportedUnits == 0 && totalDevCosts == 0L) {
                // no validation required, but row is not valid
            } else if (totalAffordableUnits == 0) {
                this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()),
                        "totalUnits", "Total units must be provided if applying for grant");
            } else if (this.showSpecialisedUnits && tenureTypeAndUnit.getSupportedUnits() == null) {
                this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()),
                        "supportedUnits", "Supported Units must be provided if applying for grant");
            } else if (this.showDevelopmentCost && totalDevCosts == 0L) {
                this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()), "totalCost",
                        "Development costs must be provided for this project");
            } else if (this.showSpecialisedUnits && supportedUnits > totalAffordableUnits) {
                this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()), "grantRequested",
                        "The project cannot have more supported units than the total number of affordable units");
            } else if (tenureTypeAndUnit.getGrantRequested() == null) {
                this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()),
                        "GrantRequested", "Grant Requested amount must be provided if units are specified");
            } else {
                oneRowValid = true;
            }
        }

        if (!oneRowValid) {
            this.addErrorMessage("Block2", "", "At least one valid row must exist");
        }
    }

    @Override
    public Integer calculateTotalUnits(ProjectTenureDetails tenure) {
        return tenure.getTotalUnits();
    }

    @Override
    public Integer calculateGrantPerUnitCost(ProjectTenureDetails tenure) {
        return this.calculateUnitDevelopmentCost(tenure);
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public boolean isShowSpecialisedUnits() {
        return showSpecialisedUnits;
    }

    public void setShowSpecialisedUnits(boolean showSpecialisedUnits) {
        this.showSpecialisedUnits = showSpecialisedUnits;
    }

    public boolean isShowDevelopmentCost() {
        return showDevelopmentCost;
    }

    public void setShowDevelopmentCost(boolean showDevelopmentCost) {
        this.showDevelopmentCost = showDevelopmentCost;
    }

    public boolean isShowPercentageCosts() {
        return showPercentageCosts;
    }

    public void setShowPercentageCosts(boolean showPercentageCosts) {
        this.showPercentageCosts = showPercentageCosts;
    }

    @Transient
    public Totals getTotals() {
        return new Totals();
    }

    @Override
    protected void copyBlockContentInto(final NamedProjectBlock target) {
        super.copyBlockContentInto(target);
        NegotiatedGrantBlock ngbTarget = (NegotiatedGrantBlock) target;
        ngbTarget.setJustification(this.getJustification());
        ngbTarget.setShowSpecialisedUnits(this.isShowSpecialisedUnits());
        ngbTarget.setShowDevelopmentCost(this.isShowDevelopmentCost());
        ngbTarget.setShowPercentageCosts(this.isShowPercentageCosts());
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {
        NegotiatedGrantBlock otherBlock = (NegotiatedGrantBlock) other;

        if (!Objects.equals(StringUtils.trimAllWhitespace(this.justification),
                StringUtils.trimAllWhitespace(otherBlock.justification))) {
            differences.add(new ProjectDifference(this, "justification"));
        }

        List<ProjectTenureDetails> thisTenure = this.getTenureTypeAndUnitsEntriesSorted();
        List<ProjectTenureDetails> otherTenure = otherBlock.getTenureTypeAndUnitsEntriesSorted();

        // compare each tenure type
        for (int i = 0; i < thisTenure.size(); i++) {
            ProjectTenureDetails thisUnits = thisTenure.get(i);
            ProjectTenureDetails otherUnits = otherTenure.get(i);

            if (!Objects.equals(thisUnits.getGrantRequested(), otherUnits.getGrantRequested())) {
                differences.add(new ProjectDifference(thisUnits, "grantRequested"));
            }
            if (!Objects.equals(thisUnits.getTotalUnits(), otherUnits.getTotalUnits())) {
                differences.add(new ProjectDifference(thisUnits, "totalUnits"));
            }
            if (!Objects.equals(thisUnits.getSupportedUnits(), otherUnits.getSupportedUnits())) {
                differences.add(new ProjectDifference(thisUnits, "supportedUnits"));
            }
            if (!Objects.equals(thisUnits.getTotalCost(), otherUnits.getTotalCost())) {
                differences.add(new ProjectDifference(thisUnits, "totalCost"));
            }
            if (GlaUtils.compareBigDecimals(thisUnits.getPercentageOfTotalCost(), otherUnits.getPercentageOfTotalCost()) != 0) {
                differences.add(new ProjectDifference(thisUnits, "percentageOfTotalCost"));
            }
        }

        // compare totals row
        if (!Objects.equals(this.getTotals().getTotalCost(), otherBlock.getTotals().getTotalCost())) {
            differences.add(new ProjectDifference(this, "totals.totalCost"));
        }

        if (!Objects.equals(this.getTotals().getTotalGrantRequested(), otherBlock.getTotals().getTotalGrantRequested())) {
            differences.add(new ProjectDifference(this, "totals.totalGrantRequested"));
        }

        if (!Objects.equals(this.getTotals().getTotalUnits(), otherBlock.getTotals().getTotalUnits())) {
            differences.add(new ProjectDifference(this, "totals.totalUnits"));
        }

        if (!Objects.equals(this.getTotals().getTotalSupportedUnits(), otherBlock.getTotals().getTotalSupportedUnits())) {
            differences.add(new ProjectDifference(this, "totals.totalSupportedUnits"));
        }

        if (GlaUtils.compareBigDecimals(this.getTotals().getPercentageOfTotalCost(),
                otherBlock.getTotals().getPercentageOfTotalCost()) != 0) {
            differences.add(new ProjectDifference(this, "totals.percentageOfTotalCost"));
        }

        List<TenureSummaryDetails> thisSummary = this.getTenureSummaryDetails();
        List<TenureSummaryDetails> otherSummary = otherBlock.getTenureSummaryDetails();

        // compare tenure summary tiles
        for (int i = 0; i < thisSummary.size(); i++) {
            if (!Objects.equals(thisSummary.get(i).getUnitDevelopmentCost(), otherSummary.get(i).getUnitDevelopmentCost())) {
                differences.add(new ProjectDifference(thisSummary.get(i), "unitDevelopmentCost"));
            }

            if (!Objects.equals(thisSummary.get(i).getGrantPerUnit(), otherSummary.get(i).getGrantPerUnit())) {
                differences.add(new ProjectDifference(thisSummary.get(i), "grantPerUnit"));
            }
        }

        // compare total eligibilty
        if (!Objects.equals(this.getTotalGrantEligibility(), otherBlock.getTotalGrantEligibility())) {
            differences.add(new ProjectDifference(this, "totalGrantEligibility"));
        }

    }

    public class Totals {

        private Long totalGrantRequested = 0L;
        private Integer totalUnits = 0;
        private Integer totalSupportedUnits = 0;
        private Long totalCost = 0L;
        private BigDecimal percentageOfTotalCost = null;


        private Totals() {
            calculateTotals();
        }

        private void calculateTotals() {
            if (getTenureTypeAndUnitsEntries() != null) {
                for (ProjectTenureDetails projectTenureDetailsEntry : getTenureTypeAndUnitsEntries()) {
                    if (projectTenureDetailsEntry.getGrantRequested() != null) {
                        totalGrantRequested += projectTenureDetailsEntry.getGrantRequested();
                    }
                    if (projectTenureDetailsEntry.getTotalUnits() != null) {
                        totalUnits += projectTenureDetailsEntry.getTotalUnits();
                    }
                    if (projectTenureDetailsEntry.getSupportedUnits() != null) {
                        totalSupportedUnits += projectTenureDetailsEntry.getSupportedUnits();
                    }
                    if (projectTenureDetailsEntry.getTotalCost() != null) {
                        totalCost += projectTenureDetailsEntry.getTotalCost();
                    }
                }
                if (totalGrantRequested != 0 && totalCost != 0) {
                    percentageOfTotalCost = new BigDecimal((totalGrantRequested / (double) (totalCost)) * 100)
                            .setScale(1, BigDecimal.ROUND_HALF_UP);
                }
            }
        }

        public Long getTotalGrantRequested() {
            return totalGrantRequested;
        }

        public Integer getTotalUnits() {
            return totalUnits;
        }

        public Integer getTotalSupportedUnits() {
            return totalSupportedUnits;
        }

        public Long getTotalCost() {
            return totalCost;
        }

        public BigDecimal getPercentageOfTotalCost() {
            return percentageOfTotalCost;
        }
    }

    @Override
    public boolean isBlockRevertable() {
        return true;
    }

    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        super.initFromTemplateSpecific(templateBlock);
        NegotiatedGrantTemplateBlock ngtb = (NegotiatedGrantTemplateBlock) templateBlock;
        this.setShowDevelopmentCost(ngtb.isShowDevelopmentCost());
        this.setShowPercentageCosts(ngtb.isShowPercentageCosts());
        this.setShowSpecialisedUnits(ngtb.isShowSpecialisedUnits());
    }
}
