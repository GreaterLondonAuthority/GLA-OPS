/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.finance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.Environment;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;

/**
 * Support for financial years, which start in a different month to calendar years.
 *
 * @author Steve Leach
 */
@Component
public class FinancialCalendar {

    @Autowired
    Environment environment;

    private final int firstMonthOfFinancialYear;

    public static final int DEFAULT_START_MONTH = 4;

    public FinancialCalendar() {
        this(DEFAULT_START_MONTH);
    }

    public FinancialCalendar(int firstMonthOfFinancialYear) {
        this.firstMonthOfFinancialYear = firstMonthOfFinancialYear;
    }

    public int getFirstMonthOfFinancialYear() {
        return firstMonthOfFinancialYear;
    }

    /**
     * Returns a financial year & month, given a calendar year & month.
     */
    public YearMonth financialFromCalendarYearMonth(int calendarYear, int calendarMonth) {
        if (calendarMonth < firstMonthOfFinancialYear) {
            // In the last months of the previous financial year
            return YearMonth.of(calendarYear-1, calendarMonth+(12- firstMonthOfFinancialYear)+1);
        } else {
            return YearMonth.of(calendarYear, calendarMonth- firstMonthOfFinancialYear +1);
        }
    }

    /**
     * Returns the current financial year.
     */
    public int currentFinancialYear() {
        OffsetDateTime now = environment.now();
        Integer year = now.getYear();
        if (now.getMonthValue() < firstMonthOfFinancialYear) {
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

    public String financialYearString(int startYear) {
        return String.format("%d/%d", startYear, startYear+1);
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
                ? YearMonth.of(financialYear, financialMonth + getFirstMonthOfFinancialYear() -1)
                : YearMonth.of(financialYear + 1, (financialMonth + getFirstMonthOfFinancialYear() -1) % 12);
    }

    private int getLastMonthOfFinancialYear() {
        return 12 - getFirstMonthOfFinancialYear() + 1;
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

