/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function ConfirmationDialog($uibModal, $timeout, _) {
  return {
    show(config) {
      var defaultConfig = {
        title: false,
        approveText: 'Yes',
        dismissText: 'No',
        showApprove: true,
        showDismiss: true,
        message: 'Are you sure?',
        info: false
      };

      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/components/common/confirmation-dialog/confirmationDialog.html',
        size: 'confirm',
        controller: [function() {
          this.config = _.merge(defaultConfig, config);
        }]
      });
    },

    delete(message){
      let config = {
        message: message || 'Are you sure you want to delete?',
        approveText: 'DELETE',
        dismissText: 'KEEP'
      };

      return this.show(config);
    },

    warn(message){
      let config = {
        message: message || 'Something went wrong!',
        dismissText: 'CLOSE',
        showApprove: false
      };

      this.show(config);
    },

    info(message){
      let config = {
        message: message || 'Something went wrong!',
        dismissText: 'CLOSE',
        showApprove: false,
        info: true
      };

      this.show(config);
    }
  }
}

ConfirmationDialog.$inject = ['$uibModal', '$timeout', '_'];

angular.module('GLA')
  .service('ConfirmationDialog', ConfirmationDialog);
