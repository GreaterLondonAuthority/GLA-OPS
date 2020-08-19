/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class Programmes {

  constructor(ProgrammeService, UserService, SessionService, $rootScope){
    this.ProgrammeService = ProgrammeService;
    this.UserService = UserService;
    this.searchText = '';
    this.SessionService = SessionService;
    this.$rootScope = $rootScope;
    this.$rootScope.showGlobalLoadingMask = true;

    this.defaultProgrammeStatusFilter = [
      {
        id: 'Active',
        label:'Active',
        model: true
      },
      {
        id: 'Archived',
        label:'Archived'
      },{
        id: 'Abandoned',
        label:'Abandoned'
      }
    ];

    this.defaultManagingOrgsFilter = _.filter(this.UserService.currentUserOrganisations(), {isManagingOrganisation: true})
      .map(o => {
        return {
          id: o.id,
          label: o.name
        }
      });

    this.searchState = this.SessionService.getProgrammeSearchState();
    this.searchText = this.searchState.searchText;
    this.programmeStatusDropdown = this.searchState.programmeStatusDropdown || angular.copy(this.defaultProgrammeStatusFilter);
    this.programmeManagingOrganisationsDropdown = this.searchState.programmeManagingOrganisationsDropdown || angular.copy(this.defaultManagingOrgsFilter);
    if (this.searchText) this.getProgrammeData();
  }

  $onInit(){
    this.initSearchDropdown();
    this.getProgrammeData();
  }


  initSearchDropdown() {
    this.searchOptions = [
      {
        name: 'title',
        description: 'Programme',
        hint: 'Enter programme ID or name',
        maxLength: '50'
      }
    ];

    this.selectedSearchOption = this.searchOptions[0];
  }

  getProgrammeData(){

    let selectedStatuses = _.filter(this.programmeStatusDropdown, {model: true}).map(s => s.id);
    let selectedManagingOrganisations = _.filter(this.programmeManagingOrganisationsDropdown, {model: true}).map(o => o.id);

    let params = {
      searchText : this.searchText,
      statuses: selectedStatuses,
      managingOrganisations: selectedManagingOrganisations
    };

    this.ProgrammeService.getProgrammes(params).then(rsp => {
      this.programmes = rsp.data.content;
      this.SessionService.setProgrammesSearchState({
        searchText: this.searchText,
        programmeStatusDropdown: this.programmeStatusDropdown,
        programmeManagingOrganisationsDropdown: this.programmeManagingOrganisationsDropdown
      });
      this.$rootScope.showGlobalLoadingMask = false;
    });

  }

  clearSearchText(){
    this.searchText = '';
    this.getProgrammeData();
  }

  isAnyFilterApplied() {
    return this.searchText
      || this.hasDropdownChanged(this.programmeStatusDropdown, this.defaultProgrammeStatusFilter)
      || this.hasDropdownChanged(this.programmeManagingOrganisationsDropdown, this.UserService.currentUserOrganisations());
  }

  hasDropdownChanged (dropdown,defaultDropdown){
    return dropdown.some(checkbox => {
      let defaultModel = _.find(defaultDropdown, {id: checkbox.id}).model;
      return (!!defaultModel) != (!!checkbox.model);
    });
  }

  clearFiltersAndSearch(){
    this.programmeStatusDropdown = angular.copy(this.defaultProgrammeStatusFilter);
    this.programmeManagingOrganisationsDropdown = angular.copy(this.defaultManagingOrgsFilter);
    this.clearSearchText();
  }

}

Programmes.$inject = ['ProgrammeService', 'UserService', 'SessionService', '$rootScope'];




angular.module('GLA')
  .component('programmesPage', {
    templateUrl: 'scripts/pages/programmes/programmes.html',
    bindings: {
    },
    controller: Programmes
  });

