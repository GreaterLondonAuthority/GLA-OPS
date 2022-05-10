import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DateUtilsService {

  constructor() { }

  /**
   * Formats a full year integer to string 'YYYY/YY'
   * @param {Number} year
   * @return {String} 'YYYY/YY'
   */
  static toFinancialYearString(year: number) {
    let nextYear = (year + 1) % 100;
    return `${year}/${nextYear}`;
  }

}
