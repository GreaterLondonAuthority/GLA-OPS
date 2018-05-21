/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api.finance;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.finance.SapData;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.NotFoundException;
import uk.gov.london.ops.repository.SapDataRepository;
import uk.gov.london.ops.service.finance.SapDataService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SapDataAPI {

    @Autowired
    SapDataService sapDataService;

    @Autowired
    SapDataRepository sapDataRepository;

    @Autowired
    Environment environment;

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/finance/processData", method = RequestMethod.GET)
    @ApiOperation(value = "POC for finance integration.", hidden = true)
    public String processSapData() throws IOException {
        sapDataService.processSapData();
        return "Done";
    }

    @Secured(Role.OPS_ADMIN)
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

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/finance/sapData/{id}/processed", method = RequestMethod.PUT)
    public void updateProcessed(@PathVariable Integer id, @RequestBody String processed) {
        SapData sapData = sapDataRepository.findOne(id);
        sapData.setProcessed(Boolean.parseBoolean(processed));
        sapDataRepository.save(sapData);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/finance/sapData", method = RequestMethod.GET)
    public List<SapData> getSapData() {
        return sapDataRepository.findAll();
    }


    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/finance/sapData/{id}", method = RequestMethod.GET)
    public SapData getSapDataEntry(@PathVariable Integer id) {
        SapData data = sapDataRepository.findOne(id);
        if (data == null) {
            throw new NotFoundException("No sap_data found with ID " + id);
        }
        return data;
    }

}
