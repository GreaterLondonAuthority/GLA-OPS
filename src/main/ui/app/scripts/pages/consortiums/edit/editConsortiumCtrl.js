/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class EditConsortiumCtrl {
  constructor(UserService, OrganisationGroupService, ToastrUtil, $state, _, organisationGroup, programmes) {
    this.OrganisationGroupService = OrganisationGroupService;
    this.ToastrUtil = ToastrUtil;
    this.UserService = UserService;
    this.$state = $state;
    this.programmes = programmes;
    this.organisationGroup = organisationGroup;
  }

  $onInit() {
    this.loading = true;
    this.data = this.organisationGroup;
    this.title = this.organisationGroup.name;
    this.readOnly = true;
    this.editable = (this.UserService.hasPermission(`cons.edit.${this.organisationGroup.leadOrganisationId}`) ||
      this.UserService.hasPermission(`cons.edit.*`));
    if (this.editable) {
      this.OrganisationGroupService.organisationsInProjects(this.organisationGroup.id).then(resp => {
        this.organisationsInProjects = resp.data;
        this.loading = false;
      });
    }
    else {
      this.loading = false;
    }
    this.leadOrganisations = this.getLeadOrganisations();
    this.lastRequestId = 0;
  }

  submit(data) {
    return this.OrganisationGroupService.updateOrganisationGroup(data)
      .then(response => {
        this.$state.go('consortiums');
        this.ToastrUtil.success('Saved successfully');
      })
  }


  /**
   * Gets lead organisations. If org admin, it returns organisations where he can create a consortium.
   * If OPS Admin, just finds a lead organisation since form is shown as disabled
   * @returns {*}
   */
  getLeadOrganisations() {
    if (this.editable) {
      return this.UserService.currentUserOrganisations('cons.edit')
    }
    return [_.find(this.data.organisations, {id: this.data.leadOrganisationId})];
  }

  back() {
    this.$state.go('consortiums');
  }

  edit() {
    this.readOnly = false;
  }
}

EditConsortiumCtrl.$inject = ['UserService', 'OrganisationGroupService', 'ToastrUtil', '$state', '_', 'organisationGroup', 'programmes'];

angular.module('GLA')
  .controller('EditConsortiumCtrl', EditConsortiumCtrl);
