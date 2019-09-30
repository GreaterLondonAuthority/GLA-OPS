/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


angular.module('GLA')
  .component('mpField', {
    templateUrl: 'scripts/components/multi-panel/mp-field/mpField.html',
    transclude: true,
    bindings: {
      label: '<',
      mpDefault: '<?'
    }
  });
