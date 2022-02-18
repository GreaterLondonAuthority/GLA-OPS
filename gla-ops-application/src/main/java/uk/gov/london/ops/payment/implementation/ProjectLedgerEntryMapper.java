/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.MapResult;
import uk.gov.london.ops.framework.enums.GrantType;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.payment.*;
import uk.gov.london.ops.payment.implementation.sap.model.SapDataModel;
import uk.gov.london.ops.payment.implementation.sap.model.SapPaymentDataModel;
import uk.gov.london.ops.payment.implementation.sap.model.SapReceiptDataModel;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectService;
import uk.gov.london.ops.project.WbsCodeEntity;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.budget.ProjectBudgetsBlock;
import uk.gov.london.ops.project.milestone.Milestone;
import uk.gov.london.ops.project.milestone.ProjectMilestonesBlock;
import uk.gov.london.ops.project.template.domain.GrantSourceTemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateBlock;
import uk.gov.london.ops.refdata.FinanceCategory;
import uk.gov.london.ops.refdata.PaymentSource;
import uk.gov.london.ops.refdata.RefDataService;
import uk.gov.london.ops.user.UserServiceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static uk.gov.london.common.GlaUtils.createNullIgnoringList;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.SUPPLEMENTARY_PAYMENT;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.getPaymentSourceFromLedgerType;
import static uk.gov.london.ops.payment.implementation.di.PaymentDataInitialiser.TEST_INVALID_SAP_DATA_CONTENT;
import static uk.gov.london.ops.refdata.PaymentSourceKt.*;

@Component
public class ProjectLedgerEntryMapper {

    public static final String RECLAIMED_PREFIX = "Reclaimed ";
    public static final String BESPOKE_PREFIX = "Bespoke ";
    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    Environment environment;

    @Autowired
    PaymentService paymentService;

    @Autowired
    RefDataService refDataService;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FeatureStatus featureStatus;

    @Value("#{new java.text.SimpleDateFormat('dd/MM/yyyy').parse('${actuals.cutoff.date}')}")
    Date actualsCutoffDate;

    public MapResult<ProjectLedgerEntry> map(SapData sapData) {
        SapDataModel model = getModel(sapData);

        Project project = null;
        boolean wbsCodeUsedForIdentification = false;
        if (model.getWBSElement() != null && featureStatus.isEnabled(Feature.ResolveInboundSAPRecordsByWBSCode)) {
            Set<Project> payments = projectService.findAllByPaymentsWBSCode(ProjectBlockType.ProjectBudgets.name(),
                    model.getWBSElement());
            Set<Project> receipts = projectService.findAllByPaymentsWBSCode(ProjectBlockType.Receipts.name(),
                    model.getWBSElement());
            wbsCodeUsedForIdentification = true;
            if (payments.size() + receipts.size() > 1) {
                List<Integer> projectIds = new ArrayList<>();
                projectIds.addAll(payments.stream().map(Project::getId).collect(Collectors.toList()));
                projectIds.addAll(receipts.stream().map(Project::getId).collect(Collectors.toList()));
                String message = String.format("Multiple projects found with WBS Code %s: %s", model.getWBSElement(),
                        StringUtils.join(projectIds, ", "));
                if (message.length() > 255) {
                    message = message.substring(0, 255);
                }
                return new MapResult<>(message, false);
            }

            if (model instanceof SapPaymentDataModel) {
                project = payments.isEmpty() ? null : payments.iterator().next();
            } else if (model instanceof SapReceiptDataModel) {
                project = receipts.isEmpty() ? null : receipts.iterator().next();
            }
            if (project == null) {
                return new MapResult<>("Could not find project with WBS Code " + model.getWBSElement(), false);
            }
        } else {
            if (StringUtils.isNumeric(model.getPCSProjectNumber())) {
                project = projectService.findFirstByLegacyProjectCode(Integer.valueOf(model.getPCSProjectNumber()));
            }
            if (project == null) {
                return new MapResult<>("Could not find project with PCS ID " + model.getPCSProjectNumber(), false);
            }
        }

        NamedProjectBlock block = project.getSingleLatestBlockOfType(
                sapData.isPayment() ? ProjectBlockType.ProjectBudgets : ProjectBlockType.Receipts);
        if (block == null) {
            throw new IllegalStateException("Could not find relevant block for project " + project.getId());
        }

        if (!project.getStateModel().isApprovalRequired()) {
            // won't actually be cloned unless it's required.
            block = projectService.getBlockAndLock(project, block, false);
            if (block == null) {
                throw new RuntimeException("Null block");
            }
            block.setLastModified(environment.now());
            block.setModifiedBy(userService.getSystemUserName());
        }

        if (sapData.isPayment()) {
            return toResult((SapPaymentDataModel) model, project, block, sapData, wbsCodeUsedForIdentification);
        } else {
            return toResult((SapReceiptDataModel) model, project, block, sapData);
        }
    }

