/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import DateUtil from '../util/DateUtil';

AnnualSubmissionService.$inject = ['$http', 'config', '$rootScope', 'fYearFilter', 'currencyFilter'];

function AnnualSubmissionService($http, config, $rootScope, fYearFilter, currencyFilter) {

  return {


    getAnnualSubmission(id) {
      return $http({
        url: config.basePath + `/annualSubmissions/${id}`
      });
    },

    updateAnnualSubmissionOverview(id, annualSubmission) {
      return $http({
        url: config.basePath + `/annualSubmissions/${id}`,
        method: 'PUT',
        data: annualSubmission
      })
    },
    updateAnnualSubmissionStatus(id, status, agreementText) {
      return $http({
        url: config.basePath + `/annualSubmissions/${id}/status`,
        method: 'PUT',
        params: {
          status: status,
        },
        data: agreementText
      })
    },

    /**
     *
     */
    createNewAnnualSubmission(organisationId, financialYear) {
      const data = {
        organisationId: organisationId,
        financialYear: financialYear
      };

      return $http({
        url: config.basePath + '/annualSubmissions/',
        method: 'POST',
        data: data
      })
    },

    updateAnnualSubmissionBlock(submissionId, block) {
      return $http.put(`${config.basePath}/annualSubmissions/${submissionId}/blocks/${block.id}`, block);
    },

    getRemainingYears(orgId) {
      return $http.get(`${config.basePath}/availableYearsForCreation`, {params: {organisationId: orgId}}).then(rsp => {
        const list = [];
        _.forEach(rsp.data, i => {
          list.push({
              label: i.toString() + '/' + (i + 1).toString().slice(2, 4),
              financialYear: i
            }
          );
        });
        return list;
      });
    },

    getAnnualSubmissionCategories() {
      return $http.get(`${config.basePath}/annualSubmissionCategories`);
    },

    saveAnnualSubmissionEntry(submissionId, blockId, entry) {
      return $http.post(`${config.basePath}/annualSubmissions/${submissionId}/blocks/${blockId}/entries`, entry);
    },

    updateAnnualSubmissionEntry(submissionId, blockId, entry) {
      return $http.put(`${config.basePath}/annualSubmissions/${submissionId}/blocks/${blockId}/entries/${entry.id}`, entry);
    },

    deleteAnnualSubmissionEntry(submissionId, blockId, entry){
      return $http.delete(`${config.basePath}/annualSubmissions/${submissionId}/blocks/${blockId}/entries/${entry.id}`, entry);
    },

    getAnnualSubmissionBlock(submissionId, blockId) {
      return $http.get(`${config.basePath}/annualSubmissions/${submissionId}/blocks/${blockId}`);
    },

    getBalanceTiles(block) {
      let format = (val) => {
        return val != null ? currencyFilter(val, '£', 0) : '-'
      };

      return (block.totals || []).map(t => {
        return {
          name: `BALANCE ${fYearFilter(t.financialYear)}`,
          items: [{
            itemName: 'Opening balance',
            itemValue: format(t.openingBalance),
          }, {
            itemName: 'Total generated',
            itemValue: format(t.totalGenerated),
            icon: 'glyphicon-plus'
          }, {
            itemName: 'Total spent',
            itemValue: format(t.totalSpent),
            icon: 'glyphicon-minus'
          }, {
            itemName: 'Closing balance',
            itemValue: format(t.closingBalance)
          }]
        };
      });
    },

    getAvailableCategories(categories, grantSource,  type, status, existingEntries) {
      return _.filter(categories, (c) => {
        return c.grant === grantSource && c.hidden === false && c.type === type && c.status === status && !_.find(existingEntries, {category: {id: c.id}});
      });
    }
  };
}

angular.module('GLA')
  .service('AnnualSubmissionService', AnnualSubmissionService);

/**
 Validation failed for argument at index 1 in method:
 public void uk.gov.london.ops.web.api.AnnualSubmissionAPI.update(java.lang.Integer,uk.gov.london.ops.domain.organisation.AnnualSubmission), with 2 error(s):
 [Field error in object 'annualSubmission' on field 'organisationId':
 rejected value [null]; codes [NotNull.annualSubmission.organisationId,NotNull.organisationId,NotNull.java.lang.Integer,NotNull]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable:
 codes [annualSubmission.organisationId,organisationId]; arguments []; default message [organisationId]]; default message [may not be null]] [Field error in object 'annualSubmission' on field 'financialYear':
 rejected value [null]; codes [NotNull.annualSubmission.financialYear,NotNull.financialYear,NotNull.java.lang.Integer,NotNull]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable:
 codes [annualSubmission.financialYear,financialYear]; arguments []; default message [financialYear]]; default message [may not be null]]
 **/
