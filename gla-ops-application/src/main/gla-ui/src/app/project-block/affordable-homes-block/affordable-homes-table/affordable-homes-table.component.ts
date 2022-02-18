import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {DateUtilsService} from "../../../utils/date-utils.service";

@Component({
  selector: 'gla-affordable-homes-table',
  templateUrl: './affordable-homes-table.component.html',
  styleUrls: ['./affordable-homes-table.component.scss']
})
export class AffordableHomesTableComponent implements OnInit, OnChanges {

  @Input() tableSelector
  @Input() tenures: any[]
  @Input() ofWhichCategories
  @Input() yearsEnabled: boolean
  @Input() entries
  @Input() totals
  @Input() readOnly: boolean
  @Output() onChange: EventEmitter<any> = new EventEmitter()
  entriesByYearAndTenure = {}
  entriesByOfWhichCategory = {}
  collapsed: boolean = false;

  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.entries) {
      this.entries.forEach(entry => {
        if (entry.year != null) {
          if (this.entriesByYearAndTenure[entry.year] == null) {
            this.entriesByYearAndTenure[entry.year] = {};
          }
          this.entriesByYearAndTenure[entry.year][entry.tenureTypeId] = entry;
        }
        else if (entry.ofWhichCategory) {
          if (this.entriesByOfWhichCategory[entry.ofWhichCategory] == null) {
            this.entriesByOfWhichCategory[entry.ofWhichCategory] = {};
          }
          this.entriesByOfWhichCategory[entry.ofWhichCategory][entry.tenureTypeId] = entry;
        }
      });
    }
  }

  toFinancialYearString(year: string) {
    return DateUtilsService.toFinancialYearString(Number(year));
  }

  getOfWhichCategoryDisplayName(category: string) {
    if (category == 'SpecialisedAndSupportedHousing') {
      return 'Specialised & Supported Housing';
    } else {
      return category;
    }
  }

}
