/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function DeleteNotificationModal($uibModal) {
  return {
    show: function (project) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/notifications/deleteNotification/modal.html',
        size: 'md',
        resolve: {

        },
        controller: ['$uibModalInstance', function ($uibModalInstance) {
          this.doNotShowAgain = false;
          this.onDelete = () => {
            $uibModalInstance.close(this.doNotShowAgain);
          }
        }]
      });
    }
  };
}

DeleteNotificationModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('DeleteNotificationModal', DeleteNotificationModal);
