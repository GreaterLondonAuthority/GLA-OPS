/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
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
import org.springframework.util.StringUtils;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.ComparableItem;

@Entity(name = "other_funding")
public class OtherFunding implements RepeatingEntity, ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "other_funding_gen")
    @SequenceGenerator(name = "other_funding_gen", sequenceName = "other_funding_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "funding_source")
    private String fundingSource;

    @Column(name = "funder_name")
    private String funderName;

    @Column(name = "description")
    private String description;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "is_funding_secured")
    private Boolean isFundingSecured;

    @Column(name = "date_secured")
    private LocalDate dateSecured;

    @Column(name = "estimate_date_secured")
    private LocalDate estimateDateSecured;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "other_funding_attachment",
            joinColumns = @JoinColumn(name = "other_funding_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "attachment_id", referencedColumnName = "id"))
    Set<AttachmentFile> attachments = new HashSet<>();

    @Column
    private String createdBy;

    @Column
    private OffsetDateTime createdOn;

    @JoinColumn(name = "modified_by")
    private String modifiedBy;

    @Column
    private OffsetDateTime modifiedOn;

    @Column(name = "original_id")
    @JoinData(targetTable = "other_funding", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "A reference to a previous version of this item if this is a cloned via a new block version.")
    private Integer originalId;

    public OtherFunding() {
    }

    public OtherFunding(String fundingSource) {
        this.fundingSource = fundingSource;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(getOriginalId());
    }

    public String getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
    }

    public String getFunderName() {
        return funderName;
    }

    public void setFunderName(String funderName) {
        this.funderName = funderName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Boolean isFundingSecured() {
        return isFundingSecured;
    }

    public void setFundingSecured(Boolean fundingSecured) {
        isFundingSecured = fundingSecured;
    }

    public LocalDate getDateSecured() {
        return dateSecured;
    }

    public void setDateSecured(LocalDate dateSecured) {
        this.dateSecured = dateSecured;
    }

    public LocalDate getEstimateDateSecured() {
        return estimateDateSecured;
    }

    public void setEstimateDateSecured(LocalDate estimateDateSecured) {
        this.estimateDateSecured = estimateDateSecured;
    }

    public Set<AttachmentFile> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<AttachmentFile> attachments) {
        this.attachments = attachments;
    }

    @Override
    public boolean isComplete() {
        return !(StringUtils.isEmpty(fundingSource));
    }

    public OtherFunding copy() {
        OtherFunding copy = new OtherFunding();
        copy.setFundingSource(getFundingSource());
        copy.setFunderName(getFunderName());
        copy.setDescription(getDescription());
        copy.setAmount(getAmount());
        copy.setFundingSecured(isFundingSecured());
        copy.setDateSecured(getDateSecured());
        copy.setEstimateDateSecured(getEstimateDateSecured());
        copy.attachments.addAll(getAttachments());
        copy.setOriginalId(getOriginalId());
        copy.setCreatedBy(getCreatedBy());
        copy.setCreatedOn(getCreatedOn());
        return copy;
    }

    @Override
    public void update(RepeatingEntity fromEntity) {
        OtherFunding fromOF = (OtherFunding) fromEntity;
        this.setFundingSource(fromOF.getFundingSource());
        this.setFunderName(fromOF.getFunderName());
        this.setDescription(fromOF.getDescription());
        this.setAmount(fromOF.getAmount());
        this.setFundingSecured(fromOF.isFundingSecured());
        this.setDateSecured(fromOF.getDateSecured());
        this.setEstimateDateSecured(fromOF.getEstimateDateSecured());
        this.setAttachments(fromOF.getAttachments());
    }

}
