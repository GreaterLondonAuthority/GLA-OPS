/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.framework.portableentity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import static uk.gov.london.common.user.BaseRole.*;

@RestController
@RequestMapping("/api/v1")
public class PortableEntityAPI {

    @Autowired
    PortableEntityService portableEntityService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY})
    @RequestMapping(value = "/portable/entity/{className}/{id}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody String getSanitisedEntity(@PathVariable Integer id, @PathVariable String className) throws Exception {
            return portableEntityService.getSanitisedEntity(className, id);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY})
    @RequestMapping(value = "/portable/entity/{className}", method = RequestMethod.POST)
    public void saveSanitisedEntity(@PathVariable String className, @RequestBody String json) {
        portableEntityService.saveSanitisedEntity(className, json);
    }
}