    public List<ProjectLedgerEntry> map(final Project project,
                                        final ProjectMilestonesBlock milestoneBlock,
                                        final Integer milestoneId) {

        final Milestone milestone = milestoneBlock.getMilestoneById(milestoneId);
        List<ProjectLedgerEntry> entries = createNullIgnoringList();
        Map<String, PaymentSource> paymentSourceMap = refDataService.getPaymentSourceMap();

        Set<String> paymentSources = new HashSet<>();
        TemplateBlock singleBlockByType = project.getTemplate().getSingleBlockByType(ProjectBlockType.GrantSource);
        if (singleBlockByType instanceof GrantSourceTemplateBlock) {
            GrantSourceTemplateBlock grant = (GrantSourceTemplateBlock) singleBlockByType;
            paymentSources = grant.getPaymentSources();
        } else {
            paymentSources.add(GRANT);
            paymentSources.add(RCGF);
            paymentSources.add(DPF);
        }

        Map<String, PaymentSource> relevantSources = new HashMap<>();

        for (String paymentSource : paymentSources) {
            relevantSources.put(paymentSource, paymentSourceMap.get(paymentSource));
        }
        Long milestoneClaimedGrant = milestoneBlock.getMilestoneGrantClaimed(milestoneId);
        entries.add(generatePaymentForClaim(project, milestone, milestoneClaimedGrant, GrantType.Grant,
                getPaymentSource(GrantType.Grant, relevantSources)));
        entries.add(generatePaymentForClaim(project, milestone, milestone.getClaimedRcgf(), GrantType.RCGF,
                getPaymentSource(GrantType.RCGF, relevantSources)));
        entries.add(generatePaymentForClaim(project, milestone, milestone.getClaimedDpf(), GrantType.DPF,
                getPaymentSource(GrantType.DPF, relevantSources)));
        return entries;
    }

    public SapDataModel getModel(SapData sapData) {
        return this.getModel(sapData, true);
    }

