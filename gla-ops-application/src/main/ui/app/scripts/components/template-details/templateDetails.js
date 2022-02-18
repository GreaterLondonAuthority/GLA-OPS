/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const gla = angular.module('GLA');

class TemplateDetails {

  constructor(TemplateService) {
    this.TemplateService = TemplateService
  }

  $onInit() {
    this.stateModels = [
      {
        id: 'AutoApproval',
        label: 'Auto Approval'
      },
      {
        id: 'ChangeControlled',
        label: 'Change Controlled'
      },
      {
        id: 'MultiAssessment',
        label: 'Multi Assessment'
      }
    ]
    this.isEditingTemplateType = false
  }

  hasErrors(){
    return !!(this.template.numberOfProjectAllowedPerOrg && this.template.numberOfProjectAllowedPerOrg < 1);
  }

  editTemplateType() {
    if (this.isEditingTemplateType == false) {
      this.isEditingTemplateType = true
    } else {
      this.isEditingTemplateType = false
      this.performUpdate()
    }
  }

  getReadOnlyText() {
    return this.template.programmeAllocation ? 'Project Allocation' : 'Project'
  }

}

TemplateDetails.$inject = ['TemplateService']

gla.component('templateDetails', {
  templateUrl: 'scripts/components/template-details/templateDetails.html',
  controller: TemplateDetails,
  bindings: {
    template: '<',
    readOnly: '<',
    templateId: '<',
    editable: '<',
    hasErrors: '&',
    performUpdate: '&'
  },
});

