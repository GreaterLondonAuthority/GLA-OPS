/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

//import NewOrganisationCtrl from "../../organisation/organisationForm/newOrganisationCtrl";

// function NewProjectCtrl($scope, $state, $log, UserService, ProgrammeService, ProjectService, OrganisationService) {
class NewProjectCtrl{
  constructor($scope, $state, $log, UserService, ProgrammeService, ProjectService, OrganisationService){
    this.ProjectService = ProjectService;
    this.user = UserService.currentUser();

    this.$log = $log;
    this.$state = $state;
    this.organisations = this.user.organisations || [];
    this.templates = [];
    this.title = null;
    this.selectedOrganisation = null;
    this.selectedProgramme = null;
    this.selectedTemplate = null;
    this.consortium = null;
    this.isSaving = false;
    this.showMissingProfileOrgAdmin = false;
    this.showMissingProfileProjectEditor = false;
    this.showPendingProfileProjectEditor = false;
    this.availableOrganisationsForProgramme = [];
    this.availableOrganisationsForProgrammeAndUser = [];

    this.programmeSelected = (programme) => {

      this.showMissingProfileOrgAdmin = false;
      this.showMissingProfileProjectEditor = false;
      this.showPendingProfileProjectEditor = false;

      this.availableOrganisationsForProgramme = _.filter(this.organisations, (org) => {
        return org.managingOrganisationId === programme.managingOrganisationId || org.id === programme.managingOrganisationId;
      });

      this.availableOrganisationsForProgrammeAndUser = [];
      _.forIn(this.availableOrganisationsForProgramme, (value, key) => {
        if(UserService.hasPermission('proj.create', value.id)) {
          this.availableOrganisationsForProgrammeAndUser.push(value);
        }
      });

      this.selectedOrganisation = this.availableOrganisationsForProgrammeAndUser.length === 1 ? this.availableOrganisationsForProgrammeAndUser[0] : null;

      if(!this.availableOrganisationsForProgrammeAndUser.length){
        this.pendingProfile = _.find(this.user.roles, {managingOrganisationId: programme.managingOrganisationId, orgStatus: 'Pending'});

        if(this.pendingProfile) {
          this.showPendingProfileProjectEditor = true;
        }
      }

      this.templates = _.filter(_.orderBy(programme.templates, 'name'), {status: 'Active'});
    };

    // if (this.$state.params.programmes) {
    //   this.programmes = this.$state.params.programmes;
    //
    // } else {
    //   ProgrammeService.getEnabledProgrammes()
    //     .then(resp => {
    //       this.programmes = _.orderBy(resp.data, 'name');
    //     });
    // }
  }

  canProjectBeAssignedToTemplate(){
    this.ProjectService.canProjectBeAssignedToTemplate(this.selectedTemplate.id, this.selectedOrganisation.id).then(rsp => {
      this.showMaxProjectsForTemplateError = rsp.data === false;
    });
  }

  /**
   * Submit
   */
  submit(){
    if(this.isSaving){
      return;
    }
    this.isSaving = true;
    let data = {
      title: this.title,
      programme: {
        id: this.selectedProgramme.id
      },
      template: {
        id: this.selectedTemplate.id
      },

    };
    if (this.selectedOrganisation) {
      data.organisation = {
        id: this.selectedOrganisation.id
      };
    }

    this.ProjectService.createProject(data)
      .then(resp => {
        this.isSaving = false;
        if (!resp) return;
        const createdProjectId = resp.data;
        this.$state.go('project-overview', {
          'projectId': createdProjectId
        });
      })
      .catch(
        (err) => {
          this.isSaving = false;
          this.$log.error(err);
        }
      );
  };
}

// angular.module('GLA')
//   .controller('NewProjectCtrl', NewProjectCtrl);

NewProjectCtrl.$inject = ['$scope', '$state', '$log', 'UserService', 'ProgrammeService', 'ProjectService', 'OrganisationService'];

export default NewProjectCtrl;

angular.module('GLA')
  .component('newProjectPage', {
    templateUrl: 'scripts/pages/projects/new/newProject.html',
    controller: NewProjectCtrl,
    bindings: {
      programmes: '<'
    }
  });
