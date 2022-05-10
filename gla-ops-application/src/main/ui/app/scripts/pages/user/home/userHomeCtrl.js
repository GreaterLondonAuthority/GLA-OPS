/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';
class UserHomeCtrl {
  constructor(UserService) {
    this.UserService = UserService;
  }

  $onInit(){
    this.user = this.UserService.currentUser();
    this.canViewProjects = this.UserService.hasPermission('proj');
  }
}


UserHomeCtrl.$inject = ['UserService'];

angular.module('GLA')
  .component('userHomePage', {
    templateUrl: 'scripts/pages/user/home/userHome.html',
    bindings: {
      userDashboardMetricsToggle: '<',
      dashboardMetrics: '<',
      homePageMessage: '<'
    },
    controller: UserHomeCtrl,
    controllerAs: '$ctrl'
  });
