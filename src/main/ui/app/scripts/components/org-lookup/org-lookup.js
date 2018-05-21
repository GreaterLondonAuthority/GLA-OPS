/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');



function OrgLookup(OrganisationService) {



  return {
    link(scope, element, attr) {

      function searchOrg() {
        let orgCode = scope.orgCode;
        if (scope.orgCode) {
          OrganisationService.lookupOrgNameByCode(scope.orgCode)
            .then((response) => {
              //Don't update if request is out of order
              if (orgCode === scope.orgCode) {
                if (!response || response.status != 200) {
                  scope.orgName = null;
                } else {
                  scope.orgName = response.data;
                }
              }
            })
            .catch((err) => {
              if (orgCode === scope.orgCode) {
                scope.orgName = null;
              }
            });
        } else {
          scope.orgName = null;
        }
      }



      scope.$watch('orgCode', (newOrgCode) => {
        searchOrg(newOrgCode);
      });
    },

    scope: {
      'orgName': '=orgLookup',
      'orgCode': '=ngModel'
    }
  };
}

OrgLookup.$inject = ['OrganisationService'];

angular.module('GLA')
  .directive('orgLookup', OrgLookup);
