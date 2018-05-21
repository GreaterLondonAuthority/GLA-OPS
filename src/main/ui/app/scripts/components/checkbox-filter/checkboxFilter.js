/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class CheckboxFilterCtrl {


  $onInit() {
    this.countSelections();
  }

  $onChanges(changes){
    this.countSelections();
  }


  countSelections(){
    this.selections = _.filter(this.filterDropdownItems, {model:true});
    this.hasFilterSelections = this.selections.length > 0;
    this.title = this.getFilterMenuButtonText();
  }




  getFilterMenuButtonText() {
    if (!this.hasFilterSelections) {
      return 'None selected';
    } else if (this.selections.length === this.filterDropdownItems.length) {
      return 'All selected';
    } else {
      return 'Filter applied';
    }
  }

  toggleAllFilters($event, selectAll) {
    this.filterDropdownItems.forEach(item => item.model = !!selectAll);
    this.countSelections();
    this.onCheckboxesChange();
    $event.stopPropagation();
  };

  onCheckboxesChange(){
    console.log('change');
    this.countSelections();
    this.onChange();
  }
}

gla.component('checkboxFilter', {
  templateUrl: 'scripts/components/checkbox-filter/checkboxFilter.html',
  controller: CheckboxFilterCtrl,
  bindings: {
    filterDropdownItems: '<',
    onChange: '&'
  },
});

