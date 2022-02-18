/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.contracts;

import uk.gov.london.ops.framework.enums.ContractWorkflowType;

import java.util.Collection;
import java.util.List;

public interface ContractService {
    List<ContractModel> findAll();

    List<ContractTemplatesModel> findAllWithTemplates();

    ContractModel find(Integer id);

    ContractModel findByName(String name);

    ContractModel create(ContractModel contractModel);

    void update(ContractModel contractModel);

    void updateAll(Collection<ContractModel> contractModels);

    ContractModel findById(Integer id);

    void updateContractWorkflowType(Integer contractId, ContractWorkflowType contractWorkflowType);
}
