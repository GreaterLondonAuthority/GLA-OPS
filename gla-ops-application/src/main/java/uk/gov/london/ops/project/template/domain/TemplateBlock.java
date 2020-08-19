/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.grant.GrantType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.listToCsString;

/**
 * Created by chris on 09/12/2016.
 */
@Entity(name = "template_block")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "block_type")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")

@JsonSubTypes({
        @JsonSubTypes.Type(value = ProjectDetailsTemplateBlock.class),
        @JsonSubTypes.Type(value = GrantSourceTemplateBlock.class),
        @JsonSubTypes.Type(value = IndicativeGrantTemplateBlock.class),
        @JsonSubTypes.Type(value = MilestonesTemplateBlock.class),
        @JsonSubTypes.Type(value = OutputsTemplateBlock.class),
        @JsonSubTypes.Type(value = QuestionsTemplateBlock.class),
        @JsonSubTypes.Type(value = ReceiptsTemplateBlock.class),
        @JsonSubTypes.Type(value = RisksTemplateBlock.class),
        @JsonSubTypes.Type(value = FundingTemplateBlock.class),
        @JsonSubTypes.Type(value = LearningGrantTemplateBlock.class),
        @JsonSubTypes.Type(value = OutputsCostsTemplateBlock.class),
        @JsonSubTypes.Type(value = DeliveryPartnersTemplateBlock.class),
        @JsonSubTypes.Type(value = FundingClaimsTemplateBlock.class),
        @JsonSubTypes.Type(value = ProjectObjectivesTemplateBlock.class),
        @JsonSubTypes.Type(value = OtherFundingTemplateBlock.class),
        @JsonSubTypes.Type(value = UserDefinedOutputTemplateBlock.class),
        @JsonSubTypes.Type(value = ProjectElementsTemplateBlock.class)
})

@DiscriminatorValue("BASE")
public class TemplateBlock implements Serializable, Comparable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_block_seq_gen")
    @SequenceGenerator(name = "template_block_seq_gen", sequenceName = "template_block_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "block_display_name")
    protected String blockDisplayName;

    @Column(name = "block")
    @Enumerated(EnumType.STRING)
    protected ProjectBlockType block;

    @Column(name = "display_order")
    protected Integer displayOrder;

    @Column(name = "block_appears_on_status")
    private String blockAppearsOnStatus;

    @Column(name = "info_message")
    private String infoMessage;

    @Column(name = "grant_types")
    private String paymentSourcesString;

    @JsonIgnore
    @Column(name = "block_data")
    protected String blockData;

    @Column(name = "detached_block_template_id")
    protected Integer detachedTemplateId;

    public TemplateBlock() {
    }

    public TemplateBlock(ProjectBlockType projectBlockType) {
        this.block = projectBlockType;
    }

    public TemplateBlock(String blockName) {
        this(ProjectBlockType.valueOf(blockName));
    }

    public TemplateBlock(Integer displayOrder, ProjectBlockType block) {
        this.block = block;
        this.displayOrder = displayOrder;
        this.blockDisplayName = block.getDefaultName();
    }

    public TemplateBlock(Integer displayOrder, ProjectBlockType block, String blockDisplayName) {
        this.blockDisplayName = blockDisplayName;
        this.block = block;
        this.displayOrder = displayOrder;
    }

    public boolean shouldSaveBlockData() {
        return false;
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

    public ProjectBlockType getBlock() {
        return block;
    }

    public void setBlock(ProjectBlockType block) {
        this.block = block;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getBlockAppearsOnStatus() {
        return blockAppearsOnStatus;
    }

    public void setBlockAppearsOnStatus(String blockAppearsOnStatus) {
        this.blockAppearsOnStatus = blockAppearsOnStatus;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public String getPaymentSourcesString() {
        return paymentSourcesString;
    }

    public void setPaymentSourcesString(String paymentSourcesString) {
        this.paymentSourcesString = paymentSourcesString;
    }

    public Set<String> getGrantTypes() {
        return this.getPaymentSources().stream().map(m -> getGrantType(m).name()).collect(Collectors.toSet());
    }

    public GrantType getGrantType(String paymentSource) {
        switch (paymentSource) {
            case "RCGF":
                return GrantType.RCGF;
            case "DPF":
                return GrantType.DPF;
            default:
                return GrantType.Grant;
        }
    }

    // these are really PaymentSources
    // left for template compatibility atm
    public void setGrantTypes(List<String> grantTypes) {
        if (paymentSourcesString == null) {
            this.paymentSourcesString = listToCsString(grantTypes);
        }
    }

    public Set<String> getPaymentSources() {
        if (paymentSourcesString == null || paymentSourcesString.length() == 0) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(paymentSourcesString.split(",")));
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

    /**
     * Returns the block's display name.
     *
     * If no display name is specified in the template, the default for the block type is used.
     */
    public String displayName() {
        if (getBlockDisplayName() == null || getBlockDisplayName().length() == 0) {
            return getBlock().getDefaultName();
        } else {
            return getBlockDisplayName();
        }
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) {
            return 0;
        }
        if (o instanceof TemplateBlock) {
            TemplateBlock other = (TemplateBlock) o;
            if (this.getDisplayOrder() != null) {
                return this.getDisplayOrder().compareTo(other.getDisplayOrder());
            }
        }
        return 0;
    }

    public TemplateBlock cloneTemplateBlock() {
        TemplateBlock clone;
        try {
            clone = this.getClass().newInstance();
        } catch (Exception e) {
            // should never happen
            throw new RuntimeException("Error creating instance of" + this.getClass().getName(), e);

        }
        clone.setBlock(this.getBlock());
        clone.setBlockAppearsOnStatus(this.getBlockAppearsOnStatus());
        clone.setBlockDisplayName(this.getBlockDisplayName());
        clone.setDisplayOrder(this.getDisplayOrder());
        clone.setInfoMessage(this.getInfoMessage());
        clone.setPaymentSourcesString(this.getPaymentSourcesString());
        clone.setDetachedTemplateId(this.getDetachedTemplateId());
        clone.setBlockData(this.getBlockData());
        updateCloneFromBlock(clone);
        return clone;
    }

    // subclasses override this method
    public void updateCloneFromBlock(TemplateBlock clone) {

    }
}
