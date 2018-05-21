/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';
import './evidenceModal/evidenceModal';
import './reclaimInfoModal/reclaimInfoModal.js';


class ProjectMilestonesCtrl extends ProjectBlockCtrl {

  constructor($scope, $state, $log, ProjectService, MilestonesService, ProjectMilestoneModal, ConfirmationDialog,
              ClaimMilestoneModal, ReclaimMilestoneModal, project, template, $timeout, ToastrUtil, $injector, UserService,
              FeatureToggleService, EvidenceModal, payments, ReclaimInfoModal) {
    super(project, $injector);
    this.$log = $log;
    this.loading = true;
    this.payments = payments;
    FeatureToggleService.isFeatureEnabled('payments').then(resp => {
      $scope.claimFeatureEnabled = resp.data;
      this.grantSourceBlock = _.find(project.projectBlocksSorted, {blockType: 'GrantSource'});
      this.zeroGrantRequested = this.grantSourceBlock && this.grantSourceBlock.zeroGrantRequested;
      this.associatedProject = this.grantSourceBlock && this.grantSourceBlock.associatedProject;
      this.grantValue = this.grantSourceBlock && this.grantSourceBlock.grantValue;
      this.$scope = $scope;
      this.$state = $state;
      this.ProjectService = ProjectService;
      this.MilestonesService = MilestonesService;
      this.ProjectMilestoneModal = ProjectMilestoneModal;
      this.ConfirmationDialog = ConfirmationDialog;
      this.ClaimMilestoneModal = ClaimMilestoneModal;
      this.ReclaimMilestoneModal = ReclaimMilestoneModal;
      this.ReclaimInfoModal = ReclaimInfoModal;
      this.$timeout = $timeout;
      this.ToastrUtil = ToastrUtil;
      this.UserService = UserService;

      this.template = _.find(template.blocksEnabled, { block: 'Milestones' });
      this.maxEvidenceAttachments = this.template.maxEvidenceAttachments;
      // console.log('metadata', this.template);
      // console.log('template', template);

      this.descriptionEnabled = this.template.descriptionEnabled;

      this.processingRoutes = _.sortBy(this.template.processingRoutes, 'name');
      this.processingRoute = _.find(this.template.processingRoutes, {
        id: this.projectBlock.processingRouteId
      }) || this.template.defaultProcessingRoute;
      this.selectedProcessingRoute = this.processingRoute;
      this.milestones = this.projectBlock.milestones;

      this.availableToReclaimByType = this.projectBlock.availableToReclaimByType;

      this.maxEvidenceAttachments = this.projectBlock.maxEvidenceAttachments;
      this.isEvidenceAllowedForAll = this.projectBlock.evidenceApplicability === 'ALL_MILESTONES';
      this.isEvidenceAllowedForNew = this.projectBlock.evidenceApplicability === 'NEW_MILESTONES_ONLY';

      this.isAutoApproval = template.autoApproval;
      this.milstoneStatuses = [{
        key: 'ACTUAL',
        label: 'Actual'
      }, {
        key: 'FORECAST',
        label: 'Forecast'
      }];
      this.showGrantColumn = false;
      this.showNaColumn = false;
      this.showEvidenceColumn = this.isEvidenceAllowedForAll || this.isEvidenceAllowedForNew;
      this.monetarySplitTitle = template.monetarySplitTitle;
      this.milestoneType = template.milestoneType;
      this.isMonetaryValueType = template.milestoneType === 'MonetaryValue';
      this.isMonetarySplitType = template.milestoneType === 'MonetarySplit';
      this.isNonMonetarySplitType = template.milestoneType === 'NonMonetary';

      // do not update the logic below as we want the user to be able to specify empty string as the hint text
      this.descriptionHintText = template.milestoneDescriptionHintText === null ? 'Enter milestone description' : template.milestoneDescriptionHintText;

      this.loading = true;


      this.refreshData(true);
      // this.disableSubmit = false;

      this.canCreateConditionalMilestone = this.UserService.hasPermission(`proj.milestone.conditional.create`);
      this.canEditConditionalMilestone = this.UserService.hasPermission(`proj.milestone.conditional.edit`);
      this.canDeleteConditionalMilestone = this.UserService.hasPermission(`proj.milestone.conditional.delete`);

      this.isGlaAdmin = this.UserService.currentUser().primaryRole === 'Admin';
      this.isAddBtnDisabled = this.readOnly || (this.assess && !this.canCreateConditionalMilestone);


      this.assess = this.project.status.toLowerCase() === 'assess';
      this.active = this.project.status.toLowerCase() === 'active';

      this.showClaimStatus = $scope.claimFeatureEnabled && !_.every(this.milestones, {claimStatus: null}) && (!this.isAutoApproval || this.isMonetaryValueType || this.isMonetarySplitType);


      if (!this.readOnly && this.MilestonesService.hasForecastInThePast(this.milestones)) {
        this.autoSave();
      }

      this.showDescription = this.isMonetaryValueType && this.descriptionEnabled;

      let conditionalColumnsVisibility = [
        this.showDescription,
        this.isMonetaryValueType,
        this.showGrantColumn,
        this.showEvidenceColumn,
        this.showClaimStatus,
        this.showNaColumn
      ];

      this.columnCount = 10 - _.filter(conditionalColumnsVisibility, visible => !visible).length;
    });

    this.EvidenceModal = EvidenceModal;
  }

