/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;

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

    @Value("${urls.sgw}")
    String sgwUrl;

    @Value("${env.isTestEnvironment}")
    String isTestEnvironment;

    @Value("${spring.datasource.driver-class-name}")
    String dbDriverClass;

    @Value("${urls.about}")
    String aboutUrl;

    @Value("${storage.option}")
    String storageOption;

    String profile = null;

    boolean printGetProjectStackTrace;

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
    public String storageOption() {
        return storageOption;
    }

    public void setStorageOption(String storageOption) {
        this.storageOption = storageOption;
    }

    @Override
    public String reportServerUrl() {
        return reportServerUrl;
    }

    @Override
    public String sgwUrl() {
        return sgwUrl;
    }

    @Override
    public String gaAccount() {
        return gaAccount;
    }

    public Clock clock() {
        return EnvironmentUtils.clock();
    }

    @Override
    public boolean initTestData() {
        return (initTestDataConfig != null) && (initTestDataConfig.equalsIgnoreCase("true"));
    }

    @Override
    public boolean isTestEnvironment() {
        return (isTestEnvironment != null) && (isTestEnvironment.equalsIgnoreCase("true"));
    }

    @Override
    public String defPwHash() {
        return defPwHash;
    }

    @Override
    public OffsetDateTime now() {
        return EnvironmentUtils.now();
    }

    @Override
    public boolean printGetProjectStackTrace() {
        return printGetProjectStackTrace;
    }

    @Override
    public void setPrintGetProjectStackTrace(boolean printGetProjectStackTrace) {
        this.printGetProjectStackTrace = printGetProjectStackTrace;
    }

    @Override
    public boolean isH2Database() {
        return dbDriverClass.toLowerCase().contains("h2");
    }
}
