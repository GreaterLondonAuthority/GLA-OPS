/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.TableJoinJpaAnnotationProcessor;

import java.util.List;

/**
 * Scans annotations on JPA Entity classes to determine join columns.
 *
 * @author Steve Leach
 */
@Component
@Transactional
public class TableJoinJpaAnnotationModule implements DataInitialiserModule {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    JdbcTemplate jdbc;

    public boolean runInAllEnvironments() {
        return true;
    }

    public String getName() {
        return "TableJoinJpaAnnotationModule";
    }

    public void beforeInitialisation() {}

    public void addReferenceData() {
        List<Join> joins = new TableJoinJpaAnnotationProcessor().getJpaEntityJoins();
        writeJoinListToDatabase(joins);
    }

    private void writeJoinListToDatabase(List<Join> joins) {
        for (Join join : joins) {
            jdbc.update("INSERT INTO table_relationships (from_table,from_column,to_table,to_column,join_type,comments) "
                            + "VALUES (?,?,?,?,?,?)",
                    join.getFromTable(), join.getFromColumn(),
                    join.getToTable(), join.getToColumn(),
                    join.getJoinType().name(), join.getComments());
        }
    }

    public void afterInitialisation() {
        int rowCount = jdbc.queryForObject("SELECT count(*) FROM table_relationships", Integer.class);
        log.info("{} table joins recorded", rowCount);
    }
}
