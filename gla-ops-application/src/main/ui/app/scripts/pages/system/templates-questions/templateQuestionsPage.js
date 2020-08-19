/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class TemplatesQuestionsCtrl {
  constructor($rootScope, $state, QuestionsService, SessionService) {
    this.$rootScope = $rootScope;
    this.$state = $state;
    this.QuestionsService = QuestionsService;
    this.SessionService = SessionService;
    this.$rootScope.showGlobalLoadingMask = true;
  }


  $onInit() {
    this.searchOptions = [
      {
        name: 'text',
        description: 'Question text or ID',
        hint: 'Search by questions text or ID',
        maxLength: '255'
      },
      {
        name: 'text',
        description: 'Template name or ID',
        hint: 'Search by template name or ID',
        maxLength: '255'
      }
    ];
    this.cachedQuestionsFilter = this.SessionService.getQuestionsFilter() || {};
    this.searchText = this.cachedQuestionsFilter.searchText;
    this.selectedSearchOption = this.searchOptions[this.cachedQuestionsFilter.selectedSearchOptionIndex || 0];


    this.totalItems = 0;
    this.indexStart = 0;
    this.indexEnd = 0;
    this.itemsPerPage = 50;
    //Current page starts by 1 in UI but by 0 in backend
    this.currentPage = 1;
    this.getQuestions();
  }

  goToQuestionDetails(id) {
    this.$state.go('system-question', {
      'questionId': id
    });
  };

  getQuestions(resetPage) {
    if (resetPage) {
      this.currentPage = 1;
    }
    let page = this.currentPage - 1;
    let questionText = this.isSearchByQuestion()? this.searchText : null;
    let templateText = this.isSearchByQuestion()? null : this.searchText;
    this.QuestionsService.getQuestions(page, questionText, templateText).then(response => {
      this.$rootScope.showGlobalLoadingMask = false;
      this.questions = response.data.content;
      this.totalItems = response.data.totalElements;
      this.SessionService.setQuestionsFilter({
        searchText: this.searchText,
        selectedSearchOptionIndex: this.searchOptions.indexOf(this.selectedSearchOption)
      });
    });
  }

  select(searchOption) {
    this.searchText = null;
    this.selectedSearchOption = searchOption;
  }

  search() {
    this.getQuestions(true);
  }

  clearSearchText() {
    this.searchText = null;
    // this.selectedSearchOption = this.searchOptions[0];
    this.getQuestions(true);
  }

  isSearchByQuestion(){
    return this.searchOptions.indexOf(this.selectedSearchOption) === 0;
  }

}

TemplatesQuestionsCtrl.$inject = ['$rootScope', '$state', 'QuestionsService', 'SessionService'];

angular.module('GLA')
  .component('templatesQuestionsPage', {
    templateUrl: 'scripts/pages/system/templates-questions/templatesQuestionsPage.html',
    bindings: {
    },
    controller: TemplatesQuestionsCtrl
  });
