/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.london.ops.domain.template.TemplateTenureType;
import uk.gov.london.ops.util.GlaOpsUtils;
import uk.gov.london.ops.util.jpajoins.Join;
import uk.gov.london.ops.util.jpajoins.JoinData;

import javax.persistence.*;
import javax.validation.constraints.Max;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.ops.util.GlaOpsUtils.nullSafeAdd;

/**
 * Created by chris on 25/05/2017.
 */
@Entity(name = "unit_details_block")
@DiscriminatorValue("UNIT_DETAILS")
@JoinData(sourceTable = "unit_details_block", sourceColumn = "id", targetTable = "project_block", targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the unit details block is a subclass of the project block and shares a common key")
public class UnitDetailsBlock extends NamedProjectBlock {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = UnitDetailsTableEntry.class)
    @JoinColumn(name = "block_id")
    private Set<UnitDetailsTableEntry> tableEntries = new HashSet<>();

    @Max(999999999)
    @Column(name = "new_build_units")
    private Integer newBuildUnits;

    @Max(999999999)
    @Column(name = "refurbished_units")
    private Integer refurbishedUnits;

    @Max(999999999)
    @Column(name = "type_1_units")
    private Integer type1Units;

    @Max(999999999)
    @Column(name = "type_2_units")
    private Integer type2Units;

    @Max(999999999)
    @Column(name = "type_3_units")
    private Integer type3Units;

    @Max(999999999)
    @Column(name = "type_4_units")
    private Integer type4Units;

    @Max(999999999)
    @Column(name = "type_5_units")
    private Integer type5Units;

    @Max(999999999)
    @Column(name = "type_6_units")
    private Integer type6Units;

    @Max(999999999)
    @Column(name = "type_7_units")
    private Integer type7Units;

    @Max(999999999)
    @Column(name = "type_8_units")
    private Integer type8Units;

    @Max(999999999)
    @Column(name = "nb_wheelchair_units")
    private Integer nbWheelchairUnits;

    @Max(999999999)
    @Column(name = "gross_internal_area")
    private Integer grossInternalArea;


    /**
     * Used to cache the calculation retrieved from project and avoid doing the same thing twice.
     */
    @Transient
    @JsonIgnore
    Map<Integer, Integer> totalUnitsByExternalId;

    public UnitDetailsBlock() {
    }

    public UnitDetailsBlock(Project project) {
        super(project);
    }

    @Override
    public boolean isComplete() {
        return isVisited()
                && profiledUnitsValid()
                && buildTypeValid()
                && unitsByNumberOfPeopleValid()
                && nbWheelChairUnitsValid()
                && grossInternalAreaValid();
    }

    @Override
    protected void generateValidationFailures() {
        if (!profiledUnitsValid()) {
            addErrorMessage("ProfiledUnits", "", "Unit details profiled must match the total units against that tenure");
        }

        if (!buildTypeValid()) {
            addErrorMessage("BuildType", "", "The total must equal the number of units on the project");
        }

        if (!unitsByNumberOfPeopleValid()) {
            addErrorMessage("UnitsByNumberOfPeople", "", "The total must equal the number of units on the project");
        }

        if (!nbWheelChairUnitsValid()) {
            addErrorMessage("WheelChairUnits", "", "This value must be between 0 and the number of units on the project");
        }

        if (!grossInternalAreaValid()) {
            addErrorMessage("GrossInternalArea", "", "This value must be provided");
        }
    }

    private boolean profiledUnitsValid() {
        return getTotalUnits().equals(getProfiledUnits());
    }

    private boolean buildTypeValid() {
        return getTotalUnits().equals(nullSafeAdd(newBuildUnits, refurbishedUnits));
    }

    private boolean unitsByNumberOfPeopleValid() {
        return getTotalUnits().equals(nullSafeAdd(type1Units, type2Units, type3Units, type4Units, type5Units, type6Units, type7Units, type8Units));
    }

    private boolean nbWheelChairUnitsValid() {
        return nbWheelchairUnits != null && nbWheelchairUnits <= getTotalUnits();
    }

    private boolean grossInternalAreaValid() {
        return grossInternalArea != null;
    }

    public Set<UnitDetailsTableEntry> getTableEntries() {
        return tableEntries;
    }

    public void setTableEntries(Set<UnitDetailsTableEntry> tableEntries) {
        this.tableEntries = tableEntries;
    }

    public Integer getNewBuildUnits() {
        return newBuildUnits;
    }

    public void setNewBuildUnits(Integer newBuildUnits) {
        this.newBuildUnits = newBuildUnits;
    }

    public Integer getRefurbishedUnits() {
        return refurbishedUnits;
    }

    public void setRefurbishedUnits(Integer refurbishedUnits) {
        this.refurbishedUnits = refurbishedUnits;
    }

    public Integer getType1Units() {
        return type1Units;
    }

    public void setType1Units(Integer type1Units) {
        this.type1Units = type1Units;
    }

    public Integer getType2Units() {
        return type2Units;
    }

    public void setType2Units(Integer type2Units) {
        this.type2Units = type2Units;
    }

    public Integer getType3Units() {
        return type3Units;
    }

    public void setType3Units(Integer type3Units) {
        this.type3Units = type3Units;
    }

    public Integer getType4Units() {
        return type4Units;
    }

    public void setType4Units(Integer type4Units) {
        this.type4Units = type4Units;
    }

    public Integer getType5Units() {
        return type5Units;
    }

    public void setType5Units(Integer type5Units) {
        this.type5Units = type5Units;
    }

    public Integer getType6Units() {
        return type6Units;
    }

    public void setType6Units(Integer type6Units) {
        this.type6Units = type6Units;
    }

    public Integer getType7Units() {
        return type7Units;
    }

    public void setType7Units(Integer type7Units) {
        this.type7Units = type7Units;
    }

    public Integer getType8Units() {
        return type8Units;
    }

    public void setType8Units(Integer type8Units) {
        this.type8Units = type8Units;
    }

    public Integer getNbWheelchairUnits() {
        return nbWheelchairUnits;
    }

    public void setNbWheelchairUnits(Integer nbWheelchairUnits) {
        this.nbWheelchairUnits = nbWheelchairUnits;
    }

    public Integer getGrossInternalArea() {
        return grossInternalArea;
    }

    public void setGrossInternalArea(Integer grossInternalArea) {
        this.grossInternalArea = grossInternalArea;
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.UnitDetails;
    }

    @Override
    public void merge(NamedProjectBlock block) {
        UnitDetailsBlock unitDetailsBlock = (UnitDetailsBlock) block;
        this.setNewBuildUnits(unitDetailsBlock.newBuildUnits);
        this.setRefurbishedUnits(unitDetailsBlock.refurbishedUnits);
        this.setType1Units(unitDetailsBlock.type1Units);
        this.setType2Units(unitDetailsBlock.type2Units);
        this.setType3Units(unitDetailsBlock.type3Units);
        this.setType4Units(unitDetailsBlock.type4Units);
        this.setType5Units(unitDetailsBlock.type5Units);
        this.setType6Units(unitDetailsBlock.type6Units);
        this.setType7Units(unitDetailsBlock.type7Units);
        this.setType8Units(unitDetailsBlock.type8Units);
        this.setNbWheelchairUnits(unitDetailsBlock.nbWheelchairUnits);
        this.setGrossInternalArea(unitDetailsBlock.grossInternalArea);
    }

    @Override
    public boolean allowMultipleVersions() {
        return true;
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        super.copyBlockContentInto(target);

        UnitDetailsBlock unitDetailsBlock = (UnitDetailsBlock) target;
        unitDetailsBlock.setNewBuildUnits(this.newBuildUnits);
        unitDetailsBlock.setRefurbishedUnits(this.refurbishedUnits);
        unitDetailsBlock.setType1Units(this.type1Units);
        unitDetailsBlock.setType2Units(this.type2Units);
        unitDetailsBlock.setType3Units(this.type3Units);
        unitDetailsBlock.setType4Units(this.type4Units);
        unitDetailsBlock.setType5Units(this.type5Units);
        unitDetailsBlock.setType6Units(this.type6Units);
        unitDetailsBlock.setType7Units(this.type7Units);
        unitDetailsBlock.setType8Units(this.type8Units);
        unitDetailsBlock.setNbWheelchairUnits(this.nbWheelchairUnits);
        unitDetailsBlock.setGrossInternalArea(this.grossInternalArea);
        if (tableEntries != null) {
            for (UnitDetailsTableEntry entry : tableEntries) {
                unitDetailsBlock.getTableEntries().add(entry.copy());
            }
        }
    }

    /**
     * @return the unit entry identified by the given id or null if not found.
     */
    public UnitDetailsTableEntry getEntry(Integer id) {
        return tableEntries.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }

    @JsonIgnore
    /** Used to cache the calculation retrieved from project and avoid doing the same thing twice. */
    private Map<Integer, Integer> getTotalUnitsByExternalId() {
        if (totalUnitsByExternalId == null) {
            totalUnitsByExternalId = project.getTotalUnitsByExternalId();
        }
        return totalUnitsByExternalId;
    }

    public Integer getTotalUnits() {
        Integer total = 0;
        for (Integer value : getTotalUnitsByExternalId().values()) {
            total += value;
        }
        return total;
    }

    @Transient
    public Integer getProfiledUnits() {
        Integer total = 0;
        for (UnitDetailsTableEntry value : getTableEntries()) {
            total = nullSafeAdd(total, value.getNbUnits());
        }
        return total;
    }

    public TenureProfiles getTenureProfiles() {
        Map<Integer, Integer> totalUnitsByExternalId = getTotalUnitsByExternalId();
        List<TenureProfile> profiles = new ArrayList<>();
        Set<TemplateTenureType> tenureTypes = project.getTemplate().getTenureTypes();

        int totalUnits = 0;
        Map<Integer, TemplateTenureType> tenureTypeMap =
                tenureTypes.stream().collect(Collectors.toMap(TemplateTenureType::getExternalId, item -> item));
        for (Map.Entry<Integer, Integer> result : totalUnitsByExternalId.entrySet()) {
            TemplateTenureType tenureType = tenureTypeMap.get(result.getKey());

            int profiledUnits = 0;
            for (UnitDetailsTableEntry tableEntry : this.tableEntries) {
                if (tenureType.getExternalId().equals(tableEntry.getTenureId())) {
                    profiledUnits = nullSafeAdd(profiledUnits, tableEntry.getNbUnits());
                }
            }

            if (tenureType != null) {
                TenureProfile profile = new TenureProfile(tenureType.getExternalId(), tenureType.getName(), profiledUnits, result.getValue());
                profiles.add(profile);
                totalUnits = nullSafeAdd(totalUnits, result.getValue());
            }
        }

        List<TenureProfile> breakdown = profiles.stream().sorted(Comparator.comparingInt(TenureProfile::getExtId)).collect(Collectors.toList());

        return new TenureProfiles(breakdown, totalUnits);
    }

    public class TenureProfiles {
        List<TenureProfile> breakdown;
        int totalUnits;

        TenureProfiles(List<TenureProfile> tenureProfiles, int totalUnits) {
            this.breakdown = tenureProfiles;
            this.totalUnits = totalUnits;
        }

        public List<TenureProfile> getBreakdown() {
            return breakdown;
        }

        public int getTotalUnits() {
            return totalUnits;
        }
    }

    public class TenureProfile implements ComparableItem {
        private int extId;
        private String tenureName;
        private int profiledUnits;
        private int totalUnits;

        public TenureProfile(int extId, String tenureName, int profiledUnits, int totalUnits) {
            this.extId = extId;
            this.tenureName = tenureName;
            this.profiledUnits = profiledUnits;
            this.totalUnits = totalUnits;
        }

        public int getExtId() {
            return extId;
        }

        public void setExtId(int extId) {
            this.extId = extId;
        }

        public String getTenureName() {
            return tenureName;
        }

        public void setTenureName(String tenureName) {
            this.tenureName = tenureName;
        }

        public int getProfiledUnits() {
            return profiledUnits;
        }

        public void setProfiledUnits(int profiledUnits) {
            this.profiledUnits = profiledUnits;
        }

        public int getTotalUnits() {
            return totalUnits;
        }

        public void setTotalUnits(int totalUnits) {
            this.totalUnits = totalUnits;
        }

        @Override
        public String getComparisonId() {
            return tenureName;
        }
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock otherBlock, ProjectDifferences differences) {
        super.compareBlockSpecificContent(otherBlock, differences);

        UnitDetailsBlock other = (UnitDetailsBlock) otherBlock;

        if (!Objects.equals(newBuildUnits, other.newBuildUnits)) {
            differences.add(new ProjectDifference(this, "newBuildUnits"));
        }
        if (!Objects.equals(refurbishedUnits, other.refurbishedUnits)) {
            differences.add(new ProjectDifference(this, "refurbishedUnits"));
        }
        if (!Objects.equals(type1Units, other.type1Units)) {
            differences.add(new ProjectDifference(this, "type1Units"));
        }
        if (!Objects.equals(type2Units, other.type2Units)) {
            differences.add(new ProjectDifference(this, "type2Units"));
        }
        if (!Objects.equals(type3Units, other.type3Units)) {
            differences.add(new ProjectDifference(this, "type3Units"));
        }
        if (!Objects.equals(type4Units, other.type4Units)) {
            differences.add(new ProjectDifference(this, "type4Units"));
        }
        if (!Objects.equals(type5Units, other.type5Units)) {
            differences.add(new ProjectDifference(this, "type5Units"));
        }
        if (!Objects.equals(type6Units, other.type6Units)) {
            differences.add(new ProjectDifference(this, "type6Units"));
        }
        if (!Objects.equals(type7Units, other.type7Units)) {
            differences.add(new ProjectDifference(this, "type7Units"));
        }
        if (!Objects.equals(type8Units, other.type8Units)) {
            differences.add(new ProjectDifference(this, "type8Units"));
        }
        if (!Objects.equals(nbWheelchairUnits, other.nbWheelchairUnits)) {
            differences.add(new ProjectDifference(this, "nbWheelchairUnits"));
        }
        if (!Objects.equals(grossInternalArea, other.grossInternalArea)) {
            differences.add(new ProjectDifference(this, "grossInternalArea"));
        }

        // compare tiles
        List<TenureProfile> breakdown = this.getTenureProfiles().getBreakdown();
        List<TenureProfile> otherBreakdown = other.getTenureProfiles().getBreakdown();
        for (int i = 0; i < breakdown.size(); i++) {

            String compID = breakdown.get(i).getComparisonId();

            if (!Objects.equals(breakdown.get(i).getProfiledUnits(), otherBreakdown.get(i).getProfiledUnits())) {
                differences.add(new ProjectDifference(compID, "profiledUnits"));
            }

            if (!Objects.equals(breakdown.get(i).getTotalUnits(), otherBreakdown.get(i).getTotalUnits())) {
                differences.add(new ProjectDifference(compID, "totalUnits"));
            }
        }

        Map<Integer, UnitDetailsTableEntry> thisRental = getEntriesFromList(this.getTableEntries(), true, false);
        Map<Integer, UnitDetailsTableEntry> otherRental = getEntriesFromList(other.getTableEntries(), true, false);

        Map<Integer, UnitDetailsTableEntry> thisSales = getEntriesFromList(this.getTableEntries(), false, true);
        Map<Integer, UnitDetailsTableEntry> otherSales = getEntriesFromList(other.getTableEntries(), false, true);

        compareUnitsTables(differences,  otherRental, thisRental);
        compareUnitsTables(differences,  otherSales, thisSales);

    }

    private void compareUnitsTables(ProjectDifferences differences, Map<Integer, UnitDetailsTableEntry> left,  Map<Integer, UnitDetailsTableEntry> right) {
        Collection inBoth = CollectionUtils.intersection(left.keySet(), right.keySet());
        Collection inLeft = CollectionUtils.subtract(left.keySet(), right.keySet());
        Collection inRight = CollectionUtils.subtract(right.keySet(), left.keySet());

        for (Object o : inBoth) {
            UnitDetailsTableEntry leftEntry = left.get(o);
            UnitDetailsTableEntry rightEntry = right.get(o);
            // assuming type can't change or we'll have more issues with alignment etc
            if (!Objects.equals(leftEntry.getDiscountOffMarketValue(), rightEntry.getDiscountOffMarketValue())) {
                differences.add(new ProjectDifference(leftEntry, "discountOffMarketValue"));
            }
            if (!Objects.equals(leftEntry.getFirstTrancheSales(), rightEntry.getFirstTrancheSales())) {
                differences.add(new ProjectDifference(leftEntry, "firstTrancheSales"));
            }
            if (!Objects.equals(leftEntry.getMarketType(), rightEntry.getMarketType())) {
                differences.add(new ProjectDifference(leftEntry, "marketType"));
            }
            if (!GlaOpsUtils.areEqual(leftEntry.getMarketValue(), rightEntry.getMarketValue())) {
                differences.add(new ProjectDifference(leftEntry, "marketValue"));
            }
            if (!Objects.equals(leftEntry.getNbBeds(), rightEntry.getNbBeds())) {
                differences.add(new ProjectDifference(leftEntry, "nbBeds"));
            }
            if (!Objects.equals(leftEntry.getNbUnits(), rightEntry.getNbUnits())) {
                differences.add(new ProjectDifference(leftEntry, "nbUnits"));
            }
            if (!GlaOpsUtils.areEqual(leftEntry.getRentPercentageOfMarket(), rightEntry.getRentPercentageOfMarket())) {
                differences.add(new ProjectDifference(leftEntry, "rentPercentageOfMarket"));
            }
            if (!GlaOpsUtils.areEqual(leftEntry.getNetWeeklyRent(), rightEntry.getNetWeeklyRent())) {
                differences.add(new ProjectDifference(leftEntry, "netWeeklyRent"));
            }
            if (!GlaOpsUtils.areEqual(leftEntry.getWeeklyMarketRent(), rightEntry.getWeeklyMarketRent())) {
                differences.add(new ProjectDifference(leftEntry, "weeklyMarketRent"));
            }
            if (!GlaOpsUtils.areEqual(leftEntry.getRentTotal(), rightEntry.getRentTotal())) {
                differences.add(new ProjectDifference(leftEntry, "rentTotal"));
            }
            if (!Objects.equals(leftEntry.getUnitType(), rightEntry.getUnitType())) {
                differences.add(new ProjectDifference(leftEntry, "unitType"));
            }
            if (!GlaOpsUtils.areEqual(leftEntry.getWeeklyServiceCharge(), rightEntry.getWeeklyServiceCharge())) {
                differences.add(new ProjectDifference(leftEntry, "weeklyServiceCharge"));
            }
        }

        for (Object o : inLeft) {
            UnitDetailsTableEntry leftEntry = left.get(o);
            ProjectDifference difference = new ProjectDifference(leftEntry);
            difference.setDifferenceType(ProjectDifference.DifferenceType.Deletion);
            differences.add(difference);
        }

        for (Object o : inRight) {
            UnitDetailsTableEntry rightEntry = right.get(o);
            ProjectDifference difference = new ProjectDifference(rightEntry);
            difference.setDifferenceType(ProjectDifference.DifferenceType.Addition);
            differences.add(difference);
        }



    }



    private Map<Integer, UnitDetailsTableEntry> getEntriesFromList(Set<UnitDetailsTableEntry> entries, boolean rentals, boolean sales) {

        Map<Integer, UnitDetailsTableEntry> map = new HashMap<>();

        for (UnitDetailsTableEntry entry : entries) {
            if (rentals && entry.getMarketType().getAvailableForRental()) {
                map.put(entry.getOriginalId(), entry);
            }
            if (sales && entry.getMarketType().getAvailableForSales()) {
                map.put(entry.getOriginalId(), entry);
            }
        }

        return map;
    }

}

