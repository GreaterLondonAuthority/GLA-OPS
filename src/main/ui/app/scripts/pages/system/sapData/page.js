/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class SapDataCtrl {
  constructor($state, ConfirmationDialog, SapDataService, $window) {
    this.$state = $state;
    this.ConfirmationDialog = ConfirmationDialog;
    this.SapDataService = SapDataService;
    this.$window = $window;
  }

  $onInit() {
  }

  onBack() {
    this.$window.history.back();
  }

  showAllEntriesFn() {
    _.forEach(this.sapData, (entry) => {
        entry.expanded = this.showAllEntries;
      }
    );
  }

  delete(entry) {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete this warning? This error will not display again once deleted and the file will not be matched to a project.');
    modal.result.then(() => {
      return this.SapDataService.ignore(entry.id).then(() => {
      })
    }).then(() => {
      return this.$state.reload();
    })
  }
}

SapDataCtrl.$inject = ['$state', 'ConfirmationDialog', 'SapDataService', '$window'];

angular.module('GLA')
  .component('sapDataPage', {
    templateUrl: 'scripts/pages/system/sapData/page.html',
    bindings: {
      sapData: '='
    },
    controller: SapDataCtrl
  });
