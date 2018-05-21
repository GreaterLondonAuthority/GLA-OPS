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

  constructor($state, project, ProjectService, $rootScope, $injector) {
    super(project, $injector);
    this.$state = $state;
    this.ProjectService = ProjectService;
    this.$rootScope = $rootScope;
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

    this.ProjectService.updateDesignStandards(this.project.id, this.data).then(rsp => {
      this.returnToOverview(this.blockId);
    });
  };
}

DesignStandardsCtrl.$inject = ['$state', 'project', 'ProjectService', '$rootScope', '$injector'];


angular.module('GLA')
  .controller('DesignStandardsCtrl', DesignStandardsCtrl);
