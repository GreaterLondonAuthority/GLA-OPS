/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.skills;

import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.framework.OpsEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity(name = "payment_profile")
/**
 * Entity storing the percentage data for a specific month/year, by grant type.  
 */
public class SkillsPaymentProfile implements OpsEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_profile_seq_gen")
    @SequenceGenerator(name = "payment_profile_seq_gen", sequenceName = "payment_profile_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private SkillsGrantType type = SkillsGrantType.AEB_GRANT;

    @Column(name = "year")
    private Integer year;

    @Column(name = "period")
    private Integer period;

    @Column(name = "percentage")
    private BigDecimal percentage;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Column(name = "payment_date")
    private OffsetDateTime paymentDate;

    @Transient
    private boolean editable;

    public SkillsPaymentProfile() {
    }

    public SkillsPaymentProfile(SkillsGrantType type, Integer year, Integer period, BigDecimal percentage) {
        this.type = type;
        this.year = year;
        this.period = period;
        this.percentage = percentage;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SkillsGrantType getType() {
        return type;
    }

    public void setType(SkillsGrantType type) {
        this.type = type;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
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
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
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
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public OffsetDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(OffsetDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
