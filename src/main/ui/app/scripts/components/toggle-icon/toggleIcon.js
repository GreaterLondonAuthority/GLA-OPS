/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


const gla = angular.module('GLA');
class ToggleIcon {

  constructor($timeout){
    this.$timeout = $timeout;
  }

  $onInit(){
    this.collapsedIcon = this.icon === 'triangle'? 'glyphicon-triangle-bottom' : 'glyphicon-menu-right';
    this.expandedIcon = this.icon === 'triangle'? 'glyphicon-triangle-top' : 'glyphicon-menu-down';
  }

  onSectionClick(){
    this.collapsed = !this.collapsed;
    //Need $timeout to allow to finish digest cycle and make it propagate to collapsed: '='
    this.$timeout(()=> {
      this.onCollapseChange({
        $event: this.collapsed
      })
    });
  }
}

ToggleIcon.$inject = ['$timeout'];

gla.component('toggleIcon', {
  templateUrl: 'scripts/components/toggle-icon/toggleIcon.html',
  controller: ToggleIcon,
  bindings: {
    collapsed: '=?',
    onCollapseChange: '&',
    icon: '<'
  },
  transclude: true
});

