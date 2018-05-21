/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.util.jpajoins;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import uk.gov.london.ops.domain.project.NamedProjectBlock;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Scans annotations on JPA Entity classes to determine join columns.
 *
 * @author Steve Leach
 */
public class TableJoinJpaAnnotationProcessor {

    Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Returns a list of all the JPA Entity joins that can be found in the domain model.
     */
    public List<Join> getJpaEntityJoins() {

        List<Join> joins = new LinkedList<>();

        for (Class entityClass : findJpaEntities("uk.gov.london.ops.domain")) {
            List<Join> entityJoins = getJoinsForEntityClass(entityClass);
            joins.addAll(entityJoins);
        }

        sortJoins(joins);

        return joins;
    }

    /**
     * Returns a list of all JPA Entity joins that can be identified by looking at annotations on the specified class.
     */
    public List<Join> getJoinsForEntityClass(Class<?> beanClass) {

        List<Join> joins = new LinkedList<>();

        if (beanClass.getAnnotation(NonJoin.class) != null) {
            // Entity is specifically marked as not providing join information, so return the empty list
            return joins;
        }

        Join join = getJoinDataForDeclaringClass(beanClass);
        if (join != null) {
            joins.add(join);
        }

        for (Field field : beanClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                // Static fields can't be columns

                List<Join> fieldJoins = getJoinDataForField(field);

                joins.addAll(fieldJoins);
            }
        }

