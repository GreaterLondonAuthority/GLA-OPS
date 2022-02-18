/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.contracts;

import uk.gov.london.ops.framework.enums.ContractWorkflowType;

import java.util.List;

public class ContractTemplatesModel extends ContractModel {

    private List<String> templateNames;

    public ContractTemplatesModel() {
    }

    public ContractTemplatesModel(Integer id, String name, ContractWorkflowType contractWorkflowType, List<String> templateNames) {
        super(id, name, contractWorkflowType);
        this.templateNames = templateNames;
    }

    public List<String> getTemplateNames() {
        return templateNames;
    }

    public String getWorkflowName() {
        String returnString = "";
        switch (this.getContractWorkflowType()) {
            case SIGNED_TO_AUTHORISE_PAYMENTS:
                returnString = "Sign to Authorise Payments";
                break;
            case CONTRACT_OFFER_AND_ACCEPTANCE:
                returnString = "Contract Offer And Acceptance";
                break;
        }
        return returnString;
    }
}
