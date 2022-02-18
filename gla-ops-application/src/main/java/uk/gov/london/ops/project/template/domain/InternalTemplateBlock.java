/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.gov.london.ops.project.internalblock.InternalBlockType;

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
import javax.persistence.SequenceGenerator;
import java.io.Serializable;
import java.util.List;

@Entity(name = "internal_template_block")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "block_type")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "json_type")

@JsonSubTypes({
        @JsonSubTypes.Type(value = InternalAssessmentTemplateBlock.class),
        @JsonSubTypes.Type(value = InternalRiskTemplateBlock.class),
        @JsonSubTypes.Type(value = InternalQuestionsTemplateBlock.class),
        @JsonSubTypes.Type(value = InternalProjectAdminTemplateBlock.class)
})

@DiscriminatorValue("BASE")
public class InternalTemplateBlock implements Serializable, Commandable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "internal_template_block_seq_gen")
    @SequenceGenerator(name = "internal_template_block_seq_gen", sequenceName = "internal_template_block_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

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
    @Column(name = "block_data")
    protected String blockData;

    @Column(name = "detached_block_template_id")
    protected Integer detachedTemplateId;

    public InternalTemplateBlock() {
    }

    public InternalTemplateBlock(InternalBlockType type) {
        this.type = type;
    }

    public InternalTemplateBlock(InternalBlockType type, Integer displayOrder) {
        this.type = type;
        this.displayOrder = displayOrder;
    }

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

    public String getBlockData() {
        return blockData;
    }

    public void setBlockData(String blockData) {
        this.blockData = blockData;
    }

    public Integer getDetachedTemplateId() {
        return detachedTemplateId;
    }

    public void setDetachedTemplateId(Integer detachedTemplateId) {
        this.detachedTemplateId = detachedTemplateId;
    }

    public InternalTemplateBlock clone() {
        InternalTemplateBlock clone;
        try {
            clone = this.getClass().newInstance();
        } catch (Exception e) {
            // should never happen
            throw new RuntimeException("Error creating instance of" + this.getClass().getName(), e);

        }
        clone.setType(this.getType());
        clone.setBlockDisplayName(this.getBlockDisplayName());
        clone.setDisplayOrder(this.getDisplayOrder());
        clone.setBlockData(this.getBlockData());
        clone.setDetachedTemplateId(this.getDetachedTemplateId());
        return clone;
    }


    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public List<TemplateBlockCommand> getTemplateBlockCommands() {
        return TemplateBlockCommand.GLOBAL_COMMANDS;
    }

    @Override
    public void performCommand(@NotNull TemplateBlockCommand command, @Nullable CommandPayload payload) {
    }
}
