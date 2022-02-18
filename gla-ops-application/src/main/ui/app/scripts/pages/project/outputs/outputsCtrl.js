/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';
import {ClaimModalComponent} from '../../../../../../gla-ui/src/app/claim-modal/claim-modal.component'
import './assumptionModal/assumptionModal';
import './output-entry-modal/outputEntryModal';

const ClaimType = {
  ADVANCE: 'ADVANCE'
};


class OutputsCtrl extends ProjectBlockCtrl {
  constructor(project, $injector, $scope, $timeout, template, PermPermissionStore, OutputsService, Util, outputsMessage, currentFinancialYear, currentAcademicYear, ConfirmationDialog, ErrorService, OutputEntryModal, NgbModal) {
    super($injector);
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.template = template;
    this.blockConfig = _.find(template.blocksEnabled, {block: 'Outputs'});
    this.outputsBlock = _.find(project.projectBlocksSorted, {blockType: 'Outputs'});
    this.outputsMessage = outputsMessage;
    this.ConfirmationDialog = ConfirmationDialog;
    this.ErrorService = ErrorService;
    this.PermPermissionStore = PermPermissionStore;
    this.OutputsService = OutputsService;
    this.NumberUtil = Util.Number;
    this.DateUtil = Util.Date;
    this.currentFinancialYear = currentFinancialYear;
    this.currentAcademicYear = currentAcademicYear;
    this.OutputEntryModal = OutputEntryModal;
    this.NgbModal = NgbModal
  }

  $onInit(){
    super.$onInit();
    this.realReadOnly = this.readOnly;
    this.currentYear = null;
    this.tableData = null;
    this.validationFailures = null;
    this.loadBaselineData();
    this.isPastFinancialYear = false;

   /* this.tableColumnOffsets1 = {
      category: '19%',
      outputType: '17%',
      value: '14%',
      month: '8%',
      forecast: '14%',
      actual: '14%',
      difference: '14%'
    };

    this.tableColumnOffsetsHeader2 = {
      category: '50%',
      outputType: '0%',
      value: '0%',
      month: '2%',
      forecast: '16%',
      actual: '16%',
      difference: '16%'
    };


    this.tableColumnOffsets2 = {
      category: '25%',
      outputType: '0%',
      value: '0%',
      month: '10%',
      forecast: '14%',
      actual: '14%',
      difference: '14%'
    };*/

    let displayColumns =  this.blockConfig.showValueColumn || this.blockConfig.showOutputTypeColumn;
    // this.tableColumnOffsetsHeader = displayColumns ? this.tableColumnOffsets1 : this.tableColumnOffsetsHeader2;
    // this.tableColumnOffsets = displayColumns ? this.tableColumnOffsets1 : this.tableColumnOffsets2;
    this.showQuarterlyOutputs = this.blockConfig.outputGroupType === 'ByQuarter';

    // this.baselineColumnOffsets = {
    //   category: '30%',
    //   value: '30%',
    //   baseline: '30%',
    //   difference: '10%' //Delete button
    // };


    this.unitConfig = this.OutputsService.getUnitConfig();

    //Map of {categoryName: isExpanded}
    this.blockSessionStorage.expandeOutputsTableCategories = this.blockSessionStorage.expandeOutputsTableCategories || {};
    this.blockSessionStorage.expandedBaslineCategories = this.blockSessionStorage.expandedBaslineCategories || {};

    this.originalEditable = this.editable;
    this.originalReadOnly = this.readOnly;
    this.sectionExpanded = true;
    this.baselineExpanded = false;
    this.outputsExpanded = true;
    this.initOutputSummaries();



    // on EDIT the page reloads, so we keep a reference of the currently selected year
    // let previousSelectedYear = this.blockSessionStorage.currentYear || null;

    // initial load
    this.$rootScope.showGlobalLoadingMask = true;
    this.realCurrentYear = this.currentFinancialYear;

    this.onYearSelected(this.blockSessionStorage.currentYear ||
      {
        label: '',
        financialYear: this.realCurrentYear
      }
    );

    // setup forecast data
    this.setupCategoryData();

    this.outputsCostBlock = (_.find(this.project.projectBlocksSorted, {blockType: 'OutputsCosts'}));
    this.categoriesCosts = this.outputsCostBlock ? (this.outputsCostBlock || {}).categoriesCosts : [];
    this.recoveryOutputs = this.categoriesCosts.reduce((result, cc) => {
      result[cc.outputCategoryConfigurationId] = !!cc.recoveryOutput;
      return result;
    }, {});
    this.displayUnitCost = this.outputsCostBlock == undefined ? false : true;

    this.claimable = this.outputsBlock.claimable;
    this.showAdvancedPaymentColumn = this.blockConfig.showAdvancedPaymentColumn;
    this.claimStatus = (this.projectBlock.advancePaymentClaim || {}).claimStatus;
  }

