import { Component, OnInit } from '@angular/core';
import {SessionService} from "../session/session.service";
import {NavigationService} from "../navigation/navigation.service";

@Component({
  selector: 'gla-registration-type-page',
  templateUrl: './registration-type-page.component.html',
  styleUrls: ['./registration-type-page.component.scss']
})
export class RegistrationTypePageComponent implements OnInit {

  constructor(private sessionService: SessionService,
              private navigationService: NavigationService) { }

  ngOnInit(): void {
  }

  registerNewOrg(){
    this.sessionService.setOrgRegistration(null);
    this.navigationService.goToUiRouterState('organisation.registration-programme');
  }

  goTo(uiRouterState: string) {
    this.navigationService.goToUiRouterState(uiRouterState);
  }
}
