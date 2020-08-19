/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class SqlManagerCtrl {
  constructor($state, DatabaseUpdateService, $window) {
    this.$state = $state;
    this.DatabaseUpdateService = DatabaseUpdateService;
    this.$window = $window;
    this.disableRequestUpdate = false;
  }

  $onInit() { }

  back() {
    this.$window.history.back();
  }

  ppdApprove(ppdTested, id) {
    return this.DatabaseUpdateService.approvePpd(ppdTested, id).then((rsp) => {
      }
    );
  }

  sqlStringCheck() {
    if(_.includes(this.sqlUpdateRequest.toLowerCase(), 'update')
      || _.includes(this.sqlUpdateRequest.toLowerCase(), 'delete')
      || _.includes(this.sqlUpdateRequest.toLowerCase(), 'set')) {
        this.disableRequestUpdate = true;
    }
    else {
      this.disableRequestUpdate = false;
    }
  }

  sqlUpdateDetails(id) {
    console.log('get details');
    console.log(id);
    this.$state.go('sql-details', {sqlId: id});
  }

  createUpdateRequest() {
    console.log('create update request');
    this.$state.go('sql-create');
  }

}

SqlManagerCtrl.$inject = ['$state', 'DatabaseUpdateService', '$window'];

angular.module('GLA')
  .component('sqlManager', {
    templateUrl: 'scripts/pages/sql/sql.html',
    bindings: {
      isSqlEditorEnabled: '<?',
      sqlUpdates: '<',
    },
    controller: SqlManagerCtrl
  });
