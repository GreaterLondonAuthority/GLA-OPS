/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import BootstrapUtil from '../../util/BootstrapUtil';


class ProjectHistoryCtrl {
  constructor($timeout, ProjectService){
    this.$timeout = $timeout;
    this.transitionMap = ProjectService.getTransitionMap();
  }

  $onInit(){
    this.$timeout(()=>{
      BootstrapUtil.setAriaDefaults();
    }, 0);
  }

  formatDate(date) {
    return moment(date).format('DD/MM/YYYY [at] HH:mm');
  }

  // getTransitionText(transitionCode) {
  //   return transitionMap[transitionCode] || transitionCode;
  // }
}

ProjectHistoryCtrl.$inject = ['$timeout', 'ProjectService'];

angular.module('GLA')
  .component('projectHistory', {
    bindings: {
      items: '='
    },
    templateUrl: 'scripts/components/project-history/projectHistory.html',
    controller: ProjectHistoryCtrl
  });
