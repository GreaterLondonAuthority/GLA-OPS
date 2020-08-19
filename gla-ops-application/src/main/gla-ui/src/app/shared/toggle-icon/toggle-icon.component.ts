import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'toggle-icon',
  templateUrl: './toggle-icon.component.html',
  styleUrls: ['./toggle-icon.component.scss']
})
export class ToggleIconComponent implements OnInit {

  @Input() collapsed: boolean
  @Output() collapsedChange = new EventEmitter<boolean>();

  collapsedIcon: string;
  expandedIcon: string;

  constructor() {
  }

  ngOnInit(): void {
    this.collapsedIcon = 'glyphicon-triangle-right';
    this.expandedIcon = 'glyphicon-triangle-bottom';
  }

  onSectionClick() {
    this.collapsed = !this.collapsed;
    this.collapsedChange.emit(this.collapsed)
  }
}
