import {Component, Input, OnDestroy, OnInit, ViewEncapsulation} from '@angular/core';
import {ProjectAbandonModalComponent} from "../project-abandon-modal/project-abandon-modal.component";
import {ProjectTransferModalComponent} from "../project-transfer-modal/project-transfer-modal.component";
import {ProjectLabelModalComponent} from "../project-label-modal/project-label-modal.component";
import {ProjectShareModalComponent} from "../project-share-modal/project-share-modal.component";
import {NavigationService} from "../../navigation/navigation.service";
import {SessionService} from "../../session/session.service";
import {UserService} from '../../user/user.service';
import {ProjectService} from "../project.service";
import {find, forEach, includes, remove, replace, some, sortBy} from 'lodash-es';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ToastrUtilService} from "../../shared/toastr/toastr-util.service";
import {ConfirmationDialogService} from "../../shared/confirmation-dialog/confirmation-dialog.service";
import {ErrorService} from '../../shared/error/error.service';
import {TransitionService} from "../transition.service";
import {NotificationService} from "../../admin/email-reports/notification.service";
import {environment} from "../../../environments/environment";
import {ProjectAssignModalComponent} from "../project-assign-modal/project-assign-modal.component";
import {ProjectPaymentConfirmModalComponent} from "../project-payment-confirm-modal/project-payment-confirm-modal.component";
import {ProjectPaymentConfirmService} from "../project-payment-confirm-modal/project-payment-confirm.service";
import {FeatureToggleService} from "../../feature-toggle/feature-toggle.service";


