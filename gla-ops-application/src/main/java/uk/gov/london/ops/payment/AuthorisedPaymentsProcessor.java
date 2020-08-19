/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import com.jcraft.jsch.ChannelSftp;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.domain.ScheduledTask;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.organisation.OrganisationService;
import uk.gov.london.ops.organisation.model.Organisation;
import uk.gov.london.ops.payment.implementation.sap.model.SalesInvoiceDocument;
import uk.gov.london.ops.refdata.RefDataService;
import uk.gov.london.ops.service.ScheduledTaskService;
import uk.gov.london.ops.user.UserService;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
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

import static uk.gov.london.ops.payment.LedgerType.PAYMENT;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.INVOICE_TRANSACTION;

@Component
@Transactional
public class AuthorisedPaymentsProcessor {

    Logger log = LoggerFactory.getLogger(getClass());

    static final String AUTHORISED_PAYMENTS_LOCK = "AUTHORISED_PAYMENTS_LOCK";
    static final String TASK_KEY = "AUTHORISED_PAYMENTS";

    @Autowired
    ScheduledTaskService scheduledTaskService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private UserService userService;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    RefDataService refDataService;

    @Autowired
    JdbcLockRegistry lockRegistry;

    @Autowired
    SessionFactory<ChannelSftp.LsEntry> sessionFactory;

    @Autowired
    Environment environment;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    PaymentAuditService paymentAuditService;

    @Value("${sap.ftp.enabled}")
    boolean enabled;

    @Value("${sap.ftp.remote.path.incoming.invoices}")
    String remotePathIncomingInvoice;

    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

    AtomicInteger executionCount = new AtomicInteger(0);
    LocalDateTime lastExecuted = null;

