/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

TransitionService.$inject = ['UserService'];

function TransitionService(UserService) {

  return {

    // This config holds all the buttons that can be shown on the overview page
    // they are split as follow:
    // - level 1: fromStatus (current state of the project)
    //    - level 2: toStatus (state the project can transit to)
    //        - level 3: button config for that transition OR
    //        in some cases (for example: from Assess to Assess) we need to show multiple buttons
    //        so we have an array of configs
    //
    // Button configs:
    //  - text: string displayed in the button
    //  - callback: function used as a callback on this controller
    //  - disableState: variable against this controller to control disabling
    //  - disabledStateFunction: function called to control disabling (not used)
    //  - order: attribute used to sort out buttons display order.

    getTransitions(isSubmitToApproveDisabled) {
      let transitions = {
        Draft: {
          Submitted: {
            text: 'SUBMIT PROJECT',
            callback: 'onSubmitProject',
            disableState: 'disableDrafSubmit',
            // disableStateFunction: 'disableDrafSubmit'
            // },
            // // NOT IMPLEMENTED YET
            // Closed: {
            //  text: 'CLOSE',
            //  callback: 'onCloseProject'
          },

          Active: {
            text: 'SAVE PROJECT TO ACTIVE',
            callback: 'onSaveProjectToActive'
          }
        },
        Submitted: {
          Draft: {
            text: 'WITHDRAW',
            callback: 'onWithdrawProject'
            // },
            // NOT IMPLEMENTED YET
            // {
            //   text: 'ASSESS',
            //   callback: 'onAssessProject'
            //
          }
        },
        Assess: {
          Active: {
            text: 'APPROVE PROJECT',
            callback: 'onApprove',
            order: 0
          },
          'Closed_Rejected': {
            text: 'REJECT PROJECT',
            callback: 'onReject',
            order: 2
          },
          Returned: {
            text: 'RETURN TO ORGANISATION',
            callback: 'onReturnProject',
            order: 1
          },
          'Assess_Recommended': {
            multiple: true,
            buttons: [
              {
                text: 'RECOMMEND FOR APPROVAL',
                callback: 'onRecommendForApproval',
                order: 0
              },
              {
                text: 'RECOMMEND FOR REJECTION',
                callback: 'onRecommendForReject',
                order: 2,
                // THIS IS AN EXCEPTION!!!! data should come from transition
                // as we don't distinguish sub status transition, we need to manually add it here
                commentsRequired: true
              }
            ]
          }
        },
        'Assess_Recommended': {
          Active: {
            text: 'APPROVE PROJECT',
            callback: 'onApprove',
            order: 0
          },
          'Closed_Rejected': {
            text: 'REJECT PROJECT',
            callback: 'onReject',
            order: 2
          },
          Returned: {
            text: 'RETURN TO ORGANISATION',
            callback: 'onReturnProject',
            order: 1
          }
        },
        Returned: {
          Assess: {
            text: 'SUBMIT PROJECT',
            callback: 'onSubmitReturnedProject',
            disableState: 'disableReturnedSubmit',
            order: 0
          },
          'Closed_Rejected': {
            text: 'REJECT PROJECT',
            callback: 'onReject',
            order: 1
          }
        },
        Active: {
          // 'Active': {
          //   text: 'REQUEST APPROVAL',
          //   callback: 'onRequestApproval',
          //   acceptIncomplete: true,
          //   order: 1
          // },
          // NOT IMPLEMENTED YET
          // Closed: {
          //   text: 'REJECT PROJECT',
          //   callback: 'onReject'
          // }
        },
        'Active_UnapprovedChanges': {
          // NO CTA TRANSITION
          // 'Active': {
          //   text: 'RP DELETES ALL CHANGES',
          // },
          'Active_ApprovalRequested': {
            text: 'REQUEST APPROVAL',
            callback: 'onRequestApproval',
            acceptIncomplete: true,
            order: 1
          },
        },
        'Active_ApprovalRequested': {
          'Active': {
            text: 'APPROVE CHANGES',
            callback: 'onApproveFromApprovalRequested',
            order: 0
          },

          'Active_UnapprovedChanges': {
            text: 'RETURN TO ORGANISATION',
            callback: 'onReturnFromApprovalRequested',
            order: 1
          },

          'Active_PaymentAuthorisationPending': {
            text: 'REQUEST PAYMENT AUTHORISATION',
            callback: 'onRequestPaymentAuthorisation',
            order: 2
          }
        },
        'Active_AbandonPending': {
          'Active': {
            text: 'APPROVE ABANDON',
            callback: 'onApproveAbandon',
            order: 0
          },

          'Closed_Abandoned': {
            text: 'REJECT ABANDON',
            callback: 'onRejectAbandon',
            order: 1
          }
        }
      };

      if (isSubmitToApproveDisabled) {
        delete transitions.Draft.Active
      }

      return transitions;
    },

    getTransitionId(status, subStatus) {
      if (!status) {
        throw Error('Missing required parameter');
      }

      if (subStatus) {
        return `${status}_${subStatus}`
      }

      return status;
    },

    getAllowedTransitions(status, subStatus, isSubmitToApproveDisabled) {
      if (!status) {
        throw Error('Missing required parameter')
      }
      let transitionId = this.getTransitionId(status, subStatus);
      return this.getTransitions(isSubmitToApproveDisabled)[transitionId];
    },

    /**
     * loop for all transitions the project can go to and look for button configs
     * that match (note, transitions are mapped based on final solution, but
     * individual transitions may not have been developed yet)
     * @param project
     * @param isSubmitToApproveDisabled
     */
    getTransitionButtons(project, isSubmitToApproveDisabled) {
      let buttons = [];
      const allowedTransitionsInCurrentState = this.getAllowedTransitions(project.status, project.subStatus, isSubmitToApproveDisabled);
      if (allowedTransitionsInCurrentState) {
        _.forEach(project.allowedTransitions, (transition) => {
          const toState = this.getTransitionId(transition.status, transition.subStatus);
          let commentsRequired = transition.commentsRequired;
          let btnConfig = allowedTransitionsInCurrentState[toState];
          if (btnConfig && !btnConfig.menuItem) {
            if (btnConfig.multiple) {
              _.forEach(btnConfig.buttons, (button) => {
                button.commentsRequired = commentsRequired || button.commentsRequired;
                buttons.push(button);
              });
            } else {
              btnConfig.commentsRequired = commentsRequired || btnConfig.commentsRequired;
              buttons.push(btnConfig);
            }
          }
        });
      }
      return buttons;
    },


    getMenuItems(project) {
      let menuItems = [];

      if (this.isAbandonMenuItemVisible(project)) {
        menuItems.push({
          menuItem: true,
          type: 'action',
          action: 'abandon-project',
          icon: 'glyphicon-remove',
          displayText: 'Abandon Project'
        });
      }

      if (this.isCompleteMenuItemVisible(project)) {
        menuItems.push({
          menuItem: true,
          type: 'action',
          action: 'complete-project',
          icon: 'glyphicon-ok-sign',
          displayText: 'Complete Project'
        })
      }

      if (UserService.hasPermission('proj.transfer')) {
        menuItems.push({
          menuItem: true,
          type: 'action',
          action: 'transfer-project',
          icon: 'glyphicon-transfer',
          displayText: 'Transfer Project'
        });
      }

      if (this.canReinstate(project)) {
        menuItems.push({
          menuItem: true,
          type: 'action',
          action: 'reinstate-project',
          icon: 'glyphicon-arrow-up',
          displayText: 'Reinstate project'
        })
      }


      return menuItems;
    },

    isAbandonMenuItemVisible(project) {
      let status = this.status(project);

      let isAbandonPending = project.status === 'Active' && project.subStatus === 'AbandonPending';
      let canBeAbandoned = this.isTransitionAllowed(project.allowedTransitions, 'Closed', 'Abandoned');
      let canBeRequestAbandoned = this.isTransitionAllowed(project.allowedTransitions, 'Active', 'AbandonPending');

      //PM and SPM don't have this permission, but have project.allowedTransitions allowing them to close autoApproval projects
      let hasAbandonPermission = UserService.hasPermission('proj.abandon');

      let isMenuAlwaysHidden = isAbandonPending || status.closed;

      return !isMenuAlwaysHidden && (canBeAbandoned || canBeRequestAbandoned || hasAbandonPermission)
    },

    isCompleteMenuItemVisible(project) {
      let hasPermission = UserService.hasPermission('proj.complete');
      return hasPermission && project.status === 'Active'
    },



    /**
     * Checks if project can transition to state: status, subStatus
     * @param allowedTransitions Array of allowed transitions (project.allowedTransitions)
     * @param toStatus
     * @param toSubStatus
     * @returns {boolean}
     */
    isTransitionAllowed(allowedTransitions, toStatus, toSubStatus) {
      if (!toStatus) {
        throw Error('Missing required parameter');
      }
      return !!this.findTransition(allowedTransitions, toStatus, toSubStatus);
    },


    findTransition(allowedTransitions, toStatus, toSubStatus) {
      if (!toStatus) {
        throw Error('Missing required parameter');
      }
      return _.find(allowedTransitions || [], {status: toStatus, subStatus: toSubStatus || null});
    },


    /**
     * Returns project status object with boolean properties
     * @param project
     * @returns {{draft: true, active: false, ...}}
     */
    status(project) {
      let allStatuses = ['draft', 'submitted', 'assess', 'active', 'returned', 'closed'];
      let result = {};
      allStatuses.forEach(status => result[status] = (project.status.toLowerCase() === status));
      return result;
    },

    getTransitionToClose(project) {
      let transitionsToClose = [
        {status: 'Closed', subStatus: 'Abandoned'},
        {status: 'Active', subStatus: 'AbandonPending'},
      ];

      for (let i = 0; i < transitionsToClose.length; i++) {
        let transition = this.findTransition(project.allowedTransitions, transitionsToClose[i].status, transitionsToClose[i].subStatus);
        if (transition) {
          return transition;
        }
      }
      return null;
    },

    canReinstate(project){
      return project.status === 'Closed';
    }
  };
}

angular.module('GLA')
  .service('TransitionService', TransitionService);
