/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class NotificationsPageCtrl {
  constructor($rootScope, NotificationsService, FeatureToggleService, MetadataService, SessionService, DeleteNotificationModal) {
    this.$rootScope = $rootScope;
    this.MetadataService = MetadataService;
    this.NotificationsService = NotificationsService
    this.maxDisplay = this.$rootScope.envVars['notifications-max-display'];
    this.FeatureToggleService = FeatureToggleService;
    this.SessionService = SessionService;
    this.DeleteNotificationModal = DeleteNotificationModal;
  }

  $onInit() {
    this.originalList = [];
    this.currentPage = 0;
    this.allLoaded = false;
    this.FeatureToggleService.isFeatureEnabled('Notifications').subscribe(resp => {
      if(resp){
        this.loadNotificationsPage(this.currentPage);
      }
    });
  }

  loadNotificationsPage(page) {
    this.$rootScope.showGlobalLoadingMask = true;
    return this.NotificationsService.getNotifications(page, this.maxDisplay).then((data)=>{
      this.currentPage = data.number;
      this.allLoaded = !data.last;
      this.$rootScope.showGlobalLoadingMask = false;
      _.forEach(data.content, (notification)=>{
        notification.notification.text = this.decodeHtml(notification.notification.text);
        this.originalList.push(notification);
      });
      this.sortNotificationsIntoGroups();
    });
  }

  decodeHtml(html) {
    var txt = document.createElement('textarea');
    txt.innerHTML = html;
    return txt.value;
  }

  sortNotificationsIntoGroups() {

    let temp = _.sortBy(this.originalList, (content)=>{
      return content.notification.createdOn
    }).reverse();
    temp = _.groupBy(temp, (content)=>{
      let mDate = moment(content.notification.createdOn);
      content.mDate = mDate;
      return mDate.format('YYYY/MM/DD');
    });
    let groups = [];
    _.forEach(temp, (t, index)=>{
      groups.push({
        id: index,
        title: t[0].mDate.calendar(null, {
          sameDay: '[Today]',
          nextDay: '[Tomorrow]',
          nextWeek: 'dddd',
          lastDay: '[Yesterday]',
          lastWeek: '[Last] dddd',
          sameElse: 'dddd D MMMM'
        }),
        notifications: t
      });
    });
    this.notifcationsGroups = groups;
  }
  loadMore() {
    return this.loadNotificationsPage(this.currentPage + 1);
  }
  notificationDeleted() {
    this.originalList = [];
    this.loadNotificationsPage(0);
    this.MetadataService.fireMetadataUpdate();
  }

  anyNotificationSelected() {
    return this.getSelectedNotificationIds().length > 0;
  }

  getSelectedNotificationIds() {
    let notificationIdsToBeDeleted = [];
    _.forEach(this.notifcationsGroups, (group) => {
      _.forEach(group.notifications, (notification) => {
        if (notification.notification.isSelected) {
          notificationIdsToBeDeleted.push(notification.id)
        }
      });
    });
    return notificationIdsToBeDeleted;
  }

  deleteNotifications() {
    if(this.SessionService.getDoNotShowAgainDeleteNotificationModal()) {
      this._deleteNotifications();
    }else{
      this.DeleteNotificationModal.show().result.then((doNotShowAgain)=>{
        this.SessionService.setDoNotShowAgainDeleteNotificationModal(doNotShowAgain);
        this._deleteNotifications();
      });
    }
  }

  _deleteNotifications() {
    let notificationIdsToBeDeleted = this.getSelectedNotificationIds();
    this.NotificationsService.deleteNotifications(notificationIdsToBeDeleted).then(()=>{
      this.notificationDeleted();
    });
  }

}

NotificationsPageCtrl.$inject = ['$rootScope','NotificationsService', 'FeatureToggleService', 'MetadataService', 'SessionService', 'DeleteNotificationModal'];


angular.module('GLA')
  .component('notificationsPage', {
    templateUrl: 'scripts/pages/notifications/notifications-page.html',
    bindings: {

    },
    controller: NotificationsPageCtrl
  });
