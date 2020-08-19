/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


const gla = angular.module('GLA');
// TODO named section-header2 to replace original section-header eventually. Should be easier to rename in org details only then everywhere else
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

gla.component('sectionHeader2', {
  templateUrl: 'scripts/components/section-header2/section-header.html',
  controller: SectionHeader,
  bindings: {
    subheader: '@',
    collapsed: '=?',
    onCollapseChange: '&',
    showBar: '<',
  },
  transclude: true
});

