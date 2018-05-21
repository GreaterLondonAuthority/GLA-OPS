/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

/**
 * Util Class for date manipulations
 */
class DateUtil {
  /**
   * Generate dates for date dropdowns
   * @param {String} chosen - 'YYYY/YY', uses current date if not available
   * @param {Number} backLimit - 'YYYY/YY'
   * @param {Number} forwardLimit - 'YYYY/YY'
   * @return {Array} list - list of generated dates
   */

  // static generateDates(chosen, backLimit, forwardLimit) {
  //   chosen = chosen || this.getFinancialYear(moment());
  //   var now = +chosen.split('/')[0];
  //   var start = backLimit > 0 ? now - backLimit : now;
  //   var end = forwardLimit > 0 ? now + forwardLimit : now;
  //
  //   var list = [];
  //   var limit = end - start;
  //   for (let i = 0; i <= limit; i++) {
  //     list.push(
  //       (start + i).toString() + '/' + (start + i + 1).toString().slice(2, 4)
  //     );
  //   }
  //
  //   return list;
  // }
  static generateDatesObjects(chosen, backLimit, forwardLimit) {
    chosen = chosen || this.getFinancialYear2(moment());
    var now = chosen; //+chosen.split('/')[0];
    // NOTE: negative values are allowed to generate date ranges that are before
    // or after the current financial yearList
    // Example: from date of project budgets block
    var start = _.isNumber(backLimit) > 0 ? now - backLimit : now;
    var end = _.isNumber(forwardLimit) > 0 ? now + forwardLimit : now;

    var list = [];
    var limit = end - start;
    if(limit < 1 ){
      console.error('list back limit cannot be futher than forward limit');
    }
    for (let i = 0; i <= limit; i++) {
      list.push({
        label: (start + i).toString() + '/' + (start + i + 1).toString().slice(2, 4),
        financialYear: start + i
      }
      );
    }

    return list;
  }

  /**
   * Formats a full year integer to string 'YYYY/YY'
   * @param {Number} year
   * @return {String} 'YYYY/YY'
   */
  static toFinancialYearString(year) {
    year = eval(year);
    return `${year}/${moment(year, 'YYYY').add(1, 'y').format('YY')}`;
  }


  /**
   * Returns financial year of the date
   * @param {Moment} date A date to get financial year
   * @returns {string} 'YYYY/YY+1'
   */
  // static getFinancialYear(date) {
  //   let startYear = +(date.format('YYYY'));
  //   let startOfTheNewFinancialYear = moment(`${startYear}/04/01`, 'YYYY/MM/DD');
  //   if (date.isBefore(startOfTheNewFinancialYear)) {
  //     startYear--;
  //   }
  //   let endYear = `${startYear + 1}`.substr(2);
  //   return `${startYear}/${endYear}`;
  // }

  static getFinancialYear2(date) {
    let startYear = +(date.format('YYYY'));
    let startOfTheNewFinancialYear = moment(`${startYear}/04/01`, 'YYYY/MM/DD');
    if (date.isBefore(startOfTheNewFinancialYear)) {
      startYear--;
    }

    return startYear;
  }

  /**
   * Generate list of financial months
   * @param {Number} fYear - Financial Year
   * @returns {Array} list of months from 'April YYYY' to 'March YYYY+1'
   */
  static generateMonthList(fYear) {
    let list = [];
    let month = moment(`03/${fYear}`, 'MM/YYYY');
    for (let i = 0, max = 12; i < max; i++) {
      const date = month.add(1, 'month');
      list.push({
        label: date.format('MMMM YYYY'),
        value: (date.month() + 1),
        calendarYear: date.year(),
        financialYear: fYear
      });
    }
    return list;
  }

  /**
   * Generate list of financial quarters
   * @param {Number} fYear - Financial Year
   * @returns {Array} list of quarters
   */
  static generateQuarterList(fYear) {
    let list = [];
    let month = moment(`04/${fYear}`, 'MM/YYYY');
    for (let i = 0, max = 4; i < max; i++) {
      list.push({
        label: `Q${i+1}`,
        value: (month.month() + 1),
        calendarYear: month.year(),
        financialYear: fYear
      });
      month = month.add(3, 'month');
    }
    return list;
  }

  /**
   * Return the first (default) or second year of YYYY/YY date format of financial year
   * @param {string} financialYear YYYY/YY format of financial year
   * @param {boolean} secondYear Second part of financial year
   */
  static getYear(financialYear, secondYear) {
    if (!/\d{4}\/\d{2}/.test(financialYear + '')) {
      throw new Error(`Invalid parameter. Must be a year format YYYY/YY: ${financialYear}`)
    }
    const years = (financialYear + '').split('/');
    let year = years[0];

    if (secondYear) {
      year = year.substring(0, 2) + years[1];
    }
    return year;
  }
}

export default DateUtil;
