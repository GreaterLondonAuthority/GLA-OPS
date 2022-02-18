/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONUtils {

    private static final Logger log = LoggerFactory.getLogger(JSONUtils.class);

    public static String toJSON(Object o) {
        if (o != null) {
            try {
                return new ObjectMapper().writeValueAsString(o);
            } catch (Exception e) {
                log.error("could not serialise object to JSON: " + o, e);
            }
        }
        return null;
    }

    public static <T> T fromJSON(String json, Class<T> type) {
        if (StringUtils.isNotEmpty(json)) {
            try {
                return new ObjectMapper().readValue(json, type);
            } catch (Exception e) {
                log.error("could not deserialise JSON: " + json, e);
            }
        }
        return null;
    }

}
