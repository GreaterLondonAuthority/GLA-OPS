/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ProjectFundingService.$inject = ['$http', 'config'];

import DateUtil from '../../util/DateUtil';

function ProjectFundingService($http, config) {

  return {

    getProjectFunding: (projectId, blockId, year) => {
      let self = this;
      return $http({
        url: `${config.basePath}/projects/${projectId}/funding/${blockId}`,
        method: 'GET',
        params: {
          year: year
        }
      });
    },

    processBudgetSummaries(budgetSummaries) {
      let yearlyData = [], groupedEntries  = _.groupBy(budgetSummaries, 'year');
      _.forEach(groupedEntries, (val, year)=>{
        yearlyData.push(this.processGroupedEntries(year, val));
      });
      return yearlyData;
    },

    processGroupedEntries(year, entries) {
      let res = {
        year: _.toNumber(year),
        yearLabel: DateUtil.toFinancialYearString(year),
      };
      _.forEach(entries, entry => {
        if(entry.spendType === 'CAPITAL' && entry.category !== 'MatchFund'){
          res.capitalValue = entry;
        } else if(entry.spendType === 'CAPITAL' && entry.category === 'MatchFund'){
          res.capitalMatchFund = entry;
        } else if(entry.spendType === 'REVENUE' && entry.category !== 'MatchFund'){
          res.revenueValue = entry;
        } else if(entry.spendType === 'REVENUE' && entry.category === 'MatchFund') {
          res.revenueMatchFund = entry;
        }
      });
      return res;
    },

    addQuarterlyEntry(projectId, blockId, data) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/funding/${blockId}/activities`,
        method: 'POST',
        data: data
      });

    },

    isActivityValid(activity){
      activity.emptyNameWarning = !activity.name;

      if(
        !(
          _.isNumber(activity.capitalValue) ||
          _.isNumber(activity.capitalMatchFundValue) ||
          _.isNumber(activity.revenueValue) ||
          _.isNumber(activity.revenueMatchFundValue)
        )
      ) {
        activity.emptyBudgetsWarning = true;
      } else {
        activity.emptyBudgetsWarning = false;
      }
    },

    mapProjectFundingSummary(populatedYears, yearData){
      let results = [];
      _.forEach(populatedYears, yearIndex => {
        let result =_.merge({
          year: yearIndex
        },_.find(yearData, {year: yearIndex}));
        results.push(result);
      });
      return results;
    },


    getMappedSections(yearBreakdown, nbSections) {
      let yearSections = [];

      let sectionLabels = DateUtil.getQuaterLabels();


      for (let i = 0; i < nbSections; i++) {
        //Need to set null values for claim/status to fix merging issues when BE is not returning a property
        let section =
          _.merge(
            {
              label: sectionLabels[i],
              totalLabel: 'Q' + (i + 1) + ' TOTALS',
              quarter: (i + 1),
              year: yearBreakdown.year,
              toggleId: yearBreakdown.year + '_' + (i + 1),
              claim: null,
              status: null,
              notClaimableReason: null
            },
            _.find(yearBreakdown.sections, {sectionNumber: i + 1})
          );

        section.hasMilestones = section.milestones && section.milestones.length > 0;
        _.forEach(section.milestones, milestone => {
          _.forEach(milestone.activities, activity => {
            activity.originalName = activity.name;
            this.isActivityValid(activity);
          });
        });

        yearSections.push(section);
      }
      return yearSections;
    },

    deleteActivity(projectId, blockId, activityId){
      return $http({
        url: `${config.basePath}/projects/${projectId}/funding/${blockId}/activities/${activityId}`,
        method: 'DELETE'
      });
    },

    attachEvidence(projectId, blockId, activityId, fileId) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/funding/${blockId}/activities/${activityId}/attachments`,
        method: 'POST',
        params: {
          fileId: fileId
        }
      });
    },

    deleteEvidence(projectId, blockId, activityId, attachmentId) {
      return $http.delete(`${config.basePath}/projects/${projectId}/funding/${blockId}/activities/${activityId}/attachments/${attachmentId}`);
    },

    addYearLabels(years) {
      _.forEach(years, (year) => {
        year['yearLabel'] = DateUtil.toFinancialYearString(_.toNumber(year.year));
      });
      return years;
    },

    claim(projectId, blockId, financialYear, quarter) {
      return $http.post(`${config.basePath}/projects/${projectId}/funding/${blockId}/claim/${financialYear}/quarter/${quarter}`);
    },

    cancelClaim(projectId, blockId, claimId) {
      return $http.delete(`${config.basePath}/projects/${projectId}/funding/${blockId}/claim/${claimId}`);
    }


  }
}

angular.module('GLA')
  .service('ProjectFundingService', ProjectFundingService);
