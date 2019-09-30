/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.domain.PreSetLabel;
import uk.gov.london.ops.domain.user.User;


@Entity(name = "label")
public class Label implements OpsEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "label_seq_gen")
    @SequenceGenerator(name = "label_seq_gen", sequenceName = "label_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name="text")
    private String text;

    @ManyToOne(cascade = {})
    @JoinColumn(name="pre_set_label_id")
    private PreSetLabel preSetLabel;

    @Column(name="type")
    @Enumerated(EnumType.STRING)
    private LabelType type = LabelType.Custom;

    @Column(name = "project_id")
    private Integer projectId;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "created_by")
    private User creator;

    @Column(name="created_on", updatable = false)
    private OffsetDateTime createdOn;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "modified_by")
    private User modifiedBy;

    @Column(name="modified_on")
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

    public PreSetLabel getPreSetLabel() {
        return preSetLabel;
    }

    public void setPreSetLabel(PreSetLabel preSetLabel) {
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Override
    public String getCreatedBy() {
        return creator != null ? creator.getUsername() : null;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.creator = new User(createdBy);
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
        return modifiedBy != null ? modifiedBy.getUsername() : null;
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = new User(modifiedBy);
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
