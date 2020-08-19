/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.london.common.GlaUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Verifies that supported reports SQL has not been broken by database schema changes.
 */
@Component
public class SupportedReportsVerifier extends SimpleFileVisitor<Path> {

    Logger log = LoggerFactory.getLogger(getClass());

    Path reportRoot;

    public int errorCount = 0;
    public int successCount = 0;

    @Autowired
    JdbcTemplate jdbc;

    @Value("${spring.datasource.url}")
    String springDatasourceUrl;

    @PostConstruct
    public void postContruct() throws URISyntaxException {
        reportRoot = Paths.get(this.getClass().getResource("Supported Reports").toURI());
    }

    public void setSpringDatasourceUrl(String springDatasourceUrl) {
        this.springDatasourceUrl = springDatasourceUrl;
    }

    /**
     * Finds all SQL files in the supported reports folder and runs the SQL to make sure it works.
     */
    public void verifySupportedReports() throws IOException {
        if (!springDatasourceUrl.toLowerCase().contains("h2")) {
            // Walk the supported reports directory tree and test any SQL files found
            Files.walkFileTree(reportRoot, this);
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.endsWith("OPS database structure and views")) {
            // Don't bother testing this as it is querying the PostgeSQL metadata, not the OPS data
            log.info("Skipping " + reportRoot.relativize(dir));
            return FileVisitResult.SKIP_SUBTREE;
        }
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (isSqlFile(file)) {
            testSqlFile(file);
        }

        return FileVisitResult.CONTINUE;
    }

    private void testSqlFile(Path file) {
        String sql = GlaUtils.getFileContent(file.toFile());

        try {
            jdbc.execute(sql);

            log.info("Successfully executed query {}", reportRoot.relativize(file));
            successCount = successCount + 1;
        } catch (DataAccessException e) {
            handleError(file, sql, e);
        }
    }

    private void handleError(Path file, String sql, DataAccessException e) {
        String summary = sqlErrorSummary(e);
        log.error("Error running " + reportRoot.relativize(file) + " : " + summary);
        errorCount = errorCount + 1;
    }

    /**
     * Gets a summary of the cause of a DataAccessException.
     */
    private String sqlErrorSummary(DataAccessException exception) {
        // The actual error is described in the cause, not in the DataAccessException itself.
        String message = exception.getCause().getMessage();

        // The SQL statement may be included in the message, but we just want the error description.
        int index = message.indexOf("; SQL statement:");
        if (index > 0) {
            message = message.substring(0,index);
        }

        return message;
    }

    private boolean isSqlFile(Path file) {
        return file.toFile().getName().endsWith(".sql");
    }

}
