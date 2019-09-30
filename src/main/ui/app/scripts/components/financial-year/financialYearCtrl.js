/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import DateUtil from  '../../util/DateUtil'

class FinancialYearCtrl {
  constructor($scope, $timeout) {
    this.$scope = $scope;
    this.$timeout = $timeout;
  }

  $onInit() {
    // using isNumber to allow passing a value of 0
    this.back = _.isNumber(this.back) ? this.back : 20;
    this.forward = _.isNumber(this.forward) ? this.forward : 20;

    // SHOULD component be making assumptions????
    this.currentFinancialYear = this.currentFinancialYearConst || DateUtil.getFinancialYear2(moment());

    if(_.isNumber(this.from)){
      this.back = this.currentFinancialYear - this.from;
    } else {
      this.$scope.$watch('$ctrl.back', value => {
        if(value) {
          this.generateYearList();
        }
      });
    }

    if(_.isNumber(this.to)){
      this.forward = this.to - this.currentFinancialYear;
    } else {
      this.$scope.$watch('$ctrl.forward', value => {
        if(value) {
          this.generateYearList();
        }
      });
    }


    this.generateYearList();

  }

  generateYearList () {
    this.yearList = DateUtil.generateDatesObjects(this.currentFinancialYear, this.back, this.forward);
    this.selectedYear = _.find(this.yearList, {financialYear: (this.selectedYear && this.selectedYear.financialYear) || this.currentFinancialYear});
  }

  }

FinancialYearCtrl.$inject = ['$scope', '$timeout'];

export default FinancialYearCtrl;
