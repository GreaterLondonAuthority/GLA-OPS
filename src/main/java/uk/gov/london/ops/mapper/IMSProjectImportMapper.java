/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.london.ops.domain.finance.LedgerSource;
import uk.gov.london.ops.domain.finance.LedgerStatus;
import uk.gov.london.ops.domain.finance.LedgerType;
import uk.gov.london.ops.domain.finance.PaymentGroup;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.domain.refdata.CategoryValue;
import uk.gov.london.ops.domain.refdata.TenureType;
import uk.gov.london.ops.domain.template.*;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.repository.CategoryValueRepository;
import uk.gov.london.ops.repository.OrganisationRepository;
import uk.gov.london.ops.repository.PaymentGroupRepository;
import uk.gov.london.ops.repository.ProjectLedgerRepository;
import uk.gov.london.ops.util.CSVFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chris on 13/10/2017.
 */
@Component
public class IMSProjectImportMapper {

    private static final String PREFIX = "Q:";
    static final String IMS_MIGRATION_USER = "ims.migration@gla.com";
    public static final String INTERIM_PAYMENT_RECLAIM = "Interim Payment Reclaim";
    public static final String COMPLETION_PAYMENT_RECLAIM = "Completion Payment Reclaim";
    public static final String RECLAIM_DATE = "Reclaim Date";
    public static final String RECLAIM_STATUS = "Reclaim Status";
    public static final String RECLAIM_GRANT_STATUS = "Reclaim Grant status";
    private static final String STARTS_REPORTED = "Starts reported";
    private static final String COMPLETIONS_REPORTED = "Completions reported";
    private static final String TENURE_FOR_GRANT_IMPORT = "Tenure";

    @Autowired
    MilestoneMapper milestoneMapper;

    @Autowired
    ProjectLedgerRepository projectLedgerRepository;

    @Autowired
    CategoryValueRepository categoryValueRepository;

    @Autowired
    PaymentGroupRepository paymentGroupRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Value("${default.wbs.code}")
    String defaultWbsCode = "GW0901.025";

    static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String COMPLETION_KEY = "ms_completion";
    private static final String START_ON_SITE_KEY = "ms_start_site";
    private static final String NIL_GRANT = "Nil grant";
    private static final String TOTAL_GRANT = "Grant";
    private static final String DPF_VALUE = "DPF";

    private static final String RCGF_VALUE = "RCGF";
    private static final String JUSTIFICATION = "Scheme migrated from IMS";
    private static final String TENURE_TYPE = "Tenure Type";
    private static final String GRANT_REQUESTED = "GRANT REQUESTED";
    private static final String SHARED_OWNERSHIP_STRING = "LCHO";
    private static final String AFFORDABLE_RENT_STRING = "RENT";
    private static final String DEV_COST = "Total Development Cost";
    private static final String TOTAL_AFFORDABLE_UNITS = "TOTAL AFFORDABLE UNITS";
    private static final String SUPPORTED_UNITS = "OF WHICH SUPPORTED & SPECIALISED UNITS";

    private static final String NEW_BUILD_UNITS = "Build type: New Build";
    private static final String REFURBISHED_UNITS = "Build type: Refurbished";
    private static final String UNITS_1 = "Units by Number of People: People 1";
    private static final String UNITS_2 = "Units by Number of People: People 2";
    private static final String UNITS_3 = "Units by Number of People: People 3";
    private static final String UNITS_4 = "Units by Number of People: People 4";
    private static final String UNITS_5 = "Units by Number of People: People 5";
    private static final String UNITS_6 = "Units by Number of People: People 6";
    private static final String UNITS_7 = "Units by Number of People: People 7";
    private static final String UNITS_8 = "Units by Number of People: People 8+";
    private static final String WHEELCHAIR_USERS = "Number of Wheelchair Units";
    private static final String INTERNAL_AREA = "Gross Internal Area";



