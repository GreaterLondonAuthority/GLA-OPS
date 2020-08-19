/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.jpa;

/**
 * Information about a join between two JPA entities.
 *
 * @author Steve Leach
 */
public class Join {

    public enum JoinType {
        OneToOne, OneToMany, ManyToOne, ManyToMany, Unknown, Complex, MultiColumn
    }

    public Join() {}

    public Join(JoinType joinType) {
        this.joinType = joinType;
    }

    private String fromTable = "?";
    private String fromColumn = "?";

    private String toTable = "?";
    private String toColumn = "?";
    private JoinType joinType = null;
    private String declaringEntity = "?";
    private String comments = "";

    public String getFromTable() {
        return fromTable;
    }

    public void setFromTable(String fromTable) {
        this.fromTable = fromTable;
    }

    public String getFromColumn() {
        return fromColumn;
    }

    public void setFromColumn(String fromColumn) {
        this.fromColumn = fromColumn;
    }

    public String getToTable() {
        return toTable;
    }

    public void setToTable(String toTable) {
        this.toTable = toTable;
    }

    public String getToColumn() {
        return toColumn;
    }

    public void setToColumn(String toColumn) {
        this.toColumn = toColumn;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    public String getDeclaringEntity() {
        return declaringEntity;
    }

    public void setDeclaringEntity(String declaringEntity) {
        this.declaringEntity = declaringEntity;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        if (fromColumn.equals("id")) {
            // We generally want to treat columns called just "id" as the "to" side of a relationship
            return String.format("%s.%s = %s.%s (%s)", toTable, toColumn, fromTable, fromColumn, joinType);
        } else {
            return String.format("%s.%s = %s.%s (%s)", fromTable, fromColumn, toTable, toColumn, joinType);
        }
    }
}
