/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

GrantSourceService.$inject = ['$http', 'config'];

function GrantSourceService($http, config) {


  return {
    /**
     * Returns which grant source types are visible based on the template configuration
     * @param template
     * @returns {{showNilGrant: boolean, showRcgfGrant: *, showDpfGrant: *, showGrant: *}}
     */
    getSourceVisibilityConfig(template){
      let blockMetaData = this.getBlockMetaData(template);
      return {
        showNilGrant: !blockMetaData.nilGrantHidden,
        showRcgfGrant: this.isGrantSourceAvailable(blockMetaData.grantTypes, 'RCGF'),
        showDpfGrant: this.isGrantSourceAvailable(blockMetaData.grantTypes, 'DPF'),
        showGrant: this.isGrantSourceAvailable(blockMetaData.grantTypes, 'Grant')
      };
    },
    getBlockMetaData(template){
      return _.find(template.blocksEnabled, {block: 'GrantSource'});
    },
    getAssociatedVisibilityConfig(data, config){
      config = config || {};
      config.showAssociated = (data.left && data.left.associatedProject) || (data.right && data.right.associatedProject);
      return config;
    },

    isGrantSourceAvailable(templateGrantTypes, grantType) {
      if(templateGrantTypes && templateGrantTypes.length){
        return templateGrantTypes.indexOf(grantType) !== -1;
      }
      return true;
    },
    getGrantSourceBlock(projectId){
      return $http({
        url: `${config.basePath}/projects/${projectId}/grant/`,
        method: 'GET'
      });
    }
  };
}

angular.module('GLA')
  .service('GrantSourceService', GrantSourceService);
