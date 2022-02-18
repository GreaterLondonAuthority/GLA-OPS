import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'gla-indicative-costs-and-contributions-summary',
  templateUrl: './indicative-costs-and-contributions-summary.component.html',
  styleUrls: ['./indicative-costs-and-contributions-summary.component.scss']
})
export class IndicativeCostsAndContributionsSummaryComponent implements OnInit {

  @Input() summaryTotals: any;
  @Input() grantRequestedTotals: any;
  @Input() grantTypes: any;
  @Input() totalCostsPercentage: any;
  @Input() validationFailures: any

  constructor() { }

  ngOnInit(): void {
  }

}
