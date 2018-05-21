/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MetricsLoggingAspect {

    Logger log = LoggerFactory.getLogger(getClass());

    @Value("${metrics.execution.time.threshold}")
    long threshold = 100;

    @Around("@annotation(uk.gov.london.ops.aop.LogMetrics)")
    public Object logMetrics(ProceedingJoinPoint pjp) throws Throwable {
        long before = System.currentTimeMillis();
        Object result = pjp.proceed();
        long after = System.currentTimeMillis();
        long executionTime = after - before;
        if (executionTime > threshold) {
            log.info("{} execution time: {}", pjp.getSignature().toShortString(), executionTime);
        }
        return result;
    }

}
