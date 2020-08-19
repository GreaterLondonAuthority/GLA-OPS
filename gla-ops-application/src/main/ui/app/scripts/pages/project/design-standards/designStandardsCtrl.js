/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class DesignStandardsCtrl extends ProjectBlockCtrl {

  constructor($state, project, ProjectService, ProjectBlockService, $rootScope, $injector) {
    super($injector);
    this.$state = $state;
    this.ProjectService = ProjectService;
    this.$rootScope = $rootScope;
  }

  $onInit() {
    super.$onInit();
    this.data = this.projectBlock;
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
    this.data.type = 'DesignStandardsBlock';

    if (this.data.meetingLondonHousingDesignGuide) {
      this.data.reasonForNotMeetingDesignGuide = null;
    }

    return this.ProjectBlockService.updateBlock(this.project.id, this.data.id, this.data, true);

  };
}

DesignStandardsCtrl.$inject = ['$state', 'project', 'ProjectService', 'ProjectBlockService', '$rootScope', '$injector'];


angular.module('GLA')
  .controller('DesignStandardsCtrl', DesignStandardsCtrl);
