/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.common.CSVFile;
import uk.gov.london.common.CSVRowSource;
import uk.gov.london.ops.report.implementation.LegacyImsProjectRepository;
import uk.gov.london.ops.report.implementation.LegacyImsReportedFiguresRepository;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

@Component
public class LegacyIMSDataImporter {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private LegacyImsProjectRepository legacyImsProjectRepository;

    @Autowired
    private LegacyImsReportedFiguresRepository legacyImsReportedFiguresRepository;

    public void importLegacyImsProject(InputStream is) {
        try {
            List<LegacyImsProject> entries = new CSVFile(is).loadData(
                    csv -> {
                        LegacyImsProject entry = new LegacyImsProject();
                        entry.setProgramme(csv.getString("programme"));
                        entry.setLeadOrgCode(csv.getString("lead_org_code"));
                        entry.setLeadOrgName(csv.getString("lead_org_name"));
                        entry.setConsortiumPartnership(csv.getString("consortium_partnership"));
                        entry.setDevOrg(csv.getString("dev_org"));
                        entry.setStatus(csv.getString("status"));
                        entry.setSchemeId(csv.getIntegerOrNull("scheme_id"));
                        entry.setProjectName(csv.getString("project_name"));
                        entry.setAddress(csv.getString("address"));
                        entry.setBorough(csv.getString("borough"));
                        entry.setPostCode(csv.getString("post_code"));
                        entry.setxCoordinate(csv.getIntegerOrNull("x_coordinate"));
                        entry.setyCoordinate(csv.getIntegerOrNull("y_coordinate"));
                        entry.setTenureType(csv.getString("tenure_type"));
                        entry.setGrantRequested(csv.getIntegerOrNull("grant_requested"));
                        entry.setTotalDevelopmentCost(csv.getIntegerOrNull("total_development_cost"));
                        entry.setTotalAffordableUnits(csv.getIntegerOrNull("total_affordable_units"));
                        entry.setOfWhichSupportedSpecialisedUnits(csv.getIntegerOrNull("of_which_supported_specialised_units"));
                        entry.setNilGrant(csv.getString("nil_grant") != null ? Boolean.parseBoolean(csv.getString("nil_grant")) : null);
                        entry.setGrant(csv.getIntegerOrNull("grant_value"));
                        entry.setRcgf(csv.getIntegerOrNull("rcgf_value"));
                        entry.setDpf(csv.getIntegerOrNull("dpf_value"));
                        entry.setOtherPublicSubsidy(csv.getIntegerOrNull("other_public_subsidy"));
                        entry.setOtherContributions(csv.getIntegerOrNull("other_contributions"));
                        entry.setInitialSales(csv.getCurrencyValue("initial_sales"));
                        entry.setEstimatedRentalIncomePaAffordableRent(csv.getIntegerOrNull("estimated_rental_income_pa_affordable_rent"));
                        entry.setEstimatedRentalIncomePaAffordableHomeOwnership(csv.getIntegerOrNull("estimated_rental_income_pa_affordable_home_ownership"));
                        entry.setOfferLineId(csv.getIntegerOrNull("offer_line_id"));
                        entry.setOfferLineSpId(csv.getIntegerOrNull("offer_line_sp_id"));
                        entry.setOfferLineSpSchemeId(csv.getIntegerOrNull("offer_line_sp_scheme_id"));
                        entry.setNumberOfSelfContainedUnits(csv.getIntegerOrNull("number_of_self_contained_units"));
                        entry.setNumberOfSharedUnits(csv.getIntegerOrNull("number_of_shared_units"));
                        entry.setNumberOfLargerHomes(csv.getIntegerOrNull("number_of_larger_homes"));
                        entry.setProsEligHbServiceCharge(csv.getString("pros_elig_hb_service_charge"));
                        entry.setProsNonEligHbServiceCharge(csv.getString("pros_non_elig_hb_service_charge"));
                        entry.setExpMarketValueRent(csv.getString("exp_market_value_rent"));
                        entry.setAveNetWklyRentAho(csv.getString("ave_net_wkly_rent_aho"));
                        entry.setClientGroup(csv.getString("client_group"));
                        entry.setSchemeStatusDescriptionAtPointOfMigration(csv.getString("scheme_status_description_at_point_of_migration"));
                        entry.setProcessingRoute(csv.getString("processing_route"));
                        entry.setPlanningConsentActualDate(csv.getString("planning_consent_actual_date"));
                        entry.setDetailedPlanningPermissionAchievedGrant(csv.getIntegerOrNull("detailed_planning_permission_achieved_grant"));
                        entry.setDetailedPlanningPermissionAchievedStatus(csv.getString("detailed_planning_permission_achieved_status"));
                        entry.setDetailedPlanningPermissionAchievedClaimStatus(csv.getString("detailed_planning_permission_achieved_claim_status"));
                        entry.setSosDate(csv.getString("sos_date"));
                        entry.setStartOnSiteGrant(getPercentageAsInt(csv, "start_on_site_grant"));
                        entry.setStartOnSiteStatus(csv.getString("start_on_site_status"));
                        entry.setStartOnSiteClaimStatus(csv.getString("start_on_site_claim_status"));
                        entry.setInterimDate(csv.getString("interim_date"));
                        entry.setInterimPaymentGrant(getPercentageAsInt(csv, "interim_payment_grant"));
                        entry.setInterimPaymentStatus(csv.getString("interim_payment_status"));
                        entry.setInterimPaymentClaimStatus(csv.getString("interim_payment_claim_status"));
                        entry.setCompletionDate(csv.getString("completion_date"));
                        entry.setCompletionGrant(getPercentageAsInt(csv, "completion_grant"));
                        entry.setCompletionStatus(csv.getString("completion_status"));
                        entry.setCompletionClaimStatus(csv.getString("completion_claim_status"));
                        entry.setReclaimJournalDate(csv.getString("reclaim_journal_date"));
                        entry.setReclaimGrant(csv.getIntegerOrNull("reclaim_grant"));
                        entry.setReclaimStatus(csv.getString("reclaim_status"));
                        entry.setReclaimGrantStatus(csv.getString("reclaim_grant_status"));
                        entry.setSosSpend(csv.getIntegerOrNull("sos_spend"));
                        entry.setSosPaymentReclaim(csv.getIntegerOrNull("sos_payment_reclaim"));
                        entry.setInterimPayment(getCurrencyValueAsInt(csv, "interim_payment"));
                        entry.setInterimPaymentReclaim(csv.getIntegerOrNull("interim_payment_reclaim"));
                        entry.setFcPayment(csv.getIntegerOrNull("fc_payment"));
                        entry.setFcPaymentReclaim(csv.getIntegerOrNull("fc_payment_reclaim"));
                        entry.setNb(csv.getIntegerOrNull("nb"));
                        entry.setRh(csv.getIntegerOrNull("rh"));
                        entry.setWheelchairUnits(csv.getIntegerOrNull("wheelchair_units"));
                        entry.setTotalUnitSize(csv.getCurrencyValue("total_unit_size"));
                        entry.setNbPeople1(csv.getIntegerOrNull("nb_people_1"));
                        entry.setNbPeople2(csv.getIntegerOrNull("nb_people_2"));
                        entry.setNbPeople3(csv.getIntegerOrNull("nb_people_3"));
                        entry.setNbPeople4(csv.getIntegerOrNull("nb_people_4"));
                        entry.setNbPeople5(csv.getIntegerOrNull("nb_people_5"));
                        entry.setNbPeople6(csv.getIntegerOrNull("nb_people_6"));
                        entry.setNbPeople7(csv.getIntegerOrNull("nb_people_7"));
                        entry.setNbPeople8Plus(csv.getIntegerOrNull("nb_people_8_plus"));
                        return entry;
                    }
            );

            legacyImsProjectRepository.deleteAll();
            legacyImsProjectRepository.saveAll(entries);
        }
        catch (IOException e) {
            log.error("error loading legacy IMS projects", e);
        }
    }

