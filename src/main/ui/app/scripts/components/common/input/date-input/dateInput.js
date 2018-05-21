/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


'use strict';

DateInputCtrl.$inject = ['$scope', '$element', '$attrs', 'moment', '$parse', '$timeout'];

function DateInputCtrl($scope, $element, $attrs, moment, $parse, $timeout) {
  var ctrl = this;
  this.isFocused = angular.isDefined(this.isFocused) ? this.isFocused : false;

  ctrl.focusDay = ctrl.isFocused;

  if (ctrl.defaultDate) {
    let today = moment(new Date(), 'YYYY-MM-DD', true);
    ctrl.model = today;
    ctrl.day = today.format('DD');
    ctrl.month = today.format('MM');
    ctrl.year = today.format('YYYY');
  }

  // pre-populate
  this.ngModelwatch = $scope.$watch('$ctrl.model', function(val) {
    var date = moment(val, 'YYYY-MM-DD', true);

    if(val && date.isValid()) {
      ctrl.day = date.format('DD');
      ctrl.month = date.format('MM');
      ctrl.year = date.format('YYYY');
      this.ngModelwatch();
    }
  }.bind(this));


  $scope.$watch('$ctrl.clear', function(newValue){
    if(newValue) {
      ctrl.day = null;
      ctrl.month = null;
      ctrl.year = null;
    }
    this.clear = false;
  }.bind(this));


  ctrl.onInputFocus = function() {
    ctrl.isFocused = true;
  };

  ctrl.rawDateValue = function(year, month, day){
    let rawValue = '';
    if(year || month || day) {
      rawValue = [year, month, day].map(item => item || '').join('-')
    }
    return rawValue;
  };

  ctrl.onInputBlur = function() {
    ctrl.isFocused = false;
    //$timeout to validate that it was not focused back in one of the other date-input fields
    $timeout(function() {
      if(!ctrl.isFocused) {
        let rawValue = ctrl.rawDateValue(ctrl.year, ctrl.month, ctrl.day);
        //TODO set error on ngModel
        ctrl.hasErrors = rawValue && !ctrl.getDateModel(ctrl.year, ctrl.month, ctrl.day);

        if (ctrl.hasErrors) {
          angular.element($element[0]).addClass('invalid');
        } else {
          angular.element($element[0]).removeClass('invalid');
        }

        if(ctrl.onBlur) {
          ctrl.onBlur({$event: rawValue})
        }
      }
    });

  };

  /**
   * Move to next/prev input box
   * @param id
   */
  function changeFocus(id) {
    ctrl.focusDay = id === 'day';
    ctrl.focusMonth = id === 'month';
    ctrl.focusYear = id === 'year';
  }

  /**
   * Validate full date
   */
  function updateModel() {
    ctrl.model = ctrl.getDateModel(ctrl.year, ctrl.month, ctrl.day);
  }

  /**
   * Returns date model (string YYYY-MM-DD) if it passes validation. Null otherwise
   * @param <string> year
   * @param <string> month
   * @param <string> day
   * @returns {*}
   */
  ctrl.getDateModel = function(year, month, day){
    const date = moment(`${year}-${month}-${day}`, 'YYYY-MM-DD', true);
    const minDate = moment('1900-01-01');
    const maxDate = ctrl.maxDate ? moment(ctrl.maxDate) : moment('3000-01-01');
    if(date.isValid() && !date.isBefore(minDate) && !date.isAfter(maxDate)){
      return date.format('YYYY-MM-DD');
    };
    return null;
  };

  ctrl.onDayChange = function() {
    //Workaround to validate 'DD' format. It validates by current date's month by default which is not correct
    var day = moment('2016-01-' + ctrl.day, 'YYYY-MM-DD', true);

    if(day.isValid()) {
      changeFocus('month');
    }
    updateModel();
  };

  ctrl.onMonthChange = function() {
    var month = moment(ctrl.month, 'MM', true);
    if(month.isValid()) {
      changeFocus('year');
    }
    updateModel();
  };

  ctrl.onYearChange = function() {
    updateModel();
  };
}

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
