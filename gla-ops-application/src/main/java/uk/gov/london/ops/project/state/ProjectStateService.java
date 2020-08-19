/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.state;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import liquibase.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.project.template.TemplateService;
import uk.gov.london.ops.user.UserService;
import uk.gov.london.ops.user.domain.User;

@Service
public class ProjectStateService {

    static Logger log = LoggerFactory.getLogger(ProjectStateService.class);

    public static Set<ProjectStateEntity> projectStates;
    public static HashMap<StateModel, Set<ProjectState>> allProjectStates;

    @Autowired
    private UserService userService;

    @Autowired
    private TemplateService templateService;

    static {
        try {
            allProjectStates = new HashMap<>();
            projectStates = new HashSet<>(
                    new CSVFile(ProjectStateEntity.class.getResourceAsStream("ProjectStates.csv")).loadData(csvRow -> {
                        StateModel stateModel = StateModel.valueOf(csvRow.getString("STATE_MODEL"));
                        String statusName = csvRow.getString("STATUS_NAME");
                        String subStatusName =
                                StringUtils.isNotEmpty(csvRow.getString("SUB_STATUS_NAME")) ? csvRow.getString("SUB_STATUS_NAME")
                                        : null;

                        addStatusToMap(stateModel, new ProjectState(statusName, subStatusName));

                        return new ProjectStateEntity(
                                csvRow.getInteger("ID"),
                                stateModel,
                                statusName,
                                subStatusName,
                                StringUtils.isNotEmpty(csvRow.getString("STATUS_TYPE")) ? ProjectStatus
                                        .valueOf(csvRow.getString("STATUS_TYPE")) : null,
                                StringUtils.isNotEmpty(csvRow.getString("SUB_STATUS_TYPE")) ? ProjectSubStatus
                                        .valueOf(csvRow.getString("SUB_STATUS_TYPE")) : null);

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

    private static void addStatusToMap(StateModel stateModel, ProjectState projectState) {
        Set<ProjectState> statuses = allProjectStates.get(stateModel);
        if (statuses == null) {
            statuses = new HashSet<>();
        }
        statuses.add(projectState);
        allProjectStates.put(stateModel, statuses);
    }


    public Set<ProjectState> getAvailableProjectStatesForUser() {
        User currentUser = userService.currentUser();

        Set<StateModel> availableStateModels = templateService
                .getAvailableStateModelsForManagingOrgIds(currentUser.getManagingOrganisationsIds());

        Set<ProjectState> projectStatesForUser = new HashSet<>();
        for (StateModel stateModel : availableStateModels) {
            projectStatesForUser.addAll(allProjectStates.get(stateModel));
        }
        return projectStatesForUser;
    }

}
