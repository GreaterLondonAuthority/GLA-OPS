/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class NewConsortiumCtrl {
  constructor(UserService, OrganisationGroupService, ToastrUtil, $state, _, programmes) {
    this.OrganisationGroupService = OrganisationGroupService;
    this.ToastrUtil = ToastrUtil;
    this.UserService = UserService;
    this.$state = $state;
    this.programmes = programmes;
  }

  $onInit() {
    this.data = {};
    this.leadOrganisations = this.UserService.currentUserOrganisations();
    if (this.leadOrganisations.length === 1) {
      this.data.leadOrganisationId = this.leadOrganisations[0].id;
    }
    this.lastRequestId = 0;
  }

  submit(data) {
    return this.OrganisationGroupService.createOrganisationGroup(data)
      .then(response => {
        this.$state.go('consortiums', {
          createdConsortiumId: response.data.id
        });
        this.ToastrUtil.success('Created successfully');
      })
  }

  back() {
    this.$state.go('consortiums');
  }
}

NewConsortiumCtrl.$inject = ['UserService', 'OrganisationGroupService', 'ToastrUtil', '$state', '_', 'programmes'];

angular.module('GLA')
  .controller('NewConsortiumCtrl', NewConsortiumCtrl);
