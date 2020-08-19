/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class NotificationsGroupCtrl {
  constructor() {

  }

  $onInit() {

  }

}

NotificationsGroupCtrl.$inject = [];


angular.module('GLA')
  .component('notificationsGroup', {
    templateUrl: 'scripts/pages/notifications/notifications-group.html',
    bindings: {
      notificationsGroup: '<',
      title: '<',
      notificationDeleted: '&'
    },
    controller: NotificationsGroupCtrl
  });
