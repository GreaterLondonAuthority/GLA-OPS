/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class SystemCtrl {
  constructor(ActuatorService, ConfigurationService, $state, DatabaseUpdateService, ActionModal) {
    this.message = 'This is the GLA OPS System Administration Dashboard';
    this.dateFormat = 'dd/MM/yyyy HH:mm';
    this.ActuatorService = ActuatorService;
    this.ConfigurationService = ConfigurationService;
    this.$state = $state;
    this.DatabaseUpdateService = DatabaseUpdateService;
    this.ActionModal = ActionModal;
  }

  $onInit() {
    this.disableQueryButton = false;
    this.disableApproveButton = false;

    this.numberServerErrors = this.sumServerErrors();
    this.showSqlEditor = this.isSqlEditorEnabled;
    this.numberTestValidationErrors = _.filter(this.sysInfo.dataValidation.validationFailures, { validationType : 'TestValidation' }).length;
    this.numberDuplicateBlockValidationErrors = _.filter(this.sysInfo.dataValidation.validationFailures, { validationType : 'DuplicateBlocks' }).length;

    this.featureTogglesOnCount = _.filter((this.features || []), {enabled: true}).length;
    this.featureTogglesOffCount = (this.features || []).length - this.featureTogglesOnCount;

  }

  runSqlRead(e) {
    console.log('here is the SQL ' + e)
  }

  goToSqlManager() {
    this.$state.go('sql');
  };

  goToAuditActivity() {
    this.$state.go('audit-activity');
  };

  goToValidationDetails() {
    console.log('Go to more details on data validation error')
    this.$state.go('data-validation-details');
  }

  sumServerErrors() {
    let sum = 0;
    _.forOwn(this.sysMetrics, (value, key) => {
      if(_.includes(key, 'counter.status.500')) {
        sum+=value;
      }
    });
    return sum;
  }

  refresh() {
    this.$state.reload();
  }
}

SystemCtrl.$inject = ['ActuatorService', 'ConfigurationService', '$state', 'DatabaseUpdateService', 'ActionModal'];

angular.module('GLA')
  .component('systemPage', {
    templateUrl: 'scripts/pages/system/system.html',
    bindings: {
      sysInfo: '<',
      sysMetrics: '<',
      isSqlEditorEnabled: '<?',
      features: '<?'
    },
    controller: SystemCtrl
  });
