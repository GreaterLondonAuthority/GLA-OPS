/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

const DEFAULT_NUMBER_CONTRACTS_SHOWN = 3;
class ContractsList {
  constructor($rootScope, $scope, OrganisationService, UserService, NavigationService) {
    this.OrganisationService = OrganisationService;
    this.UserService = UserService;
    this.NavigationService = NavigationService
  }

  $onInit(){
    this.showAll = false;
    this.showHowMany = DEFAULT_NUMBER_CONTRACTS_SHOWN;
    this.contractStatusMap = {
      'NotRequired': 'Not Required',
      'Signed': 'Signed',
      'Blank': 'Pending',
      'PendingOffer': 'Pending Offer',
      'Offered': 'Offered',
      'Accepted': 'Accepted',
    };
    this.contractStatusConst = {
      BLANK: 'Blank',
      SIGNED: 'Signed',
      NOT_REQUIRED: 'NotRequired',
      PENDING_OFFER: 'PendingOffer'
    };
  }

  getContractTypeTitle(contract){
    return  contract.variation ? '    ' + (contract.variationName ? contract.variationName : '[empty]') :
                               contract.name + (contract.orgGroupType ? (' - ' + contract.orgGroupType) : '')

  }

  performAction(contract, action) {
    if (action.newVariationEntry && !contract.variation) {
      this.OrganisationService.createContractVariation(this.org.id, contract).then((resp) => {
        this.NavigationService.goToUiRouterState('organisation.contract-variation',
         {orgId: this.org.id, orgContractId: resp.data.id}, {reload: true});
      })
    } else if(contract.variation) {
      this.NavigationService.goToUiRouterState('organisation.contract-variation',
        {orgId: this.org.id, orgContractId: contract.id}, {reload: true});
    } else if (action.doViewDetails) {
      this.NavigationService.goToUiRouterState('organisation.contract-details',
        {organisation: this.org, orgContractId: contract.id}, {reload: true});
    } else if (action.nextStatus) {
      this.updateContract(contract, action.nextStatus)
    }
  }

  hasContractVariation(contract){
     return _.find(this.org.contracts, {name: contract.name, variation:true}) ? true : false
  }

  updateContract(contract, status) {
    let contractClone = _.clone(contract);
    if (status === 'Blank') {
      this.OrganisationService.deleteContractStatus(this.org.id, contractClone.id).then(() => {
        this.refreshDetails();
      })
    } else {
      contractClone.status = contractClone.status === status ? 'Blank' : status;
      this.OrganisationService.updateContractStatus(this.org.id, contractClone.id, contractClone).then(()=>{
        this.refreshDetails();
      });
    }
  }

  showMoreLessContract() {
    this.showAll = !this.showAll;
    this.showHowMany = this.showAll ? this.org.contracts.length : DEFAULT_NUMBER_CONTRACTS_SHOWN;
  }
}

ContractsList.$inject = ['$rootScope', '$scope', 'OrganisationService', 'UserService', 'NavigationService'];

angular.module('GLA')
  .component('contractsList', {
    bindings: {
      org: '<',
      refreshDetails: '&'
    },
    templateUrl: 'scripts/pages/organisation/contracts/contractsList.html',
    controller: ContractsList
  });
