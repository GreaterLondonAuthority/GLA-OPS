/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.EventType;
import uk.gov.london.ops.OpsEvent;
import uk.gov.london.ops.domain.template.TemplateBlock;
import uk.gov.london.ops.domain.template.TemplateTenureType;
import uk.gov.london.ops.spe.SimpleProjectExportConfig;
import uk.gov.london.ops.spe.SimpleProjectExportConstants;
import uk.gov.london.ops.spe.SimpleProjectExportUtils;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final Set<Integer> START_ON_SITE_EXTERNAL_IDS = Stream.of(Milestone.START_ON_SITE_ID).collect(Collectors.toSet());
    private static final Set<Integer> COMPLETION_MILESTONE_EXTERNAL_IDS = Stream.of(Milestone.COMPLETION_ID).collect(Collectors.toSet());

    public static final String TOTAL_UNITS = "total_units_";
    public static final String NIL_GRANT_UNITS = "nil_grant_units_";
    public static final String S106_UNITS = "s106_units_";
    public static final String DEV_COST = "dev_cost_";
    public static final String GRANT_PER_UNIT = "gpu_";
    public static final String TOTAL_GRANT_ELIGIBILITY = "total_grant";
    public static final String TENURE_TYPE_PROPERTY_PREFIX = "tenuretype.";

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "block_id")
    private Set<TenureTypeAndUnits> tenureTypeAndUnitsEntries;

    @Column(name = "sos_milestone_authorised")
    protected OffsetDateTime startOnSiteMilestoneAuthorised;

    @Column(name = "completion_milestone_authorised")
    protected OffsetDateTime completionMilestoneAuthorised;


    public abstract Long getTotalGrantEligibility();

    public Set<TenureTypeAndUnits> getTenureTypeAndUnitsEntries() {
        return tenureTypeAndUnitsEntries;
    }



    @JsonIgnore
    @Transient
    public List<TenureTypeAndUnits> getTenureTypeAndUnitsEntriesSorted() {
        List<TenureTypeAndUnits> list = new ArrayList<>(getTenureTypeAndUnitsEntries());

        list.sort(Comparator.comparing(o -> o.getTenureType().getDisplayOrder()));
        return list;
    }

    public void setTenureTypeAndUnitsEntries(Set<TenureTypeAndUnits> tenureTypeAndUnitsEntries) {
        this.tenureTypeAndUnitsEntries = tenureTypeAndUnitsEntries;
    }


    @Override
    public Map<String, Object> simpleDataExtract(SimpleProjectExportConfig simpleProjectExportConfig) {
        final Map<String, Object> map = new HashMap<>();
        final SimpleProjectExportConstants.ReportPrefix prefix =
                SimpleProjectExportConstants.ReportPrefix.eg_;
        for (TenureTypeAndUnits tenureTypeAndUnits : getTenureTypeAndUnitsEntriesSorted()) {
            String keySuffix = simpleProjectExportConfig.getReplacementProperty(TENURE_TYPE_PROPERTY_PREFIX + tenureTypeAndUnits.getTenureType().getExternalId());
            map.put(SimpleProjectExportUtils.formatForExport(prefix + TOTAL_UNITS + keySuffix), this.calculateTotalUnits(tenureTypeAndUnits));
            map.put(SimpleProjectExportUtils.formatForExport(prefix + NIL_GRANT_UNITS + keySuffix), this.calculateNilGrantUnits(tenureTypeAndUnits));
            map.put(SimpleProjectExportUtils.formatForExport(prefix + S106_UNITS + keySuffix), this.calculateS106Units(tenureTypeAndUnits));
            map.put(SimpleProjectExportUtils.formatForExport(prefix + DEV_COST + keySuffix), this.calculateDevCosts(tenureTypeAndUnits));
            map.put(SimpleProjectExportUtils.formatForExport(prefix + GRANT_PER_UNIT + keySuffix), this.calculateGrantPerUnitCost(tenureTypeAndUnits));
        }
        map.put(prefix + TOTAL_GRANT_ELIGIBILITY, this.getTotalGrantEligibility());

        return map;
    }

    @Transient
    @JsonIgnore
    public Integer calculateTotalUnits(TenureTypeAndUnits tenureType) {
        return null;
    }

    @Transient
    @JsonIgnore
    public Integer calculateNilGrantUnits(TenureTypeAndUnits tenureType) {
        return null;
    }

    @Transient
    @JsonIgnore
    public Integer calculateS106Units(TenureTypeAndUnits tenureType) {
        return null;
    }

    @Transient
    @JsonIgnore
    public Long calculateDevCosts(TenureTypeAndUnits tenureType) {
        return null;
    }

    @Transient
    @JsonIgnore
    public Integer calculateGrantPerUnitCost(TenureTypeAndUnits tenureType) {
        return null;
    }

    @Transient
    @Override
    public boolean isComplete() {

        return isVisited() && getValidationFailures().size()==0;

    }

    @Override
    protected void generateValidationFailures() {
        if (StringUtils.isNotEmpty(project.getTemplate().getStartOnSiteRestrictionText())) {
            for (TenureTypeAndUnits entry: getTenureTypeAndUnitsEntries()) {
                if (entry.getTotalUnitsAtStartOnSite() != null && entry.getTotalUnitsAtStartOnSite() < this.calculateTotalUnits(entry)) {
                    String message = "You can't increase the total units above "+entry.getTotalUnitsAtStartOnSite()+" as the start on site milestone has been claimed. Submit a new project to increase units.";
                    addErrorMessage(String.valueOf(entry.getId()), "totalUnits", message);
                }
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
            for (TenureTypeAndUnits tenureTypeAndUnitsEntry : this.getTenureTypeAndUnitsEntries()) {
                tenureTypeAndUnitsEntry.setProject(project);
                calculateTotals(tenureTypeAndUnitsEntry);
            }
        }
    }

    /**
     * Calculate total eligible units, grant per unit and total grant eligable
     */
    public abstract void calculateTotals(TenureTypeAndUnits tenureInfo);

    private boolean hasTenureInfo(BaseGrantBlock newValue) {
        return newValue.getTenureTypeAndUnitsEntries() != null && !newValue.getTenureTypeAndUnitsEntries().isEmpty();
    }

    public int getTotalStartOnSiteApprovedUnits() {

        int response = 0;

        if (!(this instanceof IndicativeGrantBlock)) {
            if (this.getStartOnSiteMilestoneAuthorised() != null) {
                for (TenureTypeAndUnits tenureTypeAndUnitsEntry : tenureTypeAndUnitsEntries) {
                    if (tenureTypeAndUnitsEntry.getTotalUnitsAtStartOnSite() != null) {
                        response += tenureTypeAndUnitsEntry.getTotalUnitsAtStartOnSite();
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
                for (TenureTypeAndUnits tenureTypeAndUnitsEntry : tenureTypeAndUnitsEntries) {
                    if (tenureTypeAndUnitsEntry.getTotalUnitsAtCompletion() != null) {
                        response += tenureTypeAndUnitsEntry.getTotalUnitsAtCompletion();
                    }
                }
            }
        }
        return response;
    }

    @Transient
    public abstract List<TenureSummaryDetails> getTenureSummaryDetails();

    @Transient
    public abstract void initialiseFromTenureTypes(Set<TemplateTenureType> tenureTypes);

    protected abstract boolean isRowValid(TenureTypeAndUnits tenureTypeAndUnit);

    @Override
    protected void initFromTemplateSpecific(TemplateBlock templateBlock) {
        initialiseFromTenureTypes(getProject().getTemplate().getTenureTypes());
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        final BaseGrantBlock targetCalculateGrantBlock = (BaseGrantBlock)target;
        final Set<TenureTypeAndUnits> items = getTenureTypeAndUnitsEntries()
                .stream()
                .map(TenureTypeAndUnits::copy)
                .collect(Collectors.toSet());
        targetCalculateGrantBlock.setTenureTypeAndUnitsEntries(items);
        targetCalculateGrantBlock.setStartOnSiteMilestoneAuthorised(this.startOnSiteMilestoneAuthorised);
        targetCalculateGrantBlock.setCompletionMilestoneAuthorised(this.completionMilestoneAuthorised);
    }

    @Override
    public boolean allowMultipleVersions() {
        return true;
    }

    public void startOnSiteMilestoneApproved() {
        if (!(this instanceof IndicativeGrantBlock)) {
            for (TenureTypeAndUnits tenureTypeAndUnitsEntry : tenureTypeAndUnitsEntries) {
                Integer unitsAtStartOnSite = this.calculateTotalUnits(tenureTypeAndUnitsEntry);
                tenureTypeAndUnitsEntry.setTotalUnitsAtStartOnSite(unitsAtStartOnSite);
            }
        }
        this.setStartOnSiteMilestoneAuthorised(OffsetDateTime.now());
    }

    public void completionMilestoneApproved() {
        if (!(this instanceof IndicativeGrantBlock)) {
            for (TenureTypeAndUnits tenureTypeAndUnitsEntry : tenureTypeAndUnitsEntries) {
                Integer unitsAtStartOnSite = this.calculateTotalUnits(tenureTypeAndUnitsEntry);
                tenureTypeAndUnitsEntry.setTotalUnitsAtCompletion(unitsAtStartOnSite);
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
                if (START_ON_SITE_EXTERNAL_IDS.contains(opsEvent.getExternalId()) ) {
                    this.startOnSiteMilestoneApproved();
                }
                if (COMPLETION_MILESTONE_EXTERNAL_IDS.contains(opsEvent.getExternalId())) {
                    this.completionMilestoneApproved();
                }
            }
        }
    }

    public TenureTypeAndUnits getTenureTypeAndUnitsEntry(String tenureType) {
        for (TenureTypeAndUnits entry: getTenureTypeAndUnitsEntries()) {
            if (entry.getTenureType().getName().equals(tenureType)) {
                return entry;
            }
        }
        return null;
    }

}
