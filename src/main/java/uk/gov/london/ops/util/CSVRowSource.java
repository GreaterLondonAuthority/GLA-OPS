/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Interface for mapping rows from a CSV file into Java objects.
 *
 * @author Steve Leach
 */
public interface CSVRowSource {

    String getString(String key);

    int getInteger(String key);

    BigDecimal getCurrencyValue(String key);

    LocalDate getDate(String key, String format);

    int getRowIndex();

    String getCurrentRowSource() throws IOException;

}
