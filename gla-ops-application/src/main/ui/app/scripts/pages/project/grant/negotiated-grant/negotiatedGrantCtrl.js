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
    super($injector);

    this.$state = $state;
    this.ProjectService = ProjectService;
    this.orderBy = orderBy;
    this.$rootScope = $rootScope;
    this.GrantService = GrantService;
    this.FeatureToggleService = FeatureToggleService;
    this.template = template;
  }

  $onInit() {
    super.$onInit();
    this.lastRequestId = 0;
    this.data = this.GrantService.sortTenureTypes(this.projectBlock);
    this.tenureSummaryDetails =  this.GrantService.negotiatedGrantBlock(this.data);
    this.tenureClaimedDetails =  this.GrantService.calculateClaimedTenure(this.data, this.project);
    this.updateErrors(this.data.validationFailures);

    this.startOnSiteRestrictionText = this.template.startOnSiteRestrictionText;

    this.FeatureToggleService.isFeatureEnabled('StartOnSiteRestrictionText').subscribe((resp)=>{
      this.showStartOnSiteMessage = resp && this.data.startOnSiteMilestoneAuthorised && this.startOnSiteRestrictionText;
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


  saveTenure (showAnimation, t) {
    this.loading = !!showAnimation;
    var requestId = ++this.lastRequestId;

    let p = this.ProjectService.updateProjectNegotiatedGrant(this.project.id, this.cleanRequestData(this.data), !!showAnimation).then(rsp => {
      if(requestId == this.lastRequestId) {
        var data = this.GrantService.sortTenureTypes(rsp.data);
        //Need to merge to preserve focus inside table
        _.assign(this.data, data);
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
    return this.$q.all(this.requestsQueue).then(results => {
      return this.saveTenure(false);
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
