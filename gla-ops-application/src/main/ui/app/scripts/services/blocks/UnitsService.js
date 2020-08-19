/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

UnitsService.$inject = ['$http', 'config', 'ReportService'];

function UnitsService($http, config, ReportService) {
  return {
    LEGACY_RENT_MARKET_TYPE_ID: 2,
    DISCOUNTED_RATE_MARKET_TYPE_ID: 4,
    LEGACY_SALES_MARKET_TYPE_ID: 5,

    /**
     * Add profiled unit from the wizard
     * @param projectId
     * @param blockId
     * @param unitTableEntry Data to save
     */

    addUnit(projectId, blockId, unitTableEntry){
      return $http.post(`${config.basePath}/projects/${projectId}/units/${blockId}`, unitTableEntry);
    },

    /**
     * Edit profiled unit
     * @param projectId
     * @param blockId
     * @param unitTableEntry Data to update
     */

    editUnit(projectId, blockId, id,  unitTableEntry){
      return $http.put(`${config.basePath}/projects/${projectId}/units/${blockId}/entries/${id}`, unitTableEntry);
    },

    /**
     * Deletes the unit row
     * @param projectId
     * @param blockId
     * @param id of unitTableEntry
     */
    deleteUnit(projectId, blockId, id){
      if(!id){
        throw Error('Missing parameter');
      }
      return $http.delete(`${config.basePath}/projects/${projectId}/units/${blockId}/${id}`);
    },

    getUnitsMetadata(projectId){
      return $http.get(`${config.basePath}/projects/${projectId}/units/metadata`);
    },

    prepareReportData(left, right, tenureIdToName) {
      let reportData = {};
      let tenureSummariesToCompare = [];
      let breakdown  = left ? (left.tenureProfiles || {}).breakdown : (right.tenureProfiles || {}).breakdown;
      breakdown = breakdown || [];

      _.forEach(breakdown, (breakdown) => {

        let leftTenureBreakdown = left && _.find(left.tenureProfiles.breakdown, {extId: breakdown.extId});
        if(leftTenureBreakdown){
          leftTenureBreakdown.comparisonId = leftTenureBreakdown.tenureName;
        }
        let rightTenureBreakdown = right && _.find(right.tenureProfiles.breakdown, {extId: breakdown.extId});
        if(rightTenureBreakdown){
          rightTenureBreakdown.comparisonId = rightTenureBreakdown.tenureName;
        }
        tenureSummariesToCompare.push({
          left: leftTenureBreakdown,
          right: rightTenureBreakdown
        });
      });
      reportData.tenureSummariesToCompare = tenureSummariesToCompare;

      //Filter to identify left side tenure type row when you have the right side row
      let leftSideRentalUnitsFilter = function (row) {
        return {id: row.originalId};
      };

      let leftRentalUnitsSummaries =  _.filter(((left || {}).tableEntries || []), {type: 'Rent'});
      let rightRentalUnitsSummaries =  _.filter(((right || {}).tableEntries || []), {type: 'Rent'});
      reportData.rentalsUnits = ReportService.rowsToCompare(leftRentalUnitsSummaries, rightRentalUnitsSummaries, leftSideRentalUnitsFilter);

      let leftSaleUnitsSummaries =  _.filter(((left || {}).tableEntries || []), {type: 'Sales'});
      let rightSalesUnitsSummaries =  _.filter(((right || {}).tableEntries || []), {type: 'Sales'});
      reportData.salesUnits = ReportService.rowsToCompare(leftSaleUnitsSummaries, rightSalesUnitsSummaries, leftSideRentalUnitsFilter);

      reportData.buildTypeTotalText = {}
      let buildTypeLeft = [];
      let total = 0;
      if (left) {
        total = (left.newBuildUnits || 0) * 1 + (left.refurbishedUnits || 0) * 1;
        buildTypeLeft.push({
          id: 1,
          buildType: 'UNITS',
          newBuildUnits: left.newBuildUnits,
          refurbishedUnits: left.refurbishedUnits,
          total: total,
          comparisonId: left.comparisonId
        });
        reportData.buildTypeTotalText.left = {text: 'The total build type split must match the total of ' + total + ' units in the project'};
      }
      let buildTypeRight = [];
      if (right) {
        total = (right.newBuildUnits || 0) * 1 + (right.refurbishedUnits || 0) * 1;
        buildTypeRight.push({
          id: 1,
          buildType: 'UNITS',
          newBuildUnits: right.newBuildUnits,
          refurbishedUnits: right.refurbishedUnits,
          total: total,
          comparisonId: right.comparisonId
        });
        reportData.buildTypeTotalText.right = {text: 'The total build type split must match the total of ' + total + ' units in the project'};
      }

      let buildTypeFilter = function (row) {
        return {id: row.id};
      };

      reportData.buildType = ReportService.rowsToCompare(buildTypeLeft, buildTypeRight, buildTypeFilter);

      reportData.byNumberOfPeopleText = {};
      let byNumberOfPeopleLeft = [];
      if(left){
        total = 1 * (left.type1Units || 0) + 1 * (left.type2Units || 0) + 1 * (left.type3Units || 0) + 1 * (left.type4Units || 0) + 1 * (left.type5Units || 0) + 1 * (left.type6Units || 0) + 1 * (left.type7Units || 0) + 1 * (left.type8Units || 0);
        byNumberOfPeopleLeft = [{
          id: 1,
          people: 'UNITS',
          type1Units: left.type1Units,
          type2Units: left.type2Units,
          type3Units: left.type3Units,
          type4Units: left.type4Units,
          type5Units: left.type5Units,
          type6Units: left.type6Units,
          type7Units: left.type7Units,
          type8Units: left.type8Units,
          total: total,
          comparisonId: left.comparisonId
        }];
        reportData.byNumberOfPeopleText.left = {text: 'Detail how many people each unit is designed to accommodate, total must match the ' + total + ' units in the project.'};
      }
      let byNumberOfPeopleRight = [];
      if(right){
        total = 1 * (right.type1Units || 0) + 1 * (right.type2Units || 0) + 1 * (right.type3Units || 0) + 1 * (right.type4Units || 0) + 1 * (right.type5Units || 0) + 1 * (right.type6Units || 0) + 1 * (right.type7Units || 0) + 1 * (right.type8Units || 0);
        byNumberOfPeopleRight = [{
          id: 1,
          people: 'UNITS',
          type1Units: right.type1Units,
          type2Units: right.type2Units,
          type3Units: right.type3Units,
          type4Units: right.type4Units,
          type5Units: right.type5Units,
          type6Units: right.type6Units,
          type7Units: right.type7Units,
          type8Units: right.type8Units,
          total: total,
          comparisonId: right.comparisonId
        }];
        reportData.byNumberOfPeopleText.right = {text: 'Detail how many people each unit is designed to accommodate, total must match the ' + total + ' units in the project.'};
      }
      reportData.byNumberOfPeople = ReportService.rowsToCompare(byNumberOfPeopleLeft, byNumberOfPeopleRight, buildTypeFilter);

      return reportData;
    },

    hasMarketType(unitsMetadata, marketTypeId) {
      let allMarketTypes = this.uniqueMarketTypes(unitsMetadata);
      return allMarketTypes.some(mt => mt.id === marketTypeId);
    },

    uniqueMarketTypes(unitsMetadata){
      let allMarketTypes = (unitsMetadata.tenureDetails || []).reduce((marketTypes, tenureDetail) =>{
        return marketTypes.concat(tenureDetail.marketTypes);
      }, []);
      return _.uniqBy(allMarketTypes, 'id');
    },

    uniqueSalesMarketTypes(unitsMetadata){
      let allMarketTypes = this.uniqueMarketTypes(unitsMetadata);
      return _.filter(allMarketTypes, {availableForSales:true});
    },

    hiddenSalesColumns(unitsMetadata) {
      let hasLegacySalesMarketType =  this.hasMarketType(unitsMetadata, this.LEGACY_SALES_MARKET_TYPE_ID);

      let hiddenFields = {
        discountOffMarketValue: !this.hasMarketType(unitsMetadata, this.DISCOUNTED_RATE_MARKET_TYPE_ID),
        netWeeklyRent: !hasLegacySalesMarketType
      };
      return hiddenFields
    },

    enrichTableEntry(tableEntryInsideBlock, tenureIdToMetadataEntry, ){
      let metadataApiTenureType = tenureIdToMetadataEntry[tableEntryInsideBlock.tenureId];
      if(metadataApiTenureType) {
        let metadataApiMarketType = _.find(metadataApiTenureType.marketTypes, {id: tableEntryInsideBlock.marketType.id}) || {};
        tableEntryInsideBlock.tenureName = metadataApiTenureType.name;
        tableEntryInsideBlock.marketType.name = metadataApiMarketType.name || tableEntryInsideBlock.marketType.name;
      }
      return tableEntryInsideBlock;
    }
  };
}

angular.module('GLA')
  .service('UnitsService', UnitsService);
