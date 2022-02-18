import {Component, Input, OnInit} from '@angular/core';
import {cloneDeep, find} from "lodash-es";
import {ReportService} from "../report.service";

@Component({
  selector: 'gla-indicative-starts-and-completions-change-report',
  templateUrl: './affordable-homes-change-report.component.html',
  styleUrls: ['./affordable-homes-change-report.component.scss']
})
export class AffordableHomesChangeReportComponent implements OnInit {
  @Input() data: any
  templateBlock: any;
  tenuresFields: any;
  reportData: any;
  comparableTenureTypeRows: any[];
  summaryFields: any;
  comparableSummaryRows: any[];

  constructor(private reportService: ReportService) { }

  ngOnInit(): void {
    this.templateBlock = find(this.data.context.template.blocksEnabled, {block: 'AffordableHomes'});

    this.comparableTenureTypeRows = this.getTenureTypesComparableRows(this.data.left, this.data.right);
    this.comparableSummaryRows = this.getSummaryComparableRows(this.data.left, this.data.right);

    this.tenuresFields = [
      {
        field: 'name',
        label: 'TENURE TYPE'
      }
    ];

    this.templateBlock.grantTypes.forEach(grantType => {
      this.tenuresFields.push({
        field: `grantTypes.${grantType}.value`,
        format: 'number',
        label: `£ ${grantType.toUpperCase()} REQUESTED`,
        changeAttribute(row){
          return row? `${row.externalId}:${grantType}:value`: null;
        }
      });
    });

    this.tenuresFields = this.tenuresFields.concat([
      {
        field: 'totals.totalUnits',
        format: 'number',
        label: 'TOTAL UNITS (SOS)',
        changeAttribute(row){
          return row? `${row.externalId}:totals:totalUnits`: null;
        }
      },
      {
        field: 'totals.grantPerUnit',
        format: 'number',
        label: '£ GRANT PER UNIT',
        changeAttribute(row){
          return row? `${row.externalId}:totals:grantPerUnit`: null;
        }
      },
      {
        field: 'grantTypes.TOTAL_SCHEME_COST.value',
        format: 'number',
        label: '£ TOTAL SCHEME COSTS (TSC)',
        changeAttribute(row){
          return row? `${row.externalId}:TOTAL_SCHEME_COST:value`: null;
        }
      },
      {
        field: 'totals.tscPerUnit',
        format: 'number',
        label: '£ TSC PER UNIT',
        changeAttribute(row){
          return row? `${row.externalId}:totals:tscPerUnit`: null;
        }
      },
      {
        field: 'totals.grantAsPercentageOfTsc',
        format: 'number',
        label: 'GRANT AS % OF TSC',
        changeAttribute(row){
          return row? `${row.externalId}:totals:grantAsPercentageOfTsc`: null;
        }
      }
    ]);

    this.summaryFields = [
      {
        field: 'totalCosts',
        format: 'number',
        label: 'TOTAL SCHEME COSTS'
      }, {
        field: 'totalContributions',
        format: 'number',
        label: 'TOTAL SCHEME CONTRIBUTIONS'
      }, {
        field: 'totalEligibleGrant',
        format: 'number',
        label: 'TOTAL ELIGIBLE GRANT'
      }
    ];

    this.templateBlock.grantTypes.forEach(grantType => {
      this.summaryFields.push({
        field: `totals.totalsByType.${grantType}`,
        format: 'number',
        label: `${grantType.toUpperCase()} REQUESTED`,
        changeAttribute(row){
          return row? `totalsByType:${grantType}`: null;
        }
      });
    });

    this.summaryFields.push({
      field: `totalCostsPercentage`,
      format: 'number',
      label: `GRANT % OF TOTAL COSTS`,
      defaultValue: 'N/A'
    });
  }

  getTenureTypesComparableRows(leftBlock, rightBlock) {
    let leftAndRightBlock = [leftBlock, rightBlock].map(block => {
      if(!block){
        return [];
      }
      let tenureTypes = cloneDeep(this.data.context.template.tenureTypes || []);
      tenureTypes.forEach(tt => {
        let totals = block.grantRequestedTotals;
        tt.totals = totals.totalsByTenure[tt.externalId];
        let grantRequestedEntries = (block.grantRequestedEntries  || []).filter(entry => tt.externalId === entry.tenureTypeId);
        grantRequestedEntries.forEach(entry => {
          tt.grantTypes = tt.grantTypes || {};
          if (tt.grantTypes[entry.type] == null) {
            tt.grantTypes[entry.type] = entry;
          }
        });
      });
      return tenureTypes;
    });

    //Filter to identify left side tenure type row when you have the right side row
    let leftSideFilter = function (rightRow) {
      return {id: rightRow.id}
    };
    return this.reportService.rowsToCompare(leftAndRightBlock[0], leftAndRightBlock[1], leftSideFilter);
  }

  getSummaryComparableRows(leftBlock, rightBlock) {
    let leftAndRightBlock = [leftBlock, rightBlock].map(block => {
      if(!block){
        return [];
      }
      let row = block.summaryTotals;
      row.id = 'fakeIdToAlwaysMatch';
      row.totals = block.grantRequestedTotals;
      row.totalCostsPercentage = block.totalCostsPercentage;
      return [row];
    });

    let leftSideFilter = function (rightRow) {
      return {id: rightRow.id}
    };
    return this.reportService.rowsToCompare(leftAndRightBlock[0], leftAndRightBlock[1], leftSideFilter);
  }
}


