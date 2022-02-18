/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


CommentsService.$inject = ['$http', 'config', 'orderByFilter'];

const INTERNAL_RISK_ENTITY_TYPE  = 'internalRiskBlock';

function CommentsService($http, config, orderByFilter) {

  return {
    getComments(params) {
      return $http.get(`${config.basePath}/comments`, {params});
    },

    saveComment(commentEntity) {
      return $http.post(`${config.basePath}/comments`, commentEntity);
    },

    getInternalComments(entityId, entityType){
      return this.getComments({
        entityType: entityType,
        entityId: entityId,
        size: 10000
      });
    },

    getInternalRiskComments(entityId){
      return this.getInternalComments(entityId, 'internalRiskBlock');
    },

    saveInternalComments(projectId, entityId, entityType, comment){
      return this.saveComment({
        entityType: entityType,
        projectId: projectId,
        entityId: entityId,
        comment: comment
      });
    }


  };
}

angular.module('GLA')
  .service('CommentsService', CommentsService);
