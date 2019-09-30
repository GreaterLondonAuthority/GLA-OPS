/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ReportService.$inject = ['$http', 'config', 'numberFilter', 'currencyFilter', 'dateFilter', '$q', '$timeout'];

function index(obj, i) {
  if (!obj) {
    return;
  }
  if (obj.hasOwnProperty(i)) {
    return obj[i];
  } else {
    //  throw obj + 'doesn\'t have proprty ' + i;
  }
}

function ReportService($http, config, numberFilter, currencyFilter, dateFilter, $q, $timeout) {
  let displayModes = {
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

  return {
    numberFilter: numberFilter,
    currencyFilter: currencyFilter,
    dateFilter: dateFilter,

    mapFields(fields, formats) {
      return _.map(fields, (field, index) => {
        let mappedField = {}
        if (_.isString(field)) {
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
    },

    extractValue(field, data) {
      return field.split('.').reduce(index, data);
    },


    formatFieldValue(value, format, data, defaultValue) {
      if (!_.isFunction(format) && value == null && defaultValue) {
        if (_.isFunction(defaultValue)) {
          return defaultValue(data);
        }
        return defaultValue;
      }

      if (!format) {
        return value;
      }

      if (_.isFunction(format)) {
        return format(data);
      }

      let formatSplit = format.split('|');
      if (formatSplit[0] === 'number') {
        return this.numberFilter(value, formatSplit.length > 1 ? formatSplit[1] : 0);
      } else if (formatSplit[0] === 'currency') {
        return this.currencyFilter(value, '£', formatSplit.length > 1 ? formatSplit[1] : 0);
      } else if (formatSplit[0] === 'yesno') {
        if (_.isBoolean(value)) {
          return value ? 'Yes' : 'No';
        }
        return value;
      } else if (formatSplit[0] === 'date') {
        return this.dateFilter(value, formatSplit.length > 1 ? formatSplit[1] : 'dd/MM/yyyy');
      } else if (formatSplit[0] === 'time') {
        return this.dateFilter(value, 'HH:mm');
      } else if (formatSplit[0] === 'datetime') {
        return this.dateFilter(value, 'dd/MM/yyyy HH:mm');
      }
      return value;
    },

    /**
     * Extracts data from the object and formats it based on format property
     * @param fieldName <string> field expression
     * @param format <Function|string> function or supported formatting
     * @param data <Object> Data against which field expression is evaluated
     */
    getDisplayValue(fieldName, format, data, defaultValue) {
      let value = this.extractValue(fieldName, data);
      return this.formatFieldValue(value, format, data, defaultValue);
    },

    findSelectedBorough(boroughs, boroughName) {
      return _.find(boroughs, {boroughName: boroughName}) || {};
    },
    findSelectedWard(wards, wardId) {
      return _.find(wards, {id: wardId}) || {};
    },

    /**
     * Returns string representation of the block version
     * @param block
     */
    version(block, autoApproval) {
      if (autoApproval && block.lastModified) {
        return `Version ${block.versionNumber} saved on ${this.dateFilter(block.lastModified, 'dd/MM/yyyy')}`;
      } else if (block.versionNumber && block.approvalTime) {
        return `Version ${block.versionNumber} Approved on ${this.dateFilter(block.approvalTime, 'dd/MM/yyyy')}`;
      } else if (block.lastModified) {
        return `Unapproved changes on ${this.dateFilter(block.lastModified, 'dd/MM/yyyy')} by ${block.modifiedByName}`;
      }
      return 'New unedited block';
    },

    //  this.showingRight = (rightBlock) => {
    //    return rightBlock.blockStatus == 'UNAPPROVED' || rightBlock.newBlock;
    //  }
    //
    //  /**
    //   * Returns if a block should be showed in the left sid side of change report
    //   * @param {String} block
    //   */
    //  this.showingLeft = (leftBlock) => {
    //    return leftBlock.blockStatus == 'LAST_APPROVED' || (leftBlock.blockStatus != 'LAST_APPROVED' && !leftBlock.newBlock);
    //  }
    //
    //  this.updateDataVisibility = (data) => {
    //    const showingLeft = this.showingLeft(data.left);
    //    const showingRight = this.showingRight(data.right);
    //    if(!showingLeft) {
    //      data.left = null;
    //    }
    //    if(!showingRight) {
    //      data.right = null;
    //    }
    //  }

    displayModes: displayModes,

    displayMode: displayModes.HALF_SCREEN,

    getReportDisplayMode() {
      return this.displayMode;
    },
    setFullWidth() {
      this.displayMode = displayModes.FULL_SCREEN;
    },
    setHalfWidth() {
      this.displayMode = displayModes.HALF_SCREEN;
    },

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
        let leftRow = _.find(leftArray, leftSideRowFilter(rightArray[i])) || null;
        if (leftRow) {
          _.remove(leftArray, leftRow);
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
    },


    /**
     * Sort comparable rows by property
     * @param comparableRows
     * @param sortProperty
     * @returns {Array}
     */

    sortComparableRows(comparableRows, sortProperty) {
      return _.sortBy(comparableRows || [], row => (row.right || row.left)[sortProperty]);
    },

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
    },


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
    },

    getReports() {
      return $http.get(`${config.basePath}/reports`);
    },

    generateReport(url) {
      return $http.get(`${url}`);
    },
    generateReportWithParameters(reportName, filters) {
      return $http.post(`${config.basePath}/generate/csv/${reportName }`, filters).then(rsp => rsp.data)
    },

    getFilterDropDowns(programmeIds, reportName) {
      let cfg = {
        params: {
          programmes: programmeIds
        }
      };

      return $http.get(`${config.basePath}/reports/${reportName}/filters`, cfg).then(rsp => rsp.data);
    },

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
        };
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
    },

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
    },



    getGeneratedReports() {
      return $http.get(`${config.basePath}/userReports/`);
    },


    pollReports(callback) {
      let poll = {
        /* interval: 1000, */
        isStopped: false,
        stop() {
          this.isStopped = true;
        },
/* 
        nextInterval() {
          let interval = this.interval;
          this.interval = this.interval * 2;
          return interval;
        } */
      };

      let tick = () => {
       /*  let interval = poll.nextInterval(); */
        this.getGeneratedReports().then(rsp => {
            callback(rsp);
          if (!poll.isStopped) {
            $timeout(tick, 500);
          }
        });
      };
      tick();
      return poll;
    }
  }
}

angular.module('GLA')
  .service('ReportService', ReportService);
