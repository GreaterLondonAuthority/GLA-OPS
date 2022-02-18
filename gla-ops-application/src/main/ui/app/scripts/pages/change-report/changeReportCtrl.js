/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


import CollapsibleReportCtr from '../../controllers/collapsibleReportCtrl.js';


class ChangeReportCtrl extends CollapsibleReportCtr {
  constructor($injector, GlaReportService) {
    super($injector);
    this.GlaReportService = GlaReportService;

    this.ReportService.setHalfWidth();
    this.GlaReportService.setHalfWidth();
  }

  $onInit(){
    this.comparisonDate = new Date();
    if (this.$state.params.comparisonDate) {
      this.comparisonDate = moment(this.$state.params.comparisonDate).subtract(1, 'Month').toDate();
    }

    let dateFirstActive;
    for(let i=0;i<this.projectHistory.length;i++) {
      if(this.projectHistory[i].transition === 'Approved') {
        dateFirstActive = this.projectHistory[i].createdOn;
      }
    }

    this.dateOptions = {
      formatYear: 'yyyy',
      formatMonth: 'MMM',
      minMode: 'month',
      maxMode: 'month',
      yearColumns: 3,
      // when firstApproved is null? - shouldn't be null after SQL update.
      minDate: new Date(dateFirstActive),
      maxDate: new Date(),
      datepickerMode: 'month'
    };


    let approvalTimes = this.latestProject.projectBlocksSorted.map(item => {
      return item.approvalTime;
    });
    //TODO probably should use last modified date on lastApprovedProject
    this.lastApproved = _.max(approvalTimes);
    this.numberOfUnapprovedBlocks = _.filter(this.latestProject.projectBlocksSorted, {blockStatus: 'UNAPPROVED'}).length;



    /**
     * Pre-populate with project history
     */
    this.latestComment = undefined;
    _.forEach(this.projectHistory, (item) => {
      this.latestComment = this.latestComment || (item.comments ? item : undefined);
    });

    this.blocksToCompare = this.ReportService.getBlocksToCompare(this.latestProject, this.lastApprovedProject, this.template, this.currentFinancialYear);

    if (this.latestComment) {
      this.transitionMap = this.ProjectService.getTransitionMap();
      this.blocksToCompare.push({
        type: 'ProjectHistory',
        blockDisplayName: 'Project History',
        blockDisplayCls: 'project-history-block',
        id: 'projecthistoryId',
        expanded: true,
        latestComment: this.latestComment
      });
    }

    if ((this.latestProject.internalBlocksSorted || []).length && this.UserService.hasPermission('proj.view.internal.blocks')) {
      this.blocksToCompare.push({
        type: 'InternalBlocks',
        blockDisplayName: 'GLA governance activities',
        blockDisplayCls: 'internal-blocks',
        id: 'internal-blocks-id',
        expanded: true
      });
    }

    this.data = {
      left: this.lastApprovedProject,
      right: this.latestProject
    };

    this.showDatePicker = this.isDatePickerEnabled && !this.latestProject.stateModel.approvalRequired && this.lastApprovedProject.projectBlocksSorted.length && this.latestProject.projectBlocksSorted.length;
  }

  onBack() {
    this.$state.go('project-overview', {
      projectId: this.latestProject.id,
    }, {
      reload: true
    });
  }

  onComparisonDateChange(reportDate) {
    let comparisonDate = moment(reportDate).add(1, 'month').format('YYYY-MM-DD');
    this.$state.go(this.$state.current, {comparisonDate: comparisonDate}, {reload: true});
  }
}

ChangeReportCtrl.$inject = ['$injector', 'GlaReportService'];


angular.module('GLA')
  .component('changeReportPage', {
    templateUrl: 'scripts/pages/change-report/changeReport.html',
    bindings: {
      latestProject: '<',
      lastApprovedProject: '<',
      template: '<',
      projectHistory: '<',
      currentFinancialYear: '<',
      isDatePickerEnabled: '<?',
      internalRiskComments: '<?',
    },
    controller: ChangeReportCtrl
  });
