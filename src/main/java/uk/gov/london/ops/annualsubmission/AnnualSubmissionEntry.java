/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "annual_submission_entry")
public class AnnualSubmissionEntry implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "annual_submission_entry_seq_gen")
    @SequenceGenerator(name = "annual_submission_entry_seq_gen", sequenceName = "annual_submission_entry_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @JsonIgnore
    @Column(name = "block_id")
    private Integer blockId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private AnnualSubmissionCategory category;

    @Column(name = "value")
    private Integer value;

    @Column(name = "comments")
    private String comments;

    @Column(name = "financial_year")
    private Integer financialYear;

    @Transient
    private Integer categoryId;

    public AnnualSubmissionEntry() {}

    public AnnualSubmissionEntry(AnnualSubmissionCategory category, Integer value, String comments) {
        this.category = category;
        this.value = value;
        this.comments = comments;
    }

    public AnnualSubmissionEntry(Integer financialYear, AnnualSubmissionCategory category, Integer value, String comments) {
        this(category, value, comments);
        this.financialYear = financialYear;
    }

    public Integer getId() {
        return id;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }

    public AnnualSubmissionCategory getCategory() {
        return category;
    }

    public void setCategory(AnnualSubmissionCategory category) {
        this.category = category;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(Integer financialYear) {
        this.financialYear = financialYear;
    }

    public Integer getCategoryId() {
        return category != null ? category.getId() : categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnualSubmissionEntry entry = (AnnualSubmissionEntry) o;
        return Objects.equals(id, entry.id) &&
                Objects.equals(blockId, entry.blockId) &&
                Objects.equals(category, entry.category) &&
                Objects.equals(value, entry.value) &&
                Objects.equals(comments, entry.comments) &&
                Objects.equals(financialYear, entry.financialYear) &&
                Objects.equals(categoryId, entry.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, blockId, category, value, comments, financialYear, categoryId);
    }

}
