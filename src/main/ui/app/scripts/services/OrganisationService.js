/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

OrganisationService.$inject = ['$http', 'config'];

function OrganisationService($http, config) {

  return {

    /**
     * Retrieve list of all organisations
     * @return {Object} promise
     */
    retrieveAll(page, size, sort, userRegStatuses, searchText, entityTypes, orgStatuses, teamStatuses) {
      const params = {
        page: page,
        size: size,
        sort: sort,
        userRegStatuses: userRegStatuses.join(','),
        searchText: searchText,
        entityTypes: entityTypes.join(','),
        orgStatuses: orgStatuses.join(','),
        teams: _.join(_.map(teamStatuses, s=>{return s.organisationId+'|'+(s.teamId||'')}),',')
      };

      Object.keys(params).forEach(key => {
        if (!params[key]) {
          delete params[key];
        }
      });

      return $http({
        url: config.basePath + '/organisations/page',
        method: 'GET',
        params: params
      })
    },

    /**
     * Retrieve the details for a given organisation
     * @param {Number} organisationId
     * @returns {Object} promise
     */
    getDetails(organisationId) {
      return $http({
        url: config.basePath + '/organisations/' + organisationId,
        method: 'GET'
      })
    },

    /**
     * Retrieve the details for a given organisation
     * @param {Number} organisationId
     * @param {Object} data
     * @returns {Object} promise
     */
    updateDetails(organisationId, data) {
      return $http({
        url: `${config.basePath}/organisations/${organisationId}`,
        method: 'PUT',
        data: data,
        serialize: false
      })
    },

    /**
     * Looks up an organisation name by ID or IMS code, return a 404 if not found.
     * @param  {String} orgCode organisation ID or IMS code
     * @return {Object} promise
     */
    lookupOrgNameByCode(orgCode) {
      let code = (orgCode || '').toString();//.toUpperCase();
      return $http({
        url: `${config.basePath}/organisations/${code}/name`,
        method: 'GET',
        transformResponse: (data, headers, status) => {
          headers.ignoreError = 404;
          return data;
        }
      })
    },


    /**
     * Approve a user
     * @param  {String} organisationId
     * @param  {String} userEmail
     * @return {Object} promise
     */
    approveUser(organisationId, userEmail) {
      return $http({
        url: config.basePath + '/organisations/' + organisationId + '/users/' + userEmail + '/approved',
        method: 'PUT',
        data: true,
        serialize: false
      });
    },

    /**
     * Request a user to be linked to an organisation
     * @param  {String} orgCode organisation ID or IMS code
     * @param  {String} userEmail
     * @return {Object} promise
     */
    linkUserToOrganisation(orgCode, userEmail) {
      return $http({
        url: config.basePath + '/organisations/' + orgCode + '/users/' + userEmail,
        method: 'POST',
        serialize: false
      })
    },

    /**
     * Removes user from organisation
     * @param  {String} organisationId
     * @param  {String} username
     * @return {Object} promise
     */
    removeUserFromOrganisation(organisationId, username) {
      return $http({
        url: config.basePath + '/organisations/' + organisationId + '/users/' + username,
        method: 'DELETE'
      })
    },

    /**
     * Create a new organisation
     * @param  {Object} data
     * @return {Object} promise
     */
    createOrganisation(data) {
      return $http({
        url: config.basePath + '/organisations',
        method: 'POST',
        data: data,
        serialize: false
      })
    },

    /**
     * Transforms organisation form data to format api expects
     * @param orgForm Data from form
     * @returns {*}
     */
    formModelToApiData(orgForm) {
      let data = angular.copy(orgForm);
      data.website = data.website && data.website.toLowerCase();
      data.email = data.email && data.email.toLowerCase();
      data.ceoName = `${data.ceoFirstName || ''} ${data.ceoLastName || ''}`;
      data.primaryContactEmail = data.primaryContactEmail && data.primaryContactEmail.toLowerCase();
      data.regulated = data.regulated === 'yes' ? true : false;
      data.managingOrganisation = {id: data.managingOrganisationId};
      data.parentOrganisation = data.parentOrganisationId ? {id: data.parentOrganisationId} : null;
      data.ukprn = this.isLearningProvider(orgForm) ? orgForm.ukprn : null;

      return data;
    },

    checkOrgCode(orgCode) {
      let org = {
        code: orgCode,
        name: '',
        found: false
      };
      return this.lookupOrgNameByCode(orgCode)
        .then(function (response) {
          if (response == undefined || response.status != 200) {
            org.found = false;
          } else {
            org.found = true;
            org.name = response.data;
          }
          return org;
        })
        .catch(function (err) {
          return org;
        });
    },

    /**
     * Parse server details data to fit organisation form model
     * @param apiData
     * @returns {{}}
     */
    apiToFormModelData(apiData) {
      let model = _.omit(apiData, 'users');
      if (apiData.ceoName) {
        const ceoName = apiData.ceoName.split(' ');
        model.ceoFirstName = ceoName[0];
        model.ceoLastName = apiData.ceoName.replace(ceoName[0] + ' ', '');
      }
      model.contactNumber = apiData.contactNumber ? +apiData.contactNumber : null;
      model.primaryContactNumber = apiData.primaryContactNumber ? +apiData.primaryContactNumber : null;
      model.regulated = apiData.regulated === true ? 'yes' : 'no';
      return model;
    },


    /**
     *  Get all available roles user can have in the organisation. If user and organisation
     *  is not specified then only generic roles available to all organisations will be returned
     *
     * @param user optional parameter of the user we want to get roles for.
     * @param org options parameter to restrict roles for some organisation specific
     */
    getAvailableUserRoles(orgId) {
      return $http({
        url: `${config.basePath}/roles?orgId=${orgId}`,
        method: 'GET'
      });
    },

    getGlaRoles() {
      return this.getAvailableUserRoles(8000);
    },

    updateContractStatus(organisationId, contractId, contract) {
      if (contractId) {
        return $http({
          url: `${config.basePath}/organisations/${organisationId}/contracts/${contractId}`,
          method: 'PUT',
          data: contract
        });
      } else {
        return $http({
          url: `${config.basePath}/organisations/${organisationId}/contracts/`,
          method: 'POST',
          data: contract
        });
      }
    },

    getOrganisationProgramme(organisationId, programmeId) {
      return $http({
        url: `${config.basePath}/organisations/${organisationId}/programmes/${programmeId}`,
        method: 'GET'
      });
    },

    updateOrganisationProgramme(organisationId, programmeId, entry) {
      if (entry && entry.approvedBy === '') {
        entry.approvedBy = null;
      }

      return $http({
        url: `${config.basePath}/organisations/${organisationId}/programmes/${programmeId}`,
        method: 'PUT',
        data: entry
      });
    },

    updatePlannedUnits(organisationId, programmeId, tenureExtId, unitsPlanned) {
      return $http({
        url: `${config.basePath}/organisations/${organisationId}/programmes/${programmeId}/tenure/${tenureExtId}/plannedUnits/`,
        method: 'PUT',
        data: unitsPlanned
      });
    },
    deletePlannedUnits(organisationId, programmeId, tenureExtId) {
      return $http({
        url: `${config.basePath}/organisations/${organisationId}/programmes/${programmeId}/tenure/${tenureExtId}/plannedUnits/`,
        method: 'DELETE'
      });
    },

    createBudgetEntry(organisationId, programmeId, entry) {
      return $http({
        url: `${config.basePath}/organisations/${organisationId}/programmes/${programmeId}/budget`,
        method: 'POST',
        data: entry
      });
    },

    updateBudgetEntry(organisationId, programmeId, entryId, entry) {
      return $http({
        url: `${config.basePath}/organisations/${organisationId}/programmes/${programmeId}/budget/${entryId}`,
        method: 'PUT',
        data: entry
      });
    },

    deleteBudgetEntry(organisationId, programmeId, entryId) {
      return $http({
        url: `${config.basePath}/organisations/${organisationId}/programmes/${programmeId}/budget/${entryId}`,
        method: 'DELETE'
      });
    },

    getPaymentsAndRequests(organisationId, programmeId) {
      return $http({
        url: `${config.basePath}/organisations/${organisationId}/programmes/${programmeId}/paymentsAndRequests`,
        method: 'GET'
      });
    },

    organisationTypes() {
      return $http.get(`${config.basePath}/organisations/types`).then(resp => resp.data);
    },

    getImsLabel(org) {
      if (org.entityType === 2) {
        //BOROUGH (2, "Borough"),
        return 'Local authority housing code';
      } else if (org.entityType === 3) {
        //PROVIDER (3, "Registered Provider"),
        return 'Regulator code';
      } else {
        return false;
      }
    },

    isOrganisationNameUnique(orgName, managingOrganisationId) {
      return $http.get(`${config.basePath}/checkOrganisationNameNotUsed?name=${orgName}&managingOrganisationId=${managingOrganisationId}`)
        .then(resp => resp.status === 200)
        .catch(resp => false);
    },

    countOccuranceOfUkprn(ukprn) {
      return $http({
        url: config.basePath + '/organisations/countOccuranceOfUkprn',
        method: 'GET',
        params:{
          ukprn: ukprn
        },
        serialize: false
      });
    },

    managingOrganisations(allowedRegistrationOnly) {
      return $http.get(`${config.basePath}/managingOrganisations`, {
        params:{
          allowedRegistrationOnly: allowedRegistrationOnly || false
        }
      });
    },

    userRegStatuses(selections) {
      selections = selections || [];
      return ['Approved', 'Pending'].map(status => {
        return {
          id: status,
          label: status,
          model: selections.indexOf(status) === -1 ? false : true
        }
      });
    },

    approveOrganisation(orgId){
      let requestBody = {
        status: 'Approved'
      };
      return this.changeStatus(orgId, requestBody);
    },

    rejectOrganisation(orgId, reason, reasonText){
      let requestBody = {
        status: 'Rejected',
        reason: reason,
        details: reasonText,
      };
      return this.changeStatus(orgId, requestBody);
    },

    inactivateOrganisation(orgId, reason, reasonText, duplicateOrgId){
      let requestBody = {
        status: 'Inactive',
        reason: reason,
        details: reasonText,
        duplicateOrgId: duplicateOrgId
      };
      return this.changeStatus(orgId, requestBody);
    },

    changeStatus(orgId, requestBody){
      return $http.put(`${config.basePath}/organisations/${orgId}/status`, requestBody);
    },

    getManagingOrganisationsTeams() {
      return $http.get(`${config.basePath}/managingOrganisationsAndTeams/`);
    },

    getOrganisationTeams(organisationId) {
      return $http.get(`${config.basePath}/organisations/${organisationId}/managedTeams`)
    },

    getOrganisationUsers(organisationId){
      return $http.get(`${config.basePath}/organisations/${organisationId}/users`)
    },

    isLearningProvider(org){
      return (org || {}).entityType === 6
    }
  };
}

angular.module('GLA')
  .service('OrganisationService', OrganisationService);
