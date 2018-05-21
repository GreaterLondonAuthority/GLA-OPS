/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.domain.finance.LedgerType;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.organisation.OrganisationGroup;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.refdata.TenureType;
import uk.gov.london.ops.domain.template.MilestonesTemplateBlock;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.service.OrganisationGroupService;
import uk.gov.london.ops.service.OrganisationService;
import uk.gov.london.ops.service.finance.FinanceService;
import uk.gov.london.ops.service.finance.FinancialCalendar;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.london.ops.util.GlaOpsUtils.nullSafeAdd;

@Component
public class AffordableHousingReportMapper {

    Logger log = LoggerFactory.getLogger(getClass());

    public static final String GLA_OPS_PROJECT_ID = "GLA Ops Project Id.";
    public static final String PROJECT_ADDRESS = "Project Address";
    public static final String PROJECT_TITLE = "Project Title";
    public static final String PROJECT_STATUS = "Project Status";
    public static final String GLA_PROGRAMME_NAME = "GLA Programme Name";
    public static final String BIDDING_ROUTE_PROJECT_TYPE = "Bidding Route (Project Type)";
    public static final String BOROUGH = "Borough";
    public static final String LEAD_ORGANISATION_CODE = "Lead Organisation Code";
    public static final String LEAD_ORGANISATION_NAME = "Lead Organisation Name";
    public static final String DEVELOPING_ORGANISATION_CODE = "Developing Organisation Code";
    public static final String DEVELOPING_ORGANISATION_NAME = "Developing Organisation Name";
    public static final String NEW_BUILD_NUMBER = "New Build (number)";
    public static final String REFURB_NUMBER = "Refurb (number)";
    public static final String PROCESSING_ROUTE = "Processing Route";
    public static final String PLANNING_STATUS = "Planning Status";
    public static final String PROJECT_PREVIOUSLY_FUNDED = "Project Previously Funded?";
    public static final String AFFORDABLE_RENT_AS_OF_MARKET_RENT = "Affordable Rent as % of Market Rent";
    public static final String HABITABLE_ROOM_Y_N = "40% habitable room? (Y/N)";
    public static final String LAR_TOTAL_UNITS = "LAR Total Units";
    public static final String LAR_NIL_GRANT_UNITS = "LAR Nil Grant Units";
    public static final String LAR_ADDITIONAL_AFF_UNITS = "LAR Additional Aff Units";
    public static final String LAR_S_106_UNITS = "LAR S106 Grant Units";
    public static final String LAR_SUPPORTED_HSG_UNITS = "LAR Supported Hsg Units";
    public static final String LLR_TOTAL_UNITS = "LLR Total Units";
    public static final String LLR_NIL_GRANT_UNITS = "LLR Nil Grant Units";
    public static final String LLR_ADDITIONAL_AFF_UNITS = "LLR Additional Aff Units";
    public static final String LLR_S_106_UNITS = "LLR S106 Grant Units";
    public static final String LLR_SUPPORTED_HSG_UNITS = "LLR Supported Hsg Units";
    public static final String LSO_TOTAL_UNITS = "LSO Total Units";
    public static final String LSO_NIL_GRANT_UNITS = "LSO Nil Grant Units";
    public static final String LSO_ADDITIONAL_AFF_UNITS = "LSO Additional Aff Units";
    public static final String LSO_S_106_UNITS = "LSO S106 Grant Units";
    public static final String LSO_SUPPORTED_HSG_UNITS = "LSO Supported Hsg Units";
    public static final String OTHER_AFF_TOTAL_UNITS = "Other Aff Total Units";
    public static final String OTHER_AFF_NIL_GRANT_UNITS = "Other Aff Nil Grant Units";
    public static final String OTHER_AFF_ADDITIONAL_AFF_UNITS = "Other Aff Additional Aff Units";
    public static final String OTHER_AFF_S_106_UNITS = "Other Aff S106 Grant Units";
    public static final String OTHER_AFF_SUPPORTED_HSG_UNITS = "Other Aff Supported Hsg Units";
    public static final String OTHER_AFFORDABLE_TENURE_TYPE = "Other Affordable Tenure Type";
    public static final String NO_OF_LARGER_HOMES = "No. of Larger Homes";
    public static final String TOTAL_SUPPORTED_HOUSING_UNITS = "Total Supported Housing Units";
    public static final String TOTAL_MOVE_ON_UNITS = "Total Move On Units";
    public static final String S_106_REQ_GRANT_UNITS = "S106 Req Grant units";
    public static final String S_106_NIL_GRANT_UNITS = "S106 Nil Grant units";
    public static final String TOTAL_GRANT_APPROVED_ON_PROJECT = "Total grant approved on project";
    public static final String TOTAL_RCGF_APPROVED_ON_PROJECT = "Total RCGF approved on project";
    public static final String TOTAL_DPF_APPROVED_ON_PROJECT = "Total DPF approved on project";
    public static final String SOS_FORECAST_DATE = "SOS Forecast Date";
    public static final String SOS_FORECAST_FINANCIAL_YEAR = "SOS Forecast Financial Year";
    public static final String ACQUISITION_FORECAST_DATE = "Acquisition Forecast Date";
    public static final String COMPS_FORECAST_DATE = "Comps Forecast Date";
    public static final String COMPS_FORECAST_FINANCIAL_YEAR = "Comps Forecast Financial Year";

