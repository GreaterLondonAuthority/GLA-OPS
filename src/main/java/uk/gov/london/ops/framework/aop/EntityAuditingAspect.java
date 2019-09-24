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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.OpsEntity;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.service.UserService;

import java.lang.reflect.Field;
import java.util.Collection;

@Aspect
@Component
public class EntityAuditingAspect {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

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

        auditChangesWhereApplicable(entity,true);

        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field: fields) {
            field.setAccessible(true);
            auditChangesWhereApplicable(field.get(entity),false);
        }
    }

    void auditChangesWhereApplicable(Object entity,boolean updateLastModificationValues) {
        if (entity instanceof OpsEntity) {
            auditChanges((OpsEntity) entity,updateLastModificationValues);
        }

        if (entity instanceof Collection) {
            Collection collection = (Collection) entity;
            collection.stream().filter(o -> o instanceof OpsEntity).forEach(o -> auditChanges((OpsEntity) o,updateLastModificationValues));
        }
    }

    private void auditChanges(OpsEntity entity,boolean updateLastModificationValues) {
        User currentUser = userService.currentUser();
        
		if (updateLastModificationValues) {
			if (currentUser != null) {
				if(entity.getCreatedBy() == null) {
				    entity.setCreatedBy(currentUser.getUsername());
				}else{
				    entity.setModifiedBy(currentUser.getUsername());
				}
			}
			if (entity.getCreatedOn() == null) {
				entity.setCreatedOn(environment.now());
			}else{
			    entity.setModifiedOn(environment.now());
			}
		} else {
			if (currentUser != null && entity.getCreatedBy() == null) {
				entity.setCreatedBy(currentUser.getUsername());
			}
			if (entity.getCreatedOn() == null) {
				entity.setCreatedOn(environment.now());
			}
		}
    }

}
