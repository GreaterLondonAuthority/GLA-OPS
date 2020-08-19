/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class RiskAndIssuesChangeReport {
  constructor(RisksService, ReportService) {
    this.RisksService = RisksService;
    this.ReportService = ReportService;
  }

  $onInit() {
    this.overallRatings = this.RisksService.getOverallRatings();

    [this.data.left, this.data.right].forEach(block => {
      if (block) {
        let overallRating = _.find(this.overallRatings, {id: block.rating});
        if (overallRating) {
          block.overallRating = `${overallRating.color.toUpperCase()} - ${overallRating.description}`;
        }
      }
    });


    let issueImpactLevelsDisplayMap = this.RisksService.getIssueImpactLevelsDisplayMap();

    this.risksToCompare = this.risksToCompare(this.data.left, this.data.right);
    this.issuesToCompare = this.issuesToCompare(this.data.left, this.data.right);


    this.riskFields = [
      {
        field: 'title',
        label: 'TITLE',
      },
      {
        field: 'description',
        label: 'DESCRIPTION OF CAUSE AND IMPACT',
      },
      {
        field: 'riskCategory.displayValue',
        label: 'CATEGORY',
        changeAttribute: 'riskCategory'
      },
      {
        field: 'computedInitialRating',
        label: 'INITIAL RISK RATING',
        changeAttribute: 'computedInitialRating',
        format(row){
          return row ? `${row.computedInitialRating} ${row.initialRiskLevel.level}` : null;
        }
      },
      {
        field: 'computedResidualRating',
        label: 'RESIDUAL RISK RATING',
        changeAttribute: 'computedResidualRating',

        format(row){
          if(row){
            if(row.computedResidualRating && row.residualRiskLevel && row.residualRiskLevel.level){
              return `${row.computedResidualRating} ${row.residualRiskLevel.level}`;
            }else{
              return 'N/A';
            }
          }
          return null;
        }
      },
      {
        field: 'status',
        label: 'STATUS'
      }
    ];

    this.riskMitigations = [
      {
        field: 'action',
        label: 'MITIGATION',
      },
      {
        field: 'owner',
        label: 'OWNER',
      }
    ];

    this.issueActions = [
      {
        field: 'action',
        label: 'ACTIONS',
      },
      {
        field: 'owner',
        label: 'OWNER',
      }
    ];

    this.issueFields = [
      {
        field: 'title',
        label: 'TITLE',
      },
      {
        field: 'description',
        label: 'DESCRIPTION OF ISSUES AND IMPACT',
      },
      {
        field: 'computedInitialRating',
        label: 'IMPACT LEVEL',
        changeAttribute: 'initialImpactRating',
        format(row){
          return row ? issueImpactLevelsDisplayMap[row.initialImpactRating].displayValue : null;
        }
      },
      {
        field: 'status',
        label: 'STATUS',
      }
    ];



    this.risksToCompare.forEach(row => this.subrows(row, true));
    this.issuesToCompare.forEach(row => this.subrows(row, false));

    if(this.data.left && this.data.right){
      this.data.changes.addDeletions(this.risksToCompare);
      this.data.changes.addDeletions(this.issuesToCompare);
      this.risksToCompare.concat(this.issuesToCompare).forEach(item => {
        this.data.changes.addDeletions(item.subrows);
      })
    }
  }

  removeClosedFilter(row) {
    const CLOSED = 'Closed';
    return !(row.left && row.right && row.left.status === CLOSED && row.right.status === CLOSED)
  }

  risksToCompare(leftBlock, rightBlock){
    let leftItems = this.RisksService.getRisks(leftBlock);
    let rightItems = this.RisksService.getRisks(rightBlock);


    //Filter to identify left side tenure type row when you have the right side row
    let leftSideFilter = function (rightRow) {
      return {comparisonId: rightRow.comparisonId}
    };

    let rowsToCompare =  this.ReportService.rowsToCompare(leftItems, rightItems, leftSideFilter);
    return _.filter(rowsToCompare, this.removeClosedFilter);
  }

  issuesToCompare(leftBlock, rightBlock){
    let leftItems = this.RisksService.getIssues(leftBlock);
    let rightItems = this.RisksService.getIssues(rightBlock);

    //Filter to identify left side tenure type row when you have the right side row
    let leftSideFilter = function (rightRow) {
      return {comparisonId: rightRow.comparisonId}
    };

    let rowsToCompare =  this.ReportService.rowsToCompare(leftItems, rightItems, leftSideFilter);
    return _.filter(rowsToCompare, this.removeClosedFilter);
  }

  /**
   * Creates a comparable array of actions rows <{left, right}>
   * And sets it as a subrows property which is then rendered by change-report-table-rows component
   * @param row comparable row <{left, right}> of risk/issue
   * @param isRisk is it risk or issue
   */
  subrows(row, isRisk){
      let leftItems = (row.left || {}).actions || [];
      let rightItems = (row.right || {}).actions || [];

      //Filter to identify left side tenure type row when you have the right side row
      let leftSideFilter = function (rightRow) {
        return {comparisonId: rightRow.comparisonId}
      };

      row.subrows =  this.ReportService.rowsToCompare(leftItems, rightItems, leftSideFilter);
      row.subrowFields = (isRisk? this.riskMitigations : this.issueActions);
  }
}

RiskAndIssuesChangeReport.$inject = ['RisksService', 'ReportService'];

angular.module('GLA')
  .component('riskAndIssuesChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/riskAndIssuesChangeReport.html',
    controller: RiskAndIssuesChangeReport
  });
