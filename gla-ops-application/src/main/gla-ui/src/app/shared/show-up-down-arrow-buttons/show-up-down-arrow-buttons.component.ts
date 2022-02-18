import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'gla-show-up-down-arrow-buttons',
  templateUrl: './show-up-down-arrow-buttons.component.html',
  styleUrls: ['./show-up-down-arrow-buttons.component.scss']
})
export class ShowUpDownArrowButtonsComponent implements OnInit {

  @Input() currentItem
  @Input() sortedCollectionItems
  @Input() titleUp
  @Input() titleDown
  @Output() onDisplayOrderChange = new EventEmitter();

  constructor() { }

  ngOnInit(): void {
  }

  returnModifiedCollection(){
    this.onDisplayOrderChange.emit(this.sortedCollectionItems)
  }

  onMoveRow(moveRowBy){
    let currentIndex = this.sortedCollectionItems.indexOf(this.currentItem);
    let displayOrder = this.currentItem.displayOrder;
    this.sortedCollectionItems[currentIndex].displayOrder = this.sortedCollectionItems[currentIndex+moveRowBy].displayOrder;
    this.sortedCollectionItems[currentIndex+moveRowBy].displayOrder = displayOrder;
    this.sortedCollectionItems.sort((item1, item2) =>  item1.displayOrder - item2.displayOrder);
    this.returnModifiedCollection();
  }

  isUpArrowAllowed(){
    let currentIndex = this.sortedCollectionItems.indexOf(this.currentItem);
    return currentIndex >0
  }
  isDownArrowAllowed(){
    let currentIndex = this.sortedCollectionItems.indexOf(this.currentItem);
    return this.sortedCollectionItems.length > 1 && currentIndex < this.sortedCollectionItems.length -1
  }

}

