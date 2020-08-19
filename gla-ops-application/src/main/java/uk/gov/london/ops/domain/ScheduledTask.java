/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.OffsetDateTime;

/**
 * Represents a scheduled task executed in the background by the OPS system.
 *
 * @author Steve Leach
 */
@Entity(name = "scheduled_task")
public class ScheduledTask {

    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";
    public static final String SKIPPED = "SKIPPED";

    @Id()
    @Column(name = "task_key")
    private String key;

    @Column(name = "last_executed")
    private OffsetDateTime lastExecuted = null;

    @Column(name = "last_success")
    private OffsetDateTime lastSuccess = null;

    @Column(name = "status")
    private String status = null;

    @Column(name = "results")
    private String results = null;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public OffsetDateTime getLastExecuted() {
        return lastExecuted;
    }

    public void setLastExecuted(OffsetDateTime lastExecuted) {
        this.lastExecuted = lastExecuted;
    }

    public OffsetDateTime getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(OffsetDateTime lastSuccess) {
        this.lastSuccess = lastSuccess;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }
}
