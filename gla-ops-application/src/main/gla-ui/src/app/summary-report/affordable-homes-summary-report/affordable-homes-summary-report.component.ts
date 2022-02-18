import {Component, Input, OnInit} from '@angular/core';
import {find} from "lodash-es";

@Component({
  selector: 'gla-affordable-homes-summary-report',
  templateUrl: './affordable-homes-summary-report.component.html',
  styleUrls: ['./affordable-homes-summary-report.component.scss']
})
export class AffordableHomesSummaryReportComponent implements OnInit {

  @Input() block: any
  @Input() project: any
  @Input() template: any;
  templateBlock: any;

  constructor() { }

  ngOnInit(): void {
    this.templateBlock = find(this.template.blocksEnabled, {block: 'AffordableHomes'});
  }

}
