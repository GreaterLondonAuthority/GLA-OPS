/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.state;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import org.apache.commons.lang3.EnumUtils;

@Entity(name = "project_state")
public class ProjectStateEntity {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "state_model")
    @Enumerated(EnumType.STRING)
    private StateModel stateModel;

    @Column(name = "status_name")
    private String statusName;

    @Column(name = "sub_status_name")
    private String subStatusName;

    @Column(name = "status_type")
    @Enumerated(EnumType.STRING)
    private ProjectStatus statusType;

    @Column(name = "sub_status_type")
    @Enumerated(EnumType.STRING)
    private ProjectSubStatus subStatusType;

    public ProjectStateEntity() {
    }

    public ProjectStateEntity(Integer id, StateModel stateModel, String statusName, String subStatusName,
            ProjectStatus statusType, ProjectSubStatus subStatusType) {
        this.id = id;
        this.stateModel = stateModel;
        this.statusName = statusName;
        this.subStatusName = subStatusName;
        this.statusType = statusType;
        this.subStatusType = subStatusType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public StateModel getStateModel() {
        return stateModel;
    }

    public void setStateModel(StateModel stateModel) {
        this.stateModel = stateModel;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getSubStatusName() {
        return subStatusName;
    }

    public void setSubStatusName(String subStatusName) {
        this.subStatusName = subStatusName;
    }

    public ProjectStatus getStatusType() {
        return statusType;
    }

    public static ProjectStatus getStatusType(String statusName, String subStatusName) {
        ProjectStateEntity projectStateEntity = ProjectStateService.find(statusName, subStatusName);

        if (projectStateEntity != null) {
            return projectStateEntity.getStatusType();
        }

        if (EnumUtils.isValidEnum(ProjectStatus.class, statusName)) {
            return ProjectStatus.valueOf(statusName);
        }

        return null;
    }

    public void setStatusType(ProjectStatus statusType) {
        this.statusType = statusType;
    }

    public void setSubStatusType(ProjectSubStatus subStatusType) {
        this.subStatusType = subStatusType;
    }

    public ProjectSubStatus getSubStatusType() {
        return subStatusType;
    }

    public static ProjectSubStatus getSubStatusType(String statusName, String subStatusName) {
        ProjectStateEntity projectStateEntity = ProjectStateService.find(statusName, subStatusName);

        if (projectStateEntity != null) {
            return projectStateEntity.getSubStatusType();
        }

        if (EnumUtils.isValidEnum(ProjectSubStatus.class, subStatusName)) {
            return ProjectSubStatus.valueOf(subStatusName);
        }

        return null;
    }

}
