/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

SkillProfilesService.$inject = ['$http', 'config'];

function SkillProfilesService($http, config) {
  return {
    getSkillsPaymentProfiles(){
      return $http.get(`${config.basePath}/skills/paymentProfiles`);
    },

    createNewYearData(type){
      return $http.post(`${config.basePath}/skills/paymentProfiles/${type}`).then(rsp => rsp.data);
    },

    updateValue(value){
      return $http.put(`${config.basePath}/skills/paymentProfiles/${value.id}`, value).then(rsp => rsp.data);
    },

    deletePaymentProfilesByTypeAndYear(type, year) {
      return $http.delete(`${config.basePath}/skills/paymentProfiles/${type}/${year}`);
    },

    /**
     * Retrieve the current set academic year
     * @returns {Object} promise
     */
    getCurrentAcademicYear:
      () => {
        return $http({
          url: `${config.basePath}/skills/currentAcademicYear`,
          method: 'GET'
        });
      },

  };
}

angular.module('GLA')
  .service('SkillProfilesService', SkillProfilesService);
