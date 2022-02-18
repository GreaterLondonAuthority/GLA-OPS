/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.OpsEntity;
import uk.gov.london.ops.framework.environment.Environment;

import java.lang.reflect.Field;
import java.util.Collection;

import static uk.gov.london.ops.framework.OPSUtils.currentUsername;

@Aspect
@Component
public class EntityAuditingAspect {

    @Autowired
    Environment environment;

    @Before("execution(* org.springframework.data.jpa.repository.JpaRepository+.save(..))")
    public void auditEntityChangeOnSave(JoinPoint joinPoint) throws Throwable {
        auditEntityChange(joinPoint);
    }

    @Before("execution(* org.springframework.data.jpa.repository.JpaRepository+.saveAll(..))")
    public void auditEntityChangeOnSaveAll(JoinPoint joinPoint) throws Throwable {
        auditEntityChange(joinPoint);
    }

    void auditEntityChange(JoinPoint joinPoint) throws Throwable {
        Object entity = joinPoint.getArgs()[0];

        auditChangesWhereApplicable(entity, true);

        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            auditChangesWhereApplicable(field.get(entity), false);
        }
    }

    void auditChangesWhereApplicable(Object entity, boolean updateLastModificationValues) {
        if (entity instanceof OpsEntity) {
            auditChanges((OpsEntity) entity, updateLastModificationValues);
        }

        if (entity instanceof Collection) {
            Collection collection = (Collection) entity;
            collection.stream()
                    .filter(o -> o instanceof OpsEntity)
                    .forEach(o -> auditChanges((OpsEntity) o, updateLastModificationValues));
        }
    }

    private void auditChanges(OpsEntity entity, boolean updateLastModificationValues) {
        String currentUsername = currentUsername();

        if (updateLastModificationValues) {
            if (currentUsername != null) {
                if (entity.getCreatedBy() == null) {
                    entity.setCreatedBy(currentUsername);
                } else {
                    entity.setModifiedBy(currentUsername);
                }
            }
            if (entity.getCreatedOn() == null) {
                entity.setCreatedOn(environment.now());
            } else {
                entity.setModifiedOn(environment.now());
            }
        } else {
            if (currentUsername != null && entity.getCreatedBy() == null) {
                entity.setCreatedBy(currentUsername);
            }
            if (entity.getCreatedOn() == null) {
                entity.setCreatedOn(environment.now());
            }
        }
    }

}