  showClaimModal(item, amount) {
    item = item || {};
    let claimStatus = item.claimStatus || 'Claim';
    let isClaimed = (item.claimStatus === 'Claimed' || item.claimStatus === 'Approved');

    let textsByStatus = {
      claim: `By claiming the advance payment, you are confirming that the outputs will be delivered and the recovery output has been agreed. Claimed payments will display in the Payments section of OPS once the project changes have been approved`,
      claimed: 'Advance payment must be cancelled before it can be edited',
      approved: null
    };

    let config = {
      title: `${claimStatus.toUpperCase()} OUTPUTS`,
      subtitle: 'Advance Payment',
      text: textsByStatus[claimStatus.toLowerCase()],
      claimableAmount: amount,
      isClaimed: isClaimed,
      claimBtnText: 'CLAIM ADVANCE PAYMENT',
      readOnly: this.readOnly || item.claimStatus === 'Approved'
    };

    let claimRequest = {
      id: (this.projectBlock.advancePaymentClaim || {}).id,
      projectId: this.project.id,
      blockId: this.projectBlock.id,
      claimType: ClaimType.ADVANCE,
    };

    let modal = this.NgbModal.open(ClaimModalComponent)
    modal.componentInstance.config = config
    modal.componentInstance.claimRequest = claimRequest
    modal.result.then((result) => {
      this.loadDataForYear(this.blockSessionStorage.currentYear.financialYear, false);
    }, err => {});
  }


