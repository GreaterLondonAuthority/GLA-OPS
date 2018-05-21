/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.mapper.model;

public class MapResult<T> {

    private boolean ignored = false;
    private String error = null;
    private int dataIndex;
    private String rawData;
    private T dataObject = null;

    public MapResult(T dataObject) {
        this.dataObject = dataObject;
    }

    public MapResult(String error) {
        this(error, true);
    }

    public MapResult(String error, boolean ignored) {
        this(error, ignored, -1, null);
    }

    public MapResult(String error, boolean ignored, int dataIndex, String rawData) {
        this.error = error;
        this.ignored = ignored;
        this.dataIndex = dataIndex;
        this.rawData = rawData;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getDataIndex() {
        return dataIndex;
    }

    public String getRawData() {
        return rawData;
    }

    public T getDataObject() {
        return dataObject;
    }

}
