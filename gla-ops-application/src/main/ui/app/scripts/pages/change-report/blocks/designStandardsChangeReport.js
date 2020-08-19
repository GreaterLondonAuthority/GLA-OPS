/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class DesignStandardsChangeReport {
  constructor($rootScope, $scope, ReferenceDataService, OrganisationGroupService, ProjectService) {
    
  }
}

DesignStandardsChangeReport.$inject = ['$rootScope', '$scope', 'ReferenceDataService', 'OrganisationGroupService', 'ProjectService'];

angular.module('GLA')
  .component('designStandardsChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/designStandardsChangeReport.html',
    controller: DesignStandardsChangeReport  });