    @PostConstruct
    public void initialise() {
        marshaller.setPackagesToScan("uk.gov.london.ops.payment");
        Map<String, Object> props = new HashMap<>();
        props.put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
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
                final List<ProjectLedgerEntry> authorisedPayments = getAuthorisedPayments();
                if (!CollectionUtils.isEmpty(authorisedPayments)) {
                    final Session<ChannelSftp.LsEntry> session = sessionFactory.getSession();

                    final List<ProjectLedgerEntry> updatedPayments = authorisedPayments
                            .stream()
                            .map(p -> processAuthorisedPayment(p, session))
                            .filter(p -> p != null)
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
            } else {
                taskDesc = ScheduledTask.SKIPPED;
                logMessage = "Could not obtain lock: " + AUTHORISED_PAYMENTS_LOCK;
            }
            scheduledTaskService.update(TASK_KEY, taskDesc, logMessage);
            log.debug(logMessage);
        } catch (Exception e) {
            scheduledTaskService.update(TASK_KEY, e);
            log.error("Error processing authorised payments", e);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    public List<ProjectLedgerEntry> getAuthorisedPayments() {
        List<ProjectLedgerEntry> authorisedPayments = financeService.findByStatusAndTypeIn(LedgerStatus.Authorised, new LedgerType[] {PAYMENT});

        boolean includeSkills = featureStatus.isEnabled(Feature.SkillsPayments);


        authorisedPayments = authorisedPayments
                .stream()
                .filter(ple -> includeSkills || !"Skills".equals(ple.getCategory()))
                .filter(ple -> ple.getPaymentSource() != null
                    && refDataService.getPaymentSourceMap().get(ple.getPaymentSource()).shouldPaymentSourceBeSentToSAP())
                .collect(Collectors.toList());
        return authorisedPayments;
    }

    ProjectLedgerEntry processAuthorisedPayment(ProjectLedgerEntry payment, Session<ChannelSftp.LsEntry> session) {
        String xml = paymentToXmlString(payment);

        ProjectLedgerEntry response = processAuthorisedPayment(payment, xml, session);
        if (response != null) {
            response.setSentOn(environment.now());
            response.setXmlFile(xml);

            paymentAuditService.recordPaymentAuditItem(response, PaymentAuditItemType.Sent, xml);
            return response;

        }
        return null;
    }

    ProjectLedgerEntry processAuthorisedPayment(ProjectLedgerEntry payment, String xml, Session<ChannelSftp.LsEntry> session) {
        try {
            String fileName = this.remotePathIncomingInvoice + "/" + payment.buildInvoiceFileName();

            log.debug("xml file for {}: \n{}", payment.getId(), xml);

            InputStream inputStream = new CharSequenceInputStream(xml, Charset.defaultCharset());
            session.write(inputStream, fileName);

            payment.setLedgerStatus(LedgerStatus.Sent);
            payment.setInvoiceFileName(fileName);
            return payment;
        } catch (Exception e) {
            log.error("failed to process authorised payment " + payment.getId(), e);
            return null;
        }
    }

    private String paymentToXmlString(ProjectLedgerEntry payment) {
        return toXmlString(generateInvoice(payment));
    }

    public ProjectLedgerEntry reprocessAuthorisedPayment(Integer paymentId, String wbsCode,
                                                         String ceCode, String sapVendorId, String companyName) {
        ProjectLedgerEntry payment = financeService.findOne(paymentId);
        if (!payment.isResendable()) {
            throw new ValidationException("Unable to resend this payment");
        }

        // only override the payment XML file if the codes have been overridden during a resend request
        boolean reprocessXML = false;
        String audit = "resent payment with ID " + paymentId;
        if (!StringUtils.equals(wbsCode, payment.getWbsCode())) {
            payment.setWbsCode(wbsCode);
            audit += " wbs code updated to: " + wbsCode;
            reprocessXML = true;
        }

        if (!StringUtils.equals(ceCode, payment.getCeCode())) {
            payment.setCeCode(ceCode);
            audit += " ce code updated to: " + ceCode;
            reprocessXML = true;
        }

        if (!StringUtils.equals(sapVendorId, payment.getSapVendorId())) {
            payment.setSapVendorId(sapVendorId);
            audit += " Sap Vendor Id updated to: " + sapVendorId;
            reprocessXML = true;
        }

        if (!StringUtils.equals(companyName, payment.getCompanyName())) {
            payment.setCompanyName(companyName);
            audit += " company name updated to: " + companyName;
            reprocessXML = true;
        }

        if (reprocessXML) {
            payment.setXmlFile(paymentToXmlString(payment));
            paymentAuditService.recordPaymentAuditItem(payment, PaymentAuditItemType.Modified, payment.getXmlFile());

        }

        if (enabled) {
            Session<ChannelSftp.LsEntry> session = sessionFactory.getSession();

            payment = processAuthorisedPayment(payment, payment.getXmlFile(), session);

            session.close();
        }

        if (payment != null) {
            payment.setResentOn(environment.now());
            payment.setResender(userService.currentUser());
            payment.setLedgerStatus(LedgerStatus.Sent);
            financeService.save(payment);
            auditService.auditCurrentUserActivity(audit);
            paymentAuditService.recordPaymentAuditItem(payment, PaymentAuditItemType.Resent, payment.getXmlFile());
        }

        return payment;
    }

    SalesInvoiceDocument generateInvoice(ProjectLedgerEntry payment) {
        String authorisedOnString = DateTimeFormatter.ofPattern("yyyyMMdd").format(payment.getAuthorisedOn());
        SalesInvoiceDocument salesInvoiceDocument = new SalesInvoiceDocument();
        salesInvoiceDocument.invoiceReferences.SuppliersInvoiceNumber = String.format("P%d-%d", payment.getProjectId(), payment.getId());
        salesInvoiceDocument.invoiceDate = authorisedOnString;
        salesInvoiceDocument.taxPointDate = authorisedOnString;
        String companyName;
        if (GlaUtils.isNullOrEmpty(payment.getCompanyName())) {
            companyName = "GLA";
            log.warn("Payment created without specific company name, defaulting to GLA for now. Project: " + payment.getProjectId() + " Payment : " + payment.getId());
        } else {
            companyName = payment.getCompanyName();
        }
        salesInvoiceDocument.supplier.references.BuyersCodeForSupplier = companyName;
        salesInvoiceDocument.invoiceTo.contact.name = companyName;
        final Organisation organisation = organisationService.findOne(payment.getOrganisationId());
        salesInvoiceDocument.buyer.buyerReferences.suppliersCodeForBuyer = organisation.getsapVendorId();
        salesInvoiceDocument.narrative = String.format("P%d / %s / %s", payment.getProjectId(), payment.getCategory(), payment.getSubCategory());
        salesInvoiceDocument.head.invoiceType.invoiceType = INVOICE_TRANSACTION;
        BigDecimal paymentValue = payment.getValue();
        if (payment.isReclaim()) { // reclaims are credits.
            salesInvoiceDocument.head.invoiceType.invoiceType = "CRN";
        } else {
            // payments are negated
            paymentValue = paymentValue.negate();
        }
        salesInvoiceDocument.addLine(paymentValue, "WBS-" + payment.getWbsCode(), payment.getCeCode());
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
        xmlContent = xmlContent.replace("xmlns:ns2=", "xmlns=");
        xmlContent = xmlContent.replace("ns2:Invoice", "Invoice");

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
