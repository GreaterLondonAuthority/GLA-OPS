/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.label;

import uk.gov.london.ops.framework.OpsEntity;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity(name = "label")
public class Label implements OpsEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "label_seq_gen")
    @SequenceGenerator(name = "label_seq_gen", sequenceName = "label_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "text")
    private String text;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "pre_set_label_id")
    private PreSetLabelEntity preSetLabel;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private LabelType type = LabelType.Custom;

    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    public Label() {
    }

    public Label(String text) {
        this.text = text;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public PreSetLabelEntity getPreSetLabel() {
        return preSetLabel;
    }

    public void setPreSetLabel(PreSetLabelEntity preSetLabel) {
        this.preSetLabel = preSetLabel;
    }

    public LabelType getType() {
        return type;
    }

    public void setType(LabelType type) {
        this.type = type;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
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

}
