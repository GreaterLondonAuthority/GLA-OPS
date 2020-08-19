/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
import './mp-field/mpField'

const gla = angular.module('GLA');

gla.component('multiPanel', {
  templateUrl: 'scripts/components/multi-panel/multiPanel.html',
  transclude: true,
  bindings: {
    editable: '<?',
    onEdit: '&'
  }
});

