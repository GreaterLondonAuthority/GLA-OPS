/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

/**
 * Describes a difference between the data in two versions of a project block.
 *
 * @author Steve Leach
 */
public class ProjectDifference {

    public enum DifferenceType {Change, Addition, Deletion}

    private String field = null;
    private String comparisonId = null;
    private String entityType = null;
    private DifferenceType differenceType = DifferenceType.Change;

    public ProjectDifference() {}

    public ProjectDifference(ComparableItem item, String field) {
        this.field = field;
        this.comparisonId = item.getComparisonId();
        this.entityType = item.getClass().getSimpleName();
    }

    public ProjectDifference(ComparableItem parent, ComparableItem child, String field) {
        this.field = field;
        this.comparisonId = parent.getComparisonId() + ":" + child.getComparisonId();
        this.entityType = child.getClass().getSimpleName();
    }

    public ProjectDifference(String comparisonId, String field) {
        this.field = field;
        this.comparisonId = comparisonId;
    }

    public ProjectDifference(ComparableItem item) {
        this.comparisonId = item.getComparisonId();
        this.entityType = item.getClass().getSimpleName();
    }

    public ProjectDifference(ComparableItem item, DifferenceType differenceType) {
        this(item);
        this.differenceType = differenceType;
    }

    public ProjectDifference(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getComparisonId() {
        return comparisonId;
    }

    public void setComparisonId(String comparisonId) {
        this.comparisonId = comparisonId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public DifferenceType getDifferenceType() {
        return differenceType;
    }

    public void setDifferenceType(DifferenceType differenceType) {
        this.differenceType = differenceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectDifference that = (ProjectDifference) o;

        if (field != null ? !field.equals(that.field) : that.field != null) return false;
        if (comparisonId != null ? !comparisonId.equals(that.comparisonId) : that.comparisonId != null) return false;
//        if (entityType != null ? !entityType.equals(that.entityType) : that.entityType != null) return false;
        return differenceType == that.differenceType;
    }

    @Override
    public int hashCode() {
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (comparisonId != null ? comparisonId.hashCode() : 0);
//        result = 31 * result + (entityType != null ? entityType.hashCode() : 0);
        result = 31 * result + (differenceType != null ? differenceType.hashCode() : 0);
        return result;
    }
}
