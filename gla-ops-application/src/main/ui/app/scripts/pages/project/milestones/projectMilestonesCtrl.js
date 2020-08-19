/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';
import './reclaimInfoModal/reclaimInfoModal.js';

/**
 * This class handles processing route and api calls triggered by milestones-table actions.
 */
class ProjectMilestonesCtrl extends ProjectBlockCtrl {

  constructor($scope, $state, $log, ProjectService, MilestonesService, ErrorService, project, template, $timeout, ToastrUtil, $injector, payments, claimFeatureEnabled, isMonetaryValueReclaimsEnabled) {
    super($injector);
    this.claimFeatureEnabled = claimFeatureEnabled;
    this.isMonetaryValueReclaimsEnabled = isMonetaryValueReclaimsEnabled;

    this.$log = $log;
    this.$scope = $scope;
    this.$state = $state;
    this.ProjectService = ProjectService;
    this.MilestonesService = MilestonesService;
    this.$timeout = $timeout;
    this.ToastrUtil = ToastrUtil;
    this.ErrorService = ErrorService;

    this.payments = payments;
    this.template = template;
  }

  $onInit() {
    super.$onInit();
    this.milestonesConfig = _.find(this.template.blocksEnabled, {block: 'Milestones'});
    this.processingRoutes = _.sortBy(this.milestonesConfig.processingRoutes, 'name');
    this.processingRoute = _.find(this.milestonesConfig.processingRoutes, {
      id: this.projectBlock.processingRouteId
    }) || this.milestonesConfig.defaultProcessingRoute;
    this.selectedProcessingRoute = this.processingRoute;

    this.assess = this.project.statusType.toLowerCase() === 'assess';
    this.active = this.project.statusType.toLowerCase() === 'active';
  }


  /**
   * Processing route changed
   */
  processingRouteSelected(selected) {
    this.selectedProcessingRoute = selected;
  }

  /**
   * Confirm processing route selection and update table data
   */
  confirmProcessingRoute() {
    this.loading = true;
    this.$log.debug('confirmProcessingRoute');
    let p = this.ProjectService.updateProjectProcessingRoute(this.project.id, this.blockId, this.selectedProcessingRoute.id)
      .then(resp => {
        this.projectBlock = resp.data;
        this.processingRoute = _.find(this.milestonesConfig.processingRoutes, {
          id: resp.data.processingRouteId
        });
        this.$log.debug(this.processingRoute);
        this.loading = false;
      });

    this.addToRequestsQueue(p);
  }

  /**
   * Remove currently selected processing route
   */
  removeProcessingRoute() {
    if (this.processingRoute) {
      var modal = this.ConfirmationDialog.show({
        message: 'Are you sure you want to change the processing route?\nThis will change your milestone plan and any data input may be lost.',
        approveText: 'YES',
        dismissText: 'CANCEL'
      });

      modal.result
        .then(() => {
          this.processingRoute = null;
        });
    } else {
      this.processingRoute = null;
    }
  }

  back() {
    if (this.readOnly || this.loading) {
      this.returnToOverview();
    } else {
      this.submit();
    }
  }

  save(keepLock) {
    //TODO why de we need to set lockDetails shouldn't we just save this.projectBlock?
    const data = {
      lockDetails: this.projectBlock.lockDetails,
      milestones: this.projectBlock.milestones,
      type: 'ProjectMilestonesBlock'
    };

    let p = this.ProjectService.updateProjectMilestones(this.project.id, this.blockId, data, keepLock);
    return this.addToRequestsQueue(p);
  }

  /**
   * Submit
   */
  submit() {
    //$timeout to fix autosafe=true after saving
    return this.$timeout(() => {
      return this.$q.all(this.requestsQueue).then(results => {
        return this.save(false);
      });
    });
  };


  add(milestone) {
    let p = this.ProjectService.addProjectMilestones(this.project.id, this.blockId, milestone)
      .then(resp => {
        this.projectBlock = resp.data;
        this.ToastrUtil.success('Milestone Added');
        return resp;
      });
    return this.addToRequestsQueue(p);
  }


  delete(milestone) {
    let p = this.ProjectService.deleteProjectMilestone(this.project.id, this.blockId, milestone.id)
      .then(() => {
        this.ToastrUtil.success('Milestone Deleted');
        return true;
      }).catch(this.ErrorService.apiValidationHandler());
    return this.addToRequestsQueue(p);
  }

  autoSave() {
    return this.save(true);
  }

  onReclaimMilestoneModalAction($event) {
    let p = this.autoSave().then(() => {
      this.$state.go(this.$state.current.name, this.$stateParams, {reload: true});
    });
    return this.addToRequestsQueue(p);
  }

  onClaimMilestoneModalAction($event) {
    let data = $event.data;
    let milestone = $event.milestone;

    if (data.action === this.MilestonesService.claimActions.claim) {
      return this.MilestonesService.claimMilestone(this.project.id, milestone.id, data).then(() => {
        this.$state.go(this.$state.current.name, this.$stateParams, {reload: true});
      });
    }

    if (data.action === this.MilestonesService.claimActions.cancel) {
      return this.MilestonesService.cancelClaim(this.project.id, milestone.id).then(() => {
        this.$state.go(this.$state.current.name, this.$stateParams, {reload: true});
      });
    }

    if (data.action === this.MilestonesService.claimActions.cancelReclaim) {
      return this.MilestonesService.cancelReclaim(this.project.id, milestone.id).then(() => {
        this.$state.go(this.$state.current.name, this.$stateParams, {reload: true});
      });
    }


    return this.addToRequestsQueue(p);
  }
}

ProjectMilestonesCtrl.$inject = [
  '$scope', '$state', '$log', 'ProjectService', 'MilestonesService', 'ErrorService' , 'project', 'template', '$timeout', 'ToastrUtil', '$injector', 'payments', 'claimFeatureEnabled', 'isMonetaryValueReclaimsEnabled'
];

angular.module('GLA')
  .controller('ProjectMilestonesCtrl', ProjectMilestonesCtrl);
