import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';

@Component({
  selector: 'gla-indicative-grant-requested-table',
  templateUrl: './indicative-grant-requested-table.component.html',
  styleUrls: ['./indicative-grant-requested-table.component.scss']
})
export class IndicativeGrantRequestedTableComponent implements OnInit, OnChanges {

  @Input() tenures
  @Input() grantTypes
  @Input() entries
  @Input() totals
  @Input() zeroGrantRequested: boolean
  @Input() completionOnly: boolean
  @Input() readOnly: boolean
  @Output() onChange: EventEmitter<any> = new EventEmitter()
  entriesByTenureAndType = {}

  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.entries) {
      this.entries.forEach(entry => {
        if (this.entriesByTenureAndType[entry.tenureTypeId] == null) {
          this.entriesByTenureAndType[entry.tenureTypeId] = {};
        }
        this.entriesByTenureAndType[entry.tenureTypeId][entry.type] = entry;
      });
    }
  }

}
