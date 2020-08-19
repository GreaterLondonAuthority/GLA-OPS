/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Maps a Java 8 LocalDate to/from a JPA TimeSteamp
 *
 * Based on http://www.thoughts-on-java.org/persist-localdate-localdatetime-jpa/
 *
 * @author Chris Melville
 */
@Converter(autoApply = true)
public class JPADateConverter implements AttributeConverter<LocalDate, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(LocalDate dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            return Timestamp.from(dateTime.atStartOfDay().toInstant(ZoneOffset.UTC));
        }
    }

    @Override
    public LocalDate convertToEntityAttribute(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        } else {
            return timestamp.toLocalDateTime().toLocalDate();
        }
    }
}
