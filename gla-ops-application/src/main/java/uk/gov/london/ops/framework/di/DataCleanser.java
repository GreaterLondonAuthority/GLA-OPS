/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.organisation.OrganisationBuilder;
import uk.gov.london.ops.organisation.OrganisationService;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

/**
    Data cleanser truncates tables and resets the org sequence
 */
@Transactional
@Component
public class DataCleanser  {

    @Autowired protected JdbcTemplate jdbcTemplate;
    @Autowired protected OrganisationService organisationService;
    @Autowired protected OrganisationBuilder organisationBuilder;
    @Autowired protected EntityManager entityManager;
    @Autowired protected Environment environment;
    Logger log = LoggerFactory.getLogger(getClass());


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void clearDatabase() {
        if (environment.isTestEnvironment()) {

            if (environment.isH2Database()) {
                toggleReferentialIntegrity(false);
                removeOldData();
                resetSequence();
                toggleReferentialIntegrity(true);
            } else {
                removeOldData();
                resetSequence();
            }
            entityManager.clear();
        }

    }

    private void resetSequence() {
        String query = "ALTER SEQUENCE organisation_seq RESTART WITH 10001;";
        jdbcTemplate.update(query);
    }

    private void toggleReferentialIntegrity(boolean enabled) {
        String query = "SET REFERENTIAL_INTEGRITY " + enabled;
        jdbcTemplate.update(query);
    }

    private void removeOldData() {
        String query = "SELECT table_name  FROM information_schema.tables " +
                "where upper(table_schema) = 'PUBLIC' and (table_type = 'BASE TABLE'  or table_type = 'TABLE') " +
                "and upper(table_name) != 'DATABASECHANGELOG' " +
                "and upper(table_name) != 'ENV_INFO' ORDER BY table_name";

        List<String> columns = jdbcTemplate.queryForList(query, String.class);

        columns.forEach(this::truncateTable);
    }
    private void truncateTable(String table) {
        log.debug("Truncating " + table);
        String query = environment.isH2Database() ?
                "TRUNCATE table " + table :
                "TRUNCATE " + table + " cascade";
        jdbcTemplate.update(query);
    }



}
