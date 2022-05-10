import {Injectable} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import {ToastComponent} from "./toast/toast.component";

const DEFAULT_CONFIG = {
  toastComponent: ToastComponent,
  closeButton: false,
  newestOnTop: false,
  progressBar: false,
  positionClass: 'toast-top-center',
  preventDuplicates: true,
  timeOut: 5000,
  extendedTimeOut: 5000
};

@Injectable({
  providedIn: 'root'
})
export class ToastrUtilService {

  constructor(private toastr: ToastrService) {
  }


  success(message, title?) {
    this.toastr.success(message, title, DEFAULT_CONFIG);
  }

  error(message, title?) {

    this.toastr.error(message, title, DEFAULT_CONFIG);
  }

  info(message, title?) {
    this.toastr.info(message, title, DEFAULT_CONFIG);
  }

  warning(message, title?) {
    this.toastr.warning(message, title, DEFAULT_CONFIG);
  }

  clear() {
    this.toastr.clear();
  }
}
