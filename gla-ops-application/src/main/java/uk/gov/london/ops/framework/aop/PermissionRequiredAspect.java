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
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.permission.PermissionService;
import uk.gov.london.ops.permission.PermissionType;
import uk.gov.london.ops.user.UserService;

import java.lang.reflect.Method;

@Aspect
@Component
public class PermissionRequiredAspect {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserService userService;

    @Before("@annotation(uk.gov.london.ops.framework.annotations.PermissionRequired)")
    public void check(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        PermissionRequired annotation = method.getAnnotation(PermissionRequired.class);

        PermissionType permission = annotation.value()[0];
        if (userService.currentUser() == null || !permissionService.currentUserHasPermission(permission)) {
            throw new ForbiddenAccessException();
        }
    }

}
