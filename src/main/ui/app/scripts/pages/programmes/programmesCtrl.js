/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

ProgrammesCtrl.$inject = ['UserService', 'ProgrammeService', '$scope', '$state'];

function ProgrammesCtrl(UserService, ProgrammeService, $scope, $state) {
  this.user = UserService.currentUser();

  ProgrammeService.getProgrammes()
    .then(resp => {
      $scope.programmes = resp.data;
    });

  $scope.getDetails = function ( programmeId ) {
    $state.go( 'programme', { 'programmeId': programmeId });
  };
}

angular.module('GLA')
  .controller('ProgrammesCtrl', ProgrammesCtrl);
