/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.refdata.RefDataService;

import java.time.OffsetDateTime;

public class BaseAnnualSummaryMapper {

    @Autowired
    RefDataService refDataService;

    @Autowired
    Environment environment;

    int getCurrentYearStart(int year) {
        return (year * 100) + 4;
    }

    int getCurrentYearEnd(int year) {
        return ((year + 1) * 100) + 3;
    }

    int getCurrentYearMonth() {
        OffsetDateTime currentDate = environment.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();
        return (currentYear * 100) + currentMonth;
    }

    int getArrayPositionByMonth(int month) {
        month = month - 4;
        if (month < 0) {
            month += 12;
        }
        return month;
    }

}
