/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


import modals from './modals';
// TODO rename to transition modal?
function AbandonModal($uibModal, TransitionService, ProjectService) {
  return {
    show: function (project, transition, errorMsg, isRejecting) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/project/overview/abandonModal/modal.html',
        size: 'md',
        controller: ['hintMessage', function (hintMessage) {
          console.log('----hintMessage----', hintMessage)
          this.project = project;
          this.status = TransitionService.status(project);

          this.transition = transition || TransitionService.getTransitionToClose(project);
          this.dataBlock = {};


          /**
           * Sets correct modal view based on allowed transitions.
           */
          this.updateView = (hintMessage) => {
            let transition = this.transition || {};

            this.showRejectBtn = !hintMessage && transition.subStatus === 'Rejected';
            this.showAbandonBtn = !hintMessage && transition.subStatus === 'Abandoned';
            this.showRequestAbandonBtn = !hintMessage && transition.subStatus === 'AbandonPending';

            this.dataBlock.requestAbandon = this.showRequestAbandonBtn;

            this.reinstateProject = this.project.statusType === 'Closed';
            this.completeProject = transition && transition.subStatus === 'Completed';
            let modalsConfig = modals.config();
            if(this.completeProject){
              this.modal = modalsConfig.complete;
              if (hintMessage) {
                this.modal.actionBtnName = null;
                this.modal.hintMessage = hintMessage;
              }
            }else if (this.reinstateProject) {
              this.modal = modalsConfig.reinstate;
              if (this.project.hasReclaimedPayments) {
                this.modal.warning = 'This project contains authorised or pending reclaim(s). Changes made to this project once reinstated may affect calculations leading to errors. It is not recommended you reinstate this project.'
              }
              if (hintMessage) {
                this.modal.actionBtnName = null;
                this.modal.hintMessage = hintMessage;
              }
            } else if (this.showAbandonBtn) {
              this.modal = modalsConfig.abandon;
            } else if (this.showRejectBtn) {
              this.modal = modalsConfig.reject;
            } else if (this.showRequestAbandonBtn) {
              this.modal = modalsConfig.requestAbandon;

            } else {
              if(isRejecting){
                this.modal = modalsConfig.warningReject;
              } else {
                this.modal = modalsConfig.warning;
              }
              if (hintMessage) {
                this.modal.hintMessage = hintMessage;
              }
            }
          };

          this.action = () => {
            //aksjdl akss
            let p;
            if (this.completeProject){
              p = ProjectService.completeProject(project.id, this.dataBlock.reason);
            }else if (this.reinstateProject){
              p = ProjectService.reinstateProject(project.id, this.dataBlock.reason);
            } else if (this.showRequestAbandonBtn) {
              p = ProjectService.requestAbandon(project.id, this.dataBlock.reason);
            } else if(this.showRejectBtn){
              p = ProjectService.reject(project.id, this.dataBlock.reason);
            } else {
              p = ProjectService.abandon(project.id, this.dataBlock.reason);
            }
            return p.then(() => this.$close(this.dataBlock))
              .catch(err => {
                console.error('error', err);
                this.updateView(err.data.description);
                console.log(err);
              })
          };


          this.updateView(hintMessage);

        }],
        resolve: {
          //Get transition and validate to get hint message.
          hintMessage: function () {
            if(errorMsg){
              return errorMsg;
            }

            let closeTransition = TransitionService.getTransitionToClose(project);
            let transitionToValidate = transition || closeTransition;
            if (transitionToValidate) {
              //Some transitions are allowed but requires extra validation
              return ProjectService.validateTransition(project.id, transitionToValidate)
                .then(() => null)
                .catch(err => {
                  console.log('err:', err);
                  return err.data.description
                });
            } else if (project.statusType === 'Submitted') {
              return 'Submitted projects have to be withdrawn before you can amend or abandon them.'
            }
          }
        }
      });
    }
  };
}

AbandonModal.$inject = ['$uibModal', 'TransitionService', 'ProjectService'];

angular.module('GLA')
  .service('AbandonModal', AbandonModal);
