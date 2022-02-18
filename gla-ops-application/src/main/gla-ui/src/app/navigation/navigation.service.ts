import { Injectable } from '@angular/core';
import {UserService} from "../user/user.service";
import {LoadingMaskService} from "../shared/loading-mask/loading-mask.service";
//TODO This is a temporary workaround until we use ng1 ui-router with ng10 components
@Injectable({
  providedIn: 'root'
})
export class NavigationService {

  private listeners: { (uiRouterStateName: string, uiRouterParams: any, options: any): void; }[] = [];
  private $state: any

  constructor(private userService: UserService, private loadingMaskService: LoadingMaskService) {
    userService.onLogout(event => {
      if(this.$state && this.$state.current.name !== 'home' && this.$state.current.name !== 'password-expired' ) {
        console.log('sending home')
        this.$state.go('home', {reasonError: event.message});
      }else{
        loadingMaskService.showLoadingMask(false);
      }
    })
  }

  setCurrentState($state){
    this.$state = $state;
  }

  getCurrentStateParams(){
    return (this.$state || {}).params;
  }

  addStateChangeListener(listener: { (uiRouterStateName: string, uiRouterParams: any, options: any): void }) {
    this.listeners.push(listener);
  }

  goToUiRouterState(uiRouterState: string, uiRouterParams?: any, options?: any) {
    this.listeners.forEach(listener => {
      listener(uiRouterState, uiRouterParams, options)
    })
  }

  goToCurrentUiRouterState(uiRouterParams: any, options: any) {
    this.listeners.forEach(listener => {
      listener(this.$state.current, uiRouterParams, options)
    })
  }

  reloadCurrentState() {
    this.listeners.forEach(listener => {
      listener(this.$state.current, this.$state.params, {reload: true})
    })
  }

  uiRouterStateIncludes(state: string){
    if(this.$state){
      return this.$state.includes(state);
    }
    return false;
  }
}
