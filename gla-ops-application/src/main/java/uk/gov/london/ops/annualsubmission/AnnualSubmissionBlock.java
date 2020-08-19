/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.domain.LockableEntity;
import uk.gov.london.ops.user.domain.User;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.nullSafeAdd;
import static uk.gov.london.ops.annualsubmission.AnnualSubmissionStatusType.Actual;
import static uk.gov.london.ops.annualsubmission.AnnualSubmissionStatusType.Forecast;

@Entity(name = "annual_submission_block")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AnnualSubmissionBlock implements Serializable, LockableEntity {

    public enum Action { EDIT }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "annual_submission_block_seq_gen")
    @SequenceGenerator(name = "annual_submission_block_seq_gen", sequenceName = "annual_submission_block_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "no_generated_data")
    private boolean noGeneratedData;

    @Column(name = "no_spent_data")
    private boolean noSpentData;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_type")
    private AnnualSubmissionStatusType statusType;

    @Enumerated(EnumType.STRING)
    @Column(name = "grant_type")
    private AnnualSubmissionGrantType grantType;

    @Column(name = "total_unspent_grant")
    private Integer totalUnspentGrant = 0;

    @Column(name = "rolled_over_unspent_grant")
    private Integer rolledOverUnspentGrant = 0;

    @Column(name = "interested_accumulated")
    private Integer interestedAccumulated = 0;

    @Column(name = "rolled_over_interested_accumulated")
    private Integer rolledOverInterestAccumulated = 0;

    @Column(name = "balance_rollover_confirmed")
    private Boolean balanceRolloverConfirmed;

    @Column(name = "unspent_grant_year_1")
    private Integer unspentGrantYear1;

    @Column(name = "unspent_grant_year_2")
    private Integer unspentGrantYear2;

    @Column(name = "unspent_grant_year_3")
    private Integer unspentGrantYear3;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "locked_by")
    private User lockedBy;

    @Column(name = "lock_timeout_time")
    private OffsetDateTime lockTimeoutTime;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = AnnualSubmissionEntry.class)
    @JoinColumn(name = "block_id")
    private List<AnnualSubmissionEntry> entries = new ArrayList<>();

    @Column(name = "opening_balance")
    private Integer openingBalance;

    @Transient
    private int computedOpeningBalance = 0;

    @Column(name = "closing_balance")
    private Integer persistedClosingBalance;

    @Transient
    private Integer closingBalance;

    @Transient
    private Integer financialYear;

    @Transient
    private List<AnnualSubmissionBlockTotals> totals = new ArrayList<>();

    @Transient
    private List<AnnualSubmissionBlockYearBreakdown> yearBreakdown = new ArrayList<>();

    @Transient
    private Set<Action> allowedActions = new HashSet<>();

    public AnnualSubmissionBlock() {}

    public AnnualSubmissionBlock(AnnualSubmissionStatusType statusType, AnnualSubmissionGrantType grantType) {
        this.statusType = statusType;
        this.grantType = grantType;
    }

    public Integer getId() {
        return id;
    }

    public boolean isNoGeneratedData() {
        return noGeneratedData;
    }

    public void setNoGeneratedData(boolean noGeneratedData) {
        this.noGeneratedData = noGeneratedData;
    }

    public boolean isNoSpentData() {
        return noSpentData;
    }

    public void setNoSpentData(boolean noSpentData) {
        this.noSpentData = noSpentData;
    }

    public AnnualSubmissionStatusType getStatusType() {
        return statusType;
    }

    public void setStatusType(AnnualSubmissionStatusType statusType) {
        this.statusType = statusType;
    }

    public AnnualSubmissionGrantType getGrantType() {
        return grantType;
    }

    public void setGrantType(AnnualSubmissionGrantType grantType) {
        this.grantType = grantType;
    }

    public Integer getTotalUnspentGrant() {
        return totalUnspentGrant;
    }

    public void setTotalUnspentGrant(Integer totalUnspentGrant) {
        this.totalUnspentGrant = totalUnspentGrant;
    }

    public Integer getSumOfTotalUnspentGrantAndInterest() {
        return nullSafeAdd(totalUnspentGrant,interestedAccumulated);
    }

    public Integer getInterestedAccumulated() {
        return interestedAccumulated;
    }

    public void setInterestedAccumulated(Integer interestedAccumulated) {
        this.interestedAccumulated = interestedAccumulated;
    }

    public Boolean isBalanceRolloverConfirmed() {
        return balanceRolloverConfirmed;
    }

    public void setBalanceRolloverConfirmed(Boolean balanceRolloverConfirmed) {
        this.balanceRolloverConfirmed = balanceRolloverConfirmed;
    }

    public Integer getUnspentGrantYear1() {
        return unspentGrantYear1;
    }

    public void setUnspentGrantYear1(Integer unspentGrantYear1) {
        this.unspentGrantYear1 = unspentGrantYear1;
    }

    public Integer getUnspentGrantYear2() {
        return unspentGrantYear2;
    }

    public void setUnspentGrantYear2(Integer unspentGrantYear2) {
        this.unspentGrantYear2 = unspentGrantYear2;
    }

    public Integer getUnspentGrantYear3() {
        return unspentGrantYear3;
    }

    public void setUnspentGrantYear3(Integer unspentGrantYear3) {
        this.unspentGrantYear3 = unspentGrantYear3;
    }

    @Override
    public User getLockedBy() {
        return lockedBy;
    }

    @Override
    public void setLockedBy(User lockedBy) {
        this.lockedBy = lockedBy;
    }

    @Override
    public OffsetDateTime getLockTimeoutTime() {
        return lockTimeoutTime;
    }

    @Override
    public void setLockTimeoutTime(OffsetDateTime lockTimeoutTime) {
        this.lockTimeoutTime = lockTimeoutTime;
    }

    public Integer getRolledOverUnspentGrant() {
        return rolledOverUnspentGrant;
    }

    public void setRolledOverUnspentGrant(Integer rolledOverUnspentGrant) {
        this.rolledOverUnspentGrant = rolledOverUnspentGrant;
    }

    public Integer getRolledOverInterestAccumulated() {
        return rolledOverInterestAccumulated;
    }

    public void setRolledOverInterestAccumulated(Integer rolledOverInterestAccumulated) {
        this.rolledOverInterestAccumulated = rolledOverInterestAccumulated;
    }

    public List<AnnualSubmissionEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<AnnualSubmissionEntry> entries) {
        this.entries = entries;
    }

    public boolean isRolloverInterestExceeded() {
        return (balanceRolloverConfirmed != null && balanceRolloverConfirmed) &&
                (rolledOverInterestAccumulated != null &&  interestedAccumulated != null) &&
                (rolledOverInterestAccumulated > interestedAccumulated);

    }

    public boolean isRolloverGrantExceeded() {
        return (balanceRolloverConfirmed != null && balanceRolloverConfirmed) &&
                (rolledOverUnspentGrant != null &&  totalUnspentGrant != null) &&
                (rolledOverUnspentGrant > totalUnspentGrant);

    }

    public boolean isComplete() {
        if (!noSpentData && getSpentEntries().isEmpty()) {
            return false;
        }

        if (Actual.equals(statusType)) {
            if (!noGeneratedData && getGeneratedEntries().isEmpty()) {
                return false;
            }

            if (totalUnspentGrant == null || (totalUnspentGrant > 0 && interestedAccumulated == null)) {
                return false;
            }

            if (getClosingBalance() < 0) {
                return false;
            }

            if (balanceRolloverConfirmed != null && balanceRolloverConfirmed) {
                if (rolledOverUnspentGrant == null || rolledOverInterestAccumulated == null) {
                    return false;
                }
            }

            return !isRolloverInterestExceeded() && !isRolloverGrantExceeded();
        }
        else if (Forecast.equals(statusType)) {
            if (unspentGrantYear1 == null || unspentGrantYear2 == null || unspentGrantYear3 == null) {
                return false;
            }

            return getUnspentGrantTotal() == null || getClosingBalance() == getUnspentGrantTotal();
        }

        return true;
    }

    public void merge(AnnualSubmissionBlock updated) {
        this.setNoGeneratedData(updated.isNoGeneratedData());
        this.setNoSpentData(updated.isNoSpentData());

        this.setTotalUnspentGrant(updated.getTotalUnspentGrant());
        this.setInterestedAccumulated(updated.getInterestedAccumulated());

        if ((updated.getTotalUnspentGrant() == null || updated.getTotalUnspentGrant() == 0)
                && (updated.getInterestedAccumulated() == null || updated.getInterestedAccumulated() == 0)) {
            updated.setBalanceRolloverConfirmed(false);
        }

        this.setBalanceRolloverConfirmed(updated.isBalanceRolloverConfirmed());
        if (updated.isBalanceRolloverConfirmed() == null || !updated.isBalanceRolloverConfirmed()) {
            this.setRolledOverUnspentGrant(null);
            this.setRolledOverInterestAccumulated(null);
        }
        else {
            this.setRolledOverUnspentGrant(updated.getRolledOverUnspentGrant());
            this.setRolledOverInterestAccumulated(updated.getRolledOverInterestAccumulated());
        }

        this.setUnspentGrantYear1(updated.getUnspentGrantYear1());
        this.setUnspentGrantYear2(updated.getUnspentGrantYear2());
        this.setUnspentGrantYear3(updated.getUnspentGrantYear3());
        this.openingBalance = updated.openingBalance;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.annualSubmissionBlock;
    }

    @Override
    public String getLockedByUsername() {
        return lockedBy != null ? lockedBy.getUsername() : null;
    }

    @Override
    public String getLockedByFirstName() {
        return lockedBy != null ? lockedBy.getFirstName() : null;
    }

    @Override
    public String getLockedByLastName() {
        return lockedBy != null ? lockedBy.getLastName() : null;
    }

    public List<AnnualSubmissionEntry> getGeneratedEntries() {
        return entries.stream().filter(e -> AnnualSubmissionType.Generated.equals(e.getCategory().getType())).collect(Collectors.toList());
    }

    public List<AnnualSubmissionEntry> getSpentEntries() {
        return entries.stream().filter(e -> AnnualSubmissionType.Spent.equals(e.getCategory().getType())).collect(Collectors.toList());
    }

    public List<AnnualSubmissionEntry> getEntries(Integer financialYear) {
        return entries.stream().filter(e -> financialYear.equals(e.getFinancialYear())).collect(Collectors.toList());
    }

    public AnnualSubmissionEntry getEntryByCategory(AnnualSubmissionCategory category, Integer financialYear) {
        return entries.stream().filter(e -> Objects.equals(category, e.getCategory()) && Objects.equals(financialYear, e.getFinancialYear())).findFirst().orElse(null);
    }

    public AnnualSubmissionEntry getEntryById(Integer id) {
        return entries.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }

    public AnnualSubmissionBlockTemplate getTemplate() {
        if (AnnualSubmissionGrantType.DPF == grantType && Integer.valueOf(2019).equals(financialYear)) {
            return new AnnualSubmissionBlockTemplate(financialYear, false, 1, 1);
        }
        else {
            return new AnnualSubmissionBlockTemplate(financialYear, true, 3, 4);
        }
    }

    public int getOpeningBalance() {
        return openingBalance != null ? openingBalance : computedOpeningBalance;
    }

    public Integer getPersistedOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(Integer openingBalance) {
        this.openingBalance = openingBalance;
    }

    public int getComputedOpeningBalance() {
        return computedOpeningBalance;
    }

    public void setComputedOpeningBalance(int computedOpeningBalance) {
        this.computedOpeningBalance = computedOpeningBalance;
    }

    public Integer getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(Integer financialYear) {
        this.financialYear = financialYear;
    }

    public int getTotalGenerated() {
        return getGeneratedEntries().stream().map(AnnualSubmissionEntry::getValue).reduce(0, Integer::sum);
    }

    public int getTotalSpent() {
        return getSpentEntries().stream().map(AnnualSubmissionEntry::getValue).reduce(0, Integer::sum);
    }

    public int getTotal(Integer financialYear) {
        return getEntries(financialYear).stream().map(AnnualSubmissionEntry::getValue).reduce(0, Integer::sum);
    }

    public int getClosingBalance() {
        if (closingBalance == null) {
            closingBalance = getOpeningBalance() + getTotalGenerated() - getTotalSpent();
            this.persistedClosingBalance = closingBalance;
        }
        return closingBalance;
    }

    public void setClosingBalance(Integer closingBalance) {
        this.closingBalance = closingBalance;
        this.persistedClosingBalance = closingBalance;
    }

    public Integer getPersistedClosingBalance() {
        return persistedClosingBalance;
    }

    public void setPersistedClosingBalance(Integer persistedClosingBalance) {
        this.persistedClosingBalance = persistedClosingBalance;
    }

    public void recalculateClosingBalance() {
        this.closingBalance = null;
    }

    public List<AnnualSubmissionBlockTotals> getTotals() {
        return totals;
    }

    public void setTotals(List<AnnualSubmissionBlockTotals> totals) {
        this.totals = totals;
    }

    public List<AnnualSubmissionBlockYearBreakdown> getYearBreakdown() {
        return yearBreakdown;
    }

    public void setYearBreakdown(List<AnnualSubmissionBlockYearBreakdown> yearBreakdown) {
        this.yearBreakdown = yearBreakdown;
    }

    public Set<Action> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(Set<Action> allowedActions) {
        this.allowedActions = allowedActions;
    }

    public Integer getUnspentGrantTotal() {
        return nullSafeAdd(totalUnspentGrant, unspentGrantYear1, unspentGrantYear2, unspentGrantYear3, interestedAccumulated);
    }

}