@Component({
  selector: 'gla-project-overview-page',
  templateUrl: './project-overview-page.component.html',
  styleUrls: ['./project-overview-page.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ProjectOverviewPageComponent implements OnInit, OnDestroy {

  @Input() isSubmitToApproveEnabled = false
  @Input() isMarkedForCorporateEnabled = false
  @Input() isLabelsFeatureEnabled = false
  @Input() isAllowAllFileDownloadEnabled = false
  @Input() isProjectSharingEnabled = false
  @Input() labelMessage: any
  @Input() project: any
  @Input() template: any
  @Input() preSetLabels: any[]
  internalBlocksSectionExpanded = true
  loading = true
  linkMenuItems = []
  isLandProject = false;
  projectBlocks: any[];
  internalBlocksSorted: any[];
  showInternalBlocks: boolean;
  submitted: boolean;
  assess: boolean;
  returned: boolean;
  active: boolean;
  draft: boolean;
  closed: boolean;
  canReject: boolean;
  canRecommend: boolean;
  canReinstate: boolean;
  canCreateConditionalMilestone: boolean;
  subStatusText: string;
  menuConfigItems: any;
  autoApproval: boolean;
  programmeClosed: boolean;
  disableDrafSubmit: boolean;
  disableReturnedSubmit: boolean;
  historyItems: any;
  hasReturnTransitionInHistory: boolean;
  comments: any;
  originalComments: any;
  blocksLoading: boolean;
  fullProject: any;
  numberOfProjectAllowedPerOrg: any;
  missingComment: boolean;
  actionMenuItems: any[];
  transitionButtons: any[];
  commentBoxVisibility: boolean;
  commentBoxEditability: boolean;
  ctrl = this

  constructor(private navigationService: NavigationService,
              private sessionService: SessionService,
              private userService: UserService,
              private projectService: ProjectService,
              private ngbModal: NgbModal,
              private toastrUtilService: ToastrUtilService,
              private confirmationDialogService: ProjectPaymentConfirmService,
              private errorService: ErrorService,
              private transitionService: TransitionService,
              private notificationService: NotificationService,
              private featureToggleService: FeatureToggleService,
              private confirmationDialog: ConfirmationDialogService) {
  }

  ngOnInit(): void {
    /**
     * Missing fields in overview project:
     *
     * programme.enabled
     * allowedTransitions
     * approvalWillCreatePendingPayment
     * approvalWillCreatePendingReclaim
     * pendingContractSignature
     * approvalWillCreatePendingGrantPayment
     * sapVendorId
     * leadOrganisationId
     * allowedActions
     * internalBlocksSorted
     * projectBlocksSorted[i].hasUpdates in overview api is 'false' vs 'true'for example Auto Approval with Questions : PROGRESS UPDATE block
     * paymentOnlyApprovalPossible
     */

    this.loading = true;

    let $stateParams = this.navigationService.getCurrentStateParams();

    if ($stateParams.backNavigation) {
      this.sessionService.setProjectOverview({backNavigation: $stateParams.backNavigation});
    }

    this.isLandProject = !this.template.stateModel.approvalRequired;
    this.projectBlocks = this.project.projectBlocksSorted;
    this.internalBlocksSorted = sortBy(this.project.internalBlocksSorted || [], 'displayOrder');
    this.showInternalBlocks = this.internalBlocksSorted.length && this.userService.hasPermission('proj.view.internal.blocks');
    this.submitted = (this.project.statusType.toLowerCase() === 'submitted');
    this.assess = (this.project.statusType.toLowerCase() === 'assess');
    this.returned = (this.project.statusType.toLowerCase() === 'returned');
    this.active = (this.project.statusType.toLowerCase() === 'active');
    this.draft = (this.project.statusType.toLowerCase() === 'draft');
    this.closed = (this.project.statusType.toLowerCase() === 'closed');

    const orgId = this.project.organisation.id;
    this.canReject = this.userService.hasPermission('proj.approve', orgId);
    this.canRecommend = this.userService.hasPermission('proj.recommend', orgId);
    this.canReinstate = this.userService.hasPermission('proj.reinstate', orgId);
    this.canCreateConditionalMilestone = this.userService.hasPermission(`proj.milestone.conditional.create`);
    this.subStatusText = this.projectService.subStatusText(this.project);

    this.menuConfigItems = {
      'ViewSummaryReport': {
        type: 'report',
        uiSref: `summary-report({projectId: ${this.project.id}, showUnapproved: true})`,
        stateName: 'summary-report',
        stateParams: {projectId: this.project.id, showUnapproved: true},
        displayText: 'Project Summary Report'
      },
      'ViewChangeReport': {
        type: 'report',
        uiSref: `change-report({projectId: ${this.project.id}})`,
        stateName: 'change-report',
        stateParams: {projectId: this.project.id},
        displayText: 'Change Management Report'
      },
      'ViewProgrammeSummary': {
        type: 'link',
        uiSref: `organisation-programme({organisationId: ${this.project.organisation.id}, programmeId: ${this.project.programmeId}})`,
        stateName: 'organisation-programme',
        stateParams: {organisationId: this.project.organisation.id, programmeId: this.project.programmeId},
        displayText: 'Programme Summary'
      }
    };

    // MSGLA-778. Set a title for older projects created without title
    if (!this.project.title) {
      this.project.title = 'Project title Unspecified';
    }

    this.autoApproval = !this.template.stateModel.approvalRequired && this.draft;
    this.programmeClosed = !this.project.programme.enabled;

    this.disableDrafSubmit = !this.project.complete || this.programmeClosed || this.assess || this.autoApproval;
    this.disableReturnedSubmit = !this.project.complete || this.assess || this.autoApproval;

    /**
     * Pre-populate with project history
     */
    this.projectService.getProjectHistory(this.project.id).toPromise().then(data => {
        this.historyItems = data;
        this.hasReturnTransitionInHistory = find(this.historyItems, {transition: 'Returned'});
        // if the project is in draft status ...
        if (!this.submitted && this.historyItems) {
          // ... and the last project history entry is in unconfirmed ...
          if (this.historyItems.length && (this.historyItems[0].transition || '').toLowerCase() === 'unconfirmed') {
            // ... then display the last saved unconfirmed comment
            this.comments = this.historyItems[0].comments;
            this.originalComments = this.comments;
          }
        }
      })

    this.processTransitionState();

    this.blocksLoading = this.hasMissingBlockFields();

    let hasProjectOverviewMissingInfo = this.isProjectOverviewMissingInformation(this.project);
    this.loading = hasProjectOverviewMissingInfo;
    // Call the full project API if the project overview is missing information
    if (hasProjectOverviewMissingInfo) {
      this.projectService.getProject(this.project.id).toPromise().then(rsp => {
        this.fullProject = rsp;
        //$timout to allow render changes before applying css;
        setTimeout(() => {
          this.loading = false;
          this.blocksLoading = false;
        }, 0);

        this.projectBlocks.forEach(block => {
          let fullBlock = find(this.fullProject.projectBlocksSorted, {id: block.id});
          block.hasUpdates = fullBlock.hasUpdates;
          block.complete = fullBlock.complete;
        });

        this.project.approvalWillCreatePendingPayment = this.fullProject.approvalWillCreatePendingPayment;
        this.project.approvalWillCreatePendingReclaim = this.fullProject.approvalWillCreatePendingReclaim;
        this.project.paymentOnlyApprovalPossible = this.fullProject.paymentOnlyApprovalPossible;

        this.project.allowedTransitions = this.fullProject.allowedTransitions;
        this.project.allowedActions = this.fullProject.allowedActions;

        // Bellow is missing information from overview api that was used outside this call
        this.project.approvalWillCreatePendingGrantPayment = this.fullProject.approvalWillCreatePendingGrantPayment;
        this.project.pendingContractSignature = this.fullProject.pendingContractSignature;
        this.project.sapVendorId = this.fullProject.sapVendorId;
        this.project.leadOrganisationId = this.fullProject.leadOrganisationId;

        this.processTransitionState();
      });
    }


  }

  ngOnDestroy() {
    this.saveComment();
    this.ngbModal.dismissAll();
//    TODO test with $onDestroy instead
    /*
        // observe and execute only once
        const watcher = $rootScope.$on('$stateChangeStart', (event, toState, toParams, fromState, fromParams) => {
          if (fromState.name === 'project-overview') {
            if (fromParams.projectId.toString() === this.project.id.toString()) {
              this.saveComment();
            }
            this.ngbModal.dismissAll();
            watcher();
          }
        });*/
  }

  isProjectOverviewMissingInformation(project) {
    if (!project.ableToCalculateTransitions) {
      return true;
    }

    if (project.approvalWillCreatePendingPayment == true || project.approvalWillCreatePendingReclaim == true || project.approvalWillCreatePendingGrantPayment == true) {
      if (project.pendingContractSignature == null || project.sapVendorId == null || project.leadOrganisationId == null) {
        return true;
      }
    }

    return this.hasMissingBlockFields();
  };

  hasMissingBlockFields() {
    return some(this.projectBlocks, (block) => block.hasUpdates == null || block.blockMarkedComplete == null);
  };


  onActionClicked(item) {
    setTimeout(() => {
      if (item.action === 'abandon-project') {
        this.abandonProject();
      }
      if (item.action === 'delete-project') {
        this.deleteProject();
      }
      if (item.action === 'suspend-payments') {
        this.suspendPayments();
      }
      if (item.action === 'resume-payments') {
        this.resumePayments();
      }
      if (item.action === 'reject-project') {
        this.rejectProject();
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

      if (item.action === 'mark-project-corporate') {
        this.markProjectForCorporate(true);
      }

      if (item.action === 'unmark-project-corporate') {
        this.markProjectForCorporate(false);
      }

      if (item.action === 'add-label-to-project') {
        this.addLabel();
      }

      if (item.action === 'download-all-project-files') {
        this.downloadFiles();
      }

      if (item.action === 'share-project') {
        this.shareProject();
      }
    });
  }

  abandonProject() {
    const modal = this.ngbModal.open(ProjectAbandonModalComponent);
    modal.componentInstance.project = this.project;
    modal.result.then((data) => {
      if (data.requestAbandon) {
        this.navigationService.reloadCurrentState();
        this.toastrUtilService.success('Abandon Requested')
      } else {
        this.navigateToListPage();
        return this.toastrUtilService.success('Abandoned');
      }
    }, () => {
    });
  }

  deleteProject() {
    const modal = this.confirmationDialogService.show({
      title: 'Delete Project',
      message: `Are you sure you want to delete this project? You will not be able to retrieve it after you click on 'DELETE'`,
      approveText: 'DELETE',
      dismissText: 'CANCEL',
    });
    modal.result
      .then((a) => {
        this.projectService.deleteProject(this.project.id).toPromise().then(() => {
          this.navigateToListPage();
          return this.toastrUtilService.success('Deleted');
        });
      }, this.errorService.apiValidationHandler());
  };

  suspendPayments() {
    const modal = this.confirmationDialogService.show({
      title: 'Suspend Payments',
      message: 'Suspend payment claims and any payments with a status of \'Pending\'. Payments with a status of \'Sent\' cannot be suspended.',
      approveText: 'SUSPEND PAYMENTS',
      dismissText: 'CANCEL',
      userCommentRequired: true,
    });
    modal.result
      .then((userComment) => {
        this.projectService.suspendProjectPayments(this.project.id, true, userComment.userComment).toPromise().then(() => {
          this.navigationService.reloadCurrentState();
        });
      });
  };

  resumePayments() {
    const modal = this.confirmationDialogService.show({
      title: 'Resume Payments',
      message: 'Resume payment claims and payments with a status of \'Pending\'.',
      approveText: 'RESUME PAYMENTS',
      dismissText: 'CANCEL',
      userCommentRequired: true,
    });
    modal.result
      .then((userComment) => {
        this.projectService.suspendProjectPayments(this.project.id, false, userComment).toPromise().then(() => {
          this.navigationService.reloadCurrentState();
        });
      });
  };

  rejectProject() {
    const modal = this.ngbModal.open(ProjectAbandonModalComponent);
    modal.componentInstance.project = this.project;
    modal.componentInstance.transition = this.transitionService.findTransition(this.project.allowedTransitions, 'Closed', 'Rejected');
    modal.componentInstance.isRejecting = true;
    modal.result.then((data) => {
      this.navigateToListPage()
      return this.toastrUtilService.success('Rejected');
    }, () => {
    });
  };

  transferProject() {
    const modal = this.ngbModal.open(ProjectTransferModalComponent);
    modal.componentInstance.projects = this.project;
    modal.result.then(() => this.navigationService.reloadCurrentState(), () => {
    });
  };

  watchProject() {
    const modal = this.confirmationDialogService.show({
      title: 'Watch Project',
      message: 'By selecting to watch a project you will receive all relevant notifications for this project.',
      approveText: 'WATCH PROJECT',
      dismissText: 'CANCEL'
    });

    modal.result
      .then(() => {
        this.notificationService.watchProject(this.userService.currentUser().username, this.project.id).toPromise().then(() => {
          this.project.currentUserWatching = true;
          this.navigationService.reloadCurrentState();
        });
      });
  };

  unwatchProject() {
    const modal = this.confirmationDialogService.show({
      title: 'Stop watching this project',
      message: 'By selecting to stop watching this project you will cease to receive all relevant notifications for this project.',
      approveText: 'STOP WATCHING PROJECT',
      dismissText: 'CANCEL'
    });

    modal.result
      .then(() => {
        this.notificationService.unwatchProject(this.userService.currentUser().username, this.project.id).toPromise().then(() => {
          this.project.currentUserWatching = false;
          this.navigationService.reloadCurrentState();
        });
      });
  };

  reinstateProject() {
    if (this.canReinstate) {
      this.numberOfProjectAllowedPerOrg = this.template.numberOfProjectAllowedPerOrg;
      this.projectService.canProjectBeAssignedToTemplate(this.project.templateId, this.project.organisation.id).toPromise().then(rsp => {
        if (rsp) {
          const modal = this.ngbModal.open(ProjectAbandonModalComponent);
          modal.componentInstance.project = this.project;
          modal.result.then((data) => {
            this.navigationService.reloadCurrentState();
            return this.toastrUtilService.success('Reinstated')
          }, () => {
          });

        } else {
          this.confirmationDialogService.show({
            message: `Only ${this.numberOfProjectAllowedPerOrg} ${this.numberOfProjectAllowedPerOrg > 1 ? 'projects' : 'project'} ${this.numberOfProjectAllowedPerOrg > 1 ? 'are' : 'is'} permitted for this project type`,
            dismissText: 'CLOSE',
            showApprove: false,
          });
        }
      });
    } else {
      this.confirmationDialogService.show({
        message: 'Contact a GLA OPS administrator to reinstate this project.',
        dismissText: 'CLOSE',
        showApprove: false,
      });
    }
  };

  completeProject() {
    let errorMsg = null;
    if (this.project.statusType === 'Active' && ['UnapprovedChanges', 'PaymentAuthorisationPending', 'ApprovalRequested'].indexOf(this.project.subStatusType) !== -1) {
      errorMsg = 'Cannot complete a project with unapproved or incomplete blocks';
    }

    const modal = this.ngbModal.open(ProjectAbandonModalComponent);
    modal.componentInstance.project = this.project;
    modal.componentInstance.transition = {status: 'Closed', subStatus: 'Completed'};
    modal.componentInstance.errorMsg = errorMsg;
    modal.result.then((data) => {
      this.navigationService.reloadCurrentState();
      return this.toastrUtilService.success('Completed')
    }, () => {
    });
  };

  markProjectForCorporate(markForCorporate) {

    let modalTitle = markForCorporate ? 'Mark Project' : 'Unmark Project';

    let modalMessage = markForCorporate
      ? 'By selecting to mark a project for corporate reporting, you will be required to update additional fields.'
      : 'By selecting to unmark a project for corporate reporting, some fields will no longer appear on your project';


    const modal = this.confirmationDialogService.show({
      title: modalTitle,
      message: modalMessage,
      approveText: modalTitle.toUpperCase(),
      dismissText: 'CANCEL'
    });

    modal.result
      .then(() => {
        this.projectService.updateProjectMarkedForCorporate(this.project.id, markForCorporate).toPromise().then(rsp => {
          this.navigationService.reloadCurrentState();
        });
      });
  };

  addLabel() {
    const modal = this.ngbModal.open(ProjectLabelModalComponent);
    modal.componentInstance.explanatoryText = this.labelMessage;
    modal.componentInstance.existingLabels = this.project.labels;
    modal.componentInstance.preSetLabels = this.preSetLabels;
    modal.result.then((label) => {
      return this.projectService.addLabel(this.project.id, label).toPromise().then(rsp => {
        this.navigationService.reloadCurrentState();
        return this.toastrUtilService.success('Label applied ');
      });
    }, this.errorService.apiValidationHandler());
  };

  downloadFiles() {
    const modal = this.confirmationDialogService.show({
      message: 'Click \'Yes\' to download files added to this project. Note this excludes files added to the Milestones, Funding or Budget blocks, if applicable to projects under this programme.',
      approveText: 'YES',
      dismissText: 'CANCEL'
    });

    modal.result
      .then(() => {
        window.open(environment.basePath + '/projects/' + this.project.id + '/downloadAllAnswers', '_blank');
      });
  };

  shareProject() {
    const modal = this.ngbModal.open(ProjectShareModalComponent);
    modal.componentInstance.projectId = this.project.id;
    modal.result.then((orgId) => {
      if (orgId) {
        this.projectService.shareProject(this.project.id, orgId).toPromise().then(() => {
          this.toastrUtilService.success('Sharing Successful');
        });
      }
    }, () => {
    });
  };

  /**
   * Goto block page
   */
  goToSection(block) {
    let blockType = block.blockType.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();

    this.toastrUtilService.clear();

    this.navigationService.goToUiRouterState(`project-block.${blockType}`, {
      'projectId': this.project.id,
      'blockPosition': this.projectBlocks.indexOf(block) + 1,
      'blockId': block.id,
      'displayOrder': block.displayOrder,
    }, {reload: true});
  };

  /**
   * Back
   */
  onBack() {
    let previousState = (this.sessionService.getProjectOverview() || {}).backNavigation;
    if (previousState && previousState.name) {
      this.navigationService.goToUiRouterState(previousState.name, previousState.params);
    } else if (this.template && this.template.programmeAllocation) {
      this.navigationService.goToUiRouterState('programme-allocations');
    } else {
      this.navigationService.goToUiRouterState('projects');
    }
    this.sessionService.setProjectOverview(null);
  };


  /**
   * Returns the recommendation text for the project
   * @returns {string}
   */
  getRecommendation() {
    return this.projectService.recommendationText(this.project);
  };

  /**
   * Returns if the project contains a `Milestones` block
   * @returns {boolean}
   */
  getMilestonesBlock() {
    return find(this.project.projectBlocksSorted, block => {
      return block.blockType.toLowerCase() === 'milestones';
    });
  };

  /**
   * Open Milestones block
   */
  onOpenMilestones() {
    const block = this.getMilestonesBlock();
    if (block) this.goToSection(block);
  };

  hasComment() {
    return this.comments;
  };

  hasNewComment() {
    return this.comments != this.originalComments;
  };

  /**
   * Form submit handler
   */
  onSubmitProject(transition) {
    return this.projectService.transitionTo(this.project.id, transition, this.comments)
      .toPromise().then(resp => {
        this.originalComments = this.comments;
        this.showTransitionToast(transition)
        return this.navigateToListPage()
      }, this.errorService.apiValidationHandler())
  };

  /**
   * Form submit handler
   */
  onSaveProjectToActive(transition) {
    return this.projectService.saveProjectToActive(this.project.id, this.comments)
      .toPromise().then(resp => {
        this.originalComments = this.comments;
        this.showTransitionToast(transition)
        return this.navigateToListPage()
      })
  }

  /**
   * Form submit returned project handler
   */
  onSubmitReturnedProject(transition) {
    return this.projectService.onSubmitReturnedProject(this.project.id, this.comments)
      .toPromise().then(resp => {
        this.originalComments = this.comments;
        this.showTransitionToast(transition)
        return this.navigateToListPage()
      }, this.errorService.apiValidationHandler())
  };


  onWithdrawProject(transition) {
    return this.projectService.withdrawProject(this.project.id, this.comments)
      .toPromise().then(resp => {
        this.originalComments = this.comments;
        this.showTransitionToast(transition)
        return this.navigationService.reloadCurrentState();
      })
  };

  /**
   * Save comment
   */
  saveComment() {
    if (this.hasNewComment()) {
      return this.projectService.saveProjectComment(this.project.id, this.comments)
        .toPromise().then(resp => {
          this.originalComments = this.comments;
          this.toastrUtilService.success('Comments were saved');
        })
    }
  };

  /**
   * Mark a project as "recommend for approval", can be triggered by a GLA PM
   * on a project in assess mode
   */
  onRecommendForApproval(transition) {
    return this.projectService.recommendApproval(this.project.id, this.comments)
      .toPromise().then(resp => {
        this.originalComments = this.comments;
        this.showTransitionToast(transition)
        return this.navigateToListPage()
      })
  };

  /**
   * Mark a project as "recommend for reject", can be triggered by a GLA PM
   * on a project in assess mode
   */
  onRecommendForReject(transition) {
    return this.projectService.recommendReject(this.project.id, this.comments)
      .toPromise().then(resp => {
        this.originalComments = this.comments;
        this.showTransitionToast(transition)
        return this.navigateToListPage()
      })
  }

  /**
   * Mark a project as "approved", can be triggered by a GLA SPM
   * on a project in assess mode
   */
  onApprove(transition) {
    var modal = this.confirmationDialogService.show({
      message: 'Are you sure you want to approve this project?',
      approveText: 'APPROVE',
      dismissText: 'CANCEL'
    });
    return modal.result.then(() => {
      return this.projectService.approve(this.project.id, this.comments).toPromise().then(resp => {
        this.originalComments = this.comments;
        this.showTransitionToast(transition)
        return this.navigateToListPage()
        // this.toastrUtilService.success('Approved');
      }, this.errorService.apiValidationHandler())
    });
  }

  /**
   * Mark a project as "reject", can be triggered by a GLA SPM
   * on a project in assess or return
   */
  onReject(transition) {
    const modal = this.confirmationDialogService.show({
      message: 'Are you sure you want to reject this project?',
      approveText: 'REJECT',
      dismissText: 'CANCEL'
    });
    return modal.result.then(() => {
      return this.projectService.reject(this.project.id, this.comments).toPromise().then(resp => {
        this.originalComments = this.comments;
        this.showTransitionToast(transition)
        return this.navigateToListPage()
      }, this.errorService.apiValidationHandler());
    });
  };

  /**
   * Mark a project as "Active: ", can be triggered by RP users
   * on a project in "Active: unapproved changes"
   */
  onRequestApproval(transition) {
    this.featureToggleService.isFeatureEnabled('PreventClaimsWithoutFinanceEmail').subscribe((enabled) => {
      if (!enabled && this.project.financeEmailMissing && this.project.approvalWillCreatePendingGrantPayment) {
        let modal = this.confirmationDialog.warnAndContinue('We\'ve noticed your organisation record is missing a \'Finance contact email address\'. Ensure your Organisation Admin adds one to your organisation record as soon as possible, so we know where to send payment confirmation details.')
        modal.result.then(() => {
          this.proceedWithRequestApproval(transition)
        })
      } else {
          this.proceedWithRequestApproval(transition)
      }
    });

  };

  proceedWithRequestApproval(transition) {
    return this.projectService.changeStatus(this.project.id, 'Active', 'ApprovalRequested', this.comments).toPromise().then(resp => {
      this.originalComments = this.comments;
      this.showTransitionToast(transition)
      return this.navigateToListPage()
    }, this.errorService.apiValidationHandler())
  }


  onReturnProject(transition) {
    return this.projectService.returnProject(this.project.id, this.comments)
      .toPromise().then(resp => {
        this.originalComments = this.comments;
        this.showTransitionToast(transition)
        return this.navigateToListPage()
      });
  };

  onReturnFromApprovalRequested(transition) {
    return this.projectService.onReturnFromApprovalRequested(this.project.id, this.comments)
      .toPromise().then(resp => {
        this.originalComments = this.comments;
        this.showTransitionToast(transition)
        return this.navigateToListPage()
      });
  }

  onApproveFromApprovalRequested(transition) {
    let msg = 'Are you sure you want to approve the project changes?';
    let pendingPayment = this.project.approvalWillCreatePendingPayment;
    let pendingReclaim = this.project.approvalWillCreateReclaimPayment;

    if (pendingPayment) {
      msg += '<br/><br/><span class="gla-alert">Approving these changes will create a pending payment!</span>';
    }
    if (pendingReclaim) {
      msg += '<br/><br/><span class="gla-alert">Approving these changes will create a pending reclaim!</span>';
    }
    var modal = this.confirmationDialogService.show({
      message: msg,
      approveText: 'APPROVE',
      dismissText: 'CANCEL'
    });
    return modal.result.then(() => {
      return this.projectService.onApproveFromApprovalRequested(this.project.id, this.comments)
        .toPromise().then(resp => {
          this.originalComments = this.comments;
          this.showTransitionToast(transition)
          return this.navigateToListPage()
          // this.toastrUtilService.success('Project changes approved');
        }, this.errorService.apiValidationHandler());
    });
  };

  /**
   * Approves the request to abandon a project
   */
  onApproveAbandon(transition) {
    const modal = this.confirmationDialogService.show({
      message: 'Are you sure you want to approve the request to abandon and close the project?',
      approveText: 'APPROVE',
      dismissText: 'CANCEL'
    });
    return modal.result.then(() => {
      return this.projectService.approveAbandon(this.project.id, this.comments).toPromise().then(resp => {
        this.originalComments = this.comments;
        this.showTransitionToast(transition)
        return this.navigateToListPage()
      }, this.errorService.apiValidationHandler());
    });
  };

  /**
   * Rejects the request to abandon a project
   */
  onRejectAbandon(transition) {
    const modal = this.confirmationDialogService.show({
      message: 'Are you sure you want to reject the request to abandon to keep the project active?',
      approveText: 'REJECT',
      dismissText: 'CANCEL'
    });
    return modal.result.then(() => {
      return this.projectService.rejectAbandon(this.project.id, this.comments).toPromise().then(resp => {
        this.originalComments = this.comments;
        this.showTransitionToast(transition)
        return this.navigateToListPage()
      });
    });
  };

  onRequestPaymentAuthorisation(transition) {
    let programmeId = this.project.programmeId;
    let validationMessage = this.validatePaymentRequest();
    console.error(`e2e onRequestPaymentAuthorisation ${validationMessage}`)
    if (validationMessage) {
      this.confirmationDialogService.warn(validationMessage);
    } else {
      console.log('PaymentOnlyApprovalPossible',  this.project.paymentOnlyApprovalPossible );
      let msg = 'Are you sure you want to approve the project changes?';

      if (this.project.approvalWillCreatePendingPayment) {
        msg += '<br/><br/><span class="gla-alert">Approving these changes will create a pending payment!</span>';
      }
      if (this.project.approvalWillCreatePendingReclaim) {
        msg += '<br/><br/><span class="gla-alert">Approving these changes will create a pending reclaim!</span>';
      }

      this.featureToggleService.isFeatureEnabled('AllowPaymentsWithoutApproval').subscribe((enabled) => {
          this.showPaymentModal(msg, transition, programmeId, enabled)
      });
    }
  };

  showPaymentModal(msg, transition, programmeId, allowPaymentOnly) {
    var modal = this.confirmationDialogService.show({
      message: msg,
      approveText: 'APPROVE',
      dismissText: 'CANCEL',
      paymentOnlyApprovalPossible: this.project.paymentOnlyApprovalPossible && allowPaymentOnly
    });
    return modal.result.then((data) => {
      let paymentsOnly = data.action === 'paymentsOnly';
      return this.projectService.onRequestPaymentAuthorisation(this.project.id, this.comments, paymentsOnly, data.reason)
        .toPromise().then(resp => {
          this.originalComments = this.comments;
          this.showTransitionToast(transition)
          return this.navigateToListPage()
          // this.toastrUtilService.success('Project changes approved');
        }, err => {
          let description = err.error ? err.error.description : null;
          if (includes(err.error.description, 'WBS') || includes(err.error.description, 'cost element code')) {
            let split = description.split('programme');
            if (split.length > 1) {
              description = split.join(`<a href="#/programme/${programmeId}">programme</a>`);
            }
          }

          description = replace(description, '. ', '.<br>');
          this.confirmationDialogService.warn(description);
        })
    });
  }

  /**
   * Returns null if valid and validation message if invalid
   */
  validatePaymentRequest() {

    let showPaymentSuspendedMsg = this.project.suspendPayments;
    let showContractNotSignedMsg = this.project.pendingContractSignature;
    let showMissingSapVendorIdMsg = this.project.approvalWillCreatePendingGrantPayment && !this.project.sapVendorId;

    let orgDetailsId = this.project.organisationGroupId ? this.project.leadOrganisationId : this.project.organisation.id;
    let orgDetailsLink = `<a href="#/organisation/${orgDetailsId}">manage organisation</a>`;

    if (showPaymentSuspendedMsg) {
      return `Unable to request payment authorisation as project payments are suspended. Resume payments from the Project menu to enable payment.`;
    }

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

  onCommentKeyPress() {
    if (this.comments && this.comments.length && this.comments != this.originalComments) {
      this.missingComment = false;
    }
  };


  /**
   * Function used to determine which buttons are shown.
   * It bases itself on th allowed transitions returned in th project details.
   * Transitions describe where the project is allowed to go to from it's current
   * status, permissions are factored in in the back end.
   * @return {[type]} [description]
   */
  processTransitionState() {
    let linkMenuItems = [];

    //TODO should we mix report menu and state actions together? Doesn't look like the same thing
    forEach(this.project.allowedActions, allowedAction => {
      let config = this.menuConfigItems[allowedAction];
      if (config) {
        if (config.type === 'report') {
          linkMenuItems.push(config);
        } else {
          console.error('Unknow menu item type');
        }
      } else {
        console.warn('Unknown allowed menu item action: ', allowedAction);
      }
    });

    if (linkMenuItems.length) {
      linkMenuItems.push({hr: true});
    }
    linkMenuItems.push(this.menuConfigItems['ViewProgrammeSummary']);
    linkMenuItems.push({hr: true});
    let buttons = this.transitionService.getTransitionButtons(this.project, !this.isSubmitToApproveEnabled);
    let featureToggles = {
      isMarkedForCorporateEnabled: this.isMarkedForCorporateEnabled,
      isLabelsFeatureEnabled: this.isLabelsFeatureEnabled,
      isAllowAllFileDownloadEnabled: this.isAllowAllFileDownloadEnabled,
      isProjectSharingEnabled: this.isProjectSharingEnabled
    };
    this.actionMenuItems = this.transitionService.getMenuItems(this.project, featureToggles);


    this.linkMenuItems = linkMenuItems;


    // apply buttons configs to template
    this.transitionButtons = sortBy(buttons, 'order');
    // if there is at least 1 button, show the comment box
    this.commentBoxVisibility = this.transitionButtons.length > 0;
    //If at least one button is enabled then enable comment box as well
    this.commentBoxEditability = this.transitionButtons.some(btn => !this[btn.disableState]);
  };

  transitionButtonsCallback(buttonCfg) {
    if (buttonCfg.commentsRequired) {
      if (!this.hasComment()) {
        this.missingComment = true;
        return;
      }
    }
    this.missingComment = false;
    this[buttonCfg.callback]((buttonCfg || {}).transition,);
    // let p = this[buttonCfg.callback]((buttonCfg || {}).transition,);
    // if (p && p.then) {
    //   p.then((data => {
    //      // if (data) {
    //       this.showTransitionToast((buttonCfg || {}).transition)
    //      // }
    //   }));
    // }
  };

  jumpTo(id) {
    // TODO
    // $location.hash(id);
    // $anchorScroll();
  };

  toggleGovernanceSection() {
    this.internalBlocksSectionExpanded = !this.internalBlocksSectionExpanded;
  };

  goToInternalBlock(block) {
    switch (block.type) {
      case 'Risk':
        return this.navigationService.goToUiRouterState('project.internal-risk', {projectId: this.project.id, blockId: block.id});
      case 'Assessment':
        return this.navigationService.goToUiRouterState('project.internal-assessment', {projectId: this.project.id, blockId: block.id});
      case 'Questions':
        return this.navigationService.goToUiRouterState('project.internal-questions', {projectId: this.project.id, blockId: block.id});
      case 'ProjectAdmin':
        return this.navigationService.goToUiRouterState('project.internal-admin', {projectId: this.project.id, blockId: block.id});
      default:
        console.error('Internal block not recognised:', block);
    }
  };

  showTransitionToast(transition) {
    let subStatus = this.projectService.getSubStatusText(transition.subStatus);
    let fullStatus = `${transition.status} ${subStatus || ''}`.trim();
    // TODO : this is shown even when there is an error
    this.toastrUtilService.success(`Project updated: "${fullStatus}"`);
    // this.toastrUtilService.success('Project status updated');
  };

//Returns promise
  preStateTransitionActions(transition) {
    let modal = this.getConfirmationModal(transition);
    return modal ? modal.result : Promise.resolve();
  };

  postStateTransitionActions(transition, rsp) {
    this.originalComments = this.comments;
    return this.navigateToListPage()
    // Moved to combine with old approach
    // this.showTransitionToast(transition);
  };


  onTransition(transition) {
    let p = this.preStateTransitionActions(transition);
    return p.then(() => {
      return this.projectService.changeStatus(this.project.id, transition.status, transition.subStatus, this.comments)
        .toPromise().then(rsp => {
          this.showTransitionToast(transition)
          return this.postStateTransitionActions(transition, rsp);
        }, this.errorService.apiValidationHandler())
    });
  };


  getConfirmationModal(transition) {
    if (transition.statusType === 'Closed' && transition.subStatusType === 'Rejected') {
      return this.confirmationDialogService.show({
        message: 'Are you sure you want to reject this project?',
        approveText: 'REJECT',
        dismissText: 'CANCEL'
      });
    }
  };

  navigateToListPage() {
    if (this.template && this.template.programmeAllocation) {
      return this.navigationService.goToUiRouterState('programme-allocations');
    } else {
      return this.navigationService.goToUiRouterState('projects');
    }
  }

  identify(index, item){
    return index;
  }
}
