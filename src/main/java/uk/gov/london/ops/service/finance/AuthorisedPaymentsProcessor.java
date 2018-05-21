/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.finance;

import com.jcraft.jsch.ChannelSftp;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.xml.transform.StringResult;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.ScheduledTask;
import uk.gov.london.ops.domain.finance.LedgerStatus;
import uk.gov.london.ops.domain.finance.LedgerType;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.project.ProjectLedgerEntry;
import uk.gov.london.ops.repository.OrganisationRepository;
import uk.gov.london.ops.service.ScheduledTaskService;

import javax.annotation.PostConstruct;
import javax.xml.bind.Marshaller;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static uk.gov.london.ops.domain.finance.LedgerType.PAYMENT;
import static uk.gov.london.ops.domain.project.ProjectLedgerEntry.INVOICE_TRANSACTION;

@Component
public class AuthorisedPaymentsProcessor {

    Logger log = LoggerFactory.getLogger(getClass());

    static final String AUTHORISED_PAYMENTS_LOCK = "AUTHORISED_PAYMENTS_LOCK";
    static final String TASK_KEY = "AUTHORISED_PAYMENTS";

    String defaultGeneralLedgerCode = "";

    @Autowired
    ScheduledTaskService scheduledTaskService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    JdbcLockRegistry lockRegistry;

    @Autowired
    SessionFactory<ChannelSftp.LsEntry> sessionFactory;

    @Autowired
    Environment environment;

    @Value("${sap.ftp.enabled}")
    boolean enabled;

    @Value("${sap.ftp.remote.path.incoming.invoices}")
    String remotePathIncomingInvoice;

    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

    AtomicInteger executionCount = new AtomicInteger(0);
    LocalDateTime lastExecuted = null;

    @PostConstruct
    public void initMarshaller() {
        marshaller.setPackagesToScan("uk.gov.london.ops.service.finance");
        Map<String, Object> props = new HashMap<>();
        props.put(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
        props.put(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        marshaller.setMarshallerProperties(props);
    }

    @Scheduled(fixedDelayString = "${send.authorised.payments.run.interval.milliseconds}")
    public void processAuthorisedPayments() {
        if (!enabled) {
            return;
        }

        log.debug("about to process authorised payments ...");
        executionCount.incrementAndGet();
        lastExecuted = LocalDateTime.now(environment.clock());

        final Lock lock = lockRegistry.obtain(AUTHORISED_PAYMENTS_LOCK);
        try {

            final String logMessage;
            final String taskDesc;
            if (lock != null && lock.tryLock()) {

                final List<ProjectLedgerEntry> authorisedPayments = financeService.findByStatusAndTypeIn(
                        LedgerStatus.Authorised, new LedgerType[] {PAYMENT} );
                if (!CollectionUtils.isEmpty(authorisedPayments)) {
                    final Session<ChannelSftp.LsEntry> session = sessionFactory.getSession();

                    final List<ProjectLedgerEntry> updatedPayments = authorisedPayments
                            .stream()
                            .map(p-> processAuthorisedPayment(p, session))
                            .filter(p-> p != null)
                            .collect(Collectors.toList());
                    financeService.save(updatedPayments);
                    session.close();
                    taskDesc = ScheduledTask.SUCCESS;
                    logMessage = String.format("Authorised payments process: %d authorised payments processed",
                            updatedPayments.size());
                } else {
                    taskDesc = ScheduledTask.SKIPPED;
                    logMessage = "There are NO payments to process";
                }
            }else {
                taskDesc = ScheduledTask.SKIPPED;
                logMessage = "Could not obtain lock: " + AUTHORISED_PAYMENTS_LOCK;
            }
            scheduledTaskService.update(TASK_KEY, taskDesc, logMessage);
            log.debug(logMessage);
        }
        catch (Exception e) {
            scheduledTaskService.update(TASK_KEY, e);
            log.error("Error processing authorised payments", e);
        }
        finally {
            if (lock != null) lock.unlock();
        }
    }

    ProjectLedgerEntry processAuthorisedPayment(ProjectLedgerEntry payment, Session<ChannelSftp.LsEntry> session) {
        try {
            String fileName = this.remotePathIncomingInvoice+"/"+payment.buildInvoiceFileName();

            String xml = toXmlString(generateInvoice(payment));

            log.debug("xml file for {}: \n{}", payment.getId(), xml);

            InputStream inputStream = new CharSequenceInputStream(xml, Charset.defaultCharset());
            session.write(inputStream, fileName);

            payment.setLedgerStatus(LedgerStatus.Sent);
            payment.setInvoiceFileName(fileName);
            payment.setSentOn(environment.now());
            return payment;
        }
        catch (Exception e) {
            log.error("failed to process authorised payment "+payment.getId(), e);
            return null;
        }
    }

    SalesInvoiceDocument generateInvoice(ProjectLedgerEntry payment) {
        String authorisedOnString = DateTimeFormatter.ofPattern("yyyyMMdd").format(payment.getAuthorisedOn());

        SalesInvoiceDocument salesInvoiceDocument = new SalesInvoiceDocument();
        salesInvoiceDocument.invoiceReferences.SuppliersInvoiceNumber = String.format("P%d-%d", payment.getProjectId(), payment.getId());
        salesInvoiceDocument.invoiceDate = authorisedOnString;
        salesInvoiceDocument.taxPointDate = authorisedOnString;
        //todo: Change this, temporal fix
        final Organisation organisation = organisationRepository.findOne(payment.getOrganisationId());
        salesInvoiceDocument.buyer.buyerReferences.suppliersCodeForBuyer = organisation.getsapVendorId();
        salesInvoiceDocument.narrative = String.format("P%d / %s / %s", payment.getProjectId(), payment.getCategory(), payment.getSubCategory());


        salesInvoiceDocument.head.invoiceType.invoiceType = INVOICE_TRANSACTION;

        BigDecimal totalIncludingInterest = payment.getTotalIncludingInterest();
        if (payment.isReclaim()) { // relcaims are credits.
            salesInvoiceDocument.head.invoiceType.invoiceType = "CRN";
        } else {
            // payments are negated
            totalIncludingInterest = totalIncludingInterest.negate();
        }

        salesInvoiceDocument.addLine(totalIncludingInterest, "WBS-" + payment.getWbsCode(), defaultGeneralLedgerCode);

        return salesInvoiceDocument;
    }

    String toXmlString(SalesInvoiceDocument salesInvoiceDocument) {
        StringResult result = new StringResult();
        marshaller.marshal(salesInvoiceDocument, result);
        String xmlContent = result.toString();

        return fixupXmlContentForSAP(xmlContent);
    }

    /**
     * Modify the generated XML so that SAP doesn't complain when importing it.
     */
    String fixupXmlContentForSAP(String xmlContent) {
        // There will be a much better way to do this using marshaller configuration, I just can't find it yet.
        xmlContent = xmlContent.replace("xmlns:ns2=","xmlns=");
        xmlContent = xmlContent.replace("ns2:Invoice","Invoice");

        // Add the prologue manually to avoid the "standalone" attribute.
        // Marshaller has JAXB_FRAGMENT property set so it doesn't add the prologue itself.
        xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xmlContent;

        return xmlContent;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getExecutionCount() {
        return executionCount.intValue();
    }

    public LocalDateTime getLastExecuted() {
        return lastExecuted;
    }
}
