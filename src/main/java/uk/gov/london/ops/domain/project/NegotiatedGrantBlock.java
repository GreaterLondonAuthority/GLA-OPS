/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.StringUtils;
import uk.gov.london.ops.domain.template.TemplateTenureType;
import uk.gov.london.ops.spe.SimpleProjectExportConfig;
import uk.gov.london.ops.spe.SimpleProjectExportConstants;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.*;

/**
 * Created by chris on 13/10/2016.
 */
@Entity(name = "tenure_block")
@DiscriminatorValue("NEGOTIATED")
@JoinData(sourceTable = "tenure_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the negotiated grant block is a subclass of the project block and shares a common key")
public class NegotiatedGrantBlock extends BaseGrantBlock {

    @Column(name = "justification")
    private String justification;

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
        for (TenureTypeAndUnits tenureTypeAndUnit : this.getTenureTypeAndUnitsEntries()) {
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

        List<TenureTypeAndUnits> list = getTenureTypeAndUnitsEntriesSorted();

        for (TenureTypeAndUnits tenureTypeAndUnits : list) {
            TenureSummaryDetails tsd = new TenureSummaryDetails();
            tsd.setName(tenureTypeAndUnits.getTenureType().getName());
            tsd.setGrantPerUnit(calculateGrantPerUnit(tenureTypeAndUnits));
            tsd.setUnitDevelopmentCost(calculateUnitDevelopmentCost(tenureTypeAndUnits));
            details.add(tsd);
        }
        return details;
    }

    private Integer calculateGrantPerUnit(TenureTypeAndUnits tenureTypeAndUnits) {
        if (isRowValid(tenureTypeAndUnits)) {
            return new Long(Math.round(tenureTypeAndUnits.getGrantRequested() / (double) tenureTypeAndUnits.getTotalUnits())).intValue();
        } else {
            return 0;
        }
    }

    private Integer calculateUnitDevelopmentCost(TenureTypeAndUnits tenureTypeAndUnits) {
        if (isRowValid(tenureTypeAndUnits)) {
            return new Long(Math.round(tenureTypeAndUnits.getTotalCost() / (double) tenureTypeAndUnits.getTotalUnits())).intValue();
        } else {
            return 0;
        }
    }

