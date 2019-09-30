/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import liquibase.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.project.state.ProjectStateEntity;
import uk.gov.london.ops.domain.project.state.ProjectStatus;
import uk.gov.london.ops.domain.project.state.ProjectSubStatus;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.domain.template.Template;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.service.ProgrammeService;
import uk.gov.london.ops.service.UserService;
import uk.gov.london.ops.service.project.state.ProjectState;
import uk.gov.london.ops.service.project.state.StateModel;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectStateService {
    public static Set<ProjectStateEntity> projectStates;
    private static HashMap<StateModel,List<ProjectState>> allProjectStates;
    private static final Logger log = LoggerFactory.getLogger(ProjectStateEntity.class);

    @Autowired
    private UserService userService;

    @Autowired
    ProgrammeService programmeService;

    static {
        try {
            allProjectStates = new HashMap<>();
            projectStates = new HashSet<>(new CSVFile(ProjectStateEntity.class.getResourceAsStream("ProjectStates.csv")).loadData(csvRow -> {
                StateModel stateModel = StateModel.valueOf(csvRow.getString("STATE_MODEL"));
                String statusName = csvRow.getString("STATUS_NAME");
                String subStatusName = StringUtils.isNotEmpty(csvRow.getString("SUB_STATUS_NAME")) ? csvRow.getString("SUB_STATUS_NAME") : null;

                addStatusToMap(stateModel,new ProjectState(statusName,subStatusName));

                    return new ProjectStateEntity(
                            csvRow.getInteger("ID"),
                            stateModel,
                            statusName,
                            subStatusName,
                            StringUtils.isNotEmpty(csvRow.getString("STATUS_TYPE")) ? ProjectStatus.valueOf(csvRow.getString("STATUS_TYPE")) : null,
                            StringUtils.isNotEmpty(csvRow.getString("SUB_STATUS_TYPE")) ? ProjectSubStatus.valueOf(csvRow.getString("SUB_STATUS_TYPE")) : null);

            }));
        } catch (IOException e) {
            log.error("failed to load ProjectStates.csv", e);
        }
    }

    public static Set<String> getAvailableStatuses() {
        return projectStates.stream().map(ProjectStateEntity::getStatusName).collect(Collectors.toSet());
    }

    public static ProjectStateEntity find(String statusName, String subStatusName) {
        for (ProjectStateEntity entity : projectStates) {
            if (Objects.equals(statusName, entity.getStatusName()) && Objects.equals(subStatusName, entity.getSubStatusName())) {
                return entity;
            }
        }
        return null;
    }

    private static void addStatusToMap(StateModel stateModel, ProjectState projectState){
        List<ProjectState> statuses = allProjectStates.get(stateModel);
        if (statuses == null) statuses = new ArrayList<>();
        statuses.add(projectState);
        allProjectStates.put(stateModel, statuses);
    }


    public Set<ProjectState> getAvailableProjectStatesForUser(){
        Set<ProjectState> projectStatesForUser = new HashSet<>();
        User currentUser = userService.currentUser();
        for (Organisation organisation : currentUser.getOrganisations()) {
            Organisation managingOrg = organisation.isManagingOrganisation() ? organisation : organisation.getManagingOrganisation();
            List<Programme> programmes = programmeService.getProgrammesManagedBy(managingOrg);
            for (Programme programme : programmes){
                for (Template template :programme.getTemplates()){
                    projectStatesForUser.addAll(allProjectStates.get(template.getStateModel()));
                }
            }
        }

        return projectStatesForUser;
    }

}
