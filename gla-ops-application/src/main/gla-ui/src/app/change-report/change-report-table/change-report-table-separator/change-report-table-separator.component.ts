import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'gla-change-report-table-separator',
  templateUrl: './change-report-table-separator.component.html',
  styleUrls: ['./change-report-table-separator.component.scss']
})
export class ChangeReportTableSeparatorComponent implements OnInit {

  @Input() hasRightValues: boolean

  constructor() { }

  ngOnInit(): void {
  }

}
