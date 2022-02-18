/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */


class PasteAssessmentTemplateCtrl {
  constructor($state, AssessmentService, UserService, PortableEntityService, ToastrUtil) {
    this.$state = $state;
    this.AssessmentService = AssessmentService;
    this.UserService = UserService;
    this.PortableEntityService = PortableEntityService;
    this.ToastrUtil = ToastrUtil;
    this.managingOrganisations = this.UserService.currentUserOrganisations();
    this.assessmentTemplate = {};
  }

  $onInit() {
  }

  goBack() {
    this.$state.go('assessment-templates');
  }

  onManagingOrganisation(selectedOrg) {
    this.assessmentTemplate.managingOrganisationId = selectedOrg.id;
    this.assessmentTemplate.managingOrganisationName = selectedOrg.name;
  }

  paste() {
    if (!navigator.clipboard) {
      alert('Clipboard access is disabled');
      return;
    }
    navigator.clipboard.readText().then(json => {
      let jsonObj = JSON.parse(json);
      jsonObj.managingOrganisationId = this.assessmentTemplate.managingOrganisationId;
      jsonObj.managingOrganisationName = this.assessmentTemplate.managingOrganisationName;
      jsonObj.name = this.assessmentTemplate.name;
      jsonObj.status = jsonObj.status === 'Active' ? 'Draft' : jsonObj.status;
      json = JSON.stringify(jsonObj);

      if (this.isValidJson(json)) {
        this.PortableEntityService.saveSanitisedEntity('AssessmentTemplate', json).then(() => {
          this.ToastrUtil.success('Successfully pasted assessment template');
          this.AssessmentService.getAssessmentTemplates().then(rsp => {
            this.assessmentTemplates = rsp.data
            this.$state.go('assessment-templates');
          });
        });
      } else {
        this.ToastrUtil.warning('Invalid JSON pasted');
      }
    }).catch(err => {
      this.ToastrUtil.warning('Failed to get pasted JSON');
    });
  }

  isValidJson(json) {
    try {
      JSON.parse(json);
    } catch (e) {
      return false;
    }
    return true;
  }

}

PasteAssessmentTemplateCtrl.$inject = ['$state', 'AssessmentService', 'UserService', 'PortableEntityService', 'ToastrUtil'];

angular.module('GLA')
  .component('pasteAssessmentTemplate', {
    templateUrl: 'scripts/pages/assessment-templates/pasteAssessmentTemplate.html',
    controller: PasteAssessmentTemplateCtrl
  });
