/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import uk.gov.london.ops.util.jpajoins.NonJoin;

import javax.persistence.*;
import java.util.Objects;

@Entity(name="milestone_template")
public class MilestoneTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "milestone_template_seq_gen")
    @SequenceGenerator(name = "milestone_template_seq_gen", sequenceName = "milestone_template_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Column(name = "external_id")
    @NonJoin("External IDs are shared across milestons so similar milestones can be identified")
    private Integer externalId;

    @Column(name = "summary")
    private String summary;

    @Column(name="requirement")
    @Enumerated(EnumType.STRING)
    private Requirement requirement;

    @Column(name = "monetary_split")
    private Integer monetarySplit;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "monetary")
    private Boolean monetary;

    @Column(name="key_event")
    private boolean keyEvent;

    @Column(name="na_selectable")
    private boolean naSelectable;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
    }

    public Integer getMonetarySplit() {
        return monetarySplit;
    }

    public void setMonetarySplit(Integer monetarySplit) {
        this.monetarySplit = monetarySplit;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getMonetary() {
        return monetary;
    }

    public void setMonetary(Boolean monetary) {
        this.monetary = monetary;
    }

    public boolean isKeyEvent() {
        return keyEvent;
    }

    public void setKeyEvent(boolean keyEvent) {
        this.keyEvent = keyEvent;
    }

    public boolean isNaSelectable() {
        return naSelectable;
    }

    public void setNaSelectable(boolean naSelectable) {
        this.naSelectable = naSelectable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MilestoneTemplate that = (MilestoneTemplate) o;
        return Objects.equals(externalId, that.externalId) &&
                Objects.equals(summary, that.summary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(externalId, summary);
    }

}
