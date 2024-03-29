import {Component, EventEmitter, Input, OnChanges, OnInit, Output} from '@angular/core';
import {filter, find, sortBy} from "lodash-es";

@Component({
  selector: 'gla-checkbox-filter',
  templateUrl: './checkbox-filter.component.html',
  styleUrls: ['./checkbox-filter.component.scss']
})
export class CheckboxFilterComponent implements OnInit, OnChanges {

  @Input() filterDropdownItems: any[]
  @Input() grouped: boolean
  @Input() isDisabled: boolean
  @Output() onChange: EventEmitter<any> = new EventEmitter()
  @Output() onCollapseExpandToggle: EventEmitter<any> = new EventEmitter()

  dropdownItems: any[];
  private selections: any[];
  hasFilterSelections: boolean;
  title: string;

  constructor() { }

  ngOnInit(): void {
    this.init();
  }

  ngOnChanges(changes){
    this.init();
  }

  init(){
    this.dropdownItems = this.grouped? this.groupToFlat(this.filterDropdownItems) : this.filterDropdownItems;
    this.dropdownItems = sortBy(this.dropdownItems, 'displayOrder');
    this.countSelections();
  }

  countSelections(){
    this.selections = filter(this.dropdownItems, {model:true});
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
  }

  onCheckboxesChange(checkbox?, checkboxEl?){
    if(checkbox && checkbox.items){
      checkbox.items.forEach(c => c.model = checkbox.model);
    }

    if(checkbox && checkbox.groupId){
      let group = find(this.dropdownItems, {id: checkbox.groupId});
      let hasAnyChecked = group.items.some(c=> c.model);
      group.model = hasAnyChecked;
    }
    this.countSelections();
    this.onChange.emit(checkbox);
  }

  toggle(group){
    group.collapsed = !group.collapsed;
    (group.items || []).forEach(item => item.collapsed = group.collapsed);
    if(this.onCollapseExpandToggle) {
      this.onCollapseExpandToggle.emit();
    }
  }

}