    public SapDataModel getModel(SapData sapData, boolean failOnError) {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            if (sapData.isPayment()) {
                return mapper.readValue(sapData.getContent(), SapPaymentDataModel.class);
            } else {
                return mapper.readValue(sapData.getContent(), SapReceiptDataModel.class);
            }
        } catch (IOException e) {
            if (failOnError && !isTestInvalidSapData(sapData)) {
                throw new RuntimeException("Unable to process XML relating to SAP data " + sapData.getId(), e);
            } else {
                if (!isTestInvalidSapData(sapData)) {
                    log.error("Unable to process XML relating to SAP data " + sapData.getId(), e);
                }
                if (sapData.isPayment()) {
                    return new SapPaymentDataModel();
                } else {
                    return new SapReceiptDataModel();
                }
            }
        }
    }

    private boolean isTestInvalidSapData(SapData sapData) {
        return environment.isTestEnvironment() && TEST_INVALID_SAP_DATA_CONTENT.equals(sapData.getContent());
    }

    MapResult<ProjectLedgerEntry> toResult(SapPaymentDataModel model, Project project, NamedProjectBlock block, SapData sapData,
                                           boolean wbsCodeUsedForIdentification)  {
        FinanceCategory category = refDataService.getFinanceCategoryByCeCode(Integer.parseInt(model.getAccountCode()));

        if (category == null) {
            return new MapResult<>("Unrecognised SAP spend category code: " + model.getAccountDescription(),
                    false);
        }

        if (isBeforeCutoff(model.getDate())) {
            return new MapResult<>("ignored as date (" + model.getDate() + ") before cutoff date");
        }

        Integer year = model.getYear();
        Integer month = model.getMonth();

        SpendType spendType = null;
        if (wbsCodeUsedForIdentification) {
            ProjectBudgetsBlock projectBudgetsBlock = (ProjectBudgetsBlock)
                    project.getSingleLatestBlockOfType(ProjectBlockType.ProjectBudgets);
            if (projectBudgetsBlock != null) {
                for (WbsCodeEntity wbsCode : projectBudgetsBlock.getWbsCodes()) {
                    if (wbsCode.getCode().equalsIgnoreCase(model.getWBSElement()) && wbsCode.getType() != null) {
                        spendType = SpendType.valueOf(wbsCode.getType());
                        break;
                    }
                }
            }
        } else {
            spendType = getSpendType(project, model.getPCSPhaseNumber());
        }

        if (spendType == null) {
            return new MapResult<>("Project " + project.getId() + " has no WBS for phase " + model.getPCSPhaseNumber(),
                    false);
        }

        ProjectLedgerEntry ple = new ProjectLedgerEntry(project.getId(), block.getId(), year, month, LedgerStatus.ACTUAL,
                LedgerType.PAYMENT, spendType, category.getId(), category.getText(),
                model.getPaidAmount() == null ? BigDecimal.ZERO : model.getPaidAmount().negate());
        ple.setLedgerSource(LedgerSource.SAP);
        ple.setReference(model.getPaymentReference());
        ple.setPcsPhaseNumber(model.getPCSPhaseNumber());
        ple.setVendorName(model.getPayeeName());
        ple.setTransactionDate(model.getDate());
        ple.setSapCategoryCode(model.getAccountCode());
        ple.setDescription(model.getPaymentDescription());
        ple.setCostCentreCode(model.getCostCenterCode());
        ple.setTransactionNumber(model.getOrderNumber());
        ple.setSapDataId(sapData.getId());
        return new MapResult<>(ple);
    }

    MapResult<ProjectLedgerEntry> toResult(SapReceiptDataModel model, Project project, NamedProjectBlock block, SapData sapData) {
        FinanceCategory category = refDataService.getFinanceCategoryByCeCode(Integer.parseInt(model.getAccountCode()));

        if (category == null) {
            return new MapResult<>("Unrecognised SAP receipt category code: " + model.getAccountCode(), false);
        }

        if (isBeforeCutoff(model.getDate())) {
            return new MapResult<>("ignored as date (" + model.getDate() + ") before cutoff date");
        }

        Integer year = model.getYear();
        Integer month = model.getMonth();

        ProjectLedgerEntry ple = new ProjectLedgerEntry(project.getId(), block.getId(), year, month, LedgerStatus.ACTUAL,
                LedgerType.RECEIPT, null, category.getId(), category.getText(), model.getReceiptAmount() == null
                ? BigDecimal.ZERO : model.getReceiptAmount().negate());
        ple.setLedgerSource(LedgerSource.SAP);
        ple.setVendorName(model.getPayerName());
        ple.setTransactionDate(model.getDate());
        ple.setReference(model.getReceiptReference());
        ple.setCostCentreCode(model.getCostCenterCode());
        ple.setSapCategoryCode(model.getAccountCode());
        ple.setTransactionNumber(model.getInvoiceNumber());
        ple.setInvoiceDate(model.getInvoiceDate());
        try {
            ple.setPcsProjectNumber(Integer.valueOf(model.getPCSProjectNumber()));
        } catch (NumberFormatException e) {
            log.warn("Cannot parse PCS project number " + model.getPCSProjectNumber());
        }
        ple.setSapDataId(sapData.getId());
        return new MapResult<>(ple);
    }

    SpendType getSpendType(Project project, String pcsPhaseNumber) {
        for (WbsCodeEntity wbsCode: project.getProjectBudgetsBlock().getWbsCodes()) {
            if (wbsCode.getCode().endsWith(pcsPhaseNumber)) {
                return SpendType.valueOf(wbsCode.getType());
            }
        }
        return null;
    }

    public boolean isBeforeCutoff(String dateString) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse(dateString).before(actualsCutoffDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Not a valid date string: " + dateString);
        }
    }



    ProjectLedgerEntry generatePaymentForClaim(Project project, Milestone milestone, Long claimValue, GrantType grantType,
                                               String paymentSource) {
        if (claimValue == null || claimValue == 0) {
            return null;
        }
        return paymentService.createPayment(project,
                project.getMilestonesBlock().getId(),
                getLedgerType(grantType),
                paymentSource,
                LedgerStatus.Pending,
                "Milestone",
                milestone.getPaymentSubType(),
                new BigDecimal(-claimValue),
                environment.now().getYear(),
                environment.now().getMonthValue(),
                milestone.getExternalId(),
                LedgerSource.WebUI);
    }

    String getPaymentSource(GrantType grantType, Map<String, PaymentSource> paymentSourceMap) {
        return paymentSourceMap.values()
                .stream()
                .filter(p -> p.getGrantType().equals(grantType))
                .map(PaymentSource::getName).findFirst().orElse(grantType.name());
    }

    public ProjectLedgerEntry generateSupplementalPaymentForMilestone(Project project, Milestone milestone, Long claimValue) {
        if (claimValue == null || claimValue == 0) {
            return null;
        }

        return paymentService.createPayment(
                project,
                project.getMilestonesBlock().getId(),
                LedgerType.PAYMENT,
                GRANT,
                LedgerStatus.Pending,
                SUPPLEMENTARY_PAYMENT,
                milestone.getPaymentSubType(),
                new BigDecimal(claimValue),
                environment.now().getYear(),
                environment.now().getMonthValue(),
                milestone.getExternalId(),
                LedgerSource.WebUI);
    }

   public ProjectLedgerEntry generateReclaimPaymentForMilestone(Project project, Milestone milestone, Long claimValue,
                                                                ProjectLedgerEntry entry, LedgerType type) {
        if (claimValue == null || claimValue == 0) {
            return null;
        }

       return paymentService.createReclaim(
               project,
               entry,
               type,
               getPaymentSourceFromLedgerType(type),
               LedgerStatus.Pending,
               "Milestone",
               RECLAIMED_PREFIX + milestone.getPaymentSubType(),
               new BigDecimal(claimValue),
               environment.now().getYear(),
               environment.now().getMonthValue(),
               milestone.getExternalId(),
               LedgerSource.WebUI);
   }

    LedgerType getLedgerType(GrantType grantType) {
        switch (grantType) {
            case Grant:
                return LedgerType.PAYMENT;
            case RCGF:
                return LedgerType.RCGF;
            case DPF:
                return LedgerType.DPF;
            default:
                throw new ValidationException("unknown grant type: " + grantType);
        }
    }

    public void setFeatureStatus(FeatureStatus featureStatus) {
        this.featureStatus = featureStatus;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public void setActualsCutoffDate(Date actualsCutoffDate) {
        this.actualsCutoffDate = actualsCutoffDate;
    }
}
