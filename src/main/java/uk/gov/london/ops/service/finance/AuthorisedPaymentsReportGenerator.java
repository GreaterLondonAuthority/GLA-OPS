/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.finance;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.FeatureStatus;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectLedgerEntry;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.service.EmailService;
import uk.gov.london.ops.service.UserService;
import uk.gov.london.ops.service.project.ProjectService;
import uk.gov.london.ops.util.CSVFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.london.ops.domain.finance.LedgerType.PAYMENT;

@Component
@Transactional
public class AuthorisedPaymentsReportGenerator {

    static final String PAYEE           = "Payee";
    static final String PROJECT_ID      = "Project ID";
    static final String PAYMENT_ID      = "Payment ID";
    static final String WBS_CODE        = "WBS code";
    static final String SAP_VENDOR_ID   = "SAP vendor ID";
    static final String ADDRESS         = "Address";
    static final String BOROUGH         = "Borough";
    static final String POSTCODE        = "Postcode";
    static final String AMOUNT          = "Payment amount";
    static final String AUTHORISED_ON   = "Payment authorisation date";
    static final String AUTHORISED_BY   = "Authorised by";
    static final String FILE_NAME       = "Invoice file name";

    Set<String> csvHeaders = Stream.of(PAYEE, PROJECT_ID, PAYMENT_ID, WBS_CODE, SAP_VENDOR_ID, ADDRESS, BOROUGH, POSTCODE, AMOUNT, AUTHORISED_ON, AUTHORISED_BY, FILE_NAME)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    EmailService emailService;

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    FinanceService financeService;

    @Autowired
    Environment environment;

    @Scheduled(cron = "0 0 3 * * ?")
    public void generateDailyAuthorisedPaymentsReport() throws IOException {
        if (!featureStatus.isEnabled(FeatureStatus.Feature.Payments) || !featureStatus.isEnabled(FeatureStatus.Feature.AuthorisedPaymentsReport)) {
            log.info("payments feature or authorised payments report not enabled");
            return;
        }

        OffsetDateTime yesterday = environment.now().minusDays(1);
        generateDailyAuthorisedPaymentsReport(yesterday);
    }

    public void generateDailyAuthorisedPaymentsReport(OffsetDateTime day) throws IOException {
        log.debug("generateDailyAuthorisedPaymentsReport() - START");

        List<ProjectLedgerEntry> payments = getPaymentsAuthorisedDay(day);

        if (CollectionUtils.isNotEmpty(payments)) {
            log.debug("found {} authorised payments to process", payments.size());

            String csvReport = generateCsvReportPayload(payments, day);
            log.debug("CSV report:\n{}", csvReport);

            emailService.sendAuthorisedPaymentsReportEmail(day, csvReport);
        }
        else {
            log.debug("no authorised payments to process");
            emailService.sendNoAuthorisedPaymentsReportEmail(day);
        }

        log.debug("generateDailyAuthorisedPaymentsReport() - END");
    }

    String generateCsvReportPayload(List<ProjectLedgerEntry> payments, OffsetDateTime day) throws IOException {
        Map<String, List<ProjectLedgerEntry>> paymentsGroupedByPayee = payments.stream().collect(Collectors.groupingBy(ProjectLedgerEntry::getVendorName));

        StringWriter out = new StringWriter();

        CSVFile csvFile = new CSVFile(csvHeaders, out);

        BigDecimal total = new BigDecimal(0);
        for (String payee: paymentsGroupedByPayee.keySet()) {
            BigDecimal payeeTotal = new BigDecimal(0);
            for (ProjectLedgerEntry payment: paymentsGroupedByPayee.get(payee)) {
                payeeTotal = payeeTotal.add(payment.getValue());
                total = total.add(payment.getValue());
                csvFile.writeValues(toValueMap(payment));
            }

            if (paymentsGroupedByPayee.get(payee).size() > 1) {
                writeSummaryLine(csvFile, payee+" Total", payeeTotal);
            }

            csvFile.writeEmptyLine();
        }

        writeSummaryLine(csvFile, "BACS total for "+DateTimeFormatter.ofPattern("yyyy-MM-dd").format(day), total);

        String payload = out.toString();
        csvFile.close();
        return payload;
    }

    List<ProjectLedgerEntry> getPaymentsAuthorisedDay(OffsetDateTime day) {
        OffsetDateTime dayStart = day.withHour(0).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime dayEnd = day.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return financeService.findAllByAuthorisedOnBetween(PAYMENT,dayStart, dayEnd);
    }

    Map<String, Object> toValueMap(ProjectLedgerEntry payment) {
        Project project = projectService.get(payment.getProjectId());
        User user = userService.find(payment.getAuthorisedBy());

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(PAYEE, payment.getVendorName());
        valueMap.put(PROJECT_ID, payment.getProjectId());
        valueMap.put(PAYMENT_ID, payment.getId());
        valueMap.put(WBS_CODE, payment.getWbsCode());
        valueMap.put(SAP_VENDOR_ID, payment.getSapVendorId());
        valueMap.put(ADDRESS, project.getDetailsBlock().getAddress());
        valueMap.put(BOROUGH, project.getDetailsBlock().getBorough());
        valueMap.put(POSTCODE, project.getDetailsBlock().getPostcode());
        valueMap.put(AMOUNT, payment.getValue());
        valueMap.put(AUTHORISED_ON, DateTimeFormatter.ofPattern("yyyy-MM-dd").format(payment.getAuthorisedOn()));
        valueMap.put(AUTHORISED_BY, user != null ? user.getFullName() : null);
        valueMap.put(FILE_NAME, payment.getInvoiceFileName());
        return valueMap;
    }

    private void writeSummaryLine(CSVFile csvFile, String description, BigDecimal total) throws IOException {
        Map<String, Object> line = new HashMap<>();
        line.put(PAYEE, description);
        line.put(AMOUNT, total);

        csvFile.writeEmptyLine();
        csvFile.writeValues(line);
    }

}