    public static final String DETAILS_NUMBER_OF_UNITS = "UNITS";
    public static final String DETAILS_NUMBER_OF_BEDROOMS = "BED(S)";
    public static final String DETAILS_UNIT_TYPE = "UNIT TYPE";
    public static final String DETAILS_NET_WEEKLY_RENT = "NET WEEKLY RENT";
    public static final String DETAILS_WEEKLY_SC = "WEEKLY SC";
    public static final String DETAILS_MARKET_VALUE= "MARKET VALUE";
    public static final String DETAILS_FIRST_TRANCHE = "FIRST TRANCHE SALES";
    public static final String DETAILS_TENURE_TYPE = "TENURE";
    public static final String DETAILS_WEEKLY_MARKET_RENT = "WEEKLY MARKET RENT";

    public static final String PROCESSING_ROUTE = "Processing Route";
    public static final String COMPLETION_PAYMENT = "Completion Payment";
    public static final String INTERIM_PAYMENT = "Interim Payment";
    public static final String START_ON_SITE_PAYMENT = "Start On Site Payment";
    public static final String PLANNING_CONSENT_DATE = "Detailed planning permission achieved  Date";
    public static final String PLANNING_CONSENT_GRANT = "Detailed planning permission achieved Grant %";
    public static final String PLANNING_CONSENT_STATUS = "Detailed planning permission achieved Status";
    public static final String PLANNING_CONSENT_CLAIM_STATUS = "Detailed planning permission achieved Claim status";

    public static final String SOS_DATE = "Start On Site Date";
    public static final String SOS_GRANT = "Start On Site Grant %";
    public static final String SOS_STATUS = "Start On Site Status";
    public static final String SOS_CLAIM_STATUS = "Start On Site Claim Status";

    public static final String COMPLETION_DATE = "Completion Date";
    public static final String COMPLETION_GRANT = "Completion Grant %";
    public static final String COMPLETION_STATUS = "Completion Status";
    public static final String COMPLETION_CLAIM_STATUS = "Completion Claim Status";

    public static final String INTERIM_DATE = "Interim Payment Date";
    public static final String INTERIM_GRANT = "Interim Payment Grant %";
    public static final String INTERIM_STATUS = "Interim Payment Status";
    public static final String INTERIM_CLAIM_STATUS = "Interim Payment Claim Status";


    			






    private static final String ADDRESS = "Address";
    private static final String BOROUGH = "Borough";
    private static final String POST_CODE = "Post Code";
    private static final String X_COORD = "OS X Coordinate";
    private static final String Y_COORD = "OS Y Coordinate";

    private final static String DEFAULT_PROCESSING_ROUTE = "Land & Development";

    public static final int START_ON_SITE= 3003;
    public static final int COMPLETION= 3004;
    public static final int PLANNING = 3006;
    public static final int INTERIM = 3050;

