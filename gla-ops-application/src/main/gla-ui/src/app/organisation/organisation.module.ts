import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OrganisationSapIdModalComponent} from "./organisation-sap-id-modal/organisation-sap-id-modal.component";
import {SharedModule} from "../shared/shared.module";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {ContractWithdrawModalComponent} from './contracts/contract-withdraw-modal/contract-withdraw-modal.component';
import {TeamMembersComponent} from './team-members/team-members.component';

@NgModule({
  declarations: [
    OrganisationSapIdModalComponent,
    ContractWithdrawModalComponent,
    TeamMembersComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    FormsModule,
    NgbModule
  ]
})
export class OrganisationModule {
}