  refreshData() {
    this.loadDataForYear(this.blockSessionStorage.currentYear.financialYear, false);
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

    const editCurrentOrgPerm = `proj.outputs.${this.isPastFinancialYear ? 'editPast' : 'editFuture'}`;
    const hasEditPermission = this.UserService.hasPermission(editCurrentOrgPerm, this.project.organisation.id);

    if (hasEditPermission) {
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
   * @return {Object} promise
   */
  loadDataForYear(year) {
    this.loading = true;
    this.$rootScope.showGlobalLoadingMask = true;
    let outputsPromise = this.OutputsService.getProjectOutputs(this.project.id, this.blockId, year);
    let assumptionsPromise = this.OutputsService.getAssumptions(this.project.id, this.blockId, year);

    let p = this.$q.all([outputsPromise, assumptionsPromise]).then((responses) => {
      let resp = responses[0];

      let assumptions = responses[1].data;
      this.categoriesToAssumptions = (assumptions || []).reduce((idToAssumption, a) => {
        idToAssumption[a.category] = a;
        return idToAssumption;
      }, {});
      this.projectBlock = resp.data;

      // only interested in quarterly current year claims
      this.claims =  resp.data.outputsClaims.filter(function(claim, index, arr){
        return claim.year === year;
      });

      this.latestClaim = _.maxBy(resp.data.outputsClaims, c => c.yearPeriod);
      this.updateDisabledMonths(year, this.latestClaim);

      this.initOutputSummaries();
      this.tableData = this.parseTableData(resp.data.tableData);
      this.quarters = this.OutputsService.transformToQuarterlyData(year, resp.data.quarters, this.claims);

      this.loading = false;
      this.validationFailures = resp.data.validationFailures;
      this.updateEditability();
      this.$rootScope.showGlobalLoadingMask = false;
    });
    return this.addToRequestsQueue(p)
  }

  updateDisabledMonths(selectedYear, latestClaim){
    if(!latestClaim || latestClaim.year < selectedYear) {
      this.disabledMonths = [];
    }else if(latestClaim.year > selectedYear){
      this.disabledMonths = this.DateUtil.getFinancialYearMonths();
    }else{
      let lastMonthOfTheQuarter = this.DateUtil.getLastMonthInQuarter(latestClaim.claimTypePeriod);
      this.disabledMonths = this.DateUtil.getFinancialYearMonthsBeforeInclusive(lastMonthOfTheQuarter)
    }
  }

  /**
   * Load baseline data
   * @return {Object} promise
   */
  loadBaselineData() {
    this.loading = true;
    this.$rootScope.showGlobalLoadingMask = true;
    return this.OutputsService.getProjectBaselineOutputs(this.project.id, this.blockId)
      .then(resp => {
        this.baselineTableData = this.parseTableData(resp.data);
        this.loading = false;
        this.$rootScope.showGlobalLoadingMask = false;
      }).then(rsp => {
        return this.loadDataForYear(this.getYear());
      });
  }

  /**
   * @param data
   * @param assumptions
   */
  parseTableData(data) {
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

    return newTableData;
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
    this.outputTypes = data.outputTypes || [];
    this.isOutputTypes = (this.outputTypes.length > 1);
    this.displayOutputType = this.isOutputTypes ? this.blockConfig.showOutputTypeColumn : this.isOutputTypeEmpty;

    let subcategoryGroups = [];
    //_.groupBy(data, 'category');
    // Note: groupBy generates an object and not a array
    // we sort the list so we know categories appear in displayOrder specified
    // then loop through and push them in an array of arrays.

    let categoryToSubcategories =  {};

    let sortedCategories = _.sortBy(data.categories, 'displayOrder');
    _.forEach(sortedCategories, (item) => {
      if (item.hidden) {
        return;
      }

      let subcategories = categoryToSubcategories[item.category] || [];
      categoryToSubcategories[item.category] = subcategories;

      //If there was no group yet, add new group to array
      if (!subcategories.length) {
        subcategoryGroups.push(subcategories);
      }

      subcategories.push(item);
    });

    this.categories = subcategoryGroups;
    this.configLoaded = true;
  }

  /**
   * Back end call to update add a new output value
   * upon response we need to do a get
   * @param  {[type]} event [description]
   * @return {[type]}       [description]
   */
  onAddOutput(data) {
    // flag table row for expansion
    this.blockSessionStorage.expandeOutputsTableCategories[this.getCategory(data)] = true;

    return this.addOutput(data).then(()=>{
      return this.loadDataForYear(this.blockSessionStorage.currentYear.financialYear);
    }).catch(this.ErrorService.apiValidationHandler());

  }

  onAddBaselineOutput(data) {
    // flag table row for expansion
    this.blockSessionStorage.expandedBaslineCategories[this.getCategory(data)] = true;

    // this.addOutput(tableState, data, this.loadBaselineData.bind(this));
    return this.addOutput(data).then(()=>{
      return this.loadBaselineData();
    });
  }

  onAssumptionChange(assumption){
    let p;
    if(assumption.id){
      p = this.OutputsService.updateAssumption(this.project.id, this.blockId, assumption)
    }else{
      assumption.year = this.getYear();
      assumption.year = this.getYear();
      p = this.OutputsService.addAssumption(this.project.id, this.blockId, assumption)
    }
    return this.addToRequestsQueue(p.then(rsp => this.loadDataForYear(this.getYear())));
  }

  onDeleteAssumption(assumption){
    let p = this.OutputsService.deleteAssumption(this.project.id, this.blockId, assumption.id);
    return this.addToRequestsQueue(p.then(rsp => this.loadDataForYear(this.getYear())));
  }

  getYear(){
    return ((this.blockSessionStorage || {}).currentYear || {}).financialYear || this.realCurrentYear;
  }

  getCategory(categoryRow){
    return categoryRow.config.category;
  }

  addOutput(data) {
    this.$rootScope.showGlobalLoadingMask = true;
    data.projectId = this.project.id;

    let p = this.OutputsService.postProjectOutputs(data)
      .then(resp => {
        // successFunction();
      }).finally(()=>{
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
      this.submit();
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
  submit() {
    return this.unlockBlock();
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
   * Row changed handler
   */
  onBaselineRowChanged(event) {
    const data = event.data;
    let p = this.OutputsService.updateProjectOutputs(this.project.id, data)
      .then(resp => {
        this.loadBaselineData();
      });
    return this.addToRequestsQueue(p);
  }

  /**
   * Delete spend row handler
   */
  onRowDeleted(event) {
    let message = ('Are you sure you want to delete this output?');
    return this.onDelete(message, event).then(()=>{
      this.loadDataForYear(this.blockSessionStorage.currentYear.financialYear, false);
    });
  }

  /**
   * Delete baseline row handler
   */
  onBaselineRowDeleted(event) {
    let message = ('Are you sure you want to delete this baseline?');
    return this.onDelete(message, event).then(()=>{
      return this.loadBaselineData();
    });
  }

  onDelete(message, event) {
    let modal = this.ConfirmationDialog.delete(message);
    let output = event.data;
    return modal.result.then(() => {
      this.$rootScope.showGlobalLoadingMask = true;
      let p = this.OutputsService.delete(this.project.id, output.id)
        .then(resp => {
          // successFunction();
        })
        .finally(() => {
          this.$rootScope.showGlobalLoadingMask = false;
        });
      return this.addToRequestsQueue(p);
    });
  }

  toggleSection() {
    this.sectionExpanded = !this.sectionExpanded;
  }

  toggleBaseline() {
    this.baselineExpanded = !this.baselineExpanded;
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

  showOutputEntryModal(isBaselineOutput) {
    let config = {
      year: this.currentYear,
      readOnly: this.readOnly,
      baseline: isBaselineOutput,
      periodType: this.periodType,
      outputTypeName: this.outputTypeName,
      categoryName: this.categoryName,
      categories: this.categories,
      directOrIndirectChoices: this.outputTypes,
      subcategoryName: this.subcategoryName,
      displayOutputType: this.displayOutputType,
      categoriesCosts: this.categoriesCosts,
      disabledMonths: this.disabledMonths,
      displayUnitCost : this.displayUnitCost,
    };

    let modal = this.OutputEntryModal.show(config);
    modal.result.then(data => {
      if(isBaselineOutput) {
        this.onAddBaselineOutput(data.output.event);
      } else {
        this.onAddOutput(data.event);
      }
    })
  }
}

OutputsCtrl.$inject = ['project', '$injector', '$scope', '$timeout', 'template', 'PermPermissionStore', 'OutputsService', 'Util', 'outputsMessage', 'currentFinancialYear', 'currentAcademicYear', 'ConfirmationDialog', 'ErrorService', 'OutputEntryModal', 'NgbModal'];

angular.module('GLA')
  .controller('OutputsCtrl', OutputsCtrl);
