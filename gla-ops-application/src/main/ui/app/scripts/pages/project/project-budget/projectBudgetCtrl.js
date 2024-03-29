/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';
import './wbs-codes/wbsCodes'
import ForecastDataUtil from '../../../util/ForecastDataUtil';
import DateUtil from '../../../util/DateUtil';
import {ActualsMetadataModalComponent} from '../../../../../../gla-ui/src/app/actuals-metadata-modal/actuals-metadata-modal.component'

class ProjectBudgetCtrl extends ProjectBlockCtrl {
  constructor(project, $injector, $scope, $log, FileUploadErrorModal, FileDeleteConfirmationModal, ToastrUtil, BudgetService, FinanceService, fYearFilter, NgbModal) {
    super($injector);
    this.$scope = $scope;
    this.$log = $log;
    this.ToastrUtil = ToastrUtil;
    this.FileUploadErrorModal = FileUploadErrorModal;
    this.FileDeleteConfirmationModal = FileDeleteConfirmationModal;
    this.BudgetService = BudgetService;
    this.FinanceService = FinanceService;
    this.fYearFilter = fYearFilter;
    this.NgbModal = NgbModal
  }

  $onInit(){
    super.$onInit();
    this.data = this.projectBlock || {};
    this.originalData = angular.copy(this.data);

    this.uploadParams = {
      orgId: this.project.organisation.id,
      programmeId: this.project.programmeId,
      projectId: this.project.id,
      blockId: this.projectBlock.id
    };
    this.selectedDocumentType = null;
    this.documentTypes = ['DAR', 'ADD', 'DD', 'MD', 'Other'];
    // this.filesToUpload = [];

    this.toDateFromLimit = 0;

    this.$rootScope.showGlobalLoadingMask = true;
    this.yearData = null;
    this.failuresData = null;
    // this.currentYear = null;
    this.currentYearAnnualSpend = null;
    this.budgetLastModified = null;
    this.showBudgetInvalid = false;
    this.forecastLedgerTypes = null;
    this.forecastSpendTypes = null;

    this.summaryExpanded = true;

    this.showOutOfRangeWarning = false;

    this.loadPageData();
  }


  loadPageData() {
    // this.currentYear = +(moment().format('YYYY'));
    this.ProjectService.getCurrentFinancialYear()
      .then(resp => {

        const year = resp.data;
        this.realCurrentYear = year;

        this.fromDateSelected = {
          label: this.data.fromDate,
          financialYear: this.data.fromFinancialYear || this.realCurrentYear
        };


        if (this.data.toFinancialYear) {
          this.toDateSelected = {
            label: this.data.toDateSelected || this.data.toDate,
            financialYear: this.data.toFinancialYear
          };
        }

        this.onFromDateChange(true);
        this.shouldShowOutOfRangeWarning();
        this.getProjectBudgetsData();

        this.realCurrentYearAnnualSpend = year;

        this.yearSelected(this.blockSessionStorage.currentYear ||
          {
            label: DateUtil.toFinancialYearString(year),
            financialYear: year
          }
        );

        this.setupForecastData();
      });
  }

  /**
   * Retrieve project budgets specific data
   * @returns {Object} promise
   */
  getProjectBudgetsData() {
    return this.BudgetService.retrieveProjectBudgets(this.project.id,this.blockId)
      .then((resp) => {
        this.refreshData(resp.data);
      });
  }

  refreshData(block){
    this.populatedYears = block.populatedYears;
    this.summaryData = this.processSummaryData(block.projectBudgetsYearlySummary);
    this.totals = block.totals;
    this.capitalWbsCodes = _.filter(block.wbsCodes, {type: 'CAPITAL'});
    this.revenueWbsCodes = _.filter(block.wbsCodes, {type: 'REVENUE'});

    this.data.fromFinancialYear = block.fromFinancialYear;
    this.data.toFinancialYear = block.toFinancialYear;

    this.shouldShowOutOfRangeWarning();
  }

  onFileUploadProgress(data) {
    this.$rootScope.showGlobalLoadingMask = true;
    const progress = data.progress;
    this.$log.debug(`progress: ${progress}%`);
  }

