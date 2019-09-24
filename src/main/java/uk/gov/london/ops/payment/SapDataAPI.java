/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.payment.implementation.repository.SapDataRepository;
import uk.gov.london.ops.framework.exception.NotFoundException;

import java.io.IOException;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.TECH_ADMIN;

@RestController
@RequestMapping("/api/v1")
public class SapDataAPI {

    @Autowired
    SapDataService sapDataService;

    @Autowired
    SapDataRepository sapDataRepository;

    @Autowired
    Environment environment;

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/finance/processData", method = RequestMethod.GET)
    @ApiOperation(value = "POC for finance integration.", hidden = true)
    public String processSapData() throws IOException {
        sapDataService.processSapData();
        return "Done";
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/finance/sapData", method = RequestMethod.POST)
    @ApiOperation(value = "Internal tool to create SAPData table entry.", hidden = true)
    public SapData createSapData(@RequestParam String type, @RequestBody String data) throws IOException {
        SapData sapData = new SapData();
        sapData.setInterfaceType(type);
        sapData.setContent(data);
        sapData.setFileName("test_endpoint");
        sapData.setCreatedOn(environment.now());
        SapData created = sapDataRepository.save(sapData);

        sapDataService.processSapData();

        return created;
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/finance/sapData/{id}/processed", method = RequestMethod.PUT)
    public void updateProcessed(@PathVariable Integer id, @RequestBody String processed) {
        SapData sapData = sapDataRepository.getOne(id);
        sapData.setProcessed(Boolean.parseBoolean(processed));
        sapDataRepository.save(sapData);
    }


    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/finance/sapData/{id}/ignored", method = RequestMethod.PUT)
    public SapData ignoreSapData(@PathVariable Integer id, @RequestBody String ignore) {
        return sapDataService.markSapDataIgnored(id, Boolean.parseBoolean(ignore));
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/finance/sapData", method = RequestMethod.GET)
    public List<SapData> getSapData(@RequestParam(required = false) Boolean processed) {
            return sapDataService.getSapData(processed);
    }


    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/finance/sapData/{id}", method = RequestMethod.GET)
    public SapData getSapDataEntry(@PathVariable Integer id) {
        SapData data = sapDataRepository.findById(id).orElse(null);
        if (data == null) {
            throw new NotFoundException("No sap_data found with ID " + id);
        }
        return data;
    }

}