    @JsonIgnore
    @Transient
    protected boolean isRowValid(TenureTypeAndUnits tenureTypeAndUnit) {
        boolean valid = false;
        int totalAffordableUnits = tenureTypeAndUnit.getTotalUnits() == null ? 0 : tenureTypeAndUnit.getTotalUnits();
        int supportedUnits = tenureTypeAndUnit.getSupportedUnits() == null ? 0 : tenureTypeAndUnit.getSupportedUnits();
        long totalDevCosts = tenureTypeAndUnit.getTotalCost() == null ? 0L : tenureTypeAndUnit.getTotalCost();


        if (tenureTypeAndUnit.getGrantRequested() != null && totalAffordableUnits != 0 && tenureTypeAndUnit.getSupportedUnits() != null && totalDevCosts != 0L && totalAffordableUnits >= supportedUnits) {
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
    public void calculateTotals(TenureTypeAndUnits tenureInfo) {
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

    private void validateRows(Set<TenureTypeAndUnits> tenureTypeAndUnits) {
        boolean oneRowValid = false;

        for (TenureTypeAndUnits tenureTypeAndUnit : tenureTypeAndUnits) {

            long grantRequested = tenureTypeAndUnit.getGrantRequested() == null ? 0L : tenureTypeAndUnit.getGrantRequested();
            int totalAffordableUnits = tenureTypeAndUnit.getTotalUnits() == null ? 0 : tenureTypeAndUnit.getTotalUnits();
            int supportedUnits = tenureTypeAndUnit.getSupportedUnits() == null ? 0 : tenureTypeAndUnit.getSupportedUnits();
            long totalDevCosts = tenureTypeAndUnit.getTotalCost() == null ? 0L : tenureTypeAndUnit.getTotalCost();
            if (grantRequested == 0L && totalAffordableUnits == 0 && supportedUnits == 0 && totalDevCosts == 0L) {
                // no validation required, but row is not valid
            } else if (totalAffordableUnits == 0) {
                this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()),
                        "totalUnits"  , "Total units must be provided if applying for grant");
            } else if (tenureTypeAndUnit.getSupportedUnits() == null) {
                this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()),
                         "supportedUnits" , "Supported Units must be provided if applying for grant");
            } else if (totalDevCosts == 0L) {
                this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()), "totalCost", "Development costs must be provided for this project");
            } else if (supportedUnits > totalAffordableUnits) {
                this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()), "grantRequested", "The project cannot have more supported units than the total number of affordable units");
            } else if (tenureTypeAndUnit.getGrantRequested() == null) {
                this.addErrorMessage(String.valueOf(tenureTypeAndUnit.getId()),
                        "GrantRequested"  , "Grant Requested amount must be provided if units are specified");
            } else {
                oneRowValid = true;
            }
        }

        if (!oneRowValid) {
            this.addErrorMessage("Block2", "", "At least one valid row must exist");
        }
    }

    @Override
    public Integer calculateTotalUnits(TenureTypeAndUnits tenure) {
        return tenure.getTotalUnits();
    }

    @Override
    public Integer calculateGrantPerUnitCost(TenureTypeAndUnits tenure) {
        return  this.calculateUnitDevelopmentCost(tenure);
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
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
                tenureEntry.setAdditionalAffordableUnits(0);
                tenureEntry.setS106Units(0);
                tenureEntry.setTotalCost(0L);
            }
        }
    }

    @Override
    public Map<String, Object> simpleDataExtract(
            final SimpleProjectExportConfig simpleProjectExportConfig) {
        final Map<String, Object> map = super
                .simpleDataExtract(simpleProjectExportConfig);
        final SimpleProjectExportConstants.ReportPrefix prefix =
                SimpleProjectExportConstants.ReportPrefix.eg_;
        map.put(prefix + "justification", justification);
        return map;
    }

    @Transient
    public Totals getTotals() {
        return new Totals();
    }

    @Override
    protected void copyBlockContentInto(final NamedProjectBlock target) {
        super.copyBlockContentInto(target);
        ((NegotiatedGrantBlock)target)
                .setJustification(this.getJustification());
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {
        NegotiatedGrantBlock otherBlock = (NegotiatedGrantBlock) other;

        if (!Objects.equals(StringUtils.trimAllWhitespace(this.justification), StringUtils.trimAllWhitespace(otherBlock.justification))) {
            differences.add(new ProjectDifference(this, "justification"));
        }

        List<TenureTypeAndUnits> thisTenure = this.getTenureTypeAndUnitsEntriesSorted();
        List<TenureTypeAndUnits> otherTenure = otherBlock.getTenureTypeAndUnitsEntriesSorted();

        // compare each tenure type
        for (int i = 0; i < thisTenure.size(); i++) {
            TenureTypeAndUnits thisUnits = thisTenure.get(i);
            TenureTypeAndUnits otherUnits = otherTenure.get(i);

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

        List<TenureSummaryDetails> thisSummary = this.getTenureSummaryDetails();
        List<TenureSummaryDetails> otherSummary = otherBlock.getTenureSummaryDetails();

        // compare tenure summary tiles
        for (int i = 0; i < thisSummary.size(); i++) {
            if (!Objects.equals(thisSummary.get(i).getUnitDevelopmentCost(), otherSummary.get(i).getUnitDevelopmentCost())) {
                differences.add(new ProjectDifference(thisSummary.get(i),"unitDevelopmentCost"));
            }

            if (!Objects.equals(thisSummary.get(i).getGrantPerUnit(), otherSummary.get(i).getGrantPerUnit())) {
                differences.add(new ProjectDifference(thisSummary.get(i),"grantPerUnit"));
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

        private Totals() {
            calculateTotals();
        }

        private void calculateTotals() {
            if (getTenureTypeAndUnitsEntries() != null) {
                for (TenureTypeAndUnits tenureTypeAndUnitsEntry : getTenureTypeAndUnitsEntries()) {
                    if (tenureTypeAndUnitsEntry.getGrantRequested() != null) {
                        totalGrantRequested += tenureTypeAndUnitsEntry.getGrantRequested();
                    }
                    if (tenureTypeAndUnitsEntry.getTotalUnits() != null) {
                        totalUnits += tenureTypeAndUnitsEntry.getTotalUnits();
                    }
                    if (tenureTypeAndUnitsEntry.getSupportedUnits() != null) {
                        totalSupportedUnits += tenureTypeAndUnitsEntry.getSupportedUnits();
                    }
                    if (tenureTypeAndUnitsEntry.getTotalCost() != null) {
                        totalCost += tenureTypeAndUnitsEntry.getTotalCost();
                    }
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

    }

}
