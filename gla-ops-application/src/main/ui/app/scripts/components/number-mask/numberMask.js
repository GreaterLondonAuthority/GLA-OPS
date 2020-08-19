/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

const validators = {
  maxNumber: function (ctrl, value, limit) {
    var max = parseFloat(limit, 10);
    return ctrl.$isEmpty(value) || isNaN(max) || value <= max;
  },
  minNumber: function (ctrl, value, limit) {
    var min = parseFloat(limit, 10);
    return ctrl.$isEmpty(value) || isNaN(min) || value >= min;
  }
};

function NumberMask($parse, numberFilter, $timeout) {
  return {
    link(scope, element, attr, ngModel) {
      let allowNegativeNumbers = 'negativeNumber' in attr;
      let model = $parse(attr.ngModel)(scope);
      function decimals(){
        return $parse(attr.numberMask)(scope) || 0;
      }
      let lastViewValue = addCommaSeparators(model + '', decimals());
      let lastKey = null;
      let lastSelectionStart = null;

      //Allow only tabs digits and dot
      element.on('keypress', function (e) {
        let key = e.keyCode || e.which;
        let char = String.fromCharCode(e.which);
        let isTab = (key == 9);
        let isDigit = /^[\d]$/.test(char);
        let isDot = (char == '.');
        let isFirstDot = (isDot && lastViewValue.indexOf('.') == -1);
        let isMinus = (char == '-');
        let isFirstMinus = (isMinus && lastViewValue.indexOf('-') == -1);

        let validChar = isDigit || isTab || (decimals() && isFirstDot) || allowNegativeNumbers && isFirstMinus;
        if (!validChar) {
          e.preventDefault();
        }

        lastKey = char;
      });

      element.on('keydown', function (e) {
        lastSelectionStart = e.target.selectionStart;
      });

      element.on('click', function (e) {
        lastSelectionStart = e.target.selectionStart;
      });

      element.on('blur', function (e) {
        if(ngModel.$modelValue != null) {
          ngModel.$setViewValue(numberFilter(+ngModel.$modelValue, decimals()));
          ngModel.$render();
        }else if (ngModel.$valid){
          lastViewValue = '';
          ngModel.$setViewValue(null);
          ngModel.$render();
        }
      });

      element.on('focus', function (e) {
        lastViewValue = ngModel.$viewValue;
      });

      ngModel.$parsers.push(function(viewValue) {

        adjustCursorPosition(lastViewValue, viewValue);

        if(viewValue) {
          let typedInDecimals = viewValue.indexOf('.') == -1 ? 0 : viewValue.split('.')[1].length;

          //If value is valid
          if (typedInDecimals <= decimals() && !(viewValue.indexOf('-') > 0)) {
            lastViewValue = viewValue;
          }
          if (lastViewValue) {
              ngModel.$setViewValue(addCommaSeparators(lastViewValue));
              ngModel.$render();
          }
          return (lastViewValue && lastViewValue != '.' && lastViewValue != '-') ? +((lastViewValue + '').replace(/,/g, '')) : null;
        }
        lastViewValue = '';
        return null;
      });


      ngModel.$formatters.unshift(function (value) {
        return addCommaSeparators(value);
      });

      function adjustCursorPosition(lastViewValue, viewValue) {
        if(lastViewValue == null || viewValue == null){
          return;
        }

        if(Math.abs(lastViewValue.length - viewValue.length) > 1){
          lastSelectionStart = viewValue.length;
          $timeout(()=>{
            setCaretToPos(element[0], viewValue.length);
          }, 0);
        }else if(lastViewValue.length < viewValue.length) {
          lastSelectionStart += 1;
          $timeout(()=>{
            setCaretToPos(element[0], lastSelectionStart);
          }, 0);
        } else if(lastViewValue.length > viewValue.length) {
          lastSelectionStart -= 1;
          $timeout(()=>{
            setCaretToPos(element[0], lastSelectionStart > -1? lastSelectionStart : 0);
          }, 0);
        }
      }

      function addCommaSeparators(viewValue){
        if(viewValue) {
          if(viewValue == '-'){
            return viewValue;
          }
          let isNegative = viewValue.indexOf('-') == 0;
          let numberWithoutCommas = viewValue.replace(/,/g, '').replace('-', '');
          let intAndDecimal = numberWithoutCommas.split('.');
          let formattedValue = numberFilter(intAndDecimal[0]);
          if(isNegative){
            formattedValue = `-${formattedValue}`;
          }
          if (intAndDecimal.length == 2 && decimals()) {
            formattedValue += `.${intAndDecimal[1].substring(0, decimals())}`
          }
          return formattedValue;
        }
        return '';
      }


      if (attr.min) {
        var minVal;

        ngModel.$validators.min = function(modelValue) {
          return validators.minNumber(ngModel, modelValue, minVal);
        };

        scope.$watch(attr.min, function(value) {
          minVal = value;
          ngModel.$validate();
        });
      }

      if (attr.max) {
        var maxVal;

        ngModel.$validators.max = function(modelValue) {
          return validators.maxNumber(ngModel, modelValue, maxVal);
        };

        scope.$watch(attr.max, function(value) {
          maxVal = value;
          ngModel.$validate();
        });
      }
    },
    require: '?ngModel'
  };
}

function setSelectionRange(input, selectionStart, selectionEnd) {
  if (input.setSelectionRange) {
    input.focus();
    input.setSelectionRange(selectionStart, selectionEnd);
  }
  else if (input.createTextRange) {
    var range = input.createTextRange();
    range.collapse(true);
    range.moveEnd('character', selectionEnd);
    range.moveStart('character', selectionStart);
    range.select();
  }
}

function setCaretToPos (input, pos) {
  setSelectionRange(input, pos, pos);
}

NumberMask.$inject = ['$parse', 'numberFilter', '$timeout'];

angular.module('GLA')
  .directive('numberMask', NumberMask);
