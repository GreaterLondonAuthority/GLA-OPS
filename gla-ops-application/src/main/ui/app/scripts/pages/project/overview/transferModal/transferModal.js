/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


function TransferModal($uibModal, TransitionService, ProjectService, OrganisationService, ToastrUtil, $timeout, ConfirmationDialog) {
  return {
    show: function (projects) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/overview/transferModal/transferModal.html',
        size: 'md',
        resolve: {},
        controller: [function () {

          let bulkTransfer = _.isArray(projects);
          if(!bulkTransfer){
            projects = [projects];
            this.project = projects[0] || {};
            this.isTransferAllowed = (this.project.allowedActions || []).indexOf('Transfer') !== -1;
          } else {
            this.isTransferAllowed = true;
          }

          this.onTransfer = () => {
            const ids = [];
            _.map(projects, (project) => {
              ids.push(project.id);
            });
            ProjectService.transferProject(ids, this.orgCode).then((resp)=>{
              this.transferred = true;

              let transferCount = resp.data;
              if(transferCount.nbTransferred){
                this.nbTransferredMsg = transferCount.nbTransferred + ' project'+(transferCount.nbTransferred >1 ? 's ' : ' ' )+'transferred';
              } else {
                this.nbTransferredMsg = '';
              }

              if(transferCount.nbErrors){
                if(transferCount.nbTransferred > 0){

                  this.nbErrorMsg = transferCount.nbErrors +
                    ' project' +
                    (transferCount.nbErrors >1 ? 's ' : ' ' )+
                    'not transferred';
                } else {
                  this.nbErrorMsg = 'No projects transferred';
                }
              }else{
                this.nbErrorMsg = '';
              }

              if(transferCount.nbTransferred >0){
                // ToastrUtil.success('Project'+(transferCount.nbTransferred >1 ? 's' : '' )+' transferred');
                ToastrUtil.success(this.nbTransferredMsg);
              } else {
                // ToastrUtil.warning('Project'+(transferCount.nbErrors >1 ? 's' : '' )+' not transferred');
                ToastrUtil.warning(this.nbErrorMsg);
              }
              // }
              $timeout(()=>{
                $('#toast-container').css('z-index', '9999');
              });
            },(resp)=>{
              // ConfirmationDialog.warn(resp.data ? resp.data.description : null);
              this.nbErrorMsg = resp.data.description;
              this.transferred = true;
            });
          };
        }]
      });
    }
  };
}

TransferModal.$inject = ['$uibModal', 'TransitionService', 'ProjectService', 'OrganisationService', 'ToastrUtil', '$timeout', 'ConfirmationDialog'];

angular.module('GLA')
  .service('TransferModal', TransferModal);
