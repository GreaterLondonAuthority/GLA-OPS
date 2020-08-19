/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


var gla = angular.module('GLA');

class OverviewBlockCtrl {
  constructor(){
    // this.number = 1;
    // this.name = 'NAME';
    // this.status = 'STATUS';
    // this.banner = 'BANNER';
    // this.icon = 'glyphicon-ok';
    // this.state='valid';
    // this.newBlock=true;
  }
}

OverviewBlockCtrl.$inject = [];

gla.component('overviewBlock', {
  controller: OverviewBlockCtrl,
  bindings: {
    number: '<?',
    name: '<?',
    status: '<?',
    banner: '<?',
    icon: '<?',
    blockState: '<?',
    newBlock: '<?',
    color: '<?'
  },
  templateUrl: 'scripts/components/common/overview-block/overviewBlock.html',
  transclude: true
});
