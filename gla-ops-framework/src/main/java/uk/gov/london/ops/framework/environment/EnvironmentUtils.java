/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.environment;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class EnvironmentUtils {
    private static final Clock CLOCK = Clock.system(ZoneId.of("Z"));

    public static Clock clock() {
        return EnvironmentUtils.CLOCK;
    }

    public static OffsetDateTime now() {
        return OffsetDateTime.now(CLOCK);
    }
}
