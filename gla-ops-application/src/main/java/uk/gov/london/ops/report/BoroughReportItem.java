/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report;

import uk.gov.london.ops.framework.jpa.NonJoin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Entity which represents the Borough report entity.
 * This entity is a view, not an actual table.
 *
 * View name: v_borough_report
 *
 * @author  Antonio Perez Dieppa
 */

@Entity(name = "v_borough_report")
@NonJoin("Views don't define joins")
public class BoroughReportItem {

    @Id
    @Column(name = "project_id")
    private int projectId;

    @Column(name = "programme_id")
    private Integer programmeId;

    @Column(name = "programme_name")
    private String programmeName;

    @Column(name = "template_id")
    private Integer templateId;

    @Column(name = "project_type")
    private String projectType;

    @Column(name = "status")
    private String status;

    @Column(name = "project_title")
    private String projectTitle;

    @Column(name = "date_submitted")
    private Date dateSubmitted;

    @Column(name = "description")
    private String description;

    @Column(name = "org_id")
    private Integer orgId;

    @Column(name = "managing_organisation_id")
    private Integer managingOrganisationId;

    @Column(name = "org_type")
    private String orgType;

    @Column(name = "lead_org_name")
    private String leadOrgName;

    @Column(name = "developing_org")
    private String developingOrg;

    @Column(name = "borough")
    private String borough;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "x_coord")
    private String xCoord;

    @Column(name = "y_coord")
    private String yCoord;

    @Column(name = "planning_ref")
    private String planningRef;

    @Column(name = "ms_start_site")
    private Date startSite;

    @Column(name = "ms_completion")
    private Date completion;

    @Column(name = "ms_processing_route")
    private String processingRoute;

    @Column(name = "s106_dev_led")
    private Integer s016DevLed;

    @Column(name = "add_aff_units_dev_led")
    private Integer addAffUnitsDevLed;

    @Column(name = "affordable_criteria_met_dev_led")
    private Boolean affordableCriteriaMetDevLed;

    @Column(name = "lar_units")
    private Integer larUnits;

    @Column(name = "llr_units")
    private Integer llrUnits;

    @Column(name = "lso_units")
    private Integer lsoUnits;

    @Column(name = "other_units")
    private Integer otherUnits;

    @Column(name = "q_other_aff_type")
    private String otherAffType;

    @Column(name = "q_planning_status")
    private String planningStatus;

    @Column(name = "q_land_status")
    private String landStatus;

    @Column(name = "q_larger_aff_homes")
    private Integer largerAffHomes;

    @Column(name = "eg_supp_units")
    private Integer suppUnits;

    @Column(name = "aq_wheelchair_units")
    private Integer wheelchairUnits;

    @Column(name = "aq_client_group")
    private String clientGroup;

    public BoroughReportItem() {}

    public BoroughReportItem(Integer managingOrganisationId) {
        this.managingOrganisationId = managingOrganisationId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public Integer getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(Integer programmeId) {
        this.programmeId = programmeId;
    }

    public String getProgrammeName() {
        return programmeName;
    }

    public void setProgrammeName(String programmeName) {
        this.programmeName = programmeName;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public Date getDateSubmitted() {
        return dateSubmitted;
    }

    public void setDateSubmitted(Date dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public Integer getManagingOrganisationId() {
        return managingOrganisationId;
    }

    public void setManagingOrganisationId(Integer managingOrganisationId) {
        this.managingOrganisationId = managingOrganisationId;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public String getLeadOrgName() {
        return leadOrgName;
    }

    public void setLeadOrgName(String leadOrgName) {
        this.leadOrgName = leadOrgName;
    }

    public String getDevelopingOrg() {
        return developingOrg;
    }

    public void setDevelopingOrg(String developingOrg) {
        this.developingOrg = developingOrg;
    }

    public String getBorough() {
        return borough;
    }

    public void setBorough(String borough) {
        this.borough = borough;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getxCoord() {
        return xCoord;
    }

    public void setxCoord(String xCoord) {
        this.xCoord = xCoord;
    }

    public String getyCoord() {
        return yCoord;
    }

    public void setyCoord(String yCoord) {
        this.yCoord = yCoord;
    }

    public String getPlanningRef() {
        return planningRef;
    }

    public void setPlanningRef(String planningRef) {
        this.planningRef = planningRef;
    }

    public Date getStartSite() {
        return startSite;
    }

    public void setStartSite(Date startSite) {
        this.startSite = startSite;
    }

    public Date getCompletion() {
        return completion;
    }

    public void setCompletion(Date completion) {
        this.completion = completion;
    }

    public String getProcessingRoute() {
        return processingRoute;
    }

    public void setProcessingRoute(String processingRoute) {
        this.processingRoute = processingRoute;
    }

    public Integer getS016DevLed() {
        return s016DevLed;
    }

    public void setS016DevLed(Integer s016DevLed) {
        this.s016DevLed = s016DevLed;
    }

    public Integer getAddAffUnitsDevLed() {
        return addAffUnitsDevLed;
    }

    public void setAddAffUnitsDevLed(Integer addAffUnitsDevLed) {
        this.addAffUnitsDevLed = addAffUnitsDevLed;
    }

    public Boolean getAffordableCriteriaMetDevLed() {
        return affordableCriteriaMetDevLed;
    }

    public void setAffordableCriteriaMetDevLed(Boolean affordableCriteriaMetDevLed) {
        this.affordableCriteriaMetDevLed = affordableCriteriaMetDevLed;
    }

    public Integer getLarUnits() {
        return larUnits;
    }

    public void setLarUnits(Integer larUnits) {
        this.larUnits = larUnits;
    }

    public Integer getLlrUnits() {
        return llrUnits;
    }

    public void setLlrUnits(Integer llrUnits) {
        this.llrUnits = llrUnits;
    }

    public Integer getLsoUnits() {
        return lsoUnits;
    }

    public void setLsoUnits(Integer lsoUnits) {
        this.lsoUnits = lsoUnits;
    }

    public Integer getOtherUnits() {
        return otherUnits;
    }

    public void setOtherUnits(Integer otherUnits) {
        this.otherUnits = otherUnits;
    }

    public String getOtherAffType() {
        return otherAffType;
    }

    public void setOtherAffType(String otherAffType) {
        this.otherAffType = otherAffType;
    }

    public String getPlanningStatus() {
        return planningStatus;
    }

    public void setPlanningStatus(String planningStatus) {
        this.planningStatus = planningStatus;
    }

    public String getLandStatus() {
        return landStatus;
    }

    public void setLandStatus(String landStatus) {
        this.landStatus = landStatus;
    }

    public Integer getLargerAffHomes() {
        return largerAffHomes;
    }

    public void setLargerAffHomes(Integer largerAffHomes) {
        this.largerAffHomes = largerAffHomes;
    }

    public Integer getSuppUnits() {
        return suppUnits;
    }

    public void setSuppUnits(Integer suppUnits) {
        this.suppUnits = suppUnits;
    }

    public Integer getWheelchairUnits() {
        return wheelchairUnits;
    }

    public void setWheelchairUnits(Integer wheelchairUnits) {
        this.wheelchairUnits = wheelchairUnits;
    }

    public String getClientGroup() {
        return clientGroup;
    }

    public void setClientGroup(String clientGroup) {
        this.clientGroup = clientGroup;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoroughReportItem that = (BoroughReportItem) o;
        return projectId == that.projectId;

    }

    @Override
    public int hashCode() {
        return projectId;
    }
}

