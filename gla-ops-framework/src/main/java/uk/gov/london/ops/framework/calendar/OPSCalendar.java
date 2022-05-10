/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.calendar;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.london.ops.framework.environment.Environment;

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
     * Returns the custom calendar year & period, given an actual calendar year & month.
     */
    protected YearMonth getCustomCalendarYearMonthFromActualYearMonth(int actualYear, int actualMonth) {
        if (actualMonth < firstMonthOfYear) {
            // In the last months of the previous financial year
            return YearMonth.of(actualYear - 1, actualMonth + (12 - firstMonthOfYear) + 1);
        } else {
            return YearMonth.of(actualYear, actualMonth - firstMonthOfYear + 1);
        }
    }

    /**
     * Returns the current financial year.
     */
    public int currentYear() {
        OffsetDateTime now = environment.now();
        int year = now.getYear();
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
        return year * 100 + month;
    }

    public static String financialYearString(int startYear) {
        return String.format("%d/%d", startYear, startYear + 1);
    }

    /**
     * returns the string representation (ex: "2015/2016") of the financial year for the given date.
     */
    public String financialYearString(LocalDate date) {
        if (date == null) {
            return "";
        }

        int financialYear = getCustomCalendarYearMonthFromActualYearMonth(date.getYear(), date.getMonthValue()).getYear();
        return financialYearString(financialYear);
    }

    public static String yearStringShort(int startYear) {
        return String.format("%d/%d", startYear, (startYear + 1) % 100);
    }

    /**
     * Returns the first calendar year in the financial year represented by the string.
     *
     * @param yearString in format "2012/2013"
     */
    public int parseFinancialYearString(String yearString) {
        return Integer.parseInt(yearString.split("/")[0]);
    }

    /**
     * Returns an actual calendar year & month, given the custom calendar year & month.
     */
    protected YearMonth getActualYearMonthFromCustomCalendarYearMonth(int year, int month) {
        return month <= getLastMonthOfFinancialYear()
                ? YearMonth.of(year, month + firstMonthOfYear - 1)
                : YearMonth.of(year + 1, (month + firstMonthOfYear - 1) % 12);
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
        return getCustomCalendarYearMonthFromActualYearMonth(yearMonth / 100, yearMonth % 100).getYear();
    }

}

