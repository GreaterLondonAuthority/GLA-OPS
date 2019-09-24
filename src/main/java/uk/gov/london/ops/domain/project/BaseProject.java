/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.project.state.ProjectStateEntity;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.project.state.ProjectSubStatus;
import uk.gov.london.ops.framework.annotations.PermissionRequired;

import javax.persistence.*;
import java.util.List;

import static uk.gov.london.ops.service.PermissionType.PROJ_VIEW_RECOMMENDATION;

@MappedSuperclass
public class BaseProject {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_seq_gen")
    @SequenceGenerator(name = "project_seq_gen", sequenceName = "project_seq", initialValue = 10000, allocationSize = 1)
    protected Integer id;

    @Transient
    protected String title;

    @Column(name = "status")
    protected String statusName = ProjectStatus.Draft.name();

    @Column(name = "substatus")
    protected String subStatusName;

    @Column(name = "recommendation")
    @Enumerated(EnumType.STRING)
    protected Project.Recommendation recommendation;

    @Transient
    private List<NamedProjectBlock> projectBlocksSorted;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "org_id", nullable = false)
    private Organisation organisation;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    private Organisation managingOrganisation;

    @Column(name = "marked_for_corporate")
    private boolean markedForCorporate;

    @Column(name = "info_message")
    private String infoMessage;

    @Transient
    private Integer templateId;

    @Transient
    private Integer programmeId;

    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<NamedProjectBlock> getProjectBlocksSorted() {
        return projectBlocksSorted;
    }

    public void setProjectBlocksSorted(List<NamedProjectBlock> projectBlocksSorted) {
        this.projectBlocksSorted = projectBlocksSorted;
    }

    public String getTitle() {
        return title;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public Organisation getManagingOrganisation() {
        return managingOrganisation;
    }

    public void setManagingOrganisation(Organisation managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
    }


    public String getSubStatusName() {
        return subStatusName;
    }

    public void setSubStatusName(String subStatusName) {
        this.subStatusName = subStatusName;
    }

    public void setSubStatus(ProjectSubStatus subStatus) {
        if (subStatus != null) {
            setSubStatusName(subStatus.name());
        }
    }

    @JsonProperty(value = "statusType", access = JsonProperty.Access.READ_ONLY)
    public ProjectStatus getStatusType() {
        return ProjectStateEntity.getStatusType(statusName, subStatusName);
    }

    @JsonProperty(value = "subStatusType", access = JsonProperty.Access.READ_ONLY)
    public ProjectSubStatus getSubStatusType() {
        return ProjectStateEntity.getSubStatusType(statusName, subStatusName);
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
        this.subStatusName = null;
    }

    public void setStatus(ProjectStatus status) {
        if (status != null) {
            setStatusName(status.name());
        }
    }

    public String getStatusName() {
        return statusName;
    }

    @PermissionRequired({PROJ_VIEW_RECOMMENDATION})
    public Project.Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Project.Recommendation recommendation) {
        this.recommendation = recommendation;
        if (recommendation != null) {
            this.setSubStatus(ProjectSubStatus.Recommended);
        }
    }

    public boolean isComplete() {
        return this.getProjectBlocksSorted().stream().allMatch(NamedProjectBlock::isComplete);
    }

    public boolean isMarkedForCorporate() {
        return markedForCorporate;
    }

    public void setMarkedForCorporate(boolean markedForCorporate) {
        this.markedForCorporate = markedForCorporate;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public Integer getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(Integer programmeId) {
        this.programmeId = programmeId;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }
}
