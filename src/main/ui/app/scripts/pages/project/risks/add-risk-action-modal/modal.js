/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

function AddRiskActionModal($uibModal) {
  return {
    show: function (type, riskOrIssue) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/risks/add-risk-action-modal/modal.html',
        size: 'md',
        resolve: {

        },
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.type = type;
          this.dataBlock = riskOrIssue || {};
          if(type === 'Mitigation') {
          } else if(type === 'Action'){
          }
        }]
      });
    }
  };
}

AddRiskActionModal.$inject = ['$uibModal', 'RisksService'];

angular.module('GLA')
  .service('AddRiskActionModal', AddRiskActionModal);
