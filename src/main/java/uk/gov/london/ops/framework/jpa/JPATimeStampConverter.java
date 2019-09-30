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
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Maps a Java 8 OffsetDateTime to/from a JPA Timestamp
 *
 * Based on http://www.thoughts-on-java.org/persist-localdate-localdatetime-jpa/
 *
 * @author Steve Leach
 */
@Converter(autoApply = true)
public class JPATimeStampConverter implements AttributeConverter<OffsetDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            return Timestamp.from(dateTime.toInstant());
        }
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        } else {
            return OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
        }
    }
}
