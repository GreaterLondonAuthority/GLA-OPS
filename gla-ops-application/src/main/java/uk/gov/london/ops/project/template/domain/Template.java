/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.london.ops.contracts.ContractModel;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.internalblock.InternalBlockType;
import uk.gov.london.ops.project.state.StateModel;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * A project's template.
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Template implements Serializable {

    public enum MilestoneType {
        NonMonetary, MonetarySplit, MonetaryValue
    }

    public enum TemplateStatus {
        Draft, Active
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "template_seq_gen")
    @SequenceGenerator(name = "template_seq_gen", sequenceName = "template_seq", initialValue = 100, allocationSize = 1)
    private Integer id;

    @NotNull
    @Column(name = "name")
    private String name;

    @Column(name = "author")
    private String author;

    @Column(name = "template_status")
    @Enumerated(EnumType.STRING)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private TemplateStatus status = TemplateStatus.Draft;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = TemplateBlock.class)
    @JoinColumn(name = "template_id")
    private List<TemplateBlock> blocksEnabled = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = InternalTemplateBlock.class)
    @JoinColumn(name = "template_id")
    private List<InternalTemplateBlock> internalBlocks = new ArrayList<>();

    @Column(name = "clone_of_template")
    private Integer cloneOfTemplateId;

    @Column(name = "json")
    @JsonIgnore
    private String json;

    @Column(name = "clone_modified")
    private Boolean cloneModified = false;

    @Column(name = "strategic_template")
    private Boolean strategicTemplate = false;

    @Column(name = "associated_projects_enabled")
    private boolean associatedProjectsEnabled;

    @Column(name = "milestone_type")
    @Enumerated(EnumType.STRING)
    private MilestoneType milestoneType = MilestoneType.NonMonetary;

    @Column(name = "state_model")
    @Enumerated(EnumType.STRING)
    private StateModel stateModel;

    @Column(name = "milestone_description_hint_text")
    private String milestoneDescriptionHintText;

    /**
     * used to display the monetary split column heading (Grant payment %, Affordable Housing Grant %, etc.)
     */
    @Column(name = "monetary_split_title")
    private String monetarySplitTitle;

    @Column(name = "info_message")
    private String infoMessage;

    @Column(name = "warning_message")
    private String warningMessage;

    @Column(name = "max_projects_for_template")
    private Integer numberOfProjectAllowedPerOrg;

    @Embedded
    private DetailsTemplate detailsConfig;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = TemplateTenureType.class)
    @JoinColumn(name = "template_id")
    private Set<TemplateTenureType> tenureTypes;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "indicative_tenure_config_id")
    private IndicativeTenureConfiguration indicativeTenureConfiguration;

    @Column(name = "contract_id")
    private Integer contractId;

    @Transient
    private ContractModel contract;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "start_on_site_restriction_text")
    private String startOnSiteRestrictionText;

    @Column(name = "project_submission_reminder")
    private boolean projectSubmissionReminder;

    @Column
    private boolean isProgrammeAllocation = false;

    @Transient
    List<Programme> programmes;

    @Transient
    private Integer projectsCount = 0;

    public Template() {
        // Empty
    }

    public Template(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<TemplateBlock> getBlocksEnabled() {
        return blocksEnabled;
    }

    public void setBlocksEnabled(List<TemplateBlock> blocksEnabled) {
        this.blocksEnabled = blocksEnabled;
    }

    public List<InternalTemplateBlock> getInternalBlocks() {
        return internalBlocks;
    }

    public void setInternalBlocks(List<InternalTemplateBlock> internalBlocks) {
        this.internalBlocks = internalBlocks;
    }

    public DetailsTemplate getDetailsConfig() {
        return detailsConfig;
    }

    public void setDetailsConfig(DetailsTemplate detailsConfig) {
        this.detailsConfig = detailsConfig;
    }

    public Integer getCloneOfTemplateId() {
        return cloneOfTemplateId;
    }

    public void setCloneOfTemplateId(Integer cloneOfTemplateId) {
        this.cloneOfTemplateId = cloneOfTemplateId;
    }

    public Boolean isCloneModified() {
        return cloneModified;
    }

    public void setCloneModified(Boolean cloneModified) {
        this.cloneModified = cloneModified;
    }

    public void removeAllBlocksByType(ProjectBlockType blockType) {
        for (Iterator<TemplateBlock> iterator = blocksEnabled.iterator(); iterator.hasNext(); ) {
            TemplateBlock next = iterator.next();
            if (next.block.equals(blockType)) {
                iterator.remove();
            }
        }
    }

    public void addNextBlock(ProjectBlockType blockType) {
        TemplateBlock block = blockType.newTemplateBlockInstance();
        addNextBlock(block);
    }

    public void addNextBlock(TemplateBlock block) {
        int currentMax = 0;
        for (TemplateBlock templateBlock : blocksEnabled) {
            currentMax = Math.max(currentMax, templateBlock.getDisplayOrder());
        }

        blocksEnabled.add(block);
        block.setDisplayOrder(currentMax + 1);
    }

    public void addBlockWithDisplayOrder(TemplateBlock block) {
        if (block.getDisplayOrder() != null) {
            blocksEnabled.add(block);
            block.setDisplayOrder(block.getDisplayOrder());
        } else {
            addNextBlock(block);
        }
    }

    public boolean isBlockPresent(ProjectBlockType blockType) {
        return getBlocksByType(blockType).size() > 0;
    }

    public TemplateBlock getSingleBlockByType(ProjectBlockType blockType) {
        Set<TemplateBlock> blocksByType = this.getBlocksByType(blockType);
        if (blocksByType == null || blocksByType.isEmpty()) {
            return null;
        } else if (blocksByType.size() == 1) {
            return blocksByType.iterator().next();
        } else {
            throw new ValidationException(
                    String.format("Unable to return single block of type: %s for template: %s", blockType.name(),
                            this.getName()));
        }
    }

    public TemplateBlock getSingleBlockByTypeAndDisplayOrder(ProjectBlockType blockType, int blockDisplayOrder) {
        return blocksEnabled.stream().filter(b -> b.getBlock().equals(blockType) && b.getDisplayOrder().equals(blockDisplayOrder))
                .findFirst().orElse(null);
    }

    public Set<TemplateBlock> getBlocksByType(ProjectBlockType blockType) {
        Set<TemplateBlock> blocks = new HashSet<>();
        for (TemplateBlock templateBlock : blocksEnabled) {
            if (templateBlock.block.equals(blockType)) {
                blocks.add(templateBlock);
            }
        }
        return blocks;
    }

    public <T> List<T> getBlocksByType(Class<T> type) {
        List<T> blocks = new ArrayList<>();
        blocks.addAll(blocksEnabled.stream().filter(b -> type.isInstance(b)).map(b -> (T) b).collect(toList()));
        blocks.addAll(internalBlocks.stream().filter(b -> type.isInstance(b)).map(b -> (T) b).collect(toList()));
        return blocks;
    }

    public TemplateBlock getBlockById(Integer id) {
        return blocksEnabled.stream().filter(b -> b.getId().equals(id)).findFirst().orElse(null);
    }

    public InternalTemplateBlock getInternalBlockById(Integer id) {
        return internalBlocks.stream().filter(b -> b.getId().equals(id)).findFirst().orElse(null);
    }

    public Set<TemplateTenureType> getTenureTypes() {
        return tenureTypes;
    }

    public void setTenureTypes(Set<TemplateTenureType> tenureTypes) {
        this.tenureTypes = tenureTypes;
    }

    public Boolean getAllowMonetaryMilestones() {
        return MilestoneType.MonetarySplit.equals(milestoneType) || MilestoneType.MonetaryValue.equals(milestoneType);
    }

    public void setAllowMonetaryMilestones(Boolean allowMonetaryMilestones) {
        if (!getAllowMonetaryMilestones() && allowMonetaryMilestones) {
            this.milestoneType = MilestoneType.MonetarySplit;
        }
    }

    public MilestoneType getMilestoneType() {
        return milestoneType;
    }

    public void setMilestoneType(MilestoneType milestoneType) {
        this.milestoneType = milestoneType;
    }

    public String getMilestoneDescriptionHintText() {
        return milestoneDescriptionHintText;
    }

    public void setMilestoneDescriptionHintText(String milestoneDescriptionHintText) {
        this.milestoneDescriptionHintText = milestoneDescriptionHintText;
    }

    public String getMonetarySplitTitle() {
        return monetarySplitTitle;
    }

    public void setMonetarySplitTitle(String monetarySplitTitle) {
        this.monetarySplitTitle = monetarySplitTitle;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public IndicativeTenureConfiguration getIndicativeTenureConfiguration() {
        return indicativeTenureConfiguration;
    }

    public void setIndicativeTenureConfiguration(IndicativeTenureConfiguration indicativeTenureConfiguration) {
        this.indicativeTenureConfiguration = indicativeTenureConfiguration;
    }

    public StateModel getStateModel() {
        return stateModel;
    }

    public void setStateModel(StateModel stateModel) {
        this.stateModel = stateModel;
    }

    @JsonIgnore
    public Set<TenureYear> getTenureYears() {
        TemplateBlock templateBlock = getSingleBlockByType(ProjectBlockType.IndicativeGrant);
        if (templateBlock != null && templateBlock instanceof IndicativeGrantTemplateBlock) {
            IndicativeGrantTemplateBlock indicativeGrantTemplateBlock = (IndicativeGrantTemplateBlock) templateBlock;
            if (CollectionUtils.isNotEmpty(indicativeGrantTemplateBlock.getTenureYears())) {
                return indicativeGrantTemplateBlock.getTenureYears();
            }
        }

        return buildIndicativeTenureYears();
    }

    public ContractModel getContract() {
        return contract;
    }

    public void setContract(ContractModel contractModel) {
        this.contract = contractModel;
    }

    public Integer getContractId() {
        return contractId;
    }

    public void setContractId(Integer contractId) {
        this.contractId = contractId;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getStartOnSiteRestrictionText() {
        return startOnSiteRestrictionText;
    }

    public void setStartOnSiteRestrictionText(String startOnSiteRestrictionText) {
        this.startOnSiteRestrictionText = startOnSiteRestrictionText;
    }

    public boolean isProjectSubmissionReminder() {
        return projectSubmissionReminder;
    }

    public void setProjectSubmissionReminder(boolean projectSubmissionReminder) {
        this.projectSubmissionReminder = projectSubmissionReminder;
    }

    public TemplateStatus getStatus() {
        return status;
    }

    public void setStatus(TemplateStatus status) {
        this.status = status;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public Integer getNumberOfProjectAllowedPerOrg() {
        return numberOfProjectAllowedPerOrg;
    }

    public void setNumberOfProjectAllowedPerOrg(Integer numberOfProjectAllowedPerOrg) {
        this.numberOfProjectAllowedPerOrg = numberOfProjectAllowedPerOrg;
    }

    public Boolean isStrategicTemplate() {
        return strategicTemplate;
    }

    public void setStrategicTemplate(Boolean strategicTemplate) {
        this.strategicTemplate = strategicTemplate;
    }

    public boolean isAssociatedProjectsEnabled() {
        return associatedProjectsEnabled;
    }

    public void setAssociatedProjectsEnabled(boolean associatedProjectsEnabled) {
        this.associatedProjectsEnabled = associatedProjectsEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Template template = (Template) o;

        return !(id != null ? !id.equals(template.id) : template.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Template cloneTemplate(String newName) {
        return cloneIntoTemplate(new Template(), newName);
    }

    public Template cloneIntoTemplate(Template clone, String newName) {
        clone.setName(newName);
        clone.setStateModel(this.getStateModel());
        clone.setMilestoneType(this.getMilestoneType());
        clone.setCloneOfTemplateId(this.getId());
        clone.setMonetarySplitTitle(this.getMonetarySplitTitle());
        clone.setWarningMessage(this.getWarningMessage());
        clone.setNumberOfProjectAllowedPerOrg(this.getNumberOfProjectAllowedPerOrg());

        if (clone.getTenureTypes() == null) {
            clone.setTenureTypes(new HashSet<>());
        } else {
            clone.getTenureTypes().clear();
        }

        if (clone.getBlocksEnabled() == null) {
            clone.setBlocksEnabled(new ArrayList<>());
        } else {
            clone.getBlocksEnabled().clear();
        }

        if (clone.getInternalBlocks() == null) {
            clone.setInternalBlocks(new ArrayList<>());
        } else {
            clone.getInternalBlocks().clear();
        }
        clone.setStrategicTemplate(this.isStrategicTemplate());
        clone.setAssociatedProjectsEnabled(this.isAssociatedProjectsEnabled());
        clone.setProjectSubmissionReminder(this.isProjectSubmissionReminder());
        clone.setContract(this.getContract());
        clone.setContractId(this.getContractId());

        if (this.getTenureTypes() != null) {
            for (TemplateTenureType templateTenureType : this.getTenureTypes()) {
                TemplateTenureType clonedTenure = new TemplateTenureType(templateTenureType.getTenureType());
                clonedTenure.setExternalId(templateTenureType.getExternalId());
                clonedTenure.setDisplayOrder(templateTenureType.getDisplayOrder());
                clonedTenure.setName(templateTenureType.getName());
                clonedTenure.setTariffRate(templateTenureType.getTariffRate());
                clone.getTenureTypes().add(clonedTenure);
            }
        }

        DetailsTemplate clonedDetails = clone.getDetailsConfig() == null ? new DetailsTemplate() : clone.getDetailsConfig();
        DetailsTemplate sourceConfig = this.getDetailsConfig();
        clonedDetails.setAddressRequirement(sourceConfig.getAddressRequirement());
        clonedDetails.setBoroughRequirement(sourceConfig.getBoroughRequirement());
        clonedDetails.setCoordsRequirement(sourceConfig.getCoordsRequirement());
        clonedDetails.setInterestRequirement(sourceConfig.getInterestRequirement());
        clonedDetails.setLegacyProjectCodeRequirement(sourceConfig.getLegacyProjectCodeRequirement());
        clonedDetails.setMaincontactemailRequirement(sourceConfig.getMaincontactemailRequirement());
        clonedDetails.setSecondaryContactEmailRequirement(sourceConfig.getSecondaryContactEmailRequirement());
        clonedDetails.setWardIdRequirement(sourceConfig.getWardIdRequirement());
        clonedDetails.setMaincontactRequirement(sourceConfig.getMaincontactRequirement());
        clonedDetails.setSecondaryContactRequirement(sourceConfig.getSecondaryContactRequirement());
        clonedDetails.setPostcodeRequirement(sourceConfig.getPostcodeRequirement());
        clonedDetails.setProjectManagerRequirement(sourceConfig.getProjectManagerRequirement());
        clonedDetails.setSiteOwnerRequirement(sourceConfig.getSiteOwnerRequirement());
        clonedDetails.setSiteStatusRequirement(sourceConfig.getSiteStatusRequirement());
        clonedDetails.setDescriptionRequirement(sourceConfig.getDescriptionRequirement());
        clonedDetails.setPlanningPermissionReferenceRequirement(sourceConfig.getPlanningPermissionReferenceRequirement());

        clone.setDetailsConfig(clonedDetails);
        for (TemplateBlock templateBlock : this.getBlocksEnabled()) {
            TemplateBlock clonedBlock = templateBlock.cloneTemplateBlock();
            clone.getBlocksEnabled().add(clonedBlock);
        }

        for (InternalTemplateBlock internalBlock : this.getInternalBlocks()) {
            InternalTemplateBlock clonedInternalBlock = internalBlock.clone();
            clone.getInternalBlocks().add(clonedInternalBlock);
        }

        return clone;
    }

    public Set<String> getGrantTypes() {
        Set<String> grantTypes = new HashSet<>();

        for (TemplateBlock templateBlock : this.getBlocksEnabled()) {
            grantTypes.addAll(templateBlock.getGrantTypes());
        }

        return grantTypes;
    }

    public InternalTemplateBlock getInternalBlockByType(InternalBlockType type) {
        return internalBlocks.stream().filter(b -> b.getType().equals(type)).findFirst().orElse(null);
    }

    public InternalTemplateBlock getInternalBlockByTypeAndDisplayOrder(InternalBlockType type, Integer blockDisplayOrder) {
        return internalBlocks.stream()
                .filter(b -> b.getType().equals(type) && b.getDisplayOrder().equals(blockDisplayOrder))
                .findFirst().orElse(null);
    }

    @JsonIgnore
    public InternalRiskTemplateBlock getInternalRiskBlock() {
        InternalRiskTemplateBlock block = (InternalRiskTemplateBlock) getInternalBlockByType(InternalBlockType.Risk);
        if (block == null) {
            throw new NotFoundException("could not find risk template block");
        }
        return block;
    }

    public boolean getShouldMilestonesBlockShowStatus() {
        Set<TemplateBlock> blocksByType = this.getBlocksByType(ProjectBlockType.Milestones);
        if (blocksByType != null && blocksByType.size() > 0) {
            MilestonesTemplateBlock next = (MilestonesTemplateBlock) blocksByType.iterator().next();
            if (next.getShowMilestoneStatus() != null) {
                return next.getShowMilestoneStatus();
            } else if (next.getAutoCalculateMilestoneState() != null) {
                return !next.getAutoCalculateMilestoneState();
            }
        }
        return false;
    }

    @JsonIgnore
    public Set<TenureYear> buildIndicativeTenureYears() {
        Set<TenureYear> tenureYears = new HashSet<>();
        if (this.tenureTypes != null) {
            for (TemplateTenureType tenureType : this.tenureTypes) {
                Integer endYear =
                        this.indicativeTenureConfiguration.getIndicativeTenureStartYear() + this.indicativeTenureConfiguration
                                .getIndicativeTenureNumberOfYears() - 1;
                for (Integer year = this.indicativeTenureConfiguration.getIndicativeTenureStartYear(); year <= endYear; year++) {
                    tenureYears.add(new TenureYear(year, tenureType.getExternalId(), tenureType.getTariffRate()));
                }
            }
        }
        return tenureYears;
    }

    public List<Programme> getProgrammes() {
        return programmes;
    }

    public void setProgrammes(List<Programme> programmes) {
        this.programmes = programmes;
    }

    public TemplateBlock getSingleBlockByDisplayOrder(Integer displayOrderId) {
        return blocksEnabled.stream().filter(b -> b.getDisplayOrder().equals(displayOrderId)).findFirst().orElse(null);
    }

    public InternalTemplateBlock getSingleInternalBlockByDisplayOrder(Integer displayOrderId) {
        return internalBlocks.stream().filter(b -> b.getDisplayOrder().equals(displayOrderId)).findFirst().orElse(null);
    }

    public Integer getProjectsCount() {
        return projectsCount;
    }

    public void setProjectsCount(Integer projectsCount) {
        this.projectsCount = projectsCount;
    }

    public boolean isProgrammeAllocation() {
        return isProgrammeAllocation;
    }

    public void setProgrammeAllocation(boolean programmeAllocation) {
        isProgrammeAllocation = programmeAllocation;
    }
}
