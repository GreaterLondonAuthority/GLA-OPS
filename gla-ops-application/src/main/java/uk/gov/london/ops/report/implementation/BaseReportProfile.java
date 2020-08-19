/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report.implementation;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;

/**
 * Created by chris on 24/09/2017.
 */
public abstract class BaseReportProfile  {

    /**
     * Retrieves the set of column titles for this report.
     * @return headers
     */
    public abstract Set<String> getHeaders();

    protected Object getReportFieldValue(final Object value) {
        Object transformedValue = value;
        if (value != null) {
            if (value instanceof Boolean) {
                transformedValue = (Boolean)value ? "YES" : "NO";
            } else if (value instanceof Date) {
                transformedValue = new SimpleDateFormat("yyyy-MM-dd").format((Date)value);
            } else if (value instanceof OffsetDateTime) {
                OffsetDateTime dt = (OffsetDateTime) value;
                transformedValue = dt.format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
        } else {
            transformedValue = null;
        }
        return transformedValue;
    }

}
