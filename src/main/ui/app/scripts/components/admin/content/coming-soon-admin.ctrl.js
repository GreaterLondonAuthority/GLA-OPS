/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

angular.module('GLA')
  .controller('CSoonAdminCtrl', ['ConfigurationService', function (ConfigurationService) {
    var csoon = this;
    csoon.statusMessage = false;

    csoon.submit = function () {
      ConfigurationService.updateComingSoonMessage({
        code: 'coming-soon',
        text: csoon.message
      }).then(function () {
        csoon.statusMessageText = 'Coming soon message updated';
        csoon.statusMessage = true;
      });
    }
  }]);
