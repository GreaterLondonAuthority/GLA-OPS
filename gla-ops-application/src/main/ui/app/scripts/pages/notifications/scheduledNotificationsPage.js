/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ScheduledNotificationsPage {

  constructor($state, ToastrUtil, ErrorService, NotificationsService, ConfirmationDialog, CreateOrEditScheduledNotificationModal) {
    this.$state = $state;
    this.ToastrUtil = ToastrUtil;
    this.ErrorService = ErrorService;
    this.NotificationsService = NotificationsService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.CreateOrEditScheduledNotificationModal = CreateOrEditScheduledNotificationModal;
  }

  $onInit() {
  }

  refresh() {
    this.$state.reload();
  }

  showCreateOrEditModal(notification) {
    let modal = this.CreateOrEditScheduledNotificationModal.show(notification, this.availableRoles);
    modal.result.then((notification) => {
      let apiRequest;
      if (notification.id) {
        apiRequest = this.NotificationsService.updateScheduledNotification(notification)
      } else {
        apiRequest = this.NotificationsService.createScheduledNotification(notification);
      }

      apiRequest.then(()=>{
        this.$state.reload();
        this.ToastrUtil.success(notification.id ? 'Scheduled Notification Updated' : 'Scheduled Notification Added');
      }).catch(this.ErrorService.apiValidationHandler());
    });
  }

  delete(notification){
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the scheduled notification?');
    modal.result.then(() => {
      this.NotificationsService.deleteScheduledNotification(notification).then(rsp => {
        this.$state.reload();
      })
    });
  }

}

ScheduledNotificationsPage.$inject = ['$state', 'ToastrUtil', 'ErrorService', 'NotificationsService', 'ConfirmationDialog', 'CreateOrEditScheduledNotificationModal'];

angular.module('GLA')
  .component('scheduledNotificationsPage', {
    templateUrl: 'scripts/pages/notifications/scheduledNotificationsPage.html',
    bindings: {
      scheduledNotifications: '<',
      availableRoles: '<'
    },
    controller: ScheduledNotificationsPage
  });
