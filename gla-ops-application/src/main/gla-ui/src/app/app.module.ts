//------------- Angular 1.7 app --------------------------------------------
import '../../../ui/app/scripts/glaModule.js'
// templateCache.js Depends on grunt job creating this file for angular 1.7
// Once all html are part of new angular we won't need it
import '../../../ui/.tmp/scripts/templateCache.js'
import '../../../ui/app/scripts/app.js';
import {Downgrades} from "./downgrades";
import {BrowserModule} from '@angular/platform-browser';
import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';

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
import {PasswordExpiredComponent} from './password-expired/password-expired.component';
import {OrganisationModule} from "./organisation/organisation.module";
import {NgxPermissionsModule} from "ngx-permissions";
import {NgxMaskModule} from "ngx-mask";
import {NgxWebstorageModule} from "ngx-webstorage";
import {ProjectBlockModule} from "./project-block/project-block.module";
import {TopMenuComponent} from './top-menu/top-menu.component';
import {BannerMessageComponent} from './banner-message/banner-message.component';
import {SessionService} from "./session/session.service";
import {ToastrModule} from 'ngx-toastr';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {TemplateService} from "./template/template.service";
import {HttpParamsInterceptor} from "./http/http-params.interceptor";
import {ProjectService} from "./project/project.service";
import {LoginPageComponent} from './login-page/login-page.component';
import {FooterComponent} from './footer/footer.component';
import {HeaderComponent} from './header/header.component';
import {CookieWarningComponent} from './cookie-warning/cookie-warning.component';
import {TopBarComponent} from './top-bar/top-bar.component';
import {PaymentModule} from "./payment/payment.module";
import {NgIdleKeepaliveModule} from "@ng-idle/keepalive";
import {SessionTimeoutModalComponent} from './session-timeout-modal/session-timeout-modal.component';
import {OutputCategoriesComponent} from './categories/output-categories/output-categories/output-categories.component';
import {RegistrationTypePageComponent} from './registration-type-page/registration-type-page.component';
import {RequestPasswordResetPageComponent} from './request-password-reset-page/request-password-reset-page.component';
import {OutputCategoryModalComponent} from './categories/output-categories/output-category-modal/output-category-modal.component';
import {ProjectModule} from './project/project.module';
import {QuarterlyBudgetTableComponent} from './quarterly-budget-table/quarterly-budget-table.component';
import {ProjectFundingService} from "./funding/project-funding.service";
import {FundingModule} from "./funding/funding.module";
import {SummaryReportModule} from "./summary-report/summary-report.module";
import {ChangeReportModule} from "./change-report/change-report.module";
import {BroadcastsComponent} from './broadcasts/broadcasts.component';
import {BroadcastComponent} from './broadcasts/broadcast/broadcast.component';
import {DeleteModalComponent} from './broadcasts/delete-modal/delete-modal.component';
import {CategoriesComponent} from './categories/categories.component';
import {FinanceCategoriesComponent} from './categories/finance-categories/finance-categories.component';
import {FinanceCategoryModalComponent} from './categories/finance-categories/finance-category-modal/finance-category-modal.component';
import {NgbNavModule} from "@ng-bootstrap/ng-bootstrap";
import {ConfigListItemsComponent} from './categories/config-list-items/config-list-items.component';
import {BlockUsageComponent} from './admin/block-usage/block-usage.component';
import {EmailReportsComponent} from './admin/email-reports/email-reports.component';
import {ConfigListItemModalComponent} from './categories/config-list-items/config-list-item-modal/config-list-item-modal.component';
import {ClaimModalComponent} from './claim-modal/claim-modal.component';
import {ActualsMetadataModalComponent} from './actuals-metadata-modal/actuals-metadata-modal.component';
import {OrganisationRequestAccessModalComponent} from './organisation-request-access-modal/organisation-request-access-modal.component';
import {QuestionService} from './template-configuration/template-block-questions/question.service';
import {ContractDetailsComponent} from './organisation/contracts/contract-details/contract-details.component';
import {FileUploadComponent} from './file-upload/file-upload.component';
import {FileUploadButtonComponent} from './file-upload/file-upload-button/file-upload-button.component';
import {DeleteFileModalComponent} from './file-upload/delete-file-modal/delete-file-modal.component';
import {ContractVariationComponent} from './organisation/contracts/contract-variation/contract-variation.component';
import { QuestionsPageComponent } from './questions/questions-page/questions-page.component';
import { QuestionsComponent } from './questions/questions/questions.component';
import { QuestionsService } from './questions/questions.service';
import { ContractTypesComponent } from './contract-types/contract-types.component';
import { UserPasswordResetModalComponent } from './user-password-reset-modal/user-password-reset-modal.component';
import { UserPasswordResetModalService } from './user-password-reset-modal/user-password-reset-modal.service';


new Downgrades().downgrade();


//------------------------------------------------

@NgModule({
  imports: [
    BrowserModule,
    UpgradeModule,
    AppRoutingModule,
    HttpClientModule,
    NgxMaskModule.forRoot(),
    NgxWebstorageModule.forRoot(),
    NgxPermissionsModule.forRoot(),
    BrowserAnimationsModule,
    ToastrModule.forRoot({
      closeButton: false,
      newestOnTop: false,
      progressBar: false,
      positionClass: 'toast-top-center',
      preventDuplicates: true,
      timeOut: 5000,
      extendedTimeOut: 5000
    }),
    NgIdleKeepaliveModule.forRoot(),
    SharedModule,
    OrganisationModule,
    PaymentModule,
    TemplateConfigurationModule,
    ProjectModule,
    ProjectBlockModule,
    FundingModule,
    SummaryReportModule,
    ChangeReportModule,
    NgbNavModule
  ],

  declarations: [
    AppComponent,
    TeamsDefaultAccessListComponent,
    PasswordExpiredComponent,
    TopMenuComponent,
    BannerMessageComponent,
    LoginPageComponent,
    FooterComponent,
    HeaderComponent,
    CookieWarningComponent,
    TopBarComponent,
    SessionTimeoutModalComponent,
    OutputCategoriesComponent,
    RegistrationTypePageComponent,
    RequestPasswordResetPageComponent,
    OutputCategoriesComponent,
    OutputCategoryModalComponent,
    QuarterlyBudgetTableComponent,
    BroadcastsComponent,
    BroadcastComponent,
    DeleteModalComponent,
    CategoriesComponent,
    FinanceCategoriesComponent,
    ConfigListItemsComponent,
    FinanceCategoryModalComponent,
    BlockUsageComponent,
    EmailReportsComponent,
    ConfigListItemModalComponent,
    ClaimModalComponent,
    ActualsMetadataModalComponent,
    OrganisationRequestAccessModalComponent,
    ContractDetailsComponent,
    FileUploadComponent,
    FileUploadButtonComponent,
    DeleteFileModalComponent,
    ContractVariationComponent,
    QuestionsComponent,
    QuestionsPageComponent,
    ContractTypesComponent,
    UserPasswordResetModalComponent
  ],

  //Components visible to ng1 should be added here
  providers: [
    FeatureToggleService,
    ReferenceDataService,
    TemplateService,
    UserService,
    AuthService,
    NavigationService,
    SessionService,
    ProjectService,
    ProjectFundingService,
    QuestionService,
    UserPasswordResetModalService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpParamsInterceptor,
      multi: true
    }
  ],
  schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
  // bootstrap: [AppComponent]
})
export class AppModule {
  constructor(private upgrade: UpgradeModule) {
  }

  ngDoBootstrap() {
    this.upgrade.bootstrap(document.body, ['GLA'], {strictDi: true});
  }
}
