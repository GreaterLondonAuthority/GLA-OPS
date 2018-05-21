/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../../ProjectBlockCtrl';

class IndicativeGrantCtrl extends ProjectBlockCtrl{
  constructor($state, project, ProjectService, orderBy, $rootScope, GrantService, ConfirmationDialog, $injector) {
    super(project, $injector);

    this.$state = $state;
    this.ProjectService = ProjectService;
    this.orderBy = orderBy;
    this.$rootScope = $rootScope;
    this.GrantService = GrantService;
    this.ConfirmationDialog = ConfirmationDialog;

    this.skipModal = false;
    this.lastRequestId = 0;

    this.data = this.sortByDisplayOrder(this.projectBlock);
    this.criteriaPreviousValue = this.data.affordableCriteriaMet;
    this.tenureSummaryDetailsRows = GrantService.indicativeGrantBlocks(this.data);
    this.updateErrors(this.data.validationFailures);
  }


  back() {
    if (this.readOnly || !this.data) {
      this.returnToOverview();
    }
    else {
      this.submit();
    }
  };


  sortByDisplayOrder(tenure) {
    tenure.tenureTypeAndUnitsEntries = this.orderBy(tenure.tenureTypeAndUnitsEntries, 'tenureType.displayOrder');
    return tenure;
  };

  criteriaChange() {
    if (this.skipModal) {
      this.skipModal = false;
    }
    var modal = this.ConfirmationDialog.show({
      message: 'Changing your selection will re-calculate the number of units eligible for a Top-up grant',
      approveText: 'CHANGE',
      dismissText: 'CANCEL'
    });

    modal.result
      .then(
        v => this.saveTenure(true),
        v => {
          this.data.affordableCriteriaMet = this.criteriaPreviousValue;
          this.skipModal = true;
        }
      );
  };

  saveTenure (showAnimation, t) {
    this.loading = !!showAnimation;
    var requestId = ++this.lastRequestId;

    let p = this.ProjectService.updateProjectIndicativeGrant(this.project.id, this.cleanRequestData(this.data), !!showAnimation).then(rsp => {
      if (requestId == this.lastRequestId) {
        var data = this.sortByDisplayOrder(rsp.data);
        //Need to merge to preserve focus inside table
        _.merge(this.data, data);
        this.tenureSummaryDetailsRows = this.GrantService.indicativeGrantBlocks(this.data);
        this.criteriaPreviousValue = this.data.affordableCriteriaMet;
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
  submit() {
    this.$rootScope.showGlobalLoadingMask = true;
    this.$q.all(this.requestsQueue).then(results => {
      this.saveTenure(false)
        .then(() => {
          this.returnToOverview(this.blockId);
        })
    });
  };

  cleanRequestData(data) {
    var requestBody = angular.copy(data);
    delete requestBody.tenureSummaryDetails;
    delete requestBody.totals;
    delete requestBody.validationFailures;
    delete requestBody.errors;
    return requestBody;
  }

  updateErrors(errors) {
    this.data.errors = this.GrantService.extractErrors(this.data, errors);
  }
}

IndicativeGrantCtrl.$inject = ['$state', 'project', 'ProjectService', 'orderByFilter', '$rootScope', 'GrantService', 'ConfirmationDialog', '$injector'];


angular.module('GLA')
  .controller('IndicativeGrantCtrl', IndicativeGrantCtrl);
