/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

MetadataService.$inject = ['$http', 'UserService', 'config'];

function MetadataService($http, UserService, config) {
  return {
    listeners: [],
    subscribe: function(fn) {
      this.listeners.push(fn);
    },

    unsubscribe: function(fn) {
      this.listeners = this.listeners.filter(
        function(item) {
          if (item !== fn) {
            return item;
          }
        }
      );
    },

    fire: function(o, thisObj) {
      var scope = thisObj || window;
      this.listeners.forEach(function(item) {
        item.call(scope, o);
      });
    },

    fireMetadataUpdate() {
      let self = this;
      let user = UserService.currentUser();
      // let username = UserService.currentUser().username;
      if(user && user.loggedOn){
        $http({
          url: `${config.basePath}/metadata/`,
          method: 'GET',
          ignoreErrors: {403:true}
        }).then(rsp => {
          self.fire(rsp.data);
        },(rsp)=> console.log('failed to get meta data, ', rsp && rsp.message));
      } else {
        this.fire({loggedOut: true});
      }
    }
  };
}

/*
return $http({
        url: config.basePath + `/checkCurrentUserAccess?entityType=${entityType}&entityId=${entityId}`,
        method: 'GET',
        ignoreErrors: ignoreErrors
      })
 */

angular.module('GLA')
  .service('MetadataService', MetadataService);
