/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ReportFilterType {
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
    ReportFilterType(boolean external) {
        this.external = external;
    }

    ReportFilterType(boolean external, boolean sqlFilter, boolean singleSelect, String columnName) {
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
