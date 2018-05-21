/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model.project;

import uk.gov.london.ops.domain.importdata.ImportErrorLog;

import java.util.List;

/**
 * Created by chris on 24/03/2017.
 */
public class FileImportResult {

    private int recordsProcessed;

    private List<ImportErrorLog> errors;

    public FileImportResult() {
    }

    public FileImportResult(int recordsProcessed, List<ImportErrorLog> errors) {
        this.recordsProcessed = recordsProcessed;
        this.errors = errors;
    }

    public int getRecordsProcessed() {
        return recordsProcessed;
    }

    public int getErrorCount() {
        return errors == null ? 0 : errors.size();
    }

    public List<ImportErrorLog> getErrors() {
        return errors;
    }

    public void setErrors(List<ImportErrorLog> errors) {
        this.errors = errors;
    }

    public void setRecordsProcessed(int recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }
}
