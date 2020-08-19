/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function ContractTypeChangeModal($uibModal, $rootScope, FileUploadErrorModal) {
  return {
    show: function (lot) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/funding-claims/contract-type-change-modal/contractTypeChangeModal.html',
        size: 'md',

        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.title = `Are you sure you want to remove ${lot.name}?`;
        }]
      });
    }
  };
}

ContractTypeChangeModal.$inject = ['$uibModal', '$rootScope', 'FileUploadErrorModal'];

angular.module('GLA')
  .service('ContractTypeChangeModal', ContractTypeChangeModal);
