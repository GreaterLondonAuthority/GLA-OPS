/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import DateUtil from '../../util/DateUtil';

class MonthSelectorCtrl {

  constructor($scope) {
    var ctrl = this;
    // generate financial month list
    let yW = $scope.$watch('$ctrl.year', value => {
      if(value) {
        this.monthList = DateUtil.generateMonthList(value.financialYear.financialYear);
      }
    });
  }
}

MonthSelectorCtrl.$inject = ['$scope'];
angular.module('GLA')
  .component('monthSelector', {
  templateUrl: 'scripts/components/month-selector/monthSelector.html',
  transclude: true,
  require: {
    // ngModel: 'ngModel'
  },
  bindings: {
    year: '<',
    onMonthSelected: '&?',
    selectedMonth: '=',
    readOnly: '<?'
  },
  controller: MonthSelectorCtrl

});
