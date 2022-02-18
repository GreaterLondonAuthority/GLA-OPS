import { Injectable } from '@angular/core';
import {find, forEach} from "lodash-es";
import {UserService} from "../user/user.service";

@Injectable({
  providedIn: 'root'
})
export class TransitionService {

  constructor(private userService: UserService) { }

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
          disableState: 'disableDrafSubmit'
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
  }

  getTransitionId(statusType, subStatusType) {
    if (!statusType) {
      throw Error('Missing required parameter');
    }

    if (subStatusType) {
      return `${statusType}_${subStatusType}`
    }

    return statusType;
  }

  getAllowedTransitions(status, subStatus, isSubmitToApproveDisabled) {
    if (!status) {
      throw Error('Missing required parameter')
    }
    let transitionId = this.getTransitionId(status, subStatus);
    return this.getTransitions(isSubmitToApproveDisabled)[transitionId];
  }

  /**
   * loop for all transitions the project can go to and look for button configs
   * that match (note, transitions are mapped based on final solution, but
   * individual transitions may not have been developed yet)
   * @param project
   * @param isSubmitToApproveDisabled
   */
  getTransitionButtons(project, isSubmitToApproveDisabled) {
    let buttons = [];
    const allowedTransitionsInCurrentState = this.getAllowedTransitions(project.statusType, project.subStatusType, isSubmitToApproveDisabled) || {};
    let order = 0;
    forEach(project.allowedTransitions, (transition) => {
      let btnConfig;

      //New generic transitions
      if (!this.isMenuItemTransition(transition) && transition.actionName) {
        btnConfig = {
          isGenericTransition: true,
          order: order++,
          callback: 'onTransition'
        }
      }

      //Old mapped transitions
      if (!btnConfig) {
        const toState = this.getTransitionId(transition.statusType, transition.subStatusType);
        btnConfig = allowedTransitionsInCurrentState[toState];
      }

      //Combining properties of old & new.
      if (btnConfig) {
        if (btnConfig.multiple) {
          forEach(btnConfig.buttons, (button) => {
            button.transition = transition;
            button.commentsRequired = transition.commentsRequired || button.commentsRequired;
            buttons.push(button);
          });
        } else {
          btnConfig.transition = transition;
          btnConfig.commentsRequired = transition.commentsRequired || btnConfig.commentsRequired;
          btnConfig.text = transition.actionName || btnConfig.text || `${transition.status}:${transition.subStatus}`;
          buttons.push(btnConfig);
        }
      }
    });
    return buttons;
  }

  getMenuItems(project, featureToggles) {
    let menuItems = [];

    if (featureToggles.isProjectSharingEnabled && this.userService.hasPermission('proj.share')) {
      menuItems.push({
        menuItem: true,
        type: 'action',
        action: 'share-project',
        icon: 'glyphicon-share',
        displayText: 'Share Project'
      });
    }

    if (this.isAbandonMenuItemVisible(project)) {
      menuItems.push({
        menuItem: true,
        type: 'action',
        action: 'abandon-project',
        icon: 'glyphicon-remove',
        displayText: 'Abandon Project'
      });
    }

    if ((project.allowedActions || []).includes('Delete')) {
      menuItems.push({
        menuItem: true,
        type: 'action',
        action: 'delete-project',
        icon: 'glyphicon-remove',
        displayText: 'Delete Project'
      });
    }

    if (this.isSuspendPaymentsMenuItemVisible(project)) {
      menuItems.push({
        menuItem: true,
        type: 'action',
        action: 'suspend-payments',
        icon: 'glyphicon-remove',
        displayText: 'Suspend Payments'
      });
    }

    if (this.isResumePaymentsMenuItemVisible(project)) {
      menuItems.push({
        menuItem: true,
        type: 'action',
        action: 'resume-payments',
        icon: 'glyphicon-ok-sign',
        displayText: 'Resume Payments'
      });
    }

    if (this.isRejectMenuItemVisible(project)) {
      menuItems.push({
        menuItem: true,
        type: 'action',
        action: 'reject-project',
        icon: 'glyphicon-remove',
        displayText: 'Reject Project'
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

    if (this.userService.hasPermission('proj.transfer')) {
      menuItems.push({
        menuItem: true,
        type: 'action',
        action: 'transfer-project',
        icon: 'glyphicon-transfer',
        displayText: 'Transfer Project'
      });
    }

    if (featureToggles.isMarkedForCorporateEnabled && this.userService.hasPermission('corp.dash.proj.mark')) {
      if (project.markedForCorporate) {
        menuItems.push({
          menuItem: true,
          type: 'action',
          action: 'unmark-project-corporate',
          icon: 'glyphicon glyphicon-remove',
          displayText: 'Unmark from Corporate'
        });
      } else {
        menuItems.push({
          menuItem: true,
          type: 'action',
          action: 'mark-project-corporate',
          icon: 'glyphicon glyphicon-check',
          displayText: 'Corporate Reporting'
        });
      }
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

    if (featureToggles.isLabelsFeatureEnabled && this.userService.hasPermission('proj.add.label')) {
      menuItems.push({
        menuItem: true,
        type: 'action',
        action: 'add-label-to-project',
        icon: 'glyphicon-tag',
        displayText: 'Apply version label'
      });
    }

    if (featureToggles.isAllowAllFileDownloadEnabled && this.userService.hasPermission('proj.download.zip')) {
      menuItems.push({
        menuItem: true,
        type: 'action',
        action: 'download-all-project-files',
        icon: 'glyphicon-download-alt',
        displayText: 'Download files'
      });
    }
    return menuItems;
  }

  isAbandonMenuItemVisible(project) {
    let status = this.status(project);
    let isAbandonPending = project.statusType === 'Active' && project.subStatusType === 'AbandonPending';
    let canBeAbandoned = this.isTransitionAllowed(project.allowedTransitions, 'Closed', 'Abandoned');
    let canBeRequestAbandoned = this.isTransitionAllowed(project.allowedTransitions, 'Active', 'AbandonPending');

    //PM and SPM don't have this permission, but have project.allowedTransitions allowing them to close autoApproval projects
    let hasAbandonPermission = this.userService.hasPermission('proj.abandon');
    let isMenuAlwaysHidden = isAbandonPending || status.closed;
    return !isMenuAlwaysHidden && (canBeAbandoned || canBeRequestAbandoned || hasAbandonPermission)
  }

  isSuspendPaymentsMenuItemVisible(project) {
    return !project.suspendPayments && project.statusType === 'Active' && this.userService.hasPermission('proj.payments.suspend');
  }

  isResumePaymentsMenuItemVisible(project) {
    return project.suspendPayments && project.statusType === 'Active' && this.userService.hasPermission('proj.payments.suspend');
  }

  isRejectMenuItemVisible(project) {
    let status = this.status(project);
    let hasRejectPermission = this.userService.hasPermission('proj.reject');
    let isMenuAlwaysHidden = status.closed;
    return !isMenuAlwaysHidden && hasRejectPermission;
  }

  isCompleteMenuItemVisible(project) {
    let hasPermission = this.userService.hasPermission('proj.complete');
    return hasPermission && project.statusType === 'Active'
  }


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
  }

  findTransition(allowedTransitions, toStatus, toSubStatus) {
    if (!toStatus) {
      throw Error('Missing required parameter');
    }
    return find(allowedTransitions || [], {status: toStatus, subStatus: toSubStatus || null});
  }


  /**
   * Returns project status object with boolean properties
   * @param project
   * @returns {{draft: true, active: false, ...}}
   */
  status(project): any {
    let allStatuses = ['draft', 'submitted', 'assess', 'active', 'returned', 'closed'];
    let result = {};
    allStatuses.forEach(status => result[status] = (project.statusType.toLowerCase() === status));
    return result;
  }

  getTransitionToClose(project) {
    let transitionsToClose = this.getTransitionsToCloseProject();

    for (let i = 0; i < transitionsToClose.length; i++) {
      let transition = this.findTransition(project.allowedTransitions, transitionsToClose[i].status, transitionsToClose[i].subStatus);
      if (transition) {
        return transition;
      }
    }
    return null;
  }

  canReinstate(project) {
    return project.statusType === 'Closed';
  }

  isMenuItemTransition(transition) {
    let menuItemTransitions = this.getTransitionsToCloseProject();
    return !!this.findTransition(menuItemTransitions, transition.status, transition.subStatus);
  }

  getTransitionsToCloseProject() {
    return [
      {status: 'Closed', subStatus: 'Abandoned'},
      {status: 'Active', subStatus: 'AbandonPending'},
    ];
  }
}
