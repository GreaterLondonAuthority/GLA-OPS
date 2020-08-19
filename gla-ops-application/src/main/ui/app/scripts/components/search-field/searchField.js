/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class SearchFieldCtrl {

  constructor($scope) {
    $scope.$watch('$ctrl.reset', (reset) => {
      if (reset) {
        this.$onInit();
        this.reset = false;
      }
    })
  }

  $onInit() {
    this.showClearButton = !!this.searchText;
    this.lastSearchText = this.searchText;
  }

  onKeyUp($event) {
    if ($event.keyCode == 13) {
      this.search()
    }
  }

  onChange() {
    if (this.searchText) {
      this.showClearButton = false;
    }
  }


  search() {
    this.lastSearchText = this.searchText;
    this.showClearButton = !!this.searchText;
    this.onSearch();
  }


  clear() {
    this.showClearButton = false;
    this.lastSearchText = null;
    this.searchText = null;
    this.onClear();
  }

  isSearchEnabled() {
    return this.searchText || this.lastSearchText;
  }
}

SearchFieldCtrl.$inject = ['$scope'];

gla.component('searchField', {
  templateUrl: 'scripts/components/search-field/searchField.html',
  controller: SearchFieldCtrl,
  bindings: {
    options: '<',
    selectedOption: '=',
    searchText: '=',
    onSelect: '&',
    onSearch: '&',
    onClear: '&',
    reset: '=?'
  }
});

