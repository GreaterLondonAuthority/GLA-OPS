/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

require('./modal.js');

class LabelsPage {
  constructor($state, CreateLabelModal, LabelService, ConfirmationDialog) {
    this.$state = $state;
    this.CreateLabelModal = CreateLabelModal;
    this.LabelService = LabelService;
    this.ConfirmationDialog = ConfirmationDialog;
  }

  $onInit() {
  }

  refresh() {
    this.$state.reload();
  }

  showLabelModal(label) {
    let modal = this.CreateLabelModal.show(label, this.managingOrganisations, this.labels);
    modal.result.then((label) => {
      let apiRequest;
      if (label.id) {
        apiRequest = this.LabelService.updateLabel(label)
      } else {
        apiRequest = this.LabelService.createLabel(label);
      }

      apiRequest.then(()=>{
        this.$state.reload();
      });
    });
  }

  delete(label){
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the label?');
    modal.result.then(() => {
      this.LabelService.deleteLabel(label).then(rsp => {
        this.$state.reload();
      })
    });
  }

}
LabelsPage.$inject = ['$state', 'CreateLabelModal', 'LabelService', 'ConfirmationDialog'];

angular.module('GLA')
.component('labelsPage', {
  templateUrl: 'scripts/pages/labels/labelsPage.html',
  bindings: {
    labels: '<',
    managingOrganisations: '<',
    onEdit: '&'
  },
  controller: LabelsPage
});
