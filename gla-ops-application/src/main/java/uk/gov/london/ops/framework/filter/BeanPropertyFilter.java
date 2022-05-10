/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.filter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.annotations.PermissionRequired;
import uk.gov.london.ops.permission.PermissionServiceImpl;
import uk.gov.london.ops.permission.PermissionType;
import uk.gov.london.ops.user.UserServiceImpl;
import uk.gov.london.ops.user.domain.UserEntity;

import java.util.Set;

/**
 * Checks for PermissionRequried annotation and checks user's basic permissions Created by chris on 16/03/2017.
 */
@Component
public class BeanPropertyFilter implements PropertyFilter {

    @Autowired
    UserServiceImpl userService;

    @Autowired
    PermissionServiceImpl permissionService;

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer)
            throws Exception {

        if (writer.getAnnotation(PermissionRequired.class) == null) {
            writer.serializeAsField(pojo, jgen, provider);
            return;
        }
        PermissionType[] permissionRequired = writer.getAnnotation(PermissionRequired.class).value();
        UserEntity user = userService.currentUser();

        Set<String> permissionsForUser = permissionService.getPermissionsForUser(user);
        boolean showProperty = false;
        for (PermissionType required : permissionRequired) {
            for (String userPermission : permissionsForUser) {
                if (required.getPermissionKey().equals(userPermission)) {
                    showProperty = true;
                }
            }
        }

        if (showProperty) {
            writer.serializeAsField(pojo, jgen, provider);
        } else {
            writer.serializeAsOmittedField(pojo, jgen, provider);
        }
    }

    @Override
    public void serializeAsElement(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider,
            PropertyWriter propertyWriter) {

    }

    @Override
    public void depositSchemaProperty(PropertyWriter propertyWriter, ObjectNode objectNode,
            SerializerProvider serializerProvider) {

    }

    @Override
    public void depositSchemaProperty(PropertyWriter propertyWriter, JsonObjectFormatVisitor jsonObjectFormatVisitor,
            SerializerProvider serializerProvider) {

    }
}