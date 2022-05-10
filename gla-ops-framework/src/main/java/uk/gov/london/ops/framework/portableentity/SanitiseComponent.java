/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.portableentity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Component
public class SanitiseComponent {

    private final Logger log = LoggerFactory.getLogger(getClass());


    public Object sanitise(Object toStrip, List<Class> excludedClasses) {
        return stripIds(toStrip, excludedClasses, new HashSet<>());
    }

    private Object stripIds(Object toStrip, List<Class> excludedClasses, HashSet<String> visited) {

        sanitiseIds(toStrip, toStrip.getClass().getDeclaredFields(), toStrip.getClass().getSimpleName(), excludedClasses, visited);
        sanitiseIds(toStrip, toStrip.getClass().getSuperclass().getDeclaredFields(),
                toStrip.getClass().getSuperclass().getSimpleName(), excludedClasses, visited);
        if (toStrip.getClass().getSuperclass().getSuperclass() != null) {
            sanitiseIds(toStrip, toStrip.getClass().getSuperclass().getSuperclass().getDeclaredFields(),
                    toStrip.getClass().getSuperclass().getSuperclass().getSimpleName(), excludedClasses, visited);
        }
        return toStrip;
    }

    private void sanitiseIds(Object object, Field[] fields, String className, List<Class> excludedClasses,
            HashSet<String> visited) {
        if (visited.contains(className + object.hashCode())) {
            return;
        } else {
            visited.add(className + object.hashCode());
        }
        for (Field field : fields) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    && field.getAnnotationsByType(JsonIgnore.class).length == 0) {
                try {
                    field.setAccessible(true);
                    if (field.getName().equals("id") && !excludedClasses.contains(object)) {
                        field.set(object, null);
                    } else if (field.get(object) instanceof Collection) {
                        Collection collection = (Collection) field.get(object);
                        for (Object item : collection) {
                            if (!excludedClasses.contains(item.getClass())) {
                                stripIds(item, excludedClasses, visited);
                            }
                        }
                    } else if (field.get(object) != null && field.get(object).getClass().getName().startsWith("uk.gov.london.ops")
                            && !excludedClasses.contains(field.get(object).getClass())) {
                        stripIds(field.get(object), excludedClasses, visited);
                    }
                } catch (IllegalAccessException e) {
                    log.error("could not nullify field on class " + object.getClass().getName(), e);
                }
            }
        }
    }

}