  isEvidenceAllowed(milestone){
    return this.isEvidenceAllowedForAll || (this.isEvidenceAllowedForNew && milestone.manuallyCreated);
  }


  isMilestoneReadonly(milestone, skipNAColumnValue) {
    if(!skipNAColumnValue && milestone.notApplicable){
      return true;
    }

    //If edit is clicked on 'Assessed' project
    if (this.assess && !this.readOnly) {
      if(this.isGlaAdmin){
        return false
      }
      if(milestone.conditional && this.canEditConditionalMilestone){
        return false
      }
      return true;
    }
    if (this.active) {
      if (milestone.claimStatus && milestone.claimStatus !== 'Pending') {
        return true;
      }
    }
    return this.readOnly;
  }

  isNaFieldReadonly(milestone){
    return this.isMilestoneReadonly(milestone, true) || !milestone.naSelectable || milestone.manuallyCreated || (milestone.claimStatus && milestone.claimStatus !== 'Pending')
  }

  //TODO timezone issue? Out of sync with backend
  // updateMilestoneStatus(m, afterApi) {
  //   if (!m.milestoneStatus || !afterApi) {
  //     m.milestoneStatus = this.getMilestoneStatus(m.milestoneDate);
  //   }
  // };

  // getMilestoneStatus(milestoneDate) {
  //   var today = moment().format('YYYY-MM-DD');
  //   if (milestoneDate) {
  //     return (milestoneDate < today) ? 'ACTUAL' : 'FORECAST'
  //   }
  //   return 'FORECAST';
  // };

  // TODO move to directive
  // TODO: this is currently broken!!
  keyboardNavigation() {
    var previousRow = null;
    this.$timeout(() => {
      $('tr input, tr button')
        .focus(event => {
          var $row = $(event.currentTarget).parents('tr');
          $row.addClass('focused');
          if (previousRow !== $row[0]) {
            $(previousRow).removeClass('focused');
          }
          previousRow = $row[0];

        })
        .blur(event => {
          this.$timeout(() => {
            var $row = $(document.activeElement).parents('tr');
            if (previousRow !== $row[0]) {
              $(previousRow).removeClass('focused');
            }
          });
        });
    })
  }

  focusNextElement() {
    this.$timeout(() => {
      $($('table input:text')[0])
        .first()
        .focus();

      this.$scope.$apply();
    }, 100);
  };

  refreshData(focus, recentlyAdded) {
    let tomorrow = moment().add(1,'day');
    this.claimExceeded = false;
    this.reclaimRemaining = false;
    this.loading = false;
    this.keyboardNavigation();
    this.hasConditionalMilestones = false;
    this.isSplit100 = this.splitTotal() === 100;

    this.milestones.forEach(m => {
      this.showGrantColumn = this.showGrantColumn || (m.monetary && !this.isMonetaryValueType);
      this.showNaColumn = this.showNaColumn || m.naSelectable;
      if(m.milestoneDate){
        m.isDateInPast = moment(m.milestoneDate).isBefore(tomorrow,'day');
      }

      m.monetarySplit = m.monetarySplit || 0;
      // TODO figure out what to do about pagination
      m.payments = _.filter(this.payments.content, p => {
        if (m.externalId) {
          return p.externalId === m.externalId;
        } else {
          let milestoneName = p.reclaim ? `Reclaim ${m.summary}` : m.summary;
          return p.subCategory === milestoneName;
        }
      });
      // this.updateMilestoneStatus(m, true);
      if (m.conditional) {
        this.hasConditionalMilestones = true;
      }

      if (m.claimedExceeded) {
        this.claimExceeded = true;
      }


      let availableToReclaimByType = this.projectBlock.availableToReclaimByType;
      if(availableToReclaimByType && (availableToReclaimByType.RCGF + availableToReclaimByType.DPF)){
        this.reclaimRemaining = availableToReclaimByType;
      }

      m.displayText = m.claimStatus;
      m.hasAction = false;



      if(m.claimStatus === 'Approved') {

        if(!this.readOnly && (
            (m.claimedRcgf ? (availableToReclaimByType.RCGF + m.reclaimedRcgf) : 0) +
            (m.claimedDpf ? (availableToReclaimByType.DPF + m.reclaimedDpf) : 0)
          ) > 0){
          m.hasRemainingReclaim = true;
          m.hasAction = true;
        } else {
          m.hasAction = false;
        }
      }

      // Due to some UX complications in another story, we don't want to show
      // the approved modal.
      if(m.claimStatus === 'Claimed') {
        m.hasAction = true;
      }
      if(this.readOnly) {
      } else {
        if(m.milestoneStatus === 'ACTUAL' && m.claimStatus === 'Pending'){
          m.displayText = 'Claim';
          if(this.isMonetarySplitType){
            m.hasAction = this.isSplit100;
          } else {
            m.hasAction = true;
          }
        }
      }


    });

    if (focus) {
      this.focusNextElement();
    }

    if (recentlyAdded) {
      this.addedMilestone = _.find(this.milestones, {
        summary: recentlyAdded.summary,
        milestoneDate: recentlyAdded.milestoneDate
      });
    }
  }

