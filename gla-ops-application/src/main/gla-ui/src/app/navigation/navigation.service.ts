import { Injectable } from '@angular/core';
import {AuthService} from "../auth/auth.service";
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs";
import {tap} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class NavigationService {

  listeners: { (uiRouterStateName: string, uiRouterParams: any): void; }[] = [];

  constructor() {
  }

  addStateChangeListener(listener: { (uiRouterStateName: string, uiRouterParams: any): void }) {
    this.listeners.push(listener);
  }

  goToUiRouterState(uiRouterState: string, uiRouterParams: any) {
    this.listeners.forEach(listener => {
      listener(uiRouterState, uiRouterParams)
    })
  }
}
