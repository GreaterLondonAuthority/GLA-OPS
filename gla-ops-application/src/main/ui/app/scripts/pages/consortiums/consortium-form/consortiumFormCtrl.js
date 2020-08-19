/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

class ConsortiumFormCtrl {
  constructor($log, OrganisationGroupService, ConfirmationDialog) {
    this.OrganisationGroupService = OrganisationGroupService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.$log = $log;
  }

  $onInit() {
    this.isEdit = !!this.data.id;
    this.isOrgInProject = (this.organisationsInProjects || []).reduce((res, org) => {
      res[org.id] = true;
      return res;
    }, {});
    _.remove(this.data.organisations, {id: this.data.leadOrganisationId});
    this.lastRequestId = 0;
  }

  lookupOrganisationCode(orgCode) {
    var ctrl = this;
    if (orgCode.length > 2) {
      this.loading = true;
      let requestId = ++this.lastRequestId;

      this.OrganisationGroupService.lookupOrgNameByCodeForConsortium(orgCode)
        .then(resp => {
          if (orgCode === ctrl.orgCode) {
            ctrl.org = (resp == undefined || resp.status !== 200) ? null : resp.data
          }
        })
        .catch(err => {
          if (orgCode === ctrl.orgCode) {
            ctrl.org = null;
            this.$log.log('err', err);
            if(err && err.data){
              ctrl.orgCodeError = err.data.description
            }
          }
        })
        .finally(() => {
          if (requestId == this.lastRequestId) {
            this.loading = false;
          }
        });
    } else {
      ctrl.org = null;
    }
  }

  addOrganisation() {
    if (!this.data.organisations) {
      this.data.organisations = []
    }
    this.data.organisations.push(this.org);
    this.org = null;
    this.orgCode = null;
  }

  confirmOrganisationRemoval(org) {
    var modal = this.ConfirmationDialog.delete('Are you sure you want to delete this organisation?');
    modal.result.then(() => this.removeOrganisation(org.id));
  }

  removeOrganisation(id) {
    _.remove(this.data.organisations, {id: id});
  }

  isOrganisationAlreadyAdded(org) {
    if (org) {
      return _.find(this.data.organisations, {
        id: org.id
      });
    } else {
      return false;
    }
  }

  isOrganisationInAnyProject(orgId){
    return !!this.isOrgInProject[orgId];
  }

  save() {
    this.errorMsg;
    let data = angular.copy(this.data);
    data.organisations = data.organisations.map(org => {
      return {
        id: org.id
      }
    });
    data.organisations.push({id: data.leadOrganisationId});
    let promise = this.onSave({$event: data});
    if (promise) {
      promise.catch(err => {
        this.errorMsg = err.data.description;
      });
    }
  }
}

ConsortiumFormCtrl.$inject = ['$log', 'OrganisationGroupService', 'ConfirmationDialog'];

export default ConsortiumFormCtrl
