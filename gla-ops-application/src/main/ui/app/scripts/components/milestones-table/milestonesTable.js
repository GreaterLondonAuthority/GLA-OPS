/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


const gla = angular.module('GLA');

class MilestonesTable {

  constructor(MilestonesService, ProjectMilestoneModal, ConfirmationDialog, ClaimMilestoneModal, ReclaimMilestoneModal, UserService, FileUploadModal, ReclaimInfoModal) {
    this.MilestonesService = MilestonesService;
    this.ProjectMilestoneModal = ProjectMilestoneModal;
    this.ConfirmationDialog = ConfirmationDialog;
    this.ClaimMilestoneModal = ClaimMilestoneModal;
    this.ReclaimMilestoneModal = ReclaimMilestoneModal;
    this.UserService = UserService;
    this.FileUploadModal = FileUploadModal;
    this.ReclaimInfoModal = ReclaimInfoModal;
  }

  $onInit(){
    let project = this.project;
    this.evidenceDefaults = this.evidenceDefaults || {};
    this.grantSourceBlock = _.find(project.projectBlocksSorted, {blockType: 'GrantSource'});
    this.indexOfGrantSourceBlock = (project.projectBlocksSorted || []).indexOf(this.grantSourceBlock) + 1;
    this.zeroGrantRequested = this.grantSourceBlock && this.grantSourceBlock.zeroGrantRequested;
    this.associatedProject = this.grantSourceBlock && this.grantSourceBlock.associatedProject;
    this.grantValue = this.grantSourceBlock && this.grantSourceBlock.grantValue;

    this.milestonesConfig = _.find(this.template.blocksEnabled, {block: 'Milestones'});
    this.maxEvidenceAttachments = this.milestonesConfig.maxEvidenceAttachments;

    this.milestones = this.projectBlock.milestones;

    this.availableToReclaimByType = this.projectBlock.availableToReclaimByType;

    this.maxEvidenceAttachments = this.projectBlock.maxEvidenceAttachments;
    this.isEvidenceAllowedForAll = this.projectBlock.evidenceApplicability === 'ALL_MILESTONES';
    this.isEvidenceAllowedForNew = this.projectBlock.evidenceApplicability === 'NEW_MILESTONES_ONLY';

    this.isAutoApproval = !this.template.stateModel.approvalRequired;
    this.showStatusColumn = this.template.shouldMilestonesBlockShowStatus;
    this.autoCalculateMilestoneState = this.milestonesConfig.autoCalculateMilestoneState;
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
    this.monetarySplitTitle = this.template.monetarySplitTitle;
    this.milestoneType = this.template.milestoneType;
    this.isMonetaryValueType = this.template.milestoneType === 'MonetaryValue';
    this.isMonetarySplitType = this.template.milestoneType === 'MonetarySplit';
    this.isNonMonetarySplitType = this.template.milestoneType === 'NonMonetary';

    // do not update the logic below as we want the user to be able to specify empty string as the hint text
    this.descriptionHintText = this.template.milestoneDescriptionHintText == null ? 'Enter milestone description' : this.template.milestoneDescriptionHintText;

    this.loading = true;


    this.refreshData(true);

    this.canCreateConditionalMilestone = this.UserService.hasPermission(`proj.milestone.conditional.create`);
    this.canEditConditionalMilestone = this.UserService.hasPermission(`proj.milestone.conditional.edit`);
    this.canDeleteConditionalMilestone = this.UserService.hasPermission(`proj.milestone.conditional.delete`);

    //TODO why we are not using permission?
    this.isGlaAdmin = this.UserService.currentUser().primaryRole === 'Admin';
    this.isAddBtnDisabled = this.readOnly || (this.assess && !this.canCreateConditionalMilestone);

    //TODO project status
    this.assess = this.project.statusType.toLowerCase() === 'assess';
    this.active = this.project.statusType.toLowerCase() === 'active';


    this.showClaimStatus = this.claimFeatureEnabled && _.some(this.milestones, (m)=> !!m.claimStatus) && (!this.isAutoApproval || this.isMonetaryValueType || this.isMonetarySplitType);
    this.projectMarkedCorporate = this.project.markedForCorporate;

    //TODO move to router?
    if (!this.readOnly && this.MilestonesService.hasForecastInThePast(this.milestones)) {
      this.autoSave();
    }

    this.showDescription = this.milestonesConfig.descriptionEnabled;

    let conditionalColumnsVisibility = [
      this.showDescription,
      this.isMonetaryValueType,
      this.showGrantColumn,
      this.showEvidenceColumn,
      this.showClaimStatus,
      this.showNaColumn,
      this.projectMarkedCorporate
    ];

    this.columnCount = 11 - _.filter(conditionalColumnsVisibility, visible => !visible).length;
    this.templateGrantTypes = (this.template.grantTypes || []).reduce((res, grant)=>{
      res[grant] = true;
      return res;
    }, {});
  }