  onFileUploadComplete(resp) {
    var file = resp.response;
    //allows the digest cycle to reload the template on change
    this.$rootScope.showGlobalLoadingMask = false;

    this.$scope.$evalAsync(() => {
      this.data.attachments.push({
        fileId: file.id,
        fileName: file.fileName,
        documentType: this.selectedDocumentType
      });
      this.ToastrUtil.success('Added');
      this.autoSave();

      this.$log.debug('upload complete', file);
    });
  }

  onFileUploadError(error) {
    this.$rootScope.showGlobalLoadingMask = false;
    this.$log.debug('upload error:', error);
    var modal = this.FileUploadErrorModal.show(this, error);
    modal.result.then(function () {
    });
  }

  removeAttachment(attachment) {
    var modal = this.FileDeleteConfirmationModal.show(this, attachment);
    modal.result.then(() => {
      _.remove(this.data.attachments, {id: attachment.id});
      this.autoSave();
    });
  }

  /**
   * Filters 'to date' dropdown values to be in the future
   */
  onFromDateChange(skipAutoSave) {
    const dif = this.realCurrentYear - this.fromDateSelected.financialYear;
    this.toDateFromLimit = dif < 0 ? dif : 0;
    if(this.data.fromDateSelected > this.data.toDateSelected){
      this.data.toDateSelected = null;
    }
    if(!skipAutoSave){
      this.autoSave();
    }
  }
  onToDateChange() {
    this.autoSave();
  }

  onCapitalChange() {
    this.autoSave();
  }

  autoSave() {
    this.submit(true);
  }

  processSummaryData(projectBudgetsYearlySummary) {

    // get data from back end in swagger
    var summaryData = {
      years: []
    };
    if (!projectBudgetsYearlySummary) {
      this.$log.log('no projectBudgetsYearlySummary returned');
      return summaryData;
    }
    var years = [];
    _.forEach(projectBudgetsYearlySummary.summaryEntries, (entry) => {
      var transformedEntry;
      if (years[entry.financialYear]) {
        transformedEntry = years[entry.financialYear];
      } else {
        transformedEntry = {
          financialYear: entry.financialYear,
          // entry.financialYear + 1 to get the end the financial year aka the following
          // calendar year
          // %100 to get the last 2 digits only
          // 2016 -> 2016+1 = 2017 -> 2017 % 100 = 17
          // range for financialYear 2016 will be 2016/17
          range: entry.financialYear + '/' + ((entry.financialYear + 1) % 100),
          capitalForcast: null,
          capitalSpent: null,
          revenueForcast: null,
          revenueSpent: null
        };

      }
      if (entry.spendType === 'CAPITAL') {
        transformedEntry.capital = true;
        transformedEntry.capitalActual = entry.actualValue;
        transformedEntry.capitalForecast = entry.forecastValue;
        transformedEntry.capitalRemaining = entry.remainingForecastAndActuals;
      } else {
        transformedEntry.revenue = true;
        transformedEntry.revenueActual = entry.actualValue;
        transformedEntry.revenueForecast = entry.forecastValue;
        transformedEntry.revenueRemaining = entry.remainingForecastAndActuals;
      }
      years[entry.financialYear] = transformedEntry;
      if (transformedEntry.capital && transformedEntry.revenue) {
        summaryData.years.push(transformedEntry);
      }
    });

    summaryData.total = {
      totalActual: projectBudgetsYearlySummary.projectBudgetsAllYearSummary.actualValueTotal,
      totalForecast: projectBudgetsYearlySummary.projectBudgetsAllYearSummary.forecastValueTotal,
      totalRemaining: projectBudgetsYearlySummary.projectBudgetsAllYearSummary.remainingForecastAndActualsTotal,
    }

    return summaryData;
  }

  parseUpdateDate() {
    return moment(this.data.lastModified).format('DD/MM/YYYY [at] HH:mm');
  }

