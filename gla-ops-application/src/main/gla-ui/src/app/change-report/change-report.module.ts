import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AffordableHomesChangeReportComponent } from './affordable-homes-change-report/affordable-homes-change-report.component';
import { ChangeReportStaticTextComponent } from './change-report-static-text/change-report-static-text.component';
import { ChangeReportTableComponent } from './change-report-table/change-report-table.component';
import { ChangeReportTableSeparatorComponent } from './change-report-table/change-report-table-separator/change-report-table-separator.component';
import { ChangeReportTableRowComponent } from './change-report-table/change-report-table-row/change-report-table-row.component';



@NgModule({
  declarations: [
    AffordableHomesChangeReportComponent,
    ChangeReportStaticTextComponent,
    ChangeReportTableComponent,
    ChangeReportTableSeparatorComponent,
    ChangeReportTableRowComponent
  ],
  imports: [
    CommonModule
  ]
})
export class ChangeReportModule { }
