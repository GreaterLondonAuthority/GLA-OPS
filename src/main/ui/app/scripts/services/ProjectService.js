/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

ProjectService.$inject = ['$resource', '$http', 'config', 'UserService'];

function ProjectService($resource, $http, config, UserService) {

  return {
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
    getAllStatus: function () {
      return $resource(`${config.basePath}/projects/status`)
        .query({})
        .$promise;
    },


    /**
     * Retrieve list of all projects
     * @returns {Object} promise
     */
    getAllProjects: function (idOrTitle, organisationId, programmeId, programmeName) {
      return $resource(`${config.basePath}/projects`)
        .query({
          title: idOrTitle,
          organisationId: organisationId,
          programmeId: programmeId,
          programmeName: programmeName
        })
        .$promise;
    },


    /**
     * Create a new project
     * @param {Object} data
     * @returns {Object} promise
     */
    createProject: function (data) {
      return $http({
        url: config.basePath + '/projects',
        method: 'POST',
        data: data,
        serialize: false
      })
    },

    /**
     * Update a project
     * @param {Object} data
     * @param {Number} id
     * @return {Object} promise
     */
    updateProject: function (data, id) {
      return $http({
        url: config.basePath + '/projects/' + id + '/details',
        method: 'PUT',
        data: data,
        serialize: false
      })
    },

    /**
     * Retrieve project by id
     * @param {Number} id - project id
     * @returns {Object} promise
     */
    getProject: function (id, params) {
      params = params || {};
      return $http({
        url: config.basePath + '/projects/' + id,
        method: 'GET',
        params: params
      });
    },

    /**
     * Retrieve template by id
     * @param {Number} id - template id
     * @return {Object} promise
     */
    getTemplate: function (id) {
      return $http({
        url: config.basePath + '/templates/' + id,
        method: 'GET'
      });
    },

    /**
     * Retrieve answers for project template questions
     * @param {Number} projectId - project id
     * @param {Number} questionsId - block id
     * @return {Object} promise
     */
    getProjectQuestionsData: function (projectId, questionsId) {
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
    getProjectHistory: function (id) {
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
    getDesignStandards: function (id) {
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
    getGrantSource: function (id) {
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
    getProjectCalculateGrant: function (id) {
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
    getProjectNegotiatedGrant: function (id) {
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
    getProjectDeveloperLedGrant: function (id) {
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
    getProjectIndicativeGrant: function (id) {
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
    lookupProjectIdByLegacyProjectCode: function (legacyProjectCode) {
      return $http({
        url: config.basePath + '/projects/' + legacyProjectCode + '/id',
        method: 'GET'
      });
    },

    /**
     * Update project design standards
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    updateDesignStandards: function (id, data) {
      return $http({
        url: config.basePath + '/projects/' + id + '/design',
        method: 'PUT',
        data: data
      });
    },

    /**
     * Update project grant source
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    updateGrantSource: function (id, data) {
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
    updateProjectCalculateGrant: function (id, data, autosave) {
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
    updateProjectNegotiatedGrant: function (id, data, autosave) {
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
    updateProjectDeveloperLedGrant: function (id, data, autosave) {
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
    updateProjectIndicativeGrant: (id, data, autosave) => {
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
    saveProjectToActive(projectId, comment) {
      return this.changeStatus(projectId, 'Active', null, comment);
    },

    /**
     * Submit returned project
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    onSubmitReturnedProject(projectId, comment) {
      return this.changeStatus(projectId, 'Assess', null, comment);
    },

    /**
     * Withdraw a project
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    withdrawProject(projectId, comment) {
      return this.changeStatus(projectId, 'Draft', null, comment);
    },

    /**
     * Return a project
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    returnProject(projectId, comment) {
      return this.changeStatus(projectId, 'Returned', null, comment);
    },

    onReturnFromApprovalRequested(projectId, comments) {
      return this.changeStatus(projectId, 'Active', 'UnapprovedChanges', comments);
    },

    onApproveFromApprovalRequested(projectId, comments) {
      return this.changeStatus(projectId, 'Active', null, comments);
    },

    onRequestPaymentAuthorisation(projectId, comments) {
      return this.changeStatus(projectId, 'Active', 'PaymentAuthorisationPending', comments);
    },

    approveAbandon(projectId, comments) {
      return this.changeStatus(projectId, 'Closed', 'Abandoned', comments);
    },

    rejectAbandon(projectId, comments) {
      return this.changeStatus(projectId, 'Active', null, comments);
    },

    /**
     * Save a project comment
     * @param {Number} id - project id
     * @param {Object} data
     * @return {Object} promise
     */
    saveProjectComment: (id, data) => {
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
    updateProjectAnswers: (projectId, blockId, data) => {
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
    updateProjectMilestones: (id, blockId, data, keepLock) => {
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
    addProjectMilestones: (id, blockId, data) => {
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
    deleteProjectMilestone: (projectId, blockId, milestoneId) => {
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
    updateProjectProcessingRoute: (projectId, blockId, routeId) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/processingRoute/${blockId}`,
        method: 'PUT',
        data: routeId
      });
    },



    getProjectBudget: (projectId, blockId, year) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/${blockId}/annualSpendFor/${year}`,
        method: 'GET'
      });
    },

    /**
     * Retrieve SAP category codes
     * @param {String} type type of SAP codes being retrived so far values can be:
     * spend: for annual spend block (default)
     * receipt: for receipts block
     * @returns {Object} promise
     */
    getSapCategoryCodes: (type) => {
      if (type === 'receipt') {
        return $http({
          url: `${config.basePath}/finance/receiptCategories`,
          method: 'GET'
        })
      }
      else {
        return $http({
          url: `${config.basePath}/finance/spendCategories`,
          method: 'GET'
        })
      }
    },

    getFinanceCategories(){
      return $http.get(`${config.basePath}/finance/categories`, {cache: true});
    },

    getReceiptCategories(){
      return this.getFinanceCategories().then(rsp => {
        return _.filter(rsp.data, {receiptStatus: 'ReadWrite'});
      })
    },

    getSpendCategories(){
      return this.getFinanceCategories().then(rsp => {
        return _.filter(rsp.data, {spendStatus: 'ReadWrite'});
      })
    },


    /**
     * Unlock a block, without saving
     * @param {Number} projectId
     * @param {Number} blockId
     *
     * @returns {Object} promise
     */
    unlockBlock: (projectId, blockId) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/unlock/${blockId}`,
        method: 'PUT'
      })
    },

    /**
     * Retrieve the project block
     * @returns {Object} promise
     */
    getProjectBlock: (projectId, blockId, tryLock) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/${blockId}?tryLock=${!!tryLock}`,
        method: 'GET'
      });
    },

    /**
     * Retrieve the current set financial year
     * @returns {Object} promise
     */
    getCurrentFinancialYear: () => {
      return $http({
        url: `${config.basePath}/finance/currentFinancialYear`,
        method: 'GET'
      });
    },

    /**
     * Mark a list of projects (passed as ID's) to be moved into assessed status
     * @param  [Interger] ids list of project id's
     * @return http promise
     */
    projectBulkOperation: (ids, operation) => {
      return $http({
        url: `${config.basePath}/projects/bulkOperation`,
        method: 'PUT',
        data: {
          operation: operation,
          projects: ids
        }
      })
    },

    /**
     * Update the project recommendation status to `RecommendApproval`
     * @param projectId
     */
    recommendApproval: (projectId, comment) => {
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
    recommendReject: (projectId, comment) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/recommendation/RecommendRejection`,
        method: 'PUT',
        data: comment
      });
    },

    /**
     * approve a project. this project status will change to active
     * @param  {Interger} projectId
     */
    approve(projectId, comment) {
      return this.changeStatus(projectId, 'Active', null, comment);
    },

    /**
     * Reject a project. this project status will change to active
     * @param  {Interger} projectId
     */
    reject(projectId, comment) {
      return this.changeStatus(projectId, 'Closed', 'Rejected', comment);
    },
    /**
     * Abandon a project. this project status will change to active
     * @param  {Interger} projectId
     */
    abandon(projectId, comment) {
      return this.changeStatus(projectId, 'Closed', 'Abandoned', comment);
    },

    /**
     * Request to Abandon a project. this project status will change to closed
     * @param  {Interger} projectId
     */
    requestAbandon(projectId, comment) {
      return this.changeStatus(projectId, 'Active', 'AbandonPending', comment);
    },

    /**
     * Request to Abandon a project. this project status will change to closed
     * @param  {Interger} projectId
     */
    completeProject(projectId, comment) {
      return this.changeStatus(projectId, 'Closed', 'Completed', comment);
    },
    /**
     * Request to reinstate a project. this project status will change to last state (but not sub state
     * @param  {Interger} projectId
     */
    reinstateProject(projectId, comments) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/reinstate`,
        method: 'PUT',
        data: comments
      });
    },


    getBlockId(project, displayOrder) {

    },
    /**
     * Updates the project status
     * @param {Integer} projectId
     * @param {String} status to transition to
     * @param {String} subStatus optional subStatus to transition to
     * @param {String} comments optional comments
     */
    changeStatus(projectId, status, subStatus, comments, validateOnly) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/status?validateOnly=${!!validateOnly}`,
        method: 'PUT',
        data: {
          status: status,
          subStatus: subStatus,
          comments: comments
        }
      });
    },

    validateTransition(projectId, transition) {
      return this.changeStatus(projectId, transition.status, transition.subStatus, null, true);
    },

    filterDropdownItems(canViewRecommendations) {
      let filterDropdownItems = [];

      filterDropdownItems.push({
        checkedClass: 'active',
        ariaLabel: 'Filter projects by active status',
        name: 'active',
        // default value set in applyFilterState
        model: undefined,
        label: 'Active',
        projectStatusKey: 'Active',
        projectSubStatusKeys: ['ApprovedChanges', 'UnapprovedChanges', 'PaymentAuthorisationPending', null]
      });


      filterDropdownItems.push({
        checkedClass: 'activeUnapprovedChanges',
        ariaLabel: 'Filter projects by active unapproved changes status',
        name: 'activeUnapprovedChanges',
        // default value set in applyFilterState
        model: undefined,
        label: 'Active: Unapproved Changes',
        projectStatusKey: 'Active',
        projectSubStatusKeys: ['UnapprovedChanges']
      });

      filterDropdownItems.push({
        checkedClass: 'activeApprovalRequested',
        ariaLabel: 'Filter projects by active approval requested status',
        name: 'activeApprovalRequested',
        // default value set in applyFilterState
        model: undefined,
        label: 'Active: Approval Requested',
        projectStatusKey: 'Active',
        projectSubStatusKeys: ['ApprovalRequested']
      });

      filterDropdownItems.push({
        checkedClass: 'activePaymentAuthorisationPending',
        ariaLabel: 'Filter projects by active payment authorisation pending status',
        name: 'activePaymentAuthorisationPending',
        // default value set in applyFilterState
        model: undefined,
        label: 'Active: Payment Authorisation Pending',
        projectStatusKey: 'Active',
        projectSubStatusKeys: ['PaymentAuthorisationPending']
      });

      filterDropdownItems.push({
        checkedClass: 'activeAbandonPending',
        ariaLabel: 'Filter projects by active abandon pending status',
        name: 'activeAbandonPending',
        // default value set in applyFilterState
        model: undefined,
        label: 'Active: Abandon Pending',
        projectStatusKey: 'Active',
        projectSubStatusKeys: ['AbandonPending']
      });

      filterDropdownItems.push({
        checkedClass: 'assess',
        ariaLabel: 'Filter projects by assess status',
        name: 'assess',
        // default value set in applyFilterState
        model: undefined,
        label: 'Assess',
        projectStatusKey: 'Assess'
      });


      if (canViewRecommendations) {
        filterDropdownItems = _.concat(filterDropdownItems, [{
          checkedClass: 'assessRecommendApprove',
          ariaLabel: 'Filter projects by assess recommend approve status',
          name: 'assessRecommendApprove',
          // default value set in applyFilterState
          model: undefined,
          label: 'Assess: Recommend approve',
          projectStatusKey: 'Assess',
          projectRecommentationKey: 'RecommendApproval'
        },
          {
            checkedClass: 'assessRecommendReject',
            ariaLabel: 'Filter projects by assess recommend reject status',
            name: 'assessRecommendReject',
            // default value set in applyFilterState
            model: undefined,
            label: 'Assess: Recommend reject',
            projectStatusKey: 'Assess',
            projectRecommentationKey: 'RecommendRejection'
          }]);
      }

      filterDropdownItems = _.concat(filterDropdownItems, [
        {
          checkedClass: 'closed',
          ariaLabel: 'Filter projects by closed status',
          name: 'closed',
          // default value set in applyFilterState
          model: undefined,
          label: 'Closed',
          projectStatusKey: 'Closed',
          projectSubStatusKeys: ['Rejected', 'Abandoned', 'Completed', null]
        },
        {
          checkedClass: 'closedCompleted',
          ariaLabel: 'Filter projects by closed completed status',
          name: 'closedCompleted',
          model: undefined,
          label: 'Closed: Completed',
          projectStatusKey: 'Closed',
          projectSubStatusKeys: ['Completed']
        },
        {
          checkedClass: 'draft',
          ariaLabel: 'Filter projects by draft status',
          name: 'draft',
          // default value set in applyFilterState
          model: undefined,
          label: 'Draft',
          projectStatusKey: 'Draft'
        },
        {
          checkedClass: 'returned',
          ariaLabel: 'Filter projects by returned status',
          name: 'returned',
          // default value set in applyFilterState
          model: undefined,
          label: 'Returned',
          projectStatusKey: 'Returned'
        },
        {
          checkedClass: 'submitted',
          ariaLabel: 'Filter projects by submitted status',
          name: 'submitted',
          // default value set in applyFilterState
          model: undefined,
          label: 'Submitted',
          projectStatusKey: 'Submitted'
        }]);

      return filterDropdownItems;
    },

    searchOptions() {
      return [
        {
          name: 'title',
          description: 'By Project',
          hint: 'Enter the project id number or title',
          maxLength: '50'
        },
        {
          name: 'programmeName',
          description: 'By Programme',
          hint: 'Enter the programme name',
          maxLength: ''
        },
        {
          name: 'organisationId',
          description: 'By Org Code',
          hint: 'Enter the full GLA org code number',
          maxLength: '8'
        }
      ];
    },
    hasParentCondition(value) {
      return !!value.parentId;
    },

    isParentConditionMet(value, array, match) {
      let parentQuestion = _.find(array, match || {id: value.parentId});
      return parentQuestion.answer === value.parentAnswerToMatch;
    },

    transferProject(projectId, orgId) {
      return $http.put(`${config.basePath}/projects/${projectId}/organisation`, orgId);
    },

    subStatusText(project) {
      if (project.recommendation && project.status !== 'Active' && project.status !== 'Closed'
        && UserService.hasPermission('proj.view.recommendation')) {
        return this.recommendationText(project);
      }

      switch (project.subStatus) {
        case 'UnapprovedChanges':
          return 'Unapproved Changes';
        case 'ApprovalRequested':
          return 'Approval Requested';
        case 'AbandonPending':
          return 'Abandon Pending';
        case 'PaymentAuthorisationPending':
          return 'Payment Authorisation Pending';
        case 'Abandoned':
          return 'Abandoned';
        case 'Completed':
          return 'Completed';
        default:
          return null;
      }
    },

    recommendationText(project) {
      switch (project.recommendation) {
        case 'RecommendApproval':
          return 'Recommend Approve';
        case 'RecommendRejection':
          return 'Recommend Reject';
        default:
          return null;
      }
    }
  };
}

angular.module('GLA')
  .service('ProjectService', ProjectService);
