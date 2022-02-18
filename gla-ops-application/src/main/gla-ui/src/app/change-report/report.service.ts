import { Injectable } from '@angular/core';
import {find, isBoolean, isFunction, isString, map, remove, sortBy} from "lodash-es";
import {CurrencyPipe, DatePipe, DecimalPipe} from "@angular/common";

const  displayModes = {
  HALF_SCREEN: {
    id: 'half',
    colspan: 6,
    maxNumberOfCol: 2
  },
  FULL_SCREEN: {
    id: 'full',
    colspan: 12,
    maxNumberOfCol: 1
  }
};

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  private displayMode: any = displayModes.HALF_SCREEN;

  constructor(private decimalPiPe: DecimalPipe,
              private currencyPipe: CurrencyPipe,
              private datePipe: DatePipe) { }

  index(obj, i) {
    if (!obj) {
      return;
    }
    if (obj.hasOwnProperty(i)) {
      return obj[i];
    } else {
      //  throw obj + 'doesn't have proprty ' + i;
    }
  }

  mapFields(fields, formats) {
    return map(fields, (field, index) => {
      let mappedField = {} as any
      if (isString(field)) {
        mappedField.field = field;
      } else {
        mappedField.field = field.field;
        mappedField.format = field.format;
      }

      if (formats) {
        if (formats.length === 1) {
          mappedField.format = mappedField.format || formats[0];
        } else if (formats.length === fields.length) {
          mappedField.format = mappedField.format || formats[index];
        }
      }
      return mappedField;
    });
  }

  extractValue(field, data) {
    return field.split('.').reduce(this.index, data);
  }

  formatFieldValue(value, format, data, defaultValue) {
    if (!isFunction(format) && value == null && defaultValue) {
      if (isFunction(defaultValue)) {
        return defaultValue(data);
      }
      return defaultValue;
    }

    if (!format) {
      return value;
    }

    if (isFunction(format)) {
      return format(data);
    }

    let formatSplit = format.split('|');
    if (formatSplit[0] === 'number') {
      let decimals = formatSplit.length > 1 ? formatSplit[1] : 0;
      let digitInfo = formatSplit.length > 1? `1.${decimals}-${decimals}` : null;
      return this.decimalPiPe.transform(value, digitInfo)
    } else if (formatSplit[0] === 'currency') {
      let decimals = formatSplit.length > 1 ? formatSplit[1] : 0;
      return this.currencyPipe.transform(value, '£', 'code'  , `1.${decimals}-${decimals}`);
    } else if (formatSplit[0] === 'yesno') {
      if (isBoolean(value)) {
        return value ? 'Yes' : 'No';
      }
      return value;
    } else if (formatSplit[0] === 'date') {
      return this.datePipe.transform(value, formatSplit.length > 1 ? formatSplit[1] : 'dd/MM/yyyy');
    } else if (formatSplit[0] === 'time') {
      return this.datePipe.transform(value, 'HH:mm');
    } else if (formatSplit[0] === 'datetime') {
      return this.datePipe.transform(value, 'dd/MM/yyyy HH:mm');
    }
    return value;
  }

  /**
   * Extracts data from the object and formats it based on format property
   * @param fieldName <string> field expression
   * @param format <Function|string> function or supported formatting
   * @param data <Object> Data against which field expression is evaluated
   */
  getDisplayValue(fieldName, format, data, defaultValue) {
    let value = this.extractValue(fieldName, data);
    return this.formatFieldValue(value, format, data, defaultValue);
  }

  findSelectedBorough(boroughs, boroughName) {
    return find(boroughs, {boroughName: boroughName}) || {};
  }
  findSelectedWard(wards, wardId) {
    return find(wards, {id: wardId}) || {};
  }

  /**
   * Returns string representation of the block version
   * @param block
   */
  version(block, autoApproval) {
    if (autoApproval && block.lastModified) {
      return `Version ${block.versionNumber} saved on ${this.datePipe.transform(block.lastModified, 'dd/MM/yyyy')}`;
    } else if (block.versionNumber && block.approvalTime) {
      return `Version ${block.versionNumber} Approved on ${this.datePipe.transform(block.approvalTime, 'dd/MM/yyyy')}`;
    } else if (block.lastModified) {
      return `Unapproved changes on ${this.datePipe.transform(block.lastModified, 'dd/MM/yyyy')} by ${block.modifiedByName}`;
    }
    return 'New unedited block';
  }



  getReportDisplayMode() {
    return this.displayMode;
  }
  setFullWidth() {
    this.displayMode = displayModes.FULL_SCREEN;
  }
  setHalfWidth() {
    this.displayMode = displayModes.HALF_SCREEN;
  }

  /**
   * Returns an array of objects {left:row, right: row} to compare in reports
   * @param leftArray
   * @param rightArray
   * @param leftSideRowFilter Filter to identify left side row when you have a right side row
   */
  rowsToCompare(leftArray, rightArray, leftSideRowFilter) {
    leftArray = leftArray.slice(0);
    rightArray = rightArray.slice(0);
    let rowsToCompare = [];
    for (let i = 0; i < rightArray.length; i++) {
      let leftRow = find(leftArray, leftSideRowFilter(rightArray[i])) || null;
      if (leftRow) {
        remove(leftArray, leftRow);
      }

      rowsToCompare.push({
        left: leftRow,
        right: rightArray[i]
      });
    }

    leftArray.forEach(leftRow => {
      rowsToCompare.push({
        left: leftRow,
        right: null
      })
    });

    return rowsToCompare;
  }


  /**
   * Sort comparable rows by property
   * @param comparableRows
   * @param sortProperty
   * @returns {Array}
   */

  sortComparableRows(comparableRows, sortProperty) {
    return sortBy(comparableRows || [], row => (row.right || row.left)[sortProperty]);
  }

  /**
   * Group comparable rows by property
   * @param comparableRows
   * @param sortProperty
   * @returns {Array}
   */

  groupComparableRows(comparableRows, groupProperty, sort) {
    if (sort && comparableRows) {
      comparableRows = this.sortComparableRows(comparableRows, groupProperty);
    }
    let groups = [];
    let existingGroups = {};
    for (let i = 0; i < comparableRows.length; i++) {
      let row = comparableRows[i];
      let groupName = (row.right || row.left)[groupProperty];
      let group = existingGroups[groupName];
      if (!existingGroups[groupName]) {
        existingGroups[groupName] = [];
        groups.push({
          groupName: groupName,
          group: existingGroups[groupName]
        });
      }
      existingGroups[groupName].push(row);
    }
    return groups;
  }


  /**
   * Adds an object to the report block {left, right} to track changes
   * @param reportBlock {left, right}
   * @returns {hasFieldChanged(), count()}
   */
  changeTracker(reportBlock) {
    const changeTracker = {
      allChanges: {},
      visibleChanges: {},
      get count() {
        return Object.keys(this.visibleChanges).length;
      },

      /**
       * Checks if field or field in a row has changed
       * @param fieldName
       * @param rightTableRow optional right item of changeReportTableRow
       * @returns {boolean}
       */
      hasFieldChanged(fieldName, rightTableRow) {
        let hasFieldChanged = false;
        if (this.allChanges) {
          let changeId = this.changeId(fieldName, (rightTableRow || {}).comparisonId);
          hasFieldChanged = !!this.allChanges[changeId] || this.isRowAdded(rightTableRow);
          if (hasFieldChanged) {
            this.visibleChanges[changeId] = true;
          }
        }
        return hasFieldChanged;
      },

      isRowAdded(tableRow) {
        let rowChangeId = this.changeId(null, (tableRow || {}).comparisonId);
        if (rowChangeId && this.allChanges) {
          let changeItem = this.allChanges[rowChangeId] || {};
          return changeItem.differenceType === 'Addition';
        }
        return false;
      },

      addDeletions(rowsToCompare, idField) {
        rowsToCompare.forEach(leftAndRightItem => {
          let isItemDeleted = leftAndRightItem.left && !leftAndRightItem.right;
          if (isItemDeleted) {
            let deletedItem = leftAndRightItem.left;
            let idValue = deletedItem.comparisonId || deletedItem.id;
            if (idField) {
              idValue = deletedItem[idField];
            }
            let changeId = `${idValue}:deletion`;
            this.visibleChanges[changeId] = true;
          }
        })
      },

      changeId(field, comparisonId) {
        if (comparisonId) {
          let id = `${comparisonId}`;
          if (field) {
            id += `:${field}`;
          }
          return id;
        }
        return field;
      }
    };

    if (reportBlock.right && reportBlock.right.differences) {
      changeTracker.allChanges = reportBlock.right.differences.reduce((result, d) => {
        let changeId = changeTracker.changeId(d.field, d.comparisonId);
        result[changeId] = d;
        return result;
      }, {});
    }

    return changeTracker;
  }

  /**
   * TODO rename/review parameters to make sense more for both change & summary report.
   * TODO Maybe name it 'left & right' but it assumes you always have right which we don't eventually in summary report
   * @param latestProject
   * @param lastApprovedProject
   * @param template
   * @param currentFinancialYear
   * @returns {Array}
   */
  getBlocksToCompare(latestProject, lastApprovedProject, template, currentFinancialYear) {
    let latestBlocks = latestProject.projectBlocksSorted || [];
    let lastApprovedBlocks = lastApprovedProject.projectBlocksSorted || [];

    let blocksToCompare = [];
    for (let i = 0; i < latestBlocks.length; i++) {
      let right = latestBlocks[i] || {};

      let hasUnapproved = right.blockStatus !== 'LAST_APPROVED';
      let item = {
        left: lastApprovedBlocks[i],
        right: hasUnapproved ? latestBlocks[i] : undefined,
        type: latestBlocks[i].type,
        blockDisplayName: latestBlocks[i].blockDisplayName,
        blockDisplayCls: latestBlocks[i].blockDisplayName.toLowerCase().split(' ').join('-'),
        id: latestBlocks[i].id,
        expanded: true,
        context: {
          project: {
            left: lastApprovedProject,
            right: latestProject
          },
          template: template,
          currentFinancialYear: currentFinancialYear
        }
      } as any;
      //Add derived properties
      item.versionObj = {
        left: {
          versionString: 'There is no approved version of this block'
        }
      };

      if (item.left) {
        item.versionObj.left.versionString = this.version(item.left, !lastApprovedProject.stateModel.approvalRequired);
      }

      if (item.right) {
        item.versionObj.right = {
          versionString: this.version(item.right, !latestProject.stateModel.approvalRequired)
        };
      }
      item.changes = this.changeTracker(item);


      blocksToCompare.push(item);
    }
    return blocksToCompare;
  }

  /**
   * Gets same data structure as in comparison report and then removes the right side blocks;
   * @param project
   * @param template
   * @param currentFinancialYear
   */
  getBlocksToCompareForSummaryReport(project, template, currentFinancialYear) {
    let blocksToCompare = this.getBlocksToCompare(project, project, template, currentFinancialYear);
    blocksToCompare.forEach(block => delete block.right);
    return blocksToCompare;
  }
}
