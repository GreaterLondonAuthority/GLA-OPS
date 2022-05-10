/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class AllPaymentsCtrl {
  constructor(orderByFilter, PaymentService, UserService, $log, SessionService, $state, $stateParams, programmes, $q, paymentSources) {
    this.orderByFilter = orderByFilter;
    this.$log = $log;
    this.$state = $state;
    this.$stateParams = $stateParams;
    this.SessionService = SessionService;
    this.PaymentService = PaymentService;
    this.UserService = UserService;
    this.paymentSources = paymentSources;
    this.$q = $q;
    this.requestsQueue = [];


    this.formats = ['dd/MM/yyyyy','dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate'];
    this.format = this.formats[0];

    this.dateFormat = 'dd/MM/yyyy';
    this.fromDateOptions = {
      formatYear: 'yy',
      startingDay: 1,
      showWeeks: false
    };
    this.fromDatePopup = {
      opened: false
    };
    this.toDateOptions = {
      formatYear: 'yy',
      startingDay: 1,
      showWeeks: false,
    };
    this.toDatePopup = {
      opened: false
    };


    // Pagination variables
    this.totalItems = 0;
    this.itemsPerPage = 50;

    //Current page starts by 1 in UI but by 0 in backend
    this.currentPage = 1;
    this.sortByName = 'name';
    this.sortReverse = false;

    this.defaultManagingOrgsFilter = _.filter(_.sortBy(this.UserService.currentUserOrganisations(), 'name'), {isManagingOrganisation: true})
      .map(o => {
        return {
          id: o.id,
          label: o.name
        }
      });


    this.searchOptions = PaymentService.searchOptions();
    this.sourceDropdownItems = this.applyFilterState(PaymentService.sourceOptions(this.paymentSources));
    this.statusDropdownItems = this.applyFilterState(PaymentService.statusOptions());
    this.paymentDirectionDropdownItems = this.applyFilterState(PaymentService.paymentDirectionOptions());
    this.managingOrgDropdownItems = this.applyFilterState(this.defaultManagingOrgsFilter);

    let programmeDropdown = _.filter(programmes, (p)=>{return p.status != 'Abandoned';});

    this.programmeDropDownItems = this.applyFilterState(this.enhanceProgrammes(_.sortBy(programmeDropdown,'name')));
    this.applyDateFilters();

    let paymentsSearchState = this.getSearchParams();

    this.byProjectOption = _.find(this.searchOptions,{name: 'project'});
    this.byProgrammeOption = _.find(this.searchOptions,{name: 'programme'});
    this.byOrganisationOption = _.find(this.searchOptions,{name: 'organisation'});
    this.bySapId = _.find(this.searchOptions,{name: 'sapId'});

    if (paymentsSearchState.organisation) {
      this.selectedSearchOption = this.byOrganisationOption;
      this.searchTextModel = paymentsSearchState.organisation;
    } else if (paymentsSearchState.programme) {
      this.selectedSearchOption = this.byProgrammeOption;
      this.searchTextModel = paymentsSearchState.programme;
    } else if (paymentsSearchState.sapId) {
      this.selectedSearchOption = this.bySapId;
      this.searchTextModel = paymentsSearchState.sapId;
    }
    else {
      this.selectedSearchOption = this.byProjectOption;
      this.searchTextModel = paymentsSearchState.project;
    }
    this.searchText = this.searchTextModel;
  }

  $onInit(){
    this.updateFilters(true);
  }

  clearFiltersAndSearch(){
    this.SessionService.resetPaymentsFilterState();
    this.SessionService.clearPaymentsState();
    this.clearSearch();
  };

  select(searchOption) {
    this.searchTextModel = null;
    this.selectedSearchOption = searchOption;
    this.$log.log('this.selectedSearchOption', searchOption)
  }

  search() {
    this.setSearchParams({
      [this.selectedSearchOption.name]: this.searchTextModel
    });
    this.$state.go(this.$state.current, this.$stateParams, {reload: true});
  }

  clearSearch() {
    this.setSearchParams({});
    this.$state.go(this.$state.current, this.$stateParams, {reload: true});
  }
  setSearchParams(searchParams){
    searchParams = searchParams || {};
    Object.keys(this.$stateParams).forEach(key => this.$stateParams[key] = searchParams[key]);
    this.SessionService.setPaymentsSearchState(searchParams);
  }

  getSearchParams(){
    if(this.hasUrlParameter()){
      return this.$stateParams;
    }
    return this.SessionService.getPaymentsSearchState();
  }

  saveFilterState(filterDropdownItems) {
    let filterState = {};
    _.forEach(filterDropdownItems, (filter) => {
      filterState[filter.name] = filter.model;
    });
    this.SessionService.setPaymentsFilterState(filterState);
  };

  applyDateFilters() {
    let filterState = this.SessionService.getPaymentsFilterState();
    if(filterState.toDate) {
      this.toDate = new Date(filterState.toDate);
      this.fromDateOptions.maxDate = this.toDate;

    }
    if(filterState.fromDate){
      this.fromDate = new Date(filterState.fromDate);
      this.toDateOptions.minDate = this.fromDate;
    }
  }

  applyFilterState(filterDropdownItems) {
    let filterState = this.SessionService.getPaymentsFilterState();
    _.forEach(filterDropdownItems, (filter) => {
      filter.model = _.isBoolean(filterState[filter.name]) ? filterState[filter.name] : false;
    });
    return filterDropdownItems;
  };

  hasUrlParameter(){
    return Object.keys(this.$stateParams).some(key => this.$stateParams[key]);
  };

  updateFilters(resetPage){

    this.dateFilteringConditionsMet();
    this.isDefaultFilterState = !(_.some(this.statusDropdownItems, {model: true}) ||
                                  _.some(this.sourceDropdownItems, {model: true}) ||
                                  _.some(this.paymentDirectionDropdownItems, {model: true}) ||
                                  _.some(this.managingOrgDropdownItems, {model: true}) ||
                                  _.some(this.programmeDropDownItems, {model: true}));

    this.saveFilterState(_.concat(
      this.statusDropdownItems,
      this.sourceDropdownItems,
      this.paymentDirectionDropdownItems,
      this.programmeDropDownItems,
      this.managingOrgDropdownItems,
      {name: 'fromDate', model: this.fromDate},
      {name: 'toDate', model: this.toDate}
    ));

    let statuses = []
    _.forEach(this.statusDropdownItems, status => {
      if(status.model){
        //check for sub statuses and if they exist send those to API instead of status
        if(status.items){
          _.forEach(status.items, item => {
            if (item.model) {
              statuses.push(item.statusKey)
            }
          })
        }
        else {
          statuses.push(status.statusKey);
        }
      }
    });

    let sources = _.map(_.filter(this.sourceDropdownItems, {model:true}), 'sourceKey') || [];
    let programmes = _.map(_.filter(this.programmeDropDownItems, {model:true}), 'sourceKey') || [];
    let managingOrganisations = _.map(_.filter(this.managingOrgDropdownItems, {model:true}), 'id') || [];
    let paymentDirection = _.map(_.filter(this.paymentDirectionDropdownItems, {model:true}), 'sourceKey') || [];

    let projectIdOrName;
    let orgName;
    let programmeName;
    let sapId;

    let isProgrammeOption = false;
    if(this.selectedSearchOption.name === this.byProjectOption.name){
      projectIdOrName = this.searchTextModel;
    } else if(this.selectedSearchOption.name === this.byOrganisationOption.name){
      orgName = this.searchTextModel;
    } else if(this.selectedSearchOption.name === this.byProgrammeOption.name){
      programmeName = this.searchTextModel;
      isProgrammeOption = true;
    } else if(this.selectedSearchOption.name === this.bySapId.name){
      sapId = this.searchTextModel;
    }



    var page = resetPage ? 0 : this.currentPage - 1;
    var size = this.itemsPerPage;

    var sort = [
      'displayDate,desc',
      'projectId,asc'
    ];

    this.$q.all(this.requestsQueue).then(() => {
      let p = this.PaymentService.getPayments(projectIdOrName,
        orgName,
        programmeName,
        sapId,
        statuses,
        sources,
        isProgrammeOption ? null : programmes,
        managingOrganisations,
        this.toDate,
        this.fromDate,
        paymentDirection,
        page,
        size,
        sort).then(response => {

        // TODO review sorting
        this.payments = response.content;

        if (resetPage) {
          this.currentPage = 1;
        }
        this.totalItems = response.totalElements;
      });
      this.requestsQueue.push(p);
      p.finally(() => _.remove(this.requestsQueue, p));
      return p;
    })
  }

  enhanceProgrammes(programmes) {
    return _.map(programmes, (programme) => {
      return {
        checkedClass: programme.name,
        ariaLabel: programme.name,
        name: programme.name,
        // default value set in applyFilterState
        model: undefined,
        label: programme.name,
        sourceKey: programme.name
      };
    });
  }

  openFromDate() {
    this.fromDatePopup.opened = true;
  }
  openToDate() {
    this.toDatePopup.opened = true;
  }
  changeFromFilterDates(date){
    this.toDateOptions.minDate = date;
    this.updateFilters(true);
  }
  changeToFilterDates(date){
    this.fromDateOptions.maxDate = date;
    this.updateFilters(true);
  }

  dateFilteringConditionsMet() {
    let condition = false;
    // this.invalidForShowDateFiltering = false;

    let activeStatus = _.map(_.filter(this.statusDropdownItems,'model'), 'statusKey');
    if(activeStatus.indexOf('Authorised')!== -1){
      if(activeStatus.length === 1){
        condition = true;
      } else {
        // this.invalidForShowDateFiltering = true;
      }
    }

    if(!condition){
      this.toDate = null;
      this.fromDate = null;
    }

    this.showDateFiltering = condition;
  }
}

AllPaymentsCtrl.$inject = ['orderByFilter', 'PaymentService', 'UserService', '$log', 'SessionService', '$state', '$stateParams', 'programmes', '$q', 'paymentSources'];

angular.module('GLA').controller('AllPaymentsCtrl', AllPaymentsCtrl);
