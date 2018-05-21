/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model;

import java.util.Map;
import java.util.TreeMap;

/**
 * Details of the result of a bulk upload.
 *
 * Created by sleach on 25/08/2016.
 */
public class BulkUploadSummary {
    private boolean allLoaded = false;
    private int sourceRows = 0;
    private int entitiesLoaded = 0;
    private Map<Long,String> errors = new TreeMap<>();

    /**
     * True if all rows were loaded OK.
     */
    public boolean isAllLoaded() {
        return allLoaded;
    }

    public void setAllLoaded(boolean allLoaded) {
        this.allLoaded = allLoaded;
    }

    /**
     * The number of rows that were sent.
     */
    public int getSourceRows() {
        return sourceRows;
    }

    public void setSourceRows(int sourceRows) {
        this.sourceRows = sourceRows;
    }

    /**
     * The number of rows that were loaded.
     */
    public int getEntitiesLoaded() {
        return entitiesLoaded;
    }

    public void setEntitiesLoaded(int entitiesLoaded) {
        this.entitiesLoaded = entitiesLoaded;
    }

    /**
     * Details of the rows that could not be loaded.
     */
    public Map<Long, String> getErrors() {
        return errors;
    }

    public void addError(long rowNum, String error) {
        errors.put(rowNum,error);
    }
}
