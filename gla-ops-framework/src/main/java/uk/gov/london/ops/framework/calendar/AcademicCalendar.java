/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.calendar;

import java.time.YearMonth;

import static java.time.Month.AUGUST;

public class AcademicCalendar extends OPSCalendar {

    public static final AcademicCalendar academicCalendar = new AcademicCalendar();

    public AcademicCalendar() {
        super(AUGUST.getValue());
    }

    /**
     * Returns a academic year & period, given an actual year & month.
     */
    public YearMonth academicYearMonthFromActual(int actualYear, int actualMonth) {
        return super.getCustomCalendarYearMonthFromActualYearMonth(actualYear, actualMonth);
    }

    /**
     * Returns a calendar year & month, given a financial year & month.
     */
    public YearMonth actualYearMonthFromAcademic(int academicYear, int period) {
        return super.getActualYearMonthFromCustomCalendarYearMonth(academicYear, period);
    }

}