  /**
   * Processing route changed
   */
  processingRouteSelected(selected) {
    this.selectedProcessingRoute = selected;
  }

  /**
   * Confirm processing route selection and update table data
   */
  confirmProcessingRoute() {
    // this.processingRoute = this.selectedProcessingRoute;
    this.loading = true;

    this.$log.debug('confirmProcessingRoute');

    let p = this.ProjectService.updateProjectProcessingRoute(this.project.id, this.blockId, this.selectedProcessingRoute.id)
      .then(resp => {
        this.milestones = resp.data.milestones;
        this.processingRoute = _.find(this.template.processingRoutes, {
          id: resp.data.processingRouteId
        });
        this.$log.debug(this.processingRoute);
        this.refreshData(false);
        this.loading = false;
      });

    this.addToRequestsQueue(p);
  }

  /**
   * Remove currently selected processing route
   */
  removeProcessingRoute() {
    if(this.processingRoute) {
      var modal = this.ConfirmationDialog.show({
        message: 'Are you sure you want to change the processing route?\nThis will change your milestone plan and any data input may be lost.',
        approveText: 'YES',
        dismissText: 'CANCEL'
      });

      modal.result
        .then(() => {
          this.processingRoute = null;
        });
    } else {
      this.processingRoute = null;
    }
  }

  onSplitChange(m) {
    m._invaliMonetarySplit = (m.monetarySplit > 100);
  }

  splitTotal() {
    var total = 0;
    this.milestones.forEach(m => {
      if (m.monetarySplit && m.monetarySplit != '') {
        total = total + parseInt(m.monetarySplit);
      }
    });
    return total;
  }

  back() {
    if (this.readOnly || this.loading) {
      this.returnToOverview();
    } else {
      this.submit();
    }
  }

  saveAndKeepLock() {
    const data = {
      lockDetails: this.projectBlock.lockDetails,
      milestones: this.milestones,
      type: 'ProjectMilestonesBlock'
    };
    let p = this.ProjectService.updateProjectMilestones(this.project.id, this.blockId, data, true);
    return this.addToRequestsQueue(p);
  }

  save() {
    const data = {
      lockDetails: this.projectBlock.lockDetails,
      milestones: this.milestones,
      type: 'ProjectMilestonesBlock'
    };
    return this.ProjectService.updateProjectMilestones(this.project.id, this.blockId, data, false);
  }

  /**
   * Submit
   */
  submit() {
    //$timeout to fix autosafe=true after saving
    this.$timeout(()=>{
      this.$q.all(this.requestsQueue).then(results => {
        this.save()
          .then(response => {
            this.$log.log('milestones saved', response);
            this.returnToOverview(this.blockId);
          });
      });
    });
  };

  cancel() {
    this.returnToOverview();
  }

  add() {
    this.saveAndKeepLock()
      .then(() => this.refreshData(false))
      .then(() => {
        var modal = this.ProjectMilestoneModal.show({}, this.milestones, this.isMonetaryValueType, this.descriptionHintText);
        return modal.result
          .then(milestone => {
            return this.ProjectService.addProjectMilestones(this.project.id, this.blockId, milestone)
              .then(resp => {
                this.milestones = resp.data.milestones;
                this.refreshData(false, milestone);
                this.ToastrUtil.success('Milestone Added');
              });
          })
      })
  }

