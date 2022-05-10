import {Component, Input, OnInit} from '@angular/core';
import * as _ from "lodash";
import {OrganisationService} from "../../organisation.service";
import {UserService} from "../../../user/user.service";
import {ContractService} from "../contract.service";

@Component({
  selector: 'gla-contract-variation',
  templateUrl: './contract-variation.component.html',
  styleUrls: ['./contract-variation.component.scss']
})

export class ContractVariationComponent implements OnInit {
  @Input() organisation: any;
  @Input() contract: any;
  buttonText: string;
  buttonAction: any;
  canUserAcceptContract: boolean = false
  acceptContractTicked:boolean
  userAuthorisedSignatory:boolean
  contractStatusMap: any = {
    'NotRequired': 'Not Required',
    'Signed': 'Signed',
    'Blank': 'Pending',
    'PendingOffer': 'Pending Offer',
    'Offered': 'Offered',
    'Accepted': 'Accepted'
  };

  constructor(private organisationService: OrganisationService,
              private contractService: ContractService,
              private userService: UserService) { }

  variationEditable: boolean=false;
  user:any;


  ngOnInit(): void {
    this.user = this.userService.currentUser();
    this.variationEditable = this.contract.status == 'PendingOffer'
    this.acceptContractTicked = this.contract.status == 'Accepted'

    if (this.contract.status == 'PendingOffer') {
      this.buttonText = 'MAKE OFFER'
      this.buttonAction = this.offerContractVariation
    } else if (this.contract.status == 'Offered') {
      this.buttonText = 'WITHDRAW OFFER'
      this.buttonAction = this.withdrawContractVariation
    }
    this.canUserAcceptContract = this.allowedToAcceptContract()

    this.userAuthorisedSignatory = this.user.roles
             .filter(r => r.organisationId == this.organisation.id && r.authorisedSignatory).length > 0
  }

  isAuthorisedSignatory(){
    return this.user.isAdmin || this.userAuthorisedSignatory;
  }

  variationReadyForAcceptance(){
    return this.isAuthorisedSignatory()  && this.acceptContractTicked && this.contract.acceptedByJobTitle
  }

  goToOrganisation() {
    this.contractService.updateContractStatus(this.contract)
    this.contractService.goToOrganisation(this.organisation.id);
  }

  getPostUrl() {
    return `/organisations/${this.organisation.id}/contracts/${this.contract.id}/file`
  }

  onFileUpload(e: any) {
    this.contractService.uploadContractFile(e.response, this.contract, this.organisation)
  }

  withdrawContractVariation() {
    this.contractService.withdrawContract(this.contract)
  }

  deleteVariation() {
    this.contractService.deleteVariation(this.contract)
  }

  acceptVariationOffer(){
    this.contract.status ='Accepted'
    this.contractService.acceptContract(this.contract)
  }

  offerContractVariation() {
    this.moveContractToStatus("Offered")
  }

  onFileRemoval(e: any) {
    this.contractService.deleteContractFile(e.response, this.contract, this.organisation)
  }

  contractReadyForOffer(){
    return this.contract.contractFiles.length > 0 && this.contract.variationName && this.contract.variationReason
  }

  buttonDisabled() {
    if (this.contract.status === 'PendingOffer') {
      return !this.contractReadyForOffer()
    } else {
      return false
    }
  }

  deleteButtonShown() {
    return this.contract.status === 'PendingOffer';
  }

  moveContractToStatus(status){
    this.contract.status = status
    this.contractService.updateContractStatus(this.contract)
  }

  allowedToDeleteContract() {
    return this.userService.hasPermission('org.edit.contract');
  }

  allowedToAcceptContract() {
    return this.contract.status === 'Offered' && (this.user.isAdmin || this.hasNextActionAcceptContract());
  }

  hasNextActionAcceptContract(){
    let orgContract = (_.find(this.organisation.contracts, {id: this.contract.id})  || {});
    if (orgContract){
      return orgContract.availableActions.filter(a => a.nextStatus === 'Accepted').length > 0
    }
    return false;
  }

}
