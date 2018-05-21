/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';
import NumberUtil from '../../../util/NumberUtil';
import ForecastDataUtil from '../../../util/ForecastDataUtil';
import DateUtil from '../../../util/DateUtil';

class ReceiptsCtrl extends ProjectBlockCtrl {
  constructor(project, $injector, $log, ReceiptsService, ActualsMetadataModal) {
    super(project, $injector);
    this.ReceiptsService = ReceiptsService;
    this.ActualsMetadataModal = ActualsMetadataModal;
    this.data = this.projectBlock || {};
    this.$log = $log;

    this.currentYear = null;

    // shouldn't be done at a controler level, should be done once and reused
    this.ProjectService.getCurrentFinancialYear()
      .then(resp => {
        const year = resp.data;
        this.currentFinancialYearConst = year;
        // TODO refractor this into a better session management
        this.yearSelected(this.blockSessionStorage.financialYear ||
           {
             label:'',
             financialYear: year
           }
        );
      });

    this.loadCategoryData();
  }


  /**
   * Change currently selected year and loads the year data from the server
   * @param {String} financialYear
   */
  yearSelected(financialYear) {
    this.$log.log('year selected', financialYear);
    this.currentYear = {
      financialYear: financialYear
    };

    //TODO check that we are not overwritting over things stored in receipts
    this.blockSessionStorage.financialYear = financialYear;

    this.loadDataForYear(financialYear.financialYear);

  }


  loadCategoryData() {
    // this.ProjectService.getSapCategoryCodes('receipt')
    //   .then(resp => {
    //     this.categories = resp.data;
    //     console.log('old this.categories', resp.data)
    //   });

    return this.ProjectService.getReceiptCategories()
      .then(categories => {
        this.categories = categories;
        console.log('this.categories', categories)
      });
  }

  /**
   * Load data for a financial year
   * @param {Number} year
   * @return {Object} promise
   */
  loadDataForYear(year) {
    this.loading = true;
    this.$rootScope.showGlobalLoadingMask = true;
    return this.ReceiptsService.getReceipts(this.project.id, this.blockId, year)
      .then(resp => {
        this.$log.debug(resp);
        this.parseReceiptData(resp.data);
        this.loading = false;
        this.$rootScope.showGlobalLoadingMask = false;
      });
  }

  /**
   * Parse loaded data
   * @param {Object} data
   */
  parseReceiptData(data) {
    this.$log.log('data', data);
    this.projectBlock = data;
    this.yearData = this.projectBlock.annualReceiptsSummary;
    this.budgetLastModified =
      this.yearData.lastModified ?
      moment(this.yearData.lastModified).format() :
      null;
  }

  /**
   * Format number to string with comma's and append CR
   * @see `StringUtil.numberToStrWithCR`
   */
  formatTotal(value) {
    // return NumberUtil.formatWithPoundAndCR(value);
    let res = NumberUtil.formatWithCommas(value);
    return value && res? `£${res}` : '-';
  }

  /**
   * Add receipt handler
   * @param {Object} data
   */
  onAddReceipt(data) {
    this.$log.debug(data);
    // flag table row for expansion
    let tableState =
      this.blockSessionStorage.receiptsTable ?
      this.blockSessionStorage.receiptsTable :
      this.blockSessionStorage.receiptsTable = {};
    let tableExpandedState =
      tableState.expanded ?
      tableState.expanded :
      tableState.expanded = [];
    const label = moment(data.month, 'MM').format('MMM').toUpperCase();
    if(tableExpandedState.indexOf(label) < 0) {
      tableExpandedState.push(label);
    }

    // send
    data.projectId = this.project.id;
    let p =  this.ReceiptsService.postReceipt(data.projectId, this.currentYear.financialYear.financialYear, data)
      .then(resp => {
        this.loadDataForYear(this.currentYear.financialYear.financialYear);
      });
    return this.addToRequestsQueue(p);
  }

  /**
   * Change receipt handler
   * @param {Object} event
   */
  onChangeReceipt(event) {
    const data = event.data;
    let p = null;
    if(data.forecastId){
      if(data.forecast == null) {
        return this.onDeleteReceipt(event, true);
      }else{
        let p = this.ReceiptsService.editReceipt(this.project.id, data.forecastId, data.forecast)
          .then(resp => {
            this.loadDataForYear(this.currentYear.financialYear.financialYear);
          });
        return this.addToRequestsQueue(p);
      }
    }else{
      data.financialYear = this.currentFinancialYearConst;
      data.sapCategoryCode = data.categoryId;
      data.forecastValue = data.forecast;
     return this.onAddReceipt(data);
    }
  }

  /**
   * Delete receipt handler
   * @param {Object} receipt
   */
  onDeleteReceipt(event, skipConfirmation) {
    let receipt = event.data;
    this.$log.log('receipt: ', receipt);

    if(skipConfirmation){
      this.delete(receipt)
    }else{
      let modal = this.ConfirmationDialog.delete('Are you sure you want to delete receipt forecast?');
      modal.result.then(() => {
        this.delete(receipt)
      });
    }
  }

  delete(receipt){
    this.$rootScope.showGlobalLoadingMask = true;
    let p = this.ReceiptsService.delete(this.project.id, this.blockId, receipt.forecastId)
      .then(resp => {
        return this.loadDataForYear(this.currentYear.financialYear.financialYear);
      })
      .finally(() => {
        this.$rootScope.showGlobalLoadingMask = false;
      });
    this.addToRequestsQueue(p);
  }

  showMetadataModal(event){
    console.log('event', event);
    let modalDataPromise = this.ReceiptsService.getReceiptsMetadata(this.project.id, this.blockId, event.spend.categoryId, event.data.yearMonth);
    this.ActualsMetadataModal.show(modalDataPromise.then(rsp => rsp.data), event.spend.category);
  }

  /**
   * Back handler
   */
  onBack() {
    if (this.readOnly || !this.data) {
      this.returnToOverview();
    } else {
      this.onSave();
    }
  };

  /**
   * Save handler
   */
  onSave() {
    this.ReceiptsService.updateReceipts(this.project.id, this.data)
      .then(resp => {
        this.returnToOverview(this.blockId);
      });
  }
}

ReceiptsCtrl.$inject = ['project', '$injector', '$log', 'ReceiptsService', 'ActualsMetadataModal'];

angular.module('GLA')
  .controller('ReceiptsCtrl', ReceiptsCtrl);
