import {Component, Input, OnInit} from '@angular/core';
import {ReportService} from "../report.service";

@Component({
  selector: 'gla-change-report-static-text',
  templateUrl: './change-report-static-text.component.html',
  styleUrls: ['./change-report-static-text.component.scss']
})
export class ChangeReportStaticTextComponent implements OnInit {

  @Input() data: any
  @Input() text: string
  @Input() subtext: string
  displayMode: any

  constructor(private reportService: ReportService) { }

  ngOnInit(): void {
    this.displayMode = this.reportService.getReportDisplayMode();
  }
}
