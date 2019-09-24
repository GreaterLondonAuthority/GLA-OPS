/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function ProjectSubcontractorModal($uibModal) {
  return {
    show: function (subcontractor, showUKPRN) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/subcontractors/projectSubcontractorsModal.html',
        size: 'md',
        resolve: {

        },
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.subcontractor = angular.copy(subcontractor || {});
          this.showUKPRN = showUKPRN;
        }]
      });
    },

  };
}

ProjectSubcontractorModal.$inject = ['$uibModal'];

angular.module('GLA')
.service('ProjectSubcontractorModal', ProjectSubcontractorModal);

