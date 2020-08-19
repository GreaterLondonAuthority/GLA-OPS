//------------- Angular 1.7 app --------------------------------------------
import '../../../ui/app/scripts/glaModule.js'
// templateCache.js Depends on grunt job creating this file for angular 1.7
// Once all html are part of new angular we won't need it
import '../../../ui/.tmp/scripts/templateCache.js'
import '../../../ui/app/scripts/app.js';
import {Downgrades} from "./downgrades";
import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {UpgradeModule} from '@angular/upgrade/static';
import {TemplateConfigurationModule} from './template-configuration/template-configuration.module';

import {FeatureToggleService} from "./feature-toggle/feature-toggle.service";
import {AuthInterceptor} from "./auth/auth.interceptor";
import {AuthService} from "./auth/auth.service";

import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {SharedModule} from "./shared/shared.module";
import {ReferenceDataService} from "./reference-data/reference-data.service";
import {TeamsDefaultAccessListComponent} from './teams-default-access-list/teams-default-access-list.component';
import {UserService} from "./user/user.service";
import {NavigationService} from "./navigation/navigation.service";

new Downgrades().downgrade();
//--------------------------------------------------------------------------

//------------------------------------------------

@NgModule({
  imports: [
    BrowserModule,
    UpgradeModule,
    AppRoutingModule,
    HttpClientModule,
    SharedModule,
    TemplateConfigurationModule
  ],

  declarations: [
    AppComponent,
    TeamsDefaultAccessListComponent
  ],

  //Components visible to ng1 should be added here
  providers: [
    FeatureToggleService,
    ReferenceDataService,
    UserService,
    AuthService,
    NavigationService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
  ],
  // bootstrap: [AppComponent]
})
export class AppModule {
  constructor(private upgrade: UpgradeModule) {
  }

  ngDoBootstrap() {
    this.upgrade.bootstrap(document.body, ['GLA'], {strictDi: true});
  }
}