    public static final String ACQUISITION = "Acquisition";
    public static final String SOS = "SOS";
    public static final String COMPLETION = "Completion";

    public static final String MILESTONE_AUTHORISED_DATE = "{MILESTONE_NAME} Authorised Date";
    public static final String MILESTONE_FINANCIAL_YEAR = "{MILESTONE_NAME} Financial Year";
    public static final String MILESTONE_GRANT_PAID = "Grant paid at {MILESTONE_NAME}";
    public static final String MILESTONE_RCGF_PAID = "RCGF paid at {MILESTONE_NAME}";
    public static final String MILESTONE_DPF_PAID = "DPF paid at {MILESTONE_NAME}";

    public static Map<Integer, String> milestones = new HashMap<Integer, String>() {{
        put(Milestone.ACQUISITION_ID, ACQUISITION);
        put(Milestone.START_ON_SITE_ID, SOS);
        put(Milestone.COMPLETION_ID, COMPLETION);
    }};

    public static Set<String> base_headers = new LinkedHashSet<>(Arrays.asList(
            GLA_OPS_PROJECT_ID,
            PROJECT_ADDRESS,
            PROJECT_TITLE,
            PROJECT_STATUS,
            GLA_PROGRAMME_NAME,
            BIDDING_ROUTE_PROJECT_TYPE,
            BOROUGH,
            LEAD_ORGANISATION_CODE,
            LEAD_ORGANISATION_NAME,
            DEVELOPING_ORGANISATION_CODE,
            DEVELOPING_ORGANISATION_NAME,
            NEW_BUILD_NUMBER,
            REFURB_NUMBER,
            PROCESSING_ROUTE,
            PLANNING_STATUS,
            PROJECT_PREVIOUSLY_FUNDED,
            AFFORDABLE_RENT_AS_OF_MARKET_RENT,
            HABITABLE_ROOM_Y_N,
            LAR_TOTAL_UNITS,
            LAR_NIL_GRANT_UNITS,
            LAR_ADDITIONAL_AFF_UNITS,
            LAR_S_106_UNITS,
            LAR_SUPPORTED_HSG_UNITS,
            LLR_TOTAL_UNITS,
            LLR_NIL_GRANT_UNITS,
            LLR_ADDITIONAL_AFF_UNITS,
            LLR_S_106_UNITS,
            LLR_SUPPORTED_HSG_UNITS,
            LSO_TOTAL_UNITS,
            LSO_NIL_GRANT_UNITS,
            LSO_ADDITIONAL_AFF_UNITS,
            LSO_S_106_UNITS,
            LSO_SUPPORTED_HSG_UNITS,
            OTHER_AFF_TOTAL_UNITS,
            OTHER_AFF_NIL_GRANT_UNITS,
            OTHER_AFF_ADDITIONAL_AFF_UNITS,
            OTHER_AFF_S_106_UNITS,
            OTHER_AFF_SUPPORTED_HSG_UNITS,
            OTHER_AFFORDABLE_TENURE_TYPE,
            NO_OF_LARGER_HOMES,
            TOTAL_SUPPORTED_HOUSING_UNITS,
            TOTAL_MOVE_ON_UNITS,
            S_106_REQ_GRANT_UNITS,
            S_106_NIL_GRANT_UNITS,
            TOTAL_GRANT_APPROVED_ON_PROJECT,
            TOTAL_RCGF_APPROVED_ON_PROJECT,
            TOTAL_DPF_APPROVED_ON_PROJECT,
            SOS_FORECAST_DATE,
            SOS_FORECAST_FINANCIAL_YEAR,
            ACQUISITION_FORECAST_DATE,
            COMPS_FORECAST_DATE,
            COMPS_FORECAST_FINANCIAL_YEAR
    ));

