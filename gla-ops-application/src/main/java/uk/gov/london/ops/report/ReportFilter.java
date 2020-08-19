/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.report;
import java.util.ArrayList;
import java.util.List;

public class ReportFilter {

    private Report.Filter filter;

    private List<String> parameters = new ArrayList<>();

    public ReportFilter() {

    }

    public ReportFilter(Report.Filter filter) {
        this.filter = filter;
    }

    public Report.Filter getFilter() {
        return filter;
    }

    public void setFilter(Report.Filter filter) {
        this.filter = filter;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }



}
