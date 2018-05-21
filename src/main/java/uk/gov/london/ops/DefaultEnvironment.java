/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Default implementation of Environment interface.
 *
 * Created by sleach on 19/08/2016.
 */
@Component
public class DefaultEnvironment implements Environment {

    @Value("${env.shortname}")
    String envShortName = null;

    @Value("${env.fullname}")
    String envFullName = null;

    @Value("${app.release}")
    String appRelease = null;

    @Value("${app.build}")
    String appBuild = null;

    @Value("${google.analytics.account}")
    String gaAccount;

    @Value("${testdata.def_pw}")
    String defPwHash;

    @Value("${testdata.init}")
    String initTestDataConfig;

    @Value("${urls.reportserver}")
    String reportServerUrl;

    @Value("${urls.about}")
    String aboutUrl;

    String profile = null;

    Clock clock = Clock.system(ZoneId.of("Z"));

    @Override
    public String shortName() {
        return envShortName;
    }

    @Override
    public String fullName() {
        return envFullName;
    }

    @Override
    public String releaseNumber() {
        return appRelease;
    }

    @Override
    public String buildNumber() {
        return appBuild;
    }

    @Override
    public String summary() {
        if (appRelease.startsWith("___")) {
            return envShortName;
        } else {
            return String.format("%s %s.%s", envShortName, appRelease, appBuild);
        }
    }

    @Override
    public String profileName() {
        return profile;
    }

    @Override
    public String hostName() {
        return System.getenv("HOSTNAME");
    }

    @Override
    public String aboutUrl() {
        return aboutUrl;
    }

    @Override
    public String reportServerUrl() {
        return reportServerUrl;
    }

    @Override
    public String gaAccount() {
        return gaAccount;
    }

    public Clock clock() {

        return clock;
    }

    @Override
    public boolean initTestData() {
        return (initTestDataConfig != null) && (initTestDataConfig.equalsIgnoreCase("true"));
    }

    @Override
    public String defPwHash() {
        return defPwHash;
    }

    @Override
    public OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }
}
