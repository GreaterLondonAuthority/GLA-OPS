import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {groupBy, isArray, sortBy, startCase} from "lodash-es";
import {UserService} from "../user/user.service";

@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  constructor(private http: HttpClient,
              private userService: UserService) {
  }

  /**
     * Update project answers
     * @param {Number} projectId - project id
     * @param {Number} blockId - block id
     * @param {Object} data - blockData
     * @return {Object} promise
     */
    updateProjectAnswers(projectId, blockId, data) {
      return this.http.put(`${environment.basePath}/projects/${projectId}/questions/${blockId}`, data)
    }

  /**
   * Retrieve the project block
   * @returns {Object} promise
   */
  getProjectBlock (projectId, blockId, tryLock) {
    return this.http.get(`${environment.basePath}/projects/${projectId}/${blockId}?tryLock=${!!tryLock}`);
  }

  /**
   * Unlock a block, without saving
   * @param {Number} projectId
   * @param {Number} blockId
   *
   * @returns {Object} promise
   */
  unlockBlock(projectId, blockId) {
    return this.http.put(`${environment.basePath}/projects/${projectId}/unlock/${blockId}`, null)
  }

  /**
   * Returns a list of project statuses only extracted from the all project states list.
   * @param projectStates list of all the project statuses and substatuses
   */
  toProjectStatuses(projectStates: { status: string }[]) {
    let projectStatuses = [];

    let groupStatuses = groupBy(projectStates, 'status');

    let keys = Object.keys(groupStatuses);
    // Closed should be at the bottom of the filter. So zzz will come last
    keys = sortBy(keys, status => status === 'Closed' ? 'zzz' : status.toLowerCase());

    keys.forEach(status => {
      let item = {
        name: status,
        label: status
      };
      projectStatuses.push(item);
    });

    return projectStatuses;
  }

  deleteProject(projectId){
    return this.http.delete(`${environment.basePath}/projects/${projectId}`, {responseType: 'text'})
  }

  claim(projectId, blockId, claim){
    let claimRequest = {
      entityId: claim.entityId,
      year: claim.year,
      claimTypePeriod: claim.claimTypePeriod,
      claimType: claim.claimType
    };
    return this.http.post(`${environment.basePath}/projects/${projectId}/block/${blockId}/claim`, claimRequest)
  }

  cancelClaim(projectId, blockId, claimIds){
    return this.http.delete(`${environment.basePath}/projects/${projectId}/block/${blockId}/claim/${claimIds}`);
  }

  transferProject(projectIds, orgId) {
    projectIds = isArray(projectIds) ? projectIds : [projectIds];
    let params = {
      organisationId: orgId
    }
    return this.http.put(`${environment.basePath}/projects/transfer`, projectIds, {params})
  }

  canProjectBeAssignedToTemplate(templateId, organisationId) {
    return this.http.get(`${environment.basePath}/projects/template/${templateId}/organisation/${organisationId}/createAllowed`)
  }

  createProject(data) {
    return this.http.post(`${environment.basePath}/projects`, data);
  }

  /**
   * Updates the project status
   * @param {Integer} projectId
   * @param {String} status to transition to
   * @param {String} subStatus optional subStatus to transition to
   * @param {String} comments optional comments
   */
  changeStatus(projectId, status, subStatus, comments, validateOnly?) {
    return this.http.put(`${environment.basePath}/projects/${projectId}/status?validateOnly=${!!validateOnly}`, {
      status: status,
      subStatus: subStatus,
      comments: comments
    })
  }

  /**
   * Updates the project status
   * @param {Integer} projectId
   * @param {String} status to transition to
   * @param {String} subStatus optional subStatus to transition to
   * @param {String} comments optional comments
   * @param {String} reason optional comments for reason why PaymentsOnly selected
   * @param {String} approvePaymentsOnly if payment only
   */
  changeStatusPaymentsOnly(projectId, status, subStatus, comments, reason, validateOnly?) {
    return this.http.put(`${environment.basePath}/projects/${projectId}/status?validateOnly=${!!validateOnly}`, {
      status: status,
      subStatus: subStatus,
      comments: comments,
      approvePaymentsOnly: true,
      reason: reason
    })
  }

  /**
   * Updates the project status
   * @param {Integer} projectId
   * @param {String} status to transition to
   * @param {String} subStatus optional subStatus to transition to
   * @param {String} comments optional comments
   * @param {String} reason optional comments for reason/authority for payments and approve changes
   */
  changeStatusPaymentsAndApproveChanges(projectId, status, subStatus, comments, reason, validateOnly?) {
    return this.http.put(`${environment.basePath}/projects/${projectId}/status?validateOnly=${!!validateOnly}`, {
      status: status,
      subStatus: subStatus,
      comments: comments,
      reason: reason
    })
  }

  validateTransition(projectId, transition) {
    return this.changeStatus(projectId, transition.status, transition.subStatus, null, true);
  }

  /**
   * Request to Abandon a project. this project status will change to closed
   * @param  {Integer} projectId
   */
  completeProject(projectId, comment) {
    return this.changeStatus(projectId, 'Closed', 'Completed', comment);
  }

  /**
   * Request to reinstate a project. this project status will change to last state (but not sub state
   * @param  {Integer} projectId
   */
  reinstateProject(projectId, comments) {
    return this.http.put(`${environment.basePath}/projects/${projectId}/reinstate`, comments);
  }

  /**
   * Request to Abandon a project. this project status will change to closed
   * @param  {Integer} projectId
   */
  requestAbandon(projectId, comment) {
    return this.changeStatus(projectId, 'Active', 'AbandonPending', comment);
  }

  /**
   * Reject a project. this project status will change to closed rejected
   * @param  {Integer} projectId
   */
  reject(projectId, comment) {
    return this.changeStatus(projectId, 'Closed', 'Rejected', comment);
  }

  /**
   * Abandon a project. this project status will change to closed abandoned
   * @param  {Integer} projectId
   */
  abandon(projectId, comment) {
    return this.changeStatus(projectId, 'Closed', 'Abandoned', comment);
  }

  filterDropdownItems(canViewRecommendations, projectStates) {
    let labels = {
      'Assess': 'Awaiting Recommendation'
    };

    let filterDropdownItems = [];

    let groupStatuses = groupBy(projectStates, 'status');

    let keys = Object.keys(groupStatuses);
    // Closed should be at the bottom of the filter. So zzz will come last
    keys = sortBy(keys, status => status === 'Closed' ? 'zzz' : status.toLowerCase());

    keys.forEach(status => {
      let item = {
        id: status,
        label: status,
        name: status
      } as any;

      if (groupStatuses[status].length === 1) {
        item.model = status !== 'Closed';
        item.projectStatusKey = status;
      } else {
        item.items = [];
        groupStatuses[status].forEach(state => {
          if (status === 'Assess') {
            item.items = this.getAssessChildrenItems(canViewRecommendations);
          } else {
            let subStatusLabel = labels[`${status}${state.subStatus || ''}`] || startCase(state.subStatus) || 'No Changes';
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
        item.items = sortBy(item.items, subStatus => subStatus.label.toLowerCase());
      }
      filterDropdownItems.push(item);
    });

    return filterDropdownItems;
  }

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
  }

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
      },
      {
        name: 'assignee',
        description: 'By Assignee',
        hint: 'Enter assignee name or email',
        maxLength: '50'
      }
    ];
  }

  subStatusText(project) {
    if (project.recommendation && project.statusName !== 'Active' && project.statusName !== 'Closed'
      && this.userService.hasPermission('proj.view.recommendation')) {
      return this.recommendationText(project);
    }

    return this.getSubStatusText(project.subStatusName);
  }

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

  /**
   * Retrieve list of all projects
   * @returns {Object} promise
   */
  getProjects(idOrTitle, organisationName, programmeName, assignee, states, programmes, templates, watchingProject, page) {
    let cfg = {
      params: {
        project: idOrTitle,
        organisation: organisationName,
        programme: programmeName,
        assignee: assignee,
        states: states,
        programmes: programmes, //Array now
        templates: templates,
        size: 50,
        watchingProject: watchingProject,
        page: page,
        sort: 'lastModified,desc'
      }
    } as any;

    return this.http.get(`${environment.basePath}/projects`, cfg);
  }

  /**
   * Retrieve list of all projects
   * @returns {Object} promise
   */
  getProgrammeAllocations(idOrTitle, organisationName, programmeName, assignee, states, programmes, templates, page) {
    let cfg = {
      params: {
        project: idOrTitle,
        organisation: organisationName,
        programme: programmeName,
        assignee: assignee,
        states: states,
        programmes: programmes, //Array now
        templates: templates,
        size: 50,
        page: page,
        sort: 'programmeName,orgName'
      }
    } as any;

    return this.http.get(`${environment.basePath}/programmeAllocations`, cfg);
  }

  /**
   * Mark a list of projects (passed as ID's) to be moved into assessed status
   * @param  [Integer] ids list of project id's
   * @return http promise
   */
  projectBulkOperation(ids, operation) {
    return this.http.put(`${environment.basePath}/projects/bulkOperation`, {
      operation: operation,
      projects: ids
    });
  }

  getAssignableUsers(projectIds: number[]) {
    let ids = projectIds.join(',')
    let params = new HttpParams().set('projectIds', ids)
    return this.http.get(`${environment.basePath}/projects/assignableUsers`, {params: params})
  }

  assignProject(projectId, assignee) {
    return this.http.put(`${environment.basePath}/projects/${projectId}/assignee`, assignee)
  }

  assignMultipleProjects(projects) {
    const payload = projects.map(p => {return {
      projectIds: [p.id],
      assignees: p.assignee.split('|')
    }
    })
    return this.http.put(`${environment.basePath}/projects/assignUsers`, payload)
  }

  unassignMultipleProjects(projectIds, assignees) {
    const projectAssigneeSummary = {projectIds, assignees}
    return this.http.put(`${environment.basePath}/projects/unassignUsers`, projectAssigneeSummary)
  }

  /**
   * Retrieve project history
   * @param {Number} id - project id
   * @return {Object} promise
   */
  getProjectHistory(id) {
    return this.http.get(`${environment.basePath}/projects/${id}/history`)
  }

  /**
   * Retrieve project by id
   * @param {Number} id - project id
   * @returns {Object} promise
   */
  getProject(id, params?) {
    params = params || {};
    return this.http.get(`${environment.basePath}/projects/${id}`, {params})
  }

  /**
   * Request to suspend/resume any payments on the project
   * @param  {Integer} projectId
   * @param  {boolean} paymentsSuspended flag
   * @param {String} comments user input comment
   */
  suspendProjectPayments(projectId, paymentsSuspended, comments) {
    return this.http.put(`${environment.basePath}/projects/${projectId}/suspendPayments?paymentsSuspended=${paymentsSuspended}`, comments)
  }

  /**
   * Update project affordable homes block
   * @param {Number} id - project id
   * @param {Object} data - project block
   * @return {Object} promise
   */
  updateProjectAffordableHomesBlock(id, data, autosave) {
    return this.http.put(`${environment.basePath}/projects/${id}/affordableHomes?autosave=${!!autosave}`, data)
  }

  /**
   * Update if project is marked for corporate or not
   * @param projectId
   * @param markedForCorporate (boolean)
   */
  updateProjectMarkedForCorporate(projectId, markedForCorporate) {
    return this.http.put(`${environment.basePath}/projects/${projectId}/markedForCorporate`, markedForCorporate)
  }

  isProjectMarkedForCorporate(projectId) {
    return this.http.get(`${environment.basePath}/projects/${projectId}/markedForCorporate`)
  }

  addLabel(projectId, label) {
    return this.http.post(`${environment.basePath}/projects/${projectId}/labels`, label)
  }

  shareProject(projectId, orgId) {
    return this.http.post(`${environment.basePath}/projects/${projectId}/accessControlList?orgId=${orgId}`, null);
  }

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
  }

  /**
   * Save land project to active state
   * @param {Number} id - project id
   * @param {Object} data
   * @return {Object} promise
   */
  saveProjectToActive(projectId, comment) {
    return this.changeStatus(projectId, 'Active', null, comment);
  }

  /**
   * Submit returned project
   * @param {Number} id - project id
   * @param {Object} data
   * @return {Object} promise
   */
  onSubmitReturnedProject(projectId, comment) {
    return this.changeStatus(projectId, 'Assess', null, comment);
  }

  /**
   * Withdraw a project
   * @param {Number} id - project id
   * @param {Object} data
   * @return {Object} promise
   */
  withdrawProject(projectId, comment) {
    return this.changeStatus(projectId, 'Draft', null, comment);
  }

  /**
   * Save a project comment
   * @param {Number} id - project id
   * @param {Object} data
   * @return {Object} promise
   */
  saveProjectComment(id, data) {
    return this.http.put(`${environment.basePath}/projects/${id}/draftcomment`, data)
  }

  /**
   * Update the project recommendation status to `RecommendApproval`
   * @param projectId
   */
  recommendApproval(projectId, comment) {
    return this.http.put(`${environment.basePath}/projects/${projectId}/recommendation/RecommendApproval`, comment)
  }

  /**
   * Update the project recommendation status to `RecommendReject`
   * @param projectId
   */
  recommendReject(projectId, comment) {
    return this.http.put(`${environment.basePath}/projects/${projectId}/recommendation/RecommendRejection`, comment)
  }

  /**
   * approve a project. this project status will change to active
   * @param  {Integer} projectId
   */
  approve(projectId, comment) {
    return this.changeStatus(projectId, 'Active', null, comment);
  }

  /**
   * Return a project
   * @param {Number} id - project id
   * @param {Object} data
   * @return {Object} promise
   */
  returnProject(projectId, comment) {
    return this.changeStatus(projectId, 'Returned', null, comment);
  }

  onReturnFromApprovalRequested(projectId, comments) {
    return this.changeStatus(projectId, 'Active', 'UnapprovedChanges', comments);
  }

  onApproveFromApprovalRequested(projectId, comments) {
    return this.changeStatus(projectId, 'Active', null, comments);
  }

  onRequestPaymentAuthorisation(projectId, comments, paymentsOnly, reason) {
    if (paymentsOnly) {
      return this.changeStatusPaymentsOnly(projectId, 'Active', 'PaymentAuthorisationPending', comments, reason);
    } else {
      return this.changeStatusPaymentsAndApproveChanges(projectId, 'Active', 'PaymentAuthorisationPending', comments, reason);
    }
  }


  approveAbandon(projectId, comments) {
    return this.changeStatus(projectId, 'Closed', 'Abandoned', comments);
  }

  rejectAbandon(projectId, comments) {
    return this.changeStatus(projectId, 'Active', null, comments);
  }

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
  }
}
