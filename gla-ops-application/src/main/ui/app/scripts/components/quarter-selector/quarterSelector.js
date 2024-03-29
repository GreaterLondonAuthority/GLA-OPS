/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import DateUtil from '../../util/DateUtil';

class QuarterSelectorCtrl {

  constructor($scope) {
    var ctrl = this;
    // generate financial quarter list
    let yW = $scope.$watch('$ctrl.year', value => {
      if(value) {
        console.log('$ctrl.year', value)
        this.quarterList = DateUtil.generateQuarterList(_.isNumber(value)? value : value.financialYear.financialYear);
        console.log('this.quarterList', angular.copy(this.quarterList))
        if(this.selectedQuarter != null) {
          this.selectedMonth = this.quarterList[this.selectedQuarter - 1]
        }
      }
    });
  }
}

QuarterSelectorCtrl.$inject = ['$scope'];
angular.module('GLA')
  .component('quarterSelector', {
    templateUrl: 'scripts/components/quarter-selector/quarterSelector.html',
    transclude: true,
    require: {
      // ngModel: 'ngModel'
    },
    bindings: {
      year: '<',
      onQuarterSelected: '&?',
      selectedMonth: '=',
      selectedQuarter: '<?',
      readOnly: '<?'
    },
    controller: QuarterSelectorCtrl

  });
