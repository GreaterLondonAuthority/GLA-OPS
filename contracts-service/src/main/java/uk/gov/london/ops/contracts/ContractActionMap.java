/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.contracts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.framework.enums.ContractWorkflowType;
import uk.gov.london.ops.framework.enums.OrganisationContractStatus;
import uk.gov.london.ops.permission.PermissionService;
import uk.gov.london.ops.project.state.ProjectStatus;
import uk.gov.london.ops.user.User;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Component
public class ContractActionMap {

    List<ContractActionRule> rules;

    static final String ROLE_DELIMITER = " \\| ";
    static final String ANY_MATCH = "ANY";

    /**
     * Maps rows from the CSV file into StateTransition objects.
     */
    final CSVFile.CSVMapper<ContractActionRule> csvMapper = csvRow -> new ContractActionRule(
        ContractAction.valueOf(csvRow.getString("CONTRACT_ACTION")),
        ContractWorkflowType.valueOf(csvRow.getString("CONTRACT_WORKFLOW_TYPE")),
        csvRow.getString("VARIATION").equals("Yes"),
        OrganisationContractStatus.valueOf(csvRow.getString("CONTRACT_STATUS")),
        csvRow.getString("PROJECT_STATUS"),
        Arrays.asList(csvRow.getString("USER_ROLES").split(ROLE_DELIMITER)),
        csvRow.getString("PERMISSIONS")
    );

    @PostConstruct
    void loadRules() throws IOException {
        rules = CSVFile.fromResource(this, "contract-actions-mapping.csv").loadData(csvMapper);
    }

    @Autowired
    PermissionService permissionService;

    public List<ContractActionDetails> getAllowedActions(ContractWorkflowType workflowType,
                                                  boolean variation,
                                                  OrganisationContractStatus contractStatus,
                                                  ProjectStatus projectStatus, User user,
                                                         Integer orgId, Integer managingOrgId) {
        List<ContractActionDetails> allowedActions = new ArrayList<>();
        for (ContractActionRule rule : rules) {
            if (matches(rule, workflowType, variation, contractStatus, projectStatus, user, orgId, managingOrgId)) {
                ContractAction contractAction = rule.getContractAction();
                ContractActionDetails actionDetails = new ContractActionDetails(contractAction.text, contractAction.nextStatus,
                        contractAction.doViewDetails, contractAction.newVariationEntry);
                if (!allowedActions.contains(actionDetails)) {
                    allowedActions.add(actionDetails);
                }
            }
        }
        return allowedActions;
    }

    Boolean matches(ContractActionRule rule, ContractWorkflowType workflowType, boolean variation,
                    OrganisationContractStatus contractStatus,
                    ProjectStatus projectStatus, User user, Integer orgId, Integer managingOrgId) {

        return rule.getContractWorkflowType().equals(workflowType)
            && rule.isVariation() == variation
            && rule.getContractStatus().equals(contractStatus)
            && matchesProjectStatus(rule, projectStatus)
            && matchesRoleAndPermissions(rule, user, orgId, managingOrgId);
    }

    Boolean matchesProjectStatus(ContractActionRule rule, ProjectStatus status) {
        return rule.getProjectStatus().equals(status.name()) || rule.getProjectStatus().equals(ANY_MATCH);
    }

    Boolean matchesRoleAndPermissions(ContractActionRule rule, User user, Integer orgId, Integer managingOrgId) {
        Set<String> approvedRolesForOrg = new HashSet<>();
        approvedRolesForOrg.addAll(user.getApprovedRolesForOrg(orgId));
        approvedRolesForOrg.addAll(user.getApprovedRolesForOrg(managingOrgId));
        if (rule.getUserRoles().contains(ANY_MATCH)) {
            return true;
        }
        for (String role : approvedRolesForOrg) {
            if (rule.getUserRoles().contains(role)) {
                return true;
            }
        }
        return false;
    }
}