    public void mapIMSUnitDetails(UnitDetailsBlock unitDetailsBlock  , CSVFile csvFile) throws ValidationException {

        UnitDetailsTableEntry entry = new UnitDetailsTableEntry();
        entry.setNbUnits(csvFile.getIntegerOrNull(DETAILS_NUMBER_OF_UNITS));
        CategoryValue numberOfBedrooms = categoryValueRepository.findByCategoryAndDisplayValue(CategoryValue.Category.Bedrooms, csvFile.getString(DETAILS_NUMBER_OF_BEDROOMS));

        if (numberOfBedrooms == null) {
            throw new ValidationException("Number of bedrooms does not match expected format.");
        }
        entry.setNbBeds(numberOfBedrooms);

        CategoryValue unitType = categoryValueRepository.findByCategoryAndDisplayValue(CategoryValue.Category.UnitTypes, csvFile.getString(DETAILS_UNIT_TYPE));

        if (unitType == null) {
            throw new ValidationException("Unit Type does not match expected format.");
        }
        entry.setUnitType(unitType);

        Template template = unitDetailsBlock.getProject().getTemplate();
        TenureType type = null;
        String tenureString = csvFile.getString(DETAILS_TENURE_TYPE);
        for (TemplateTenureType templateTenureType : template.getTenureTypes()) {
            if (templateTenureType.getName().equals(tenureString)) {
                type = templateTenureType.getTenureType();
            }
        }

        if (type == null) {
            throw new ValidationException("Unknown tenure type for specified entry");
        }

        entry.setTenureId(type.getId());
        entry.setMarketType(type.getMarketTypes().get(0));
        entry.setProjectId(unitDetailsBlock.getProjectId());
        entry.setWeeklyServiceCharge(csvFile.getCurrencyValue(DETAILS_WEEKLY_SC));
        entry.setNetWeeklyRent(csvFile.getCurrencyValue(DETAILS_NET_WEEKLY_RENT));

        if (type.getMarketTypes().get(0).getAvailableForSales()) {
            if(entry.getNetWeeklyRent() != null && !type.getName().equals(TenureType.LEGACY_SHARED_OWNERSHIP)){
                throw new ValidationException("Unexpected net weekly rent value");
            }
            entry.setType(UnitDetailsTableEntry.Type.Sales);
            entry.setMarketValue(csvFile.getCurrencyValue(DETAILS_MARKET_VALUE));
            entry.setFirstTrancheSales(csvFile.getIntegerOrNull(DETAILS_FIRST_TRANCHE));
        } else {
            entry.setType(UnitDetailsTableEntry.Type.Rent);
            entry.setWeeklyMarketRent(csvFile.getCurrencyValue(DETAILS_WEEKLY_MARKET_RENT));
        }

        unitDetailsBlock.getTableEntries().add(entry);


    }
    public void mapIMSRecordToProject(Project project , CSVFile csvFile) throws ValidationException {

        mapProjectDetails(project, csvFile);
        mapGrantSource(project, csvFile);
        mapNegotiatedGrant(project, csvFile);
        mapProjectQuestions(project, csvFile);
        mapUnitBlockDetails(project, csvFile);
        mapMilestones(project, csvFile);
        if (csvFile.getHeaders().contains(STARTS_REPORTED)) {
            handleUpdatesToGrantSource(project, csvFile);
        }
    }



    private void mapProjectQuestions(Project project , CSVFile csvFile) {
        Map<Integer, String> questionsMap = getQuestions(csvFile);
        List<NamedProjectBlock> blocksByType = project.getBlocksByType(ProjectBlockType.Questions);
        for (NamedProjectBlock namedProjectBlock : blocksByType) {
            ProjectQuestionsBlock questionsBlock = (ProjectQuestionsBlock) namedProjectBlock;

            for (TemplateQuestion templateQuestion : questionsBlock.getQuestionEntities()) {
                String answer = questionsMap.get(templateQuestion.getQuestion().getId());
                if (!StringUtils.isEmpty(answer)) {
                    Answer answerObject = new Answer(null, templateQuestion.getQuestion(), answer, null);
                    if (templateQuestion.getQuestion().getAnswerType().equals(AnswerType.Number)) {
                        answerObject = new Answer(null, templateQuestion.getQuestion(), null, Double.parseDouble(answer));
                    }
                    questionsBlock.getAnswers().add(answerObject);
                }
            }
        }
    }

    private Map<Integer, String> getQuestions(CSVFile csvFile) {
        Map<Integer, String> map = new HashMap<>();
        for (String header : csvFile.getHeaders()) {
            if (header.startsWith(PREFIX)) {
                String substring = header.substring(PREFIX.length(), header.indexOf(" "));
                Integer questionId = Integer.parseInt(substring);
                map.put(questionId, csvFile.getString(header));
            }
        }
        return map;
    }

    private void mapUnitBlockDetails(Project project , CSVFile csvFile) {
        UnitDetailsBlock units = (UnitDetailsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.UnitDetails);
        if (units != null) {
            units.setNewBuildUnits(csvFile.getIntegerOrNull(NEW_BUILD_UNITS));
            units.setRefurbishedUnits(csvFile.getIntegerOrNull(REFURBISHED_UNITS));
            units.setNbWheelchairUnits(csvFile.getIntegerOrNull(WHEELCHAIR_USERS));
            units.setGrossInternalArea(csvFile.getRoundedDecimal(INTERNAL_AREA));

            units.setType1Units(csvFile.getIntegerOrNull(UNITS_1));
            units.setType2Units(csvFile.getIntegerOrNull(UNITS_2));
            units.setType3Units(csvFile.getIntegerOrNull(UNITS_3));
            units.setType4Units(csvFile.getIntegerOrNull(UNITS_4));
            units.setType5Units(csvFile.getIntegerOrNull(UNITS_5));
            units.setType6Units(csvFile.getIntegerOrNull(UNITS_6));
            units.setType7Units(csvFile.getIntegerOrNull(UNITS_7));
            units.setType8Units(csvFile.getIntegerOrNull(UNITS_8));
        }
    }