  /**
   * Submit data
   */
  submit(autoSave) {
    this.loading = true;

    let data = this.data;

    //TODO why BE is expecting this string instead of year? 2015/16 for example instead of 2015
    data.fromDate = this.fYearFilter(this.fromDateSelected.financialYear);
    data.fromDateFinancialYear = this.fromDateSelected.financialYear;
    if (this.toDateSelected) {
      //TODO why BE is expecting this string instead of year? 2015/16 for example instead of 2015
      data.toDate = this.fYearFilter(this.toDateSelected.financialYear);
      data.toDateFinancialYear = this.toDateSelected.financialYear;
    }

    let p = this.$q.all(this.requestsQueue).then(results => {
      return this.BudgetService.saveProjectBudgets(this.project.id, data, !autoSave)
        .then(resp => {
          this.$log.debug(resp);
          this.$rootScope.showGlobalLoadingMask = false;
          if(autoSave){
            this.data.attachments = resp.data.attachments;
            this.refreshData(resp.data);
          }
        })
        .catch(err => {
          this.$log.error(err);
          this.$rootScope.showGlobalLoadingMask = false;
        });
      });

    return this.addToRequestsQueue(p);
  }

  resetApprovalAmountToOriginal(){
    this.data.revenue = this.originalData.revenue;
    this.data.capital = this.originalData.capital;
    return this.data;
  }


  hasApprovalAmountChanged() {
    let hasRevenueChanged = this.originalData.revenue !== this.data.revenue;
    let hasCapitalChanged = this.originalData.capital !== this.data.capital;
    return hasRevenueChanged || hasCapitalChanged;
  }


  shouldShowOutOfRangeWarning() {
    this.showOutOfRangeWarning = false;
    this.showOutOfRangeWarningFrom = false;
    this.showOutOfRangeWarningTo= false;
    let min = _.min(this.populatedYears);
    let max = _.max(this.populatedYears);

    this.minDatePopulatedFormatted = min +'/'+ moment(min+1, 'YYYY').format('YY');
    this.maxDatePopulatedFormatted = max +'/'+ moment(max+1, 'YYYY').format('YY');

    let from = this.data.fromFinancialYear;
    let to = this.data.toFinancialYear || from;

    if(this.readOnly){
      if(!this.data.fromDate && !this.toDate){
        this.showOutOfRangeWarning = false;
      } else {
        if(min < from || max > to) {
          this.showOutOfRangeWarning = true;
        }
      }
    } else {
      if(min < from){
        this.showOutOfRangeWarningFrom = true;
      }
      if(max > to){
        this.showOutOfRangeWarningTo = true;
      }
    }
  }

  back() {
    if (this.readOnly || !this.data) {
      this.returnToOverview();
    } else {
      this.submit();
    }
  }

  // ANNUAL SPEND ctrl
  /**
   * Load data for a financial year
   * @param {Number} year
   * @return {Object} promise
   */
  loadDataForYear(year) {
    this.loading = true;
    this.$rootScope.showGlobalLoadingMask = true;
    return this.ProjectService.getProjectBudget(this.project.id, this.projectBlock.id, year)
      .then(resp => {
        this.$log.debug(resp);
        this.parseAnnualBudgetData(resp.data);
        this.loading = false;
        this.validateAnnualBudget();
        this.$rootScope.showGlobalLoadingMask = false;
      });
  }

  /**
   * Build forecast data
   * @returns {Object} promise
   */
  setupForecastData() {
    this.forecastLedgerTypes = ForecastDataUtil.getLedgerTypes();
    this.forecastSpendRecurrence = ForecastDataUtil.getSpendRecurrence();
    return this.FinanceService.getSpendCategories()
      .subscribe(categories => {
        this.forecastSpendCategories = categories;
      });
  }

  /**
   * Parse loaded data
   * @param {Object} data
   */
  parseAnnualBudgetData(yearSummary) {
    this.yearData = yearSummary;
    this.budgetLastModified = this.yearData.lastModified ?
      moment(this.yearData.lastModified).format('DD/MM/YYYY [at] HH:mm') :
      null;
  }



  /**
   * Change currently selected year and loads the year data from the server
   * @param {String} yearStr
   */
  yearSelected(financialYear) {
    this.$log.log('year selected', financialYear);
    this.currentYearAnnualSpend = {
      financialYear: financialYear
    };
    this.blockSessionStorage.currentYear = financialYear;
    this.loadDataForYear(financialYear.financialYear);
  }

  /**
   * Validade current Annual budget data
   */
  validateAnnualBudget() {
    const data = this.yearData;
    this.showBudgetInvalid = !this.readOnly && (data.annualBudgetCapital == null || data.annualBudgetRevenue == null);
  }

