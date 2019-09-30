/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const DEFAULT_ROLES = ['ROLE_OPS_ADMIN', 'ROLE_GLA_ORG_ADMIN', 'ROLE_GLA_SPM', 'ROLE_GLA_PM'];

function ProgrammeAssessmentTemplateModal($uibModal) {
  return {
    /**
     * Adds new assessment for project type
     * @param at {assessmentTemplate, allowedRoles}
     * @param availableTemplates Remaining available assessment templates
     * @param glaRoles All roles available
     * @returns {Promise<{assessmentTemplate, allowedRoles}>}
     */
    show: function (at, availableTemplates, glaRoles, readOnly) {

      return $uibModal.open({
        bindToController: true,
        controllerAs: '$ctrl',
        animation: false,
        templateUrl: 'scripts/pages/programme/programme-assessment-template-modal/programmeAssessmentTemplateModal.html',
        size: 'md',
        controller: function () {
          this.at = at || {};
          this.availableTemplates = availableTemplates;
          this.glaRoles = angular.copy(glaRoles);
          this.readOnly = readOnly;

          let selectedRoles = (this.at.allowedRoles || []).length? this.at.allowedRoles : DEFAULT_ROLES;
          (selectedRoles || []).forEach(r => (_.find(this.glaRoles, {name: r}) || {}).selected = true);


          this.onSave = ()=>{
            this.at.allowedRoles = _.filter(this.glaRoles, {selected: true}).map(r => r.name);
            this.$close(this.at);
          };

          this.toggleRoles = (isSelected)=>{
            (this.glaRoles || []).forEach(r => r.selected = isSelected)
          };

          this.isFormValid = ()=>{
            return this.at.assessmentTemplate && _.some(this.glaRoles, {selected: true});
          };
        }
      });
    }
  };
}

ProgrammeAssessmentTemplateModal.$inject = ['$uibModal'];

angular.module('GLA')
  .service('ProgrammeAssessmentTemplateModal', ProgrammeAssessmentTemplateModal);
