/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class ReportSection {
  constructor($location, $anchorScroll) {
    this.$location = $location;
    this.$anchorScroll = $anchorScroll;
    this.$anchorScroll.yOffset = 50;
  }

 /* toggleBlock(item) {
    item.expanded = !item.expanded;
    // this.showCollapseAll = this.blocksToCompare.some((item) => item.expanded);
  }*/

  jumpTo(id) {
    this.$location.hash(id);
    this.$anchorScroll();
  }
}

ReportSection.$inject = ['$location', '$anchorScroll'];


gla.component('reportSection', {
  templateUrl: 'scripts/components/report-section/reportSection.html',
  controller: ReportSection,
  transclude: true,
  bindings: {
    item: '<',
    nextItem: '<?',
    showChanges: '<?',
    onCollapseChange: '&'
  }
});

