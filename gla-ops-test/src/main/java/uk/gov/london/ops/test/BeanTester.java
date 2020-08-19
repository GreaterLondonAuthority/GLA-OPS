/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Automatic testing of JavaBean classes.
 * <p>
 * Ensures that getters and setters are correctly matched, so that the value supplied to the setter
 * is the same as the value returned by the getter.
 * <p>
 * Also useful to get quickly get code coverage up for "too simple to fail" property accessors.
 *
 * @author Steve Leach
 */
public class BeanTester {

    Logger log = LoggerFactory.getLogger(getClass());

    public class BeanValidationFailure extends RuntimeException {
        public BeanValidationFailure(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final Map<Class<?>, Object> classMap = new HashMap<>();

    private final Set<Method> excludedSetters = new HashSet<>();

    private final Random random = new Random();

    /**
     * Tests that the bean can be instantiated and that getters and setters match.
     *
     * @author Steve Leach
     * @param beanClass the class to be tested
     * @throws BeanValidationFailure if the bean is not valid
     */
    public void testBean(Class<?> beanClass) {
        log.info("Validating JavaBean class: " + beanClass.getCanonicalName());
        Object bean = instantiate(beanClass);
        testBeanInstance(bean);
    }

    public void testBeanInstance(Object instance) {
        log.info("Validating JavaBean instance: " + instance.getClass().getCanonicalName());
        validateToString(instance);
        validateProperties(instance.getClass(), instance);
        validateHashCodeAndEquals(instance);
    }

    private void validateHashCodeAndEquals(Object instance) {
        Object instance2 = instance;

        if (instance.hashCode() != instance2.hashCode()) {
            throw new BeanValidationFailure("Hashcode mismatch on same instance for " + instance.getClass().getName(), null);
        }

        if (!instance.equals(instance2)) {
            throw new BeanValidationFailure("Not equals on same instance for " + instance.getClass().getName(), null);
        }

        if (instance.equals(null)) {
            throw new BeanValidationFailure("Instance equals null for " + instance.getClass().getName(), null);
        }
    }

    /**
     * Use the specified object to test getters/setters of the specified class.
     */
    public void mapClass(Class<?> classDef, Object obj) {
        classMap.put(classDef, obj);
    }

    private void validateProperties(Class<?> beanClass, Object bean) {
        for (Method method : beanClass.getMethods()) {
            if (excludedSetters.contains(method)) {
                continue;
            }
            if (method.getName().startsWith("set")) {
                if (method.getParameterTypes().length == 1) {
                    Method setter = method;
                    Class<?> propertyType = setter.getParameterTypes()[0];
                    Method getter = findGetter(beanClass, setter, propertyType);
                    if ((getter != null) && getter.getReturnType().equals(propertyType)) {
                        Object value = createValue(propertyType);
                        if (value != null) {
                            setterGetterRoundTrip(beanClass, bean, setter, getter, value);
                        }
                    }
                }
            }
        }
    }

    private void validateToString(Object bean) {
        if (bean.toString() == null) {
            throw new BeanValidationFailure("toString returned null", null);
        }
    }

    private void setterGetterRoundTrip(Class<?> beanClass, Object bean, Method setter, Method getter, Object value) {
        String propertyName = beanClass.getCanonicalName() + "." + setter.getName().substring(3);
        try {
            setAccessible(setter, getter);
            setter.invoke(bean, value);
            Object result = getter.invoke(bean);
            if ((result == null) && (value != null)) {
                throw new BeanValidationFailure("Getter returned unexpected null: " + propertyName, null);
            }
            if (!result.equals(value)) {
                throw new BeanValidationFailure("Getter did not return value set with setter: " + propertyName, null);
            }
        } catch (IllegalAccessException e) {
            throw new BeanValidationFailure("Cannot access " + propertyName, null);
        } catch (InvocationTargetException e) {
            throw new BeanValidationFailure("Cannot access " + propertyName + ", " + e.getCause().getMessage(), null);
        }
    }

    private void setAccessible(Method setter, Method getter) {
        setter.setAccessible(true);
        getter.setAccessible(true);
    }

    private Method findGetter(Class<?> beanClass, Method setter, Class<?> propertyType) {
        String getterName = findGetterName(setter, propertyType);
        try {
            return beanClass.getMethod(getterName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private String findGetterName(Method setter, Class<?> propertyType) {
        if (propertyType.equals(Boolean.TYPE)) {
            return setter.getName().replaceFirst("set", "is");
        } else {
            return setter.getName().replaceFirst("set", "get");
        }
    }

    private Object createValue(Class<?> propertyType) {
        Object value = null;
        if (propertyType.equals(String.class)) {
            value = "ABC" + random.nextLong();
        } else if (propertyType.equals(Integer.TYPE) || propertyType.equals(Integer.class)) {
            value = random.nextInt();
        } else if (propertyType.equals(Boolean.TYPE)) {
            value = true;
        } else if (propertyType.equals(Date.class)) {
            value = new Date(random.nextLong());
        } else if (propertyType.equals(BigDecimal.class)) {
            value = new BigDecimal("1.0");
        } else if (propertyType.equals(Boolean.class)) {
            value = Boolean.TRUE;
        } else if (propertyType.equals(OffsetDateTime.class)) {
            value = OffsetDateTime.now();
        } else if (classMap.containsKey(propertyType)) {
            return classMap.get(propertyType);
        } else {
            value = attemptObjectInstantation(propertyType);
        }
        return value;
    }

    private Object attemptObjectInstantation(Class<?> propertyType) {
        try {
            return propertyType.newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            throw new BeanValidationFailure("Cannot access " + propertyType, null);
        }
    }

    private Object instantiate(Class<?> beanClass) {
        try {
            return beanClass.newInstance();
        } catch (Exception e) {
            throw new BeanValidationFailure("Error instantiating " + beanClass.getCanonicalName(), e);
        }
    }

    /**
     * Exclude property with the specified setter from testing.
     */
    public void excludeMethod(Method setter) {
        excludedSetters.add(setter);
    }
}
