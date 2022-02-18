import {Component, Input, OnInit} from '@angular/core';
import {ReportService} from "../../report.service";
import {isArray, isFunction} from "lodash-es";
import {NavigationService} from "../../../navigation/navigation.service";

@Component({
  selector: 'gla-change-report-table-row',
  templateUrl: './change-report-table-row.component.html',
  styleUrls: ['./change-report-table-row.component.scss']
})
export class ChangeReportTableRowComponent implements OnInit {
  @Input() heading: string
  @Input() row: any
  @Input() fields: any
  @Input() changes: any
  is2ColumnHeading = false

  constructor(private reportService: ReportService,
              private navigationService: NavigationService) { }

  ngOnInit(): void {
    this.is2ColumnHeading = this.heading && isArray(this.heading) && this.heading.length === 2;
  }

  getValue(data, field) {
    return this.reportService.getDisplayValue(field.field, field.format, data, field.defaultValue);
  }

  fileURL(data, field) {
    if(field.format && field.format === 'file'){
      let row = data || {};
      let fileId = row.fileId;
      if (fileId){
        let params = this.navigationService.getCurrentStateParams()
        return `/api/v1/project/${params.projectId}/file/${fileId}`;
      }
    }
    return null;
  }

  hasFieldChanges(field){
    let params = [field.changeAttribute || field.field, this.row.right];
    if(field.changeAttribute && isFunction(field.changeAttribute)){
      params = [field.changeAttribute(this.row.right, this.row)];
    }
    return this.changes && this.changes.hasFieldChanged(...params);
  }

  fieldVisible(field){
    if(field.hide && isFunction(field.hide)){
      return !field.hide(this.row);
    }
    return true
  }
}
