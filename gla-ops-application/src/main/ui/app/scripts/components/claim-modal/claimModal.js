/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const claimModalDefaults = {
  claimableAmountTitle: 'Payment amount',
  claimBtnText: 'CLAIM',
  cancelBtnText: 'CANCEL CLAIM'
};

function ClaimModal($uibModal, ProjectService, ErrorService) {
  return {
    show: function (config, claimRequest) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/components/claim-modal/claimModal.html',
        size: 'md',
        controller: [function () {
          _.assign(this, _.merge(angular.copy(claimModalDefaults), config));

          this.onClaim = () => {
            return ProjectService.claim(claimRequest.projectId, claimRequest.blockId, claimRequest).then(()=>{
              return this.$close('claim')
            }).catch(ErrorService.apiValidationHandler(()=> this.$dismiss('cancel')));
          };

          this.onCancelClaim = () => {
            return ProjectService.cancelClaim(claimRequest.projectId, claimRequest.blockId, claimRequest.id).then(()=>{
              return this.$close('cancel');
            }).catch(ErrorService.apiValidationHandler(()=> this.$dismiss('cancel')));
          }
        }]
      });
    }
  };
}

ClaimModal.$inject = ['$uibModal', 'ProjectService', 'ErrorService'];

angular.module('GLA')
  .service('ClaimModal', ClaimModal);
