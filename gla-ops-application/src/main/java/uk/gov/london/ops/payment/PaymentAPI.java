/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import com.jcraft.jsch.ChannelSftp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.user.UserServiceImpl;

import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.london.common.GlaUtils.parseDateString;
import static uk.gov.london.common.user.BaseRole.GLA_FINANCE;
import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_PROGRAMME_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_READ_ONLY;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;
import static uk.gov.london.common.user.BaseRole.PROJECT_READER;
import static uk.gov.london.common.user.BaseRole.TECH_ADMIN;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;
import static uk.gov.london.ops.permission.PermissionType.VIEW_PAYMENT_HISTORY;

@RestController
@RequestMapping("/api/v1")
@Api
public class PaymentAPI {

    @Autowired
    PaymentService paymentService;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    AuthorisedPaymentsProcessor authorisedPaymentsProcessor;

    @Autowired
    DailyPaymentsReportGenerator dailyPaymentsReportGenerator;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    SessionFactory<ChannelSftp.LsEntry> sessionFactory;

    @Autowired
    PaymentAuditService paymentAuditService;

    @Value("${sap.ftp.remote.path.outgoing.invoices}")
    String remotePathOutgoingInvoice;

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_PROGRAMME_ADMIN, GLA_SPM, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/payments", method = RequestMethod.GET)
    @ApiOperation(value = "Get all payments with optional filter", notes = "Get all payments")
    // TODO when getAll is called, pass PaymentFilterOption.ALL.statuses() (for updating tests)
    public Page<PaymentSummary> getAll(
            @RequestParam(name = "project", required = false) String projectIdOrName,
            @RequestParam(name = "organisation", required = false) String organisationName,
            @RequestParam(name = "programme", required = false) String programmeName,
            @RequestParam(name = "sapVendorId", required = false) String sapVendorId,
            @RequestParam(required = false) List<String> paymentSources,
            @RequestParam(required = false) List<LedgerStatus> relevantStatuses,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> relevantProgrammes,
            @RequestParam(required = false) List<Integer> managingOrganisations,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) List<String> paymentDirection,
            Pageable pageable) {
        OffsetDateTime from = null;
        if (fromDate != null) {
            from = parseDateString(fromDate + " 00:00", "dd/MM/yyyy HH:mm");
        }

        OffsetDateTime to = null;
        if (toDate != null) {
            // Looking for any date before midnight (00:00 of the following day)
            to = parseDateString(toDate + " 00:00", "dd/MM/yyyy HH:mm").plusDays(1);
        }

        ensurePaymentsFeatureIsEnabled();

        if (relevantStatuses == null || relevantStatuses.isEmpty()) {
            relevantStatuses = PaymentFilterOption.ALL_PAYMENTS.getRelevantStatusesAsList();
        }

        return paymentService.findAll(projectIdOrName,
                organisationName,
                programmeName,
                sapVendorId,
                paymentSources,
                relevantStatuses,
                categories,
                relevantProgrammes,
                managingOrganisations,
                from,
                to,
                paymentDirection,
                pageable);
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_PROGRAMME_ADMIN, GLA_SPM,
            ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/paymentGroups", method = RequestMethod.GET)
    @ApiOperation(value = "Get all payments groups with optional filter", notes = "Get all payments")
    public List<PaymentGroupEntity> getPaymentGroups(@RequestParam(required = false) PaymentFilterOption status) {
        ensurePaymentsFeatureIsEnabled();
        if (featureStatus.isEnabled(Feature.UseFastPendingPayments)) {
            return paymentService.findAllPaymentGroupsByStatusFast(status);
        } else {
            return paymentService.findAllPaymentGroupsByStatus(status);
        }
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_PROGRAMME_ADMIN,
            GLA_SPM, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/paymentGroups/{groupId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get a payment group by id", notes = "Get payment by id")
    public PaymentGroupEntity getPaymentGroupById(@PathVariable("groupId") final Integer groupId) {
        ensurePaymentsFeatureIsEnabled();
        return paymentService.getGroupById(groupId);
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, GLA_ORG_ADMIN, GLA_PROGRAMME_ADMIN, GLA_SPM, ORG_ADMIN,
            PROJECT_EDITOR, PROJECT_READER, TECH_ADMIN})
    @RequestMapping(value = "/paymentGroups/payment/{paymentId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get a payment group by the id of one of it's payments", notes = "Get payment group by id payment id")
    public PaymentGroupEntity getPaymentGroupByPaymentId(@PathVariable("paymentId") final Integer paymentId) {
        ensurePaymentsFeatureIsEnabled();
        return paymentService.getGroupByPaymentId(paymentId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/payments/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "Get a single payment request", notes = "Get a payment request")
    public PaymentSummary getById(@PathVariable Integer id) {
        ensurePaymentsFeatureIsEnabled();
        return paymentService.getById(id);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/payments/resend/{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "Reprocesses an existing payment", notes = "Reprocesses an existing payment")
    public  ProjectLedgerEntry reprocessPayment(@PathVariable Integer id,
                                                @RequestParam(required = false) String wbsCode,
                                                @RequestParam(required = false) String ceCode,
                                                @RequestParam(required = false) String sapVendorId,
                                                @RequestParam(required = false) String companyName) {
        return authorisedPaymentsProcessor.reprocessAuthorisedPayment(id, wbsCode, ceCode, sapVendorId, companyName);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/payments", method = RequestMethod.POST)
    @ApiOperation(value = "Creates a payment request", notes = "Creates a payment request")
    public @ResponseBody ProjectLedgerEntry create(
        @Valid @RequestBody ProjectLedgerEntry projectLedgerEntry,
        @RequestParam(required = false, defaultValue = "false") boolean runAuthorisedPaymentsJob,
        @RequestParam(required = false, defaultValue = "false") boolean createPaymentInGroup,
        BindingResult bindingResult) {
        ensurePaymentsFeatureIsEnabled();

        verifyBinding("Invalid payment request details!", bindingResult);

        final ProjectLedgerEntry createdProjectLedgerEntry = paymentService.create(projectLedgerEntry);

        if (createPaymentInGroup) {
            List<ProjectLedgerEntry> entries = new ArrayList<>();
            entries.add(createdProjectLedgerEntry);
            paymentService.save(new PaymentGroupEntity(entries));
        }

        if (runAuthorisedPaymentsJob) {
            authorisedPaymentsProcessor.processAuthorisedPayments();
        }

        return createdProjectLedgerEntry;
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_ORG_ADMIN, GLA_SPM})
    @RequestMapping(value = "/payments/{id}/reclaim", method = RequestMethod.POST)
    @ApiOperation(value = "Creates a payment reclaim request", notes = "Creates a payment reclaim request")
    public @ResponseBody ProjectLedgerEntry createReclaim(@PathVariable Integer id,  @RequestBody BigDecimal value) {
        if (!featureStatus.isEnabled(Feature.Reclaims)) {
            throw new ForbiddenAccessException();
        }
        return paymentService.createReclaim(id, value);
    }

    @Secured({OPS_ADMIN, GLA_PM, GLA_SPM, GLA_ORG_ADMIN})
    @RequestMapping(value = "/payments/interest", method = RequestMethod.POST)
    @ApiOperation(value = "Assign interest to multiple payments", notes = "Creates a payment reclaim request")
    public void setReclaimInterest(@RequestBody Map<Integer, BigDecimal> map) {
        if (!featureStatus.isEnabled(Feature.Reclaims)) {
            throw new ForbiddenAccessException();
        }
        paymentService.setReclaimInterestForPayments(map);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/payments/{id}/status", method = RequestMethod.PUT)
    @ApiOperation(value = "updates a payment status", notes = "updates a payment status")
    public void updateStatus(@PathVariable Integer id, @RequestBody String status) {
        ensurePaymentsFeatureIsEnabled();
        paymentService.updateStatus(id, LedgerStatus.valueOf(status));
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM})
    @RequestMapping(value = "/payments/authorise/group/{groupId}", method = RequestMethod.POST)
    @ApiOperation(value = "Authorise a set of pending payment request which belong to the same milestone"
        + "based on groupId")
    public @ResponseBody List<ProjectLedgerEntry> authorise(@PathVariable int groupId) {
        ensurePaymentsFeatureIsEnabled();
        return paymentService.authoriseByGroupId(groupId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/payments/assessInterest/group/{groupId}", method = RequestMethod.PUT)
    @ApiOperation(value = "Records that the interest for this group has been considered")
    public @ResponseBody
    PaymentGroupEntity recordInterestAssessed(@PathVariable int groupId) {
        ensurePaymentsFeatureIsEnabled();
        return paymentService.recordInterestAssessed(groupId);
    }

    @PermissionRequired(VIEW_PAYMENT_HISTORY)
    @RequestMapping(value = "/payments/auditHistory/{paymentId}", method = RequestMethod.GET)
    @ApiOperation(value = "Retrieves audit history for specified payment")
    public @ResponseBody List<PaymentAuditItem> getPaymentAuditItems(@PathVariable int paymentId) {
        return paymentAuditService.getAllAuditItems(paymentId);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM})
    @RequestMapping(value = "/payments/decline/group/{groupId}", method = RequestMethod.POST)
    @ApiOperation(value = "Decline a set of pending payment request which belong to the same projects"
        + "based on paymentGroupId")
    public @ResponseBody
    PaymentGroupEntity decline(@PathVariable Integer groupId, @Valid @RequestBody PaymentGroupEntity paymentGroup) {
        ensurePaymentsFeatureIsEnabled();
        return paymentService.declinePaymentsByGroupId(groupId, paymentGroup);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/payments/uploadTestAcknowledgement", method = RequestMethod.POST)
    @ApiOperation(value = "Upload test invoice acknowledgement file", hidden = true)
    public @ResponseBody String uploadTestAcknowledgement(MultipartFile file) throws Exception {
        String destination = remotePathOutgoingInvoice + "/" + file.getOriginalFilename();
        Session<ChannelSftp.LsEntry> session = sessionFactory.getSession();
        session.write(file.getInputStream(), destination);
        session.close();
        return "File uploaded to destination: " + destination;
    }

    private void ensurePaymentsFeatureIsEnabled() {
        if (!featureStatus.isEnabled(Feature.Payments)) {
            throw new ForbiddenAccessException();
        }
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/payments/ledger", method = RequestMethod.GET)
    @ApiOperation(value = "Get all payment ledger entries", notes = "Get all payment ledger entries")
    public List<ProjectLedgerEntry> getClaimLedgerEntries() {
        return paymentService.getPendingLedgerEntries();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/payments/generateAuthorisedPaymentsReport", method = RequestMethod.GET)
    @ApiOperation(value = "test API to trigger authorised payments report", hidden = true)
    public void triggerAuthorisedPaymentsReport(@RequestParam(required = false) String day) throws IOException {
        ensurePaymentsFeatureIsEnabled();
        if (day == null) {
            dailyPaymentsReportGenerator.generateDailyAuthorisedPaymentsReport();
        } else {
            dailyPaymentsReportGenerator.generateDailyAuthorisedPaymentsReport(OffsetDateTime.parse(day + "T00:00:00+01:00"));
        }
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/payments/scheduler/{schedulerName}/execute", method = RequestMethod.GET)
    @ApiOperation(value = "execute scheduler manually", hidden = true)
    public void executeScheduler(@PathVariable("schedulerName")  String schedulerName) {
       paymentService.executeScheduler(schedulerName);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/payments/{projectLedgerEntryId}/xmlFile", method = RequestMethod.GET,
        produces = "application/xml; text/xml")
    public String download(@PathVariable Integer projectLedgerEntryId) {
        return paymentService.getPaymentLedgerEntry(projectLedgerEntryId).getXmlFile();
    }

}
