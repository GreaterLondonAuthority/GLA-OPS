/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function TransferModal($uibModal, TransitionService, ProjectService, OrganisationService, ToastrUtil, $timeout) {
  return {
    show: function (project) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/overview/transferModal/transferModal.html',
        size: 'md',
        resolve: {},
        controller: [function () {
          this.project = project;

          this.isTransferAllowed = (this.project.allowedActions || []).indexOf('Transfer') !== -1;

          this.onTransfer = () => {
            ProjectService.transferProject(project.id, this.orgCode).then(()=>{
              this.transferred = true;
              ToastrUtil.success('Project transferred');
              $timeout(()=>{
                $('#toast-container').css('z-index', '9999');
              });
            });
          };
        }]
      });
    }
  };
}

TransferModal.$inject = ['$uibModal', 'TransitionService', 'ProjectService', 'OrganisationService', 'ToastrUtil', '$timeout'];

angular.module('GLA')
  .service('TransferModal', TransferModal);
