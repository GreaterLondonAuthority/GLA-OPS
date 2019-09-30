/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class NewSqlCtrl {
  constructor($state, DatabaseUpdateService) {
    this.$state = $state;
    this.DatabaseUpdateService = DatabaseUpdateService;
  }

  $onInit() { }

  back() {
    this.$state.go('sql');
  }

  createDatabaseUpdate() {
    let update = {
      'sql' : this.sqlUpdateRequest,
      'summary' : this.sqlSummaryRequest,
      'trackingId' : this.sqlTrackingIdRequest
    };
    this.DatabaseUpdateService.createDatabaseUpdate(update).then(rsp => {
      console.log(rsp.data);
      this.sqlUpdateResult = rsp.data;
      this.$state.go('sql', {obj:update});
    })
  }
}

NewSqlCtrl.$inject = ['$state', 'DatabaseUpdateService'];

angular.module('GLA')
  .component('createNewSql', {
    templateUrl: 'scripts/pages/sql/new/newSql.html',
    // bindings: {
    // },
    controller: NewSqlCtrl
  });
