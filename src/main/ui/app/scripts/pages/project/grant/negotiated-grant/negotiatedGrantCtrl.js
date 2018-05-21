/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../../ProjectBlockCtrl';

class NegotiatedGrantCtrl extends ProjectBlockCtrl {
  constructor($state, project, ProjectService, orderBy, $rootScope, GrantService, $injector, FeatureToggleService, template){
    super(project, $injector);

    this.$state = $state;
    this.ProjectService = ProjectService;
    this.orderBy = orderBy;
    this.$rootScope = $rootScope;
    this.GrantService = GrantService;

    this.lastRequestId = 0;

    this.data = this.sortByDisplayOrder(this.projectBlock);
    this.tenureSummaryDetails =  GrantService.negotiatedGrantBlock(this.data);
    this.tenureClaimedDetails =  GrantService.calculateClaimedTenure(this.data);
    this.updateErrors(this.data.validationFailures);

    this.startOnSiteRestrictionText = template.startOnSiteRestrictionText;

    FeatureToggleService.isFeatureEnabled('StartOnSiteRestrictionText').then((resp)=>{
      this.showStartOnSiteMessage = resp.data && this.data.startOnSiteMilestoneAuthorised && this.startOnSiteRestrictionText;
    });
  }


  back  () {
    if (this.readOnly || !this.data) {
      this.returnToOverview();
    }
    else {
      this.submit();
    }
  };




  sortByDisplayOrder  (tenure) {
    tenure.tenureTypeAndUnitsEntries = this.orderBy(tenure.tenureTypeAndUnitsEntries, 'tenureType.displayOrder');
    return tenure;
  };



  saveTenure (showAnimation, t) {
    this.loading = !!showAnimation;
    var requestId = ++this.lastRequestId;

    let p = this.ProjectService.updateProjectNegotiatedGrant(this.project.id, this.cleanRequestData(this.data), !!showAnimation).then(rsp => {
      if(requestId == this.lastRequestId) {
        var data = this.sortByDisplayOrder(rsp.data);
        //Need to merge to preserve focus inside table
        _.merge(this.data, data);
        this.tenureSummaryDetails =  this.GrantService.negotiatedGrantBlock(this.data);
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
    this.$q.all(this.requestsQueue).then(results => {
      this.saveTenure(false)
        .then(() => {
          this.returnToOverview(this.blockId);
        })
    });
  }

  cleanRequestData(data){
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

NegotiatedGrantCtrl.$inject = ['$state', 'project', 'ProjectService', 'orderByFilter', '$rootScope', 'GrantService', '$injector', 'FeatureToggleService', 'template'];

angular.module('GLA')
  .controller('NegotiatedGrantCtrl', NegotiatedGrantCtrl);
