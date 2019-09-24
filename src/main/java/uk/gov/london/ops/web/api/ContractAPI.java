/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.template.Contract;
import uk.gov.london.ops.service.ContractService;

import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;

@RestController
@RequestMapping("/api/v1")
@Api(description="contract api")
public class ContractAPI {

    @Autowired
    private ContractService contractService;

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/contracts", method = RequestMethod.GET)
    public List<Contract> getAll() {
        return contractService.findAll();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/contracts/{id}", method = RequestMethod.GET)
    public Contract get(@PathVariable Integer id) {
        return contractService.find(id);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/contracts", method = RequestMethod.POST)
    public Contract create(@RequestBody Contract contract) {
        return contractService.create(contract);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/contracts/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable Integer id, @RequestBody Contract contract) {
        contractService.update(contract);
    }

}
