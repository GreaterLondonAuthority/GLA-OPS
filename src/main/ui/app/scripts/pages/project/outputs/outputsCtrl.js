/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';
import DateUtil from '../../../util/DateUtil';
import './wizard/outputsWizard';
import OutputsUtil from './OutputsUtil';

class OutputsCtrl extends ProjectBlockCtrl {
  constructor(project, $injector, $scope, $timeout, PermPermissionStore, OutputsService, Util) {
    super(project, $injector);
    this.$scope = $scope;
    this.$timeout = $timeout;

    this.realReadOnly = this.readOnly;
    this.PermPermissionStore = PermPermissionStore;
    this.OutputsService = OutputsService;
    this.NumberUtil = Util.Number;
    this.realCurrentYear = null;
    this.currentYear = null;
    this.tableData = null;
    this.isPastFinancialYear = false;
    this.tableColumnOffsets = ['19%', '17%', '14%', '8%', '14%', '14%', '14%'];
    this.unitConfig = OutputsUtil.getUnitConfig();
    this.outputTypes = OutputsUtil.getOutputTypes();

    this.blockSessionStorage.outputsTable =
      this.blockSessionStorage.outputsTable ?
        this.blockSessionStorage.outputsTable :
        {};

    this.originalEditable = this.editable;
    this.originalReadOnly = this.readOnly;
    this.sectionExpanded = true;
    this.outputsExpanded = true;
    this.initOutputSummaries();

    // on EDIT the page reloads, so we keep a reference of the currently selected year
    // let previousSelectedYear = this.blockSessionStorage.currentYear || null;

    // initial load
    this.$rootScope.showGlobalLoadingMask = true;
    this.ProjectService.getCurrentFinancialYear()
      .then(resp => {

        const year = resp.data;
        this.realCurrentYear = year;
        this.onYearSelected(this.blockSessionStorage.currentYear ||
          {
            label: '',
            financialYear: year
          }
        );

        // setup forecast data
        this.setupCategoryData();
      });
  }


  initOutputSummaries(){
    this.blockSessionStorage.outputSummaries = this.blockSessionStorage.outputSummaries || {};
    this.outputSummaries = this.OutputsService.outputSummaries(this.projectBlock, this.blockSessionStorage.outputSummaries);

    this.outputsSummaryTitle = this.OutputsService.getOutputBlockSummariesTitle(this.projectBlock);
    this.updateExpandAllToggleState();
  }

  /**
   * Edit button override as this block has some specific 'can edit' permissions
   */
  updateEditability() {
    const reqPermission =
      `proj.outputs.${this.isPastFinancialYear ? 'editPast' : 'editFuture'}.${this.project.organisation.id}`;
    const perm = this.PermPermissionStore.getPermissionDefinition(reqPermission);

    if (perm) {
      this.editable = this.originalEditable;
      this.readOnly = this.originalReadOnly;
    } else {
      this.editable = false;
      this.readOnly = true;
    }
  }

  /**
   * Load data for a financial year
   * @param {number} year
   * @param {boolean} updateOnDiff - only update modified records, instead of full update
   * @return {Object} promise
   */
  loadDataForYear(year, updateOnDiff) {
    this.loading = true;
    this.$rootScope.showGlobalLoadingMask = true;
    return this.OutputsService.getProjectOutputs(this.project.id, this.blockId, year)
      .then(resp => {
        this.projectBlock = resp.data;
        this.initOutputSummaries();
        this.tableData = this.parseTableData(resp.data.tableData, updateOnDiff);
        this.loading = false;
        this.updateEditability();
        this.$rootScope.showGlobalLoadingMask = false;
      });
  }

  /**
   * Parse data retrieved
   * @param {boolean} updateOnDiff - only update modified records, instead of full update
   */
  parseTableData(data, updateOnDiff) {
    // aggregate and sort objects in a multi-array by category
    const newTableData = _
      .chain(data)
      .each(item => {
        //Prepend 0 to single digit months;
        let pad = item.month < 10 ? '0' : '';
        item.financialMonth = `${item.year}-${pad}${item.month}`;
        item.config.subcategory =
          item.config.subcategory.toLowerCase() === 'n/a' ?
            item.config.category :
            item.config.subcategory;
      })
      .sortBy([
        'config.category',
        'financialMonth',
        'config.subcategory',
        'outputType'
      ])
      .groupBy('config.category')
      .values()
      .value();

    if (updateOnDiff && this.tableData) {
      // store previous data
      let currentTableData = this.tableData;
      // TODO: table difference update
      this.tableData = _.each(this.tableData, (item, index) => {
        item[0].config.category = item[0].config.category.split(' | ')[0];
        item[0].config.category += ` | ${moment().format('HH:mm:ss')}`;
      });
      return this.tableData;
    } else {
      return newTableData;
    }
  }

  /**
   * Build forecast data
   * @returns {Object} promise
   */
  setupCategoryData() {
    this.OutputsService.getOutputConfigGroup(this.projectBlock.configGroupId).then((resp) => {
      this.parseConfigGroup(resp.data)
    });
  }

