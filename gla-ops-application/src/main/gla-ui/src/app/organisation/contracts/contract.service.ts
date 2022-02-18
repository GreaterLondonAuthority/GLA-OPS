import {Injectable} from '@angular/core';
import {LoadingMaskService} from "../../shared/loading-mask/loading-mask.service";
import {ToastrUtilService} from "../../shared/toastr/toastr-util.service";
import {OrganisationService} from "../organisation.service";
import {ConfirmationDialogService} from "../../shared/confirmation-dialog/confirmation-dialog.service";
import {NavigationService} from "../../navigation/navigation.service";
import {ContractWithdrawModalComponent} from './contract-withdraw-modal/contract-withdraw-modal.component';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Injectable({
  providedIn: 'root'
})
export class ContractService {

  constructor( private organisationService: OrganisationService,
               private loadingMaskService: LoadingMaskService,
               private confirmationDialog: ConfirmationDialogService,
               private ngbModal: NgbModal,
               private navigationService: NavigationService,
               private toastrUtil: ToastrUtilService) {}

  uploadContractFile(file, contract, organisation ) {
    this.loadingMaskService.showLoadingMask(false);
    contract.contractFiles.push(file)
    contract.totalAttachmentsSize += file.fileSize
    this.organisationService.updateContractStatus(organisation.id, contract.id, contract).subscribe((resp) => {
    }, error => {
    })

    this.toastrUtil.success('Contract File Added');
  }

  deleteContractFile(file, contract, organisation ) {
    let index = contract.contractFiles.indexOf(file)
    if (index !== -1) {
      contract.contractFiles.splice(index, 1)
      contract.totalAttachmentsSize -= file.fileSize
    }
    this.organisationService.updateContractStatus(organisation.id, contract.id, contract).subscribe(() => {
    }, error => {
    })

    this.toastrUtil.success('Contract File Removed');
  }

  withdrawContract(contract){
    let modal = this.ngbModal.open(ContractWithdrawModalComponent);
    modal.result.then((userComment) => {
      contract.withdrawReason = userComment;
      contract.status = 'PendingOffer'
      this.updateContractStatus(contract)
    },rsp => {});

  }

  deleteVariation(contract){
    let modal = this.confirmationDialog.show({
      message: 'Are you sure you want to delete this Contract Variation?',
      approveText: 'DELETE',
      dismissText: 'KEEP'
    });

    modal.result.then(() => {
      this.organisationService.deleteVariation(contract.organisationId, contract.id).subscribe(() => {
        this.goToOrganisation(contract.organisationId)
      })
    });
  }

  updateContractStatus(contract){
    this.organisationService.updateContractStatus(contract.organisationId, contract.id, contract).subscribe(() => {
      this.goToOrganisation(contract.organisationId)
    })
  }

  acceptContract(contract){
    this.organisationService.acceptContract(contract.organisationId, contract.id, contract).subscribe(() => {
      this.goToOrganisation(contract.organisationId)
    })
  }

  goToOrganisation(orgId) {
    this.navigationService.goToUiRouterState('organisation.view', {orgId: orgId});
  }



}
