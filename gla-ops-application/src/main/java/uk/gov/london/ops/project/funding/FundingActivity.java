/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.funding;

import static uk.gov.london.common.GlaUtils.addBigDecimals;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.MATCH_FUND_CATEGORY;
import static uk.gov.london.ops.payment.SpendType.CAPITAL;
import static uk.gov.london.ops.payment.SpendType.REVENUE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import uk.gov.london.ops.payment.ProjectLedgerEntry;
import uk.gov.london.ops.payment.SpendType;
import uk.gov.london.ops.project.StandardAttachment;

@Entity(name = "funding_activity")
public class FundingActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "funding_activity_seq_gen")
    @SequenceGenerator(name = "funding_activity_seq_gen", sequenceName = "funding_activity_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "original_id")
    private Integer originalId;

    @Column(name = "block_id")
    private Integer blockId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "quarter")
    private Integer quarter;

    @Column(name = "name")
    private String name;

    @Column(name = "external_id")
    private Integer externalId;

    @Column(name = "category_description")
    private String categoryDescription;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "funding_activity_project_ledger_entry",
            joinColumns = @JoinColumn(name = "funding_activity_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "project_ledger_entry_id", referencedColumnName = "id"))
    private List<ProjectLedgerEntry> ledgerEntries = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = StandardAttachment.class)
    @JoinColumn(name = "funding_activity_id")
    private List<StandardAttachment> attachments = new ArrayList<>();

    public FundingActivity() {
    }

    public FundingActivity(Integer blockId, Integer year, Integer quarter, Integer externalId, String categoryDescription) {
        this.blockId = blockId;
        this.year = year;
        this.quarter = quarter;
        this.externalId = externalId;
        this.categoryDescription = categoryDescription;
    }

    public FundingActivity(Integer blockId, Integer year, Integer quarter, String name, Integer externalId,
            String categoryDescription) {
        this(blockId, year, quarter, externalId, categoryDescription);
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOriginalId() {
        if (originalId == null) {
            return id;
        }
        return originalId;
    }

    public void setOriginalId(Integer originalId) {
        this.originalId = originalId;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }

    public List<ProjectLedgerEntry> getLedgerEntries() {
        return ledgerEntries;
    }

    public void setLedgerEntries(List<ProjectLedgerEntry> ledgerEntries) {
        this.ledgerEntries = ledgerEntries;
    }

    public List<StandardAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<StandardAttachment> attachments) {
        this.attachments = attachments;
    }

    @JsonIgnore
    public ProjectLedgerEntry getCapitalMainLedgerEntry() {
        return getLedgerEntry(CAPITAL, null);
    }

    @JsonIgnore
    public ProjectLedgerEntry getCapitalMatchFundLedgerEntry() {
        return getLedgerEntry(CAPITAL, MATCH_FUND_CATEGORY);
    }

    @JsonIgnore
    public ProjectLedgerEntry getRevenueMainLedgerEntry() {
        return getLedgerEntry(REVENUE, null);
    }

    @JsonIgnore
    public ProjectLedgerEntry getRevenueMatchFundLedgerEntry() {
        return getLedgerEntry(REVENUE, MATCH_FUND_CATEGORY);
    }

    public ProjectLedgerEntry getLedgerEntry(SpendType spendType, String category) {
        return ledgerEntries.stream()
                .filter(le -> Objects.equals(spendType, le.getSpendType()) && Objects.equals(category, le.getCategory()))
                .findFirst().orElse(null);
    }

    @JsonIgnore
    public BigDecimal getCapitalMainValue() {
        return getValue(getLedgerEntry(CAPITAL, null));
    }

    @JsonIgnore
    public BigDecimal getCapitalMatchFundValue() {
        return getValue(getLedgerEntry(CAPITAL, MATCH_FUND_CATEGORY));
    }

    @JsonIgnore
    public BigDecimal getRevenueMainValue() {
        return getValue(getLedgerEntry(REVENUE, null));
    }

    @JsonIgnore
    public BigDecimal getRevenueMatchFundValue() {
        return getValue(getLedgerEntry(REVENUE, MATCH_FUND_CATEGORY));
    }

    @JsonIgnore
    public BigDecimal getTotalCapitalValue() {
        return addBigDecimals(getCapitalMainValue(), getCapitalMatchFundValue());
    }

    @JsonIgnore
    public BigDecimal getTotalRevenueValue() {
        return addBigDecimals(getRevenueMainValue(), getRevenueMatchFundValue());
    }

    private BigDecimal getValue(ProjectLedgerEntry ledgerEntry) {
        return ledgerEntry != null ? ledgerEntry.getValue() : null;
    }

    public FundingActivity clone(Integer clonedProjectId, Integer clonedBlockId) {
        FundingActivity clone = new FundingActivity();
        clone.setOriginalId(this.getOriginalId());
        clone.setName(this.getName());
        clone.setBlockId(clonedBlockId);
        clone.setYear(this.getYear());
        clone.setQuarter(this.getQuarter());
        clone.setExternalId(this.getExternalId());
        clone.setCategoryDescription(this.getCategoryDescription());

        for (ProjectLedgerEntry entry : this.getLedgerEntries()) {
            clone.getLedgerEntries().add(entry.clone(clonedProjectId, clonedBlockId));
        }

        for (StandardAttachment attachment : this.getAttachments()) {
            clone.getAttachments().add(attachment.copy());
        }

        return clone;
    }

}
