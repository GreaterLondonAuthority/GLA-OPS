/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import ch.qos.logback.classic.Level;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.user.Role;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Api(description="config api")
public class ConfigAPI {

    @Autowired
    private Environment environment;

    @Value("${notifications.max.display}")
    private String notificationsMaxDisplay;

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    @ApiOperation(value="returns a map of config values")
    public Map<String, String> get() {
        Map<String, String> map = new HashMap<>();
        map.put("env-name", environment.shortName());
        map.put("system-environment", environment.summary());
        map.put("ga-account", environment.gaAccount());
        map.put("reportServer-url", environment.reportServerUrl());
        map.put("about-url", environment.aboutUrl());
        map.put("notifications-max-display", notificationsMaxDisplay);
        return map;
    }

    /**
     * Returns the log level of the specified logger path.
     */
    @RequestMapping(value="/config/logger/{name}", method = RequestMethod.GET)
    @Secured({Role.OPS_ADMIN})
    public String getLogLevel(@PathVariable String name) {
        Logger logger = LoggerFactory.getLogger(name);
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logback = (ch.qos.logback.classic.Logger) logger;
            return logback.getEffectiveLevel().toString();
        }
        if (logger.isDebugEnabled()) {
            return "DEBUG";
        }
        if (logger.isInfoEnabled()) {
            return "INFO";
        }
        if (logger.isWarnEnabled()) {
            return "WARN";
        }
        if (logger.isErrorEnabled()) {
            return "ERROR";
        }
        return "UNKNOWN";
    }

    /**
     * Sets the log level of the specified logger path.
     */
    @RequestMapping(value="/config/logger/{name}", method = RequestMethod.PUT)
    @Secured({Role.OPS_ADMIN})
    public void setLogLevel(@PathVariable String name, @RequestBody String value) {
        Logger logger = LoggerFactory.getLogger(name);
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logback = (ch.qos.logback.classic.Logger) logger;
            logback.setLevel(Level.toLevel(value));
        } else {
            throw new IllegalStateException("Logger " + logger.toString() + " not supported");
        }
    }
}
