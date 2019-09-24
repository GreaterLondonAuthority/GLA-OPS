/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.ops.domain.metadata.MetaDataSummary;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.service.MetaDataService;
import uk.gov.london.ops.service.UserService;

@RestController
@RequestMapping("/api/v1")
@Api(description="meta data that is user specific and changes over time")
public class MetaDataAPI {

    @Autowired
    MetaDataService metaDataService;

    @Autowired
    UserService userService;

    @RequestMapping(value = "/metadata/", method = RequestMethod.GET)
    @ApiOperation(value = "get user meta data", notes = "currently only returns unread notification count")
    public @ResponseBody
    ResponseEntity<MetaDataSummary>  getMetaDataSummary() {

        User currentUser = userService.currentUser();
        if (currentUser == null) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity(metaDataService.getMetaDataSummary(currentUser.getUsername()), HttpStatus.OK);
    }
}
