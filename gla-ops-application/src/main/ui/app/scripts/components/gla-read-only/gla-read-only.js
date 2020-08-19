/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');


function GlaReadOnly($timeout, boolFilter) {
  return {
    restrict: 'A',
    require: 'ngModel',
    link(scope, element, attrs, ngModelCtrl) {
      let tagName = element.prop('tagName').toLowerCase();
      let isDateInput = tagName === 'date-input';
      let isYesNoInput = tagName === 'yes-no-input';
      let isSelect = tagName === 'select'
      let isUiSelect = element.hasClass('ui-select-container');

      let id = attrs.id;

      attrs.$observe('glaReadOnly', function(glaReadOnly){

        let isReadOnly = scope.$eval(glaReadOnly);

        if (isReadOnly) {
          showReadOnlyMode();
        } else {
          showEditMode();
        }
      });


      function showReadOnlyMode() {
        if (isUiSelect) {
          showUiSelect();
        } if (isSelect) {
          showSelect();
        } else if (isDateInput) {
          showDateInput();
        } else if (isYesNoInput) {
          showYesNoInput();
        } else {
          showInput()
        }
      }

      function showEditMode() {
        element.siblings('.readonly-form-el').remove();
        element.attr('id', id);
        element.show();
      }

      function getReadOnlyTemplate(readOnlyValue){
        return `<div id="${id}" class="readonly-form-el read-only-text multiline-text">${readOnlyValue}</div>`;
      }

      function injectReadOnly(readOnlyValue) {
        let defaultValue = 'Not provided';
        element.after(getReadOnlyTemplate(readOnlyValue || defaultValue));
        element.removeAttr('id');
        element.hide();
      }

      function showUiSelect(){
        $timeout(() => {
          let readOnlyValue = element.find('.ui-select-match-text').text();
          injectReadOnly(readOnlyValue)
        }, 0);
      }

      function showSelect(){
        scope.$watch(() => {
          return ngModelCtrl.$modelValue;
        }, () => {
          let readOnlyValue = element.find('option:selected:not([disabled])').text();
          injectReadOnly(readOnlyValue);
        });
      }

      function showDateInput(){
        scope.$watch(() => {
          return ngModelCtrl.$modelValue;
        }, (dateStr) => {
          if (dateStr) {
            dateStr = moment(dateStr, 'YYYY-MM-DD').format('DD / MM / YYYY');
          }
          injectReadOnly(dateStr);
        });
      }

      function showYesNoInput(){
        scope.$watch(() => {
          return ngModelCtrl.$modelValue;
        }, (yesNo) => {
          let boolValue;
          if (yesNo === 'yes' || yesNo === true) {
            boolValue = true;
          } else if (yesNo === 'no' || yesNo === false) {
            boolValue = false;
          }
          injectReadOnly(boolFilter(boolValue));
        });
      }

      function showInput(){
        scope.$watch(() => {
          return ngModelCtrl.$modelValue;
        }, () => {
          let readOnlyValue = (attrs.glaReadOnlyValue ? scope.$eval(attrs.glaReadOnlyValue) : ngModelCtrl.$viewValue);
          injectReadOnly(readOnlyValue);
        });
      }
    }
  };
}

GlaReadOnly.$inject = ['$timeout', 'boolFilter'];

angular.module('GLA').directive('glaReadOnly', GlaReadOnly);
