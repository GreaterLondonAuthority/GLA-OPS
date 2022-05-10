import { Component, OnInit, Input, ViewChild, ElementRef, Output, EventEmitter } from '@angular/core';
declare var moment: any;

@Component({
  selector: 'gla-date-input',
  templateUrl: './date-input.component.html',
  styleUrls: ['./date-input.component.scss']
})
export class DateInputComponent implements OnInit {
  @Input() formattedDate: string
  @Input() defaultDate: boolean
  @Input() maxDate: any
  @Input() onBlur: any
  @Input() readOnly: boolean = true
  @Input() questionId: number
  @ViewChild('inputDay') dayInputElement: ElementRef
  @ViewChild('inputMonth') monthInputElement: ElementRef
  @ViewChild('inputYear') yearInputElement: ElementRef
  @Output() formattedDateChange: EventEmitter<string> = new EventEmitter<string>();
  day: string
  month: string
  year: string
  currentFocus: string
  isFocused: boolean = false
  isValid: boolean = true

  constructor() { }

  ngOnInit(): void {
    console.log('init of date-input', this.formattedDate)
    if (this.defaultDate && !this.formattedDate) {
      let today = moment(new Date(), 'YYYY-MM-DD', true);
      this.formattedDate = today;
      this.day = today.format('DD');
      this.month = today.format('MM');
      this.year = today.format('YYYY');
    } else if (this.formattedDate) {
      let date = moment(this.formattedDate, 'YYYY-MM-DD', true)
      if (date.isValid()) {
        this.day = date.format('DD');
        this.month = date.format('MM');
        this.year = date.format('YYYY');
        this.isValid = true
      }
    }
  }

  onInputFocus(currentFocus: string) {
    this.currentFocus = currentFocus
    this.isFocused = true;
  }

  onInputBlur (previousFocus: string) {
    //timeout to validate that it was not focused back in one of the other date-input fields
    setTimeout(() => {
      //ie. user has not focussed on a new date input field
      if (previousFocus == this.currentFocus) {
        this.isFocused = false
      }

      if (this.onBlur) {
        this.onBlur({$event: this.rawDateValue(this.year, this.month, this.day)})
      }
      if (this.isValid) {
        this.formattedDate = this.rawDateValue(this.year, this.month, this.day);
        this.formattedDateChange.emit(this.formattedDate)
        console.log(this.formattedDate)
      }
    }, 0)
  }

  rawDateValue(year: string, month: string, day: string): string {
    let rawValue = '';
    if (year || month || day) {
      rawValue = [year, month, day].map(item => item || '').join('-')
    }
    console.log('raw value', rawValue)
    return rawValue;
  }

  /**
   * Returns date model (string YYYY-MM-DD) if it passes validation. Null otherwise
   * @param <string> year
   * @param <string> month
   * @param <string> day
   * @returns {*}
   */
  getDateModel(year, month, day) {
    const date = moment(`${year}-${month}-${day}`, 'YYYY-MM-DD', true);
    const minDate = moment('1900-01-01');
    const maxDate = this.maxDate ? moment(this.maxDate) : moment('3000-01-01');
    if (date.isValid() && !date.isBefore(minDate) && !date.isAfter(maxDate)) {
      this.isValid = true
      return date.format('YYYY-MM-DD');
    } else {
      this.isValid = false
    }
    return null;
  }

  onDayChange(event: any) {
    //Workaround to validate 'DD' format. It validates by current date's month by default which is not correct
    let day = moment('2016-01-' + event.target.value, 'YYYY-MM-DD', true);

    if (day.isValid()) {
      this.changeFocus('month');
    } else {
    }
    this.day = event.target.value
    this.updateModel();
  };

  onMonthChange(event: any) {
    let month = moment(event.target.value, 'MM', true);
    if (month.isValid()) {
      this.changeFocus('year');
    }
    this.month = event.target.value
    this.updateModel();
  };

  onYearChange(event: any) {
    this.year = event.target.value
    this.updateModel();
  }

  updateModel() {
    this.formattedDate = this.getDateModel(this.year, this.month, this.day);
  }
  
  changeFocus(id: string) {
    if (id === 'day') {
      this.dayInputElement.nativeElement.focus();
    }
    if (id === 'month') {
      this.monthInputElement.nativeElement.focus();
    }
    if (id === 'year') {
      this.yearInputElement.nativeElement.focus();
    }
  }

}
