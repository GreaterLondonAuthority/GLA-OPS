/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

require('./modal.js');

class OverridesPage {
  constructor($state, CreateOverrideModal, OverridesService, ConfirmationDialog) {
    this.$state = $state;
    this.CreateOverrideModal = CreateOverrideModal;
    this.OverridesService = OverridesService;
    this.ConfirmationDialog = ConfirmationDialog;
  }

  $onInit() {
  }

  refresh() {
    this.$state.reload();
  }

  showOverrideModal(override) {
    let modal = this.CreateOverrideModal.show(override, this.metadata);
    modal.result.then((override) => {
      let apiRequest;
      if (override.id) {
        apiRequest = this.OverridesService.updateOverride(override)
      } else {
        apiRequest = this.OverridesService.createOverride(override);
      }

      apiRequest.then(()=>{
        this.$state.reload();
      });
    });
  }

  delete(override){
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the override?');
    modal.result.then(() => {
      this.OverridesService.deleteOverride(override).then(rsp => {
        this.$state.reload();
      })
    });
  }

}
OverridesPage.$inject = ['$state', 'CreateOverrideModal', 'OverridesService', 'ConfirmationDialog'];

angular.module('GLA')
.component('overridesPage', {
  templateUrl: 'scripts/pages/overrides/overridesPage.html',
  bindings: {
    metadata: '<',
    overrides: '<',
    onEdit: '&'
  },
  controller: OverridesPage
});
