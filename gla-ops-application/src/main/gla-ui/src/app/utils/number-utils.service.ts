import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class NumberUtilsService {

  constructor() { }

  /**
   * TODO why can't we just use angular number filter?
   * Formats a integer into a formatted number string with commas
   * @param {number} value - integer
   * @param {number} precision - integer
   * @returns {String} i.e. 100000 to `100,000`
   */
   static formatWithCommas(value: number, precision: number) {
    let rounded = Math.round(value);
    let decimals;
    let negative = value < 0;
    if (precision) {
      let temp = (Math.round(value * Math.pow(10, precision)) / Math.pow(10, precision)).toFixed(precision).split('.');
      rounded = parseInt(temp[0]);
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
   static formatWithCommasAndCR(value: number, precision: number) {
    let str = null;
    if(value) {
      str = this.formatWithCommas(value < 0 ? Math.abs(value) : value, precision);
      str = `${str}${value > 0 ? ' CR' : ''}`;
    }
    return str;
  }
}
