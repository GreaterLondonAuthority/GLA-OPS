/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.mapper;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.FeatureStatus;
import uk.gov.london.ops.domain.finance.LedgerSource;
import uk.gov.london.ops.domain.finance.LedgerStatus;
import uk.gov.london.ops.domain.finance.LedgerType;
import uk.gov.london.ops.domain.finance.SapData;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.refdata.FinanceCategory;
import uk.gov.london.ops.domain.refdata.FinanceCategoryStatus;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.mapper.model.MapResult;
import uk.gov.london.ops.mapper.model.SapDataModel;
import uk.gov.london.ops.mapper.model.SapPaymentDataModel;
import uk.gov.london.ops.mapper.model.SapReceiptDataModel;
import uk.gov.london.ops.repository.FinanceCategoryRepository;
import uk.gov.london.ops.repository.ProjectRepository;
import uk.gov.london.ops.service.UserService;
import uk.gov.london.ops.service.finance.PaymentService;
import uk.gov.london.ops.service.project.ProjectService;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static uk.gov.london.ops.util.GlaOpsUtils.createNullIgnoringList;

@Component
public class ProjectLedgerEntryMapper {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    FinanceCategoryRepository financeCategoryRepository;

    @Autowired
    Environment environment;

    @Autowired
    PaymentService paymentService;

    @Autowired
    UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FeatureStatus featureStatus;

    @Value("#{new java.text.SimpleDateFormat('dd/MM/yyyy').parse('${actuals.cutoff.date}')}")
    Date actualsCutoffDate;

    private static final SimpleDateFormat sapDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public MapResult<ProjectLedgerEntry> map(SapData sapData)  {
        SapDataModel model = getModel(sapData);

        Project project = null;
        boolean wbsCodeUsedForIdentification = false;
        if (model.getWBSElement() !=null && featureStatus.isEnabled(FeatureStatus.Feature.ResolveInboundSAPRecordsByWBSCode)) {
            Set<Project> payments = projectRepository.findAllByPaymentsWBSCode(ProjectBlockType.ProjectBudgets.name(), model.getWBSElement());
            Set<Project> receipts = projectRepository.findAllByPaymentsWBSCode(ProjectBlockType.Receipts.name(), model.getWBSElement());
            wbsCodeUsedForIdentification = true;
            if (payments.size() + receipts.size() > 1) {
                return new MapResult<>("More than one project was found with WBS Code: " + model.getWBSElement(), false);
            }

            if (model instanceof SapPaymentDataModel) {
                project = payments.isEmpty() ? null : payments.iterator().next();
            } else if (model instanceof SapReceiptDataModel) {
                project = receipts.isEmpty() ? null : receipts.iterator().next();
            }
            if (project == null) {
                return new MapResult<>("Could not find project with WBS Code " + model.getWBSElement() , false);
            }
        } else {
            if (StringUtils.isNumeric(model.getPCSProjectNumber())) {
                project = projectRepository.findFirstByLegacyProjectCode(Integer.valueOf(model.getPCSProjectNumber()));
            }
            if (project == null) {
                return new MapResult<>("Could not find project with PCS ID "+model.getPCSProjectNumber()  , false);
            }

        }


        NamedProjectBlock block = project.getSingleLatestBlockOfType(sapData.isPayment() ? ProjectBlockType.ProjectBudgets : ProjectBlockType.Receipts);
        if (block == null) {
            throw new IllegalStateException("Could not find relevant block for project "+project.getId());
        }

        if (project.isAutoApproval()) {
            // won't actually be cloned unless it's required.
            block = projectService.getBlockAndLock(project, block, false);
            if (block == null) {
                throw new RuntimeException("Null block " + block);
            }
            block.setLastModified(environment.now());
            block.setModifiedBy(userService.getSystemUserName());
        }

        if (sapData.isPayment()) {
            return toResult((SapPaymentDataModel) model, project, block, sapData, wbsCodeUsedForIdentification);
        }
        else {
            return toResult((SapReceiptDataModel) model, project, block, sapData);
        }
    }

