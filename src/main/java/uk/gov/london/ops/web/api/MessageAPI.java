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
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.Message;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.service.MessageService;

@RestController
@RequestMapping("/api/v1")
@Api(description="messages to be displayed to the user")
public class MessageAPI {

    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "/messages/{key}", method = RequestMethod.GET)
    @ApiOperation(
            value="get the message that corresponds to a specific message key",
            notes="Valid keys are: coming-soon, system-environment"
    )
    public Message get(@PathVariable String key) {
        if ("npe".equals(key)) {
            throw new NullPointerException();
        }
        return messageService.find(key);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/messages/{key}", method = RequestMethod.PUT)
    @ApiOperation(
            value="update the message to be displayed for the specified key"
    )
    public void update(@PathVariable String key, @RequestBody Message message) {
        messageService.update(message);
    }

}
