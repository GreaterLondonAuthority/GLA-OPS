/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


//TODO move to OutputsService
import OutputsUtil from '../../project/outputs/OutputsUtil';
import DateUtil from '../../../util/DateUtil';

class OutputsChangeReport {
  constructor(OutputsService, ReportService) {
    this.OutputsService = OutputsService;
    this.ReportService = ReportService;
  }


  $onInit() {


    this.title = this.getTitle();
    let unitConfig = OutputsUtil.getUnitConfig();

    let comparableRows = this.getComparableOutputGroups(this.data.left, this.data.right);
    this.comparableOutputGroups = this.ReportService.groupComparableRows(comparableRows, 'financialYear', true);

    this.data.changes.addDeletions(comparableRows);


    this.outputsFields = [
      {
        field: 'outputType',
        label: 'OUTPUT TYPE',
        format(row){
          return row ? _.startCase(_.toLower(row.outputType)) : null;
        }
      },
      {
        field: 'category',
        label: 'CATEGORY'
      },
      {
        field: 'subcategory',
        label: 'SUB CATEGORY'
      },
      {
        field: 'valueType',
        label: 'VALUE',
        format(row){
          return row ? unitConfig[row.valueType].label : null;
        }
      },
      {
        field: 'forecast',
        label: 'FORECAST'
      },
      {
        field: 'actual',
        label: 'ACTUAL'
      },
      {
        field: 'total',
        label: 'TOTAL'
      }
    ];



  }

  getTitle(){
    let allYears = [this.data.left, this.data.right].reduce((allYears, block) => {
      return this.OutputsService.getAllFinancialYears(block);
    }, []);
    return this.OutputsService.getOutputsSummariesTitle(_.min(allYears), _.max(allYears));
  }

  getComparableOutputGroups(leftBlock, rightBlock) {
    let leftAndRightBlock = [leftBlock, rightBlock].map(block => {
      let summaries = (block || {}).outputSummaries || [];
      let subCategories = summaries.reduce((subCategories, group) => subCategories.concat(group.subcategories), []);
      return subCategories;
    });


    //Filter to identify left side tenure type row when you have the right side row
    let leftSideFilter = function (rightRow) {
      return {comparisonId: rightRow.comparisonId}
    };

    return this.ReportService.rowsToCompare(leftAndRightBlock[0], leftAndRightBlock[1], leftSideFilter);
  }

  groupTitle(year) {
    let financialYear = DateUtil.toFinancialYearString(year);
    return `Financial year: ${financialYear}`;
  }
}

OutputsChangeReport.$inject = ['OutputsService', 'ReportService'];

angular.module('GLA')
  .component('outputsChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/outputsChangeReport.html',
    controller: OutputsChangeReport
  });