  //TO refresh recently added
  $onChanges(changes) {
    console.log('changes', changes);
    if (changes.processingRoute) {
      // console.log('refreshing.....');
      this.milestones = this.projectBlock.milestones;
      this.refreshData(null);
    }
  }

  isEvidenceAllowed(milestone) {
    return this.isEvidenceAllowedForAll || (this.isEvidenceAllowedForNew && milestone.manuallyCreated);
  }


  isMilestoneReadonly(milestone, skipNAColumnValue) {
    if (!skipNAColumnValue && milestone.notApplicable) {
      return true;
    }

    //If edit is clicked on 'Assessed' project
    if (this.assess && !this.readOnly) {
      if (this.isGlaAdmin) {
        return false
      }
      if (milestone.conditional && this.canEditConditionalMilestone) {
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

  isNaFieldReadonly(milestone) {
    return this.isMilestoneReadonly(milestone, true) || !milestone.naSelectable || milestone.manuallyCreated || (milestone.claimStatus && milestone.claimStatus !== 'Pending')
  }


  refreshData(recentlyAdded) {
    this.claimExceeded = false;
    this.reclaimRemaining = false;
    this.loading = false;
    this.hasConditionalMilestones = false;
    this.isSplit100 = this.splitTotal() === 100;

    let availableToReclaimByType = this.projectBlock.availableToReclaimByType;
    if (availableToReclaimByType && (availableToReclaimByType.RCGF || availableToReclaimByType.DPF || (availableToReclaimByType.Grant && this.isMonetaryValueType)   )) {
      this.reclaimRemaining = availableToReclaimByType;
    }

    this.milestones = this.MilestonesService.convertApiMilestonesToUiModel(this.milestones, this.projectBlock, this.payments.content, this.readOnly, this.isMonetarySplitType, this.isSplit100, this.isMonetaryValueType, this.isMonetaryValueReclaimsEnabled);


    //TODO move to independent methods.
    this.milestones.forEach(m => {
      this.showGrantColumn = this.showGrantColumn || (m.monetary && !this.isMonetaryValueType);
      this.showNaColumn = this.showNaColumn || m.naSelectable;
      if (m.conditional) {
        this.hasConditionalMilestones = true;
      }

      if (m.claimedExceeded) {
        this.claimExceeded = true;
      }
    });

    if (recentlyAdded) {
      this.addedMilestone = _.find(this.milestones, {
        summary: recentlyAdded.summary,
        milestoneDate: recentlyAdded.milestoneDate
      });
    }

    this.remainingReclaims = this.getRemainingReclaimsAsArray();
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


  add() {
    var modal = this.ProjectMilestoneModal.show({}, this.milestones, this.isMonetaryValueType, this.showDescription, this.descriptionHintText);
    return modal.result
      .then(milestone => {
        return this.onAddMilestone({$event: milestone}).then((resp) => {
          this.projectBlock = resp.data;
          this.milestones = resp.data.milestones;
          this.refreshData(milestone);
        });
      });
  }

  delete(milestone) {

    let modalMessage = milestone.milestoneMarkedCorporate
      ? `<p class="mtop10">Are you sure you want to delete the milestone?</p>

         <p class="mtop10">Deleting a milestone marked for corporate reporting will
                           also delete it from corporate reporting.</p>`
      : 'Are you sure you want to delete the milestone?';

    var modal = this.ConfirmationDialog.delete(modalMessage);

    modal.result.then(() => {
      this.onDeleteMilestone({$event: milestone}).then((isDeleted) => {
        if (isDeleted === true) {
          _.remove(this.milestones, milestone);
        }
      });
    });
  }

  autoSave() {
    this.onAutoSave()
      .then((resp) => {
        _.forEach(resp.data.milestones, (newMilestone) => {
          let oldMilestone = _.find(this.milestones, {
            id: newMilestone.id
          });
          if (oldMilestone) {
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

  openClaimMilestoneModal(milestone, isActionColumn) {
    if (this.showExtraInfo) {
      if(milestone.isReclaimed){
        const modalInstance = this.ReclaimInfoModal.show(milestone, this.isMonetaryValueType, isActionColumn);
        modalInstance.result.then((data) => {
          this.onClaimMilestoneModalAction({
            $event: {
              milestone: milestone,
              data: data
            }
          });
        });
      }else if (milestone.claimStatus === 'Approved' && milestone.hasRemainingReclaim) {
        const modalInstance = this.ReclaimMilestoneModal.show(milestone, this.projectBlock, this.isMonetaryValueType);
        modalInstance.result.then((data) => {
          milestone.reclaimedRcgf = data.reclaimedRcgf;
          milestone.reclaimedDpf = data.reclaimedDpf;
          milestone.reclaimedGrant = data.reclaimedGrant;
          milestone.reclaimReason = data.reclaimReason;
          this.onReclaimMilestoneModalAction({
            $event: {
              milestone: milestone,
              data: data
            }
          });
        });
      } else {
        const modalInstance = this.ClaimMilestoneModal.show(
          milestone,
          {
            maxClaims: this.projectBlock.maxClaims,
            readOnly: this.readOnly || (!isActionColumn && milestone.claimStatus === 'Claimed'),
            grantValue: this.grantValue,
            monetarySplitTitle: this.monetarySplitTitle,
            zeroGrantRequested: this.zeroGrantRequested,
            milestoneType: this.milestoneType,
            associatedProject: this.associatedProject
          }
        );
        modalInstance.result.then((data) => {
          this.onClaimMilestoneModalAction({
            $event: {
              milestone: milestone,
              data: data
            }
          });
        });
      }
    }
  }


  evidenceLinkText(milestone) {
    let hasAttachments = !!((milestone || {}).attachments || []).length;
    if (this.readOnly) {
      return hasAttachments ? this.evidenceDefaults.textForAdded || 'View' : null;
    } else {
      return hasAttachments ? 'Edit' : 'Add'
    }
  }


  showEvidenceModal(milestone) {
    if (this.showExtraInfo) {
      let modalConfig = {
        orgId: this.project.organisation.id,
        readOnly: this.readOnly,
        fileIdColumn: 'fileId',
        attachments: milestone.attachments,
        title: `Upload evidence for ${milestone.summary} milestone`,
        maxEvidenceAttachments: this.maxEvidenceAttachments,

        onFileUploadComplete: (file) => {
          return this.MilestonesService.attachEvidence(this.project.id, this.projectBlock.id, milestone.id, file.id).then(rsp => {
            return this.updateAttachments(rsp.data, milestone);
          });
        },

        onDeleteFile: (file) => {
          return this.MilestonesService.deleteEvidence(this.project.id, this.projectBlock.id, milestone.id, file.id).then(rsp => {
            return this.updateAttachments(rsp.data, milestone);
          });
        }
      };

      this.FileUploadModal.show(modalConfig);
    }
  }

  updateAttachments(block, milestone){
    milestone.attachments = (_.find(block.milestones, {id: milestone.id}) || {}).attachments || [];
    return milestone.attachments;
  }

  showPayments(m) {
    if (this.showPaymentsToggle(m)) {
      m.expanded = !m.expanded;
    }
  }

  showPaymentsToggle(m) {
    return m.approved && (m.payments || []).length && this.projectBlock.latestVersion
  }

  onNaChange(m) {
    if (m.notApplicable) {
      this.MilestonesService.setMilestoneAsNotApplicable(m);
    }
    this.autoSave();
  }

  hasReclaimRemaining(){
    let reclaimRemaining = this.reclaimRemaining || {};
    return (reclaimRemaining.Grant && this.isMonetaryValueType) || reclaimRemaining.RCGF || reclaimRemaining.DPF;
  }

  getRemainingReclaimsAsArray(){
    let reclaimRemaining = this.reclaimRemaining || {};
    let results = [];
    ['Grant', 'RCGF', 'DPF'].forEach(type =>{
      if(reclaimRemaining[type]){
        results.push({
          type: type,
          value: reclaimRemaining[type]
        });
      }
    });
    return results;
  }

  isStatusColumnReadOnly(milestone){
    return (milestone.notApplicable || this.autoCalculateMilestoneState || this.readOnly);
  }

}


MilestonesTable.$inject = ['MilestonesService', 'ProjectMilestoneModal', 'ConfirmationDialog', 'ClaimMilestoneModal', 'ReclaimMilestoneModal', 'UserService', 'FileUploadModal', 'ReclaimInfoModal'];

gla.component('milestonesTable', {
  templateUrl: 'scripts/components/milestones-table/milestonesTable.html',
  controller: MilestonesTable,
  bindings: {
    project: '<',
    projectBlock: '<',
    template: '<',
    payments: '<',
    readOnly: '<',
    claimFeatureEnabled: '<',
    isMonetaryValueReclaimsEnabled: '<',
    evidenceDefaults: '<',
    onAutoSave: '&',
    onReclaimMilestoneModalAction: '&',
    onClaimMilestoneModalAction: '&',
    onAddMilestone: '&',
    onDeleteMilestone: '&',
    showExtraInfo: '<',
    processingRoute: '<'
  }
});

