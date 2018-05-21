/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

class ProjectDetailsCtrl extends ProjectBlockCtrl{
  constructor($state, $log, project, template, ProjectService, organisationGroup, $injector, organisationGroups, OrganisationGroupService, boroughs, ProjectDetailsService) {
    super(project, $injector);

    const orgGroup = organisationGroup;
    this.$log = $log;
    this.$state = $state;
    this.ProjectService = ProjectService;
    this.OrganisationGroupService = OrganisationGroupService;
    this.ProjectDetailsService = ProjectDetailsService;

    //-----TODO review lead org once we have multiple consortiums
    this.organisationGroupsIndividualOption = {
      id: this.projectBlock.orgSelected ? null : -1,
      name: this.project.organisation.name
    };
    this.organisationGroups = organisationGroups.slice();
    //We want to preselect 'Individual' option only if it was selected before. Not by default on project creation
    this.organisationGroups.push(this.organisationGroupsIndividualOption);

    //-----

    this.updateOrganisationDetails(orgGroup);

    this.projectId = $state.params.projectId;
    this.organisations = (organisationGroup || {}).organisations;

    this.template = template;

    //Editable fields
    this.fields =  this.ProjectDetailsService.fields(this.template.detailsConfig);



    this.siteStatuses = [
      'Operational',
      'Vacant (with buildings)',
      'Vacant (no buildings)'
    ];

    this.$log.log('borough: ', boroughs);

    this.boroughs = boroughs;

    let selectedBorough = _.find(boroughs, {boroughName: this.projectBlock.borough}) || {};
    this.wards = selectedBorough.wards || [];
  }


  /**
   * Back
   */
  back(form) {
    if (this.readOnly || this.loading) {
      this.returnToOverview();
    } else {
      this.submit(form, false);
    }
  };

  /**
   * Submit
   */
  submit(form, validate) {
    if (validate && !form.$valid) return;

    if (this.existingProjectWithLegacyCode) {
      this.projectBlock.legacyProjectCode = null;
    }

    this.ProjectService.updateProject(this.projectBlock, this.projectId)
      .then(resp => {
        this.returnToOverview(this.blockId);
      })
      .catch(err => {
        this.$log.error(err);
        if (!validate) {
          this.returnToOverview();
        }
      });
  }

  changeBiddingArrangement() {
    this.projectBlock.orgSelected = true;
    this.organisationGroupsIndividualOption.id = null;
    if (this.projectBlock.organisationGroupId === -1) {
      this.projectBlock.organisationGroupId = null;

    }
    this.projectBlock.developingOrganisationId = null;
    this.projectBlock.developmentLiabilityOrganisationId = null;
    this.projectBlock.postCompletionLiabilityOrganisationId = null;
    if(this.projectBlock.organisationGroupId) {
      this.$rootScope.showGlobalLoadingMask = true;
      this.OrganisationGroupService.findById(this.projectBlock.organisationGroupId)
        .then(orgGroup => {
          this.$rootScope.showGlobalLoadingMask = false;
          this.organisations = orgGroup.data.organisations;
          this.updateOrganisationDetails(orgGroup.data);
        });
    }else{
      this.organisations = [];
      this.updateOrganisationDetails(null);
    }
  }

  updateOrganisationDetails(orgGroup){
    this.orgType = (orgGroup && orgGroup.type) ? orgGroup.type : 'Individual';
    this.orgName = orgGroup ? orgGroup.name : this.project.organisation.name;
    this.leadOrg = orgGroup ? _.find(orgGroup.organisations, {id: orgGroup.leadOrganisationId}) : null;
  }

  onBoroughChange(borough) {
    this.projectBlock.wardId = null;
    this.wards = borough.wards;
  }

  checkLegacyProjectCode() {
    this.ProjectService.lookupProjectIdByLegacyProjectCode(this.projectBlock.legacyProjectCode).then((resp)=>{
      this.existingProjectWithLegacyCode = resp.data;
      console.log('existingProjectWithLegacyCode', this.existingProjectWithLegacyCode);
    });
  }

}

ProjectDetailsCtrl.$inject = ['$state', '$log', 'project', 'template', 'ProjectService', 'organisationGroup', '$injector', 'organisationGroups', 'OrganisationGroupService', 'boroughs', 'ProjectDetailsService'];

angular.module('GLA')
  .controller('ProjectDetailsCtrl', ProjectDetailsCtrl);
