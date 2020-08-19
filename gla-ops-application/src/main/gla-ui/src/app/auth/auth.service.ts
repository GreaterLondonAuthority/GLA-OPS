import {Injectable} from '@angular/core';
import {HttpErrorResponse} from "@angular/common/http";
import {downgradeInjectable} from "@angular/upgrade/static";
import {angular} from 'angular';

declare var angular: angular.IAngularStatic;

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  listeners: { (data: HttpErrorResponse): void; }[] = [];

  constructor() {
  }

  addResponseErrorListener(listener: { (data: HttpErrorResponse): void }) {
    this.listeners.push(listener);
  }

  triggerResponseError(err: HttpErrorResponse) {
    this.listeners.forEach(listener => {
      listener(err)
    })
  }
}