    SapDataModel getModel(SapData sapData) {
        try {
            if (sapData.isPayment()) {
                return new XmlMapper().readValue(sapData.getContent(), SapPaymentDataModel.class);
            }
            else {
                return new XmlMapper().readValue(sapData.getContent(), SapReceiptDataModel.class);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to process XML relating to SAP data "+sapData.getId(), e);
        }
    }

    MapResult<ProjectLedgerEntry> toResult(SapPaymentDataModel model, Project project, NamedProjectBlock block, SapData sapData, boolean wbsCodeUsedForIdentification)  {
        FinanceCategory category = financeCategoryRepository.findByCeCode(Integer.parseInt(model.getAccountCode()));

        if (category == null) {
            return new MapResult<>("Unrecognised SAP spend category code: "+model.getAccountDescription(), false);
        }

        if (isBeforeCutoff(model.getPaymentDate())) {
            return new MapResult<>("ignored as date ("+model.getPaymentDate()+") before cutoff date");
        }

        Integer year = Integer.parseInt(model.getPaymentDate().split("/")[2]);
        Integer month = Integer.parseInt(model.getPaymentDate().split("/")[1]);

        SpendType spendType = null;
        if (wbsCodeUsedForIdentification) {
            ProjectBudgetsBlock projectBudgetsBlock = (ProjectBudgetsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.ProjectBudgets);
            if (projectBudgetsBlock != null) {
                for (WbsCode wbsCode : projectBudgetsBlock.getWbsCodes()) {
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
            return new MapResult<>("Project "+project.getId()+ " has no WBS for phase " + model.getPCSPhaseNumber(), false);
        }

        ProjectLedgerEntry ple = new ProjectLedgerEntry(project.getId(), block.getId(), year, month, LedgerStatus.ACTUAL,
                LedgerType.PAYMENT, spendType,
                category.getId(), category.getText(), model.getPaidAmount() == null ? BigDecimal.ZERO : model.getPaidAmount().negate());
        ple.setLedgerSource(LedgerSource.SAP);
        ple.setReference(model.getPaymentReference());
        ple.setPcsPhaseNumber(model.getPCSPhaseNumber());
        ple.setVendorName(model.getPayeeName());
        ple.setTransactionDate(model.getPaymentDate());
        ple.setSapCategoryCode(model.getAccountCode());
        ple.setDescription(model.getPaymentDescription());
        ple.setCostCentreCode(model.getCostCenterCode());
        ple.setTransactionNumber(model.getOrderNumber());
        ple.setSapDataId(sapData.getId());
        return new MapResult<>(ple);
    }

    MapResult<ProjectLedgerEntry> toResult(SapReceiptDataModel model, Project project, NamedProjectBlock block, SapData sapData) {
        FinanceCategory category = financeCategoryRepository.findByCeCode(Integer.parseInt(model.getAccountCode()));

        if (category == null) {
            return new MapResult<>("Unrecognised SAP receipt category code: "+model.getAccountCode(), false);
        }

        if (category.getReceiptStatus().equals(FinanceCategoryStatus.Hidden)) {
            // TODO : in an upcoming story change this to process hidden entries
            return new MapResult<>("ignored receipt category "+category.getId()+" - "+category.getText());
        }

        if (isBeforeCutoff(model.getReceiptDate())) {
            return new MapResult<>("ignored as date ("+model.getReceiptDate()+") before cutoff date");
        }

        Integer year = Integer.parseInt(model.getReceiptDate().split("/")[2]);
        Integer month = Integer.parseInt(model.getReceiptDate().split("/")[1]);

        ProjectLedgerEntry ple = new ProjectLedgerEntry(project.getId(), block.getId(), year, month, LedgerStatus.ACTUAL,
                LedgerType.RECEIPT, null, category.getId(), category.getText(), model.getReceiptAmount() == null ? BigDecimal.ZERO : model.getReceiptAmount().negate());
        ple.setLedgerSource(LedgerSource.SAP);
        ple.setVendorName(model.getPayerName());
        ple.setTransactionDate(model.getReceiptDate());
        ple.setReference(model.getReceiptReference());
        ple.setCostCentreCode(model.getCostCenterCode());
        ple.setSapCategoryCode(model.getAccountCode());
        ple.setTransactionNumber(model.getInvoiceNumber());
        ple.setInvoiceDate(model.getInvoiceDate());
        ple.setPcsProjectNumber(Integer.valueOf(model.getPCSProjectNumber()));
        ple.setSapDataId(sapData.getId());
        return new MapResult<>(ple);
    }

    SpendType getSpendType(Project project, String pcsPhaseNumber) {
        for (WbsCode wbsCode: project.getProjectBudgetsBlock().getWbsCodes()) {
            if (wbsCode.getCode().endsWith(pcsPhaseNumber)) {
                return SpendType.valueOf(wbsCode.getType());
            }
        }
        return null;
    }

    private boolean isBeforeCutoff(String dateString) {
        try {
            return sapDateFormat.parse(dateString).before(actualsCutoffDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Not a valid date string: " + dateString);
        }
    }

    public List<ProjectLedgerEntry> map(final Project project,
                                        final ProjectMilestonesBlock milestoneBlock,
                                        final Integer milestoneId) {

        final Milestone milestone = milestoneBlock.getMilestoneById(milestoneId);
        List<ProjectLedgerEntry> entries = createNullIgnoringList();
        Long milestoneClaimedGrant = milestoneBlock.getMilestoneGrantClaimed(milestoneId);
        entries.add(generatePaymentForClaim(project, milestone, milestoneClaimedGrant, GrantType.Grant));
        entries.add(generatePaymentForClaim(project, milestone, milestone.getClaimedRcgf(), GrantType.RCGF));
        entries.add(generatePaymentForClaim(project, milestone, milestone.getClaimedDpf(), GrantType.DPF));
        return entries;
    }

    ProjectLedgerEntry generatePaymentForClaim(Project project, Milestone milestone, Long claimValue, GrantType grantType) {
        if (claimValue == null || claimValue == 0) {
            return null;
        }
        return paymentService.createPayment(project,
                getLedgerType(grantType),
                LedgerStatus.Pending,
                "Milestone",
                milestone.getPaymentSubType(),
                new BigDecimal(-claimValue),
                environment.now().getYear(),
                environment.now().getMonthValue(),
                milestone.getExternalId(),
                LedgerSource.WebUI);
    }

    public ProjectLedgerEntry generateSupplementalPaymentForMilestone(Project project, Milestone milestone, Long claimValue) {
        if (claimValue == null || claimValue == 0) {
            return null;
        }

        return paymentService.createPayment(
                project,
                getLedgerType(GrantType.Grant),
                LedgerStatus.Pending,
                "Supplementary",
                milestone.getPaymentSubType(),
                new BigDecimal(claimValue),
                environment.now().getYear(),
                environment.now().getMonthValue(),
                milestone.getExternalId(),
                LedgerSource.WebUI);
    }

   public ProjectLedgerEntry generateReclaimPaymentForMilestone(Project project, Milestone milestone, Long claimValue, ProjectLedgerEntry entry, LedgerType type) {
        if (claimValue == null || claimValue == 0) {
            return null;
        }

       ProjectLedgerEntry reclaim = paymentService.createReclaim(
               project,
               entry,
               type,
               LedgerStatus.Pending,
               "Milestone",
               "Reclaimed " + milestone.getPaymentSubType(),
               new BigDecimal(claimValue),
               environment.now().getYear(),
               environment.now().getMonthValue(),
               milestone.getExternalId(),
               LedgerSource.WebUI);
        return reclaim;
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
                throw new ValidationException("unknown grant type: "+grantType);
        }
    }

}
