/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class AnnualSubmissionCtrl {
  constructor($state, AnnualSubmissionService, ConfirmationDialog, ToastrUtil, UserService, $rootScope) {
    this.$state = $state;
    this.AnnualSubmissionService = AnnualSubmissionService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.ToastrUtil = ToastrUtil;
    this.UserService = UserService;
    this.$rootScope = $rootScope;
  }

  $onInit() {
    this.dateFormat = 'dd/MM/yyyy';
    this.envVars = this.$rootScope.envVars;
    this.currentUser = this.UserService.currentUser();
    this.submittingUser = this.currentUser.fullName;
    let blocks = [];

    if (this.annualSubmission) {
      this.annualSubmission.authorisedOn = this.annualSubmission.authorisedOn? new Date(this.annualSubmission.authorisedOn) : null;

      this.isSubmitted = this.annualSubmission.status === 'Submitted';
      this.isApproved = this.annualSubmission.status === 'Approved';
      if (this.isSubmitted) {
        this.showSubmissionCommentText = true;
        this.showApprovalText = false;
        this.submittingUser = this.annualSubmission.submittedByFullName;
      } else if (this.isApproved) {
        this.showApprovalText = true;
        this.showSubmissionCommentText = true;
        this.submittingUser = this.annualSubmission.submittedByFullName;
      }

      this.hasRcgf = !!this.annualSubmission.annualRcgf;
      this.hasDpf = !!this.annualSubmission.annualDpf;
      let submission;
      submission = this.annualSubmission.annualRcgf;
      if (submission) {
        blocks.push({
          name: 'ANNUAL RCGF SUBMISSION',
          icon: submission.complete ? 'glyphicon-ok' : 'glyphicon-exclamation-sign',
          blockState: submission.complete ? 'valid' : 'invalid',
          status: submission.complete ? 'COMPLETE' : 'INCOMPLETE',
          isAnnualSubmission: true,
          blockId: submission.id
        });
      }
      submission = this.annualSubmission.forecastRcgf;
      if (submission) {
        blocks.push({
          name: 'RCGF COMMITMENTS & AGE ANALYSIS',
          icon: submission.complete ? 'glyphicon-ok' : 'glyphicon-exclamation-sign',
          blockState: submission.complete ? 'valid' : 'invalid',
          status: submission.complete ? 'COMPLETE' : 'INCOMPLETE',
          isForecastSubmission: true,
          blockId: submission.id
        });
      }
      submission = this.annualSubmission.annualDpf;
      if (submission) {
        blocks.push({
          name: 'ANNUAL DPF SUBMISSION',
          icon: submission.complete ? 'glyphicon-ok' : 'glyphicon-exclamation-sign',
          blockState: submission.complete ? 'valid' : 'invalid',
          status: submission.complete ? 'COMPLETE' : 'INCOMPLETE',
          isAnnualSubmission: true,
          blockId: submission.id
        });
      }
      submission = this.annualSubmission.forecastDpf;
      if (submission) {
        blocks.push({
          name: 'DPF COMMITMENTS & AGE ANALYSIS',
          icon: submission.complete ? 'glyphicon-ok' : 'glyphicon-exclamation-sign',
          blockState: submission.complete ? 'valid' : 'invalid',
          status: submission.complete ? 'COMPLETE' : 'INCOMPLETE',
          isForecastSubmission: true,
          blockId: submission.id
        });
      }
    }


    let configs = {
      'Draft_Submitted': {
        text: 'SUBMIT',
        callback: 'onSubmit',
        // showSubmissionCommentInput: true,
        enableSubmissionCommentInput: true,
        disableStateFunction: 'submitButtonDisabledFn',
        transitionButtonsCallback: 'submitSubmissionFn',
        displayOrder: 0
      },

      'Submitted_Draft': {
        text: 'REVERT TO DRAFT',
        transitionButtonsCallback: 'revertToDraftFn',
        displayOrder: 1
      },

      'Submitted_Approved': {
        text: 'APPROVE',
        showApprovalInputs: true,
        disableStateFunction: 'approvalButtonDisabledFn',
        transitionButtonsCallback: 'approveSubmissionFn',
        displayOrder: 2
      },

      'Approved_Submitted': {
        text: 'REVERT TO SUBMITTED',
        transitionButtonsCallback: 'revertToApprovedFn',
        displayOrder: 3
      }
    };


    this.allowedTransitions = this.annualSubmission.allowedTransitions;

    let transitionButtons = [];

    _.forEach(this.allowedTransitions, transition => {
      let config = configs[this.annualSubmission.status + '_' + transition.status];
      if (config) {
        transitionButtons.push(config);
        // this.showSubmissionCommentInput = this.showSubmissionCommentInput || config.showSubmissionCommentInput;
        this.enableSubmissionCommentInput = this.enableSubmissionCommentInput || config.enableSubmissionCommentInput;
        this.showApprovalInputs = this.showApprovalInputs || config.showApprovalInputs;
      }
    });

    if (!this.isSubmitted && !this.isApproved && transitionButtons.length == 0) {
      // push dummy button
      transitionButtons.push({
        text: 'SUBMIT',
        disableState: true
      })
    }

    transitionButtons = _.sortBy(transitionButtons, 'displayOrder');
    this.transitionButtons = transitionButtons;

    this.blocks = blocks;
  }

  goBack() {
    this.$state.go('organisation.view', {orgId: this.organisation.id});
  }

  goToBlock(block) {
    let params = {
      submissionId: this.annualSubmission.id,
      blockId: block.blockId,
      orgId: this.organisation.id,
      grantSource: block.grantType
    };
    if (block.isAnnualSubmission) {

      console.log('params', params);
      this.$state.go('grant-annual-submission', params)
    } else if (block.isForecastSubmission) {
      this.$state.go('grant-forecast-submission', params)
    }
  }

  resetAgreementSigned(){
    this.agreementSigned = false;
  }

  autoSave() {
    let annualSubmissionRequest = _.pick(this.annualSubmission, ['submissionComments', 'approvalComments','approvedBy', 'approvedOn',
                                                                 'dpfRollover', 'rcgfRollover', 'dpfRolloverInterest', 'rcgfRolloverInterest',
                                                                 'dpfWithdrawal', 'rcgfWithdrawal', 'dpfWithdrawalInterest', 'rcgfWithdrawalInterest',
                                                                 'authorisedBy', 'authorisedByJobTitle', 'authorisedOn']);
    return this.AnnualSubmissionService.updateAnnualSubmissionOverview(this.annualSubmission.id, annualSubmissionRequest);
  }

  transitionButtonsCallback(config) {
    this[config.transitionButtonsCallback]();
  };

  submitButtonDisabledFn() {
    let requiredFields = ['submissionComments', 'authorisedBy', 'authorisedByJobTitle', 'authorisedOn'];
    let isMissingRequiredFields = _.some(requiredFields, fieldName => !this.annualSubmission[fieldName]);
    return isMissingRequiredFields || !this.enableSubmissionCommentInput || !this.agreementSigned;
  }

  approvalButtonDisabledFn() {
    if (this.hasRcgf &&
      (!(_.isNumber(this.annualSubmission.rcgfRollover) && this.annualSubmission.rcgfRollover >= 0) ||
      !(_.isNumber(this.annualSubmission.rcgfRolloverInterest) && this.annualSubmission.rcgfRolloverInterest >= 0) ||
      !(_.isNumber(this.annualSubmission.rcgfWithdrawal) && this.annualSubmission.rcgfWithdrawal >= 0) ||
      !(_.isNumber(this.annualSubmission.rcgfWithdrawalInterest) && this.annualSubmission.rcgfWithdrawalInterest >= 0))
    ) {
      return true;
    }
    if (this.hasDpf &&
      (!(_.isNumber(this.annualSubmission.dpfRollover) && this.annualSubmission.dpfRollover >= 0) ||
      !(_.isNumber(this.annualSubmission.dpfRolloverInterest) && this.annualSubmission.dpfRolloverInterest >= 0) ||
      !(_.isNumber(this.annualSubmission.dpfWithdrawal) && this.annualSubmission.dpfWithdrawal >= 0) ||
      !(_.isNumber(this.annualSubmission.dpfWithdrawalInterest) && this.annualSubmission.dpfWithdrawalInterest >= 0))
    ) {
      return true;
    }

    return false;
  }


  submitSubmissionFn() {
    this.autoSave().then(() => {
      this.AnnualSubmissionService.updateAnnualSubmissionStatus(
        this.annualSubmission.id, 'Submitted', this.getAgreementText()
      ).then(() => {
        this.ToastrUtil.success('Submitted');
        this.$state.go(this.$state.current, this.$stateParams, {reload: true});
      }, (resp) => {
        this.ConfirmationDialog.warn(resp.data ? resp.data.description : null);
      });
    });
  }

  revertToDraftFn() {
    var modal = this.ConfirmationDialog.show({
      message: 'Any approval comments or approved rollover added will be deleted if you revert to draft. Are you sure you want to revert to draft?',
      approveText: 'REVERT TO DRAFT',
      dismissText: 'CANCEL'
    });
    modal.result.then(() => {
      this.AnnualSubmissionService.updateAnnualSubmissionStatus(
        this.annualSubmission.id, 'Draft'
      ).then(() => {
        this.ToastrUtil.success('Reverted to draft');
        this.$state.go(this.$state.current, this.$stateParams, {reload: true});
      }, (resp) => {
        this.ConfirmationDialog.warn(resp.data ? resp.data.description : null);
      });
    });
  }

  approveSubmissionFn() {
    this.autoSave().then(() => {
      this.AnnualSubmissionService.updateAnnualSubmissionStatus(
        this.annualSubmission.id, 'Approved'
      ).then(() => {
        this.ToastrUtil.success('Approved');
        this.$state.go(this.$state.current, this.$stateParams, {reload: true});
      }, (resp) => {
        this.ConfirmationDialog.warn(resp.data ? resp.data.description : null);
      });
    });
  }

  revertToApprovedFn() {
    var modal = this.ConfirmationDialog.show({
      message: 'Are you sure you want to revert to submitted?',
      approveText: 'REVERT TO SUBMITTED',
      dismissText: 'CANCEL'
    });
    modal.result.then(() => {
      this.AnnualSubmissionService.updateAnnualSubmissionStatus(
        this.annualSubmission.id, 'Submitted'
      ).then(() => {
        this.ToastrUtil.success('Reverted to submitted');
        this.$state.go(this.$state.current, this.$stateParams, {reload: true});
      }, (resp) => {
        this.ConfirmationDialog.warn(resp.data ? resp.data.description : null);
      });
    });
  }

  getGrantTypesTextForCertification(){
    let text = '';
    if(this.hasRcgf){
      text = 'RCGF';
    }

    if(this.hasDpf){
      if(text.length){
        text += '/'
      }
      text += 'DPF';
    }
    return text;
  }


  getAgreementText() {
    if (this.annualSubmission.agreementText) {
      return this.annualSubmission.agreementText;
    } else {
      return `I ${this.submittingUser} confirm that the authorised signatory (${this.annualSubmission.authorisedBy}, ${this.annualSubmission.authorisedByJobTitle}) has certified that the information submitted in this ${this.getGrantTypesTextForCertification()} return meets the GLA certification requirements.`
    }
  }
}

AnnualSubmissionCtrl.$inject = ['$state', 'AnnualSubmissionService', 'ConfirmationDialog', 'ToastrUtil', 'UserService', '$rootScope'];


angular.module('GLA')
  .component('annualSubmission', {
    templateUrl: 'scripts/pages/organisation/annual-submission/annualSubmission.html',
    bindings: {
      annualSubmission: '<',
      organisation: '<'
    },

    controller: AnnualSubmissionCtrl
  });
