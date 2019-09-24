/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.domain.attachment.ProjectBudgetsAttachment;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.web.model.AnnualSpendSummary;
import uk.gov.london.ops.web.model.ProjectBudgetsAllYearSummary;
import uk.gov.london.ops.web.model.ProjectBudgetsSummaryEntry;
import uk.gov.london.ops.web.model.ProjectBudgetsYearlySummary;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity(name = "project_budgets")
@DiscriminatorValue("PROJECT_BUDGETS")
@JoinData(sourceTable = "project_budgets", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the project spend block is a subclass of the project block and shares a common key")
public class ProjectBudgetsBlock extends BaseFinanceBlock {

    @Column(name = "from_date")
    private String fromDate;

    @Column(name = "to_date")
    private String toDate;

    @Column(name = "revenue")
    private Long revenue;

    @Column(name = "capital")
    private Long capital;

    @Transient
    private Totals totals;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, targetEntity = WbsCode.class)
    @JoinColumn(name="block_id")
    private Set<WbsCode> wbsCodes = new HashSet<>();

    @JoinData(joinType = Join.JoinType.OneToMany, sourceColumn = "id", targetColumn = "project_budgets_id", targetTable = "attachment",
            comment = "")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = ProjectBudgetsAttachment.class)
    @JoinColumn(name = "project_budgets_id")
    private Set<ProjectBudgetsAttachment> attachments = new HashSet<>();

    @Transient
    private ProjectBudgetsYearlySummary projectBudgetsYearlySummary;

    @Transient
    private List<AnnualSpendSummary> annualSpendSummaries = new ArrayList<>();

    public ProjectBudgetsBlock() {}

    public ProjectBudgetsBlock(Project project) {
        this.project = project;
    }

    public void merge(ProjectBudgetsBlock projectBudgetsBlock) {
        this.fromDate = projectBudgetsBlock.fromDate;
        this.toDate = projectBudgetsBlock.toDate;
        this.revenue = projectBudgetsBlock.revenue;
        this.capital = projectBudgetsBlock.capital;

        for (ProjectBudgetsAttachment attachment: projectBudgetsBlock.attachments) {
            if (!this.attachments.contains(attachment)) {
                this.attachments.add(attachment);
            }
        }

        this.attachments.removeIf(a -> !projectBudgetsBlock.attachments.contains(a));

        this.wbsCodes.clear();
        this.wbsCodes.addAll(projectBudgetsBlock.wbsCodes);
    }

    @Override
    public boolean isComplete() {
        return isVisited() && (revenue != null || capital != null) ;
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.ProjectBudgets;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public Long getRevenue() {
        return revenue;
    }

    public void setRevenue(Long revenue) {
        this.revenue = revenue;
    }

    public Long getCapital() {
        return capital;
    }

    public void setCapital(Long capital) {
        this.capital = capital;
    }

    public Set<WbsCode> getWbsCodes() {
        return wbsCodes;
    }

    public void setWbsCodes(Set<WbsCode> wbsCodes) {
        this.wbsCodes = wbsCodes;
    }

    public Set<ProjectBudgetsAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<ProjectBudgetsAttachment> attachments) {
        this.attachments = attachments;
    }

    public ProjectBudgetsYearlySummary getProjectBudgetsYearlySummary() {
        return projectBudgetsYearlySummary;
    }

    public void setProjectBudgetsYearlySummary(ProjectBudgetsYearlySummary projectBudgetsYearlySummary) {
        this.projectBudgetsYearlySummary = projectBudgetsYearlySummary;
    }

    public Integer getFromFinancialYear() {
        return getFinancialYear(getFromDate());
    }

    public Integer getToFinancialYear() {
        return getFinancialYear(getToDate());
    }

    private Integer getFinancialYear(String year) {
        if (year != null && year.length() == 7) {
            return Integer.parseInt(year.substring(0,4));
        }
        return null;
    }

    public List<AnnualSpendSummary> getAnnualSpendSummaries() {
        return annualSpendSummaries;
    }

    public void setAnnualSpendSummaries(List<AnnualSpendSummary> annualSpendSummaries) {
        this.annualSpendSummaries = annualSpendSummaries;
    }

    public void setTotals(Totals totals) {
        this.totals = totals;
    }

    public Totals getTotals() {
        if (totals == null) {
            totals = new Totals();
        }
        return totals;
    }

    public Set<WbsCode> getWbsCodes(SpendType spendType) {
        return wbsCodes.stream().filter(wbsCode -> spendType.name().equals(wbsCode.getType())).collect(Collectors.toSet());
    }

    protected void copyBlockContentInto(NamedProjectBlock target) {
        final ProjectBudgetsBlock t = (ProjectBudgetsBlock)target;
        t.setFromDate(this.getFromDate());
        t.setToDate(this.getToDate());
        t.setRevenue(this.getRevenue());
        t.setCapital(this.getCapital());

        if(this.getWbsCodes() != null) {
            for(final WbsCode code : getWbsCodes()) {
                t.getWbsCodes().add(code.copy());
            }
        }
        if(this.getAttachments() != null) {
            for (final ProjectBudgetsAttachment attachment : this.getAttachments()) {
                t.getAttachments().add(attachment.copy());
            }
        }

    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock otherBlock, ProjectDifferences differences) {
        super.compareBlockSpecificContent(otherBlock, differences);

        ProjectBudgetsBlock other = (ProjectBudgetsBlock) otherBlock;

        if (!Objects.equals(this.getFromDate(), other.getFromDate())) {
            differences.add(new ProjectDifference(this,"fromDate"));
        }

        if (!Objects.equals(this.getToDate(), other.getToDate())) {
            differences.add(new ProjectDifference(this,"toDate"));
        }


        if (!Objects.equals(this.getRevenue(), other.getRevenue())) {
            differences.add(new ProjectDifference(this, "revenue"));
        }
        if (!Objects.equals(this.getCapital(), other.getCapital())) {
            differences.add(new ProjectDifference(this, "capital"));
        }

        compareAttachments(differences, other);
        compareTotals(differences, other);

        ProjectBudgetsYearlySummary thisYearlySummary = this.getProjectBudgetsYearlySummary();
        ProjectBudgetsYearlySummary otherYearlySummary = other.getProjectBudgetsYearlySummary();

        generateYearlySummaryDifferences(differences, thisYearlySummary, otherYearlySummary);
        generateAllYearSummaryDifferences(differences, thisYearlySummary, otherYearlySummary);

        generateSummaryDifferences(differences, other);
    }


    private void generateAllYearSummaryDifferences(ProjectDifferences differences, ProjectBudgetsYearlySummary thisYearlySummary, ProjectBudgetsYearlySummary otherYearlySummary) {

        if (thisYearlySummary != null && otherYearlySummary != null) {
            ProjectBudgetsAllYearSummary thisYearSummaryRow = thisYearlySummary.getProjectBudgetsAllYearSummary();
            ProjectBudgetsAllYearSummary otherYearSummaryRow = otherYearlySummary.getProjectBudgetsAllYearSummary();

            if (thisYearSummaryRow != null && otherYearSummaryRow != null) {
                if (GlaUtils.compareBigDecimals(thisYearSummaryRow.getForecastValueTotal(), otherYearSummaryRow.getForecastValueTotal()) != 0) {
                    differences.add(new ProjectDifference(thisYearSummaryRow, "forecastValueTotal"));
                }
                if (GlaUtils.compareBigDecimals(thisYearSummaryRow.getActualValueTotal(), otherYearSummaryRow.getActualValueTotal()) != 0) {
                    differences.add(new ProjectDifference(thisYearSummaryRow, "actualValueTotal"));
                }
                if (GlaUtils.compareBigDecimals(thisYearSummaryRow.getRemainingForecastAndActualsTotal(), otherYearSummaryRow.getRemainingForecastAndActualsTotal()) != 0) {
                    differences.add(new ProjectDifference(thisYearSummaryRow, "remainingForecastAndActualsTotal"));
                }
            }
        }
    }

    private void generateYearlySummaryDifferences(ProjectDifferences differences, ProjectBudgetsYearlySummary thisYearlySummary, ProjectBudgetsYearlySummary otherYearlySummary) {
        Map<String, ProjectBudgetsSummaryEntry> thisSummaryEntries = new HashMap<>();
        if (thisYearlySummary != null) {
            thisSummaryEntries = thisYearlySummary.getSummaryEntries().stream().
                    collect(Collectors.toMap(ProjectBudgetsSummaryEntry::getComparisonId, Function.identity()));
        }

        Map<String, ProjectBudgetsSummaryEntry> otherSummaryEntries = new HashMap<>();
        if (otherYearlySummary != null) {
            otherSummaryEntries = otherYearlySummary.getSummaryEntries().stream().
                    collect(Collectors.toMap(ProjectBudgetsSummaryEntry::getComparisonId, Function.identity()));
        }


        for (Iterator<String> iterator = thisSummaryEntries.keySet().iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            ProjectBudgetsSummaryEntry thisEntry = thisSummaryEntries.get(key);
            ProjectBudgetsSummaryEntry otherEntry = otherSummaryEntries.get(key);
            if (otherEntry != null) {
                if (GlaUtils.compareBigDecimals(thisEntry.getActualValue(), otherEntry.getActualValue()) != 0) {
                    differences.add(new ProjectDifference(thisEntry, "actualValue"));
                }
                if (GlaUtils.compareBigDecimals(thisEntry.getForecastValue(), otherEntry.getForecastValue()) != 0) {
                    differences.add(new ProjectDifference(thisEntry, "forecastValue"));
                }
                iterator.remove();
                otherSummaryEntries.remove(key);
            }
        }

        // added in this unapproved version
        for (ProjectBudgetsSummaryEntry record : thisSummaryEntries.values()) {
            differences.add(new ProjectDifference(record, ProjectDifference.DifferenceType.Addition));
        }
        // deleted by this unapproved version
        for (ProjectBudgetsSummaryEntry record : otherSummaryEntries.values()) {
            differences.add(new ProjectDifference(record, ProjectDifference.DifferenceType.Deletion));
        }


    }

    private void compareAttachments(ProjectDifferences differences, ProjectBudgetsBlock other) {
        Map<Integer, ProjectBudgetsAttachment> thisAttachments = this.getAttachments().stream().
                collect(Collectors.toMap(ProjectBudgetsAttachment::getFileId, Function.identity()));
        Map<Integer, ProjectBudgetsAttachment> otherAttachments = other.getAttachments().stream().
                collect(Collectors.toMap(ProjectBudgetsAttachment::getFileId, Function.identity()));


        for (Iterator<Integer> iterator = thisAttachments.keySet().iterator(); iterator.hasNext(); ) {
            Integer key = iterator.next();
            if (otherAttachments.get(key) != null) {
                iterator.remove();
                otherAttachments.remove(key);
            }
        }

        // added in this unapproved version
        for (ProjectBudgetsAttachment record : thisAttachments.values()) {
            differences.add(new ProjectDifference(record, ProjectDifference.DifferenceType.Addition));
        }
        // deleted by this unapproved version
        for (ProjectBudgetsAttachment record : otherAttachments.values()) {
            differences.add(new ProjectDifference(record, ProjectDifference.DifferenceType.Deletion));
        }
    }

    private void generateSummaryDifferences(ProjectDifferences differences, ProjectBudgetsBlock other) {
        Map<Integer, AnnualSpendSummary> thisSummaries = new HashMap<>();
        Map<Integer, AnnualSpendSummary> otherSummaries = new HashMap<>();
        if (this.getAnnualSpendSummaries() != null) {
            thisSummaries = this.getAnnualSpendSummaries().stream().
                    collect(Collectors.toMap(AnnualSpendSummary::getYear, Function.identity()));
        }
        if (other.getAnnualSpendSummaries() != null) {
            otherSummaries = other.getAnnualSpendSummaries().stream().
                    collect(Collectors.toMap(AnnualSpendSummary::getYear, Function.identity()));
        }

        for (Iterator<Integer> iterator = thisSummaries.keySet().iterator(); iterator.hasNext(); ) {
            Integer key = iterator.next();
            AnnualSpendSummary thisSpendSummary = thisSummaries.get(key);
            AnnualSpendSummary otherSpendSummary = otherSummaries.get(key);

            if (otherSpendSummary != null) {

                if (GlaUtils.compareBigDecimals(thisSpendSummary.getTotals().getLeftToSpendCapitalInclCurrentMonth(),
                        otherSpendSummary.getTotals().getLeftToSpendCapitalInclCurrentMonth()) != 0) {
                    differences.add(new ProjectDifference(thisSpendSummary, "leftToSpendCapitalInclCurrentMonth"));
                }
                if (GlaUtils.compareBigDecimals(thisSpendSummary.getTotals().getLeftToSpendRevenueInclCurrentMonth(),
                        otherSpendSummary.getTotals().getLeftToSpendRevenueInclCurrentMonth()) != 0) {
                    differences.add(new ProjectDifference(thisSpendSummary, "leftToSpendRevenueInclCurrentMonth"));
                }

                if (GlaUtils.compareBigDecimals(thisSpendSummary.getTotals().getAvailableToForecastCapital(),
                        otherSpendSummary.getTotals().getAvailableToForecastCapital()) != 0) {
                    differences.add(new ProjectDifference(thisSpendSummary, "availableToForecastCapital"));
                }
                if (GlaUtils.compareBigDecimals(thisSpendSummary.getTotals().getAvailableToForecastRevenue(),
                        otherSpendSummary.getTotals().getAvailableToForecastRevenue()) != 0) {
                    differences.add(new ProjectDifference(thisSpendSummary, "availableToForecastRevenue"));
                }


                if (GlaUtils.compareBigDecimals(thisSpendSummary.getAnnualBudgetCapital(),
                        otherSpendSummary.getAnnualBudgetCapital()) != 0) {
                    differences.add(new ProjectDifference(thisSpendSummary, "annualBudgetCapital"));
                }
                if (GlaUtils.compareBigDecimals(thisSpendSummary.getAnnualBudgetRevenue(),
                        otherSpendSummary.getAnnualBudgetRevenue()) != 0) {
                    differences.add(new ProjectDifference(thisSpendSummary, "annualBudgetRevenue"));
                }


                iterator.remove();
                otherSummaries.remove(key);
            }
        }

        for (AnnualSpendSummary summary : thisSummaries.values()) {
            differences.add(new ProjectDifference(summary, ProjectDifference.DifferenceType.Addition));
        }
        for (AnnualSpendSummary summary : otherSummaries.values()) {
            differences.add(new ProjectDifference(summary, ProjectDifference.DifferenceType.Deletion));
        }
    }

    private void compareTotals(ProjectDifferences differences, ProjectBudgetsBlock other) {
        Totals thisTotals = this.getTotals();
        Totals otherTotals = other.getTotals();

        if (thisTotals != null && otherTotals != null) {
            if (GlaUtils.compareBigDecimals(thisTotals.getAvailableToForecastCapital(), otherTotals.getAvailableToForecastCapital()) != 0) {
                differences.add(new ProjectDifference("totals", "availableToForecastCapital"));
            }

            if (GlaUtils.compareBigDecimals(thisTotals.getAvailableToForecastRevenue(), otherTotals.getAvailableToForecastRevenue()) != 0) {
                differences.add(new ProjectDifference("totals", "availableToForecastRevenue"));
            }

            if (GlaUtils.compareBigDecimals(thisTotals.getLeftToSpendOnProjectCapital(), otherTotals.getLeftToSpendOnProjectCapital()) != 0) {
                differences.add(new ProjectDifference("totals", "leftToSpendOnProjectCapital"));
            }

            if (GlaUtils.compareBigDecimals(thisTotals.getApprovedProjectForecastCapital(), otherTotals.getApprovedProjectForecastCapital()) != 0) {
                differences.add(new ProjectDifference("totals", "approvedProjectForecastCapital"));
            }

            if (GlaUtils.compareBigDecimals(thisTotals.getUnapprovedProjectForecastCapital(), otherTotals.getUnapprovedProjectForecastCapital()) != 0) {
                differences.add(new ProjectDifference("totals", "unapprovedProjectForecastCapital"));
            }

            if (GlaUtils.compareBigDecimals(thisTotals.getLeftToSpendOnProjectRevenue(), otherTotals.getLeftToSpendOnProjectRevenue()) != 0) {
                differences.add(new ProjectDifference("totals", "leftToSpendOnProjectRevenue"));
            }

            if (GlaUtils.compareBigDecimals(thisTotals.getApprovedProjectForecastRevenue(), otherTotals.getApprovedProjectForecastRevenue()) != 0) {
                differences.add(new ProjectDifference("totals", "approvedProjectForecastRevenue"));
            }

            if (GlaUtils.compareBigDecimals(thisTotals.getUnapprovedProjectForecastRevenue(), otherTotals.getUnapprovedProjectForecastRevenue()) != 0) {
                differences.add(new ProjectDifference("totals", "unapprovedProjectForecastRevenue"));
            }
        }

    }

    public static class Totals {

        private BigDecimal availableToForecastCapital;
        private BigDecimal availableToForecastRevenue;

        private BigDecimal leftToSpendOnProjectCapital;
        private BigDecimal leftToSpendOnProjectRevenue;

        private BigDecimal approvedProjectForecastCapital;
        private BigDecimal approvedProjectForecastRevenue;

        private BigDecimal unapprovedProjectForecastCapital;
        private BigDecimal unapprovedProjectForecastRevenue;

        public BigDecimal getAvailableToForecastCapital() {
            return availableToForecastCapital;
        }

        public void setAvailableToForecastCapital(BigDecimal availableToForecastCapital) {
            this.availableToForecastCapital = availableToForecastCapital;
        }

        public BigDecimal getAvailableToForecastRevenue() {
            return availableToForecastRevenue;
        }

        public void setAvailableToForecastRevenue(BigDecimal availableToForecastRevenue) {
            this.availableToForecastRevenue = availableToForecastRevenue;
        }

        public BigDecimal getLeftToSpendOnProjectCapital() {
            return leftToSpendOnProjectCapital;
        }

        public void setLeftToSpendOnProjectCapital(BigDecimal leftToSpendOnProjectCapital) {
            this.leftToSpendOnProjectCapital = leftToSpendOnProjectCapital;
        }

        public BigDecimal getApprovedProjectForecastCapital() {
            return approvedProjectForecastCapital;
        }

        public void setApprovedProjectForecastCapital(BigDecimal approvedProjectForecastCapital) {
            this.approvedProjectForecastCapital = approvedProjectForecastCapital;
        }

        public BigDecimal getUnapprovedProjectForecastCapital() {
            return unapprovedProjectForecastCapital;
        }

        public void setUnapprovedProjectForecastCapital(BigDecimal unapprovedProjectForecastCapital) {
            this.unapprovedProjectForecastCapital = unapprovedProjectForecastCapital;
        }

        public BigDecimal getLeftToSpendOnProjectRevenue() {
            return leftToSpendOnProjectRevenue;
        }

        public void setLeftToSpendOnProjectRevenue(BigDecimal leftToSpendOnProjectRevenue) {
            this.leftToSpendOnProjectRevenue = leftToSpendOnProjectRevenue;
        }

        public BigDecimal getApprovedProjectForecastRevenue() {
            return approvedProjectForecastRevenue;
        }

        public void setApprovedProjectForecastRevenue(BigDecimal approvedProjectForecastRevenue) {
            this.approvedProjectForecastRevenue = approvedProjectForecastRevenue;
        }

        public BigDecimal getUnapprovedProjectForecastRevenue() {
            return unapprovedProjectForecastRevenue;
        }

        public void setUnapprovedProjectForecastRevenue(BigDecimal unapprovedProjectForecastRevenue) {
            this.unapprovedProjectForecastRevenue = unapprovedProjectForecastRevenue;
        }

    }

}
