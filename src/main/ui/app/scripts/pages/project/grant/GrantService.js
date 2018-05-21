/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

'use strict';

function GrantService(numberFilter, orderByFilter, ReportService) {
  return {
    extractErrors: function (tenure, errors) {
      var allErrors = [];
      var errors = errors || {};
      var errorBlock = (tenure.key === 'NegotiatedGrant' || tenure.key === 'DeveloperLedGrant') ? 'Block2' : 'Block1';
      tenure.tenureTypeAndUnitsEntries.forEach(function (item, index) {
        var rowErrors = errors[item.id];
        if (rowErrors) {
          rowErrors.forEach(function (e) {
            e.rowId = item.tenureType.name;
          });
          allErrors = allErrors.concat(rowErrors);
        }
      });
      allErrors = allErrors.concat(errors[errorBlock] || []);
      return allErrors;
    },

    negotiatedGrantBlock: function (apiData) {
      var summaryDetails = apiData.tenureSummaryDetails || [];
      return summaryDetails.map(negotiatedGrantBlock);
    },

    calculateGrantBlock: function (apiData) {
      var summaryDetails = apiData.tenureSummaryDetails || [];
      return summaryDetails.map(calculateGrantBlock);
    },
    calculateClaimedTenure: function (apiData) {
      var summaryDetails = apiData.tenureTypeAndUnitsEntries || [];
      return summaryDetails.map(calculateClaimedTenure);
    },

    developerLedGrantBlock: function (apiData) {
      var summaryDetails = apiData.tenureSummaryDetails || [];
      return summaryDetails.map(developerLedGrantBlock);
    },

    indicativeGrantBlocks: function(apiData){
      var summaryDetails = apiData.tenureSummaryDetails || [];
      var groupedDetails = _.groupBy(summaryDetails, 'name');
      var tenureTypesAndUnits = apiData.tenureTypeAndUnitsEntries || [];
      var summaryDetailsRows = tenureTypesAndUnits.map(item => {
        return {
          name: item.tenureType.name,
          grantBlocks: groupedDetails[item.tenureType.name].map(indicativeGrantBlock)
        }
      });
      return summaryDetailsRows;
    },

    sortTenureTypes: function (grantBlock) {
      if(grantBlock) {
        grantBlock.tenureTypeAndUnitsEntries = orderByFilter(grantBlock.tenureTypeAndUnitsEntries, 'tenureType.displayOrder');
      }
      return grantBlock;
    },

    /**
     * Creates the report data structure used in multiple grant report blocks
     * @param leftBlock
     * @param rightBlock
     * @returns {{left: *, right: *, tenuresToCompare: *, summariesToCompare: Array, totalsToCompare: Array}}
     */
    prepareReportData: function (leftBlock, rightBlock) {
      this.sortTenureTypes(leftBlock);
      this.sortTenureTypes(rightBlock);

      let leftTenures = (leftBlock || {}).tenureTypeAndUnitsEntries || [];
      let rightTenures = (rightBlock || {}).tenureTypeAndUnitsEntries || [];

      //Filter to identify left side tenure type row when you have the right side row
      let leftSideTenureTypeFilter = function (rightRow) {
        return {tenureType: {id: rightRow.tenureType.id}}
      };

      //Filter to identify left side summary row when you have the right side row
      let leftSideSummaryFilter = function (rightRow) {
        return {name: rightRow.name};
      };

      let leftSummaries =  (leftBlock || {}).tenureSummaryDetails || [];
      let rightSummaries =  (rightBlock || {}).tenureSummaryDetails || [];

      let leftTotals = (leftBlock || {}).totals;
      let rightTotals = (rightBlock || {}).totals;

      if(leftTotals) {
        leftTotals.comparisonId = (leftBlock || {}).comparisonId;
      }
      if(rightTotals) {
        rightTotals.comparisonId = (rightBlock || {}).comparisonId;
      }

      return {
        left: leftBlock,
        right: rightBlock,
        tenuresToCompare: ReportService.rowsToCompare(leftTenures, rightTenures, leftSideTenureTypeFilter),
        summariesToCompare: ReportService.rowsToCompare(leftSummaries, rightSummaries, leftSideSummaryFilter),
        totalsToCompare: [{
          left: leftTotals,
          right: rightTotals
        }]
      }
    }
  };


  function negotiatedGrantBlock(summaryBlock) {
    return {
      name: summaryBlock.name,
      items: [
        {
          itemName: 'Unit development cost',
          itemValue: '£' + numberFilter(summaryBlock.unitDevelopmentCost)
        },
        {
          itemName: 'Grant per unit',
          itemValue: '£' + numberFilter(summaryBlock.grantPerUnit)
        }
      ]
    }
  }

  function calculateGrantBlock(summaryBlock) {
    return {
      name: summaryBlock.name,
      items: [
        {
          // itemName: 'Total grant eligible units',
          itemName: 'Total grant eligible units',
          itemValue: numberFilter(summaryBlock.grantEligibleUnits)
        },
        {
          itemName: 'Grant per unit',
          itemValue: '£' + numberFilter(summaryBlock.grantRate)
        },
        {
          itemName: 'Total grant',
          itemValue: '£' + numberFilter(summaryBlock.totalGrant)
        },
      ]
    }
  }

  function calculateClaimedTenure(summaryBlock) {
    return {
      name: summaryBlock.tenureType.name,
      items: [
        {
          itemName: 'Total Units at Start on Site',
          itemValue: numberFilter(summaryBlock.totalUnitsAtStartOnSite)
        },
        {
          itemName: 'Total Units at Completion',
          itemValue: numberFilter(summaryBlock.totalUnitsAtCompletion) || '-'
        }
      ]
    }
  }


  function developerLedGrantBlock(summaryBlock) {
    return {
      name: summaryBlock.name,
      items: [
        {
          itemName: 'Total grant eligible units',
          itemValue: numberFilter(summaryBlock.grantEligibleUnits)
        },
        {
          itemName: 'Grant per unit',
          itemValue: '£' + numberFilter(summaryBlock.grantRate)
        },
        {
          itemName: 'Total grant',
          itemValue: '£' + numberFilter(summaryBlock.totalGrant)
        },
      ]
    }
  }




  function indicativeGrantBlock(summaryBlock) {
    return {
      name: summaryBlock.year + '/' + (summaryBlock.year + 1).toString().substr(-2),
      items: [
        {
          itemName: 'Total grant eligible units',
          itemValue: numberFilter(summaryBlock.grantEligibleUnits)
        },
        {
          itemName: 'Grant per unit',
          itemValue: '£' + numberFilter(summaryBlock.grantRate)
        },
        {
          itemName: 'Total grant',
          itemValue: '£' + numberFilter(summaryBlock.totalGrant)
        },
      ]
    }
  }
}

GrantService.$inject = ['numberFilter', 'orderByFilter', 'ReportService'];

angular.module('GLA')
  .service('GrantService', GrantService);
