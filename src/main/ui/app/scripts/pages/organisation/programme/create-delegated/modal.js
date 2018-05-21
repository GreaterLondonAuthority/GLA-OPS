/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function CreateDelegatedModal($uibModal, moment) {
  return {
    show: function (delegate, grantTypes, totals) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/organisation/programme/create-delegated/modal.html',
        size: 'md',
        resolve: {

        },
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.dataBlock = delegate || {};
          this.maxDate = new Date();
          this.grantTypes = grantTypes;
          this.totals = totals;


          this.isAmountValid = function() {
            let totalKey = (this.dataBlock.strategic ? 'strategic' : 'nonStrategic')+this.dataBlock.grantType+'Total';
            return parseInt(this.dataBlock.amount) + this.totals[totalKey] >= 0;
          };

          this.isApprovedOnValid = function() {
            return this.dataBlock.approvedOn.isAfter(moment(new Date()));
          };

          this.onGrantTypeSelect = function(grantTypeObj){
            this.dataBlock.grantType = grantTypeObj.grantType;
            this.dataBlock.strategic = grantTypeObj.strategic;
            this.dataBlock.type = grantTypeObj.type;
          };

          if (this.grantTypes.length == 1) {
            this.grantType = this.grantTypes[0];
            this.onGrantTypeSelect(this.grantType);
          }
        }]
      });
    }
  };
}

CreateDelegatedModal.$inject = ['$uibModal', 'moment'];

angular.module('GLA')
  .service('CreateDelegatedModal', CreateDelegatedModal);
