import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'gla-multi-panel',
  templateUrl: './multi-panel.component.html',
  styleUrls: ['./multi-panel.component.scss']
})
export class MultiPanelComponent implements OnInit {

  @Input() editable: boolean
  @Output() onEdit: EventEmitter<any> = new EventEmitter()

  constructor() { }

  ngOnInit(): void {
  }

}
