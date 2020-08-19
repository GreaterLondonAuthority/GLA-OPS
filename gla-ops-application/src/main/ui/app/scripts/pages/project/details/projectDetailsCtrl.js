/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import ProjectBlockCtrl from '../ProjectBlockCtrl';

/*
 * 'null' here means 'organisationGroupId' is missing from the details block JSON body returned by the BE, meaning
 * its individual bidding arrangement explicitly selected that should appear in the dropdown
 */
const INDIVIDUAL_BIDDING_ARRANGEMENT_SELECTED = null;

// -1 here is an arbitrary value different to 'null' to show that the dropdown doesnt have a value selected
const BIDDING_ARRANGEMENT_NOT_SELECTED = -1;

class ProjectDetailsCtrl extends ProjectBlockCtrl{
  constructor($state, $log, project, template, ProjectService, organisationGroup, $injector, organisationGroups, OrganisationGroupService, boroughs, ProjectDetailsService) {
    super($injector);
    this.organisationGroup = organisationGroup;
    this.organisationGroups = organisationGroups;
    this.$log = $log;
    this.$state = $state;
    this.ProjectService = ProjectService;
    this.OrganisationGroupService = OrganisationGroupService;
    this.ProjectDetailsService = ProjectDetailsService;
    this.template = template;
    this.boroughs = boroughs;
  }

  $onInit(){
    super.$onInit();
    // if no organisation group is selected, organisationGroupId value will be 'undefined', which breaks UI form field validation, forcing it null fixes that
    if (!this.projectBlock.organisationGroupId) {
      this.projectBlock.organisationGroupId = null;
    }

    //-----TODO review lead org once we have multiple consortiums
    this.organisationGroupsIndividualOption = {
      id: this.projectBlock.orgSelected ? INDIVIDUAL_BIDDING_ARRANGEMENT_SELECTED : BIDDING_ARRANGEMENT_NOT_SELECTED,
      name: this.project.organisation.name
    };
    this.organisationGroups = this.organisationGroups.slice();
    //We want to preselect 'Individual' option only if it was selected before. Not by default on project creation
    this.organisationGroups.push(this.organisationGroupsIndividualOption);

    //-----

    this.updateOrganisationDetails(this.organisationGroup);

    this.projectId = this.$state.params.projectId;
    this.organisations = (this.organisationGroup || {}).organisations;

    //Editable fields
    this.fields =  this.ProjectDetailsService.fields(this.template.detailsConfig);
    let templateProjectDetails = _.find(this.template.blocksEnabled, {block: 'Details'});
    this.maxBoroughs = templateProjectDetails.maxBoroughs;
    this.boroughOptions = this.getMultiSelectBoroughOptions();

    this.siteStatuses = [
      'Operational',
      'Vacant (with buildings)',
      'Vacant (no buildings)'
    ];

    let selectedBorough = _.find(this.boroughs, {boroughName: this.projectBlock.borough}) || {};
    this.wards = selectedBorough.wards || [];
  }


  /**
   * Back
   */
  back(form) {
    if (this.readOnly || this.loading) {
      this.returnToOverview();
    } else {
      //TODO this shouldn't happen any more with STOP EDITING PATTERN
      this.submit();
    }
  };

  restrictedUpdated() {
    if (!this.projectBlock.addressRestricted) {
      this.projectBlock.address = ''
    }
  }
  /**
   * Submit
   */
  submit() {
    if (this.newProjForm && !this.newProjForm.$valid) return;

    if (this.existingProjectWithLegacyCode) {
      this.projectBlock.legacyProjectCode = null;
    }

    return this.ProjectService.updateProject(this.projectBlock, this.projectId);
  }

  getMultiSelectBoroughOptions() {
    return _
      .chain(this.boroughs)
      .sortBy('displayOrder')
      .map(borough => {
        return {
          label: borough.boroughName,
          model: this.isBoroughSelected(borough.boroughName)
        }
      })
      .value();
  }

  isBoroughSelected(boroughName){
    return (this.projectBlock.borough
         && this.projectBlock.borough.split(this.projectBlock.boroughDelimiter).includes(boroughName)) ? true: false
  }

  changeBiddingArrangement() {
    this.projectBlock.orgSelected = true;
    this.organisationGroupsIndividualOption.id = null;
    if (this.projectBlock.organisationGroupId === BIDDING_ARRANGEMENT_NOT_SELECTED) {
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

  onBoroughChange(boroughName) {
    this.projectBlock.wardId = null;
    this.wards = (_.find(this.boroughs, {boroughName: boroughName}) || {}).wards || [];
  }

  onBoroughOptionChange(check, boroughs) {
    let selectedBoroughs = (boroughs || []).filter(ao => !!ao.model);
    this.projectBlock.borough = selectedBoroughs.length == 0? null:
                                   selectedBoroughs.map(bo => bo.label).join(this.projectBlock.boroughDelimiter)
    this.resetWardDetails(selectedBoroughs)
  }

  resetWardDetails(boroughs) {
    if (boroughs.length == 1) {
      this.onBoroughChange(boroughs[0].label)
    } else {
      this.projectBlock.wardId = null
      this.wards = null
    }

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
