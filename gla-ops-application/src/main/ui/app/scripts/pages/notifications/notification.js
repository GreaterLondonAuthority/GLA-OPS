/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class NotificationCtrl {
  constructor($state, NotificationsService, UserService, ModalDisplayService, DeleteNotificationModal, SessionService) {
    this.NotificationsService = NotificationsService;
    this.UserService = UserService;
    this.$state = $state;
    this.ModalDisplayService = ModalDisplayService;
    this.DeleteNotificationModal = DeleteNotificationModal;
    this.SessionService = SessionService;
  }

  $onInit() {
    this.notification = this.content.notification;
  }

  _deleteNotification() {
    this.NotificationsService.updateNotificationStatus(this.content.id, 'Deleted').then(()=>{
      this.notificationDeleted();
    });
  }

  deleteNotificationClicked($event) {
    $event.stopPropagation();
    if(this.SessionService.getDoNotShowAgainDeleteNotificationModal()) {
      this._deleteNotification();
    }else{
      this.DeleteNotificationModal.show().result.then((doNotShowAgain)=>{
        this.SessionService.setDoNotShowAgainDeleteNotificationModal(doNotShowAgain);
        this._deleteNotification();
      });
    }
  }

  notificationClicked() {
    this.NotificationsService.markNotificationAsRead(this.content.id).then(()=>{
      this.content.timeRead = true;
      if(this.notification.targetEntityType){
        this.UserService.checkCurrentUserAccess(
          this.notification.targetEntityType,
          this.notification.targetEntityId,
          {403: this.notification.targetEntityType === 'project'}).then(() => {

          if(this.notification.targetEntityType === 'project') {
            this.$state.go('project-overview', {
              'projectId': this.notification.targetEntityId,
            });
          } else if(this.notification.targetEntityType === 'organisation') {
            this.$state.go('organisation.view', {
              'orgId': this.notification.targetEntityId
            });
          } else if(this.notification.targetEntityType === 'payment') {
            this.$state.go('all-payments');
          } else if(this.notification.targetEntityType === 'paymentGroup') {
            this.$state.go('pending-payments', {
              'paymentGroupId': this.notification.targetEntityId
            });
          } else if (this.notification.targetEntityType === 'user') {
            let userId = this.notification.targetEntityId;
            if (userId) {
              this.$state.go('user-account', {'userId': userId});
            } else {
              this.$state.go('users');
            }

          } else if (this.notification.targetEntityType === 'annualSubmission') {
            this.$state.go('annual-submission', {
              'annualSubmissionId': this.notification.targetEntityId
            });
          } else {
            console.error('No action found for action type ', this.notification.targetEntityType);
          }

        }).catch( () => {
          if(this.notification.targetEntityType === 'project'){

            this.ModalDisplayService.standardError({
              header: 'Oops!',
              subHeader: 'Access denied',
              body: 'You cannot access this project as you do not currently have a role within the organisation who owns the project. Contact the organisation if you need more information.'
            });
          }
        });

      }
    });
  }

}

NotificationCtrl.$inject = ['$state','NotificationsService', 'UserService', 'ModalDisplayService', 'DeleteNotificationModal', 'SessionService'];


angular.module('GLA')
  .component('notification', {
    templateUrl: 'scripts/pages/notifications/notification.html',
    bindings: {
      content: '<',
      notificationDeleted: '&'
    },
    controller: NotificationCtrl
  });
