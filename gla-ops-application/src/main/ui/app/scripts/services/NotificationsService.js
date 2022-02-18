/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

NotificationsService.$inject = ['$http', 'config'];

const STATUS_DELETED = 'Deleted';

function NotificationsService($http, config) {
  return {

    /**
     * Return grouped payments by status. Defaults to ALL
     * @param status [PENDING | AUTHORISED | ALL]
     */
    getNotifications(page, size){
      const cfg = {
        params: {
          status: status || 'ALL'
        }
      };
      return $http.get(`${config.basePath}/notifications?page=${page}&size=${size}&sort=createdOn,desc`, cfg).then((resp) => {
        return resp.data;
      });
    },

    markNotificationAsRead(id){
      return $http({
        url: `${config.basePath}/notifications/${id}/read`,
        method: 'PUT'
      })
    },

    deleteNotification(id){
      return $http({
        url: `${config.basePath}/notifications/${id}/status`,
        method: 'PUT',
        data: STATUS_DELETED
      })
    },

    deleteNotifications(ids){
      let deleteNotificationsRequest = [];
      _.forEach(ids, (id) => {
        deleteNotificationsRequest.push({
          id: id,
          status: STATUS_DELETED
        })
      });
      return $http({
        url: `${config.basePath}/notifications/statuses`,
        method: 'PUT',
        data: deleteNotificationsRequest
      })
    },

    getWatched(entityType) {
      return $http({
        url: `${config.basePath}/subscriptions`,
        method: 'GET',
        params: {
          entityType: entityType
        }
      })
    },

    //TODO remove
    watchEntity(userName, entityId, entityType){
      return $http({
        url: `${config.basePath}/subscriptions`,
        method: 'POST',
        data: {
          username: userName,
          entityType: entityType,
          entityId: entityId
        }
      })
    },

    //TODO remove
    unwatchEntity(username, projectId, entityType) {
      return $http({
        url: `${config.basePath}/subscriptions/${username}/${entityType}/${projectId}`,
        method: 'DELETE'
      })
    },

    //TODO remove
    watchProject(username, projectId) {
      return this.watchEntity(username, projectId, 'project');
    },

    //TODO remove
    unwatchProject(username, projectId) {
      return this.unwatchEntity(username, projectId, 'project');
    },
    watchOrganisation(username, organisationId) {
      return this.watchEntity(username, organisationId, 'organisation');
    },

    unwatchOrganisation(username, organisationId) {
      return this.unwatchEntity(username, organisationId, 'organisation');
    },

    getAllNotifications(){
      return $http.get(`${config.basePath}/notificationTypes`);
    },

    getScheduledNotifications() {
      return $http.get(`${config.basePath}/scheduledNotifications`);
    },

    createScheduledNotification(notification) {
      return $http.post(`${config.basePath}/scheduledNotifications`, notification);
    },

    updateScheduledNotification(notification) {
      return $http.put(`${config.basePath}/scheduledNotifications/${notification.id}`, notification);
    },

    deleteScheduledNotification(notification) {
      return $http.delete(`${config.basePath}/scheduledNotifications/${notification.id}`);
    }

  };
}

angular.module('GLA').service('NotificationsService', NotificationsService);
