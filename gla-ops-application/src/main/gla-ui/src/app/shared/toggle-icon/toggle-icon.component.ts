import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';

@Component({
  selector: 'toggle-icon',
  templateUrl: './toggle-icon.component.html',
  styleUrls: ['./toggle-icon.component.scss']
})
export class ToggleIconComponent implements OnInit, OnChanges {

  @Input() collapsed: boolean
  @Input() sessionStorage: any
  @Input() sessionId: string
  @Output() collapsedChange = new EventEmitter<boolean>();

  collapsedIcon: string;
  expandedIcon: string;

  constructor() {
  }

  ngOnInit(): void {
    this.collapsedIcon = 'glyphicon-triangle-right';
    this.expandedIcon = 'glyphicon-triangle-bottom';
    // console.log('ngOnInit: this.sessionId', this.sessionId)
  }

  ngOnChanges(changes: SimpleChanges): void {
    if(this.sessionId != null){
      let sessionCollapsed = (this.sessionStorage || {})[this.sessionId];
      if(sessionCollapsed != null && this.collapsed !== sessionCollapsed){
        this.collapsed = sessionCollapsed;
        this.collapsedChange.emit(this.collapsed)
      } else {
        (this.sessionStorage || {})[this.sessionId] = !!this.collapsed;
      }
    }
  }

  onToggle() {
    this.collapsed = !this.collapsed;
    if(this.sessionId != null){
      (this.sessionStorage || {})[this.sessionId] = this.collapsed;
    }
    this.collapsedChange.emit(this.collapsed)
  }
}