    private void mapProjectDetails(Project project , CSVFile csvFile) {
        ProjectDetailsBlock details = (ProjectDetailsBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Details);

        String address = csvFile.getString(ADDRESS);
        if (address != null && address.length() > 255) {
            throw new ValidationException("Address is too long, maximum is 255");
        }
        details.setAddress(address);

        String contact = project.getOrganisation().getName();
        if (project.getOrganisation().getPrimaryContactFirstName() != null) {

            if (project.getOrganisation().getPrimaryContactLastName() != null){
                contact = project.getOrganisation().getPrimaryContactFirstName() + " " + project.getOrganisation().getPrimaryContactLastName();
            }

        }
        details.setMainContact(contact);
        details.setMainContactEmail(project.getOrganisation().getEmail());

        details.setPostcode(csvFile.getString(POST_CODE));
        details.setBorough(csvFile.getString(BOROUGH));
        details.setCoordX(csvFile.getString(X_COORD));
        details.setCoordY(csvFile.getString(Y_COORD));
    }

    private void mapNegotiatedGrant(Project project , CSVFile csvFile) {
        NegotiatedGrantBlock negotiatedGrantBlock = project.getNegotiatedGrantBlock();
        negotiatedGrantBlock.setJustification(JUSTIFICATION);
        Set<TenureTypeAndUnits> tenureTypeAndUnitsEntries = negotiatedGrantBlock.getTenureTypeAndUnitsEntries();
        TenureTypeAndUnits toUpdate = null;
        for (TenureTypeAndUnits tenureTypeAndUnitsEntry : tenureTypeAndUnitsEntries) {
            if (tenureTypeAndUnitsEntry.getTenureType().getName().equals(csvFile.getString(TENURE_TYPE))) {

                 toUpdate = tenureTypeAndUnitsEntry;
           }
        }


        if (toUpdate != null) {
            BigDecimal grant = csvFile.getCurrencyValue(GRANT_REQUESTED);
            if (grant != null) {
                toUpdate.setGrantRequested( grant.longValue());
            }

            BigDecimal currencyValue = csvFile.getCurrencyValue(DEV_COST);
            if (currencyValue != null) {
                toUpdate.setTotalCost(currencyValue.longValue());
            }

            toUpdate.setTotalUnits(csvFile.getIntegerOrNull(TOTAL_AFFORDABLE_UNITS));
            toUpdate.setSupportedUnits(csvFile.getIntegerOrNull(SUPPORTED_UNITS));
        }



    }

    private void mapGrantSource(Project project , CSVFile csvFile) {
        GrantSourceBlock grantSourceBlock = (GrantSourceBlock) project.getSingleLatestBlockOfType(ProjectBlockType.GrantSource);

        if ("Y".equalsIgnoreCase(csvFile.getString(NIL_GRANT))) {
            grantSourceBlock.setZeroGrantRequested(true);
        } else {
            if (grantSourceBlock != null) {
                BigDecimal currencyValue = csvFile.getCurrencyValue(TOTAL_GRANT);
                if (currencyValue != null) {
                    grantSourceBlock.setGrantValue(currencyValue.longValue());
                }
                currencyValue = csvFile.getCurrencyValue(DPF_VALUE);
                if (currencyValue != null) {
                    grantSourceBlock.setDisposalProceedsFundValue(currencyValue.longValue());
                }
                currencyValue = csvFile.getCurrencyValue(RCGF_VALUE);
                if (currencyValue != null) {
                    grantSourceBlock.setRecycledCapitalGrantFundValue(currencyValue.longValue());
                }
            }
        }
    }

