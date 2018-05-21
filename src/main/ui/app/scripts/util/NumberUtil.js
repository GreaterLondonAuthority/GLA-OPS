/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

/**
 * Util Class for number manipulations
 */
class NumberUtil {
  /**
   * Formats a integer into a formatted number string with commas
   * @param {Number} value - integer
   * @returns {String} i.e. 100000 to `100,000`
   */
  static formatWithCommas(value, precision) {
    let rounded = Math.round(value);
    let decimals;
    let negative = value < 0;
    if (precision) {
      let temp = parseFloat(Math.round(value * Math.pow(10, precision)) / Math.pow(10, precision)).toFixed(precision).split('.');
      rounded = temp[0];
      decimals = temp[1];
    }
    if(negative) {
      rounded *= -1;
    }
    return (negative ? '-':'') + rounded.toString().split(/(?=(?:\d{3})+(?:\.|$))/g).join(',') + (precision ? ('.'+ decimals ) :'');
  }

  /**
   * Formats an integer value into a number string with commas and `CR` appended if credit
   * @see `NumberUtil.formatWithCommas()`
   * @param {Number} value - integer
   * @returns {String} i.e. 100000 to `100,000 CR`
   */
  static formatWithCommasAndCR(value, precision) {
    let str = null;
    if(value) {
      str = this.formatWithCommas(value < 0 ? Math.abs(value) : value, precision);
      str = `${str}${value > 0 ? ' CR' : ''}`;
    }
    return str;
  }

  /**
   * Format number to string with pound sign, comma's and CR
   * @see `StringUtil.formatWithCommasAndCR`
   */
  static formatWithPoundAndCR(value, precision) {
    return `£${value ? this.formatWithCommasAndCR(value, precision) : 0}`;
  }
}

export default NumberUtil;
