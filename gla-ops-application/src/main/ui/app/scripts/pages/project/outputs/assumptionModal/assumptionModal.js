/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function AssumptionModal($uibModal) {
  return {
    show: function (assumption) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/outputs/assumptionModal/assumptionModal.html',
        size: 'md',
        resolve: {},
        controller: [function () {
          this.assumption = angular.copy(assumption);
        }]
      });
    }
  };
}

AssumptionModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('AssumptionModal', AssumptionModal);
