/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.grant;

import static uk.gov.london.common.GlaUtils.nullSafeAdd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.OpsEvent;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConfig;
import uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants;
import uk.gov.london.ops.project.implementation.spe.SimpleProjectExportUtils;
import uk.gov.london.ops.project.milestone.Milestone;
import uk.gov.london.ops.project.template.domain.TemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateTenureType;

@MappedSuperclass
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CalculateGrantBlock.class),
        @JsonSubTypes.Type(value = DeveloperLedGrantBlock.class),
        @JsonSubTypes.Type(value = IndicativeGrantBlock.class),
        @JsonSubTypes.Type(value = NegotiatedGrantBlock.class),
})
public abstract class BaseGrantBlock extends NamedProjectBlock {

    private static final Set<Integer> START_ON_SITE_EXTERNAL_IDS = Stream.of(Milestone.START_ON_SITE_ID)
            .collect(Collectors.toSet());
    private static final Set<Integer> COMPLETION_MILESTONE_EXTERNAL_IDS = Stream.of(Milestone.COMPLETION_ID)
            .collect(Collectors.toSet());

    public static final String TOTAL_UNITS = "total_units_";
    public static final String NIL_GRANT_UNITS = "nil_grant_units_";
    public static final String S106_UNITS = "s106_units_";
    public static final String DEV_COST = "dev_cost_";
    public static final String GRANT_PER_UNIT = "gpu_";
    public static final String TOTAL_GRANT_ELIGIBILITY = "total_grant";
    public static final String TENURE_TYPE_PROPERTY_PREFIX = "tenuretype.";

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "block_id")
    private Set<ProjectTenureDetails> tenureTypeAndUnitsEntries;

    @Column(name = "sos_milestone_authorised")
    protected OffsetDateTime startOnSiteMilestoneAuthorised;

    @Column(name = "completion_milestone_authorised")
    protected OffsetDateTime completionMilestoneAuthorised;

    public abstract Long getTotalGrantEligibility();

    public Set<ProjectTenureDetails> getTenureTypeAndUnitsEntries() {
        return tenureTypeAndUnitsEntries;
    }

    @JsonIgnore
    @Transient
    public List<ProjectTenureDetails> getTenureTypeAndUnitsEntriesSorted() {
        List<ProjectTenureDetails> list = new ArrayList<>(getTenureTypeAndUnitsEntries());

        list.sort(Comparator.comparing(o -> o.getTenureType().getDisplayOrder()));
        return list;
    }

    public void setTenureTypeAndUnitsEntries(Set<ProjectTenureDetails> projectTenureDetailsEntries) {
        this.tenureTypeAndUnitsEntries = projectTenureDetailsEntries;
    }


    @Override
    public Map<String, Object> simpleDataExtract(SimpleProjectExportConfig simpleProjectExportConfig) {
        final Map<String, Object> map = new HashMap<>();
        final SimpleProjectExportConstants.ReportPrefix prefix =
                SimpleProjectExportConstants.ReportPrefix.eg_;
        for (ProjectTenureDetails projectTenureDetails : getTenureTypeAndUnitsEntriesSorted()) {
            String keySuffix = simpleProjectExportConfig.getReplacementProperty(TENURE_TYPE_PROPERTY_PREFIX + projectTenureDetails
                    .getTenureType().getExternalId());
            map.put(SimpleProjectExportUtils.formatForExport(prefix + TOTAL_UNITS + keySuffix), this.calculateTotalUnits(
                    projectTenureDetails));
            map.put(SimpleProjectExportUtils.formatForExport(prefix + NIL_GRANT_UNITS + keySuffix), this.calculateNilGrantUnits(
                    projectTenureDetails));
            map.put(SimpleProjectExportUtils.formatForExport(prefix + S106_UNITS + keySuffix), this.calculateS106Units(
                    projectTenureDetails));
            map.put(SimpleProjectExportUtils.formatForExport(prefix + DEV_COST + keySuffix), this.calculateDevCosts(
                    projectTenureDetails));
            map.put(SimpleProjectExportUtils.formatForExport(prefix + GRANT_PER_UNIT + keySuffix), this.calculateGrantPerUnitCost(
                    projectTenureDetails));
        }
        map.put(prefix + TOTAL_GRANT_ELIGIBILITY, this.getTotalGrantEligibility());

        return map;
    }

    @Transient
    @JsonIgnore
    public Integer calculateTotalUnits(ProjectTenureDetails tenureType) {
        return null;
    }

    @Transient
    @JsonIgnore
    public Integer calculateNilGrantUnits(ProjectTenureDetails tenureType) {
        return null;
    }

    @Transient
    @JsonIgnore
    public Integer calculateS106Units(ProjectTenureDetails tenureType) {
        return null;
    }

    @Transient
    @JsonIgnore
    public Long calculateDevCosts(ProjectTenureDetails tenureType) {
        return null;
    }

    @Transient
    @JsonIgnore
    public Integer calculateGrantPerUnitCost(ProjectTenureDetails tenureType) {
        return null;
    }

    @Transient
    @Override
    public boolean isComplete() {
        return isVisited() && getValidationFailures().size() == 0;

    }

    @Override
    protected void generateValidationFailures() {
        if (StringUtils.isNotEmpty(project.getTemplate().getStartOnSiteRestrictionText())) {
            Integer totalUnitsAtStartOnSite = null;
            Integer totalUnits = null;

            for (ProjectTenureDetails entry : getTenureTypeAndUnitsEntries()) {
                if (entry.getTotalUnitsAtStartOnSite() != null) {
                    totalUnitsAtStartOnSite = nullSafeAdd(totalUnitsAtStartOnSite, entry.getTotalUnitsAtStartOnSite());
                }

                Integer calculatedTotalEntries = calculateTotalUnits(entry);
                if (calculatedTotalEntries != null) {
                    totalUnits = nullSafeAdd(totalUnits, calculatedTotalEntries);
                }
            }

            if (totalUnitsAtStartOnSite != null && totalUnits != null && totalUnitsAtStartOnSite < totalUnits) {
                String message = "You can't increase the total units above " + totalUnitsAtStartOnSite
                        + " as the start on site milestone has been claimed. Submit a new project to increase units.";
                addErrorMessage("Block1", "totalUnits", message);
            }
        }
    }

    public OffsetDateTime getStartOnSiteMilestoneAuthorised() {
        return startOnSiteMilestoneAuthorised;
    }

    public void setStartOnSiteMilestoneAuthorised(OffsetDateTime startOnSiteMilestoneAuthorised) {
        this.startOnSiteMilestoneAuthorised = startOnSiteMilestoneAuthorised;
    }

    public OffsetDateTime getCompletionMilestoneAuthorised() {
        return completionMilestoneAuthorised;
    }

    public void setCompletionMilestoneAuthorised(OffsetDateTime completionMilestoneAuthorised) {
        this.completionMilestoneAuthorised = completionMilestoneAuthorised;
    }

    @Transient
    public void merge(BaseGrantBlock newValue) {
        this.getTenureTypeAndUnitsEntries().clear();
        if (hasTenureInfo(newValue)) {
            this.getTenureTypeAndUnitsEntries().addAll(newValue.getTenureTypeAndUnitsEntries());
            for (ProjectTenureDetails projectTenureDetailsEntry : this.getTenureTypeAndUnitsEntries()) {
                projectTenureDetailsEntry.setProject(project);
                calculateTotals(projectTenureDetailsEntry);
            }
        }
    }

    /**
     * Calculate total eligible units, grant per unit and total grant eligable
     */
    public abstract void calculateTotals(ProjectTenureDetails tenureInfo);

    private boolean hasTenureInfo(BaseGrantBlock newValue) {
        return newValue.getTenureTypeAndUnitsEntries() != null && !newValue.getTenureTypeAndUnitsEntries().isEmpty();
    }

    public int getTotalStartOnSiteApprovedUnits() {

        int response = 0;

        if (!(this instanceof IndicativeGrantBlock)) {
            if (this.getStartOnSiteMilestoneAuthorised() != null) {
                for (ProjectTenureDetails projectTenureDetailsEntry : tenureTypeAndUnitsEntries) {
                    if (projectTenureDetailsEntry.getTotalUnitsAtStartOnSite() != null) {
                        response += projectTenureDetailsEntry.getTotalUnitsAtStartOnSite();
                    }
                }
            }
        }
        return response;
    }

    public int getTotalCompletionApprovedUnits() {

        int response = 0;

        if (!(this instanceof IndicativeGrantBlock)) {
            if (this.getCompletionMilestoneAuthorised() != null) {
                for (ProjectTenureDetails projectTenureDetailsEntry : tenureTypeAndUnitsEntries) {
                    if (projectTenureDetailsEntry.getTotalUnitsAtCompletion() != null) {
                        response += projectTenureDetailsEntry.getTotalUnitsAtCompletion();
                    }
                }
            }
        }
        return response;
    }

    @Transient
    public abstract List<TenureSummaryDetails> getTenureSummaryDetails();

    protected abstract boolean isRowValid(ProjectTenureDetails tenureTypeAndUnit);

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        final BaseGrantBlock targetCalculateGrantBlock = (BaseGrantBlock) target;
        final Set<ProjectTenureDetails> items = getTenureTypeAndUnitsEntries()
                .stream()
                .map(ProjectTenureDetails::copy)
                .collect(Collectors.toSet());
        targetCalculateGrantBlock.setTenureTypeAndUnitsEntries(items);
        targetCalculateGrantBlock.setStartOnSiteMilestoneAuthorised(this.startOnSiteMilestoneAuthorised);
        targetCalculateGrantBlock.setCompletionMilestoneAuthorised(this.completionMilestoneAuthorised);
    }

    public void startOnSiteMilestoneApproved() {
        if (!(this instanceof IndicativeGrantBlock)) {
            for (ProjectTenureDetails projectTenureDetailsEntry : tenureTypeAndUnitsEntries) {
                Integer unitsAtStartOnSite = this.calculateTotalUnits(projectTenureDetailsEntry);
                projectTenureDetailsEntry.setTotalUnitsAtStartOnSite(unitsAtStartOnSite);
            }
        }
        this.setStartOnSiteMilestoneAuthorised(OffsetDateTime.now());
    }

    public void completionMilestoneApproved() {
        if (!(this instanceof IndicativeGrantBlock)) {
            for (ProjectTenureDetails projectTenureDetailsEntry : tenureTypeAndUnitsEntries) {
                Integer unitsAtStartOnSite = this.calculateTotalUnits(projectTenureDetailsEntry);
                projectTenureDetailsEntry.setTotalUnitsAtCompletion(unitsAtStartOnSite);
            }
        }
        // todo take this from the event
        this.setCompletionMilestoneAuthorised(OffsetDateTime.now());
    }

    @Override
    public void handleEvent(OpsEvent opsEvent) {
        super.handleEvent(opsEvent);
        if (EventType.MilestoneApproval.equals(opsEvent.getEventType())) {
            if (!(this instanceof IndicativeGrantBlock)) {
                if (START_ON_SITE_EXTERNAL_IDS.contains(opsEvent.getExternalId())) {
                    this.startOnSiteMilestoneApproved();
                }
                if (COMPLETION_MILESTONE_EXTERNAL_IDS.contains(opsEvent.getExternalId())) {
                    this.completionMilestoneApproved();
                }
            }
        }
    }

    public ProjectTenureDetails getTenureTypeAndUnitsEntry(String tenureType) {
        for (ProjectTenureDetails entry : getTenureTypeAndUnitsEntries()) {
            if (entry.getTenureType().getName().equals(tenureType)) {
                return entry;
            }
        }
        return null;
    }

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        Set<TemplateTenureType> tenureTypes = getProject().getTemplate().getTenureTypes();
        HashSet<ProjectTenureDetails> projectTenureDetailsEntries = new HashSet<>();
        this.setTenureTypeAndUnitsEntries(projectTenureDetailsEntries);
        if (tenureTypes != null) {
            for (TemplateTenureType tenureType : tenureTypes) {
                ProjectTenureDetails tenureEntry = new ProjectTenureDetails(project);
                projectTenureDetailsEntries.add(tenureEntry);
                tenureEntry.setTenureType(tenureType);
            }
        }
    }

    @Override
    public boolean isSelfContained() {
        return false;
    }

}
