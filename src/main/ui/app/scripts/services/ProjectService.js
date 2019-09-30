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
     * Retrieve list of all projects
     * @returns {Object} promise
     */
    getProjects(idOrTitle, organisationName, programmeName, states, programmes, templates, watchingProject, page) {
      let cfg = {
        params: {
          project: idOrTitle,
          organisation: organisationName,
          programme: programmeName,
          states: states,
          programmes: programmes, //Array now
          templates: templates,
          size: 50,
          watchingProject: watchingProject,
          page: page,
          sort: 'lastModified,desc'
        }
      };

      return $http.get(`${config.basePath}/projects`, cfg);
    },


    /**
     * Create a new project
     * @param {Object} data
     * @returns {Object} promise
     */
    createProject(data) {
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
    getProject(id, params) {
      params = params || {};
      return $http({
        url: config.basePath + '/projects/' + id,
        method: 'GET',
        params: params
      });
    },

    canProjectBeAssignedToTemplate(templateId, organisationId) {
      return $http({
        url: config.basePath + `/projects/template/${templateId}/organisation/${organisationId}/createAllowed`,
        method: 'GET',
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
    updateProjectMarkedForCorporate(projectId, markedForCorporate) {
      return $http({
        url: `${config.basePath}/projects/${projectId}/markedForCorporate`,
        method: 'PUT',
        data: markedForCorporate
      });
    },

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
     * Mark a list of projects (passed as ID's) to be moved into assessed status
     * @param  [Interger] ids list of project id's
     * @return http promise
     */
    projectBulkOperation(ids, operation) {
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
    recommendReject(projectId, comment) {
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
     * Reject a project. this project status will change to closed rejected
     * @param  {Interger} projectId
     */
    reject(projectId, comment) {
      return this.changeStatus(projectId, 'Closed', 'Rejected', comment);
    },

    /**
     * Abandon a project. this project status will change to closed abandoned
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


    /**
     * Transition to
     * @param projectId
     * @param status
     * @param subStatus
     * @param comments
     * @param validateOnly
     * @returns {*}
     */
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

    validateTransition(projectId, transition) {
      return this.changeStatus(projectId, transition.status, transition.subStatus, null, true);
    },

    addLabel(projectId, label) {
      return $http.post(`${config.basePath}/projects/${projectId}/labels`, label)
    },

    filterDropdownItems(canViewRecommendations, projectStates) {
      let labels = {
        'Assess': 'Awaiting Recommendation'
      };

      let filterDropdownItems = [];

      let groupStatuses = _.groupBy(projectStates, 'status');

      let keys = Object.keys(groupStatuses);
      // Closed should be at the bottom of the filter. So zzz will come last
      keys = _.sortBy(keys, status => status === 'Closed' ? 'zzz' : status.toLowerCase());

      keys.forEach(status => {
        let item = {
          id: status,
          label: status,
          name: status
        };

        if (groupStatuses[status].length === 1) {
          item.model = status !== 'Closed';
          item.projectStatusKey = status;
        } else {
          item.items = [];
          groupStatuses[status].forEach(state => {
            if (status === 'Assess') {
              item.items = this.getAssessChildrenItems(canViewRecommendations);
            } else {
              let subStatusLabel = labels[`${status}${state.subStatus || ''}`] || _.startCase(state.subStatus) || 'No Changes';
              item.items.push({
                name: `${status}${state.subStatus}`,
                model: status !== 'Closed',
                label: subStatusLabel,
                ariaLabel: `${status}: ${subStatusLabel}`,
                projectStatusKey: status,
                projectSubStatusKeys: state.subStatus === 'Rejected' ? [state.subStatus, null] : [state.subStatus]
              });
            }
          });
          item.items = _.sortBy(item.items, subStatus => subStatus.label.toLowerCase());
        }
        filterDropdownItems.push(item);
      });

      return filterDropdownItems;
    },

    getAssessChildrenItems(canViewRecommendations) {
      if (canViewRecommendations) {
        return [{
          name: 'assessRecommendApprove',
          model: true,
          label: 'Recommend Approve',
          projectStatusKey: 'Assess',
          projectRecommentationKeys: ['RecommendApproval']
        }, {
          name: 'assessRecommendReject',
          model: true,
          label: 'Recommend Reject',
          projectStatusKey: 'Assess',
          projectRecommentationKeys: ['RecommendRejection']
        }, {
          checkedClass: 'assess',
          name: 'assessNull',
          model: true,
          label: 'Awaiting Recommendation',
          projectStatusKey: 'Assess',
          projectRecommentationKeys: [null]

        }];
      } else {
        return [{
          checkedClass: 'assess',
          name: 'assessAll',
          model: true,
          label: 'Awaiting Recommendation',
          projectStatusKey: 'Assess',
          projectRecommentationKeys: [null, 'RecommendApproval', 'RecommendRejection']
        }];
      }
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
          maxLength: '50'
        },
        {
          name: 'organisationName',
          description: 'By Organisation',
          hint: 'Enter the org name or id',
          maxLength: '50'
        }
      ];
    },

    transferProject(projectIds, orgId) {
      projectIds = _.isArray(projectIds) ? projectIds : [projectIds];
      return $http({
        method: 'PUT',
        url: `${config.basePath}/projects/transfer`,
        params: {
          // projectIds: projectIds,
          organisationId: orgId
        },
        data: projectIds
      })
    },

    subStatusText(project) {
      if (project.recommendation && project.statusName !== 'Active' && project.statusName !== 'Closed'
        && UserService.hasPermission('proj.view.recommendation')) {
        return this.recommendationText(project);
      }

      return this.getSubStatusText(project.subStatusName);
    }
    ,
    getProjectStates() {
      return $http.get(`${config.basePath}/projects/filters/statuses`);
    },

    getSubStatusText(subStatusCode) {
      switch (subStatusCode) {
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
        case 'Rejected':
          return 'Rejected';
        case 'Completed':
          return 'Completed';
        default:
          return subStatusCode;
      }
    }
    ,

    recommendationText(project) {
      switch (project.recommendation) {
        case 'RecommendApproval':
          return 'Recommend Approve';
        case 'RecommendRejection':
          return 'Recommend Reject';
        default:
          return null;
      }
    },

    /**
     * Update project subcontractors
     * @param {Number} id - project id
     * @param {Number} blockId - block id
     * @param {Object} data
     * @return {Object} promise
     */
    updateProjectSubcontractors: (id, blockId, data) => {
      return $http({
        url: `${config.basePath}/projects/${id}/subcontractors/${blockId}`,
        method: 'PUT',
        data: data
      });
    },


    /**
     * Add project subcontractors
     * @param {Number} id - project id
     * @param {Number} blockId - block id
     * @return {Object} promise
     */
    addProjectSubcontractor(id, blockId, data) {
      return $http({
        url: `${config.basePath}/projects/${id}/subcontractors/${blockId}`,
        method: 'POST',
        data: data
      });
    },

    /**
     * Add subcontractor deliverable
     * @param {Number} id - project id
     * @param {Number} blockId - block id
     * @param {Number} subcontractorId - subcontractor id
     * @return {Object} promise
     */
    addProjectSubcontractorDeliverable(id, blockId, subcontractorId, data) {
      return $http({
        url: `${config.basePath}/projects/${id}/block/${blockId}/subcontractor/${subcontractorId}/deliverable`,
        method: 'POST',
        data: data
      });
    },

    /**
     * update subcontractor deliverable
     * @param {Number} id - project id
     * @param {Number} blockId - block id
     * @param {Number} subcontractorId - subcontractor id
     * @param {Number} deliverableId - deliverable Id
     * @return {Object} promise
     */
    updateProjectSubcontractorDeliverable(id, blockId, subcontractorId, deliverableId,  data) {
      return $http({
        url: `${config.basePath}/projects/${id}/block/${blockId}/subcontractor/${subcontractorId}/deliverable/${deliverableId}`,
        method: 'PUT',
        data: data
      });
    },

    /**
     * Delete project subcontractors
     * @param {Number} projectId - project id
     * @param {Number} blockId - block id
     * @param {Number} subcontractorId
     * @return {Object} promise
     */
    deleteProjectSubcontractor: (projectId, blockId, subcontractorId) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/subcontractors/${blockId}/subcontractor/${subcontractorId}`,
        method: 'DELETE'
      });
    },

    /**
     * Delete project subcontractor deliverable
     * @param {Number} projectId - project id
     * @param {Number} blockId - block id
     * @param {Number} subcontractorId
     * @param {Number} deliverableId
     * @return {Object} promise
     */
    deleteProjectDeliverable: (projectId, blockId, subcontractorId, deliverableId) => {
      return $http({
        url: `${config.basePath}/projects/${projectId}/block/${blockId}/subcontractor/${subcontractorId}/deliverable/${deliverableId}`,
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

    cancelClaim(projectId, blockId, claimId){
      return $http.delete(`${config.basePath}/projects/${projectId}/block/${blockId}/claim/${claimId}`);
    }

  };
}

angular.module('GLA')
  .service('ProjectService', ProjectService);
