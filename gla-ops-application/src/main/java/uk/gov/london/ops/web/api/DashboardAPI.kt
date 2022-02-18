/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.london.common.user.BaseRole.*;
import uk.gov.london.ops.service.DashboardService
import uk.gov.london.ops.service.GCData

@RestController
@RequestMapping("/api/v1")
@Api(description = "dashboard api")
class DashboardAPI @Autowired constructor(val service: DashboardService) {

    @RequestMapping(value = ["/dashboard/metrics"], method = [RequestMethod.GET])
    @ApiOperation(value = "returns a map of dashboard metrics")
    fun getMetrics(): Map<String, Int> = service.getMetricsForCurrentUser()

    @Secured(OPS_ADMIN, TECH_ADMIN)
    @RequestMapping(value = ["/dashboard/gc"], method = [RequestMethod.GET])
    fun getGCData(): GCData = service.getGCData()

// This API is insecure, and not needed because the data is now available via the KeyDataInfoContributor
// Steve Leach, 16 Aug 2018

//    @RequestMapping(value = "/dashboard/keydDataEntityCounts", method = RequestMethod.GET)
//    @ApiOperation(value = "returns the summary of key data entity counts in the form of a map")
//    public Map<String, String> getKeyDataEntityCounts() {
//        return service.getSummaryOfKeyDataEntityCounts();
//    }
}
