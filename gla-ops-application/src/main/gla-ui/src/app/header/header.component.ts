import { Component, OnInit } from '@angular/core';
import {UserService} from "../user/user.service";
import {NavigationService} from "../navigation/navigation.service";
import {ConfigurationService} from "../configuration/configuration.service";
import {MetadataService} from "../metadata/metadata.service";

@Component({
  selector: 'gla-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  numberOfUnreadNotifications: number
  userData: any
  envName: string

  constructor(private userService:UserService,
              private navigationService: NavigationService,
              private configurationService: ConfigurationService,
              private metadataService: MetadataService) { }

  ngOnInit(): void {
    this.userData = this.userService.currentUser();


    this.userService.onLogin( () => {
      this.userData = this.userService.currentUser();
    });

    this.userService.onLogout(() => {
      this.userData = this.userService.currentUser();
    });

    this.configurationService.getConfig().subscribe(config => {
      this.envName = config['env-name'];
    });

    this.metadataService.onMetadataChange((metadata) => {
      this.numberOfUnreadNotifications = metadata.numberOfUnreadNotifications;
    });
  }

  logout() {
    console.log('logout')
    this.userService.logout().subscribe();
  }

  goTo(uiStateName: string, uiRouterParams?: any) {
    this.navigationService.goToUiRouterState(uiStateName, uiRouterParams);
  }
}

