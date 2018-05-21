/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.importdata;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.time.OffsetDateTime;

/**
 * Created by chris on 20/03/2017.
 */
@Entity
public class ImportProcessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "import_process_log_seq_gen")
    @SequenceGenerator(name = "import_process_log_seq_gen", sequenceName = "import_process_log_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public ImportJobType importJobType;

    @Column(name = "execution_date")
    public OffsetDateTime executionDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ImportJobStatus importJobStatus;

    @Column(name = "import_count")
    public Integer importCount;

    @Column(name = "error_count")
    public Integer errorCount;

    public ImportProcessLog() {
    }

    public ImportProcessLog(ImportJobType importJobType, OffsetDateTime executionDate) {
        this.importJobType = importJobType;
        this.executionDate = executionDate;
    }

    public ImportProcessLog(ImportJobType importJobType, OffsetDateTime executionDate, ImportJobStatus importJobStatus) {
        this(importJobType, executionDate);
        this.importJobStatus = importJobStatus;
    }

    public ImportJobType getImportJobType() {
        return importJobType;
    }

    public void setImportJobType(ImportJobType importJobType) {
        this.importJobType = importJobType;
    }

    public OffsetDateTime getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(OffsetDateTime executionDate) {
        this.executionDate = executionDate;
    }

    public ImportJobStatus getImportJobStatus() {
        return importJobStatus;
    }

    public void setImportJobStatus(ImportJobStatus importJobStatus) {
        this.importJobStatus = importJobStatus;
    }

    public Integer getImportCount() {
        return importCount;
    }

    public void setImportCount(Integer importCount) {
        this.importCount = importCount;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

}
