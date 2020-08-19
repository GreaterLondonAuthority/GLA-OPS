/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class templatesPageCtrl {
  constructor($rootScope, $state, TemplateService, JSONViewerDialog, SessionService) {
    this.$rootScope = $rootScope;
    this.TemplateService = TemplateService;
    this.JSONViewerDialog = JSONViewerDialog;
    this.SessionService = SessionService;
    this.$state = $state;
    this.$rootScope.showGlobalLoadingMask = true;
  }

  $onInit() {
    this.searchOptions = [
      {
        name: 'text',
        description: 'Template name or ID',
        hint: 'Search by template name or ID',
        maxLength: '255'
      },
      {
        name: 'text',
        description: 'Programme text or ID',
        hint: 'Search by programme text or ID',
        maxLength: '255'
      }
    ];

    this.templateStatusFilters = [
      {
        id: 'Draft',
        label: 'Draft'
      },
      {
        id: 'Active',
        label: 'In use'
      }
    ];

    this.cachedTemplatesFilter = this.SessionService.getTemplatesFilter() || {};
    this.searchText = this.cachedTemplatesFilter.searchText;
    this.selectedSearchOption = this.searchOptions[this.cachedTemplatesFilter.selectedSearchOptionIndex || 0];
    this.templateStatusFilters = this.cachedTemplatesFilter.templateStatusFilters || this.templateStatusFilters;

    this.totalItems = this.cachedTemplatesFilter.totalItems || 0;
    this.indexStart = this.cachedTemplatesFilter.indexStart || 0;
    this.indexEnd = this.cachedTemplatesFilter.indexEnd || 0;
    this.itemsPerPage = this.cachedTemplatesFilter.itemsPerPage || 50;
    //Current page starts by 1 in UI but by 0 in backend
    this.currentPage = this.cachedTemplatesFilter.currentPage || 1;
    this.search(false);
  }

  templateSelected(template) {
    this.$state.go('system-template-details', {
      templateId: template.id
    });
  }

  getTemplates(resetPage, selectedTemplateStatuses) {
    if (resetPage) {
      this.currentPage = 1;
    }

    let page = this.currentPage - 1;
    let programmeText = this.isSearchByProgramme() ? this.searchText : null;
    let templateText = this.isSearchByProgramme() ? null : this.searchText;

    this.TemplateService.getAllProjectTemplateSummaries(page, programmeText, templateText, selectedTemplateStatuses).then(rsp => {
      this.$rootScope.showGlobalLoadingMask = false;
      this.templates = rsp.data.content;
      this.totalItems = rsp.data.totalElements;
      this.SessionService.setTemplatesFilter({
        searchText: this.searchText,
        selectedSearchOptionIndex: this.searchOptions.indexOf(this.selectedSearchOption),
        templateStatusFilters: this.templateStatusFilters,
        totalItems: this.totalItems,
        indexStart: this.indexStart,
        indexEnd: this.indexEnd,
        itemsPerPage: this.itemsPerPage,
        currentPage: page + 1
      });
    });

  }

  select(searchOption) {
    _.forEach(this.templateStatusFilters, status => {
      status.model = false;
    });

    this.searchText = null;
    this.selectedSearchOption = searchOption;
    this.getTemplates(true);
  }

  search(resetPage) {
    const selectedTemplateStatuses = this.getTemplateStatusesSelected() || [];
    this.getTemplates(resetPage, selectedTemplateStatuses);
  }

  getTemplateStatusesSelected() {
    return this.templateStatusFilters
      .filter(status => status.model)
      .map(status => status.id);
  }

  clearSearchText() {
    this.searchText = null;
    this.search(true);
  }

  isSearchByProgramme() {
    return this.searchOptions.indexOf(this.selectedSearchOption) === 1;
  }

  createNewTemplate() {
    // this.JSONViewerDialog.create();
    this.$state.go('system-template-details-create');
  }

  stateModelText(stateModel) {
    return _.startCase(stateModel.name);
  }

  templateStatusText(status) {
    switch (status) {
      case 'Active' :
        return 'In use';
      default :
        return _.startCase(status);
    }
  }
}

templatesPageCtrl.$inject = ['$rootScope', '$state', 'TemplateService', 'JSONViewerDialog', 'SessionService'];

angular.module('GLA')
  .component('templatesPage', {
    templateUrl: 'scripts/pages/system/templates/templatesPage.html',
    bindings: {
      templates: '<'
    },
    controller: templatesPageCtrl
  });
