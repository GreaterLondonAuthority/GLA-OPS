/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';
import './profiled-unit-table/profiledUnitTable';

class UnitsCtrl extends ProjectBlockCtrl {
  constructor($state, $log, ProjectService, moment, project, $injector, unitsMetadata, UnitsService){
    super(project, $injector);

    this.$state = $state;
    this.$log = $log;
    this.ProjectService = ProjectService;
    this.UnitsService = UnitsService;
    this.unitsMetadata = unitsMetadata;
    this.moment = moment;
    this.init();
  }

  updateValidationMessages() {
    const messages = this.blockData.validationFailures;
  }

  init(data){
    let summaryTiles = [];

    this.blockData = this.projectBlock;

    _.forEach(this.blockData.tenureProfiles.breakdown, (tenure) => {

      summaryTiles.push({
        name: tenure.tenureName,
        items: [{
          itemName: 'Profiled Units',
          itemValue: tenure.profiledUnits,
          displayWarn: (tenure.profiledUnits != tenure.totalUnits)
        },{
          itemName: 'Total Units',
          itemValue: tenure.totalUnits,
        }]
      });
    });

    this.summaryTiles = summaryTiles;

    this.tenureIdToName = this.unitsMetadata.tenureDetails.reduce((idToName, item)=>{
      idToName[item.id] = item.name;
      return idToName;
    }, {});

    this.tableEntries = (this.projectBlock.tableEntries || []).map(item => {
      item.tenureName = this.tenureIdToName[item.tenureId];
      return item;
    });

    this.filteredRentUnits = _.filter(this.tableEntries, {type: 'Rent'});
    this.filteredSalesUnits = _.filter(this.tableEntries, {type: 'Sales'});

    this.hasLegacyRent = this.UnitsService.hasMarketType(this.unitsMetadata, this.UnitsService.LEGACY_RENT_MARKET_TYPE_ID);
    this.hiddenSalesColumns = this.UnitsService.hiddenSalesColumns(this.unitsMetadata);


    const allMarketTypes = this.UnitsService.uniqueMarketTypes(this.unitsMetadata) || [];
    this.showRentMarketTypes = _.filter(allMarketTypes, mt => mt.availableForRental).length > 1;
    this.showSalesMarketTypes = _.filter(allMarketTypes, mt => mt.availableForSales).length > 1;
  }

  addUnit(unit) {
    this.$rootScope.showGlobalLoadingMask = true;
    this.UnitsService.addUnit(this.project.id, this.blockId, unit)
      .then((block) => {
        return this.ProjectService.getProjectBlock(this.project.id, this.blockId, true).then((rsp) => {
          this.projectBlock = rsp.data;
          this.init();
        });
      })
      .finally(()=>{
        this.$rootScope.showGlobalLoadingMask = false;
      });
  }


  editUnit(event){
    let p = this.UnitsService.editUnit(this.project.id, this.blockId, event.data.id, event.data)
      .then((block) => {
        return this.ProjectService.getProjectBlock(this.project.id, this.blockId, true).then((rsp) => {
          this.projectBlock = rsp.data;
          this.init();
        });
      });
    this.addToRequestsQueue(p);
  }


  deleteUnit(event) {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete the row?');
    let unit = event.data;
    modal.result.then(() => {
      this.$rootScope.showGlobalLoadingMask = true;
      let p = this.UnitsService.deleteUnit(this.project.id, this.blockId, event.data.id)
        .then((block) => {
          return this.ProjectService.getProjectBlock(this.project.id, this.blockId, true).then((rsp) => {
            this.projectBlock = rsp.data;
            this.init();
          });
        })
        .finally(() => {
          this.$rootScope.showGlobalLoadingMask = false;
        });
      this.addToRequestsQueue(p);
    });
  }

  sumUnitsByNumberOfPeople() {
    return (this.blockData.type1Units * 1)
      + (this.blockData.type2Units * 1)
      + (this.blockData.type3Units * 1)
      + (this.blockData.type4Units * 1)
      + (this.blockData.type5Units * 1)
      + (this.blockData.type6Units * 1)
      + (this.blockData.type7Units * 1)
      + (this.blockData.type8Units * 1);
  }

  onSaveData(releaseLock) {
    let data = {
      type: this.blockData.type,
      newBuildUnits: this.blockData.newBuildUnits,
      refurbishedUnits: this.blockData.refurbishedUnits,
      type1Units: this.blockData.type1Units,
      type2Units: this.blockData.type2Units,
      type3Units: this.blockData.type3Units,
      type4Units: this.blockData.type4Units,
      type5Units: this.blockData.type5Units,
      type6Units: this.blockData.type6Units,
      type7Units: this.blockData.type7Units,
      type8Units: this.blockData.type8Units,
      nbWheelchairUnits: this.blockData.nbWheelchairUnits,
      grossInternalArea: this.blockData.grossInternalArea
    };
    return this.UnitsService.save(this.project.id, this.blockId, data, releaseLock);
  }

  back() {
    if (this.readOnly) {
      this.returnToOverview();
    } else {
      this.onSaveData(true).then(()=>{
        this.returnToOverview(this.blockId);
      });
    }
  }

  submit() {
    this.onSaveData(true).then(()=>{
      this.returnToOverview(this.blockId);
    });
  }

  autoSave() {
    this.onSaveData(false).then((resp)=>{
      this.blockData = resp.data;
    });
  }
}

UnitsCtrl.$inject = ['$state', '$log', 'ProjectService', 'moment', 'project', '$injector', 'unitsMetadata', 'UnitsService'];

angular.module('GLA')
  .controller('UnitsCtrl', UnitsCtrl);
