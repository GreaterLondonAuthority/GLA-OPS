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
    this.init();
  }

  $onChanges(changes){
    this.init();
  }

  init(){
    this.dropdownItems = this.grouped? this.groupToFlat(this.filterDropdownItems) : this.filterDropdownItems;
    this.countSelections();
  }


  countSelections(){
    this.selections = _.filter(this.dropdownItems, {model:true});
    this.hasFilterSelections = this.selections.length > 0;
    this.title = this.getFilterMenuButtonText();
  }

  groupToFlat(groupedCheckboxes){
    let flatCheckboxes = [];
    groupedCheckboxes.forEach(checkbox => {
      flatCheckboxes.push(checkbox);
      if(checkbox.items && checkbox.items.length){
        checkbox.items.forEach(c => c.groupId = checkbox.id);
        checkbox.model = checkbox.items.some(c => c.model);
        checkbox.collapsed = checkbox.items.some(c => c.collapsed);
        flatCheckboxes = flatCheckboxes.concat(checkbox.items);
      }
    });
    return flatCheckboxes;
  }

  getFilterMenuButtonText() {
    if (!this.hasFilterSelections) {
      return 'None selected';
    } else if (this.selections.length === this.dropdownItems.length) {
      return 'All selected';
    } else {
      return 'Filter applied';
    }
  }

  toggleAllFilters($event, selectAll) {
    this.dropdownItems.forEach(item => item.model = !!selectAll);
    this.countSelections();
    this.onCheckboxesChange();
    $event.stopPropagation();
  };

  onCheckboxesChange(checkbox){
    if(checkbox && checkbox.items){
      checkbox.items.forEach(c => c.model = checkbox.model);
    }

    if(checkbox && checkbox.groupId){
      let group = _.find(this.dropdownItems, {id: checkbox.groupId});
      let hasAnyChecked = group.items.some(c=> c.model);
      group.model = hasAnyChecked;
    }
    this.countSelections();
    this.onChange();
  }

  toggle(group){
    group.collapsed = !group.collapsed;
    (group.items || []).forEach(item => item.collapsed = group.collapsed);
    if(this.onCollapseExpandToggle) {
      this.onCollapseExpandToggle();
    }
  }
}

gla.component('checkboxFilter', {
  templateUrl: 'scripts/components/checkbox-filter/checkboxFilter.html',
  controller: CheckboxFilterCtrl,
  bindings: {
    filterDropdownItems: '<',
    grouped: '<?',
    onChange: '&',
    isDisabled: '<?',
    onCollapseExpandToggle: '&?'
  },
});

