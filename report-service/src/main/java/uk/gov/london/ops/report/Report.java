/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.framework.OpsEntity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "report")
public class Report implements OpsEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_seq_gen")
    @SequenceGenerator(name = "report_seq_gen", sequenceName = "report_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Column(name = "name")
    private String name;

    @JsonIgnore
    @Column(name = "sql_query", length = Integer.MAX_VALUE)
    private String sqlQuery;

    @Column(name = "external")
    private boolean external;

    @Size(max = 250)
    @Column(name = "description")
    private String description;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = ReportFilterType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "report_filters", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "filters")
    private List<ReportFilterType> reportFiltersList = new ArrayList<>();

    public Report() {}

    public Report(String name, String description, String sqlQuery) {
        this.name = name;
        this.description = description;
        this.sqlQuery = sqlQuery;
    }

    public Report(String name, String description, String sqlQuery, boolean external) {
        this(name, description, sqlQuery);
        this.external = external;
    }

    public Report(String name, String description, String sqlQuery, boolean external, List<ReportFilterType> reportFiltersList) {
        this(name, description, sqlQuery, external);
        this.reportFiltersList = reportFiltersList;
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

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<ReportFilterType> getReportFiltersList() {
        if (reportFiltersList != null) {
            return reportFiltersList;
        } else {
            List<ReportFilterType> reportFilterList = new ArrayList<>();
            reportFilterList.add(ReportFilterType.Programme);
            reportFilterList.add(ReportFilterType.ProjectStatus);
            reportFilterList.add(ReportFilterType.Borough);
            reportFilterList.add(ReportFilterType.ProjectType);
            return reportFilterList;
        }
    }

    public void setReportFiltersList(List<ReportFilterType> reportFiltersList) {
        this.reportFiltersList = reportFiltersList;
    }
}
