/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.internalblock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import uk.gov.london.ops.framework.OpsEntity;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.risk.InternalRiskBlock;
import uk.gov.london.ops.project.template.domain.InternalTemplateBlock;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Abstract base class for different internal project block types.
 */
@Entity(name = "internal_project_block")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "block_type")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        property = "json_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InternalRiskBlock.class),
        @JsonSubTypes.Type(value = InternalQuestionsBlock.class),
        @JsonSubTypes.Type(value = InternalProjectAdminBlock.class)
})
@DiscriminatorValue("BASE")
public abstract class InternalProjectBlock implements Serializable, OpsEntity<Integer>, Comparable<InternalProjectBlock> {

    @Id
    @JoinData(joinType = Join.JoinType.Complex, sourceTable = "internal_project_block",
            comment = "This id is shared amongst all child blocks, for example internal_risk_block etc")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "internal_project_block_seq_gen")
    @SequenceGenerator(name = "internal_project_block_seq_gen", sequenceName = "internal_project_block_seq",
            initialValue = 10000, allocationSize = 1)
    protected Integer id;

    @Column(name = "block_display_name")
    protected String blockDisplayName;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    protected InternalBlockType type;

    @Column(name = "display_order")
    protected Integer displayOrder;

    @Column(name = "info_message")
    private String infoMessage;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "project_id")
    protected Project project;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Column(name = "detached_block_project_id")
    protected Integer detachedProjectId;

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBlockDisplayName() {
        return blockDisplayName;
    }

    public void setBlockDisplayName(String blockDisplayName) {
        this.blockDisplayName = blockDisplayName;
    }

    public InternalBlockType getType() {
        return type;
    }

    public void setType(InternalBlockType type) {
        this.type = type;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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

    public Integer getDetachedProjectId() {
        return detachedProjectId;
    }

    public void setDetachedProjectId(Integer detachedProjectId) {
        this.detachedProjectId = detachedProjectId;
    }

    /**
     * Initialise the new project block using the template block configuration.
     */
    public final void initFromTemplate(InternalTemplateBlock templateBlock) {
        this.setType(templateBlock.getType());
        this.setDisplayOrder(templateBlock.getDisplayOrder());
        this.setBlockDisplayName(templateBlock.getBlockDisplayName());
        this.setInfoMessage(templateBlock.getInfoMessage());
        this.initFromTemplateSpecific(templateBlock);
    }

    protected void initFromTemplateSpecific(InternalTemplateBlock templateBlock) {
        // subclasses should override as necessary
    }

    @Override
    public InternalProjectBlock clone() {
        InternalProjectBlock clone = type.newBlockInstance();
        clone.setType(this.getType());
        clone.setDisplayOrder(this.getDisplayOrder());
        clone.setBlockDisplayName(this.getBlockDisplayName());
        clone.setInfoMessage(this.getInfoMessage());
        clone.setDetachedProjectId(this.getDetachedProjectId());
        return clone;
    }

    /**
     * @return audit message if needs be or null if nothing is to be audited.
     */
    public String merge(InternalProjectBlock updated) {
        return null;
    }

    @Override
    public int compareTo(InternalProjectBlock other) {
        if (this == other) {
            return 0;
        }
        if (this.getDisplayOrder() != null) {
            return this.getDisplayOrder().compareTo(other.getDisplayOrder());
        }
        return 0;
    }

}
