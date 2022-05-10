import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AffordableHomesSummaryReportComponent } from './affordable-homes-summary-report/affordable-homes-summary-report.component';
import {SharedModule} from "../shared/shared.module";
import {ProjectBlockModule} from "../project-block/project-block.module";


@NgModule({
  declarations: [
    AffordableHomesSummaryReportComponent],
  imports: [
    CommonModule,
    SharedModule,
    ProjectBlockModule
  ]
})
export class SummaryReportModule { }
