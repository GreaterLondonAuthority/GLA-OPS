/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.london.ops.Environment;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;

/**
 * Support for financial and academic years, which start in a different month to calendar years.
 *
 * @author Steve Leach
 */
public class OPSCalendar {

    @Autowired
    Environment environment;

    private final int firstMonthOfYear;

    public OPSCalendar(int firstMonthOfYear) {
        this.firstMonthOfYear = firstMonthOfYear;
    }

    public int getFirstMonthOfYear() {
        return firstMonthOfYear;
    }

    /**
     * Returns a financial year & month, given a calendar year & month.
     */
    public YearMonth financialFromCalendarYearMonth(int calendarYear, int calendarMonth) {
        if (calendarMonth < firstMonthOfYear) {
            // In the last months of the previous financial year
            return YearMonth.of(calendarYear-1, calendarMonth+(12- firstMonthOfYear)+1);
        } else {
            return YearMonth.of(calendarYear, calendarMonth- firstMonthOfYear +1);
        }
    }

    /**
     * Returns the current financial year.
     */
    public int currentYear() {
        OffsetDateTime now = environment.now();
        Integer year = now.getYear();
        if (now.getMonthValue() < firstMonthOfYear) {
            return year - 1;
        }
        return year;
    }

    /**
     * Returns a single integer representing a year and month.
     *
     * This makes storage and comparisons of the value very simple.
     *
     * The value is "year*100+month".
     */
    public int asInt(int year, int month) {
        return year*100+month;
    }

    public static String financialYearString(int startYear) {
        return String.format("%d/%d", startYear, startYear+1);
    }

    public static String yearStringShort(int startYear) {
        return String.format("%d/%d", startYear, (startYear+1)%100);
    }

    /**
     * Returns the first calendar year in the financial year represented by the string.
     *
     * @param yearString
     *      in format "2012/2013"
     */
    public int parseFinancialYearString(String yearString) {
        return Integer.parseInt(yearString.split("/")[0]);
    }


    /**
     * Returns a calendar year & month, given a financial year & month.
     */
    public YearMonth calendarFromFinancialYearMonth(final int financialYear, final int financialMonth) {
        return financialMonth <= getLastMonthOfFinancialYear()
                ? YearMonth.of(financialYear, financialMonth + firstMonthOfYear -1)
                : YearMonth.of(financialYear + 1, (financialMonth + firstMonthOfYear -1) % 12);
    }

    private int getLastMonthOfFinancialYear() {
        return 12 - firstMonthOfYear + 1;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * transform to financial year / month to the financial year: for example 201604 is 2016 but 201603 is 2015
     */
    public int financialFromYearMonth(Integer yearMonth) {
        return financialFromCalendarYearMonth(yearMonth / 100, yearMonth % 100).getYear();
    }

    /**
     * returns the string representation (ex: "2015/2016") of the financial year for the given date.
     */
    public String financialYearString(LocalDate date) {
        if (date == null) {
            return "";
        }

        int financialYear = financialFromCalendarYearMonth(date.getYear(), date.getMonthValue()).getYear();
        return financialYearString(financialYear);
    }

}

