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
    super(project, $injector);

    this.$state = $state;
    this.ProjectService = ProjectService;
    this.$rootScope = $rootScope;
    this.data = this.projectBlock;
    // console.log('this.projectBlock', this.projectBlock, template);
    this.GrantSourceService = GrantSourceService;
    this.config = this.GrantSourceService.getSourceVisibilityConfig(template);
    this.blockMetaData = this.GrantSourceService.getBlockMetaData(template);

    this.GrantSourceService.getGrantSourceBlock(this.project.id).then((resp)=>{
      let data = resp.data;
      if(!data.associatedProject && !data.associatedProjectFlagUpdatable){
        this.showAssociatedProjectMarker = false;
        this.enableAssociatedProjectMarker = false;
      } else if(data.associatedProject && !data.associatedProjectFlagUpdatable){
        this.showAssociatedProjectMarker = true;
        this.enableAssociatedProjectMarker = false;
      } else {
        this.showAssociatedProjectMarker = true;
        this.enableAssociatedProjectMarker = true;
      }
    });



// TODO why not a simple ng-change
    $scope.$watch('$ctrl.data.zeroGrantRequested', (zeroGrantRequested, previousValue) => {
      $log.log('watch', zeroGrantRequested);
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
    $scope.$watch('$ctrl.data.associatedProject', (associatedProject, previousValue) => {
      $log.log('watch', associatedProject);
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
    this.ProjectService.updateGrantSource(this.project.id, this.data).then(rsp => {
      this.returnToOverview(this.blockId);
    });
  }

  total() {
    return +(this.data.grantValue || 0) + +(this.data.recycledCapitalGrantFundValue || 0) + +(this.data.disposalProceedsFundValue || 0)  + +(this.data.strategicFunding || 0)
  }
}

GrantSourceCtrl.$inject = ['$scope', '$state', '$log', 'project', 'ProjectService', '$rootScope', '$injector', 'template', 'GrantSourceService'];


angular.module('GLA')
  .controller('GrantSourceCtrl', GrantSourceCtrl);
