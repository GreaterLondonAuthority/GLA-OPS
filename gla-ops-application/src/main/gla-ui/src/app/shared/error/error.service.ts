import { Injectable } from '@angular/core';
import {ConfirmationDialogService} from "../confirmation-dialog/confirmation-dialog.service";

@Injectable({
  providedIn: 'root'
})
export class ErrorService {

  constructor(private confirmationDialog: ConfirmationDialogService) { }

  apiValidationHandler(callback?) {
    return function errorHandler(err) {
      //If its not 400 it already caught by interceptor
      if (err && err.status === 400) {
        let errInfo = err.data || err.error || {};
        let modal = this.confirmationDialog.warn(errInfo.description || 'Failed validation on backend');
        if(callback){
          callback(err, modal);
        }
      }
    }.bind(this)
  }
}
