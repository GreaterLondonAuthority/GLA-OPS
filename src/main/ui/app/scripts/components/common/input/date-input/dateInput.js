/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class DateInputCtrl {
  constructor($scope, $element, moment, $timeout) {
    this.$scope = $scope;
    this.$element = $element;
    this.moment = moment;
    this.$timeout = $timeout;
  }

  $onInit(){
    this.isFocused = angular.isDefined(this.isFocused) ? this.isFocused : false;

    this.focusDay = this.isFocused;

    if (this.defaultDate && !this.model) {
      let today = this.moment(new Date(), 'YYYY-MM-DD', true);
      this.model = today;
      this.day = today.format('DD');
      this.month = today.format('MM');
      this.year = today.format('YYYY');
    }

    // pre-populate
    this.ngModelwatch = this.$scope.$watch('$ctrl.model', (val) => {
      var date = this.moment(val, 'YYYY-MM-DD', true);

      if (val && date.isValid()) {
        this.day = date.format('DD');
        this.month = date.format('MM');
        this.year = date.format('YYYY');
        this.ngModelwatch();
      }
    });


    this.$scope.$watch('$ctrl.clear', (newValue) => {
      if (newValue) {
        this.day = null;
        this.month = null;
        this.year = null;
      }
      this.clear = false;
    });
  }

  onInputFocus() {
    this.isFocused = true;
  };

  rawDateValue(year, month, day) {
    let rawValue = '';
    if (year || month || day) {
      rawValue = [year, month, day].map(item => item || '').join('-')
    }
    return rawValue;
  };

  onInputBlur () {
    this.isFocused = false;
    //$timeout to validate that it was not focused back in one of the other date-input fields
    this.$timeout(() => {
      if (!this.isFocused) {
        let rawValue = this.rawDateValue(this.year, this.month, this.day);
        //TODO set error on ngModel
        this.hasErrors = rawValue && !this.getDateModel(this.year, this.month, this.day);

        if (this.hasErrors) {
          angular.element(this.$element[0]).addClass('invalid');
        } else {
          angular.element(this.$element[0]).removeClass('invalid');
        }

        if (this.onBlur) {
          this.onBlur({$event: rawValue})
        }
      }
    });

  };

  /**
   * Returns date model (string YYYY-MM-DD) if it passes validation. Null otherwise
   * @param <string> year
   * @param <string> month
   * @param <string> day
   * @returns {*}
   */
  getDateModel(year, month, day) {
    const date = this.moment(`${year}-${month}-${day}`, 'YYYY-MM-DD', true);
    const minDate = this.moment('1900-01-01');
    const maxDate = this.maxDate ? this.moment(this.maxDate) : this.moment('3000-01-01');
    if (date.isValid() && !date.isBefore(minDate) && !date.isAfter(maxDate)) {
      return date.format('YYYY-MM-DD');
    }
    return null;
  };

  onDayChange() {
    //Workaround to validate 'DD' format. It validates by current date's month by default which is not correct
    var day = this.moment('2016-01-' + this.day, 'YYYY-MM-DD', true);

    if (day.isValid()) {
      this.changeFocus('month');
    }
    this.updateModel();
  };

  onMonthChange() {
    var month = this.moment(this.month, 'MM', true);
    if (month.isValid()) {
      this.changeFocus('year');
    }
    this.updateModel();
  };

  onYearChange() {
    this.updateModel();
  };

  /**
   * Move to next/prev input box
   * @param id
   */
  changeFocus(id) {
    this.focusDay = id === 'day';
    this.focusMonth = id === 'month';
    this.focusYear = id === 'year';
  }

  /**
   * Validate full date
   */
  updateModel() {
    this.model = this.getDateModel(this.year, this.month, this.day);
  }
}

DateInputCtrl.$inject = ['$scope', '$element', 'moment', '$timeout'];

angular.module('GLA')
  .component('dateInput', {
    bindings: {
      name: '@',
      model: '=ngModel',
      isFocused: '=?',
      onBlur: '&?',
      hasErrors: '=?',
      disabled: '=ngDisabled',
      dateFormat: '=?',
      defaultDate: '<',
      maxDate: '<',
      clear: '<?',
    },
    templateUrl: 'scripts/components/common/input/date-input/dateInput.html',
    controller: DateInputCtrl
  });
