/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.di.DataInitialiserModule;
import uk.gov.london.ops.report.Report;

import java.util.List;

/**
 * Initialises some basic reference data in all environments (not just test environments).
 *
 * Includes market and tenure types from unit details block, and risk levels from risks block.
 *
 * @author  Steve Leach
 */
@Component
public class FilterDataInitialiser implements DataInitialiserModule {

    Logger log = LoggerFactory.getLogger(getClass());


    @Autowired
    ReportRepository reportRepository;

    @Override
    public String getName() {
        return "Filter data initialiser";
    }

    @Override
    public boolean runInAllEnvironments() {
        return true;
    }

    @Override
    public void beforeInitialisation() {}

    @Override
    public void cleanupOldData() {



    }

    @Override
    public void addReferenceData() {

    }


    @Override
    public void addUsers() {}

    @Override
    public void addOrganisations() {}

    @Override
    public void addTemplates() {}

    @Override
    public void addProgrammes() {}

    @Override
    public void addProjects() {}

    @Override
    public void addSupplementalData() {}

    @Override
    public void afterInitialisation() {
        createReportFiltersForHistoricReports();
    }



    public void createReportFiltersForHistoricReports() {
        List<Report> all = reportRepository.findAll();
        for (Report report : all) {
            report = reportRepository.findById(report.getId()).orElse(null);
            if (report.getReportFiltersList().isEmpty()) {
                if (report.getName().toLowerCase().equals("housing outturn")) {
                    List<Report.Filter> reportFilterList = report.getReportFiltersList();
                    reportFilterList.add(Report.Filter.Programme);
                    reportFilterList.add(Report.Filter.Borough);
                    reportFilterList.add(Report.Filter.ProjectStatus);
                    reportFilterList.add(Report.Filter.ProjectType);
                    reportFilterList.add(Report.Filter.Team);
                    reportRepository.save(report);
                } else if (report.getName().toLowerCase().equals("financial ledger")) {
                    List<Report.Filter> reportFilterList = report.getReportFiltersList();
                    reportFilterList.add(Report.Filter.Programme);
                    reportFilterList.add(Report.Filter.Borough);
                    reportFilterList.add(Report.Filter.ProjectType);
                    reportRepository.save(report);
                }
            } else {
                log.warn("Report : " + report.getName() + " is not configured with any filters");
            }
        }
    }

}
