/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.OpsEntity;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity(name="report")
public class Report implements OpsEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_seq_gen")
    @SequenceGenerator(name = "report_seq_gen", sequenceName = "report_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Column(name="name")
    private String name;

    @JsonIgnore
    @Column(name="sql_query")
    private String sqlQuery;

    @Column(name="created_by", updatable = false)
    private String createdBy;

    @Column(name="created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name="modified_by")
    private String modifiedBy;

    @Column(name="modified_on")
    private OffsetDateTime modifiedOn;

    public Report() {}

    public Report(String name, String sqlQuery) {
        this.name = name;
        this.sqlQuery = sqlQuery;
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

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
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

}
