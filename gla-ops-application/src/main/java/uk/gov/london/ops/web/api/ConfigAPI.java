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
import uk.gov.london.ops.file.StorageOption;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.ops.permission.PermissionType.SWITCH_STORAGE;
import static uk.gov.london.ops.permission.PermissionType.SYS_FEATURES_EDIT;

@RestController
@RequestMapping("/api/v1")
@Api
public class ConfigAPI {

    @Autowired
    private Environment environment;

    @Value("${notifications.max.display}")
    private String notificationsMaxDisplay;

    @Value("${annual.submissions.first.year}")
    private Integer annualSubmissionsFirstYear;

    @Value("${annual.submissions.nb.future.years}")
    private Integer annualSubmissionsNbFutureYears;

    @Value("${annual.submissions.dpf.last.year}")
    private Integer annualSubmissionsDpfLastYear;

    @Value("${annual.submissions.agreement.url}")
    private String annualSubmissionsAgreementUrl;

    @Value("${http.cache.max.age}")
    private Integer httpCacheMaxAge;

    @Value("${max.sap.ids.per.org}")
    private Integer maxSapIdsPerOrg;

    @Autowired
    FeatureStatus featureStatus;


    @RequestMapping(value = "/config", method = RequestMethod.GET)
    @ApiOperation(value = "returns a map of config values")
    public Map<String, Object> get() {
        Map<String, Object> map = new HashMap<>();
        map.put("env-name", environment.shortName());
        map.put("system-environment", environment.summary());
        map.put("ga-account", environment.gaAccount());
        map.put("reportServer-url", environment.reportServerUrl());
        map.put("sgw-url", environment.sgwUrl());
        map.put("about-url", environment.aboutUrl());
        if (featureStatus.isEnabled(Feature.ShowAccessibilityUrl)) {
            map.put("accessibility-url", environment.accessibilityUrl());
        }
        map.put("privacy-policy-url", environment.privacyUrl());
        map.put("is-test-env", environment.isTestEnvironment());
        map.put("notifications-max-display", notificationsMaxDisplay);
        map.put("annual-submissions-first-year", annualSubmissionsFirstYear);
        map.put("annual-submissions-nb-future-years", annualSubmissionsNbFutureYears);
        map.put("annual-submissions-dpf-last-year", annualSubmissionsDpfLastYear);
        map.put("annual-submissions-agreement-url", annualSubmissionsAgreementUrl);
        map.put("http-cache-max-age", httpCacheMaxAge);
        map.put("max-sap-ids-per-org", maxSapIdsPerOrg);
        return map;
    }

    /**
     * Returns the log level of the specified logger path.
     */
    @RequestMapping(value = "/config/logger/{name}", method = RequestMethod.GET)
    @Secured({OPS_ADMIN})
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
    @RequestMapping(value = "/config/logger/{name}", method = RequestMethod.PUT)
    @Secured({OPS_ADMIN})
    public void setLogLevel(@PathVariable String name, @RequestBody String value) {
        Logger logger = LoggerFactory.getLogger(name);
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logback = (ch.qos.logback.classic.Logger) logger;
            logback.setLevel(Level.toLevel(value));
        } else {
            throw new IllegalStateException("Logger " + logger.toString() + " not supported");
        }
    }

    /**
     * Switch between 3 storage options: Database, S3 and OwnCloud.
     * The default option is database.
     */
    @PermissionRequired(SWITCH_STORAGE)
    @ApiOperation(value = "Switch between 3 storage options: Database, S3 and OwnCloud.", notes = "The default option is Database.")
    @RequestMapping(value = "/config/storage/{option}", method = RequestMethod.PUT)
    public String switchStorageOption(@PathVariable StorageOption option) {
        if ((option.equals(StorageOption.S3) || option.equals(StorageOption.OwnCloud))
            && !featureStatus.isEnabled(Feature.AllowExternalFileStorage)) {
            throw new ValidationException("AllowExternalFileStorage must be enabled to use S3 and OwnCloud storage. "
                    + "Try enabling and switch again.");
        } else {
            environment.setStorageOption(String.valueOf(option));
        }
        return "The storage option is " + environment.storageOption();
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/config/printGetProjectStackTrace", method = RequestMethod.PUT)
    public void printGetProjectStackTrace(@RequestParam boolean value) {
        environment.setPrintGetProjectStackTrace(value);
    }

}
