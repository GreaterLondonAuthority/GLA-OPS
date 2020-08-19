/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

function CreateOrEditScheduledNotificationModal($uibModal) {
  return {
    show: function (notification, availableRoles) {
      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/notifications/createOrEditScheduledNotification/modal.html',
        size: 'md',
        resolve: {
          notification: () => {
            return notification;
          },
          availableRoles: () => {
            return availableRoles;
          }
        },
        controller: ['$uibModalInstance', 'notification', 'availableRoles', function ($uibModalInstance) {
          this.notification = notification;
          this.availableRoles = availableRoles;

          if (!this.notification) {
            this.notification = {};
          }

          if (!this.notification.targetOrgIdsString && ! this.notification.targetRolesString) {
            this.notification.allUsers = true;
          }

          if (!this.notification.scheduledDateTime) {
            let nowPlusOneHour = new Date();
            nowPlusOneHour.setHours(nowPlusOneHour.getHours() + 1);
            this.notification.scheduledDateTime = nowPlusOneHour;
            this.time = nowPlusOneHour;
          }
          else {
            this.notification.scheduledDateTime = new Date(this.notification.scheduledDateTime);
            this.time = new Date(this.notification.scheduledDateTime);
          }

          let selectedRoles = this.notification.targetRoles;
          (selectedRoles || []).forEach(r => (_.find(this.availableRoles, {name: r}) || {}).selected = true);

          this.isEditable = () => {
            return this.notification.status !== 'Sent';
          };

          this.allUsersSelected = () => {
            if (this.notification.allUsers) {
              this.notification.targetOrgIdsString = undefined;
              this.toggleRoles(false);
            }
          };

          this.toggleRoles = (isSelected)=>{
            (this.availableRoles || []).forEach(r => r.selected = isSelected)
          };

          this.isFormValid = () => {
            return this.notification.scheduledDateTime && this.notification.text &&
              (this.notification.allUsers || this.notification.targetOrgIdsString || this.getTargetRolesAsString());
          };

          this.getTargetRolesAsString = () => {
            return _.filter(this.availableRoles, {selected: true}).map(r => r.name).join(',');
          };

          this.createOrSave = () => {
            if (!this.notification.status) {
              this.notification.status = 'Scheduled';
            }

            this.notification.scheduledDateTime = new Date(this.notification.scheduledDateTime);
            this.notification.scheduledDateTime.setHours(this.time.getHours());
            this.notification.scheduledDateTime.setMinutes(this.time.getMinutes());
            this.notification.scheduledDateTime.setSeconds(this.time.getSeconds());

            this.notification.targetRolesString = this.getTargetRolesAsString();
            this.notification.targetRoles = null;

            $uibModalInstance.close(this.notification);
          };

        }]
      });
    }
  };
}

CreateOrEditScheduledNotificationModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('CreateOrEditScheduledNotificationModal', CreateOrEditScheduledNotificationModal);
