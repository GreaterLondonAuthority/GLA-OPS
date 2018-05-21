/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class MilestonesChangeReport {
  constructor(ReportService, $log) {
    this.ReportService = ReportService;
    this.$log = $log;
  }

  $onInit() {
    const template = this.data.context.template;

    //Processing route can not be changed
    let milestonesExtraData = _.find(template.blocksEnabled, {block: 'Milestones'});
    this.processingRoutes = milestonesExtraData.processingRoutes;
    this.processingRouteId = (this.data.left && this.data.left.processingRouteId) || (this.data.right && this.data.right.processingRouteId);

    if(!this.processingRouteId && milestonesExtraData.defaultProcessingRoute){
      this.processingRouteId = milestonesExtraData.defaultProcessingRoute.id;
    }

    if (this.processingRouteId) {
      this.processingRoute = _.find(milestonesExtraData.processingRoutes, {
        id: this.processingRouteId
      });
    }

    let leftMilestones = (this.data.left ? this.data.left.milestones : []) || [];
    let rightMilestones = (this.data.right ? this.data.right.milestones : []) || [];

    this.allowMonetaryMilestones = (leftMilestones).concat(rightMilestones).some(m => m.monetary) ;
    this.showNaColumn = (leftMilestones).concat(rightMilestones).some(m => m.naSelectable) ;
    this.monetarySplitTitle = template.monetarySplitTitle || 'GRANT %';
    this.isMonetaryValueType = template.milestoneType === 'MonetaryValue';



    this.milestonesToCompare = [];

    for (let i = 0; i < rightMilestones.length; i++) {
      let matchFilter = {'externalId': rightMilestones[i].externalId};
      if (!rightMilestones[i].externalId) {
        matchFilter = {'summary': rightMilestones[i].summary};
      }

      let leftMilestone = _.find(leftMilestones, matchFilter);
      if(leftMilestone) {
        _.remove(leftMilestones, leftMilestone);
      }



      this.milestonesToCompare.push({
        left: leftMilestone,
        right: rightMilestones[i]
      });
    }

    leftMilestones.forEach(m => {
      this.milestonesToCompare.push({
        left: m,
        right: null
      })
    });



    if(this.data.left && this.data.right){
      this.data.changes.addDeletions(this.milestonesToCompare);
    }

    function defaultNAValue(milestone){
      if (milestone) {
        if (milestone.notApplicable) {
          return 'N/A';
        } else {
          return '';
        }
      }
    }

    let showNaColumn = this.showNaColumn;

    this.fields = [
      {
        field: 'summary',
        label: 'MILESTONE',
      }, {
        field: 'notApplicable',
        label: 'N/A',
        hidden: !this.showNaColumn,
        format(milestone){
          if (milestone) {
            if (milestone.notApplicable) {
              return 'N/A';
            } else {
              return '';
            }
          }
        }
      }, {
        field: 'milestoneDate',
        label: 'DATE',
        format: 'date',
        defaultValue: defaultNAValue,
      }, {
        hidden: !this.isMonetaryValueType,
        field: 'description',
        label: 'DESCRIPTION',
        cls: 'multiline-text'
      }, {
        hidden: !this.isMonetaryValueType,
        field: 'monetaryValue',
        label: 'VALUE £',
        format: 'number'
      }, {
        hidden: !this.allowMonetaryMilestones || this.isMonetaryValueType,
        field: 'monetarySplit',
        label: this.monetarySplitTitle,
        format(milestone){
          //Milestone is null when not shown on left of right side
          if(milestone) {
            if (!milestone.monetary) {
              return 'N/A'
            } else {
              return milestone.monetarySplit
            }
          }
        }
      }, {
        field: 'milestoneStatus',
        label: 'STATUS',
        defaultValue: defaultNAValue
      }, {
        field: 'claimStatus',
        label: 'CLAIM STATUS',
        format(milestone){
          if (milestone) {
            if (milestone.notApplicable) {
              return 'N/A';
            } else {
              return milestone.claimStatus;
            }
          }
        },
        changeAttribute: !showNaColumn? null : (rightSide, bothSides) => {
          let left = (bothSides || {}).left;
          let right = (bothSides || {}).right;
          if(rightSide){
            let changeAttr =  (left && right && left.notApplicable != right.notApplicable)? `notApplicable`: `claimStatus`;
            return `${rightSide.comparisonId}:${changeAttr}`;
          }
          return null;
        }
      }
    ];

    this.attachmentFields = [
      {
        field: 'fileName',
        label: 'EVIDENCE',
        format: 'file'
      }
    ];

    this.milestonesToCompare.forEach(row => this.subrows(row));
    this.milestonesToCompare.forEach(item => {
      this.data.changes.addDeletions(item.subrows);
    });

    this.fields = _.filter(this.fields, f => {return !f.hidden });
  }

  /**
   * Creates a comparable array of attachment rows <{left, right}>
   * And sets it as a subrows property which is then rendered by change-report-table-rows component
   * @param row comparable row <{left, right}> of attachment
   */
  subrows(row){
    let leftItems = (row.left || {}).attachments || [];
    let rightItems = (row.right || {}).attachments || [];

    //Filter to identify left side tenure type row when you have the right side row
    let leftSideFilter = function (rightRow) {
      return {comparisonId: rightRow.comparisonId}
    };

    row.subrows =  this.ReportService.rowsToCompare(leftItems, rightItems, leftSideFilter);
    row.subrowFields = this.attachmentFields;
  }
}

MilestonesChangeReport.$inject = ['ReportService', '$log'];

angular.module('GLA')
  .component('milestonesChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/milestonesChangeReport.html',
    controller: MilestonesChangeReport
  });