  delete(milestone) {
    var modal = this.ConfirmationDialog.delete('Are you sure you want to delete the milestone?');

    let p = modal.result
      .then(() => {
        return this.ProjectService.deleteProjectMilestone(this.project.id, this.blockId, milestone.id)
          .then(() => {
            _.remove(this.milestones, milestone);
            this.ToastrUtil.success('Milestone Deleted');
          });
      });
    return this.addToRequestsQueue(p);
  }

  autoSave() {
    this.saveAndKeepLock()
      .then((resp) => {
        _.forEach(resp.data.milestones, (newMilestone) => {
          let oldMilestone = _.find(this.milestones, {
            id: newMilestone.id
          });
          if(oldMilestone){
            // oldMilestone.milestoneStatus = newMilestone.milestoneStatus;
            _.assign(oldMilestone, newMilestone);
          }
        });
        this.projectBlock.tally = resp.data.tally;
        this.projectBlock.valid = resp.data.valid;
        this.projectBlock.validationFailures = resp.data.validationFailures;

        this.refreshData(false);
      });
  }

  openClaimMilestoneModal(milestone) {
    if(milestone.claimStatus === 'Approved' && milestone.hasRemainingReclaim) {
      const modalInstance = this.ReclaimMilestoneModal.show(milestone, this.projectBlock);
      let p = modalInstance.result.then((data) => {
        milestone.reclaimedRcgf = data.reclaimedRcgf;
        milestone.reclaimedDpf = data.reclaimedDpf;
        milestone.reclaimReason = data.reclaimReason;
        return this.saveAndKeepLock().then(()=>{
          this.$state.go(this.$state.current.name, this.$stateParams, {reload: true});
        });

      });

    } else {

      const modalInstance = this.ClaimMilestoneModal.show(
        milestone,
        {
          maxClaims: this.projectBlock.maxClaims,
          readOnly:this.readOnly,
          grantValue:this.grantValue,
          monetarySplitTitle:this.monetarySplitTitle,
          zeroGrantRequested:this.zeroGrantRequested,
          milestoneType:this.milestoneType,
          associatedProject:this.associatedProject
        }
      );
      let p = modalInstance.result.then( (data) => {
        if (data.action === this.MilestonesService.claimActions.claim) {
          return this.MilestonesService.claimMilestone(this.project.id, milestone.id, data).then(() => {
            this.$state.go(this.$state.current.name, this.$stateParams, {reload: true});
          });
        }

        if (data.action === this.MilestonesService.claimActions.cancel) {
          return this.MilestonesService.cancelClaim(this.project.id, milestone.id).then(() => {
            this.$state.go(this.$state.current.name, this.$stateParams, {reload: true});
          });
        }


      });

      return this.addToRequestsQueue(p);
    }
  }

  showReclaimInfoIcon(milestone){
    return this.readOnly && (milestone.reclaimed || milestone.reclaimedDpf || milestone.reclaimedRcgf);
  }


  openReclaimInfoModal(milestone){
    if(milestone.reclaimedDpf || milestone.reclaimedRcgf){
      this.ReclaimInfoModal.show(milestone);
    }else if(milestone.reclaimed){
      this.ConfirmationDialog.info('A repayment/reclaim has been made against this milestone.')
    }
  }


  evidenceLinkText(milestone) {
    let hasAttachments = !!((milestone || {}).attachments || []).length;
    if (this.readOnly) {
      return hasAttachments ? 'View' : null;
    } else {
      return hasAttachments ? 'Edit' : 'Add'
    }
  }



  showEvidenceModal(milestone){
    this.EvidenceModal.show(this.project, this.blockId, milestone, this.maxEvidenceAttachments, this.readOnly);
  }

  showPayments(m){
    if(this.showPaymentsToggle(m)) {
      m.expanded = !m.expanded;
    }
  }

  showPaymentsToggle(m){
    return m.approved && (m.payments ||[]).length && this.projectBlock.latestVersion
  }

  onNaChange(m){
    if(m.notApplicable){
      this.MilestonesService.setMilestoneAsNotApplicable(m);
    }
    this.autoSave();
  }
}

ProjectMilestonesCtrl.$inject = [
  '$scope', '$state', '$log', 'ProjectService', 'MilestonesService', 'ProjectMilestoneModal', 'ConfirmationDialog',
  'ClaimMilestoneModal', 'ReclaimMilestoneModal', 'project', 'template', '$timeout', 'ToastrUtil', '$injector', 'UserService',
  'FeatureToggleService', 'EvidenceModal', 'payments', 'ReclaimInfoModal'
];

angular.module('GLA')
  .controller('ProjectMilestonesCtrl', ProjectMilestonesCtrl);
