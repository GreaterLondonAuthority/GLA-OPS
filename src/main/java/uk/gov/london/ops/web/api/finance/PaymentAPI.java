/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api.finance;

import com.jcraft.jsch.ChannelSftp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.ops.FeatureStatus;
import uk.gov.london.ops.domain.finance.LedgerStatus;
import uk.gov.london.ops.domain.finance.LedgerType;
import uk.gov.london.ops.domain.finance.PaymentGroup;
import uk.gov.london.ops.domain.finance.PaymentSummary;
import uk.gov.london.ops.domain.project.ProjectLedgerEntry;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ForbiddenAccessException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.service.UserService;
import uk.gov.london.ops.service.finance.AuthorisedPaymentsProcessor;
import uk.gov.london.ops.service.finance.AuthorisedPaymentsReportGenerator;
import uk.gov.london.ops.service.finance.PaymentService;

import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static uk.gov.london.ops.util.GlaOpsUtils.parseDateString;

@RestController
@RequestMapping("/api/v1")
@Api(description="Payment request API")
public class PaymentAPI {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    PaymentService paymentService;

    @Autowired
    UserService userService;

    @Autowired
    AuthorisedPaymentsProcessor authorisedPaymentsProcessor;

    @Autowired
    AuthorisedPaymentsReportGenerator authorisedPaymentsReportGenerator;

    @Autowired
    FeatureStatus featureStatus;

    @Autowired
    SessionFactory<ChannelSftp.LsEntry> sessionFactory;

    @Value("${sap.ftp.remote.path.outgoing.invoices}")
    String remotePathOutgoingInvoice;

