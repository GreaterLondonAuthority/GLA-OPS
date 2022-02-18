import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {sortBy} from "lodash-es";

@Component({
  selector: 'gla-indicative-costs-and-contributions-table',
  templateUrl: './indicative-costs-and-contributions-table.component.html',
  styleUrls: ['./indicative-costs-and-contributions-table.component.scss']
})
export class IndicativeCostsAndContributionsTableComponent implements OnChanges {

  @Input() costs: any;
  @Input() contributions: any;
  @Input() total: any;
  @Input() readOnly: boolean
  @Input() validationFailures: any
  @Output() onChange: EventEmitter<any> = new EventEmitter()

  rows: any

  constructor() { }

  ngOnChanges(changes: SimpleChanges): void {
    this.rows =  this.costs ? this.costs : this.contributions
    this.rows = sortBy(this.rows, 'displayOrder');
  }

  trackByKey(index: number, obj: any): string {
    return obj.id;
  };

}
