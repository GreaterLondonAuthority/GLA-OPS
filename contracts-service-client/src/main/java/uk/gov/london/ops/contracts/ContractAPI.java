/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.contracts;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.framework.enums.ContractWorkflowType;

import java.util.List;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.ops.permission.PermissionType.CONTRACT_TYPES;

@RestController
@RequestMapping("/api/v1")
@Api("contract api")
public class ContractAPI {

    @Autowired
    private ContractService contractService;

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/contracts", method = RequestMethod.GET)
    public List<ContractModel> getAll() {
        return contractService.findAll();
    }

    @PermissionRequired(CONTRACT_TYPES)
    @RequestMapping(value = "/contractsWithTemplates", method = RequestMethod.GET)
    public List<ContractTemplatesModel> getAllWithTemplates() {
        return contractService.findAllWithTemplates();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/contracts/{id}", method = RequestMethod.GET)
    public ContractModel get(@PathVariable Integer id) {
        return contractService.find(id);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/contracts", method = RequestMethod.POST)
    public ContractModel create(@RequestBody ContractModel contractModel) {
        return contractService.create(contractModel);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/contracts/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable Integer id, @RequestBody ContractModel contractModel) {
        contractService.update(contractModel);
    }

    @Secured(OPS_ADMIN)
    @PutMapping(value = "/contracts/{id}/contractWorkflowType")
    @ApiOperation(value = "Update contract workflow type")
    public void updateContractWorkflow(@PathVariable Integer id,
                                       @RequestBody ContractWorkflowType contractWorkflowType) {
        contractService.updateContractWorkflowType(id, contractWorkflowType);
    }
}
