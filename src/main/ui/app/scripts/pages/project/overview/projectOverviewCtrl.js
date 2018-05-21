/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './TransitionService';

ProjectOverviewCtrl.$inject = [
  '$stateParams', '$state', '$log', 'ProjectService', '$rootScope', '$location', '$anchorScroll',
  'ToastrUtil', 'MessageModal', 'ConfirmationDialog', 'UserService', 'AbandonModal', 'TransitionService',
  'NotificationsService', 'TransferModal'
];

function ProjectOverviewCtrl($stateParams, $state, $log, ProjectService, $rootScope, $location, $anchorScroll,
                             ToastrUtil, MessageModal, ConfirmationDialog, UserService, AbandonModal, TransitionService,
                             NotificationsService, TransferModal) {


  this.isLandProject = this.template.autoApproval;


  this.$log = $log;

  // this.project = project;
  this.MessageModal = MessageModal;
  this.ConfirmationDialog = ConfirmationDialog;
  this.AbandonModal = AbandonModal;
  this.TransferModal = TransferModal;
  this.projectBlocks = this.project.projectBlocksSorted;

  this.submitted = (this.project.status.toLowerCase() === 'submitted');
  this.assess = (this.project.status.toLowerCase() === 'assess');
  this.returned = (this.project.status.toLowerCase() === 'returned');
  this.active = (this.project.status.toLowerCase() === 'active');
  this.draft = (this.project.status.toLowerCase() === 'draft');
  this.closed = (this.project.status.toLowerCase() === 'closed');

  const orgId = this.project.organisation.id;
  this.canReject = UserService.hasPermission('proj.approve', orgId);
  this.canRecommend = UserService.hasPermission('proj.recommend', orgId);
  this.canReinstate= UserService.hasPermission('proj.reinstate', orgId);
  this.canCreateConditionalMilestone = UserService.hasPermission(`proj.milestone.conditional.create`);
  this.subStatusText = ProjectService.subStatusText(this.project);

  let menuConfigItems = {
    'ViewChangeReport': {
      type: 'report',
      uiSref: `change-report({projectId: ${this.project.id}})`,
      displayText: 'Change Management Report'
    },
    'ViewProgrammeSummary': {
      type: 'link',
      //organisation/9999/programme/1006
      uiSref: `organisation-programme({organisationId: ${this.project.organisation.id}, programmeId: ${this.project.programmeId}})`,
      displayText: 'Programme Summary'
    }

  };

  this.onActionClicked = (item) => {
    if (item.action === 'abandon-project') {
      this.abandonProject();
    }
    if (item.action === 'watch') {
      this.watchProject();
    }
    if (item.action === 'unwatch') {
      this.unwatchProject();
    }

    if (item.action === 'transfer-project') {
      this.transferProject();
    }

    if (item.action === 'reinstate-project') {
      this.reinstateProject();
    }

    if (item.action === 'complete-project') {
      this.completeProject();
    }
  };

  this.abandonProject = () => {
    const modal = this.AbandonModal.show(this.project);
    modal.result.then((data) => {
      if (data.requestAbandon) {
        return $state.reload().then(() => ToastrUtil.success('Abandon Requested'));
      } else {
        $state.go('projects');
        return ToastrUtil.success('Abandoned');
      }
    });
  };

  this.transferProject = () => {
    const modal = this.TransferModal.show(this.project);
    modal.result.then(data => $state.reload());
  };

  this.watchProject = () => {
    const modal = this.ConfirmationDialog.show({
      title: 'Watch Project',
      message: 'By selecting to watch a project you will receive all relevant notifications for this project.',
      approveText: 'WATCH PROJECT',
      dismissText: 'CANCEL'
    });

    modal.result
      .then(() => {
        NotificationsService.watchProject(UserService.currentUser().username, this.project.id).then(() => {
          this.project.currentUserWatching = true;
          $state.reload();
        });
      });
  };

  this.unwatchProject = () => {
    const modal = this.ConfirmationDialog.show({
      title: 'Stop watching this project',
      message: 'By selecting to stop watching this project you will cease to receive all relevant notifications for this project.',
      approveText: 'STOP WATCHING PROJECT',
      dismissText: 'CANCEL'
    });

    modal.result
      .then(() => {
        NotificationsService.unwatchProject(UserService.currentUser().username, this.project.id).then(() => {
          this.project.currentUserWatching = false;
          $state.reload();
        });
      });
  };

  this.reinstateProject = () => {
    if(this.canReinstate){
      const modal = this.AbandonModal.show(this.project);
      modal.result.then(()=>{
        return $state.reload().then(() => {return ToastrUtil.success('Reinstated')});
      });
    } else {
      const modal = this.ConfirmationDialog.show({
        message: 'Contact a GLA OPS administrator to reinstate this project.',
        dismissText: 'CLOSE',
        showApprove: false,
      });
    }
  };

  this.completeProject = () => {
    let errorMsg = null;
    if(this.project.status === 'Active' && ['UnapprovedChanges', 'PaymentAuthorisationPending', 'ApprovalRequested'].indexOf(this.project.subStatus) !== -1){
      errorMsg = 'Cannot complete a project with unapproved or incomplete blocks';
    }
    const modal = this.AbandonModal.show(this.project, {status: 'Closed', subStatus: 'Completed'}, errorMsg);
    modal.result.then(()=>{
      return $state.reload().then(() => {return ToastrUtil.success('Completed')});
    });
  };

  this.autoApproval = this.template.autoApproval && this.draft;
  this.programmeClosed = !this.project.programme.enabled;

  this.disableDrafSubmit = !this.project.complete || this.programmeClosed || this.assess || this.autoApproval;
  this.disableReturnedSubmit = !this.project.complete || this.assess || this.autoApproval;


  if ($stateParams.projectSectionSaved && !this.submitted) {
    const block = _.find(this.projectBlocks, {
      'id': $stateParams.projectSectionSaved
    });

    if (block && block.complete) {
      ToastrUtil.success('Saved: Section completed');
    } else {
      ToastrUtil.warning('Saved: Section incomplete');
    }
    // Once digested by ToastrUtil, rest state params
    $stateParams.projectSectionSaved = null;
  }

  /**
   * Pre-populate with project history
   */
  ProjectService.getProjectHistory(this.project.id)
    .then(data => {
      this.historyItems = data;
      // if the project is in draft status ...
      if (!this.submitted && this.historyItems) {
        // ... and the last project history entry is in unconfirmed ...
        if (this.historyItems.length && (this.historyItems[0].transition || '').toLowerCase() === 'unconfirmed') {
          // ... then display the last saved unconfirmed comment
          this.comments = this.historyItems[0].comments;
          this.originalComments = this.comments;
        }
      }
      this.$log.debug(this.submitted, this.historyItems);
    })
    .catch(this.$log.error);

  /**
   * Goto block page
   */
  this.goToSection = (block) => {
    let blockType = block.blockType.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();

    ToastrUtil.clear();

    $state.go(`project.block.${blockType}`, {
      'projectId': this.project.id,
      'blockPosition': this.projectBlocks.indexOf(block) + 1,
      'blockId': block.id,
      'blockClick': true
    }, {reload: true});
  };

  /**
   * Back
   */
  this.onBack = () => {
    $state.go('projects');
  };

  /**
   *
   */
  this.goToSaveDiv = () => {
    $location.hash('save-div');
    $anchorScroll();
  };

  /**
   *
   */
  this.getCommentsPlaceholder = () => {
    if (this.submitted) {
      return 'You can describe why you are withdrawing this project.';
    } else if (this.assess || this.returned && this.canReject) {
      return 'Add an explanatory comment';
    } else {
      return 'If you have any supporting comments, you can add them here.';
    }
  };

  /**
   * Returns the recommendation text for the project
   * @returns {string}
   */
  this.getRecommendation = () => {
    return ProjectService.recommendationText(this.project);
  };

  /**
   * Returns if the project contains a `Milestones` block
   * @returns {boolean}
   */
  this.getMilestonesBlock = () => {
    return _.find(this.project.projectBlocksSorted, block => {
      return block.blockType.toLowerCase() === 'milestones';
    });
  };

  /**
   * Open Milestones block
   */
  this.onOpenMilestones = () => {
    const block = this.getMilestonesBlock();
    if (block) this.goToSection(block);
  };

  this.hasComment = () => {
    return this.comments;
  };

  this.hasNewComment = () => {
    return this.comments != this.originalComments;
  };

  /**
   * Form submit handler
   */
  this.onSubmitProject = () => {
    ProjectService.submitProject(this.project.id, this.comments)
      .then(resp => {
        this.originalComments = this.comments;
        $state.go('projects');
      })
      .catch(rsp => {
        let error = rsp.data || {};
        let msg = error.description || 'Can\'t submit the project';
        this.MessageModal.show({
          message: msg
        })
      });
  };

  /**
   * Form submit handler
   */
  this.onSaveProjectToActive = () => {
    ProjectService.saveProjectToActive(this.project.id, this.comments)
      .then(resp => {
        this.originalComments = this.comments;
        $state.go('projects');
        ToastrUtil.success('Project saved as Active');
      })
      .catch(this.$log.error);
  };


  /**
   * Form submit returned project handler
   */
  this.onSubmitReturnedProject = () => {
    ProjectService.onSubmitReturnedProject(this.project.id, this.comments)
      .then(resp => {
        this.originalComments = this.comments;
        $state.go('projects');
      })
      .catch(rsp => {
        let error = rsp.data || {};
        let msg = error.description || 'Can\'t submit the project';
        this.MessageModal.show({
          message: msg
        })
      });
  };


  this.onWithdrawProject = () => {
    ProjectService.withdrawProject(this.project.id, this.comments)
      .then(resp => {
        this.originalComments = this.comments;
        ToastrUtil.success('You can now edit this project');
        $state.reload();
      })
      .catch(this.$log.error);
  };

  /**
   * Save comment
   */
  this.saveComment = () => {
    if (this.hasNewComment()) {
      ProjectService.saveProjectComment(this.project.id, this.comments)
        .then(resp => {
          this.originalComments = this.comments;
          ToastrUtil.success('Comments were saved');
        })
        .catch(this.$log.error);
    }
  };
  /**
   * Mark a project as "recommend for approval", can be triggered by a GLA PM
   * on a project in assess mode
   */
  this.onRecommendForApproval = () => {
    ProjectService.recommendApproval(this.project.id, this.comments)
      .then(resp => {
        this.originalComments = this.comments;
        $state.go('projects');
        ToastrUtil.success('Recommended Approve');
      })
      .catch(this.$log.error);
  };

  /**
   * Mark a project as "recommend for reject", can be triggered by a GLA PM
   * on a project in assess mode
   */
  this.onRecommendForReject = () => {
    ProjectService.recommendReject(this.project.id, this.comments)
      .then(resp => {
        this.originalComments = this.comments;
        $state.go('projects');
        ToastrUtil.success('Recommended Reject');
      })
      .catch(this.$log.error);
  }

  /**
   * Mark a project as "approved", can be triggered by a GLA SPM
   * on a project in assess mode
   */
  this.onApprove = () => {
    var modal = this.ConfirmationDialog.show({
      message: 'Are you sure you want to approve this project?',
      approveText: 'APPROVE',
      dismissText: 'CANCEL'
    });
    modal.result.then(() => {
      ProjectService.approve(this.project.id, this.comments).then(resp => {
        this.originalComments = this.comments;
        $state.go('projects');
        ToastrUtil.success('Approved');
      });
    });
  }
  /**
   * Mark a project as "reject", can be triggered by a GLA SPM
   * on a project in assess or return
   */
  this.onReject = () => {
    var modal = this.ConfirmationDialog.show({
      message: 'Are you sure you want to reject this project?',
      approveText: 'REJECT',
      dismissText: 'CANCEL'
    });
    modal.result.then(() => {
      ProjectService.reject(this.project.id, this.comments).then(resp => {
        this.originalComments = this.comments;
        $state.go('projects');
        ToastrUtil.success('Rejected');
      });
    });
  };

  /**
   * Mark a project as "Active: ", can be triggered by RP users
   * on a project in "Active: unapproved changes"
   */
  this.onRequestApproval = () => {
    this.$log.log('request approval');
    ProjectService.changeStatus(this.project.id, 'Active', 'ApprovalRequested', this.comments).then(resp => {
      this.originalComments = this.comments;
      $state.go('projects');
      ToastrUtil.success('Approval Requested');
    });
  };


  this.onReturnProject = () => {
    ProjectService.returnProject(this.project.id, this.comments)
      .then(resp => {
        this.originalComments = this.comments;
        $state.go('projects');
        ToastrUtil.success('Returned');
      }).catch(this.$log.error);
  };

  this.onReturnFromApprovalRequested = () => {
    ProjectService.onReturnFromApprovalRequested(this.project.id, this.comments)
      .then(resp => {
        this.originalComments = this.comments;
        $state.go('projects');
        ToastrUtil.success('Returned');
      });
  }

  this.onApproveFromApprovalRequested = () => {
    let msg = 'Are you sure you want to approve the project changes?';
    if (this.project.approvalWillCreatePendingPayment) {
      msg += '<br/><br/><span class="gla-alert">Approving these changes will create a pending payment!</span>';
    }
    if (this.project.approvalWillCreatePendingReclaim) {
      msg += '<br/><br/><span class="gla-alert">Approving these changes will create a pending reclaim!</span>';
    }
    var modal = this.ConfirmationDialog.show({
      message: msg,
      approveText: 'APPROVE',
      dismissText: 'CANCEL'
    });
    modal.result.then(() => {
      ProjectService.onApproveFromApprovalRequested(this.project.id, this.comments)
        .then(resp => {
          this.originalComments = this.comments;
          $state.go('projects');
          ToastrUtil.success('Project changes approved');
        });
    });
  };

  /**
   * Approves the request to abandon a project
   */
  this.onApproveAbandon = () => {
    var modal = this.ConfirmationDialog.show({
      message: 'Are you sure you want to approve the request to abandon and close the project?',
      approveText: 'APPROVE',
      dismissText: 'CANCEL'
    });
    modal.result.then(() => {
      ProjectService.approveAbandon(this.project.id, this.comments).then(resp => {
        this.originalComments = this.comments;
        $state.go('projects');
        ToastrUtil.success('Project Closed');
      }).catch(err => {
        this.ConfirmationDialog.warn(err.data ? err.data.description : null);
      });
    });
  };

  /**
   * Rejects the request to abandon a project
   */
  this.onRejectAbandon = () => {
    var modal = this.ConfirmationDialog.show({
      message: 'Are you sure you want to reject the request to abandon to keep the project active?',
      approveText: 'REJECT',
      dismissText: 'CANCEL'
    });
    modal.result.then(() => {
      ProjectService.rejectAbandon(this.project.id, this.comments).then(resp => {
        this.originalComments = this.comments;
        $state.go('projects');
        ToastrUtil.success('Project Active');
      });
    });
  };

  this.onRequestPaymentAuthorisation = () => {
    let validationMessage = this.validatePaymentRequest();
    if (validationMessage) {
      this.ConfirmationDialog.warn(validationMessage);
    } else {
      let msg = 'Are you sure you want to approve the project changes?';
      // msg += '<br/><br/><span class="gla-alert">Approving these changes will create a pending payment!</span>';

      if (this.project.approvalWillCreatePendingPayment) {
        msg += '<br/><br/><span class="gla-alert">Approving these changes will create a pending payment!</span>';
      }
      if (this.project.approvalWillCreatePendingReclaim) {
        msg += '<br/><br/><span class="gla-alert">Approving these changes will create a pending reclaim!</span>';
      }

      var modal = this.ConfirmationDialog.show({
        message: msg,
        approveText: 'APPROVE',
        dismissText: 'CANCEL'
      });

      modal.result.then(() => {
        ProjectService.onRequestPaymentAuthorisation(this.project.id, this.comments)
          .then(resp => {
            this.originalComments = this.comments;
            $state.go('projects');
            ToastrUtil.success('Project changes approved');
          })
          .catch(err => {
            this.ConfirmationDialog.warn(err.data ? err.data.description : null);
          });
      });
    }
  };
  /**
   * Returns null if valid and validation message if invalid
   */
  this.validatePaymentRequest = () => {
    let showContractNotSignedMsg = this.project.pendingContractSignature;
    let showMissingSapVendorIdMsg = this.project.approvalWillCreatePendingGrantPayment && !this.project.organisation.sapVendorId;
    let orgDetailsLink = `<a href="#/organisation/${this.project.organisation.id}" ng-click="$dismiss()">manage organisation</a>`;

    if (showMissingSapVendorIdMsg && showContractNotSignedMsg) {
      return `Pending payments cannot be submitted for authorisation as the contract for this project type has not been signed and a SAP vendor ID has not been provided. The SAP vendor ID must be added to the ${orgDetailsLink} section by a OPS Admin.`;
    }

    if (showMissingSapVendorIdMsg) {
      return `SAP vendor ID has not been provided. The SAP vendor ID must be added to the ${orgDetailsLink} section by a OPS Admin.`;
    }

    if (showContractNotSignedMsg) {
      return `Pending payments cannot be submitted for authorisation as the contract for this project type has not been signed. The contract must be selected as signed in the ${orgDetailsLink} section.`;
    }
    return null;
  };

  this.onCommentKeyPress = (comment) => {
    if (this.comments && this.comments.length && this.comments != this.originalComments) {
      this.missingComment = false;
    }
  };

  // observe and execute only once
  const watcher = $rootScope.$on('$stateChangeStart', (event, toState, toParams, fromState, fromParams) => {
    if (fromState.name === 'project.overview') {
      if (fromParams.projectId.toString() === this.project.id.toString()) {
        this.saveComment();
      }
      watcher();
    }
  });

  /**
   * Function used to determine which buttons are shown.
   * It bases itself on th allowed transitions returned in th project details.
   * Transitions describe where the project is allowed to go to from it's current
   * status, permissions are factored in in the back end.
   * @return {[type]} [description]
   */
  this.processTransitionState = () => {
    let linkMenuItems = [];

    //TODO should we mix report menu and state actions together? Doesn't look like the same thing
    _.forEach(this.project.allowedActions, allowedAction => {
      let config = menuConfigItems[allowedAction];
      if (config) {
        if (config.type === 'report') {
          linkMenuItems.push(config);
        } else if (config.type === 'action') {
          alert('Never Called');
          //TODO this never happens?
          // actionMenuItems.push(config);
        } else {
          $log.error('Unknow menu item type');
        }
      } else {
        $log.error('Unknown allowed menu item action: ', allowedAction);
      }
    });
    if(linkMenuItems.length){
      linkMenuItems.push({hr:true});
    }
    linkMenuItems.push(menuConfigItems['ViewProgrammeSummary']);
    linkMenuItems.push({hr:true});
    let buttons = TransitionService.getTransitionButtons(this.project, !this.isSubmitToApproveEnabled);
    this.actionMenuItems = TransitionService.getMenuItems(this.project);


    this.linkMenuItems = linkMenuItems;


    // apply buttons configs to template
    this.transitionButtons = _.sortBy(buttons, 'order');
    // if there is at least 1 button, show the comment box
    this.commentBoxVisibility = this.transitionButtons.length > 0;
    //If at least one button is enabled then enable comment box as well
    this.commentBoxEditability = this.transitionButtons.some(btn => !this[btn.disableState]);
  }

  this.transitionButtonsCallback = (buttonCfg) => {
    if (buttonCfg.commentsRequired) {
      if (!this.hasComment()) {
        this.$log.info('Transition requires a comment');
        this.missingComment = true;
        return;
      }
    }
    this.missingComment = false;
    this[buttonCfg.callback]();
  };

  this.jumpTo = (id) => {
    $location.hash(id);
    $anchorScroll();
  };

  this.processTransitionState();
}

angular.module('GLA')
  .component('projectOverview', {
    templateUrl: 'scripts/pages/project/overview/projectOverview.html',
    bindings: {
      isSubmitToApproveEnabled: '<',
      project: '<',
      template: '<'
    },
    controller: ProjectOverviewCtrl
  });
