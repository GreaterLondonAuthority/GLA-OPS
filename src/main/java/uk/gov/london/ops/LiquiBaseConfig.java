/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by dieppa on 05/04/17.
 */
@Configuration
@Component
public class LiquiBaseConfig implements InfoContributor {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private final  boolean failsOnError;
    private final String changeLogFile;
    private final DataSource dataSource;

    public LiquiBaseConfig(final DataSource dataSource,
                           final @Value("${liquibase.change-log}") String changeLogFile,
                           final @Value("${liquibase.view.creation.failsOnError}") boolean failsOnError) {
        this.dataSource = dataSource;
        this.changeLogFile = changeLogFile;
        this.failsOnError = failsOnError;
    }


    @Bean
    public SpringLiquibase liquibase() {
        final SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        final Map<String, String> liquiBaseParams = new HashMap<>();
        liquiBaseParams.put("failsOnError", String.valueOf(failsOnError));
        liquibase.setChangeLog(changeLogFile);
        liquibase.setChangeLogParameters(liquiBaseParams);
        return liquibase;
    }

    public void contribute(Info.Builder builder) {

        Map<String, Object> summary = summariseLiquibase();
        builder.withDetail("changelogDetails", summary);

    }

    public Map<String, Object> summariseLiquibase() {

        List<Map<String, Object>> data = getChangeLogInfo();
        int numberNotExecuted = 0;
        Timestamp lastExecutedTime = null;
        Object lastExecutedFileName = null;

        for(Map<String, Object> entry: data) {
            Object o = entry.get("DATEEXECUTED");
            if(o instanceof Timestamp) {
                Timestamp t = (Timestamp)o;
                if(lastExecutedTime==null || t.after(lastExecutedTime)) {
                    lastExecutedTime = t;
                    lastExecutedFileName = entry.get("FILENAME");
                }
            }
            Object ex = entry.get("EXECTYPE");
            if(ex!=null) {
                if(!ex.equals("EXECUTED")) {
                    numberNotExecuted += 1;
                }
            }
        }

        Map<String, Object> summary = new TreeMap<>();
        summary.put("numberEntries", data.size());
        summary.put("lastExecutedFileName", lastExecutedFileName);
        summary.put("lastExecutedFileTime", lastExecutedTime);
        summary.put("numberNotExecuted", numberNotExecuted);
        return summary;

    }

    public List<Map<String, Object>> getChangeLogInfo() {

        List<Map<String, Object>> data = jdbcTemplate.queryForList("select * from DATABASECHANGELOG");
        return data;

    }
}