  /**
   * Annual budget input change handler
   */
  onAnnualBudgetChange() {
    this.validateAnnualBudget();
  }
  /**
   * Annual budget input blur handler
   */
  onAnnualBudgetBlur() {
    this.validateAnnualBudget();
    this.saveAnnualBudget();
  }

  /**
   * Add spend handler
   */
  onAddSpendForecast(event) {
    this.$rootScope.showGlobalLoadingMask = true;

    console.log('event:', event);

    // flag table row for expansion
    let tableState =
      this.blockSessionStorage.forecastTable ?
      this.blockSessionStorage.forecastTable :
      this.blockSessionStorage.forecastTable = {};
    let tableExpandedState =
      tableState.expanded ?
      tableState.expanded :
      tableState.expanded = [];
    const label = moment(event.data.month, 'MM').format('MMM').toUpperCase();
    if(tableExpandedState.indexOf(label) < 0) {
      tableExpandedState.push(label);
    }
    let p =  this.BudgetService.updateAnnualSpendForecast(this.project.id, this.currentYearAnnualSpend.financialYear.financialYear, event.data, false)
      .then(resp => {
        this.loadPageData();
        // this.parseAnnualBudgetData(resp.data);
        this.loading = false;
        this.$rootScope.showGlobalLoadingMask = false;
      });

    return this.addToRequestsQueue(p);
  }

  /**
   * Remove spend handler
   */
  onRemoveSpendForecast(event) {
    const data = event.data;
    this.$rootScope.showGlobalLoadingMask = true;

    let p =  this.BudgetService.deleteAnnualSpendForecast(
      this.project.id, this.projectBlock.id, this.currentYearAnnualSpend.financialYear.financialYear, data.month, data.entityType, data.categoryId, data.year)
      .then(resp => {
        this.parseAnnualBudgetData(resp.data);
        this.loading = false;
        this.$rootScope.showGlobalLoadingMask = false;
      });
    return this.addToRequestsQueue(p);
  }

  showMetadataModal(event){
    this.$rootScope.showGlobalLoadingMask = true;
    let actualKey = event.spendType === 'REVENUE'? 'revenueActual' : 'capitalActual';
    let actual = event.spend[actualKey];
    let isCR = actual < 0;
    this.BudgetService.getBudgetsMetadata(this.project.id, this.blockId, event.spend.categoryId, event.data.yearMonth).then((rsp) => {
      this.$rootScope.showGlobalLoadingMask = false;
      let modal = this.NgbModal.open(ActualsMetadataModalComponent, {size: 'lg'})
      modal.componentInstance.data = rsp.data
      modal.componentInstance.title = event.spend.spendCategory
      modal.componentInstance.spendType = event.spendType
      modal.componentInstance.isCR = isCR
    });
  }

  /**
   * Save annual budget
   * @param {Boolean} releaseLock
   * @return {Object} promise
   */
  saveAnnualBudget(releaseLock) {
    let p =  this.BudgetService.updateAnnualSpend(this.project.id, this.projectBlock.id, this.currentYearAnnualSpend.financialYear.financialYear, this.yearData, releaseLock)
      .then(resp => {
        this.parseAnnualBudgetData(resp.data);
      });

    return this.addToRequestsQueue(p);
  }

  /**
   * Save annual forecast
   * @param {Boolean} releaseLock
   * @return {Object} promise
   */
  saveAnnualForecast(releaseLock) {
    this.$rootScope.showGlobalLoadingMask = true;
    let p =  this.BudgetService.updateAnnualSpendForecast(this.project.id, this.currentYearAnnualSpend.financialYear.financialYear, this.projectBlock, releaseLock)
      .then(resp => {
        this.parseAnnualForecastData(resp.data);
        this.$rootScope.showGlobalLoadingMask = false;
      });
    return this.addToRequestsQueue(p);
  }
}

ProjectBudgetCtrl.$inject = ['project', '$injector', '$scope', '$log', 'FileUploadErrorModal', 'FileDeleteConfirmationModal', 'ToastrUtil', 'BudgetService', 'FinanceService', 'fYearFilter', 'NgbModal'];

angular.module('GLA')
  .controller('ProjectBudgetCtrl', ProjectBudgetCtrl);
