/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

 /**
  * Parse element state
  */
  function parseState(state, template) {
   return _
     .chain(template.detailsConfig)
     .mapKeys((value, key) => {
       return key.replace('Requirement', '');
     })
     .mapValues((value, key) => {
       return value ? (value.toLowerCase() === state.toLowerCase()) : value;
     })
     .value();
   }

class ProjectDetailsChangeReport {
  constructor($rootScope, $scope, ReferenceDataService, OrganisationGroupService, ReportService) {


    // let hasUnapproved = !!this.data.right;
    let left = this.data.left;
    let right = this.data.right;
    let changes = this.data.changes;
    let visibleChangedFields = this.data.visibleChangedFields;

    this.project = this.data.context.project;
    this.template = this.data.context.template || {};

    if(left){
      left.template = this.data.context.template || {};
      left.programme = this.project.left.programme;
    }
    if(right){
      right.template = this.data.context.template || {};
      right.programme = this.project.right.programme;
    }
    this.hiddenState = parseState('hidden', this.template);

    ReferenceDataService.getBoroughs().then((data)=>{
      this.boroughs = data;
      if(left){
        left.boroughObj = ReportService.findSelectedBorough(this.boroughs, left.borough);
        left.ward = ReportService.findSelectedWard(left.boroughObj.wards, left.wardId);
      }
      if(right){
        right.boroughObj = ReportService.findSelectedBorough(this.boroughs,right.borough);
        right.ward = ReportService.findSelectedWard(right.boroughObj.wards, right.wardId);
      }
    });


// left and right are the same
    let organisationGroupId = (left && left.organisationGroupId) || (right && right.organisationGroupId);
    if(organisationGroupId){
      OrganisationGroupService.findById(organisationGroupId).then((data)=>{
        let organisationGroup = data.data;
        this.organisations = organisationGroup.organisations;

        let leadOrg = organisationGroup ? _.find(organisationGroup.organisations, {id: organisationGroup.leadOrganisationId}): null;
        if(left) {
            left.organisationGroup = organisationGroup;
            left.orgName = organisationGroup ? organisationGroup.name : this.project.left.organisation.name;
            left.leadOrg = leadOrg;
        }
        if(right){
          right.organisationGroup = organisationGroup;
          right.orgName = organisationGroup ? organisationGroup.name : this.project.right.organisation.name;
          right.leadOrg = leadOrg;
        }

      });
    } else {
      if(left && left.orgSelected){
        left.organisationGroup = this.project.left.organisation || {};
        left.orgName = left.organisationGroup ? left.organisationGroup.name : this.project.left.organisation.name;
      }
      if(right && right.orgSelected){
        right.organisationGroup = this.project.right.organisation || {};
        right.orgName = right.organisationGroup ? right.organisationGroup.name : this.project.right.organisation.name;
      }
    }


    // left.aNumber = right.aNumber = '1234567890.1234';
    // left.aCurrency = right.aCurrency = '1234567890';
    // left.aDate = right.aDate = '2017-07-24T15:24:22.713+01:00';
    // left.aBoolean = right.aBoolean = false

    // go and find block in block sorted list
    this.details = {
      left: left,
      right: right,
      changes: changes
    };

  }
}

ProjectDetailsChangeReport.$inject = ['$rootScope', '$scope', 'ReferenceDataService', 'OrganisationGroupService', 'ReportService'];

angular.module('GLA')
  .component('projectDetailsChangeReport', {
    bindings: {
      data: '<'
    },
    templateUrl: 'scripts/pages/change-report/blocks/projectDetailsChangeReport.html',
    controller: ProjectDetailsChangeReport  });
