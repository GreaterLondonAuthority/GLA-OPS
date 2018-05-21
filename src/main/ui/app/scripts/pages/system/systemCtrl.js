/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class SystemCtrl {
  constructor(ActuatorService, ConfigurationService, $state) {
    this.message = 'This is the GLA OPS System Administration Dashboard';
    this.dateFormat = 'dd/MM/yyyy HH:mm';
    this.ActuatorService = ActuatorService;
    this.ConfigurationService = ConfigurationService;
    this.$state = $state;
  }

  $onInit() {
  }

  refresh() {
    this.$state.reload();
  }
}

SystemCtrl.$inject = ['ActuatorService', 'ConfigurationService', '$state'];


angular.module('GLA')
  .component('systemPage', {
    templateUrl: 'scripts/pages/system/system.html',
    bindings: {
      sysInfo : '<',
      sysMetrics : '<'
    },
    controller: SystemCtrl
  });
