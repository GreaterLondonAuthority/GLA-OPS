/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function FinanceCategoriesUpdateModal($uibModal, FinanceService) {
  return {
    show: function (category) {
      const modal = this;
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/finance-categories/updateModal/modal.html',
        size: 'md',
        resolve: {
          category: () => {
            return category;
          }
        },

        controller: ['$uibModalInstance', 'category', function ($uibModalInstance, category) {
          this.originalCategory = category;
          this.category = _.clone(category);
          this.financeCategoryId = (_.find(category.ceCodes, function(o) { return _.isNumber(o.financeCategoryId) }) || {}).financeCategoryId;
          this.ceCodesString = category.ceCodes ? _.map(category.ceCodes, code => code.id).join(', ') : null;

          this.statuses = [{
            label: 'Allow new forecasts & show in OPS',
            value: 'ReadWrite'
          },{
            label: 'No new forecasts & hide in OPS',
            value: 'Hidden'
          },{
            label: 'No new forecasts & show in OPS',
            value: 'ReadOnly'
          }];


          this.onUpdate = () => {
            let codes = _.uniq(_.split(this.ceCodesString, ','));
            let res = [];
            _.each(codes, (code)=>{
              code = _.toNumber(code);
              if(code){
                let existingCode = _.find(this.originalCategory.ceCodes, {id:code});
                res.push(existingCode || {
                  id: code,
                  financeCategoryId: this.financeCategoryId
                });
              }
            });
            this.category.ceCodes = res;
            this.errorMessage = null;
            FinanceService[this.category.id?'updateCategory':'createCategory'](this.category).then(()=>{
              $uibModalInstance.close();
            }, (resp)=>{
              this.errorMessage = resp.data.description;

            });
          }

        }]

      });
    }
  };
}

FinanceCategoriesUpdateModal.$inject = ['$uibModal', 'FinanceService'];

angular.module('GLA')
  .service('FinanceCategoriesUpdateModal', FinanceCategoriesUpdateModal);
