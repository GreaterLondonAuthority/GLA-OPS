/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

GrantSourceService.$inject = ['$http', 'config','ReferenceDataService'];

function GrantSourceService($http, config, ReferenceDataService) {


  return {
    /**
     * Returns which grant source types are visible based on the template configuration
     * @param template
     * @returns {{showNilGrant: boolean, showRcgfGrant: *, showDpfGrant: *, showGrant: *}}
     */
    getSourceVisibilityConfig(template){
      let blockMetaData = this.getBlockMetaData(template);
      return ReferenceDataService.getAvailablePaymentSources().toPromise().then((resp) => {
        let allPaymentSources = resp;
        let sources = blockMetaData.paymentSources.length > 0 ? blockMetaData.paymentSources : blockMetaData.grantTypes;
        let config = {};
        _.forEach(sources, type => {
          let ps = _.find(allPaymentSources, ['name', type]);

          switch (ps.grantType) {
            case ('RCGF'):
              config.rcgf = ps;
              break;
            case ('DPF'):
              config.dpf = ps;
              break;
            case ('Grant'):
              config.grant = ps;
              break;
            default:
              console.log('Unrecognised grant type: ' + ps.grantType);
          }
          config.showRcgfGrant = config.rcgf;
          config.showDpfGrant = config.dpf;
          config.showGrant = config.grant;
          config.showNilGrant =  !blockMetaData.nilGrantHidden;

        });
        return config;
      });


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
