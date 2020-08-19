import {Injectable} from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ConfirmationDialogComponent} from "./confirmation-dialog.component";
import {merge} from 'lodash-es'

@Injectable({
  providedIn: 'root'
})
export class ConfirmationDialogService {

  constructor(private modalService: NgbModal) {
  }

  show(config) {
    const defaultConfig = {
      title: false,
      approveText: 'Yes',
      dismissText: 'No',
      showApprove: true,
      showDismiss: true,
      message: 'Are you sure?',
      info: false,
      showIcon: true
    };

    const modal = this.modalService.open(ConfirmationDialogComponent);
    modal.componentInstance.config = merge(defaultConfig, config);
    return modal;
  }

  delete(message) {
    let config = {
      message: message || 'Are you sure you want to delete?',
      approveText: 'DELETE',
      dismissText: 'KEEP'
    };

    return this.show(config);
  }

  warn(message) {
    let config = {
      message: message || 'Something went wrong!',
      dismissText: 'CLOSE',
      showApprove: false
    };

    return this.show(config);
  }

  info(message) {
    let config = {
      message: message || 'Something went wrong!',
      dismissText: 'CLOSE',
      showApprove: false,
      info: true
    };

    return this.show(config);
  }
}
