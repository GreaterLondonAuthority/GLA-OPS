/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class EntitiesListItem {

}

gla.component('entitiesListItem', {
  templateUrl: 'scripts/components/entities-list/entities-list-item/entitiesListItem.html',
  controller: EntitiesListItem,
  transclude: true,
  require: {
    parentCtrl: '^^entitiesList'
  },
  bindings: {
    entity: '<'
  },
});

