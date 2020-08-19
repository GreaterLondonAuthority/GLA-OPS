/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an implicit foreign key that is not managed by JPA.
 *
 * @author Steve Leach
 */
@Target({ElementType.FIELD,  ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinData {
    String sourceTable() default "";
    String sourceColumn() default "";
    String targetTable() default  "-";
    String targetColumn() default  "-";
    Join.JoinType joinType() default Join.JoinType.Unknown;
    String comment();
}
