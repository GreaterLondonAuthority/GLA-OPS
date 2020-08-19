/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class NavigationCircles {
  constructor() {
  }

  $onInit(){
    //Could be calculated based on items count
    this.itemWidth = (100/this.items.length) + '%';
  }
}

gla.component('navigationCircles', {
  templateUrl: 'scripts/components/navigation-circles/navigationCircles.html',
  controller: NavigationCircles,
  bindings: {
    items: '<'
  }
});

