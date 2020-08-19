/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class AuditActivityCtrl {
  constructor($state, AuditService, $window, SessionService) {
    this.$state = $state;
    this.AuditService = AuditService;
    this.$window = $window;
    this.SessionService = SessionService;
  }

  $onInit() {
    this.auditEvents = [];
    this.currentPage = 1;
    this.itemsPerPage = 50;

    this.searchOptions = this.AuditService.searchOptions();
    this.selectedSearchOption = this.searchOptions[0];

    this.dateFormat = 'dd/MM/yyyy';
    this.fromDateOptions = {
      formatYear: 'yy',
      startingDay: 1,
      showWeeks: false
    };

    this.toDateOptions = {
      formatYear: 'yy',
      startingDay: 1,
      showWeeks: false,
    };

    this.initFilters();
    this.loading = true;
    this.getAuditEvents(true);
  }

  search() {
    this.getAuditEvents(true);
  }

  clearSearchText() {
    this.searchText = null;
    this.getAuditEvents(true);
  }

  clearAll() {
    this.resetSearch = true;
    this.SessionService.setAuditHistoryFilter(null);
    this.initFilters();
    this.clearSearchText();
  }

  initFilters() {
    let cachedFilter = this.SessionService.getAuditHistoryFilter() || {};

    this.searchText = cachedFilter.searchText;
    this.toDate = null;
    this.fromDateOptions.maxDate = null;

    this.fromDate = null;
    this.toDateOptions.minDate = null;

    if (cachedFilter.toDate) {
      this.toDate = new Date(cachedFilter.toDate);
      this.fromDateOptions.maxDate = this.toDate;
    }

    if (cachedFilter.fromDate) {
      this.fromDate = new Date(cachedFilter.fromDate);
      this.toDateOptions.minDate = this.fromDate;
    }
  }

  changeFromFilterDates(date) {
    this.toDateOptions.minDate = date;
    this.getAuditEvents(true);
  }

  changeToFilterDates(date) {
    this.fromDateOptions.maxDate = date;
    this.getAuditEvents(true);
  }

  back() {
    this.$window.history.back();
  }

  getAuditEvents(resetPage) {
    let page = resetPage ? 0 : this.currentPage - 1;
    let size = this.itemsPerPage;
    let sort = ['timestamp,desc'];
    this.AuditService.getPagedAuditEvents(page, sort, size, this.searchText, this.fromDate, this.toDate).then(rsp => {
      this.auditEvents = rsp.data.content;
      this.totalItems = rsp.data.totalElements;
      this.numberOfElements = rsp.data.numberOfElements;
      if (resetPage) {
        this.currentPage = 1;
      }

      this.SessionService.setAuditHistoryFilter({
        searchText: this.searchText,
        fromDate: this.fromDate,
        toDate: this.toDate
      });

      this.loading = false;
    })
  }

}

AuditActivityCtrl.$inject = ['$state', 'AuditService', '$window', 'SessionService'];

angular.module('GLA')
  .component('auditActivity', {
    templateUrl: 'scripts/pages/audit-activity/auditActivity.html',
    controller: AuditActivityCtrl
  });
