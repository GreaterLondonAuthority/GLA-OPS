/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RepositoryAuditingAspect {

    Logger log = LoggerFactory.getLogger(getClass());

    @Value("${repository.execution.time.threshold}")
    long threshold = 1000;

    @Around("execution(* uk.gov.london.ops.repository.*Repository.*(..))")
    public Object logRepositoryMethodExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long before = System.currentTimeMillis();

        Object result = pjp.proceed();

        long after = System.currentTimeMillis();

        long executionTime = after-before;
        if (executionTime > threshold) {
            log.info("{} execution time: {}", getClassAndMethodName(pjp), executionTime);
        }

        return result;
    }

    private String getClassAndMethodName(ProceedingJoinPoint pjp) {
        try {
            for (Class itf: pjp.getTarget().getClass().getInterfaces()) {
                if (itf.getName().contains("uk.gov.london.ops.repository")) {
                    return pjp.getTarget().getClass().getInterfaces()[0].getSimpleName()+"."+pjp.getSignature().getName()+"(..)";
                }
            }
        }
        catch (Exception e) {
            log.warn("could not extract target class name", e);
        }

        return pjp.getSignature().toShortString();
    }

}
