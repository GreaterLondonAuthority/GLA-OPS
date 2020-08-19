/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


LockingService.$inject = ['$http', 'config', 'UserService'];

function LockingService($http, config, UserService) {

  return {
    lock(type, entityId) {
      return $http.post(`${config.basePath}/locks?entityType=${type}&entityId=${entityId}`);
    },

    unlock(type, entityId) {
      return $http.delete(`${config.basePath}/locks?entityType=${type}&entityId=${entityId}`);
    },

    getLockDetails(block) {
      if (block.locked) {
        return {
          username: block.lockedByUsername,
          firstName: block.lockedByFirstName,
          lastName: block.lockedByLastName,
        }
      }
      return null;
    },

    isBlockEditable(block){

      if(!_.includes(block.allowedActions, 'EDIT')){
        return false;
      }

      let lock = this.getLockDetails(block);
      return !lock || this.isLockedByCurrentUser(block);
    },

    isLockedByCurrentUser(block){
      let lock = this.getLockDetails(block);
      return lock && lock.username === UserService.currentUser().username;
    }
  };
}

angular.module('GLA')
  .service('LockingService', LockingService);
