/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const DEFAULT_NUMBER_CONTRACTS_SHOWN = 3;
class ContractsList {
  constructor($rootScope, $scope, OrganisationService, UserService) {
    this.showAll = false;
    this.showHowMany = DEFAULT_NUMBER_CONTRACTS_SHOWN;
    this.canEditContracts = UserService.hasPermission('org.edit.contract');
    this.contractStatusMap = {
      'NotRequired': 'Not Required',
      'Signed': 'Signed',
      'Blank': 'Not Signed'
    };
    this.contractStatusConst = {
      BLANK: 'Blank',
      SIGNED: 'Signed',
      NOT_REQUIRED: 'NotRequired'
    };
    this.OrganisationService = OrganisationService;
  }

  statusCheckboxClicked(contract, status) {
    let contractClone = _.clone(contract)
    contractClone.status = contractClone.status === status ? 'Blank' : status;
    this.OrganisationService.updateContractStatus(this.org.id, contractClone.id, contractClone).then(()=>{
      this.refreshDetails();
    });
  }

  showMoreLessContract() {
    this.showAll = !this.showAll;
    this.showHowMany = this.showAll ? this.org.contracts.length : DEFAULT_NUMBER_CONTRACTS_SHOWN;
  }
}

ContractsList.$inject = ['$rootScope', '$scope', 'OrganisationService', 'UserService'];

angular.module('GLA')
  .component('contractsList', {
    bindings: {
      org: '<',
      refreshDetails: '&'
    },
    templateUrl: 'scripts/pages/organisation/contracts/contractsList.html',
    controller: ContractsList
  });
