/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.*;

@Component
public class CpuSpeedInformationContributor implements InfoContributor {

    public static final int TEST_TIME_IN_MS = 200;

    public void contribute(Info.Builder builder) {
        Map<String,Object> data = new TreeMap<>();
        data.put("iterations", measureCpuPerformance(TEST_TIME_IN_MS));
        data.put("elapsed_ms", 200);
        builder.withDetail("cpu_performance", data);
    }

    public static int measureCpuPerformance(long testTimeInMs) {
        long start = System.currentTimeMillis();
        long elapsed = 0;
        int iterations;

        for (iterations=0; elapsed < testTimeInMs; iterations++) {
            // Lots of heavy maths
            double d = tan(atan(tan(atan(tan(atan(tan(atan(tan(atan(123456789.123456789))))))))));
            cbrt(d);

            if (iterations % 100 == 0) {
                elapsed = System.currentTimeMillis() - start;
            }
        }

        return iterations;
    }
}