    public static String getHeader(String header, Integer milestoneId) {
        return header.replace("{MILESTONE_NAME}", milestones.get(milestoneId));
    }

    public static Set<String> getHeaders(Integer milestoneId) {
        Set<String> headers = new LinkedHashSet<>(base_headers);
        if (milestones.containsKey(milestoneId)) {
            headers.add(getHeader(MILESTONE_AUTHORISED_DATE, milestoneId));
            headers.add(getHeader(MILESTONE_FINANCIAL_YEAR, milestoneId));
            headers.add(getHeader(MILESTONE_GRANT_PAID, milestoneId));
            headers.add(getHeader(MILESTONE_RCGF_PAID, milestoneId));
            headers.add(getHeader(MILESTONE_DPF_PAID, milestoneId));
        }
        return headers;
    }

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private OrganisationGroupService organisationGroupService;

    @Autowired
    private FinancialCalendar financialCalendar;

    @Autowired
    private FinanceService financeService;

    public List<Map<String, Object>> convertProjectsToAffordableHousingReportEntries(List<Project> projects, Integer milestoneId) {
        return projects.stream().map(project -> convertProjectToAffordableHousingReportEntry(project, milestoneId)).collect(Collectors.toList());
    }

    private Map<String, Object> convertProjectToAffordableHousingReportEntry(Project project, Integer milestoneId) {
        Map<String, Object> map = new HashMap<>();

        // project level data
        map.put(GLA_OPS_PROJECT_ID, "P"+project.getId());
        map.put(GLA_PROGRAMME_NAME, project.getProgramme().getName());
        map.put(BIDDING_ROUTE_PROJECT_TYPE, project.getTemplate().getName());
        map.put(PROJECT_STATUS, project.currentState().toString());

        if (milestoneId != null && milestoneId != -1) {
            ProjectHistory milestoneApprovalHistoryEntry = getMilestoneApprovalProjectHistory(project, milestoneId);
            if (milestoneApprovalHistoryEntry != null) {
                // as the time is set automatically, it could be a couple millis of between the history and block approval timestamps
                OffsetDateTime approvalDate = milestoneApprovalHistoryEntry.getCreatedOn().plusSeconds(3);
                addBlockVersionSpecificData(project, map, approvalDate, milestoneId);
            }
        }
        else {
            addBlockVersionSpecificData(project, map, null, milestoneId);
        }

        return map;
    }

