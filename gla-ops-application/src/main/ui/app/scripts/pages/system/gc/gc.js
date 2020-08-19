/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class GCCtrl {
  constructor($state, DashboardService) {
    this.$state = $state;
    this.DashboardService = DashboardService;
  }

  $onInit() {
    this.DashboardService.getGCData().then(resp => {
      return this.gcData = resp.data;
    });
  }

  onClick() {
    this.refresh();
  };

  goBack() {
    this.$state.go('system');
  }

  refresh() {
    this.$state.reload();
  }
}

GCCtrl.$inject = ['$state', 'DashboardService'];

angular.module('GLA')
  .component('gc', {
    templateUrl: 'scripts/pages/system/gc/gc.html',
    bindings: {
    },
    controller: GCCtrl
  });