    private Integer getPercentageAsInt(CSVRowSource csv, String columnName) {
        String str = csv.getString(columnName);
        if (str != null && str.endsWith("%")) {
            str = str.substring(0, str.length() - 1);
            return Integer.parseInt(str);
        }
        return null;
    }

    private Integer getCurrencyValueAsInt(CSVRowSource csv, String columnName) {
        BigDecimal bd = csv.getCurrencyValue(columnName);
        return bd != null ? bd.intValue() : null;
    }

    public void importLegacyImsReportedFigures(InputStream is) {
        try {
            List<LegacyImsReportedFigures> entries = new CSVFile(is).loadData(
                    csv -> {
                        LegacyImsReportedFigures entry = new LegacyImsReportedFigures();
                        entry.setSchemeId(csv.getIntegerOrNull("scheme_id"));
                        entry.setProgramme(csv.getString("programme"));
                        entry.setTenureType(csv.getString("tenure_type"));
                        entry.setStartsAchieved(csv.getIntegerOrNull("starts_achieved"));
                        entry.setCompletionsAchieved(csv.getIntegerOrNull("completions_achieved"));
                        entry.setSosDate(csv.getString("sos_date"));
                        entry.setCompletionDate(csv.getString("completion_date"));
                        return entry;
                    }
            );

            legacyImsReportedFiguresRepository.deleteAll();
            legacyImsReportedFiguresRepository.saveAll(entries);
        }
        catch (IOException e) {
            log.error("error loading legacy IMS reported figures", e);
        }
    }

}
