package uk.gov.london.ops; /**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 
 * Gathers information on audits.
 *
 * Created by rbettison on 13/03/2018
 * 
 */

@Configuration
@Component
public class AuditInfoContributor implements InfoContributor {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void contribute(Info.Builder builder) {

        builder.withDetail("auditSummary", getAuditSummary());

    }

    public Map<String, Object> getAuditSummary() {

        Map<String, Object> auditInfo = new HashMap<>();
        List<Map<String, Object>> auditActivity = jdbcTemplate
                .queryForList("select * from audit_activity order by activity_time desc");
        int numberAuditActivities = auditActivity.size();
        Timestamp mostRecentEventTime = (Timestamp) auditActivity.get(0).get("ACTIVITY_TIME");
        auditInfo.put("numberAuditActivities", numberAuditActivities);
        auditInfo.put("mostRecentEventTime", mostRecentEventTime);
        return auditInfo;

    }

}
