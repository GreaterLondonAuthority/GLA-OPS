/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class SqlDetailsCtrl {
  constructor($state, DatabaseUpdateService) {
    this.$state = $state;
    this.DatabaseUpdateService = DatabaseUpdateService;
  }

  $onInit() {
    this.updateString = this.sqlUpdateDetails.sql;
  }

  back() {
    this.$state.go('sql');
  }

  actionUpdate(approve) {
    if(approve) {
      this.sqlUpdateDetails.status = 'Approved';
    }
    this.DatabaseUpdateService.approveUpdate(this.sqlUpdateDetails, this.sqlUpdateDetails.id).then((rsp) => {
      console.log(rsp);
      this.$state.reload();
    })
  }
}

SqlDetailsCtrl.$inject = ['$state', 'DatabaseUpdateService'];

angular.module('GLA')
  .component('sqlDetails', {
    templateUrl: 'scripts/pages/sql/sqlDetails.html',
    bindings: {
      sqlUpdateDetails: '<'
    },
    controller: SqlDetailsCtrl
  });
