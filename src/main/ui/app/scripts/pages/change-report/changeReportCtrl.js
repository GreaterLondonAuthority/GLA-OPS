/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class ChangeReportCtrl {
  constructor($state, $location, $log, $anchorScroll, ReportService, ProjectService) {
    this.$log = $log;
    this.$state = $state;
    this.$location = $location;
    this.$anchorScroll = $anchorScroll;
    this.ReportService = ReportService;
    this.$anchorScroll.yOffset = 50;
    this.showCollapseAll = true;
    this.ReportService.setHalfWidth();
    this.comparisonDate = new Date();
    if (this.$state.params.comparisonDate) {
      this.comparisonDate = moment(this.$state.params.comparisonDate).subtract(1, 'Month').toDate();
    }
    this.subStatusText = ProjectService.subStatusText(this.latestProject);

    let dateFirstActive;
    for(var i=0;i<this.projectHistory.length;i++) {
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


    this.typeConfig = {
      ProjectDetailsBlock: {
        supported: true
      },
      ProjectBudgetsBlock: {
        supported: true
      },
      CalculateGrantBlock: {
        supported: true
      },
      NegotiatedGrantBlock: {
        supported: true
      },
      DeveloperLedGrantBlock: {
        supported: true
      },
      IndicativeGrantBlock: {
        supported: true
      },
      GrantSourceBlock: {
        supported: true
      },
      DesignStandardsBlock: {
        supported: true
      },
      ProjectRisksBlock: {
        supported: true
      },
      ProjectQuestionsBlock: {
        supported: true
      },
      OutputsBlock: {
        supported: true
      },
      ReceiptsBlock: {
        supported: true
      },
      UnitDetailsBlock: {
        supported: true
      },
      ProjectMilestonesBlock: {
        supported: true
      }
    };

    let approvalTimes = this.latestProject.projectBlocksSorted.map(item => {
      return item.approvalTime;
    });
    //TODO probably should use last modified date on lastApprovedProject
    this.lastApproved = _.max(approvalTimes);
    this.numberOfUnapprovedBlocks = _.filter(this.latestProject.projectBlocksSorted, {blockStatus: 'UNAPPROVED'}).length;
    this.blocksToCompare = [];

    let latestBlocks = this.latestProject.projectBlocksSorted;
    let lastApprovedBlocks = this.lastApprovedProject.projectBlocksSorted;
    this.blocksToCompare = [];

    /**
     * Pre-populate with project history
     */
    this.latestComment = undefined;
    _.forEach(this.projectHistory, (item) => {
      this.latestComment = this.latestComment || (item.comments ? item : undefined);
    });


    for (let i = 0; i < latestBlocks.length; i++) {
      let type = (latestBlocks[i] || lastApprovedBlocks[i]).type;
      // if(this.typeConfig[type].supported){
      let left = lastApprovedBlocks[i] || {};
      let right = latestBlocks[i] || {};

      let hasUnapproved = right.blockStatus !== 'LAST_APPROVED';
      let item = {
        config: this.typeConfig[type],
        left: lastApprovedBlocks[i],
        right: hasUnapproved ? latestBlocks[i] : undefined,
        type: latestBlocks[i].type,
        blockDisplayName: latestBlocks[i].blockDisplayName,
        blockDisplayCls: latestBlocks[i].blockDisplayName.toLowerCase().split(' ').join('-'),
        id: latestBlocks[i].id,
        expanded: true,
        context: {
          project: {
            left: this.lastApprovedProject,
            right: this.latestProject
          },
          template: this.template,
          currentFinancialYear: this.currentFinancialYear
        }
      };
      //Add derived properties
      item.versionObj = {
        left: {
          versionString: 'There is no approved version of this block'
        }
      };
      if (item.left) {
        item.versionObj.left.versionString = this.ReportService.version(item.left, this.lastApprovedProject.autoApproval);
      }

      if (item.right) {
        item.versionObj.right = {
          versionString: this.ReportService.version(item.right, this.latestProject.autoApproval) || 'New unedited block'
        };
      }
      item.changes = this.ReportService.changeTracker(item);


      this.blocksToCompare.push(item);
      // }
    }
    if (this.latestComment) {
      this.transitionMap = ProjectService.getTransitionMap();
      this.blocksToCompare.push({
        type: 'ProjectHistory',
        blockDisplayName: 'Project History',
        blockDisplayCls: 'project-history-block',
        id: 'projecthistoryId',
        expanded: true,
        latestComment: this.latestComment
      });
    }


    // this.$log.log('AAAAA', this.latestProject, this.lastApprovedProject, this.template);
    this.data = {
      left: this.lastApprovedProject,
      right: this.latestProject
    };

    this.showDatePicker = this.isDatePickerEnabled && this.latestProject.autoApproval && lastApprovedBlocks.length && latestBlocks.length;
  }

  onBack() {
    this.$state.go('project.overview', {
      projectId: this.latestProject.id,
    }, {
      reload: true
    });
  }

  toggleAllSections(item) {
    this.blocksToCompare.forEach(item => {
      item.expanded = !this.showCollapseAll;
    });
    if (this.latestComment) {
      this.latestComment.expanded = !this.showCollapseAll;
    }
    this.showCollapseAll = !this.showCollapseAll;
  }

  toggleBlock(item) {
    item.expanded = !item.expanded;
    this.showCollapseAll = this.blocksToCompare.some((item) => item.expanded);
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

  onComparisonDateChange(reportDate) {
    let comparisonDate = moment(reportDate).add(1, 'month').format('YYYY-MM-DD');
    this.$state.go(this.$state.current, {comparisonDate: comparisonDate}, {reload: true});
  }
}

ChangeReportCtrl.$inject = ['$state', '$location', '$log', '$anchorScroll', 'ReportService', 'ProjectService'];


// angular.module('GLA')
//   .controller('ChangeReportCtrl', ChangeReportCtrl);

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
    },
    controller: ChangeReportCtrl
  });
