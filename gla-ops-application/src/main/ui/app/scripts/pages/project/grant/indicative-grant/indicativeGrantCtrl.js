/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../../ProjectBlockCtrl';

class IndicativeGrantCtrl extends ProjectBlockCtrl {
  constructor(project, template, GrantService, $injector) {
    super($injector);

    this.template = template;
    this.GrantService = GrantService;
  }

  $onInit() {
    super.$onInit();
    this.skipModal = false;
    this.lastRequestId = 0;

    this.title = this.GrantService.indicativeGrantSectionTitle(this.template, !this.readOnly);
    this.data = this.GrantService.enhanceIndicativeBlock(this.projectBlock);
    this.criteriaPreviousValue = this.data.affordableCriteriaMet;
    this.tenureSummaryDetailsRows = this.GrantService.indicativeGrantBlocks(this.data);
    let tenureWithMostTiles = _.maxBy(this.tenureSummaryDetailsRows, 'grantBlocks.length');
    this.tilesPerRow = tenureWithMostTiles.grantBlocks.length;
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

  saveTenure(showAnimation, t) {
    this.loading = !!showAnimation;
    var requestId = ++this.lastRequestId;

    let p = this.ProjectService.updateProjectIndicativeGrant(this.project.id, this.cleanRequestData(this.data), !!showAnimation).then(rsp => {
      if (requestId == this.lastRequestId) {
        let projectBlock = this.GrantService.enhanceIndicativeBlock(rsp.data);
        //Need to merge to preserve focus inside table
        _.assign(this.data, projectBlock);
        this.tenureSummaryDetailsRows = this.GrantService.indicativeGrantBlocks(this.data);
        this.criteriaPreviousValue = this.data.affordableCriteriaMet;
        this.data.validationFailures = projectBlock.validationFailures;
        this.updateErrors(projectBlock.validationFailures);
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
    return this.$q.all(this.requestsQueue).then(results => {
      return this.saveTenure(false);
    });
  };

  cleanRequestData(data) {
    let requestBody = angular.copy(data);
    requestBody.tenureTypeAndUnitsEntries.forEach(tt => {
      tt.indicativeTenureValuesSorted = tt.indicativeTenureValuesSorted.filter(t => !t.disabled);
    });
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

IndicativeGrantCtrl.$inject = ['project', 'template', 'GrantService', '$injector'];


angular.module('GLA')
  .controller('IndicativeGrantCtrl', IndicativeGrantCtrl);


/*
angular.module('GLA')
  .component('indicative-grant-page', {
    templateUrl: 'scripts/pages/project/grant/indicative-grant/indicativeGrant.html',
    bindings: {
      organisationTypes: '<',
      organisation: '<',
      showUsers: '<?'
    },
    controller: OrganisationCtrl,
  });
*/