  /**
   * Parse response to generate a nested list (categories/subcategories
   *  come in as a flat list).
   * @param  {[type]} data [description]
   * @return {[type]}      [description]
   */
  parseConfigGroup(data) {
    this.periodType = data.periodType ? data.periodType : 'Monthly';
    this.outputTypeName = data.outputTypeName ? data.outputTypeName : 'Output Type';
    this.categoryName = data.categoryName ? data.categoryName : 'Category';
    this.subcategoryName = data.subcategoryName ? data.subcategoryName : 'Sub Category';

    OutputsUtil.setDirectOrIndirect(data.outputTypes);

    var categories = [];
    //_.groupBy(data, 'category');
    // Note: groupBy generates an object and not a array
    // we sort the list so we are guaraneed to have all the categories grouped
    // then loop through then and push them in an array of arrays.

    var temp = _.sortBy(data.categories, 'displayOrder');
    _.forEach(temp, (item) => {
      // if nothing push the first item as a new array
      if (!categories.length) {
        categories.push([item]);
      } else {
        // if the last category in categories is the same as the next item
        // add it to the same list.
        //
        // othewise create a new categories entry
        if (categories[categories.length - 1][0].category === item.category) {
          categories[categories.length - 1].push(item);
        } else {
          categories.push([item]);
        }
      }
    });
    //TODO why we use static setters?
    OutputsUtil.setCategories(categories);
    this.categories = categories;

    this.configLoaded = true;
  }

  /**
   * Back end call to update add a new output value
   * upon response we need to do a get
   * @param  {[type]} event [description]
   * @return {[type]}       [description]
   */
  onAddOutput(data) {
    this.$rootScope.showGlobalLoadingMask = true;

    // flag table row for expansion
    let tableState = this.blockSessionStorage.outputsTable;
    let tableExpandedState =
      tableState.expanded ?
        tableState.expanded :
        tableState.expanded = [];
    const label = data.config.category;
    if (tableExpandedState.indexOf(label) < 0) {
      tableExpandedState.push(label);
    }

    data.projectId = this.project.id;

    let p = this.OutputsService.postProjectOutputs(data)
      .then(resp => {
        this.loadDataForYear(this.blockSessionStorage.currentYear.financialYear);
        this.$rootScope.showGlobalLoadingMask = false;
      });

    return this.addToRequestsQueue(p);
  }

  /**
   * Back button handler
   */
  onBack() {
    if (this.realReadOnly || this.loading) {
      this.returnToOverview();
    } else {
      this.unlockAndExit();
    }
  }

  /**
   * Change currently selected year and loads the year data from the server
   * @param {String} yearStr
   */
  onYearSelected(financialYear) {
    this.isPastFinancialYear = (financialYear.financialYear < this.realCurrentYear);

    this.currentYear = {
      financialYear: financialYear
    };

    this.blockSessionStorage.currentYear = financialYear;
    this.loadDataForYear(financialYear.financialYear);
  }

  /**
   * Unlock block and exit
   */
  unlockAndExit() {
    this.unlockBlock()
      .then(() => {
        this.returnToOverview(this.blockId);
      });
  }

  /**
   * Row changed handler
   */
  onRowChanged(event) {
    const data = event.data;
    let p = this.OutputsService.updateProjectOutputs(this.project.id, data)
      .then(resp => {
        this.loadDataForYear(this.blockSessionStorage.currentYear.financialYear, false);
      });
    return this.addToRequestsQueue(p);
  }

  /**
   * Delete spend row handler
   */
  onRowDeleted(event) {
    let modal = this.ConfirmationDialog.delete('Are you sure you want to delete this output?');
    let output = event.data;
    modal.result.then(() => {
      this.$rootScope.showGlobalLoadingMask = true;
      let p = this.OutputsService.delete(this.project.id, output.id)
        .then(resp => {
          return this.loadDataForYear(output.year);
        })
        .finally(() => {
          this.$rootScope.showGlobalLoadingMask = false;
        });
      this.addToRequestsQueue(p);
    });
  }

  toggleSection() {
    console.log('toggleSection', this.sectionExpanded);
    this.sectionExpanded = !this.sectionExpanded;
  }

  toggleOutputs() {
    this.outputsExpanded = !this.outputsExpanded;
    this.outputSummaries.forEach(os=>{
      os.collapsed = !this.outputsExpanded;
      this.blockSessionStorage.outputSummaries[os.comparisonId] = os.collapsed;
    });
  }

  toggleRow(row){
    row.collapsed = !row.collapsed;
    this.blockSessionStorage.outputSummaries[row.comparisonId] = row.collapsed;
    this.updateExpandAllToggleState();
  }

  updateExpandAllToggleState(){
    let outputsExpandedShouldChange = this.outputSummaries.every(o => o.collapsed == this.outputsExpanded);
    if(outputsExpandedShouldChange){
      this.outputsExpanded = !this.outputsExpanded;
    }
  }

  formatNumber(value, valueType) {
    const precision = this.unitConfig[valueType].precision || 0;
    return value ? this.NumberUtil.formatWithCommas(value, precision) : '';
  }
}

OutputsCtrl.$inject = ['project', '$injector', '$scope', '$timeout', 'PermPermissionStore', 'OutputsService', 'Util'];

angular.module('GLA')
  .controller('OutputsCtrl', OutputsCtrl);
