/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report.implementation;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.report.Report;
import uk.gov.london.ops.report.ReportFilterType;

import javax.transaction.Transactional;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Component
public class ReportingDataInitialiser implements DataInitialiserModule {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    ReportRepository reportRepository;

    @Override
    public String getName() {
        return "Reporting data initialiser";
    }

    @Override
    public void addSupplementalData() {
        List<ReportFilterType> programmeFilter = new ArrayList<>();
        programmeFilter.add(ReportFilterType.Programme);

        Report costExceeded = new Report("Cost Exceeded", "Report for testing exceeded cost", "cost_exceeded", false,
                programmeFilter);

        reportRepository.save(costExceeded);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:**/testdata/reports/*.sql");
            List<ReportFilterType> reportFilterList;
            for (Resource resource : resources) {
                Report report = new Report(resource.getFilename().replace(".sql", ""), null,
                        IOUtils.toString(resource.getInputStream(), Charset.defaultCharset()));

                if (report.getName().toLowerCase().contains("external")) {
                    report.setExternal(true);
                    reportFilterList = report.getReportFiltersList();
                    reportFilterList.add(ReportFilterType.Programme);
                    reportFilterList.add(ReportFilterType.Borough);
                    reportFilterList.add(ReportFilterType.ProjectStatus);
                    reportFilterList.add(ReportFilterType.ProjectType);
                    reportFilterList.add(ReportFilterType.Team);
                }
                if (report.getName().toLowerCase().contains("internal")) {
                    reportFilterList = report.getReportFiltersList();
                    reportFilterList.add(ReportFilterType.Programme);
                    reportFilterList.add(ReportFilterType.Team);

                }
                if (report.getName().toLowerCase().contains("corporate")) {
                    report.setExternal(true);
                    report.setDescription("Report about corporate projects");
                    reportFilterList = report.getReportFiltersList();
                    reportFilterList.add(ReportFilterType.Programme);
                    reportFilterList.add(ReportFilterType.Label);

                }
                reportRepository.save(report);
            }
        } catch (Exception e) {
            log.error("Error loading SQL reports", e);
        }
    }

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
                    List<ReportFilterType> reportFilterList = report.getReportFiltersList();
                    reportFilterList.add(ReportFilterType.Programme);
                    reportFilterList.add(ReportFilterType.Borough);
                    reportFilterList.add(ReportFilterType.ProjectStatus);
                    reportFilterList.add(ReportFilterType.ProjectType);
                    reportFilterList.add(ReportFilterType.Team);
                    reportRepository.save(report);
                } else if (report.getName().toLowerCase().equals("financial ledger")) {
                    List<ReportFilterType> reportFilterList = report.getReportFiltersList();
                    reportFilterList.add(ReportFilterType.Programme);
                    reportFilterList.add(ReportFilterType.Borough);
                    reportFilterList.add(ReportFilterType.ProjectType);
                    reportRepository.save(report);
                }
            } else {
                log.warn("Report : " + report.getName() + " is not configured with any filters");
            }
        }
    }

}