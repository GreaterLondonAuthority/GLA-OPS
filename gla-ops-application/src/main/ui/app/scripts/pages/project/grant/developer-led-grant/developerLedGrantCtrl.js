/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../../ProjectBlockCtrl';

class DeveloperLedGrantCtrl extends ProjectBlockCtrl{
  constructor($state, $log, project, ProjectService, orderBy, $rootScope, GrantService, ConfirmationDialog, $injector, FeatureToggleService, template){
    super($injector);

    this.$state = $state;
    this.$log = $log;
    this.ProjectService = ProjectService;
    this.orderBy = orderBy;
    this.$rootScope = $rootScope;
    this.GrantService = GrantService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.FeatureToggleService = FeatureToggleService;
    this.template = template;
  }

  $onInit() {
    super.$onInit();
    this.skipModal = false;
    this.lastRequestId = 0;

    this.data = this.GrantService.sortTenureTypes(this.projectBlock);
    this.otherAffordableTenureTypes = _.find(this.template.blocksEnabled, {block: 'DeveloperLedGrant'}).otherAffordableTenureTypes;
    this.showOtherAffordableQuestion = _.find(this.template.blocksEnabled, {block: 'DeveloperLedGrant'}).showOtherAffordableQuestion;
    this.criteriaPreviousValue = this.data.affordableCriteriaMet;
    this.tenureSummaryDetails = this.GrantService.developerLedGrantBlock(this.data);
    this.tenureClaimedDetails =  this.GrantService.calculateClaimedTenure(this.data, this.project);
    this.updateErrors(this.data.validationFailures);
    this.requestsQueue = [];

    this.startOnSiteRestrictionText = this.template.startOnSiteRestrictionText;

    this.FeatureToggleService.isFeatureEnabled('StartOnSiteRestrictionText').subscribe((resp)=>{
      this.showStartOnSiteMessage = resp && this.data.startOnSiteMilestoneAuthorised && this.startOnSiteRestrictionText;
    });
  }

  back () {
    if (this.readOnly || !this.data) {
      this.returnToOverview();
    }
    else {
      this.submit();
    }
  };

  criteriaChange () {
    if (this.skipModal) {
      this.skipModal = false;
    }

    if (!this.hasAnyValue()) {
      this.criteriaPreviousValue = !this.criteriaPreviousValue;
      return this.saveTenure(false, true);
    }
    var modal = this.ConfirmationDialog.show({
      message: 'Changing your selection will re-calculate the number of units eligible for a Top-up grant',
      approveText: 'CHANGE',
      dismissText: 'CANCEL'
    });

    modal.result
      .then(
        v => {
          this.saveTenure(true, true);
          this.criteriaPreviousValue = !this.criteriaPreviousValue;
        },
        v => {
          this.data.affordableCriteriaMet = this.criteriaPreviousValue;
          this.skipModal = true;
        }
      );
  };

  hasAnyValue () {
    return this.data.tenureTypeAndUnitsEntries.some(el => el.s106Units || el.additionalAffordableUnits || el.totalCost);
  };

  saveTenure(showAnimation, autosave) {
    this.loading = !!showAnimation;
    var requestId = ++this.lastRequestId;

    let p =  this.ProjectService.updateProjectDeveloperLedGrant(this.project.id, this.cleanRequestData(this.data), !!autosave).then(rsp => {
      if (requestId == this.lastRequestId) {
        var data = this.GrantService.sortTenureTypes(rsp.data);
        //Need to merge to preserve focus inside table
        delete data.affordableCriteriaMet;
        _.assign(this.data, data);
        this.tenureSummaryDetails = this.GrantService.developerLedGrantBlock(this.data);
        this.data.validationFailures = data.validationFailures;
        this.updateErrors(data.validationFailures);
        this.loading = false;
      }
    });
    return this.addToRequestsQueue(p);
  };

  /**
   * [submit description]
   * @return {[type]} [description]
   */
  submit () {
    this.$rootScope.showGlobalLoadingMask = true;
    return this.$q.all(this.requestsQueue).then(results => {
      return this.saveTenure(false, false);
    });
  }

  cleanRequestData(data) {
    var requestBody = angular.copy(data);
    delete requestBody.tenureSummaryDetails;
    delete requestBody.totals;
    delete requestBody.validationFailures;
    delete requestBody.errors;
    return requestBody;
  }

  updateErrors (errors) {
    this.data.errors = this.GrantService.extractErrors(this.data, errors);
    this.$log.log('allErrors', this.data.errors);
  }
}

DeveloperLedGrantCtrl.$inject = ['$state', '$log', 'project', 'ProjectService', 'orderByFilter', '$rootScope', 'GrantService', 'ConfirmationDialog', '$injector', 'FeatureToggleService', 'template'];

angular.module('GLA')
  .controller('DeveloperLedGrantCtrl', DeveloperLedGrantCtrl);
