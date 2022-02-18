/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * Spring MVC controller for performing actions immediately after user login.
 *
 * Created by Carmina Matias on 12/06/2020.
 */
@RestController
@RequestMapping("/api/v1/postLogin")
@Api("managing any post login actions")
class PostLoginAPI @Autowired constructor(val postLoginService: PostLoginService) {

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = ["/checkOrgType"], method = [RequestMethod.GET], produces = ["application/json"])
    @ApiOperation(value = "notify if user organisations have org type populated")
    fun checkForDeprecatedOrgType(): String? {
        return postLoginService.notifyDeprecatedOrgType()
    }
}
