import {Component, Input, OnInit, ViewEncapsulation} from '@angular/core';
import {forEach} from "lodash-es";

@Component({
  selector: 'gla-change-report-table',
  templateUrl: './change-report-table.component.html',
  styleUrls: ['./change-report-table.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ChangeReportTableComponent implements OnInit {

  @Input() label: string
  @Input() heading: string
  @Input() rows: any
  @Input() fields: any
  @Input() changes: any
  @Input() showTableSeparators: boolean //Shows gaps in risks block when now risks and issues. Adding this property to workaround it
  @Input() showNoElementMessage: boolean
  hasRightValues = false
  hasLeftValues = false
  sideController: any;

  constructor() { }

  ngOnInit(): void {
    forEach(this.rows, (row)=>{
      if(!!row.right){
        this.hasRightValues = true;
      }
      if(!!row.left){
        this.hasLeftValues = true;
      }
    });

    this.sideController = {
      left: this.hasLeftValues,
      right: this.hasRightValues
    };
  }

  getValue(data, field) {
    if(!data){
      return;
    }

    return field.split('.').reduce((obj, i) => {
      if (!obj) {
        return;
      }
      return obj[i];
    }, data);
  }
}
