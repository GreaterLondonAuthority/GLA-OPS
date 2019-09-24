/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.calendar;

/**
 * Support for financial years, which start in a different month to calendar years.
 *
 * @author Steve Leach
 */
public class FinancialCalendar extends OPSCalendar {

    public FinancialCalendar() {
        super(4);
    }

}
