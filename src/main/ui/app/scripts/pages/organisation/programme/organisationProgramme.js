/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const _ = require('lodash');
const TYPE_INITIAL = 'Initial';
const TYPE_ADDITIONAL = 'Additional';

class OrganisationProgrammeCtrl {
  constructor($scope, $state, ToastrUtil, OrganisationService, UserService, ConfirmationDialog, CreateDelegatedModal, $q) {
    this.$q = $q;
    this.editable = UserService.hasPermission('org.edit.budget');
    this.readOnly = true;

    this.$scope = $scope;
    this.$state = $state;
    this.ToastrUtil = ToastrUtil;
    this.OrganisationService = OrganisationService;
    this.ConfirmationDialog = ConfirmationDialog;

    this.organisationId = $state.params.organisationId;
    this.programmeId = $state.params.programmeId;

    this.CreateDelegatedModal = CreateDelegatedModal;

    this.refreshPage();


    //TODO cleanup unused properties
    this.requestsQueue = [];
    this.grantTypes = [];
    this.today = moment().format('YYYY-MM-DD');
    // this.initGrantTypes(['Grant', 'RCGF', 'DPF']);
  }

  initGrantTypes(grantTypes) {
    let columnsCount = grantTypes.length === 1 ? 1 : grantTypes.length + 1;
    this.grantTypesCol = columnsCount == 1 ? 'col-xs-4' : `col-xs-${12 / columnsCount}`;
    this.showTotals = grantTypes.length > 1;
    this.grantTypes = grantTypes;
    this.initialBudgets = [];
    this.initialStrategicBudgets = [];
    this.grantTypesDropdown = [];

    //TODO $onInit
    this.grantTypes.forEach(grantType => {
      this.initialBudgets.push(this.grantRecord(grantType, false, TYPE_INITIAL));
      this.initialStrategicBudgets.push(this.grantRecord(grantType, true, TYPE_INITIAL));
    });

    //Init grant types dropdown for modal
    this.grantTypes.forEach(grantType => this.grantTypesDropdown.push(this.grantRecord(grantType, false, TYPE_ADDITIONAL)));
    this.grantTypes.forEach(grantType => this.grantTypesDropdown.push(this.grantRecord(grantType, true, TYPE_ADDITIONAL)));

    console.log('grantTypesDropdown', this.grantTypesDropdown);


    console.log('init', this.initialBudgets);
    console.log('init', this.initialStrategicBudgets);
  }

  isGrantTypeAvailable(type) {
    return this.grantTypes.indexOf(type) !== -1;
  }


  label(grantType, isStrategic, type) {
    let label = grantType;
    if (isStrategic) {
      if (grantType === 'Grant') {
        label = label.toLowerCase();
      }
      label = `Strategic ${label}`
    }
    if (type === TYPE_INITIAL) {
      label = `${label} £`;
    }
    return label;
  }

  mergeBudgets(defaultBudgets, apiData) {
    let budgetEntries = apiData.budgetEntries;
    let totals = apiData.totals || {};

    defaultBudgets.forEach(grant => {
      let totalKey = grant.strategic ? 'strategic' : 'nonStrategic';
      totalKey = `${totalKey}${grant.grantType}Total`;

      let apiRecordMatch = _.find(budgetEntries, {
        grantType: grant.grantType,
        type: grant.type,
        strategic: grant.strategic,
      });
      grant.total = totals[totalKey];
      _.merge(grant, apiRecordMatch);
    })
  }

  //Move to service
  grantRecord(grantType, isStrategic, type) {
    let label = this.label(grantType, isStrategic, type);
    return {
      label: label,
      uniqueId: label.replace(/ /g, '-').toLowerCase(),
      grantType: grantType,
      type: type,
      strategic: !!isStrategic,
      organisationId: this.organisationId,
      programmeId: this.programmeId
    };
  }

  refreshPage() {
    if (this.$state.params.organisation) {
      this.organisation = this.$state.params.organisation;
    }
    else {
      this.OrganisationService.getDetails(this.organisationId)
        .then(resp => {
          this.organisation = resp.data;
        });
    }
    this.programme = this.$state.params.programme;

    this.programme = this.$scope.programme = this.$state.params.programme;

    this.OrganisationService.getPaymentsAndRequests(this.organisationId, this.programmeId)
      .then((resp) => {
        this.grantSourceData = resp.data.nonStrategicRecord;
        this.strategicRecord = resp.data.strategicRecord;
        this.associatedProjectsRecord = resp.data.associatedProjectsRecord;
      });

    return this.OrganisationService.getOrganisationProgramme(this.organisationId, this.programmeId)
      .then(resp => {
        this.initGrantTypes(resp.data.grantTypes);
        this.mergeBudgets(this.initialBudgets, resp.data);
        this.mergeBudgets(this.initialStrategicBudgets, resp.data);
        this.totals = resp.data.totals;

        this.delegatedApprovalEntries = _.filter(resp.data.budgetEntries, {type: 'Additional'});
        this.showIndicative = resp.data.hasIndicativeTemplate;
        this.data = resp.data;
      });

  }


  back() {
    if (!this.readOnly) {
      this.save();
    }

    this.$state.go('organisation', {
      orgId: this.organisationId
    });
  }

  edit() {
    this.readOnly = false;
  }

  updateBudgetEntry(grant) {
    console.log('grant', grant);
    let p = null;
    if (grant.id) {
      p = this.OrganisationService.updateBudgetEntry(this.organisationId, this.programmeId, grant.id, grant);
    } else {
      p = this.OrganisationService.createBudgetEntry(this.organisationId, this.programmeId, grant)
    }
    p.then(() => this.refreshPage());
    this.requestsQueue.push(p);
  }

  onStrategicPartnershipChange() {
    let data = angular.copy(this.data);
    delete data.grantTypes;
    let p = this.OrganisationService.updateOrganisationProgramme(this.organisationId, this.programmeId, data);
    p.then(() => this.refreshPage());
    this.requestsQueue.push(p);
  }

  save() {
    this.readOnly = true;
    this.$q.all(this.requestsQueue)
      .then(() => this.ToastrUtil.success('Saved successfully'))
      .finally(() => {
        this.refreshPage();
        this.requestsQueue = [];
      });
  }

  cancel() {
    this.readOnly = true;
  }

  createDelegatedApprovalClicked() {
    let grantTypes = this.grantTypesDropdown;
    if (!this.data.strategicPartnership) {
      grantTypes = _.filter(this.grantTypesDropdown, {strategic: this.data.strategicPartnership});
    }
    let modal = this.CreateDelegatedModal.show(null, grantTypes, this.totals);
    modal.result.then((data) => {
      return this.OrganisationService.createBudgetEntry(this.organisationId, this.programmeId, data).then(() => {
        return this.refreshPage();
      });
    });
  }

  deleteEntry(entry) {
    var modal = this.ConfirmationDialog.delete('Are you sure you want to delete the delegated approval amount?');
    modal.result.then(() => {
      this.OrganisationService.deleteBudgetEntry(this.organisationId, this.programmeId, entry.id)
        .then(resp => {
          this.refreshPage();
        });
    });
  }
}

OrganisationProgrammeCtrl.$inject = ['$scope', '$state', 'ToastrUtil', 'OrganisationService', 'UserService', 'ConfirmationDialog', 'CreateDelegatedModal', '$q'];

angular.module('GLA').controller('OrganisationProgrammeCtrl', OrganisationProgrammeCtrl);
