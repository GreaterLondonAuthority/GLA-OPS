/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class Pagination {
  constructor($timeout){
    this.$timeout = $timeout;
  }

  calculateIndex() {
    this.indexStart = (this.totalItems == 0) ? 0 : (this.itemsPerPage * (this.currentPage - 1)) + 1;
    this.indexEnd = _.min([(this.totalItems == 0) ? 0 : this.indexStart + this.numberOfElements - 1, this.totalItems]);
    return true;
  }

  changePage(){
    this.$timeout(()=>{
      this.onChange();
    });
  }

  showPages(){
    return this.totalItems > this.itemsPerPage;
  }
}
/*
      this.indexStart = (this.totalItems == 0) ? 0 : (this.itemsPerPage * (this.currentPage - 1)) + 1;
      this.indexEnd = (this.totalItems == 0) ? 0 : this.indexStart + response.numberOfElements - 1;
 */
Pagination.$inject = ['$timeout'];


gla.component('pagination', {
  templateUrl: 'scripts/components/pagination/pagination.html',
  controller: Pagination,
  bindings: {
    currentPage: '=',
    itemsPerPage: '<',
    totalItems: '<',
    numberOfElements: '<',
    onChange: '&'
  },
});

