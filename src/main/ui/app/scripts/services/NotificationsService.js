/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

NotificationsService.$inject = ['$http', 'config'];

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
    updateNotificationStatus(id, status){
      return $http({
        url: `${config.basePath}/notifications/${id}/status`,
        method: 'PUT',
        data:  status
      })
    },

    watchProject(username, projectId) {
      return $http({
        url: `${config.basePath}/subscriptions`,
        method: 'POST',
        data: {
          username: username,
          entityType: 'project',
          entityId: projectId
        }
      })
    },

    unwatchProject(username, projectId) {
      return $http({
        url: `${config.basePath}/subscriptions/${username}/project/${projectId}`,
        method: 'DELETE'
      })
    }

  };
}

angular.module('GLA').service('NotificationsService', NotificationsService);
