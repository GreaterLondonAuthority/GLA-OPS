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
const DEFAULT_NUMBER_BUDGET_ENTRIES_SHOWN = 3;

class OrganisationProgrammeCtrl {
  constructor($scope, $state, ToastrUtil, OrganisationService, UserService, ConfirmationDialog, CreateDelegatedModal, $q) {
    this.$scope = $scope;
    this.$state = $state;
    this.ToastrUtil = ToastrUtil;
    this.OrganisationService = OrganisationService;
    this.UserService = UserService;
    this.ConfirmationDialog = ConfirmationDialog;
    this.CreateDelegatedModal = CreateDelegatedModal;
    this.$q = $q;
  }

  $onInit(){
    // this.editable = this.UserService.hasPermission('org.edit.budget');
    this.readOnly = true;

    this.showAll = false;
    this.showHowMany = DEFAULT_NUMBER_BUDGET_ENTRIES_SHOWN;

    this.organisationId = this.$state.params.organisationId;
    this.programmeId = this.$state.params.programmeId;

    this.showExpandAll = true;

    this.refreshPage();

    //TODO cleanup unused properties
    this.requestsQueue = [];
    this.grantTypes = [];
    this.today = moment().format('YYYY-MM-DD');
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

  refreshOrganisation(organisation) {
    this.organisation = organisation;
    this.editable = this.UserService.hasPermission('org.edit.budget', this.organisation.id) || this.UserService.hasPermission('org.edit.budget', this.organisation.managingOrganisationId);
  }

  refreshPage() {
    if (this.$state.params.organisation) {
      this.refreshOrganisation(this.$state.params.organisation);
    }
    else {
      this.OrganisationService.getDetails(this.organisationId)
        .then(resp => {
          this.refreshOrganisation(resp.data);
        });
    }
    this.programme = this.$state.params.programme;

    this.programme = this.$scope.programme = this.$state.params.programme;



    return this.OrganisationService.getOrganisationProgramme(this.organisationId, this.programmeId)
      .then(resp => {
        this.initGrantTypes(resp.data.grantTypes);
        this.mergeBudgets(this.initialBudgets, resp.data);
        this.mergeBudgets(this.initialStrategicBudgets, resp.data);
        this.totals = resp.data.totals;

        this.delegatedApprovalEntries = _.filter(resp.data.budgetEntries, {type: 'Additional'});
        this.collapseComments(this.showExpandAll);
        this.showIndicative = resp.data.hasIndicativeTemplate;


        // this.hasStategicTemplate = !!_.find(resp.data.programme.templates, {strategicTemplate:true});

        this.data = resp.data;


        this.OrganisationService.getPaymentsAndRequests(this.organisationId, this.programmeId)
          .then((resp) => {
            this.processPaymentsAndRequests(resp.data);
          });
      });

  }

  processPaymentsAndRequests(data) {
    this.grantSourceData = data.nonStrategicRecord;
    this.strategicRecord = data.strategicRecord;
    this.associatedProjectsRecord = data.associatedProjectsRecord;
    this.strategicPartnershipUnitSummary = data.strategicPartnershipUnitSummary;

    if(this.data.strategicPartnership){

      // Process this only for strategic programmes
      let allTenureTypes = [];
      _.forEach(this.data.programme.templates, t => {
        allTenureTypes = _.concat(allTenureTypes, t.tenureTypes);
      });
      _.forEach(allTenureTypes, tenure => {
        let list = this.strategicPartnershipUnitSummary.associatedRecords;
        let record = _.find(list, {tenureTypeExtId: tenure.externalId});
        if(!record){
          this.strategicPartnershipUnitSummary.associatedRecords.push({
            tenureTypeExtId: tenure.externalId,
            tenureTypeName: tenure.name,
            orgId: this.organisationId * 1,
            programmeId: this.programmeId * 1,
            tenureType: tenure.externalId,
            unitsPlanned: null
          })
        }
      });
      this.strategicPartnershipUnitSummary.associatedRecords = _.sortBy(
        this.strategicPartnershipUnitSummary.associatedRecords,
        'tenureTypeName'
      )
    }
  }

  updateUnitsPlaned(record) {

    let p;
    if(_.isNumber(record.unitsPlanned)){
      p = this.OrganisationService.updatePlannedUnits(this.organisationId, record.programmeId, record.tenureTypeExtId, record.unitsPlanned);
    } else {
      p = this.OrganisationService.deletePlannedUnits(this.organisationId, record.programmeId, record.tenureTypeExtId);
    }

    p.then((resp) => {
      this.strategicPartnershipUnitSummary.unitsPlannedTotal = resp.data.strategicPartnershipUnitSummary.unitsPlannedTotal;
      // this.processPaymentsAndRequests(resp.data);

    });
    this.requestsQueue.push(p);

  }

  back() {
    if (!this.readOnly) {
      this.save();
    }

    this.$state.go('organisation.view', {
      orgId: this.organisationId
    });
  }

  edit() {
    this.readOnly = false;
  }

  updateBudgetEntry(grant) {
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

  openAdjustBudgetsModal(entry) {
    let grantTypes = this.grantTypesDropdown;
    if (!this.data.strategicPartnership) {
      grantTypes = _.filter(this.grantTypesDropdown, {strategic: this.data.strategicPartnership});
    }
    let modal = this.CreateDelegatedModal.show(entry, grantTypes, this.totals);
    modal.result.then((data) => {
      this.updateBudgetEntry(data);
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

  showMoreLessBudgetEntries() {
    this.showAll = !this.showAll;
    this.showHowMany = this.showAll ? this.delegatedApprovalEntries.length : DEFAULT_NUMBER_BUDGET_ENTRIES_SHOWN;
  }

  getGrantSourceTotalsSubheader() {
    if (this.showApprovedOnlyCheckbox) {
      return 'This table shows the approved grant source totals of all the projects in this programme.';
    }
    return 'The table shows the grant source totals of all the projects in this programme that are active, have pending changes or are under assessment.';
  }

  onCollapseChange(collapsed){
    console.log('collapsed', collapsed);
    console.log('onCollapseChange', JSON.stringify(this.delegatedApprovalEntries, null, 4));
    this.showExpandAll = !(_.some(this.delegatedApprovalEntries, {collapsed: false}));
  }

  collapseComments(collapsed){
    (this.delegatedApprovalEntries || []).forEach(e => e.collapsed = collapsed);
  }

  toggleAdditionalApprovals(){
    this.showExpandAll = !this.showExpandAll;
    this.collapseComments(this.showExpandAll);
  }

}

OrganisationProgrammeCtrl.$inject = ['$scope', '$state', 'ToastrUtil', 'OrganisationService', 'UserService', 'ConfirmationDialog', 'CreateDelegatedModal', '$q'];

angular.module('GLA')
  .component('organisationProgrammePage', {
    templateUrl: 'scripts/pages/organisation/programme/organisationProgramme.html',
    bindings: {
      isStrategicUnitsSummary: '<'
    },
    controller: OrganisationProgrammeCtrl
  });
