/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity(name = "legacy_ims_project")
public class LegacyImsProject implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "legacy_ims_project_seq_gen")
    @SequenceGenerator(name = "legacy_ims_project_seq_gen", sequenceName = "legacy_ims_project_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "programme")
    private String programme;

    @Column(name = "lead_org_code")
    private String leadOrgCode;

    @Column(name = "lead_org_name")
    private String leadOrgName;

    @Column(name = "consortium_partnership")
    private String consortiumPartnership;

    @Column(name = "dev_org")
    private String devOrg;

    @Column(name = "status")
    private String status;

    @Column(name = "scheme_id")
    private Integer schemeId;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "address")
    private String address;

    @Column(name = "borough")
    private String borough;

    @Column(name = "post_code")
    private String postCode;

    @Column(name = "x_coordinate")
    private Integer xCoordinate;

    @Column(name = "y_coordinate")
    private Integer yCoordinate;

    @Column(name = "tenure_type")
    private String tenureType;

    @Column(name = "grant_requested")
    private Integer grantRequested;

    @Column(name = "total_development_cost")
    private Integer totalDevelopmentCost;

    @Column(name = "total_affordable_units")
    private Integer totalAffordableUnits;

    @Column(name = "of_which_supported_specialised_units")
    private Integer ofWhichSupportedSpecialisedUnits;

    @Column(name = "nil_grant")
    private Boolean nilGrant;

    @Column(name = "grant_value")
    private Integer grant;

    @Column(name = "rcgf_value")
    private Integer rcgf;

    @Column(name = "dpf_value")
    private Integer dpf;

    @Column(name = "other_public_subsidy")
    private Integer otherPublicSubsidy;

    @Column(name = "other_contributions")
    private Integer otherContributions;

    @Column(name = "initial_sales")
    private BigDecimal initialSales;

    @Column(name = "estimated_rental_income_pa_affordable_rent")
    private Integer estimatedRentalIncomePaAffordableRent;

    @Column(name = "estimated_rental_income_pa_affordable_home_ownership")
    private Integer estimatedRentalIncomePaAffordableHomeOwnership;

    @Column(name = "offer_line_id")
    private Integer offerLineId;

    @Column(name = "offer_line_sp_id")
    private Integer offerLineSpId;

    @Column(name = "offer_line_sp_scheme_id")
    private Integer offerLineSpSchemeId;

    @Column(name = "number_of_self_contained_units")
    private Integer numberOfSelfContainedUnits;

    @Column(name = "number_of_shared_units")
    private Integer numberOfSharedUnits;

    @Column(name = "number_of_larger_homes")
    private Integer numberOfLargerHomes;

    @Column(name = "pros_elig_hb_service_charge")
    private String prosEligHbServiceCharge;

    @Column(name = "pros_non_elig_hb_service_charge")
    private String prosNonEligHbServiceCharge;

    @Column(name = "exp_market_value_rent")
    private String expMarketValueRent;

    @Column(name = "ave_net_wkly_rent_aho")
    private String aveNetWklyRentAho;

    @Column(name = "client_group")
    private String clientGroup;

    @Column(name = "scheme_status_description_at_point_of_migration")
    private String schemeStatusDescriptionAtPointOfMigration;

    @Column(name = "processing_route")
    private String processingRoute;

    @Column(name = "planning_consent_actual_date")
    private String planningConsentActualDate;

    @Column(name = "detailed_planning_permission_achieved_grant")
    private Integer detailedPlanningPermissionAchievedGrant;

    @Column(name = "detailed_planning_permission_achieved_status")
    private String detailedPlanningPermissionAchievedStatus;

    @Column(name = "detailed_planning_permission_achieved_claim_status")
    private String detailedPlanningPermissionAchievedClaimStatus;

    @Column(name = "sos_date")
    private String sosDate;

    @Column(name = "start_on_site_grant")
    private Integer startOnSiteGrant;

    @Column(name = "start_on_site_status")
    private String startOnSiteStatus;

    @Column(name = "start_on_site_claim_status")
    private String startOnSiteClaimStatus;

    @Column(name = "interim_date")
    private String interimDate;

    @Column(name = "interim_payment_grant")
    private Integer interimPaymentGrant;

    @Column(name = "interim_payment_status")
    private String interimPaymentStatus;

    @Column(name = "interim_payment_claim_status")
    private String interimPaymentClaimStatus;

    @Column(name = "completion_date")
    private String completionDate;

    @Column(name = "completion_grant")
    private Integer completionGrant;

    @Column(name = "completion_status")
    private String completionStatus;

    @Column(name = "completion_claim_status")
    private String completionClaimStatus;

    @Column(name = "reclaim_journal_date")
    private String reclaimJournalDate;

    @Column(name = "reclaim_grant")
    private Integer reclaimGrant;

    @Column(name = "reclaim_status")
    private String reclaimStatus;

    @Column(name = "reclaim_grant_status")
    private String reclaimGrantStatus;

    @Column(name = "sos_spend")
    private Integer sosSpend;

    @Column(name = "sos_payment_reclaim")
    private Integer sosPaymentReclaim;

    @Column(name = "interim_payment")
    private Integer interimPayment;

    @Column(name = "interim_payment_reclaim")
    private Integer interimPaymentReclaim;

    @Column(name = "fc_payment")
    private Integer fcPayment;

    @Column(name = "fc_payment_reclaim")
    private Integer fcPaymentReclaim;

    @Column(name = "nb")
    private Integer nb;
    
    @Column(name = "rh")
    private Integer rh;
    
    @Column(name = "wheelchair_units")
    private Integer wheelchairUnits;
    
    @Column(name = "total_unit_size")
    private BigDecimal totalUnitSize;

    @Column(name = "nb_people_1")
    private Integer nbPeople1;

    @Column(name = "nb_people_2")
    private Integer nbPeople2;

    @Column(name = "nb_people_3")
    private Integer nbPeople3;

    @Column(name = "nb_people_4")
    private Integer nbPeople4;

    @Column(name = "nb_people_5")
    private Integer nbPeople5;

    @Column(name = "nb_people_6")
    private Integer nbPeople6;

    @Column(name = "nb_people_7")
    private Integer nbPeople7;

    @Column(name = "nb_people_8_plus")
    private Integer nbPeople8Plus;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public String getLeadOrgCode() {
        return leadOrgCode;
    }

    public void setLeadOrgCode(String leadOrgCode) {
        this.leadOrgCode = leadOrgCode;
    }

    public String getLeadOrgName() {
        return leadOrgName;
    }

    public void setLeadOrgName(String leadOrgName) {
        this.leadOrgName = leadOrgName;
    }

    public String getConsortiumPartnership() {
        return consortiumPartnership;
    }

    public void setConsortiumPartnership(String consortiumPartnership) {
        this.consortiumPartnership = consortiumPartnership;
    }

    public String getDevOrg() {
        return devOrg;
    }

    public void setDevOrg(String devOrg) {
        this.devOrg = devOrg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(Integer schemeId) {
        this.schemeId = schemeId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBorough() {
        return borough;
    }

    public void setBorough(String borough) {
        this.borough = borough;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public Integer getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(Integer xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public Integer getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(Integer yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public String getTenureType() {
        return tenureType;
    }

    public void setTenureType(String tenureType) {
        this.tenureType = tenureType;
    }

    public Integer getGrantRequested() {
        return grantRequested;
    }

    public void setGrantRequested(Integer grantRequested) {
        this.grantRequested = grantRequested;
    }

    public Integer getTotalDevelopmentCost() {
        return totalDevelopmentCost;
    }

    public void setTotalDevelopmentCost(Integer totalDevelopmentCost) {
        this.totalDevelopmentCost = totalDevelopmentCost;
    }

    public Integer getTotalAffordableUnits() {
        return totalAffordableUnits;
    }

    public void setTotalAffordableUnits(Integer totalAffordableUnits) {
        this.totalAffordableUnits = totalAffordableUnits;
    }

    public Integer getOfWhichSupportedSpecialisedUnits() {
        return ofWhichSupportedSpecialisedUnits;
    }

    public void setOfWhichSupportedSpecialisedUnits(Integer ofWhichSupportedSpecialisedUnits) {
        this.ofWhichSupportedSpecialisedUnits = ofWhichSupportedSpecialisedUnits;
    }

    public Boolean getNilGrant() {
        return nilGrant;
    }

    public void setNilGrant(Boolean nilGrant) {
        this.nilGrant = nilGrant;
    }

    public Integer getGrant() {
        return grant;
    }

    public void setGrant(Integer grant) {
        this.grant = grant;
    }

    public Integer getRcgf() {
        return rcgf;
    }

    public void setRcgf(Integer rcgf) {
        this.rcgf = rcgf;
    }

    public Integer getDpf() {
        return dpf;
    }

    public void setDpf(Integer dpf) {
        this.dpf = dpf;
    }

    public Integer getOtherPublicSubsidy() {
        return otherPublicSubsidy;
    }

    public void setOtherPublicSubsidy(Integer otherPublicSubsidy) {
        this.otherPublicSubsidy = otherPublicSubsidy;
    }

    public Integer getOtherContributions() {
        return otherContributions;
    }

    public void setOtherContributions(Integer otherContributions) {
        this.otherContributions = otherContributions;
    }

    public BigDecimal getInitialSales() {
        return initialSales;
    }

    public void setInitialSales(BigDecimal initialSales) {
        this.initialSales = initialSales;
    }

    public Integer getEstimatedRentalIncomePaAffordableRent() {
        return estimatedRentalIncomePaAffordableRent;
    }

    public void setEstimatedRentalIncomePaAffordableRent(Integer estimatedRentalIncomePaAffordableRent) {
        this.estimatedRentalIncomePaAffordableRent = estimatedRentalIncomePaAffordableRent;
    }

    public Integer getEstimatedRentalIncomePaAffordableHomeOwnership() {
        return estimatedRentalIncomePaAffordableHomeOwnership;
    }

    public void setEstimatedRentalIncomePaAffordableHomeOwnership(Integer estimatedRentalIncomePaAffordableHomeOwnership) {
        this.estimatedRentalIncomePaAffordableHomeOwnership = estimatedRentalIncomePaAffordableHomeOwnership;
    }

    public Integer getOfferLineId() {
        return offerLineId;
    }

    public void setOfferLineId(Integer offerLineId) {
        this.offerLineId = offerLineId;
    }

    public Integer getOfferLineSpId() {
        return offerLineSpId;
    }

    public void setOfferLineSpId(Integer offerLineSpId) {
        this.offerLineSpId = offerLineSpId;
    }

    public Integer getOfferLineSpSchemeId() {
        return offerLineSpSchemeId;
    }

    public void setOfferLineSpSchemeId(Integer offerLineSpSchemeId) {
        this.offerLineSpSchemeId = offerLineSpSchemeId;
    }

    public Integer getNumberOfSelfContainedUnits() {
        return numberOfSelfContainedUnits;
    }

    public void setNumberOfSelfContainedUnits(Integer numberOfSelfContainedUnits) {
        this.numberOfSelfContainedUnits = numberOfSelfContainedUnits;
    }

    public Integer getNumberOfSharedUnits() {
        return numberOfSharedUnits;
    }

    public void setNumberOfSharedUnits(Integer numberOfSharedUnits) {
        this.numberOfSharedUnits = numberOfSharedUnits;
    }

    public Integer getNumberOfLargerHomes() {
        return numberOfLargerHomes;
    }

    public void setNumberOfLargerHomes(Integer numberOfLargerHomes) {
        this.numberOfLargerHomes = numberOfLargerHomes;
    }

    public String getProsEligHbServiceCharge() {
        return prosEligHbServiceCharge;
    }

    public void setProsEligHbServiceCharge(String prosEligHbServiceCharge) {
        this.prosEligHbServiceCharge = prosEligHbServiceCharge;
    }

    public String getProsNonEligHbServiceCharge() {
        return prosNonEligHbServiceCharge;
    }

    public void setProsNonEligHbServiceCharge(String prosNonEligHbServiceCharge) {
        this.prosNonEligHbServiceCharge = prosNonEligHbServiceCharge;
    }

    public String getExpMarketValueRent() {
        return expMarketValueRent;
    }

    public void setExpMarketValueRent(String expMarketValueRent) {
        this.expMarketValueRent = expMarketValueRent;
    }

    public String getAveNetWklyRentAho() {
        return aveNetWklyRentAho;
    }

    public void setAveNetWklyRentAho(String aveNetWklyRentAho) {
        this.aveNetWklyRentAho = aveNetWklyRentAho;
    }

    public String getClientGroup() {
        return clientGroup;
    }

    public void setClientGroup(String clientGroup) {
        this.clientGroup = clientGroup;
    }

    public String getSchemeStatusDescriptionAtPointOfMigration() {
        return schemeStatusDescriptionAtPointOfMigration;
    }

    public void setSchemeStatusDescriptionAtPointOfMigration(String schemeStatusDescriptionAtPointOfMigration) {
        this.schemeStatusDescriptionAtPointOfMigration = schemeStatusDescriptionAtPointOfMigration;
    }

    public String getProcessingRoute() {
        return processingRoute;
    }

    public void setProcessingRoute(String processingRoute) {
        this.processingRoute = processingRoute;
    }

    public String getPlanningConsentActualDate() {
        return planningConsentActualDate;
    }

    public void setPlanningConsentActualDate(String planningConsentActualDate) {
        this.planningConsentActualDate = planningConsentActualDate;
    }

    public Integer getDetailedPlanningPermissionAchievedGrant() {
        return detailedPlanningPermissionAchievedGrant;
    }

    public void setDetailedPlanningPermissionAchievedGrant(Integer detailedPlanningPermissionAchievedGrant) {
        this.detailedPlanningPermissionAchievedGrant = detailedPlanningPermissionAchievedGrant;
    }

    public String getDetailedPlanningPermissionAchievedStatus() {
        return detailedPlanningPermissionAchievedStatus;
    }

    public void setDetailedPlanningPermissionAchievedStatus(String detailedPlanningPermissionAchievedStatus) {
        this.detailedPlanningPermissionAchievedStatus = detailedPlanningPermissionAchievedStatus;
    }

    public String getDetailedPlanningPermissionAchievedClaimStatus() {
        return detailedPlanningPermissionAchievedClaimStatus;
    }

    public void setDetailedPlanningPermissionAchievedClaimStatus(String detailedPlanningPermissionAchievedClaimStatus) {
        this.detailedPlanningPermissionAchievedClaimStatus = detailedPlanningPermissionAchievedClaimStatus;
    }

    public String getSosDate() {
        return sosDate;
    }

    public void setSosDate(String sosDate) {
        this.sosDate = sosDate;
    }

    public Integer getStartOnSiteGrant() {
        return startOnSiteGrant;
    }

    public void setStartOnSiteGrant(Integer startOnSiteGrant) {
        this.startOnSiteGrant = startOnSiteGrant;
    }

    public String getStartOnSiteStatus() {
        return startOnSiteStatus;
    }

    public void setStartOnSiteStatus(String startOnSiteStatus) {
        this.startOnSiteStatus = startOnSiteStatus;
    }

    public String getStartOnSiteClaimStatus() {
        return startOnSiteClaimStatus;
    }

    public void setStartOnSiteClaimStatus(String startOnSiteClaimStatus) {
        this.startOnSiteClaimStatus = startOnSiteClaimStatus;
    }

    public String getInterimDate() {
        return interimDate;
    }

    public void setInterimDate(String interimDate) {
        this.interimDate = interimDate;
    }

    public Integer getInterimPaymentGrant() {
        return interimPaymentGrant;
    }

    public void setInterimPaymentGrant(Integer interimPaymentGrant) {
        this.interimPaymentGrant = interimPaymentGrant;
    }

    public String getInterimPaymentStatus() {
        return interimPaymentStatus;
    }

    public void setInterimPaymentStatus(String interimPaymentStatus) {
        this.interimPaymentStatus = interimPaymentStatus;
    }

    public String getInterimPaymentClaimStatus() {
        return interimPaymentClaimStatus;
    }

    public void setInterimPaymentClaimStatus(String interimPaymentClaimStatus) {
        this.interimPaymentClaimStatus = interimPaymentClaimStatus;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

    public Integer getCompletionGrant() {
        return completionGrant;
    }

    public void setCompletionGrant(Integer completionGrant) {
        this.completionGrant = completionGrant;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public String getCompletionClaimStatus() {
        return completionClaimStatus;
    }

    public void setCompletionClaimStatus(String completionClaimStatus) {
        this.completionClaimStatus = completionClaimStatus;
    }

    public String getReclaimJournalDate() {
        return reclaimJournalDate;
    }

    public void setReclaimJournalDate(String reclaimJournalDate) {
        this.reclaimJournalDate = reclaimJournalDate;
    }

    public Integer getReclaimGrant() {
        return reclaimGrant;
    }

    public void setReclaimGrant(Integer reclaimGrant) {
        this.reclaimGrant = reclaimGrant;
    }

    public String getReclaimStatus() {
        return reclaimStatus;
    }

    public void setReclaimStatus(String reclaimStatus) {
        this.reclaimStatus = reclaimStatus;
    }

    public String getReclaimGrantStatus() {
        return reclaimGrantStatus;
    }

    public void setReclaimGrantStatus(String reclaimGrantStatus) {
        this.reclaimGrantStatus = reclaimGrantStatus;
    }

    public Integer getSosSpend() {
        return sosSpend;
    }

    public void setSosSpend(Integer sosSpend) {
        this.sosSpend = sosSpend;
    }

    public Integer getSosPaymentReclaim() {
        return sosPaymentReclaim;
    }

    public void setSosPaymentReclaim(Integer sosPaymentReclaim) {
        this.sosPaymentReclaim = sosPaymentReclaim;
    }

    public Integer getInterimPayment() {
        return interimPayment;
    }

    public void setInterimPayment(Integer interimPayment) {
        this.interimPayment = interimPayment;
    }

    public Integer getInterimPaymentReclaim() {
        return interimPaymentReclaim;
    }

    public void setInterimPaymentReclaim(Integer interimPaymentReclaim) {
        this.interimPaymentReclaim = interimPaymentReclaim;
    }

    public Integer getFcPayment() {
        return fcPayment;
    }

    public void setFcPayment(Integer fcPayment) {
        this.fcPayment = fcPayment;
    }

    public Integer getFcPaymentReclaim() {
        return fcPaymentReclaim;
    }

    public void setFcPaymentReclaim(Integer fcPaymentReclaim) {
        this.fcPaymentReclaim = fcPaymentReclaim;
    }

    public Integer getNb() {
        return nb;
    }

    public void setNb(Integer nb) {
        this.nb = nb;
    }

    public Integer getRh() {
        return rh;
    }

    public void setRh(Integer rh) {
        this.rh = rh;
    }

    public Integer getWheelchairUnits() {
        return wheelchairUnits;
    }

    public void setWheelchairUnits(Integer wheelchairUnits) {
        this.wheelchairUnits = wheelchairUnits;
    }

    public BigDecimal getTotalUnitSize() {
        return totalUnitSize;
    }

    public void setTotalUnitSize(BigDecimal totalUnitSize) {
        this.totalUnitSize = totalUnitSize;
    }

    public Integer getNbPeople1() {
        return nbPeople1;
    }

    public void setNbPeople1(Integer nbPeople1) {
        this.nbPeople1 = nbPeople1;
    }

    public Integer getNbPeople2() {
        return nbPeople2;
    }

    public void setNbPeople2(Integer nbPeople2) {
        this.nbPeople2 = nbPeople2;
    }

    public Integer getNbPeople3() {
        return nbPeople3;
    }

    public void setNbPeople3(Integer nbPeople3) {
        this.nbPeople3 = nbPeople3;
    }

    public Integer getNbPeople4() {
        return nbPeople4;
    }

    public void setNbPeople4(Integer nbPeople4) {
        this.nbPeople4 = nbPeople4;
    }

    public Integer getNbPeople5() {
        return nbPeople5;
    }

    public void setNbPeople5(Integer nbPeople5) {
        this.nbPeople5 = nbPeople5;
    }

    public Integer getNbPeople6() {
        return nbPeople6;
    }

    public void setNbPeople6(Integer nbPeople6) {
        this.nbPeople6 = nbPeople6;
    }

    public Integer getNbPeople7() {
        return nbPeople7;
    }

    public void setNbPeople7(Integer nbPeople7) {
        this.nbPeople7 = nbPeople7;
    }

    public Integer getNbPeople8Plus() {
        return nbPeople8Plus;
    }

    public void setNbPeople8Plus(Integer nbPeople8Plus) {
        this.nbPeople8Plus = nbPeople8Plus;
    }
}