        return joins;
    }

    /**
     * Returns a list of all JPA Entity joins that can be identified by looking at annotations on the specified field.
     *
     * In many-to-many relationships, one field can identify two table joins.
     *
     * If no joins can be identified then an empty list is returned.
     */
    public List<Join> getJoinDataForField(Field field) {
        Class<?> beanClass = field.getDeclaringClass();
        field.setAccessible(true);
        Class fieldType = field.getType();

        String tableName = getTableNameForEntity(beanClass);
        String columnName = getColumnName(field);

        List<Join> joins = new LinkedList<>();

        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        JoinColumns joinColumns = field.getAnnotation(JoinColumns.class);
        JoinTable joinTable = field.getAnnotation(JoinTable.class);
        NonJoin nonJoin = field.getAnnotation(NonJoin.class);
        JoinData joinData = field.getAnnotation(JoinData.class);
        Transient transientField = field.getAnnotation(Transient.class);

        if (nonJoin != null) {

            if (oneToOne != null || manyToOne != null || oneToMany != null || manyToMany != null) {
                log.warn("{}.{} has both NonJoin and join type annotations", beanClass.getSimpleName(), field.getName());
            }

        } else if (joinData != null) {

            joins.add( defineNonJpaJoin(columnName, joinData) );

        } else if (manyToMany != null) {

            joins.addAll( defineManyToManyJoin(field, joinTable) );

        } else if (manyToOne != null) {

            joins.add( defineManyToOneJoin(field, joinColumn, joinColumns) );

        } else if (oneToMany != null) {

            joins.add( defineOneToManyJoin(beanClass, fieldType, oneToMany, joinColumn, joinTable) );

        } else if (oneToOne != null) {

            joins.add( defineOneToOneJoin(fieldType, columnName, joinColumn) );

        }  else if (joinColumn != null || joinTable != null) {

            log.warn("JoinColumn or JoinTable on field with no join type: {}.{}", beanClass.getSimpleName(), field.getName());

        } else {
            // Field has no join annotations, but name suggests it might be a foreign key
            if ((columnName.endsWith("_id") || columnName.endsWith("_code")) && transientField == null) {
                log.warn("{}.{} should be a join column?", beanClass.getSimpleName(), field.getName());
            }
        }

        for (Join join : joins) {
            if (join.getFromTable() == null || join.getFromTable().equals("") || join.getFromTable().equals("?")) {
                join.setDeclaringEntity(beanClass.getSimpleName());
                if (StringUtils.isEmpty(join.getFromTable()) || "?".equals(join.getFromTable())) {
                    join.setFromTable(tableName);
                }
            }
        }

        return joins;
    }

    /**
     * Explicitly defined join data
     */
    Join getJoinDataForDeclaringClass(Class<?> beanClass) {
        JoinData annotation = beanClass.getAnnotation(JoinData.class);
        if (annotation != null) {
            return defineNonJpaJoin(annotation.sourceColumn(), annotation);
        }
        else {
            return null;
        }
    }

    /**
     * Explicitly defined join data
     */
    private Join defineNonJpaJoin(String columnName, JoinData joinData) {

        Join join = new Join(joinData.joinType());
        join.setFromColumn(columnName);
        join.setFromTable(joinData.sourceTable());
        join.setToTable(joinData.targetTable());
        join.setToColumn(joinData.targetColumn());
        join.setComments(joinData.comment());
        if (!joinData.sourceColumn().equals("")) {
            join.setFromColumn(joinData.sourceColumn());
        }
        return join;
    }

    private Join defineOneToOneJoin(Class fieldType, String columnName, JoinColumn joinColumn) {
        Join join = new Join(Join.JoinType.OneToOne);

        if (joinColumn != null) {
            join.setFromColumn(joinColumn.name());
            join.setToTable(getTableNameForEntity(fieldType));
            join.setToColumn(getPrimaryKey(fieldType));
        } else {
            join.setFromColumn(columnName);
            join.setToTable(getTableNameForEntity(fieldType));
            join.setToColumn(getPrimaryKey(fieldType));
        }
        return join;
    }

    private Join defineOneToManyJoin(Class<?> beanClass, Class fieldType, OneToMany oneToMany, JoinColumn joinColumn, JoinTable joinTable) {
        Join join = new Join(Join.JoinType.OneToMany);

        if (oneToMany.targetEntity() != null) {
            join.setToTable(getTableNameForEntity(oneToMany.targetEntity()));
        }
        if (oneToMany.mappedBy() != null) {
            join.setToColumn(oneToMany.mappedBy().toLowerCase());
        }

        if (joinColumn != null) {
            join.setToColumn(joinColumn.name());
            if (join.getToTable().equals("?")) {
                join.setToTable(getTableNameForEntity(fieldType));
            }
        } else if (joinTable != null) {
            join.setToTable(joinTable.name().toLowerCase());
            join.setToColumn(joinTable.joinColumns()[0].name().toLowerCase());
        }

        join.setFromColumn(getPrimaryKeyColumnName(beanClass));

        return join;
    }

    private Join defineManyToOneJoin(Field field, JoinColumn joinColumn, JoinColumns joinColumns) {
        Join join = new Join(Join.JoinType.ManyToOne);

        if (joinColumn != null) {
            join.setFromColumn(joinColumn.name());
            join.setToTable(getTableNameForEntity(field.getType()));
            join.setToColumn(getPrimaryKeyColumnName(field.getType()));
        } else if (joinColumns != null) {
            // TODO - handle this
            join.setToTable(getTableNameForEntity(field.getType()));
            log.warn("Cannot currently handle multi-column joins");
        }
        return join;
    }

    private List<Join> defineManyToManyJoin(Field field, JoinTable joinTable) {
        List<Join> joins = new ArrayList<>();

        if (joinTable != null) {
            Join joinA = new Join(Join.JoinType.ManyToMany);
            joinA.setFromColumn(joinTable.joinColumns()[0].referencedColumnName().toLowerCase());
            joinA.setToTable(joinTable.name().toLowerCase());
            joinA.setToColumn(joinTable.joinColumns()[0].name().toLowerCase());
            joins.add(joinA);

            Class collectionActualType = getCollectionActualType(field);

            Join joinB = new Join(Join.JoinType.ManyToMany);
            joinB.setFromTable(joinTable.name().toLowerCase());
            joinB.setFromColumn(joinTable.inverseJoinColumns()[0].name().toLowerCase());
            joinB.setToTable(getTableNameForEntity(collectionActualType));
            joinB.setToColumn(joinTable.inverseJoinColumns()[0].referencedColumnName().toLowerCase());
            joins.add(joinB);
        }

        return joins;
    }

    private Class getCollectionActualType(Field field) {
        ParameterizedType fieldGenericType = (ParameterizedType) field.getGenericType();
        return (Class<?>) fieldGenericType.getActualTypeArguments()[0];
    }

    String getPrimaryKey(Class<?> beanClass) {
        for (Field field : beanClass.getDeclaredFields()) {
            if (field.getAnnotation(Id.class) != null) {
                return javaToSqlName(field.getName());
            }
        }
        return "???";
    }

    Class<?> getJavaClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not instantiate instance of " + className, e);
        }
    }

    private String getColumnName(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null) {
            if (columnAnnotation.name() != null && columnAnnotation.name().trim().length() > 0) {
                return columnAnnotation.name().toLowerCase();
            }
        }
        return javaToSqlName(field.getName());
    }

    public static String javaToSqlName(String javaName) {
        StringBuilder columnName = new StringBuilder();
        for (char c : javaName.toCharArray()) {
            if (Character.isUpperCase(c) && columnName.length() > 0) {
                columnName.append('_');
            }
            columnName.append(Character.toLowerCase(c));
        }
        return columnName.toString();
    }

    private void sortJoins(List<Join> joins) {
        joins.sort(Comparator.comparing(Join::toString));
    }

    private String getTableNameForEntity(Class entityClass) {
        Entity  entityAnnotation = (Entity) entityClass.getAnnotation(Entity.class);
        if (entityAnnotation != null && entityAnnotation.name() != null && entityAnnotation.name().trim().length() > 0) {
            return entityAnnotation.name().toLowerCase();
        }
        return javaToSqlName(entityClass.getSimpleName());
    }

    private String getPrimaryKeyColumnName(Class entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.getAnnotation(Id.class) != null) {
                return field.getName();
            }
        }

        // Didn't find an ID column in this class, try it's superclass
        return getPrimaryKeyColumnName(entityClass.getSuperclass());
    }

    private List<Class> findJpaEntities(String basePackage) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        List<Class> entityClasses = new LinkedList<>();
        for (BeanDefinition bean : scanner.findCandidateComponents(basePackage)) {
            entityClasses.add( getJavaClass(bean.getBeanClassName()) );
        }

        entityClasses.add(NamedProjectBlock.class);
        return entityClasses;
    }


}
