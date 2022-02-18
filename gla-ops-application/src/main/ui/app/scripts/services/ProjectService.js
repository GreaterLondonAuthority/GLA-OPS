/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ProjectService.$inject = ['$resource', '$http', 'config', 'UserService', 'GlaProjectService'];

function ProjectService($resource, $http, config, UserService, GlaProjectService) {

  return {
    //TODO remove
    getTransitionMap() {
      return {
        'Initial_Assessment': 'Initial assessment complete',
        'Closed': 'Closed: Rejected',
        'Abandoned': 'Closed: Abandoned',
        'Completed': 'Closed: Completed',
        'Amend': 'Approved project amended',
        'ApprovalRequested': 'Active: Approval Requested',
        'UnapprovedChanges': 'Active: Unapproved Changes',
        'AbandonRequested': 'Active: Abandon Requested',
        'AbandonRejected': 'Active: Abandon Rejected',
        'Assess': 'Assessed',
        'PaymentAuthorisationRequested': 'Payment Authorisation Requested',
        'DeletedUnapprovedChanges': 'Deleted unapproved changes'
      };
    },

    /**
     * Retrieve list of all project status
     * @returns {Object} promise
     */
    getAllStatus() {
      return $resource(`${config.basePath}/projects/status`)
        .query({})
        .$promise;
    },


    /**
     * Retrieve list of all projects
     * @returns {Object} promise
     */
    getAllProjects(idOrTitle, organisationName, programmeId, programmeName) {
      return $resource(`${config.basePath}/projects`)
        .query({
          title: idOrTitle,
          organisationName: organisationName,
          programmeId: programmeId,
          programmeName: programmeName
        })
        .$promise;
    },

    /**
     * Update a project
     * @param {Object} data
     * @param {Number} id
     * @return {Object} promise
     */
    updateProject(data, id) {
      return $http({
        url: config.basePath + '/projects/' + id + '/details',
        method: 'PUT',
        data: data,
        serialize: false
      })
    },

    /**
     * Retrieve project overview by id
     * @param {Number} id - project id
     * @returns {Object} promise
     */
    getProjectOverview(id, params) {
      params = params || {};
      return $http.get(`${config.basePath}/projectOverview/${id}/`, params);
    },

    /**
     * Retrieve project by id
     * @param {Number} id - project id
     * @returns {Object} promise
     */
    //TODO remove
    getProject(id, params) {
      params = params || {};
      return $http({
        url: config.basePath + '/projects/' + id,
        method: 'GET',
        params: params
      });
    },

    /**
     * Retrieve answers for project template questions
     * @param {Number} projectId - project id
     * @param {Number} questionsId - block id
     * @return {Object} promise
     */
    getProjectQuestionsData(projectId, questionsId) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/questions/${questionsId}`,
        method: 'GET'
      })
    },

    /**
     * Retrieve project history
     * @param {Number} id - project id
     * @return {Object} promise
     */
    //TODO remove
    getProjectHistory(id) {
      return $resource(config.basePath + '/projects/:id/history')
        .query({
          id: id
        })
        .$promise;
    },

    /**
     * Retrieve project design standards
     * @param {Number} id - project id
     * @return {Object} promise
     */
    getDesignStandards(id) {
      return $resource(config.basePath + '/projects/:id/design')
        .get({
          id: id
        })
        .$promise;
    },

    /**
     * Retrieve project grant source
     * @param {Number} id - project id
     * @return {Object} promise
     */
    getGrantSource(id) {
      return $resource(config.basePath + '/projects/:id/grant')
        .get({
          id: id
        })
        .$promise;
    },

    /**
     * Retrieve project 'calculate grant'
     * @param {Number} id - project id
     * @return {Object} promise
     */
    getProjectCalculateGrant(id) {
      return $resource(config.basePath + '/projects/:id/calculateGrant')
        .get({
          id: id
        })
        .$promise;
    },

    /**
     * Retrieve project negotiated grant
     * @param {Number} id - project id
     * @return {Object} promise
     */
    getProjectNegotiatedGrant(id) {
      return $resource(config.basePath + '/projects/:id/negotiatedGrant')
        .get({
          id: id
        })
        .$promise;
    },


    /**
     * Retrieve project developer led grant
     * @param {Number} id - project id
     * @return {Object} promise
     */
    getProjectDeveloperLedGrant(id) {
      return $resource(`${config.basePath}/projects/:id/developerLedGrant`)
        .get({
          id: id
        })
        .$promise;
    },

    /**
     * Retrieve project indicative grant
     * @param {Number} id - project id
     * @return {Object} promise
     */
    getProjectIndicativeGrant(id) {
      return $resource(`${config.basePath}/projects/:id/indicativeGrant`)
        .get({
          id: id
        })
        .$promise;
    },

    /**
     * Looks up a project id given a legacy project code
     * @param {String} legacyProjectCode - legacy project code
     * @return {Object} promise
     */
    lookupProjectIdByLegacyProjectCode(legacyProjectCode) {
      return $http({
        url: config.basePath + '/projects/' + legacyProjectCode + '/id',
        method: 'GET'
      });
    },

    /**
     * Update project grant source
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    updateGrantSource(id, data) {
      return $http({
        url: config.basePath + '/projects/' + id + '/grant',
        method: 'PUT',
        data: data
      });
    },

    /**
     * Update project 'calculate grant'
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    updateProjectCalculateGrant(id, data, autosave) {
      return $http({
        url: `${config.basePath}/projects/${id}/calculateGrant?autosave=${!!autosave}`,
        method: 'PUT',
        data: data
      });
    },

    /**
     * Update project negotiated grant
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    updateProjectNegotiatedGrant(id, data, autosave) {
      return $http({
        url: `${config.basePath}/projects/${id}/negotiatedGrant?autosave=${!!autosave}`,
        method: 'PUT',
        data: data
      });
    },

    /**
     * Update project developer led grant
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    updateProjectDeveloperLedGrant(id, data, autosave) {
      return $http({
        url: `${config.basePath}/projects/${id}/developerLedGrant?autosave=${!!autosave}`,
        method: 'PUT',
        data: data
      });
    },

    /**
     * Update project indicative grant
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    updateProjectIndicativeGrant(id, data, autosave) {
      return $http({
        url: `${config.basePath}/projects/${id}/indicativeGrant?autosave=${!!autosave}`,
        method: 'PUT',
        data: data
      });
    },

    /**
     * Submit project
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    submitProject(projectId, comment) {
      return this.changeStatus(projectId, 'Submitted', null, comment);
    },

    /**
     * Save land project to active state
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    //TODO Remove
    saveProjectToActive(projectId, comment) {
      return this.changeStatus(projectId, 'Active', null, comment);
    },

    /**
     * Submit returned project
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    //TODO remove
    onSubmitReturnedProject(projectId, comment) {
      return this.changeStatus(projectId, 'Assess', null, comment);
    },

    /**
     * Withdraw a project
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    //TODO remove
    withdrawProject(projectId, comment) {
      return this.changeStatus(projectId, 'Draft', null, comment);
    },

    /**
     * Return a project
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    //TODO remove
    returnProject(projectId, comment) {
      return this.changeStatus(projectId, 'Returned', null, comment);
    },
//TODO remove
    onReturnFromApprovalRequested(projectId, comments) {
      return this.changeStatus(projectId, 'Active', 'UnapprovedChanges', comments);
    },
//TODO remove
    onApproveFromApprovalRequested(projectId, comments) {
      return this.changeStatus(projectId, 'Active', null, comments);
    },
//TODO remove
    onRequestPaymentAuthorisation(projectId, comments) {
      return this.changeStatus(projectId, 'Active', 'PaymentAuthorisationPending', comments);
    },
//TODO remove
    approveAbandon(projectId, comments) {
      return this.changeStatus(projectId, 'Closed', 'Abandoned', comments);
    },
//TODO remove
    rejectAbandon(projectId, comments) {
      return this.changeStatus(projectId, 'Active', null, comments);
    },

    /**
     * Save a project comment
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    //TODO remove
    saveProjectComment(id, data) {
      return $http({
        url: config.basePath + '/projects/' + id + '/draftcomment',
        method: 'PUT',
        data: data
      });
    },

    /**
     * Update project answers
     * @param {Number} projectId - project id
     * @param {Number} blockId - block id
     * @param {Object} data - blockData
     * @return {Object} promise
     */
    updateProjectAnswers(projectId, blockId, data) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/questions/${blockId}`,
        method: 'PUT',
        data: data
      });
    },

    /**
     * Update project milestones
     * @param {Number} id - project id
     * @param {Number} blockId - block id
     * @param {Object} data
     * @return {Object} promise
     */
    updateProjectMilestones(id, blockId, data, keepLock) {
      return $http({
        url: `${config.basePath}/projects/${id}/milestones/${blockId}?autosave=${keepLock}`,
        method: 'PUT',
        data: data
      });
    },


    /**
     * Add project milestones
     * @param {Number} id - project id
     * @param {Number} blockId - block id
     * @return {Object} promise
     */
    addProjectMilestones(id, blockId, data) {
      return $http({
        url: `${config.basePath}/projects/${id}/milestones/${blockId}`,
        method: 'POST',
        data: data
      });
    },

    /**
     * Delete project milestones
     * @param {Number} projectId - project id
     * @param {Number} blockId - block id
     * @param {Number} milestoneId
     * @return {Object} promise
     */
    deleteProjectMilestone(projectId, blockId, milestoneId) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/milestones/${blockId}/milestone/${milestoneId}`,
        method: 'DELETE'
      });
    },

    /**
     * Update the project milestones processing route
     * @param {Number} projectId
     * @param {Number} blockId
     * @param {Number} routeId
     */
    updateProjectProcessingRoute(projectId, blockId, routeId) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/processingRoute/${blockId}`,
        method: 'PUT',
        data: routeId
      });
    },


    getProjectBudget(projectId, blockId, year) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/${blockId}/annualSpendFor/${year}`,
        method: 'GET'
      });
    },

    /**
     * Update if project is marked for corporate or not
     * @param projectId
     * @param markedForCorporate (boolean)
     */
    //TODO remove
    updateProjectMarkedForCorporate(projectId, markedForCorporate) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/markedForCorporate`,
        method: 'PUT',
        data: markedForCorporate
      });
    },

    //TODO remove
    isProjectMarkedForCorporate(projectId) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/markedForCorporate`,
        method: 'GET'
      });
    },


    /**
     * update the ledger entry (e.g. receipt, funding ...)
     * @param projectId Project id
     * @param Block id
     * @param entry ledger entry
     * @returns {*}
     */
    postLedgerEntry(projectId, blockId, entry) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/blocks/${blockId}/ledgerEntries`,
        method: 'POST',
        data: entry
      });
    },

    /**
     * Delete the ledger entry (e.g. receipt, funding ...)
     * @param projectId Project id
     * @param id Receipt id
     * @returns {*}
     */
    deleteLedgerEntry(projectId, blockId, entryId) {
        return $http({
          url: `${config.basePath}/projects/${projectId}/blocks/${blockId}/ledgerEntries/${entryId}`,
          method: 'DELETE'
        });
      },

    /**
     * Retrieve SAP category codes
     * @param {String} type type of SAP codes being retrived so far values can be:
     * spend: for annual spend block (default)
     * receipt: for receipts block
     * @returns {Object} promise
     */
    getSapCategoryCodes(type) {
        if (type === 'receipt') {
          return $http({
            url: `${config.basePath}/finance/receiptCategories`,
            method: 'GET'
          })
        } else {
          return $http({
            url: `${config.basePath}/finance/spendCategories`,
            method: 'GET'
          })
        }
      },


    /**
     * Unlock a block, without saving
     * @param {Number} projectId
     * @param {Number} blockId
     *
     * @returns {Object} promise
     */
    unlockBlock(projectId, blockId) {
        return $http({
          url: `${config.basePath}/projects/${projectId}/unlock/${blockId}`,
          method: 'PUT'
        })
      },

    /**
     * Retrieve the project block
     * @returns {Object} promise
     */
    getProjectBlock (projectId, blockId, tryLock) {
        return $http({
          url: `${config.basePath}/projects/${projectId}/${blockId}?tryLock=${!!tryLock}`,
          method: 'GET'
        });
      },

    /**
     * Retrieve the current set financial year
     * @returns {Object} promise
     */
    getCurrentFinancialYear() {
        return $http({
          url: `${config.basePath}/finance/currentFinancialYear`,
          method: 'GET'
        });
      },


    /**
     * Update the project recommendation status to `RecommendApproval`
     * @param projectId
     */
    //TODO remove
    recommendApproval(projectId, comment) {
        return $http({
          url: `${config.basePath}/projects/${projectId}/recommendation/RecommendApproval`,
          method: 'PUT',
          data: comment
        });
      },

    /**
     * Update the project recommendation status to `RecommendReject`
     * @param projectId
     */
    //TODO remove

    recommendReject(projectId, comment) {
        return $http({
          url: `${config.basePath}/projects/${projectId}/recommendation/RecommendRejection`,
          method: 'PUT',
          data: comment
        });
      },

    /**
     * approve a project. this project status will change to active
     * @param  {Integer} projectId
     */
    //TODO remove

    approve(projectId, comment) {
      return this.changeStatus(projectId, 'Active', null, comment);
    },

    /**
     * Reject a project. this project status will change to closed rejected
     * @param  {Integer} projectId
     */
    //TODO remove

    reject(projectId, comment) {
      return this.changeStatus(projectId, 'Closed', 'Rejected', comment);
    },

    /**
     * Request to suspend/resume any payments on the project
     * @param  {Integer} projectId
     * @param  {boolean} paymentsSuspended flag
     * @param {String} comments user input comment
     */
    //TODO remove
    suspendProjectPayments(projectId, paymentsSuspended, comments) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/suspendPayments?paymentsSuspended=${paymentsSuspended}`,
        method: 'PUT',
        data: comments
      });
    },

    /**
     * Updates the project status
     * @param {Integer} projectId
     * @param {String} status to transition to
     * @param {String} subStatus optional subStatus to transition to
     * @param {String} comments optional comments
     */
    changeStatus(projectId, status, subStatus, comments, validateOnly) {
      return GlaProjectService.changeStatus(projectId, status, subStatus, comments, validateOnly).toPromise();
    },


    /**
     * Transition to
     * @param projectId
     * @param status
     * @param subStatus
     * @param comments
     * @param validateOnly
     * @returns {*}
     */
    //TODO remove
    transitionTo(projectId, transition, comment) {
      return this.changeStatus(projectId, transition.status, transition.subStatus, comment);
    },


    /**
     * Look up a list of project IDs given a WBS code
     * @returns {Object} promise
     */
    findAllProjectIdsByWBSCode: (wbsCode) => {
      return $http({
        url: `${config.basePath}/projects/wbsLookup?wbsCode=${wbsCode}`,
        method: 'GET'
      });
    },
    //TODO remove
    addLabel(projectId, label) {
      return $http.post(`${config.basePath}/projects/${projectId}/labels`, label)
    },

    subStatusText(project) {
      return GlaProjectService.subStatusText(project);
    },

    getProjectStates() {
      return $http.get(`${config.basePath}/projects/filters/statuses`);
    },

    getSubStatusText(subStatusCode) {
      return GlaProjectService.getSubStatusText(subStatusCode);
    },

    recommendationText(project) {
      return GlaProjectService.recommendationText(project);
    },

    /**
     * Update project delivery partners
     * @param {Number} id - project id
     * @param {Number} blockId - block id
     * @param {Object} data
     * @return {Object} promise
     */
    updateProjectDeliveryPartners: (id, blockId, data) => {
      return $http({
        url: `${config.basePath}/projects/${id}/deliveryPartners/${blockId}`,
        method: 'PUT',
        data: data
      });
    },


    /**
     * Add project delivery partners
     * @param {Number} id - project id
     * @param {Number} blockId - block id
     * @return {Object} promise
     */
    addProjectDeliveryPartners(id, blockId, data) {
      return $http({
        url: `${config.basePath}/projects/${id}/deliveryPartners/${blockId}`,
        method: 'POST',
        data: data
      });
    },

    /**
     * Add delivery partners deliverable
     * @param {Number} id - project id
     * @param {Number} blockId - block id
     * @param {Number} deliveryPartnerId - delivery partner id
     * @return {Object} promise
     */
    addProjectPartnerDeliverable(id, blockId, deliveryPartnerId, data) {
      return $http({
        url: `${config.basePath}/projects/${id}/block/${blockId}/deliveryPartner/${deliveryPartnerId}/deliverable`,
        method: 'POST',
        data: data
      });
    },

    /**
     * update delivery partner deliverable
     * @param {Number} id - project id
     * @param {Number} blockId - block id
     * @param {Number} deliveryPartnerId - delivery partner id
     * @param {Number} deliverableId - deliverable Id
     * @return {Object} promise
     */
    updateProjectPartnerDeliverable(id, blockId, deliveryPartnerId, deliverableId,  data) {
      return $http({
        url: `${config.basePath}/projects/${id}/block/${blockId}/deliveryPartner/${deliveryPartnerId}/deliverable/${deliverableId}`,
        method: 'PUT',
        data: data
      });
    },

    /**
     * Delete project delivery partners
     * @param {Number} projectId - project id
     * @param {Number} blockId - block id
     * @param {Number} deliveryPartnerId
     * @return {Object} promise
     */
    deleteProjectDeliveryPartner: (projectId, blockId, deliveryPartnerId) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/deliveryPartners/${blockId}/deliveryPartner/${deliveryPartnerId}`,
        method: 'DELETE'
      });
    },

    /**
     * Delete project delivery partners deliverable
     * @param {Number} projectId - project id
     * @param {Number} blockId - block id
     * @param {Number} deliveryPartnerId
     * @param {Number} deliverableId
     * @return {Object} promise
     */
    deleteProjectDeliverable: (projectId, blockId, deliveryPartnerId, deliverableId) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/block/${blockId}/deliveryPartner/${deliveryPartnerId}/deliverable/${deliverableId}`,
        method: 'DELETE'
      });
    },

    getDeliverableFeeCalculation: (projectId, blockId, value, fee) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/block/${blockId}/getDeliverableFeeCalculation`,
        method: 'GET',
        params: {
          value: value,
          fee: fee
        }
      });
    },

    claim(projectId, blockId, claim){
      let claimRequest = {
        entityId: claim.entityId,
        year: claim.year,
        claimTypePeriod: claim.claimTypePeriod,
        claimType: claim.claimType
      };
      return $http.post(`${config.basePath}/projects/${projectId}/block/${blockId}/claim`, claimRequest)
    },

    cancelClaim(projectId, blockId, claimIds){
      return $http.delete(`${config.basePath}/projects/${projectId}/block/${blockId}/claim/${claimIds}`);
    },
    //TODO remove
    shareProject(projectId, orgId) {
      return $http.post(`${config.basePath}/projects/${projectId}/accessControlList?orgId=${orgId}`);
    }
  };
}

angular.module('GLA')
  .service('ProjectService', ProjectService);
