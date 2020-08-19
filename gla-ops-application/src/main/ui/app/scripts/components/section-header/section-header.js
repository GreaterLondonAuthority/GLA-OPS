/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


const gla = angular.module('GLA');

class SectionHeader {
  constructor($element) {
    this.$element = $element;
  }

  $onInit() {
    this.isCollapsible = this.$element[0].hasAttribute('collapsed');
  }

  onSectionClick(){
    this.collapsed = !this.collapsed;
    this.onCollapseChange({
      $event: this.collapsed
    })
  }
}

SectionHeader.$inject = ['$element'];

gla.component('sectionHeader', {
  templateUrl: 'scripts/components/section-header/section-header.html',
  controller: SectionHeader,
  bindings: {
    subheader: '@',
    level: '@',
    collapsed: '=?',
    onCollapseChange: '&'
  },
  transclude: true
});

