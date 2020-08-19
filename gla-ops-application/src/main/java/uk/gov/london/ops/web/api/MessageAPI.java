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
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.london.ops.domain.Message;
import uk.gov.london.ops.service.MessageService;

import java.util.LinkedList;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.TECH_ADMIN;

@RestController
@RequestMapping("/api/v1")
@Api(description="messages to be displayed to the user")
public class MessageAPI {

    @Autowired
    private MessageService messageService;

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    @ApiOperation(value = "get all messages", notes = "get all messages")
    public List<Message> getAll() {
        return messageService.getAll();
    }

    @RequestMapping(value = "/messages/{key}", method = RequestMethod.GET)
    @ApiOperation(
            value = "get the message that corresponds to a specific message key",
            notes = "Valid keys are: coming-soon, system-environment"
    )
    public String get(@PathVariable String key) {
        if ("npe".equals(key)) {
            throw new NullPointerException();
        }
        Message message = messageService.find(key);
        if (message == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        return message.isEnabled() ? message.getText() : "";
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/messages/{key}", method = RequestMethod.PUT)
    @ApiOperation(
            value = "update the message to be displayed for the specified key"
    )
    public void update(@PathVariable String key, @RequestBody Message message) {
        messageService.update(message);
    }

    /**
     * Dummy API for testing "broken pipe" errors (client disconnections).
     *
     * We need an API that takes several seconds to respond to give enough time
     * for a person to kill the client.
     *
     * It also needs to return enough data to fill the 8 KB response buffer and
     * start writing data to the client.
     *
     * We therefore need a delay during sending the response, but it can't be
     * done in our main code as that is completed before we start sending the
     * response to the client. We therefore put a Thread.sleep in the getter
     * of the entities we are returning, so that the sleep happens while
     * converting the entity to JSON text.
     *
     * We also make sure that each entity has 400 characters of text, so
     * that it only takes 20 or so of them to fill the buffer.
     *
     * The buffer size in Tomcat is configured with socket.appWriteBufSize
     */
    @RequestMapping(value = "/pipebreaker")
    public List<PipeCleaner> pipeBreaker(
            @RequestParam(name = "iterations", defaultValue = "100") Integer iterations,
            @RequestParam(name = "wait", defaultValue = "100") Integer waitMillis) {
        List<PipeCleaner> things = new LinkedList<>();
        for (int n = 0; n < iterations; n++) {
            things.add(new PipeCleaner(n+1, waitMillis));
        }
        return things;
    }

    public static class PipeCleaner {
        private final int index;
        private final int waitMillis;

        public PipeCleaner(int index, int waitMillis) {
            this.index = index;
            this.waitMillis = waitMillis;
        }

        public int getIndex() {
            return index;
        }

        public String getText() {
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            StringBuilder sb = new StringBuilder();
            for (int n = 1; n < 10; n++) {
                sb.append("########################################");
            }
            return sb.toString();
        }
    }

}
