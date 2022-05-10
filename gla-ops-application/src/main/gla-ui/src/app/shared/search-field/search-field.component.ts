import {Component, EventEmitter, Input, OnChanges, OnInit, Output} from '@angular/core';

@Component({
  selector: 'gla-search-field',
  templateUrl: './search-field.component.html',
  styleUrls: ['./search-field.component.scss']
})
export class SearchFieldComponent implements OnInit {

  @Input() options: any[]
  @Input() selectedOption: any
  @Output() selectedOptionChange: EventEmitter<any> = new EventEmitter()
  @Input() searchText: string
  @Output() searchTextChange: EventEmitter<any> = new EventEmitter()
  @Output() onSearch: EventEmitter<any> = new EventEmitter()
  @Output() onSelect: EventEmitter<any> = new EventEmitter()
  @Output() onClear: EventEmitter<any> = new EventEmitter()

  showClearButton: boolean;
  lastSearchText: string;

  constructor() { }

  ngOnInit(): void {
    this.showClearButton = !!this.searchText;
    this.lastSearchText = this.searchText;
  }

  isSearchEnabled() {
    return this.searchText || this.lastSearchText;
  }

  onChange() {
    if (this.searchText) {
      this.showClearButton = false;
    }
    this.searchTextChange.emit(this.searchText);
  }

  select(){
    this.selectedOptionChange.emit(this.selectedOption)
    this.onSelect.emit(this.selectedOption)
  }

  search() {
    this.lastSearchText = this.searchText;
    this.showClearButton = !!this.searchText;
    this.onSearch.emit();
  }

  clear() {
    this.showClearButton = false;
    this.lastSearchText = null;
    this.searchText = null;
    this.onClear.emit();
  }

}
