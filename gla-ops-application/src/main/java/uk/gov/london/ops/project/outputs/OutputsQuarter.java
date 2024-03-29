/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.project.outputs;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.framework.environment.EnvironmentUtils;
import uk.gov.london.ops.payment.PaymentSummary;
import uk.gov.london.ops.project.claim.Claim;
import uk.gov.london.ops.project.claim.ClaimStatus;
import uk.gov.london.ops.project.claim.ClaimType;
import uk.gov.london.ops.project.claim.Claimable;

public class OutputsQuarter implements Claimable {

    List<OutputsMonth> outputsMonths = new ArrayList<>();
    private boolean claimable = false;
    private Claim claim;
    private Integer year;
    private Integer quarter;
    private List<PaymentSummary> payments = new ArrayList<>();

    public OutputsQuarter(Integer year, Integer quarter, boolean claimable) {
        this.year = year;
        this.quarter = quarter;
        this.claimable = claimable;
    }

    public OutputsQuarter(Integer year, Integer quarter, boolean claimable, List<PaymentSummary> payments) {
        this(year, quarter, claimable);
        this.payments = payments;
    }

    public static List<OutputsQuarter> convertToQuarters(Integer financialYear, Set<OutputTableEntry> tableData,
            Set<Claim> claims, boolean hasMissingClaimsInPreviousYear, boolean missingAdvancePaymentApproval,
            boolean paymentsEnabled, boolean projectBudgetExceeded, List<PaymentSummary> payments) {
        List<OutputsQuarter> quarters = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Integer quarter = i + 1;
            List<PaymentSummary> quarterPayments = payments.stream()
                    .filter(p -> financialYear.equals(p.getYear()) && quarter.equals(p.getQuarter()))
                    .collect(Collectors.toList());
            quarters.add(new OutputsQuarter(financialYear, quarter, false, quarterPayments));
        }

        for (OutputTableEntry entry : tableData) {
            int quarter = getQuarter(entry.getMonth());
            OutputsQuarter outputQuarter = quarters.get(quarter - 1);
            outputQuarter.addEntry(entry);
        }

        for (int i = 0; i < 4; i++) {
            OutputsQuarter quarter = quarters.get(i);
            final int quarterNumber = i + 1;
            quarter.setClaim(claims.stream().filter(c -> c.getClaimType().equals(ClaimType.QUARTER)
                    && c.getClaimTypePeriod().equals(quarterNumber) && c.getYear().equals(financialYear)).findFirst().orElse(null));
            boolean isClaimable = paymentsEnabled
                    && !hasMissingClaimsInPreviousQuarters(i + 1, quarters, hasMissingClaimsInPreviousYear)
                            && !missingAdvancePaymentApproval && quarter.getClaim() == null
                            && isQuarterInThePast(financialYear, quarterNumber) && !quarter.getOutputsMonths().isEmpty()
                            && !projectBudgetExceeded;
            quarter.setClaimable(isClaimable);
        }
        return quarters;
    }

    private static boolean hasMissingClaimsInPreviousQuarters(int quarter, List<OutputsQuarter> quarters,
            boolean hasMissingClaimsInPreviousYear) {
        if (hasMissingClaimsInPreviousYear) {
            return true;
        }

        for (int i = 0; i < quarter - 1; i++) {
            if (quarters.get(i).isClaimable()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isQuarterInThePast(Integer year, Integer quarter) {
        OffsetDateTime now = EnvironmentUtils.now();
        int currentQuarter = GlaUtils.getCurrentQuarter(now.getMonthValue());
        int currentYear = now.getYear();
        int currentFinancialYear = currentQuarter == 4 ? currentYear - 1 : currentYear;
        return !(currentFinancialYear < year || (currentFinancialYear == year && currentQuarter <= quarter));
    }

    @Override
    public boolean isClaimable() {
        return claimable;
    }

    public void setClaimable(boolean claimable) {
        this.claimable = claimable;
    }

    public Claim getClaim() {
        return claim;
    }

    public static int getQuarter(int month) {
        return month < 4 ? 4 : (int) Math.floor((month - 1) / 3);
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public List<OutputsMonth> getOutputsMonths() {
        return outputsMonths;
    }

    public void setOutputsMonths(List<OutputsMonth> outputsMonths) {
        this.outputsMonths = outputsMonths;
    }

    private void addEntry(OutputTableEntry entry) {
        Integer month = entry.getMonth();
        OutputsMonth outputsMonth = outputsMonths.stream().filter(om -> om.getMonth().equals(month)).findFirst().orElse(null);
        if (outputsMonth == null) {
            outputsMonth = new OutputsMonth(month);
            outputsMonths.add(outputsMonth);
        }
        outputsMonth.addEntry(entry);
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    @Override
    public List<PaymentSummary> getPayments() {
        return payments;
    }

    public BigDecimal getForecastTotal() {
        return outputsMonths.stream().map(c -> c.getForecastTotal()).reduce(null, GlaUtils::nullSafeAdd);
    }

    public BigDecimal getActualTotal() {
        return outputsMonths.stream().map(c -> c.getActualTotal()).reduce(null, GlaUtils::nullSafeAdd);
    }

    public BigDecimal getDifferenceTotal() {
        return outputsMonths.stream().map(c -> c.getDifferenceTotal()).reduce(null, GlaUtils::nullSafeAdd);
    }

    public BigDecimal getForecastTotalTotal() {
        return outputsMonths.stream().map(c -> c.getForecastTotalTotal()).reduce(null, GlaUtils::nullSafeAdd);
    }

    public BigDecimal getActualTotalTotal() {
        return outputsMonths.stream().map(c -> c.getActualTotalTotal()).reduce(null, GlaUtils::nullSafeAdd);
    }

    public BigDecimal getRemainingAdvancePaymentTotal() {
        return outputsMonths.stream().map(c -> c.getRemainingAdvancePaymentTotal()).reduce(null, GlaUtils::nullSafeAdd);
    }

    public BigDecimal getClaimableAmountTotal() {
        return outputsMonths.stream().map(c -> c.getClaimableAmountTotal()).reduce(null, GlaUtils::nullSafeAdd);
    }

    @Override
    public BigDecimal getClaimableAmount() {
        return getClaimableAmountTotal();
    }

    @Override
    public boolean hasPaymentStatus() {
        return hasPayments() || (claim != null && ClaimStatus.Approved.equals(claim.getClaimStatus()));
    }

}
