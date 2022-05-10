import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {min} from "lodash-es";

@Component({
  selector: 'gla-pagination',
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.scss']
})
export class PaginationComponent implements OnInit {

  @Input() currentPage: number
  @Output() currentPageChange = new EventEmitter<number>()

  @Input() itemsPerPage: number
  @Output() itemsPerPageChange = new EventEmitter<number>()

  @Input() totalItems: number
  @Input() numberOfElements: number
  @Input() showItemsPerPageDropdown: boolean

  @Output() onChange = new EventEmitter<void>()

  itemsPerPageDropdown: number[];
  indexStart: number;
  indexEnd: number;

  constructor() { }

  ngOnInit(): void {
    this.itemsPerPageDropdown = [5, 10, 20, 50];
  }


  calculateIndex() {
    this.indexStart = (this.totalItems == 0) ? 0 : (this.itemsPerPage * (this.currentPage - 1)) + 1;
    this.indexEnd = min([(this.totalItems == 0) ? 0 : this.indexStart + this.numberOfElements - 1, this.totalItems]);
    return true;
  }

  changePageSize(){
    this.currentPage = 1;
    this.itemsPerPageChange.emit(this.itemsPerPage);
    this.changePage(this.currentPage);
  }

  changePage(page:number){
    this.currentPageChange.emit(page);
    this.onChange.emit();
  }

  showPages(){
    return this.totalItems > this.itemsPerPage;
  }

}