    @Secured({Role.OPS_ADMIN, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN} )
    @RequestMapping(value = "/payments", method = RequestMethod.GET)
    @ApiOperation(value = "Get all payments with optional filter", notes = "Get all payments")
    // TODO when getAll is called, pass PaymentFilterOption.ALL.statuses() (for updating tests)
    public Page<PaymentSummary> getAll(
            @RequestParam(name = "project", required = false) String projectIdOrName,
            @RequestParam(name = "organisation", required = false) String organisationName,
            @RequestParam(required = false) List<LedgerType> relevantSources,
            @RequestParam(required = false) List<LedgerStatus> relevantStatuses,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> relevantProgrammes,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) List<String> paymentDirection,
            Pageable pageable
            ) {

        OffsetDateTime from = null;
        if(fromDate != null) {
            from = parseDateString(fromDate + " 00:00", "dd/MM/yyyy HH:mm");
        }

        OffsetDateTime to = null;
        if(toDate != null) {
            // Looking for any date before midnight (00:00 of the following day)
            to = parseDateString(toDate + " 00:00", "dd/MM/yyyy HH:mm").plusDays(1);
        }

        ensurePaymentsFeatureIsEnabled();
        boolean noSourceSpecified = relevantSources == null || relevantSources.isEmpty();

        if (noSourceSpecified) {
            relevantSources = LedgerTypeFilterOption.ALL_PAYMENTS.getRelevantStatusesAsList();
        }

        if (relevantStatuses == null || relevantStatuses.isEmpty()) {
            relevantStatuses = PaymentFilterOption.ALL_PAYMENTS.getRelevantStatusesAsList();
        }
//        Pageable pageable = new PageRequest(0, 20);

        Page<PaymentSummary> all = paymentService.findAll(projectIdOrName,
                organisationName,
                relevantSources,
                relevantStatuses,
                categories,
                relevantProgrammes,
                from,
                to,
                paymentDirection,
                pageable);
//        if (!noSourceSpecified) { // temp fix whilst we wait for reclaim filtering
//            all.removeIf(PaymentSummary::isReclaim);
//        }
        return all;
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN} )
    @RequestMapping(value = "/paymentGroups", method = RequestMethod.GET)
    @ApiOperation(value = "Get all payments groups with optional filter", notes = "Get all payments")
    public List<PaymentGroup> getPaymentGroups(@RequestParam(required = false) PaymentFilterOption status) {
        ensurePaymentsFeatureIsEnabled();
        return paymentService.findAllPaymentGroupsByStatus(status);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/paymentGroups/{groupId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get a payment group by id", notes = "Get payment by id")
    public PaymentGroup getPaymentGroupById(@PathVariable("groupId") final Integer groupId) {
        ensurePaymentsFeatureIsEnabled();
        return paymentService.getGroupById(groupId);
    }


    @Secured({Role.OPS_ADMIN, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN} )
    @RequestMapping(value = "/paymentGroups/payment/{paymentId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get a payment group by the id of one of it's payments", notes = "Get payment group by id payment id")
    public PaymentGroup getPaymentGroupByPaymentId(@PathVariable("paymentId") final Integer paymentId) {
        ensurePaymentsFeatureIsEnabled();
        return paymentService.getGroupByPaymentId(paymentId);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/payments/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "Get a single payment request", notes = "Get a payment request")
    public PaymentSummary getById(@PathVariable Integer id) {
        ensurePaymentsFeatureIsEnabled();
        return paymentService.getById(id);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/payments", method = RequestMethod.POST)
    @ApiOperation(value = "Creates a payment request", notes = "Creates a payment request")
    public @ResponseBody ProjectLedgerEntry create(@Valid @RequestBody ProjectLedgerEntry ProjectLedgerEntry,
                                               @RequestParam(required = false, defaultValue = "false") boolean runAuthorisedPaymentsJob,
                                               BindingResult bindingResult) {
        ensurePaymentsFeatureIsEnabled();

        if ((bindingResult != null) && bindingResult.hasErrors()) {
            throw new ValidationException("Invalid payment request details!", bindingResult.getFieldErrors());
        }

        final ProjectLedgerEntry createdProjectLedgerEntry = paymentService.create(ProjectLedgerEntry);

        if (runAuthorisedPaymentsJob) {
            authorisedPaymentsProcessor.processAuthorisedPayments();
        }

        return createdProjectLedgerEntry;
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_PM, Role.GLA_ORG_ADMIN, Role.GLA_SPM} )
    @RequestMapping(value = "/payments/{id}/reclaim", method = RequestMethod.POST)
    @ApiOperation(value = "Creates a payment reclaim request", notes = "Creates a payment reclaim request")
    public @ResponseBody ProjectLedgerEntry createReclaim(@PathVariable Integer id,  @RequestBody BigDecimal value) {
        if (!featureStatus.isEnabled(FeatureStatus.Feature.Reclaims)) {
            throw new ForbiddenAccessException();
        }
        return paymentService.createReclaim(id, value);

    }

    @Secured({Role.OPS_ADMIN, Role.GLA_PM, Role.GLA_SPM, Role.GLA_ORG_ADMIN})
    @RequestMapping(value = "/payments/{id}/interest", method = RequestMethod.POST)
    @ApiOperation(value = "Creates a payment reclaim request", notes = "Creates a payment reclaim request")
    public @ResponseBody ProjectLedgerEntry setReclaimInterest(@PathVariable Integer id,  @RequestBody BigDecimal value) {
        if (!featureStatus.isEnabled(FeatureStatus.Feature.Reclaims)) {
            throw new ForbiddenAccessException();
        }
        return paymentService.setReclaimInterest(id, value);

    }

    @Secured({Role.OPS_ADMIN, Role.GLA_PM, Role.GLA_SPM, Role.GLA_ORG_ADMIN})
    @RequestMapping(value = "/payments/interest", method = RequestMethod.POST)
    @ApiOperation(value = "Assign interest to multiple payments", notes = "Creates a payment reclaim request")
    public @ResponseBody void setReclaimInterest(@RequestBody Map<Integer, BigDecimal> map) {
        if (!featureStatus.isEnabled(FeatureStatus.Feature.Reclaims)) {
            throw new ForbiddenAccessException();
        }

        paymentService.setReclaimInterestForPayments(map);

    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/payments/{id}/status", method = RequestMethod.PUT)
    @ApiOperation(value = "Authorise a pending payment request", notes = "Authorise a pending payment request")
    public @ResponseBody ProjectLedgerEntry authorise(@PathVariable Integer id,
                                                  @RequestParam(required = false, defaultValue = "false") boolean runAuthorisedPaymentsJob,
                                                  @RequestBody String status) {
        ensurePaymentsFeatureIsEnabled();

        ProjectLedgerEntry ProjectLedgerEntry = paymentService.setStatus(id, status, userService.currentUser());

        if (runAuthorisedPaymentsJob) {
            authorisedPaymentsProcessor.processAuthorisedPayments();
        }

        return ProjectLedgerEntry;
    }


    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM})
    @RequestMapping(value = "/payments/authorise/group/{groupId}", method = RequestMethod.POST)
    @ApiOperation(value = "Authorise a set of pending payment request which belong to the same milestone" +
            "based on groupId")
    public @ResponseBody List<ProjectLedgerEntry> authorise(@PathVariable int groupId) {
        ensurePaymentsFeatureIsEnabled();
        return paymentService.authoriseByGroupId(groupId);
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM})
    @RequestMapping(value = "/payments/decline/group/{groupId}", method = RequestMethod.POST)
    @ApiOperation(value = "Decline a set of pending payment request which belong to the same projects" +
            "based on paymentGroupId")
    public @ResponseBody PaymentGroup decline(@PathVariable Integer groupId, @Valid @RequestBody PaymentGroup paymentGroup) {
        ensurePaymentsFeatureIsEnabled();
        return paymentService.declinePaymentsByGroupId(groupId, paymentGroup);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/payments/uploadTestAcknowledgement", method = RequestMethod.POST)
    @ApiOperation(value = "Upload test invoice acknowledgement file", hidden = true)
    public @ResponseBody String uploadTestAcknowledgement(MultipartFile file) throws Exception {
        String destination = remotePathOutgoingInvoice+"/"+file.getOriginalFilename();
        Session<ChannelSftp.LsEntry> session = sessionFactory.getSession();
        session.write(file.getInputStream(), destination);
        session.close();
        return "File uploaded to destination: "+destination;
    }

    private void ensurePaymentsFeatureIsEnabled() {
        if (!featureStatus.isEnabled(FeatureStatus.Feature.Payments)) {
            throw new ForbiddenAccessException();
        }
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/payments/ledger", method = RequestMethod.GET)
    @ApiOperation(value = "Get all payment ledger entries", notes = "Get all payment ledger entries")
    public List<ProjectLedgerEntry> getClaimLedgerEntries() {
        return paymentService.getPendingLedgerEntries();
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/payments/generateAuthorisedPaymentsReport", method = RequestMethod.GET)
    @ApiOperation(value = "test API to trigger authorised payments report", hidden = true)
    public void triggerAuthorisedPaymentsReport(@RequestParam(required = false) String day) throws IOException {
        ensurePaymentsFeatureIsEnabled();
        if (day == null) {
            authorisedPaymentsReportGenerator.generateDailyAuthorisedPaymentsReport();
        }
        else {
            authorisedPaymentsReportGenerator.generateDailyAuthorisedPaymentsReport(OffsetDateTime.parse(day+"T00:00:00+01:00"));
        }
    }

}