    private ProjectHistory getMilestoneApprovalProjectHistory(Project project, Integer milestoneId) {
        return project.getHistory().stream()
                .filter(h -> ProjectHistory.HistoryEventType.MilestoneClaimApproved.equals(h.getHistoryEventType()) && milestoneId.equals(h.getExternalId()))
                .findFirst().orElse(null);
    }

    private void addBlockVersionSpecificData(Project project, Map<String, Object> map, OffsetDateTime approvalDate, Integer milestoneId) {
        // project details block
        ProjectDetailsBlock detailsBlock = getBlock(project, ProjectBlockType.Details, approvalDate);
        if (detailsBlock != null) {
            map.put(PROJECT_ADDRESS, detailsBlock.getAddress());
            map.put(PROJECT_TITLE, detailsBlock.getTitle());
            map.put(BOROUGH, detailsBlock.getBorough());

            Organisation leadOrganisation = getLeadOrganisation(project, detailsBlock);
            map.put(LEAD_ORGANISATION_CODE, leadOrganisation.getId());
            map.put(LEAD_ORGANISATION_NAME, leadOrganisation.getName());

            Organisation developingOrganisation = getDevelopingOrganisation(detailsBlock);
            map.put(DEVELOPING_ORGANISATION_CODE, developingOrganisation.getId());
            map.put(DEVELOPING_ORGANISATION_NAME, developingOrganisation.getName());
        }

        // units
        UnitDetailsBlock unitDetailsBlock = getBlock(project, ProjectBlockType.UnitDetails, approvalDate);
        if (unitDetailsBlock != null) {
            map.put(NEW_BUILD_NUMBER, unitDetailsBlock.getNewBuildUnits());
            map.put(REFURB_NUMBER, unitDetailsBlock.getRefurbishedUnits());
        }

        // milestones
        ProjectMilestonesBlock milestonesBlock = getBlock(project, ProjectBlockType.Milestones, approvalDate);
        if (milestonesBlock != null) {
            map.put(PROCESSING_ROUTE, getProcessingRoute(milestonesBlock));

            Milestone sosMilestone = milestonesBlock.getMilestoneByExternalId(Milestone.START_ON_SITE_ID);
            if (sosMilestone != null && !sosMilestone.isApproved()) {
                map.put(SOS_FORECAST_DATE, sosMilestone.getMilestoneDate());
                map.put(SOS_FORECAST_FINANCIAL_YEAR, financialCalendar.financialYearString(sosMilestone.getMilestoneDate()));
            }

            Milestone acquisitionMilestone = milestonesBlock.getMilestoneByExternalId(Milestone.ACQUISITION_ID);
            if (acquisitionMilestone != null && !acquisitionMilestone.isApproved()) {
                map.put(ACQUISITION_FORECAST_DATE, acquisitionMilestone.getMilestoneDate());
            }

            Milestone completionMilestone = milestonesBlock.getMilestoneByExternalId(Milestone.COMPLETION_ID);
            if (completionMilestone != null && !completionMilestone.isApproved()) {
                map.put(COMPS_FORECAST_DATE, completionMilestone.getMilestoneDate());
                map.put(COMPS_FORECAST_FINANCIAL_YEAR, financialCalendar.financialYearString(completionMilestone.getMilestoneDate()));
            }

            if (approvalDate != null) {
                map.put(getHeader(MILESTONE_AUTHORISED_DATE, milestoneId), approvalDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                map.put(getHeader(MILESTONE_FINANCIAL_YEAR, milestoneId), financialCalendar.financialYearString(approvalDate.toLocalDate()));
                List<ProjectLedgerEntry> payments = financeService.getByBlockIdAndExternalId(milestonesBlock.getId(), milestoneId);
                map.put(getHeader(MILESTONE_GRANT_PAID, milestoneId), getPaymentValue(payments, LedgerType.PAYMENT));
                map.put(getHeader(MILESTONE_RCGF_PAID, milestoneId), getPaymentValue(payments, LedgerType.RCGF));
                map.put(getHeader(MILESTONE_DPF_PAID, milestoneId), getPaymentValue(payments, LedgerType.DPF));
            }
        }

        // questions
        ProjectQuestionsBlock questionsBlock = getQuestionsBlock(project, "Questions", approvalDate);
        if (questionsBlock != null) {
            map.put(PLANNING_STATUS, getAnswer(questionsBlock, 502)); // dropdown
            map.put(PROJECT_PREVIOUSLY_FUNDED, getAnswer(questionsBlock, 504)); // dropdown
            map.put(AFFORDABLE_RENT_AS_OF_MARKET_RENT, getNumericAnswer(questionsBlock, 509)); // number
            map.put(OTHER_AFFORDABLE_TENURE_TYPE, getAnswer(questionsBlock, 501)); // dropdown
            map.put(NO_OF_LARGER_HOMES, getNumericAnswer(questionsBlock, 530)); // number
        }

        ProjectQuestionsBlock additionalQuestionsBlock = getQuestionsBlock(project, "Additional Questions", approvalDate);
        if (additionalQuestionsBlock != null) {
            map.put(TOTAL_MOVE_ON_UNITS, getNumericAnswer(additionalQuestionsBlock, 514)); // number
        }

        // grant source
        BaseGrantBlock baseGrantBlock = getGrantBlock(project, approvalDate);
        boolean affordableCriteriaMet = false;
        if (baseGrantBlock != null && baseGrantBlock instanceof DeveloperLedGrantBlock) {
            affordableCriteriaMet = Boolean.TRUE.equals(((DeveloperLedGrantBlock) baseGrantBlock).getAffordableCriteriaMet());
            map.put(HABITABLE_ROOM_Y_N, toYesNo(affordableCriteriaMet));
        }

        TenureTypeAndUnits larEntry = getTenureAndUnitEntry(baseGrantBlock, TenureType.LONDON_AFFORDABLE_RENT);
        TenureTypeAndUnits llrEntry = getTenureAndUnitEntry(baseGrantBlock, TenureType.LONDON_LIVING_RENT);
        TenureTypeAndUnits lsoEntry = getTenureAndUnitEntry(baseGrantBlock, TenureType.LONDON_SHARED_OWNERSHIP);
        TenureTypeAndUnits otherAffEntry = getTenureAndUnitEntry(baseGrantBlock, TenureType.OTHER_AFFORDABLE);

        Integer larNilGrantUnits = null;
        Integer larAdditionalAffUnits = larEntry.getAdditionalAffordableUnits();
        Integer larS106Units = null;
        Integer larSupportedHsgUnits = larEntry.getSupportedUnits();
        Integer larTotalUnits = larEntry.getTotalUnits();

        Integer llrNilGrantUnits = null;
        Integer llrAdditionalAffUnits = llrEntry.getAdditionalAffordableUnits();
        Integer llrS106Units = null;
        Integer llrSupportedHsgUnits = llrEntry.getSupportedUnits();
        Integer llrTotalUnits = llrEntry.getTotalUnits();

        Integer lsoNilGrantUnits = null;
        Integer lsoAdditionalAffUnits = lsoEntry.getAdditionalAffordableUnits();
        Integer lsoS106Units = null;
        Integer lsoSupportedHsgUnits = lsoEntry.getSupportedUnits();
        Integer lsoTotalUnits = lsoEntry.getTotalUnits();

        Integer otherAffNilGrantUnits = null;
        Integer otherAffAdditionalAffUnits = otherAffEntry.getAdditionalAffordableUnits();
        Integer otherAffS106Units = null;
        Integer otherAffSupportedHsgUnits = otherAffEntry.getSupportedUnits();
        Integer otherTotalUnits = otherAffEntry.getTotalUnits();

        if (affordableCriteriaMet) {
            larS106Units = larEntry.getS106Units();
            llrS106Units = llrEntry.getS106Units();
            lsoS106Units = lsoEntry.getS106Units();
            otherAffS106Units = otherAffEntry.getS106Units();
        }
        else {
            larNilGrantUnits = larEntry.getS106Units();
            llrNilGrantUnits = llrEntry.getS106Units();
            lsoNilGrantUnits = lsoEntry.getS106Units();
            otherAffNilGrantUnits = otherAffEntry.getS106Units();
        }

        if (baseGrantBlock instanceof DeveloperLedGrantBlock) {
            larTotalUnits = nullSafeAdd(larNilGrantUnits, larAdditionalAffUnits, larS106Units, larSupportedHsgUnits);
            llrTotalUnits = nullSafeAdd(llrNilGrantUnits, llrAdditionalAffUnits, llrS106Units, llrSupportedHsgUnits);
            lsoTotalUnits = nullSafeAdd(lsoNilGrantUnits, lsoAdditionalAffUnits, lsoS106Units, lsoSupportedHsgUnits);
            otherTotalUnits = nullSafeAdd(otherAffNilGrantUnits, otherAffAdditionalAffUnits, otherAffS106Units, otherAffSupportedHsgUnits);
        }

        map.put(LAR_NIL_GRANT_UNITS, larNilGrantUnits);
        map.put(LAR_ADDITIONAL_AFF_UNITS, larAdditionalAffUnits);
        map.put(LAR_S_106_UNITS, larS106Units);
        map.put(LAR_SUPPORTED_HSG_UNITS, larSupportedHsgUnits);
        map.put(LAR_TOTAL_UNITS, larTotalUnits);

        map.put(LLR_NIL_GRANT_UNITS, llrNilGrantUnits);
        map.put(LLR_ADDITIONAL_AFF_UNITS, llrAdditionalAffUnits);
        map.put(LLR_S_106_UNITS, llrS106Units);
        map.put(LLR_SUPPORTED_HSG_UNITS, llrSupportedHsgUnits);
        map.put(LLR_TOTAL_UNITS, llrTotalUnits);

        map.put(LSO_NIL_GRANT_UNITS, lsoNilGrantUnits);
        map.put(LSO_ADDITIONAL_AFF_UNITS, lsoAdditionalAffUnits);
        map.put(LSO_S_106_UNITS, lsoS106Units);
        map.put(LSO_SUPPORTED_HSG_UNITS, lsoSupportedHsgUnits);
        map.put(LSO_TOTAL_UNITS, lsoTotalUnits);

        map.put(OTHER_AFF_NIL_GRANT_UNITS, otherAffNilGrantUnits);
        map.put(OTHER_AFF_ADDITIONAL_AFF_UNITS, otherAffAdditionalAffUnits);
        map.put(OTHER_AFF_S_106_UNITS, otherAffS106Units);
        map.put(OTHER_AFF_SUPPORTED_HSG_UNITS, otherAffSupportedHsgUnits);
        map.put(OTHER_AFF_TOTAL_UNITS, otherTotalUnits);

        map.put(TOTAL_SUPPORTED_HOUSING_UNITS, nullSafeAdd(larSupportedHsgUnits, llrSupportedHsgUnits, lsoSupportedHsgUnits, otherAffSupportedHsgUnits));

        map.put(S_106_REQ_GRANT_UNITS, nullSafeAdd(larS106Units, llrS106Units, lsoS106Units, otherAffS106Units));
        map.put(S_106_NIL_GRANT_UNITS, nullSafeAdd(larNilGrantUnits, llrNilGrantUnits, lsoNilGrantUnits, otherAffNilGrantUnits));

        GrantSourceBlock grantSourceBlock = getBlock(project, ProjectBlockType.GrantSource, approvalDate);
        if (grantSourceBlock != null) {
            map.put(TOTAL_GRANT_APPROVED_ON_PROJECT, grantSourceBlock.getGrantValue());
            map.put(TOTAL_RCGF_APPROVED_ON_PROJECT, grantSourceBlock.getRecycledCapitalGrantFundValue());
            map.put(TOTAL_DPF_APPROVED_ON_PROJECT, grantSourceBlock.getDisposalProceedsFundValue());
        }
    }

    private <T> T getBlock(Project project, ProjectBlockType blockType, OffsetDateTime approvalDate) {
        if (approvalDate != null) {
            return getBlockAsOfApprovalDate(project, blockType, approvalDate);
        }
        else {
            return getLastApproved(project, blockType);
        }
    }

    private String getAnswer(ProjectQuestionsBlock questionsBlock, Integer questionId) {
        Answer answer = questionsBlock.getAnswerByQuestionId(questionId);
        return answer != null ? answer.getAnswer() : null;
    }

    private Double getNumericAnswer(ProjectQuestionsBlock questionsBlock, Integer questionId) {
        Answer answer = questionsBlock.getAnswerByQuestionId(questionId);
        return answer != null ? answer.getNumericAnswer() : null;
    }

    /**
     * For this report we want the latest approved block if the project has been active, otherwise just the single existing block.
     */
    @SuppressWarnings("unchecked")
    private <T> T getLastApproved(Project project, ProjectBlockType blockType) {
        List<NamedProjectBlock> blocks = project.getBlocksByType(blockType);
        if (blocks.size() > 1) {
            return (T) blocks.stream().filter(pb -> NamedProjectBlock.BlockStatus.LAST_APPROVED.equals(pb.getBlockStatus())).findFirst().orElse(null);
        }
        else if (blocks.size() == 1) {
            return (T) blocks.get(0);
        }
        else {
            return null;
        }
    }

    /**
     * Returns the block of the given type which have been approved at the given approval time or before. Returns null if not found.
     */
    @SuppressWarnings("unchecked")
    <T> T getBlockAsOfApprovalDate(Project project, ProjectBlockType blockType, OffsetDateTime approvalDate) {
        return (T) project.getBlocksByType(blockType).stream()
                .filter(b -> b.isApproved() && (approvalDate.isAfter(b.getApprovalTime()) || approvalDate.equals(b.getApprovalTime())))
                .max(Comparator.comparing(NamedProjectBlock::getApprovalTime))
                .orElse(null);
    }

    /**
     * For this report lead organisation is either the org group lead org if the project has one or the org that created the project.
     */
    private Organisation getLeadOrganisation(Project project, ProjectDetailsBlock detailsBlock) {
        if (detailsBlock.getOrganisationGroupId() != null) {
            OrganisationGroup organisationGroup = organisationGroupService.find(detailsBlock.getOrganisationGroupId());
            return organisationGroup.getLeadOrganisation();
        }
        return project.getOrganisation();
    }

    private Organisation getDevelopingOrganisation(ProjectDetailsBlock detailsBlock) {
        return detailsBlock.getDevelopingOrganisationId() != null ? organisationService.findOne(detailsBlock.getDevelopingOrganisationId()) : new Organisation();
    }

    private String getProcessingRoute(ProjectMilestonesBlock milestonesBlock) {
        if (milestonesBlock.getProcessingRouteId() == null) {
            return "";
        }
        Template template = milestonesBlock.getProject().getTemplate();
        MilestonesTemplateBlock templateMilestonesBlock = (MilestonesTemplateBlock) template.getSingleBlockByType(ProjectBlockType.Milestones);
        return templateMilestonesBlock.getProcessingRoute(milestonesBlock.getProcessingRouteId()).getName();
    }

    private ProjectQuestionsBlock getQuestionsBlock(Project project, String blockDisplayName, OffsetDateTime approvalDate) {
        if (approvalDate != null) {
            return getQuestionsBlockAsOfApprovalDate(project, blockDisplayName, approvalDate);
        }
        else {
            return getLastApprovedQuestionsBlock(project, blockDisplayName);
        }
    }

    private ProjectQuestionsBlock getLastApprovedQuestionsBlock(Project project, String blockDisplayName) {
        List<ProjectQuestionsBlock> blocks = project.getQuestionsBlocks().stream()
                .filter(qb -> blockDisplayName.equals(qb.getBlockDisplayName()) || (qb.getBlockDisplayName() == null && blockDisplayName.equals("Questions")))
                .collect(Collectors.toList());
        if (blocks.size() > 1) {
            return blocks.stream().filter(pb -> NamedProjectBlock.BlockStatus.LAST_APPROVED.equals(pb.getBlockStatus())).findFirst().orElse(null);
        }
        else if (blocks.size() == 1) {
            return blocks.get(0);
        }
        else {
            return null;
        }
    }

    ProjectQuestionsBlock getQuestionsBlockAsOfApprovalDate(Project project, String blockDisplayName, OffsetDateTime approvalDate) {
        return project.getQuestionsBlocks().stream()
                .filter(b -> b.isApproved() && (approvalDate.isAfter(b.getApprovalTime()) || approvalDate.equals(b.getApprovalTime())))
                .filter(qb -> blockDisplayName.equals(qb.getBlockDisplayName()) || (qb.getBlockDisplayName() == null && blockDisplayName.equals("Questions")))
                .max(Comparator.comparing(NamedProjectBlock::getApprovalTime))
                .orElse(null);
    }

    private BaseGrantBlock getGrantBlock(Project project, OffsetDateTime approvalDate) {
        if (approvalDate != null) {
            return getGrantBlockAsOfApprovalDate(project, approvalDate);
        }
        else {
            return getLastApprovedGrantBlock(project);
        }
    }

    private BaseGrantBlock getLastApprovedGrantBlock(Project project) {
        BaseGrantBlock baseGrantBlock = getLastApproved(project, ProjectBlockType.CalculateGrant);
        if (baseGrantBlock != null) {
            return baseGrantBlock;
        }

        baseGrantBlock = getLastApproved(project, ProjectBlockType.DeveloperLedGrant);
        if (baseGrantBlock != null) {
            return baseGrantBlock;
        }

        return getLastApproved(project, ProjectBlockType.NegotiatedGrant);
    }

    private BaseGrantBlock getGrantBlockAsOfApprovalDate(Project project, OffsetDateTime approvalDate) {
        BaseGrantBlock baseGrantBlock = getBlockAsOfApprovalDate(project, ProjectBlockType.CalculateGrant, approvalDate);
        if (baseGrantBlock != null) {
            return baseGrantBlock;
        }

        baseGrantBlock = getBlockAsOfApprovalDate(project, ProjectBlockType.DeveloperLedGrant, approvalDate);
        if (baseGrantBlock != null) {
            return baseGrantBlock;
        }

        return getBlockAsOfApprovalDate(project, ProjectBlockType.NegotiatedGrant, approvalDate);
    }

    private TenureTypeAndUnits getTenureAndUnitEntry(BaseGrantBlock grantBlock, String tenureType) {
        TenureTypeAndUnits entry = null;
        if (grantBlock != null) {
            entry = grantBlock.getTenureTypeAndUnitsEntry(tenureType);
        }
        return entry != null ? entry : new TenureTypeAndUnits();
    }

    private String toYesNo(Boolean affordableCriteriaMet) {
        return Boolean.TRUE.equals(affordableCriteriaMet) ? "Y" : "N";
    }

    private BigDecimal getPaymentValue(List<ProjectLedgerEntry> payments, LedgerType ledgerType) {
        if (payments != null) {
            for (ProjectLedgerEntry payment: payments) {
                if (ledgerType.equals(payment.getLedgerType())) {
                    return payment.getValue();
                }
            }
        }
        return null;
    }

}
