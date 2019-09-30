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
      config.showAssociated = (data.left && data.context.project.left.associatedProjectsEnabled) || (data.right && data.context.project.right.associatedProjectsEnabled);
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
    },

    getAssociatedProjectConfig(block){
      let showMarker;
      let enableMarker;

      if(!block.associatedProject && !block.associatedProjectFlagUpdatable){
        showMarker = false;
        enableMarker = false;
      } else if(block.associatedProject && !block.associatedProjectFlagUpdatable){
        showMarker = true;
        enableMarker = false;
      } else {
        showMarker = true;
        enableMarker = true;
      }

      return {
        showMarker,
        enableMarker
      };
    },

    getTotal(grantSourceBlock){
      grantSourceBlock = grantSourceBlock || {};
      return +(grantSourceBlock.grantValue || 0) + +(grantSourceBlock.recycledCapitalGrantFundValue || 0) + +(grantSourceBlock.disposalProceedsFundValue || 0)  + +(grantSourceBlock.strategicFunding || 0)
    }
  };
}

angular.module('GLA')
  .service('GrantSourceService', GrantSourceService);