    void mapMilestones(Project project , CSVFile csvFile) throws ValidationException {
        ProjectMilestonesBlock milestonesBlock = (ProjectMilestonesBlock) project.getSingleLatestBlockOfType(ProjectBlockType.Milestones);
        MilestonesTemplateBlock milestonesTemplateBlock = (MilestonesTemplateBlock) project.getTemplate().getSingleBlockByType(ProjectBlockType.Milestones);
        // Assume only one processing route defined

        ProcessingRoute processingRoute = null;
        Set<ProcessingRoute> processingRoutes = milestonesTemplateBlock.getProcessingRoutes();
        String route = csvFile.getString(PROCESSING_ROUTE);
        for (ProcessingRoute pr : processingRoutes) {
            if (pr.getName().equals(route)) {
                processingRoute = pr;
            }
        }

        if (processingRoute == null) {
            throw new ValidationException("Unknown processing route: " + route);
        }


        milestonesBlock.setProcessingRouteId(processingRoute.getId());
        milestonesBlock.getMilestones().addAll(milestoneMapper.toProjectMilestones(processingRoute.getMilestones(), project.getTemplate()));
        for (Milestone milestone : milestonesBlock.getMilestones()) {

            switch (milestone.getExternalId()) {
                case PLANNING:
                    LocalDate date = csvFile.getDate(PLANNING_CONSENT_DATE, DATE_FORMAT);
                    if (date != null) {

                        milestone.setMilestoneDate(date);
                        milestone.setMonetarySplit(csvFile.getIntegerOrNull(PLANNING_CONSENT_GRANT));
                        milestone.setMilestoneStatus(getMilestoneStatus(csvFile, PLANNING_CONSENT_STATUS));
                        milestone.setClaimStatus(getMilestoneClaimStatus(csvFile, PLANNING_CONSENT_CLAIM_STATUS));
                    }
                    break;
                case START_ON_SITE:
                    LocalDate sosDate = csvFile.getDate(SOS_DATE, DATE_FORMAT);
                    if (sosDate != null) {
                        milestone.setMilestoneDate(sosDate);
                        milestone.setMonetarySplit(csvFile.getIntegerOrNull(SOS_GRANT));
                        milestone.setMilestoneStatus(getMilestoneStatus(csvFile, SOS_STATUS));
                        milestone.setClaimStatus(getMilestoneClaimStatus(csvFile, SOS_CLAIM_STATUS));
                        handlePaymentsForMilestone(project, csvFile, milestone);
                    }
                    break;
                case COMPLETION:
                    LocalDate compDate = csvFile.getDate(COMPLETION_DATE, DATE_FORMAT);
                    if (compDate != null) {

                        milestone.setMilestoneDate(compDate);
                        milestone.setMonetarySplit(csvFile.getIntegerOrNull(COMPLETION_GRANT));
                        milestone.setMilestoneStatus(getMilestoneStatus(csvFile, COMPLETION_STATUS));
                        milestone.setClaimStatus(getMilestoneClaimStatus(csvFile, COMPLETION_CLAIM_STATUS));
                        handlePaymentsForMilestone(project, csvFile, milestone);
                    }
                    break;
                case INTERIM:
                    LocalDate intDate = csvFile.getDate(INTERIM_DATE, DATE_FORMAT);
                    if (intDate !=null) {
                        milestone.setMilestoneDate(intDate);
                        milestone.setMonetarySplit(csvFile.getIntegerOrNull(INTERIM_GRANT));
                        milestone.setMilestoneStatus(getMilestoneStatus(csvFile, INTERIM_STATUS));
                        milestone.setClaimStatus(getMilestoneClaimStatus(csvFile, INTERIM_CLAIM_STATUS));
                        handlePaymentsForMilestone(project, csvFile, milestone);
                    }
                    break;
            }



        }
    }

