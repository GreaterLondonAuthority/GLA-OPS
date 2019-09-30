/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

import './consortium-form/consortiumForm.js'

class ConsortiumsCtrl {
  constructor(OrganisationGroupService, $stateParams) {
    this.OrganisationGroupService = OrganisationGroupService;
    this.$stateParams = $stateParams;

  }

  $onInit(){
    this.createdConsortiumId = this.$stateParams.createdConsortiumId;
    this.sortedBy = 'name';
    this.sortReversed = false;
    this.groups = [];
    this.isLoading = true;

    this.OrganisationGroupService.findAll()
      .then(resp => {
        this.groups = resp.data;

        // filter lead organisation and sort by name asc
        this.groups = _
          .chain(resp.data)
          .each(group => {
            group.leadOrganisation = _.find(group.organisations, {
              id: group.leadOrganisationId
            });
          })
          .orderBy('name', 'asc')
          .value();

        this.isLoading = false;
      });
  }

  /**
   * Sort table by
   */
  sortBy(id) {
    this.sortReversed = (this.sortedBy === id);
    this.sortedBy = id;

    if (id === 'programme') {
      this.groups = _.orderBy(this.groups, group => {
        return group.programme.name;
      }, `${this.sortReversed ? 'desc' : 'asc'}`);
    }
    if (id === 'lead') {
      this.groups = _.orderBy(this.groups, group => {
        return group.leadOrganisation.name;
      }, `${this.sortReversed ? 'desc' : 'asc'}`);
    } else {
      this.groups = _.orderBy(this.groups, id, `${this.sortReversed ? 'desc' : 'asc'}`);
    }
  }
}

ConsortiumsCtrl.$inject = ['OrganisationGroupService', '$stateParams'];

angular.module('GLA')
  .controller('ConsortiumsCtrl', ConsortiumsCtrl);
