/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

RepeatingEntityService.$inject = ['$http', 'config'];

function RepeatingEntityService($http, config) {

  return {

    create(entityPath, projectId, blockId, entity){
      return $http.post(`${config.basePath}/${entityPath}/project/${projectId}/block/${blockId}/item/`, entity)
    },

    update(entityPath, projectId, blockId, entity){
      return $http.put(`${config.basePath}/${entityPath}/project/${projectId}/block/${blockId}/item/`, entity)
    },

    delete(entityPath, projectId, blockId, entity) {
      return $http.delete(`${config.basePath}/${entityPath}/project/${projectId}/block/${blockId}/item/${entity.id}`)
    },

    hasEntityAnyData(entity){
      if(!entity){
        return false;
      }
      return _.some(Object.keys(entity), key => entity[key] != null && key !== '$$hashKey' && entity[key].toString().trim());
    }
  };
}

angular.module('GLA')
  .service('RepeatingEntityService', RepeatingEntityService);