    private void handlePaymentsForMilestone(Project project, CSVFile csvFile, Milestone milestone) {

        if (!milestone.isApproved()) {
            return;
        }

        boolean completion = milestone.getExternalId().equals(COMPLETION);
        boolean interim = milestone.getExternalId().equals(INTERIM);
        boolean startOnSite = milestone.getExternalId().equals(START_ON_SITE);

        String columnName = null;
        if (completion) {
            columnName = COMPLETION_PAYMENT;
        } else if (interim) {
            columnName = INTERIM_PAYMENT;
        } else if (startOnSite) {
            columnName = START_ON_SITE_PAYMENT;
        }

        BigDecimal currencyValue = csvFile.getCurrencyValue(columnName);
        PaymentGroup pg = new PaymentGroup();

        ProjectLedgerEntry grantPayment = null;
        if (currencyValue != null) {
            grantPayment = createPayment(project, milestone, currencyValue, LedgerType.PAYMENT);
            pg.getLedgerEntries().add(grantPayment);

        }
        if (completion) {
            BigDecimal rcgf = csvFile.getCurrencyValue(RCGF_VALUE);
            if (rcgf != null && (rcgf.compareTo(BigDecimal.ZERO) > 0 )) {
                pg.getLedgerEntries().add(createPayment(project, milestone, rcgf, LedgerType.RCGF));
            }
            BigDecimal dpf = csvFile.getCurrencyValue(DPF_VALUE);
            if (dpf != null && (dpf.compareTo(BigDecimal.ZERO) > 0 )) {
                pg.getLedgerEntries().add(createPayment(project, milestone, dpf, LedgerType.DPF));
            }
        }
        if (!pg.getLedgerEntries().isEmpty()) {
            pg.setApprovalRequestedBy("IMS Migration");
            paymentGroupRepository.save(pg);
        }



        BigDecimal interimReclaim = csvFile.getCurrencyValue(INTERIM_PAYMENT_RECLAIM);
        BigDecimal completionPayment = csvFile.getCurrencyValue(COMPLETION_PAYMENT_RECLAIM);

        pg = new PaymentGroup();

        if (interimReclaim != null || completionPayment != null) {

            LocalDate reclaimDate = csvFile.getDate(RECLAIM_DATE, DATE_FORMAT);
            MilestoneStatus reclaimStatus = getMilestoneStatus(csvFile, RECLAIM_STATUS);
            ClaimStatus claimStatus = getMilestoneClaimStatus(csvFile, RECLAIM_GRANT_STATUS);

            boolean auth = ClaimStatus.Approved.equals(claimStatus);

            if (interim && interimReclaim != null) {

                pg.getLedgerEntries().add(createReclaim(project, interimReclaim, reclaimDate, "Interim Reclaim", auth, grantPayment));
            }

            if (completion && completionPayment != null) {
                pg.getLedgerEntries().add(createReclaim(project, completionPayment, reclaimDate, "Completion Reclaim", auth, grantPayment));

            }

            if (!pg.getLedgerEntries().isEmpty()) {
                pg.setApprovalRequestedBy("IMS Migration");
                paymentGroupRepository.save(pg);
            }
        }

    }

    private ProjectLedgerEntry createPayment(Project project, Milestone milestone, BigDecimal currencyValue, LedgerType type) {
        ProjectLedgerEntry entry = new ProjectLedgerEntry(
                project.getId(),
                milestone.getMilestoneDate().getYear(),
                milestone.getMilestoneDate().getMonthValue(), "IMS Claimed Milestone", milestone.getSummary(), currencyValue.negate(), LedgerStatus.Cleared);
        long rounded = currencyValue.setScale(0, BigDecimal.ROUND_HALF_UP).negate().longValue();
        switch (type) {
            case PAYMENT:
                milestone.setClaimedGrant(rounded);
                break;
            case RCGF:
                milestone.setClaimedRcgf(rounded);
                break;
            case DPF:
                milestone.setClaimedDpf(rounded);
                break;
            default:
                throw new ValidationException("Unable to create payments of this type: " + type);
        }
        entry.setAuthorisedOn(OffsetDateTime.of(milestone.getMilestoneDate(), LocalTime.NOON, ZoneOffset.UTC));
        entry.setAuthorisedBy(IMS_MIGRATION_USER);

        return populatePayment(entry, project, type);

    }
    private ProjectLedgerEntry createReclaim(Project project, BigDecimal currencyValue, LocalDate date, String summary, boolean auth, ProjectLedgerEntry grant ) {
        ProjectLedgerEntry entry = new ProjectLedgerEntry(
                project.getId(),
                date.getYear(),
                date.getMonthValue(), "IMS Claimed Milestone", summary, currencyValue, LedgerStatus.Cleared);
        if (auth) {
            entry.setAuthorisedOn(OffsetDateTime.of(date, LocalTime.NOON, ZoneOffset.UTC));
            entry.setAuthorisedBy(IMS_MIGRATION_USER);
        }
        entry.setReclaimOfPaymentId(grant.getId());
        return populatePayment(entry, project, LedgerType.PAYMENT);
    }

