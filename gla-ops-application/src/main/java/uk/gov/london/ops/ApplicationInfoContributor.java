/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops;

import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.environment.Environment;

@Component
public class ApplicationInfoContributor implements InfoContributor {

    @Autowired
    Environment environment;
    @Autowired JdbcTemplate jdbc;

    @Value("${cloud.console.logon.url}")
    String cloudConsoleLogonUrl = null;

    public void contribute(Info.Builder builder) {
        Map<String, Object> data = new TreeMap<>();

        data.put("release", environment.releaseNumber());
        data.put("build", environment.buildNumber());
        data.put("environment", environment.fullName());
        data.put("cloud-console-logon-url", cloudConsoleLogonUrl);
        data.put("app-start-time", getAppStartupTime());
        data.put("server-time", environment.now());

        builder.withDetail("opsApplication", data);
    }

    private Timestamp getAppStartupTime() {
        return jdbc.queryForObject("SELECT start_time FROM env_info WHERE id = 1", Timestamp.class);
    }

}
