/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ReportService.$inject = ['$http', 'config', 'numberFilter', 'currencyFilter', 'dateFilter', '$q', '$timeout', 'GlaReportService'];

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

function ReportService($http, config, numberFilter, currencyFilter, dateFilter, $q, $timeout, GlaReportService) {
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
      return GlaReportService.mapFields(fields, formats);
    },

    extractValue(field, data) {
      return GlaReportService.extractValue(field, data);
    },


    formatFieldValue(value, format, data, defaultValue) {
      return GlaReportService.formatFieldValue(value, format, data, defaultValue);
    },

    /**
     * Extracts data from the object and formats it based on format property
     * @param fieldName <string> field expression
     * @param format <Function|string> function or supported formatting
     * @param data <Object> Data against which field expression is evaluated
     */
    getDisplayValue(fieldName, format, data, defaultValue) {
      return GlaReportService.getDisplayValue(fieldName, format, data, defaultValue);
    },

    findSelectedBorough(boroughs, boroughName) {
      return GlaReportService.findSelectedBorough(boroughs, boroughName);
    },
    findSelectedWard(wards, wardId) {
      return GlaReportService.findSelectedWard(wards, wardId);
    },

    /**
     * Returns string representation of the block version
     * @param block
     */
    version(block, autoApproval) {
      return GlaReportService.version(block, autoApproval);
    },

    displayModes: displayModes,

    displayMode: displayModes.HALF_SCREEN,

    getReportDisplayMode() {
      return GlaReportService.getReportDisplayMode();
    },
    setFullWidth() {
      GlaReportService.setFullWidth();
    },
    setHalfWidth() {
      GlaReportService.setHalfWidth();
    },

    /**
     * Returns an array of objects {left:row, right: row} to compare in reports
     * @param leftArray
     * @param rightArray
     * @param leftSideRowFilter Filter to identify left side row when you have a right side row
     */
    rowsToCompare(leftArray, rightArray, leftSideRowFilter) {
      return GlaReportService.rowsToCompare(leftArray, rightArray, leftSideRowFilter) ;
    },


    /**
     * Sort comparable rows by property
     * @param comparableRows
     * @param sortProperty
     * @returns {Array}
     */

    sortComparableRows(comparableRows, sortProperty) {
      return GlaReportService.sortComparableRows(comparableRows, sortProperty);
    },

    /**
     * Group comparable rows by property
     * @param comparableRows
     * @param sortProperty
     * @returns {Array}
     */

    groupComparableRows(comparableRows, groupProperty, sort) {
      return GlaReportService.groupComparableRows(comparableRows, groupProperty, sort);
    },


    /**
     * Adds an object to the report block {left, right} to track changes
     * @param reportBlock {left, right}
     * @returns {hasFieldChanged(), count()}
     */
    changeTracker(reportBlock) {
      return GlaReportService.changeTracker(reportBlock);
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

    generateAdHocReport(sql, fileName) {
      return $http.post(`${config.basePath}/report/adhoc?fileName=${fileName ? fileName : ''}`, sql)
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
      return GlaReportService.getBlocksToCompare(latestProject, lastApprovedProject, template, currentFinancialYear);
    },

    /**
     * Gets same data structure as in comparison report and then removes the right side blocks;
     * @param project
     * @param template
     * @param currentFinancialYear
     */
    getBlocksToCompareForSummaryReport(project, template, currentFinancialYear) {
      return GlaReportService.getBlocksToCompareForSummaryReport(project, template, currentFinancialYear);
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
      };

      let tick = () => {
        this.getGeneratedReports().then(rsp => {
            callback(rsp);
          if (!poll.isStopped) {
            $timeout(tick, 500);
          }
        });
      };
      tick();
      return poll;
    },

    /**
     * Delete specified user report
     * @param {Number} reportId - report id
     * @return {Object} promise
     */
    deleteUserReport: (reportId) => {
      return $http({
        url: `${config.basePath}/userReport/${reportId}`,
        method: 'DELETE'
      });
    }
  }
}

angular.module('GLA')
  .service('ReportService', ReportService);
