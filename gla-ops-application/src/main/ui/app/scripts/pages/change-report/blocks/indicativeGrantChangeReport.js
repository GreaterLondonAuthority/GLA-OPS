/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class IndicativeGrantChangeReport {
  constructor(GrantService) {
    this.GrantService = GrantService;
  }

  $onInit() {
    let leftBlock = this.GrantService.enhanceIndicativeBlock(this.data.left);
    let rightBlock = this.GrantService.enhanceIndicativeBlock(this.data.right);
    this.reportData = this.GrantService.prepareReportData(leftBlock, rightBlock);
    this.tenuresFields = this.createTenureFields();
    this.totalsFields = this.createTotalsFields();
    this.summariesFields = this.createSummariesFields();
    this.sectionTitle = this.GrantService.indicativeGrantSectionTitle(this.data.context.template);


    this.addTitleToSummaries(this.reportData.summariesToCompare);
    this.summariesGroups = this.createSummariesGroups();
  }

  /**
   *  Adds 'year/next' title to summaries(tiles)
   */
  addTitleToSummaries(tiles) {
    (tiles || []).forEach(row => {
      ['left', 'right'].forEach(side => {
        if (row[side]) {
          row[side].title = this.getFinancialYear(row[side].year);
        }
      });
    });
  }

  createTenureFields() {
    function valueObject(row, yearObj){
      return row ? _.find(row.indicativeTenureValuesSorted, {year: yearObj.year}): null;
    }

    let tenuresFields = [{
      label: 'TENURE TYPE',
      field: 'tenureType.name'
    }];

    this.yearColumns().forEach(yearObj => {
      tenuresFields.push({
        label: this.getFinancialYear(yearObj.year),
        field: 'units',
        format(row){
          return row ? valueObject(row, yearObj).units : null;
        },

        changeAttribute(row){
          let valueObj = valueObject(row, yearObj);
          return valueObj? `${row.comparisonId}:${valueObj.comparisonId}:units`: null;
        },

        hide(comparableRow){
          let tenureTypeYears = (comparableRow.left || comparableRow.right).indicativeTenureValuesSorted || [];
          let disabledYear = _.find(tenureTypeYears, {year: yearObj.year, disabled: true});
          return !!disabledYear;
        },
      })
    });

    return tenuresFields;
  }

  createTotalsFields() {
    let totalsFields = [];
    this.yearColumns().forEach((yearObj, index) => {
      totalsFields.push({
        label: this.getFinancialYear(yearObj.year),
        field: 'someFieldExpressionForCompare',
        format(row){
          return row ? row[yearObj.year] : null;
        },
        changeAttribute(row){
          return `${yearObj.year}:totals`;
        }
      })
    });
    return totalsFields;
  }

  createSummariesFields() {
    return [
      {
        label: 'Total grant eligible units',
        field: 'grantEligibleUnits',
        format: 'number',
        changeAttribute: row => row? `${row.year}:grantEligibleUnits`: null
      },
      {
        label: 'Grant per unit',
        field: 'grantRate',
        format: 'currency',
        changeAttribute: row => row? `${row.year}:grantRate`: null
      },
      {
        label: 'Total grant',
        field: 'totalGrant',
        format: 'currency',
        changeAttribute: row => row? `${row.year}:totalGrant`: null
      }
    ];
  }

  yearColumns() {
    let firstTenureRow = this.reportData.tenuresToCompare[0];
    return (firstTenureRow.left || firstTenureRow.right).indicativeTenureValuesSorted;
  }

  createSummariesGroups() {
    let summariesGroups = [];
    let lastGroup = {};
    for (let i = 0; i < this.reportData.summariesToCompare.length; i++) {
      let row = this.reportData.summariesToCompare[i];
      let tenureName = (row.left || row.right).name;
      if (i === 0) {
        lastGroup = {
          name: tenureName,
          rows: [row]
        }
      } else {
        if (lastGroup.name != tenureName) {
          summariesGroups.push(lastGroup);
          lastGroup = {
            name: tenureName,
            rows: [row]
          }
        } else {
          lastGroup.rows.push(row);
        }
      }
    }

    if (this.reportData.summariesToCompare.length) {
      summariesGroups.push(lastGroup);
    }
    return summariesGroups;
  }


  getFinancialYear(year) {
    let nextYear = `${year + 1}`.slice(-2);
    return `${year}/${nextYear}`
  }

}

IndicativeGrantChangeReport.$inject = ['GrantService'];

angular.module('GLA')
  .component('indicativeGrantChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/indicativeGrantChangeReport.html',
    controller: IndicativeGrantChangeReport
  });
