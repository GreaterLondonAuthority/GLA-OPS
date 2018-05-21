/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.importdata;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * Created by chris on 20/03/2017.
 */
@Entity(name="import_errors")
public class ImportErrorLog {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "import_error_log_seq_gen")
    @SequenceGenerator(name = "import_error_log_seq_gen", sequenceName = "import_error_log_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "import_type")
    @Enumerated(EnumType.STRING)
    public ImportJobType importJobType;

    @Column(name = "import_time")
    public OffsetDateTime executionDate;

    @Column(name = "imported_by")
    public String importedBy;

    @Column(name = "row_number")
    public Integer rowNumber;

    @Column(name = "error_summary")
    public String errorSummary;

    @Column(name = "raw_data")
    public String rawData;


    public ImportErrorLog() {
    }

    public ImportErrorLog(ImportJobType importJobType, OffsetDateTime executionDate, String importedBy, Integer rowNumber, String newErrorSummary, String newRawData) {
        this.importJobType = importJobType;
        this.executionDate = executionDate;
        this.importedBy = importedBy;
        this.rowNumber = rowNumber;
        if (newErrorSummary.length() > 500) {
            this.errorSummary = newErrorSummary.substring(0, 500);
        } else {
            this.errorSummary = newErrorSummary;
        }
        if (newRawData.length() > 2500) {
            this.rawData = newRawData.substring(0,2500);
        } else {
            this.rawData = newRawData;
        }
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

    public String getImportedBy() {
        return importedBy;
    }

    public void setImportedBy(String importedBy) {
        this.importedBy = importedBy;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getErrorSummary() {
        return errorSummary;
    }

    public void setErrorSummary(String errorSummary) {
        this.errorSummary = errorSummary;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }
}