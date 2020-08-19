/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.domain.OpsEntity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "report")
public class Report implements OpsEntity<Integer> {

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum Filter {
        Programme(true, true, false, "programme_ids"),
        ProjectStatus(true),
        Borough(true),
        ProjectType(true),
        Label(true, true, true, "label_ids"),
        Team(false);

        private final boolean external;
        private boolean sqlFilter = false;
        private boolean singleSelect = false;
        private String columnName;


        //TODO enforce column name and to replace ReportService.getColumn()
        Filter(boolean external) {
            this.external = external;
        }

        Filter(boolean external, boolean sqlFilter, boolean singleSelect, String columnName) {
            this.external = external;
            this.sqlFilter = sqlFilter;
            this.singleSelect = singleSelect;
            this.columnName = columnName;
        }

        public String getName() {
            return this.name();
        }

        public boolean isExternal() {
            return external;
        }

        public boolean isSqlFilter() {
            return sqlFilter;
        }

        public boolean isSingleSelect() {
            return singleSelect;
        }

        public String getColumnName() {
            return columnName;
        }

    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_seq_gen")
    @SequenceGenerator(name = "report_seq_gen", sequenceName = "report_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @Column(name = "name")
    private String name;

    @JsonIgnore
    @Column(name = "sql_query")
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
    @ElementCollection(targetClass = Report.Filter.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "report_filters", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "filters")
    private List<Filter> reportFiltersList = new ArrayList<>();

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

    public Report(String name, String description, String sqlQuery, boolean external, List<Filter> reportFiltersList) {
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


    public List<Filter> getReportFiltersList() {
        if (reportFiltersList != null) {
            return reportFiltersList;
        } else {
            List<Filter> reportFilterList = new ArrayList<>();
            reportFilterList.add(Filter.Programme);
            reportFilterList.add(Filter.ProjectStatus);
            reportFilterList.add(Filter.Borough);
            reportFilterList.add(Filter.ProjectType);
            return reportFilterList;
        }
    }

    public void setReportFiltersList(List<Filter> reportFiltersList) {
        this.reportFiltersList = reportFiltersList;
    }
}
