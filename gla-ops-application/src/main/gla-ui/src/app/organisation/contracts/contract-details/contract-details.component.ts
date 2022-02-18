import {Component, Input, OnInit} from '@angular/core';
import {OrganisationService} from '../../organisation.service';
import * as _ from "lodash";
import {UserService} from "../../../user/user.service";
import {ContractService} from "../contract.service";

@Component({
  selector: 'gla-contract-details',
  templateUrl: './contract-details.component.html',
  styleUrls: ['./contract-details.component.scss']
})
export class ContractDetailsComponent implements OnInit {
  @Input() organisation: any;
  @Input() contract: any;
  contractStatusMap: any = {
    'NotRequired': 'Not Required',
    'Signed': 'Signed',
    'Blank': 'Pending',
    'PendingOffer': 'Pending Offer',
    'Offered': 'Offered',
    'Accepted': 'Accepted'
  };

  buttonText: string
  nextStatus: string
  attachmentReadOnly: boolean=true
  canUserAcceptContract:boolean=false
  user:any
  acceptContractTicked:boolean
  userAuthorisedSignatory:boolean

  constructor( private organisationService: OrganisationService,
               private contractService: ContractService,
               private userService: UserService) {
  }

  ngOnInit(): void {
    this.user = this.userService.currentUser();
    this.acceptContractTicked = this.contract.status == 'Accepted'

    if (this.contract.status === 'PendingOffer') {
      this.attachmentReadOnly = false
      this.buttonText = 'MAKE OFFER'
      this.nextStatus = 'Offered'
    } else if (this.contract.status === 'Offered') {
      this.buttonText = 'WITHDRAW OFFER'
      this.nextStatus = 'PendingOffer'
    }

    this.userAuthorisedSignatory = this.user.roles
             .filter(r => r.organisationId == this.organisation.id && r.authorisedSignatory).length > 0

    this.canUserAcceptContract = this.allowedToAcceptContract();

  }

  isAuthorisedSignatory(){
    return this.user.isAdmin || this.userAuthorisedSignatory;
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

  getPostUrl() {
    return `/organisations/${this.organisation.id}/contracts/${this.contract.id}/file`
  }

  onFileUploadComplete(e: any) {
    this.contractService.uploadContractFile(e.response, this.contract, this.organisation)
  }

  onFileRemoval(e: any) {
    this.contractService.deleteContractFile(e.response, this.contract, this.organisation)
  }

  contractReadyForAcceptance(){
    return this.isAuthorisedSignatory()  && this.acceptContractTicked && this.contract.acceptedByJobTitle
  }

  goToOrganisation() {
    this.contractService.goToOrganisation(this.organisation.id);
  }

  acceptContractOffer(){
    this.contract.status ='Accepted'
    this.contractService.acceptContract(this.contract)
  }

  changeContractStatus() {
    if (this.buttonText == 'WITHDRAW OFFER'){
      this.contractService.withdrawContract(this.contract)
    }
    else {
      this.contract.status = this.nextStatus
      this.contractService.updateContractStatus(this.contract)
    }
  }

}
