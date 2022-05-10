import NumberUtil from '../../util/NumberUtil';
import DateUtil from '../../util/DateUtil'

/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

OutputsService.$inject = ['$http', 'config'];

function OutputsService($http, config) {
  let apiConfig = config;
  return {
    getOutputConfigGroup: (id) => {
      return $http({
        url: `${config.basePath}/outputGroup/${id}`,
        method: 'GET'
      });
    },

    /**
     * Retrieve the project outputs for the financial year
     * @returns {Object} promise
     */
    getProjectOutputs: (projectId, blockId, financialYear) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/block/${blockId}/outputs/${financialYear}`,
        method: 'GET'
      });
    },

    /**
     * Retrieve the project outputs for the financial year
     * @returns {Object} promise
     */
    getProjectBaselineOutputs(projectId, blockId) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/block/${blockId}/baselines`,
        method: 'GET'
      });
    },

    /**
     * Delete the output
     * @param projectId Project id
     * @param id Output id
     * @returns {*}
     */
    delete: (projectId, id) => {
      return $http.delete(`${config.basePath}/projects/${projectId}/outputs/${id}`);
    },

    postProjectOutputs: ({
                           projectId,
                           baseline,
                           actual,
                           forecast,
                           config,
                           month,
                           outputType,
                           year
                         }) => {
      return $http({
        url: `${apiConfig.basePath}/projects/${projectId}/outputs`,
        method: 'POST',
        data: {
          // actual/forecast is determined by dropdown and the input value is placed in the correct attribute
          baseline,
          actual,
          forecast,
          config,
          month,
          outputType,
          year,
        }
      });
    },

    updateProjectOutputs: (projectId, data) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/outputs`,
        method: 'PUT',
        data: data
      });
    },


    outputSummaries(outputsBlock, collapsedRows) {
      collapsedRows = collapsedRows || {};
      let summaries = (outputsBlock || {}).outputSummaries || [];
      summaries.forEach(o => {
        o.collapsed = collapsedRows[o.comparisonId] == null ? true : !!collapsedRows[o.comparisonId];
      });
      return summaries;
    },


    getAllFinancialYears(outputsBlock) {
      let summaries = (outputsBlock || {}).outputSummaries || [];
      return summaries.reduce((res, o) => res.concat(o.subcategories), []).map(item => item.financialYear);
    },


    getOutputBlockSummariesTitle(outputsBlock) {
      let allYears = this.getAllFinancialYears(outputsBlock);
      return this.getOutputsSummariesTitle(_.min(allYears), _.max(allYears))
    },


    getOutputsSummariesTitle(startYear, endYear) {
      let title = 'Total project outputs';

      if (startYear) {
        title += ' ' + DateUtil.toFinancialYearString(startYear);
      }

      if (endYear && startYear != endYear) {
        title += ' to ' + DateUtil.toFinancialYearString(endYear);
      }
      return title;
    },


    getUnitConfig() {
      return {
        UNITS: {
          id: 0,
          precision: 0,
          label: 'Number of units',
          placeholder: 'Enter units',
          default: undefined
        },
        HECTARES: {
          id: 1,
          precision: 2,
          label: 'Hectares',
          placeholder: 'Enter Hectares (Ha)',
          default: undefined
        },
        POSITIONS: {
          id: 2,
          precision: 0,
          label: 'Positions',
          placeholder: 'Enter number',
          default: undefined
        },
        SQUARE_METRES: {
          id: 3,
          precision: 0,
          label: 'Square meters',
          placeholder: 'Enter sqm',
          default: undefined
        },
        SQUARE_METRES_NET: {
          id: 4,
          precision: 0,
          label: 'Net Area (sqm)',
          placeholder: 'Enter internal area',
          default: undefined
        },
        SQUARE_METRES_GROSS: {
          id: 5,
          precision: 0,
          label: 'Gross Area (sqm)',
          placeholder: 'Enter gross area',
          default: undefined
        },
        BEDROOMS: {
          id: 6,
          precision: 0,
          label: 'Bedrooms',
          placeholder: 'Enter number of bedrooms',
          default: undefined

        },
        MONETARY_VALUE: {
          id: 7,
          precision: 0,
          label: 'Monetary value',
          placeholder: 'Enter value £',
          default: undefined
        },
        NUMBER_OF: {
          id: 8,
          precision: 0,
          label: 'Number of',
          placeholder: 'Enter Number',
          default: undefined
        },
        NUMBER_OF_DECIMAL: {
          id: 9,
          precision: 2,
          label: 'Number of',
          placeholder: 'Enter Number',
          default: undefined
        },
        ENTER_VALUE: {
          id: 10,
          precision: 0,
          label: 'Enter Value',
          placeholder: 'Enter Value',
          default: undefined
        },
        ENTER_VALUE_DECIMALS: {
          id: 11,
          precision: 2,
          label: 'Enter Value',
          placeholder: 'Enter Value',
          default: undefined
        },
        NET_AREA: {
          id: 12,
          precision: 0,
          label: 'Net Area (sqm)',
          placeholder: 'Enter Net Area',
          default: undefined
        },
        DISTANCE: {
          id: 13,
          precision: 0,
          label: 'Distance (m)',
          placeholder: 'Enter Distance',
          default: undefined
        },
        LENGTH: {
          id: 14,
          precision: 0,
          label: 'Length (m)',
          placeholder: 'Enter Length',
          default: undefined
        },
        OUTPUTS: {
          id: 15,
          precision: 0,
          label: 'Number of outputs',
          placeholder: 'Enter outputs',
          default: undefined
        },
        RESULTS: {
          id: 16,
          precision: 0,
          label: 'Number of results',
          placeholder: 'Enter results',
          default: undefined
        },
      };
    },

    getOutputTypes() {
      return {
        'DIRECT': 'Direct Output',
        'IND_COUNTED_IN_ANOTHER': 'Counted in Another Housing Programme',
        'IND_MINORITY_STAKE': 'Minority Stake in Joint Venture',
        'IND_UNBLOCKS': 'Unlocks Other Parts of a Site',
        'IND_UNLOCKING': 'Indirect: Unlocking Without Land Interest',
        'IND_OTHER': 'Indirect: Other'
      }
    },

    getAssumptions(projectId, blockId, year) {
      return $http.get(`${config.basePath}/projects/${projectId}/block/${blockId}/assumptions/year/${year}`);
    },

    addAssumption(projectId, blockId, data) {
      return $http.post(`${config.basePath}/projects/${projectId}/block/${blockId}/assumptions`, data);
    },

    updateAssumption(projectId, blockId, data) {
      return $http.put(`${config.basePath}/projects/${projectId}/block/${blockId}/assumptions/${data.id}`, data);
    },

    deleteAssumption(projectId, blockId, assumptionId) {
      return $http.delete(`${config.basePath}/projects/${projectId}/block/${blockId}/assumptions/${assumptionId}`);
    },

    /**
     * Returns the input decimal precision by Id
     */
    getUnitPrecision(id) {
      if (!id || !this.getUnitConfig()[id] || !this.getUnitConfig()[id].precision) {
        return 0;
      }
      return this.getUnitConfig()[id].precision;
    },

    /**
     * Evaluates if the month and year are after the today's date
     * @param {String} date 'MM' month
     * @param {String} date 'YYYY' year
     * @returns {Boolean}
     */
    isFutureDate(month, year) {
      const date = moment(`${month}/${year}`, 'MM/YYYY');
      const now = moment(); // TODO: this should be coming from the backend
      return date.isAfter(now);
    },

    /**
     * Format number to string with comma's and append CR
     * @see `NumberUtil.formatWithCommasAndCR()`
     */
    formatNumber(value, valueType) {
      const precision = this.getUnitPrecision(valueType);
      return value ? NumberUtil.formatWithCommas(value, precision) : '';
    },

    formatDifference(value, valueType) {
      if (!value) {
        return null;
      }
      const precision = this.getUnitPrecision(valueType);
      return `${value > 0 ? '+' : ''}${NumberUtil.formatWithCommas(value, precision)}`;
    },


    transformToQuarterlyData(year, quarters, claims) {
      let claimsMap = this.getClaims(claims);
      let claimedQuarters = (Object.keys(claimsMap) || []).map(q => +q).sort();

      quarters.forEach((q, index) => {
        let quarter = index + 1;
        q.name = `Q${quarter}`;
        q.quarter = quarter;
        this.transformQuarterMonths(q.outputsMonths);
      });

      return quarters;
    },

    transformQuarterMonths(outputsMonths) {
      (outputsMonths || []).forEach(m => {
        m.outputs.forEach(o => {
          o.config.sortingText = o.config.subcategory.toLowerCase() === 'n/a' ? o.config.category : o.config.subcategory;
        });

        m.outputs = _.sortBy(m.outputs, [
          'config.sortingText',
          'outputType'
        ]);
      });
    },

    getQuarter(month) {
      return month < 4 ? 4 : Math.floor((month - 1) / 3);
    },

    getTotals(items) {
      let keys = ['actual', 'forecast', 'difference', 'forecastTotal', 'actualTotal', 'remainingAdvancePayment', 'claimableAmount'];
      let totals = {};
      keys.forEach(key => totals[key] = _.sumBy(items, key));
      return totals;
    },

    getClaims(items) {
      return items.reduce(function (map, obj) {
        if (obj.claimType === 'QUARTER') {

          map[obj.claimTypePeriod] = obj;
        }
        return map;
      }, {});
    }

  }
}

angular.module('GLA')
  .service('OutputsService', OutputsService);
