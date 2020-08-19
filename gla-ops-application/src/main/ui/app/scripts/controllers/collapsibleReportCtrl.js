/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

/**
 * Base class for change/summary reports
 */
class CollapsibleReportCtrl {
  constructor($injector) {
    this.$log = $injector.get('$log');
    this.$state = $injector.get('$state');
    this.$stateParams = $injector.get('$stateParams');
    this.ReportService = $injector.get('ReportService');
    this.ProjectService = $injector.get('ProjectService');
    this.UserService = $injector.get('UserService');
    this.$location = $injector.get('$location');
    this.$anchorScroll = $injector.get('$anchorScroll');
    this.$anchorScroll.yOffset = 50;
    this.showCollapseAll = true;
  }


  toggleBlock(item, allBlocks) {
    item.expanded = !item.expanded;
    if(allBlocks) {
      this.showCollapseAll = (allBlocks || []).some((item) => item.expanded);
    }
  }

  toggleAllSections(allBlocks, additionalSections) {
    let allSections = additionalSections? allBlocks.concat(additionalSections) : allBlocks;
    (allSections || []).forEach(item => {
      item.expanded = !this.showCollapseAll;
    });

    this.showCollapseAll = !this.showCollapseAll;
  }

  jumpTo(id) {
    this.$location.hash(id);
    this.$anchorScroll();
  }

  hasTables(item) {
    let blockTypes = ['Grant', 'Milestones', 'Risk', 'OutputsBlock', 'Budgets', 'ReceiptsBlock'];
    for (let i = 0; i < blockTypes.length; i++) {
      if (item.type.indexOf(blockTypes[i]) > -1) {
        return true;
      }
    }
    return false;
  }
}

CollapsibleReportCtrl.$inject = ['$injector'];


angular.module('GLA').controller('CollapsibleReportCtrl', CollapsibleReportCtrl);

export default CollapsibleReportCtrl;


