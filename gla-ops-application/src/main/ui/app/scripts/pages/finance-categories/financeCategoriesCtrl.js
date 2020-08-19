/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class FinanceCategoriesCtrl {
  constructor($state, $stateParams, FinanceCategoriesUpdateModal, FinanceService, ToastrUtil) {
    this.FinanceService = FinanceService;
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.ToastrUtil = ToastrUtil;
    this.FinanceCategoriesUpdateModal = FinanceCategoriesUpdateModal;
  }

  $onInit() {
    this.textMapping = {
      ReadWrite: 'Allow new forecasts & show in OPS',
      Hidden: 'No new forecasts & hide in OPS',
      ReadOnly: 'No new forecasts & show in OPS'
    };
    this.financeCategories = this.processCategories(this.financeCategories);
  }

  refresh() {
  }

  processCategories(categories) {
      let data = categories;
      _.map(data, category => {
        let sortedCeCodes = _.sortBy(category.ceCodes, 'id');

        category.shortCodes = _.map(_.slice(sortedCeCodes, 0, 6),'id').join(', ');
        if(sortedCeCodes && sortedCeCodes.length > 6){
          category.longCodes = _.map(sortedCeCodes,'id').join(', ');
        }
      });
      return _.sortBy(data, [category => category.text.toLowerCase()]);
  }
  editRow(category){
    let modal = this.FinanceCategoriesUpdateModal.show(category);
    modal.result.then((data) => {
      this.ToastrUtil.success('Row updated');
      this.$state.go(this.$state.current, this.$stateParams, {reload: true});
    });
  }
  addRow(){
    let modal = this.FinanceCategoriesUpdateModal.show({});
    modal.result.then((data) => {
      this.ToastrUtil.success('New row added');
      this.$state.go(this.$state.current, this.$stateParams, {reload: true});
    });
  }
}

FinanceCategoriesCtrl.$inject = ['$state', '$stateParams', 'FinanceCategoriesUpdateModal', 'FinanceService', 'ToastrUtil'];


angular.module('GLA')
  .component('financeCategoriesPage', {
    templateUrl: 'scripts/pages/finance-categories/financeCategories.html',
    bindings: {
      financeCategories : '<'
    },
    controller: FinanceCategoriesCtrl
  });
