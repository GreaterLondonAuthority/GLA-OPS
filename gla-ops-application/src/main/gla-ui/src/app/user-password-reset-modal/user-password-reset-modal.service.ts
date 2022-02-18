import { Injectable } from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {merge} from 'lodash-es'
import { UserPasswordResetModalComponent } from './user-password-reset-modal.component';

@Injectable({
  providedIn: 'root'
})
export class UserPasswordResetModalService {

  constructor(private modalService: NgbModal) { }

  show(config) {
    const defaultConfig = {
      title: 'Reset User Password',
      confirmText: 'CONFIRM',
      cancelText: 'CANCEL',
      labels: [
        'Weak',
        'Fair',
        'Good',
        'Strong',
        'Very Strong'
      ]
    };

    const modal = this.modalService.open(UserPasswordResetModalComponent);
    modal.componentInstance.config = merge(defaultConfig, config);
    return modal;
  }
}
