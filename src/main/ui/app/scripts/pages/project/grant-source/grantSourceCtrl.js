/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

import ProjectBlockCtrl from '../ProjectBlockCtrl';


class GrantSourceCtrl extends ProjectBlockCtrl {
  constructor($scope, $state, $log, project, ProjectService, $rootScope, $injector, template, GrantSourceService) {
    super($injector);
    this.$scope = $scope;
    this.$log = $log;
    this.$state = $state;
    this.ProjectService = ProjectService;
    this.$rootScope = $rootScope;
    this.GrantSourceService = GrantSourceService;
    this.template = template;
  }

  $onInit() {
    super.$onInit();
    this.data = this.projectBlock;
    // console.log('this.projectBlock', this.projectBlock, template);
    this.config = this.GrantSourceService.getSourceVisibilityConfig(this.template);
    this.blockMetaData = this.GrantSourceService.getBlockMetaData(this.template);

    this.GrantSourceService.getGrantSourceBlock(this.project.id).then((resp)=>{
      this.associatedProjectConfig = this.GrantSourceService.getAssociatedProjectConfig(resp.data);
    });



// TODO why not a simple ng-change
    this.$scope.$watch('$ctrl.data.zeroGrantRequested', (zeroGrantRequested, previousValue) => {
      if (zeroGrantRequested) {
        this.data.grantValue = 0;
        this.data.recycledCapitalGrantFundValue = 0;
        this.data.disposalProceedsFundValue = 0;
        this.data.strategicFunding = 0;
      }
      else if (this.data && previousValue) {
        this.data.grantValue = null;
        this.data.recycledCapitalGrantFundValue = null;
        this.data.disposalProceedsFundValue = null;
        this.data.strategicFunding = null;
      }
    });
    this.$scope.$watch('$ctrl.data.associatedProject', (associatedProject, previousValue) => {
      if (associatedProject) {
        this.data.grantValue = 0;
        this.data.recycledCapitalGrantFundValue = 0;
        this.data.disposalProceedsFundValue = 0;
      }
      else if (this.data && previousValue) {
        this.data.grantValue = null;
        this.data.recycledCapitalGrantFundValue = null;
        this.data.disposalProceedsFundValue = null;
        this.data.strategicFunding = null;
      }
    });
  }


  back() {
    if (this.readOnly || !this.data) {
      this.returnToOverview();
    }
    else {
      this.submit();
    }
  };


  /**
   * [submit description]
   * @return {[type]} [description]
   */
  submit() {
    this.$rootScope.showGlobalLoadingMask = true;
    return this.ProjectService.updateGrantSource(this.project.id, this.data);
  }

  total() {
    return this.GrantSourceService.getTotal(this.data);

  }
}

GrantSourceCtrl.$inject = ['$scope', '$state', '$log', 'project', 'ProjectService', '$rootScope', '$injector', 'template', 'GrantSourceService'];


angular.module('GLA')
  .controller('GrantSourceCtrl', GrantSourceCtrl);
