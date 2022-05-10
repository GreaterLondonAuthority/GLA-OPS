import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'gla-action-dropdown',
  templateUrl: './action-dropdown.component.html',
  styleUrls: ['./action-dropdown.component.scss']
})
export class ActionDropdownComponent implements OnInit {
  @Input() actionsList: any[]
  @Input() toggleButtonText: string
  @Output() actionClick = new EventEmitter<any>();


  constructor() { }

  ngOnInit(): void {
    this.toggleButtonText = this.toggleButtonText.toUpperCase()
  }

  handleActionClick(event: any, action: any) {
    this.actionClick.emit(action)
  }

}
