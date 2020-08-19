/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class RiskRatingCtrl {
  constructor($injector){
  }
}

RiskRatingCtrl.$inject = ['$injector'];

angular.module('GLA')
  .component('riskRating', {
    bindings: {
      overallRating: '=',
      overallRatings: '<',
      readOnly: '<',
      blockData: '<',
      autoSave: '&'

    },
    templateUrl: 'scripts/pages/project/risks/risk-rating/riskRating.html',
    controller: RiskRatingCtrl
});