    private ProjectLedgerEntry populatePayment(ProjectLedgerEntry entry, Project project, LedgerType type) {

        entry.setBlockId(project.getSingleLatestBlockOfType(ProjectBlockType.Milestones).getId());
        entry.setLedgerType(type);
        entry.setCreatedOn(entry.getAuthorisedOn());
        entry.setLedgerSource(LedgerSource.IMS);
        entry.setProjectName(project.getTitle());
        entry.setProgrammeName(project.getProgramme().getName());
        if (project.getOrganisationGroupId() != null) {
            Organisation devOrg = organisationRepository.findOne(project.getDetailsBlock().getDevelopingOrganisationId());
            entry.setOrganisationId(devOrg.getId());
            entry.setVendorName(devOrg.getName());
        } else {
            entry.setVendorName(project.getOrganisation().getName());
            entry.setOrganisationId(project.getOrganisation().getId());
        }
        entry.setManagingOrganisation(project.getManagingOrganisation());
        entry.setCreatedBy(IMS_MIGRATION_USER);
        entry.setSapVendorId(project.getOrganisation().getsapVendorId());
        entry.setWbsCode(project.getProgramme().hasWbsCode() ? project.getProgramme().getWbsCode() : defaultWbsCode);


        projectLedgerRepository.save(entry);
        return entry;
    }


    public void handleUpdatesToGrantSource(Project project, CSVFile csvFile)  {
        ProjectMilestonesBlock milestonesBlock = project.getMilestonesBlock();
        Milestone sosMilestone = milestonesBlock.getMilestoneByExternalId(Milestone.START_ON_SITE_ID);
        Milestone completionMilestone = milestonesBlock.getMilestoneByExternalId(Milestone.COMPLETION_ID);

        BaseGrantBlock baseGrantBlock = project.getBaseGrantBlock();
        if (baseGrantBlock == null) {
            throw new ValidationException("Unable to find grant block for project with schemeId: " + project.getLegacyProjectCode());
        }

        if (sosMilestone != null && sosMilestone.isApproved()) {
            baseGrantBlock.setStartOnSiteMilestoneAuthorised(OffsetDateTime.of(sosMilestone.getMilestoneDate(), LocalTime.NOON, ZoneOffset.UTC));
        }
        if (completionMilestone != null && completionMilestone.isApproved()) {
            baseGrantBlock.setCompletionMilestoneAuthorised(OffsetDateTime.of(completionMilestone.getMilestoneDate(), LocalTime.NOON, ZoneOffset.UTC));
        }

        String tenure;
        if (csvFile.getHeaders().contains(TENURE_TYPE)) {
            tenure = csvFile.getString(TENURE_TYPE);
        } else {
            tenure = csvFile.getString(TENURE_FOR_GRANT_IMPORT);
        }
        TenureTypeAndUnits entry = baseGrantBlock.getTenureTypeAndUnitsEntry(tenure);
        if (entry == null) {
            throw new ValidationException("Unable to tenure of type "+tenure+" for project "+ project.getLegacyProjectCode());
        }

        entry.setTotalUnitsAtStartOnSite(csvFile.getInteger(STARTS_REPORTED));
        entry.setTotalUnitsAtCompletion(csvFile.getInteger(COMPLETIONS_REPORTED));

    }

    private MilestoneStatus getMilestoneStatus(CSVFile csvFile, String milestoneStatus) {
        String status = csvFile.getString(milestoneStatus);
        try {
            return  MilestoneStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Not possible to have Milestone with status of: " + status);
        }

    }

    private ClaimStatus getMilestoneClaimStatus(CSVFile csvFile, String milestoneClaimStatus) {
        String status = csvFile.getString(milestoneClaimStatus);
        try {
            return  ClaimStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Not possible to have Milestone with claim status of: " + status);
        }

    }

}
