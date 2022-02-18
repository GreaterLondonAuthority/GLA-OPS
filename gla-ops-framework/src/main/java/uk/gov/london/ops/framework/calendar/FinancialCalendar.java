/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.calendar;

import java.time.YearMonth;

import static java.time.Month.APRIL;

/**
 * Support for financial years, which start in a different month to calendar years.
 *
 * @author Steve Leach
 */
public class FinancialCalendar extends OPSCalendar {

    public FinancialCalendar() {
        super(APRIL.getValue());
    }

    /**
     * Returns a financial year & month, given a calendar year & month.
     */
    public YearMonth financialFromCalendarYearMonth(int calendarYear, int calendarMonth) {
        return super.getCustomCalendarYearMonthFromActualYearMonth(calendarYear, calendarMonth);
    }

    /**
     * Returns a calendar year & month, given a financial year & month.
     */
    public YearMonth calendarFromFinancialYearMonth(int financialYear, int financialMonth) {
        return super.getActualYearMonthFromCustomCalendarYearMonth(financialYear, financialMonth);
    }

}
